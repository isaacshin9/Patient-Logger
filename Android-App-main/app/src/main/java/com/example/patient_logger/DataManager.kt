package com.example.patient_logger

import android.app.Service
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Binder
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import android.util.Log

class DataManager : Service() {
    private lateinit var dbHandlerThread: HandlerThread

    // Database properties
    private var backingDb: SQLiteDatabase? = null
    private val sensorValuesTableName: String = "raw_sensor"
    private val userQualitativeTableName: String = "user_qualitative"

    private val TAG = "DataManger"

    override fun onCreate() {
        super.onCreate()

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        dbHandlerThread = HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND)

        // Start the thread
        dbHandlerThread.apply {
            start()
        }

        // Run database setup code in the thread
        dbHandlerThread.looper.queue.apply {
            // Log that the service has been started in the background
            Log.v(TAG, "[DEBUG] Starting thread in the background...")

            // Create or connect to the backing DB
            backingDb = openOrCreateDatabase("backingDB.db",MODE_PRIVATE,null)

            // Log that the database (should [have]) has been created
            Log.v(TAG, "[DEBUG] Backing DB has been opened or created")

            // Create database tables (if they don't already exist)
            backingDb?.execSQL(
                "CREATE TABLE IF NOT EXISTS $sensorValuesTableName (\n" +
                        "    reading_id INTEGER PRIMARY KEY NOT NULL,\n" +
                        "    sensor_id TEXT NOT NULL,\n" +
                        "    timestamp TEXT NOT NULL,\n" +
                        "    q1 REAL DEFAULT NULL,\n" +
                        "    q2 REAL DEFAULT NULL,\n" +
                        "    q3 REAL DEFAULT NULL,\n" +
                        "    q4 REAL DEFAULT NULL\n" +
                        ");")

            backingDb?.execSQL(
                "CREATE TABLE IF NOT EXISTS $userQualitativeTableName (\n" +
                        "    reading_id INTEGER PRIMARY KEY NOT NULL,\n" +
                        "    user_id TEXT NOT NULL,\n" +
                        "    timestamp TEXT NOT NULL,\n" +
                        "    pain_level REAL DEFAULT NULL,\n" +
                        "    symptoms TEXT DEFAULT NULL\n" +
                        ");")

//            // [DEBUG] Insert some test values
//            backingDb?.execSQL(
//                "INSERT INTO $sensorValuesTableName (timestamp, q1, q2, q3, q4) VALUES('${0}', '${0}', '${0}', '${0}', '${0}');"
//            )
        }

//        // [DEBUG] Test query the database using the thread
//        dbHandlerThread.looper.queue.apply {
//            // Check result values
//            val resultSet: Cursor? = backingDb?.rawQuery(
//                "SELECT * FROM $sensorValuesTableName", null
//            )
//            resultSet?.moveToFirst()
//            val testTimestamp = resultSet?.getString(0)
//            val testValue = resultSet?.getString(1)
//            resultSet?.close()
//            Log.v(TAG, "[INFO] Created table and got back timestamp: $testTimestamp value: $testValue")
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Log the fact that the service is being destroyed
        Log.v(TAG, "[DEBUG] Destroying DataManager service")

        // Clean up and close the database if it exists
        backingDb?.close()

        // Stop the DB handler thread
        dbHandlerThread.quit()

        // Log that the database connection has been closed
        Log.v(TAG, "[DEBUG] The database connection has been closed")
    }

    override fun onBind(intent: Intent): IBinder {
        return DataManagerBinder()
    }

    inner class DataManagerBinder : Binder() {
        fun getService(): DataManager = this@DataManager

        // Run arbitrary SQL and return a DB cursor
        // NOTE: It's not safe to use this for anything other than testing
        fun runSQL(statement: String): Cursor? {
            var resultCursor: Cursor?
            dbHandlerThread.looper.queue.apply {
                // Check result values
                val resultSet: Cursor? = backingDb?.rawQuery(statement, null)
                resultSet?.moveToFirst()

                resultCursor = resultSet
            }

            return resultCursor
        }

        // Record a sensor reading
        fun recordSensorReading(sensorId: String, timestamp: String, q1: Float, q2: Float, q3: Float, q4: Float) {
            // Insert the given values into the database
            backingDb?.execSQL(
                "INSERT INTO $sensorValuesTableName (sensor_id, timestamp, q1, q2, q3, q4) VALUES('${sensorId}', '${timestamp}', '${q1}', '${q2}', '${q3}', '${q4}');"
            )
        }

        // Record a user qualitative value
        fun recordQualitativeValue(userId: String, timestamp: String, painLevel: Number, symptoms: String) {
            // Insert the given values into the database
            backingDb?.execSQL(
                "INSERT INTO $userQualitativeTableName (user_id, timestamp, pain_level, symptoms) VALUES('${userId}', '${timestamp}', '${painLevel}', '${symptoms}');"
            )

            // [DEBUG] Verify that the item has been inserted
            dbHandlerThread.looper.queue.apply {
                // Query DB
                val resultSet: Cursor? = backingDb?.rawQuery(
                    "SELECT user_id, timestamp, pain_level, symptoms FROM $userQualitativeTableName", null
                )
                resultSet?.moveToLast()
                val testUserId = resultSet?.getString(0)
                val testTimestamp = resultSet?.getString(1)
                val testPain = resultSet?.getFloat(2)
                val testSymptoms = resultSet?.getShort(3)
                resultSet?.close()

                Log.v(TAG, "[INFO] Moved to last and got back user_id: $testUserId, timestamp: $testTimestamp, pain: $testPain, symptoms: $testSymptoms")
            }
        }

    }
}