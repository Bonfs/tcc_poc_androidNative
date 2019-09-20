package com.pocnative.bonfim.pocnativeandroid

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
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

import kotlinx.android.synthetic.main.activity_pa.*

class PAActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mLocationPermissionGranted = false
    private val FINE_LOCATION: String = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION: String = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private val DEFAULT_ZOOM = 15f

    private lateinit var map: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var isRunning = false
    private var started = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pa)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)

        getLocationPermission()
    }

    override fun onSupportNavigateUp(): Boolean {
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

    private fun initMap() {
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        val permissions = arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)

        if(ContextCompat.checkSelfPermission(this, FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, COURSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true
                initMap()
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
}
