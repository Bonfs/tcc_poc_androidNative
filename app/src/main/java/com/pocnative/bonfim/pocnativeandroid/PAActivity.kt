package com.pocnative.bonfim.pocnativeandroid

import android.app.ProgressDialog
import android.bluetooth.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.pocnative.bonfim.pocnativeandroid.models.HeartRate
import com.pocnative.bonfim.pocnativeandroid.models.Pedometer
import com.pocnative.bonfim.pocnativeandroid.utils.CustomBluetoothProfile
import com.pocnative.bonfim.pocnativeandroid.utils.showToast

import kotlinx.android.synthetic.main.activity_pa.*
import kotlinx.android.synthetic.main.content_ble.*
import java.util.*

class PAActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mLocationPermissionGranted = false
    private val MIBAND_ADDRESS = "E7:75:2F:8B:C4:98"
    private val FINE_LOCATION: String = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION: String = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private val DEFAULT_ZOOM = 15f

    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothGatt: BluetoothGatt
    private lateinit var map: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var activityIndicator: ProgressDialog
    private var isRunning = false
    private var started = false
    private var isConnectedToMiband = false
    private var isListeningHeartRate = false
    private var initialSteps = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pa)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)

        activityIndicator = ProgressDialog(this)
        activityIndicator.setMessage("Connecting to Device...")
        activityIndicator.setCancelable(false)
//        activityIndicator.show()

        fabPlay.setOnClickListener {
            if (isConnectedToMiband) {
                getSteps()
            } else {
                Log.v("onCreate", "wait until connect")
            }
        }

        getLocationPermission()
    }

    override fun onSupportNavigateUp(): Boolean {
        disconnect()
        onBackPressed()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            map = googleMap
            if (mLocationPermissionGranted) {
                getDeviceLocation()
                this.map.isMyLocationEnabled = true
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty()) {
                    grantResults.forEach { result ->
                        if(result != PackageManager.PERMISSION_DENIED){
                            mLocationPermissionGranted = false
                            Log.d("onRequestPermission", "Permission failed")
                            return
                        }
                    }
                    Log.d("onRequestPermission", "Permission granted")
                    mLocationPermissionGranted = false
                    initMap()
                }
            }
        }
    }

    private fun connect() {
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(MIBAND_ADDRESS)

        Log.v("PAA:connect", "Connecting to ${MIBAND_ADDRESS}")
        Log.v("PAA:connect", "Device name ${bluetoothDevice.name}")

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback)
    }

    private fun hasConnected() {
        bluetoothGatt.discoverServices()
        isConnectedToMiband = true
        // this.activityIndicator.hide()
//        getSteps()
        Log.d("hasConnected", "Connected to Miband")
//        this.showToast("Connected to Miband")
    }

    private fun disconnect() {
        bluetoothGatt.disconnect()
    }

    private fun getSteps() {
        try {
            val bchar: BluetoothGattCharacteristic = bluetoothGatt.getService(CustomBluetoothProfile.Basic().service)
                    .getCharacteristic(CustomBluetoothProfile.Pedometer().characteristicSteps)
            // The characteristic can be read in BluetoothGattCallback.onCharacteristicRead...
            if (!bluetoothGatt.readCharacteristic(bchar)) {
                this.showToast("Failed get pedometer info")
            }
        } catch (e: Exception) {
            Log.d("getSteps", e.message)
            e.printStackTrace()
        }
    }

    private fun initMap() {
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // get map permissions and connect() to miband
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        val permissions = arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)

        if(ContextCompat.checkSelfPermission(this, FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, COURSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                // granted permissions
                // activityIndicator.show()
                mLocationPermissionGranted = true
                initMap()
                connect()
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            if(mLocationPermissionGranted) {
                val location = mFusedLocationProviderClient.lastLocation
                location.addOnCompleteListener {
                    if(it.isSuccessful) {
                        val currentLocation: Location = it.result as Location
                         moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude))
                    }
                }
            }
        } catch (e: SecurityException){
            Log.e("deviceLocation", e.message)
            e.printStackTrace()
        }
    }

    private fun moveCamera(currentUserPosition: LatLng, zoom: Float = DEFAULT_ZOOM) {
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition, zoom))
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.v("bluetoothGattCallback", "onConnectionStateChange")

            if(newState == BluetoothProfile.STATE_CONNECTED) {
                hasConnected()
            } else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnect()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.v("test", "onServicesDiscovered")
//            listenHeartRate()
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.v("test", "onCharacteristicRead")
            val data = characteristic.value
            Log.d("onCharacteristicRead", characteristic.uuid.toString())
            if (characteristic.uuid == CustomBluetoothProfile.Pedometer().characteristicSteps) {
                Log.d("onCharacteristicRead", "pedometer")
                val steps = Pedometer(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1).toInt())
                Log.d("onCharacteristicRead", steps.steps.toString())

                if (data.size >= 8) steps.distance = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 5).toInt()
                if (data.size >= 12) steps.calories = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 9).toInt()
                // tvByte.text = "Pedometer: ${steps.steps}"
            } else if (characteristic.uuid == CustomBluetoothProfile.Basic().batteryCharacteristic /*"00000006-0000-3512-2118-0009af100700"*/) {
                Log.d("onCharacteristicRead", "battery")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.v("test", "onCharacteristicWrite")
        }

        // listen heart rate changes
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.v("test", "onCharacteristicChanged")
            //  if (characteristic.uuid.toString() == "00002a37-0000-1000-8000-00805f9b34fb") {
            if (characteristic.uuid == CustomBluetoothProfile.HeartRate().measurementCharacteristic /*"00002a37-0000-1000-8000-00805f9b34fb"*/) {
                Log.d("onCharacteristicChanged", "heart rate")
                val data = characteristic.value
                val heartRate = HeartRate(data[1].toInt())
                // tvByte.text = "Frequence: ${heartRate.rate}"
            } else {
                val data = characteristic.value
                // tvByte.text = Arrays.toString(data)
            }
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
            Log.v("test", "onDescriptorRead")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.v("test", "onDescriptorWrite")
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
            Log.v("test", "onReliableWriteCompleted")
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            Log.v("test", "onReadRemoteRssi")
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.v("test", "onMtuChanged")
        }
    }
}
