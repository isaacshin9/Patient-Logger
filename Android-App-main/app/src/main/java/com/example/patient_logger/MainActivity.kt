package com.example.patient_logger

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.patient_logger.fragments.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), CommDataSensorManagement {

    private val TAG = "MainActivity"

    // Constants/config parameters
    private val SENSOR_PACK_BLE_NAME =
        "ArduSensor"// used to differentiate the devices from Arduino's
    private var device_addr_one = "some_address" //the address of one device

    private var REQUEST_ENABLE_BT = 1 //bluetooth must be enabled
    private val SCAN_PERIOD: Long = 3000 //3 secs

    // Bluetooth setup variables
    private var mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
    private var scanning = false //to account for scanning
    private val handler = Handler()

    //we have values entered into these lists representing all the devices scanned, and those that have "ArduSensor"
    private val scannedDevices: HashMap<String?, BluetoothDevice> =
        HashMap() //contains all scanned devices
    private val arduSensorDevices: HashMap<String, BluetoothDevice> =
        HashMap() //contains only arduinos

    //variables to allow us to use the BLE service:
    private var mService: BluetoothLeService? = null
    private var mBound: Boolean = false


    // Optional to hold the Binding used to communicate with the DB service
    private var dbServiceBinding: DataManager.DataManagerBinder? = null

    private val homeFragment = HomeFragment()
    private val statisticsFragment = StatisticsFragment()
    private val profileFragment = ProfileFragment()
    private val exportFragment = ExportFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        replaceFragment(homeFragment)

        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_home -> replaceFragment(homeFragment)
                R.id.ic_statistics -> replaceFragment(statisticsFragment)
                R.id.ic_profile -> replaceFragment(profileFragment)
                R.id.ic_export -> replaceFragment(exportFragment)
                R.id.ic_settings -> replaceFragment(settingsFragment)
            }
            true
        }

        // Start the data management service
        Log.v(TAG, "Attempting to start the data management service...")
        val startServiceIntent = Intent(this, DataManager::class.java)
        startService(startServiceIntent)

        // Attempt to bind to the data management service
        bindToDataManageService()

        //attempt to bind to BLE service
        Intent(this, BluetoothLeService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        // Enable Bluetooth if not already -- Bluetooth check:
        if (!mBluetoothAdapter.isEnabled) {
            Log.v(TAG, "[INFO] Bluetooth in not enabled. Requesting Bluetooth enable from user...")

            // Will ask the user to turn on Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(
                enableBtIntent,
                REQUEST_ENABLE_BT
            ) //starting the Bluetooth Service
        } else {
            Log.v(TAG, "[INFO] Bluetooth is already enabled!")
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    override fun passDataCom(sensor1: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("message", sensor1)

        val transaction = this.supportFragmentManager.beginTransaction()
        homeFragment.arguments = bundle

        transaction.commit()

    }

    //to handle the enable bluetooth request if the user doesn't have it enabled:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle result from asking user to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "[INFO] Attempt to enable Bluetooth was successful")
            } else {
                Log.v(TAG, "[WARNING] Attempt to enable Bluetooth was UNSUCCESSFUL!")
                onStop()
            }
        }
    }

    // Function that provides an easy single-call way to bind to the data management service
    private fun bindToDataManageService() {
        Log.v(TAG, "Attempting to bind to the data management service...")
        val bindDbServiceIntent = Intent(this, DataManager::class.java)
        val dbServiceConnection = DataManageServiceConnection()
        bindService(bindDbServiceIntent, dbServiceConnection, 0)
    }

    // [DEBUG] Function to help debug data management service communication
    fun sendSQL(view: android.view.View) {
        // Attempt to execute using the DataManager service
        val result = dbServiceBinding?.recordQualitativeValue(
            "testuser",
            "unknown",
            10,
            "headache"
        )
    }

    // Class that handles return of scans and sets them into the appropriate maps:
    private var leScanCallback: ScanCallback? = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            scannedDevices[result.device.address.toString()] = result.device
            val name = scannedDevices[result.device?.address]?.name // name of the scanned device
            // If a sensor pack is found, add to the specific table of sensor packs:
            if (result.device.name == SENSOR_PACK_BLE_NAME && result.device != arduSensorDevices[result.device.address.toString()] && result.device != null) {
                arduSensorDevices[result.device.address.toString()] =
                    result.device // placing the device addresses into the hashmap
                Log.v(
                    TAG,
                    "[INFO] Found a sensor pack: " + result.device.name + " at: " + result.device.address + " with RSSI: " + result.rssi
                )
                //allows us to use the device addresses found for binding and connection
                device_addr_one =
                    arduSensorDevices[result.device.address.toString()].toString() //assigns the address of the arduino to the address variable for connection (1st device in map)
                // device_addr_two = arduSensorDevices[result.device.address.toString() + 1].toString() ////assigns the address of the arduino to the address variable for connection (2nd device in map)
            }
        }
    }

    //a connection to the BLE service to pass data:
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BluetoothLeService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    //to scan for BLE Peripherals, connect and bind, and read out characteristics -- one button:
    fun runScan(view: View) {
        scanLeDevice()
    }

    // Function that scans for available BLE devices and displays contents of hashmaps
    private fun scanLeDevice() {
        if (!scanning) {
            Log.v(TAG, "[INFO] Will attempt to start scanning...")
            handler.postDelayed({
                Log.v(TAG, "[INFO] Will attempt to automatically stop scanning...")

                // Stop scanning
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)

                // Log that devices were found in the scan
                Log.v(
                    TAG,
                    "[INFO] Got results from scan (all BLE Devices): " + scannedDevices.keys.toString() + " Number of Devices: " + scannedDevices.keys.size
                )
                Log.v(
                    TAG,
                    "[INFO] Found sensor packs (Arduino's): " + arduSensorDevices.keys + " Number of Arduino's: " + arduSensorDevices.keys.size
                )

                //automatically connecting and binding to GATT Server with detected BLE Devices:
                mService?.gatherDeviceAddresses(device_addr_one) //will pass the address of the first device to the service
                mService?.bindToBluetooth() //will bind to the Device/GATT with said device

            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            Log.v(TAG, "[INFO] Will attempt to manually stop scanning...")
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    // ServiceConnection implementation that manages the connection to the data management Service
    inner class DataManageServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Cast as a DataManagementBinder (all it should be)
            val dataManagerServiceBinder = service as DataManager.DataManagerBinder

            // Set the outer binding variable
            dbServiceBinding = dataManagerServiceBinder

            // Call a function in DataManager
//            val testResultCursor: Cursor? = dataManagerServiceBinder.runSQL("SELECT * FROM raw_sensor")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            TODO("Implement onServiceDisconnected for DbServiceConnection")
        }

    }
}