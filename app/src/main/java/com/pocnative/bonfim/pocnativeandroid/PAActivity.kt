package com.pocnative.bonfim.pocnativeandroid

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pocnative.bonfim.pocnativeandroid.models.HeartRate
import com.pocnative.bonfim.pocnativeandroid.models.Pedometer
import com.pocnative.bonfim.pocnativeandroid.utils.CustomBluetoothProfile
import com.pocnative.bonfim.pocnativeandroid.utils.showToast
import kotlinx.android.synthetic.main.activity_pa.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer

class PAActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
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
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser!!.uid
    private val activityDate = Calendar.getInstance()
    private val locations: ArrayList<Map<String, Double>> = arrayListOf()
    private lateinit var activityRef: DatabaseReference
    private lateinit var startLocation: Location
    private lateinit var fixedRateTimer: Timer

    private var elapsedRealtime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pa)

        activityIndicator = ProgressDialog(this)
        activityIndicator.setMessage("Connecting to Device...")
        activityIndicator.setCancelable(false)

        fabPlay.setOnClickListener(::handleStartStop)

        getLocationPermission()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setTitle("Leave Activity")
                .setMessage("You wish leave activity?")
                .setPositiveButton("Yes") { dialog, which ->
                    try {
                        val hours: Int =  (elapsedRealtime / 3600000).toInt()
                        val minutes: Int = ((elapsedRealtime - hours * 3600000) / 60000).toInt()
                        val seconds: Int = ((elapsedRealtime - hours * 3600000 - minutes * 60000) / 1000).toInt()

                        updateActivity(mapOf("finished" to true, "duration" to seconds))
                        fixedRateTimer.cancel()
                        disconnect()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    super.onBackPressed()
                }
                .setNegativeButton("No", null)
                .show()
    }

    private fun handleStartStop(view: View) {
        /**
         * if (isConnectedToMiband) {
                getSteps()
            } else {
                Log.v("onCreate", "wait until connect")
            }
         */
        if (!isRunning) {
            if (started) {
                chronometer.base = SystemClock.elapsedRealtime() - elapsedRealtime
                fixedRateTimer = fixedRateTimer(name = "hello-timer", initialDelay = 100, period = 2000) {
                    Log.d("hello-timer", "step count")
                    getSteps()
                }
            } else
                chronometer.base = SystemClock.elapsedRealtime()
            chronometer.start()
            fabPlay.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.wrong))
            fabPlay.supportBackgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.wrong))
            fabPlay.setImageResource(R.drawable.ic_pause)
        } else {
            fixedRateTimer.cancel()
            elapsedRealtime = SystemClock.elapsedRealtime() - chronometer.base
            /*val hours: Int =  (elapsedRealtime / 3600000).toInt()
            val minutes: Int = ((elapsedRealtime - hours * 3600000) / 60000).toInt()
            val seconds: Int = ((elapsedRealtime - hours * 3600000 - minutes * 60000) / 1000).toInt()
            Log.d("chronometer.base", seconds.toString())*/
            chronometer.stop()
            fabPlay.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.right))
            fabPlay.supportBackgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.right))
            fabPlay.setImageResource(R.drawable.ic_play)
        }

        if (started) {
            this.isRunning = !this.isRunning
        } else {
            startActivity()
            this.isRunning = !this.isRunning
            this.started = true
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun startActivity() {
        this.activityRef = this.database.getReference("debug/users/${uid}/activities").push()

        val date = this.activityDate.time
        val dateFormat = SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
        this.activityRef.setValue(mapOf(
            "started" to this.started,
            "finished" to false,
            "duration" to elapsedRealtime,
            "date" to dateFormat.format(date),
            "locations" to locations,
            "startPosition" to mapOf("latitude" to this.startLocation.latitude, "longitude" to this.startLocation.longitude)
        )).addOnCompleteListener {
            if (it.isSuccessful) {
                // TODO start step counter
                fixedRateTimer = fixedRateTimer(name = "hello-timer", initialDelay = 100, period = 2000) {
                    Log.d("hello-timer", "step count")
                    getSteps()
                }
            }
        }
    }

    private fun updateActivity(updatedValues: Map<String, Any>) = this.activityRef.updateChildren(updatedValues)

    override fun onLocationChanged(location: Location?) {
        if (mLocationPermissionGranted && location !== null) {
            moveCamera(LatLng(location.latitude, location.longitude))
            if (started && isRunning) {
                locations.add(mapOf("latitude" to location.latitude, "longitude" to location.longitude))
                updateActivity(mapOf("locations" to locations))
                tvDistance.text = "${String.format("%.2f", calcDistance())} KM"
            }
        }
    }

    private fun calcDistance(): Double {
        var distance = 0.0
        if (locations.size == 0) return distance

        for ((index, value) in locations.withIndex()) {
            if(index + 1 < locations.size ) {
                val start = Location("")
                val dest = Location("")

                start.latitude = value["latitude"] ?: error("")
                start.longitude = value["longitude"] ?: error("")

                dest.latitude = locations[index+1]["latitude"] ?: error("")
                dest.longitude = locations[index+1]["longitude"] ?: error("")


                distance += start.distanceTo(dest)
            }
        }

        Log.d("calcDistance", (distance / 1000).toString())
        return distance / 1000
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    /**
     * Requisita permissão de localização para o usuário
     */
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
        /*runOnUiThread {
            this.activityIndicator.hide()
        }*/
        // getSteps()
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

    /**
     * inicia o mapa
     */
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
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_COARSE
        criteria.powerRequirement = Criteria.POWER_HIGH
        criteria.isAltitudeRequired = false
        criteria.isBearingRequired = false

        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val provider = locationManager.getBestProvider(criteria, true)
        try {
            if(mLocationPermissionGranted) {
                locationManager.requestLocationUpdates(provider, 1L, 5f, this)
                /*locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1L, 1f, this)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1L, 1f, this)*/

                val location = mFusedLocationProviderClient.lastLocation
                // val location: Location = locationManager.getLastKnownLocation()
                location.addOnCompleteListener {
                    if(it.isSuccessful) {
                        val currentLocation: Location = it.result as Location
                        this.startLocation = currentLocation
                        locations.add(mapOf("latitude" to currentLocation.latitude, "longitude" to currentLocation.longitude))
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
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentUserPosition, zoom)
        this.map.animateCamera(cameraUpdate)
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
                if (initialSteps == -1)
                    initialSteps = steps.steps

                val currentSteps = steps.steps - initialSteps

                runOnUiThread {
                    tvSteps.text = currentSteps.toString()
                }

                updateActivity(mapOf("steps" to currentSteps))


                Log.d("onCharacteristicRead", currentSteps.toString())

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
