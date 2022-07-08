package com.example.patient_logger

import android.app.Service
import android.bluetooth.*
import android.content.*
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BluetoothLeService : Service() { //for the bluetoothLE service


    private val TAG: String = MainActivity::class.java.getSimpleName()

    //binder given to clients:
    private val binder = LocalBinder()

    //the adapter to be used:
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null //creating a bluetooth gatt instance
    private var connectionState = BluetoothProfile.STATE_DISCONNECTED //we always start off disconnected

    //GATT specific variables:
    private val SENSOR_PACK_UUID: UUID = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1215")
    private val CLIENT_CHARACTERISTIC_CONFIG: String = "00002902-0000-1000-8000-00805f9b34fb"
    private var mConnectedSensorPackCharacteristic: BluetoothGattCharacteristic? = null
    private var device_address = "some address"

    //creating a map for the gatt objects (for more than one device)
    private val connectedGattDevices: HashMap<String, BluetoothGatt> = HashMap()
    //private val arduDevices: MutableSet<BluetoothGatt> = MutableSet()

    //GATT Server: considered to be the BLE Peripheral which holds Generic Attribute Protocol definitions
    //GATT Client: the device which receives responses from the GATT Server, the client is the tablet

    //set up for the bound service - using the binder created
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    //to disconnect from the bound service
    override fun onUnbind(intent: Intent?): Boolean {
        disconnect() //may change to onDestroy if we are using a service
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    //A function to gather keys and place them into a new set, or take values one by one:
    fun gatherDeviceAddresses(address: String){
        device_address = address
    }

    // BLE GATT Items
    private var bluetoothService : BluetoothLeService? = null
    //manages the service lifestyle:
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                // Call functions on service to check connection and connect to devices: explicitly done here: via two functions
                if (!bluetooth.initialize()) { //initializing bluetooth service
                    Log.e(TAG, "Unable to initialize Bluetooth!")
                    stopSelf()
                    //finish() //end the activity
                }
                // Perform device connection to gatt server hosted by the BLE device
                bluetooth.connect(device_address)
                // Reading Characteristics of Service:
                bluetoothService?.debugReadCharacteristic()

//              //to perform device connection for another device:
//              bluetooth.connect(device_addr_two)

            }
        }
        //when the bluetooth service is disconnected:
        override fun onServiceDisconnected(componentName: ComponentName?) {
            bluetoothService = null
        }
    }

    //function that allows us to connect and bind to the GATT server using a new GATT Intent:
    fun bindToBluetooth() {
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE)
    }


    //Once the service is bound to, it needs to access the BluetoothAdapter.. to be called in service connection
    fun initialize(): Boolean {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter() //getting a handle on the bluetooth adapter

        //check if the adapter is available on the device
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter! (Bluetooth unavailable on this device)")
            return false
        } else {
            Log.v(TAG, "[INFO] Bluetooth available on this device")
        }

        return true
    }

    //connecting to a device .. to be called in service connection
    fun connect(address: String): Boolean {

        // Disconnect to a BLE peripheral
        mBluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address) //getting the device address to be used to connect to

                //Disconnect from other device if already connected
                //this.disconnect()

                // Connect to the GATT server on the device
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                connectionState = STATE_CONNECTING   //change the connection state to connected since the device has connected successfully

                return true

            } catch (exception: IllegalArgumentException) { //if getRemoteDevice() cannot get a device with the particular address
                Log.w(TAG, "Device not found with provided address. Unable to connect!")
                return false
            }
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized!")
            return false
        }
    }

    private fun disconnect() {
        // Disconnect from the BLE peripheral
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }

    //recieves the broadcast intent in
    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        // Note: This is an asynchronous call! That's why it doesn't directly return anything.
        return bluetoothGatt?.services
    }

    //reading the characteristics from the device (a list) and passing them to BluetoothGattCharacteristic
    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        // Note: This is an asynchronous call! That's why it doesn't directly return anything.

        bluetoothGatt?.let { gatt ->
            gatt.readCharacteristic(characteristic)
        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized!")
            return
        }
    }

    //for debug and display purposes (characteristic wise)
    fun debugReadCharacteristic() {
        mConnectedSensorPackCharacteristic?.let { readCharacteristic(it) }
    }

    //Enable or disable notifications/indications for a given characteristic on a BLE device
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        var success: Boolean = false;
        bluetoothGatt?.let { gatt ->
            // Enable updates for this characteristic
            success = gatt.setCharacteristicNotification(characteristic, enabled) //where we can enable and disable notifications for a characterisitc

            // Needed to actually _receive_ the updates -- after getting the characteristic from the service
            val descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor) //Writes a given characteristic and its values to the associated remote device.
        }

        Log.v(TAG, "Attempt to set notifications to $enabled returned $success for characteristic ${characteristic.toString()}")
    }

    //notifying the activity of the new state to the GATT server: (broadcasting the new state)
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action) //can contain CONNECTED or DISCONNECTED
        sendBroadcast(intent) //sending connection state to reciever
    }

    //process of performing service discovery once connected to the gatt server
    private var bluetoothGattCallback = object : BluetoothGattCallback() {
        //triggered when the connection to the device’s GATT server changes -- as well as services discovered
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
//            super.onConnectionStateChange(gatt, status, newState)

            //to get the gatt object and enter it into the GATT map:
            //var device = gatt?.device
            //var address = device?.address

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Successfully connected to the GATT server
                Log.v(TAG, "[INFO] Connected to the GATT server")

                // Broadcast update to confirm connection to GATT Server:
                connectionState = STATE_CONNECTED
                broadcastUpdate(ACTION_GATT_CONNECTED)

                // Attempt to discover services
                // Note: this runs in the background. They won't be immediately available!
                bluetoothGatt?.discoverServices()
            }

            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Disconnected from the GATT server
                Log.v(TAG, "[INFO] Disconnected from the GATT server")

                // Broadcast update to confirm the disconnect from the GATT Server:
                connectionState = STATE_DISCONNECTED
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
            }
        }

        //Service: A service can have one or more characteristics, used to break up logic entities, comes with unique UUID
        //Characteristic: Single data points that encompass a service

        //discovers services and their corresponding characteristics:
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) { //services have been successfully discovered
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                Log.v(TAG, "[INFO] Found services: " + bluetoothGatt?.services)

                // Record information that will be used throughout service/characteristic discovery process -- displaying the GATT Services for the device
                val gattServices = bluetoothGatt?.services
                var uuid: String? //a uuid to be generated once a service is found

                // [TODO] Use these lists instead of hardcoded services/characteristics
                val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf() //create a mutable list containing services
                val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> = mutableListOf() //a list of characteristic data for the service

                if (gattServices != null) { //as long as we have a gatt service for that device:

                    // Loop through services
                    gattServices.forEach { gattService ->
                        val currentServiceData = HashMap<String, String>()
                        uuid = gattService.uuid.toString() //getting the uuid for the gattservice
                        Log.v(TAG, "[INFO] Got uuid for service: $uuid")
                        val gattCharacteristics = gattService.characteristics //gathering characteristics for this unique service

                        // Loop through characteristics
                        gattCharacteristics.forEach { gattCharacteristic ->
                            uuid = gattCharacteristic.uuid.toString() //a uuid for the given characteristic of the unique service
                            Log.v(TAG, "[INFO] Got uuid for characteristic: $uuid")

                            // [DEBUG] Log Descriptors for this characteristic
//                            gattCharacteristic.descriptors.forEach { descriptor ->
//                                Log.e(TAG, "[IMPORTANT] BluetoothGattDescriptor: "+descriptor.getUuid().toString());
//                            }

                            // [DEBUG] Enable settings for "special" characteristics
                            if (uuid == SENSOR_PACK_UUID.toString()) {
                                mConnectedSensorPackCharacteristic = gattCharacteristic

                                // Set up notifications for the characteristic
                                setCharacteristicNotification(gattCharacteristic, true)
                            }
                        }
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        //Callback reporting the result of a characteristic read operation.
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate() //do we update the server on a successful characteristic read?

                // Writing data formatted in HEX:
                val data: ByteArray? = characteristic.value
                if (data?.isNotEmpty() == true) {
                    val hexString: String = data.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    Log.v(TAG, "Characteristic read value: 0x$hexString")
                } else {
                    Log.w(TAG, "Got no data from characteristic!")
                }
            }
        }
        //Callback triggered as a result of a remote characteristic notification.
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)

            // Print in hex
            val data: ByteArray? = characteristic.value
            if (data?.isNotEmpty() == true) {
                val hexString: String = data.joinToString(separator = " ") {
                    String.format("%02X", it)
                }
                Log.v(TAG, "Value: 0x$hexString")
            } else {
                Log.w(TAG, "Got no data from characteristic!")
            }
        }
    }

    // For broadcasting GATT events
    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2
        private const val STATE_CONNECTING = 1
    }
}
////to be implemented for multiple devices:
//class MyBroadCastReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        if (ConnectivityManager.CONNECTIVITY_ACTION == intent!!.action) {
//            val noConnectivity: Boolean = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
//            if (noConnectivity) {
//                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//}