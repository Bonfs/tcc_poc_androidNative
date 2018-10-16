package com.pocnative.bonfim.pocnativeandroid.activity


import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


import com.pocnative.bonfim.pocnativeandroid.R
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.pocnative.bonfim.pocnativeandroid.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task


class ActivityFragment : androidx.fragment.app.Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLocationPermissionGranted = false
    private val FINE_LOCATION: String = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION: String = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private val DEFAULT_ZOOM = 15f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_activity, container, false)
        getLocationPermission()
        return view
    }

    fun initMap(){
        val mapFragment: SupportMapFragment = childFragmentManager?.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap?) {
        if(map != null){
            this.map = map
            if(mLocationPermissionGranted){
                getDeviceLocation()
            }
            map.setOnMyLocationChangeListener{
                val currentUserPosition = LatLng(it.latitude, it.longitude)
                this.map.addMarker(MarkerOptions().position(currentUserPosition).title("You"))
                this.map.moveCamera(CameraUpdateFactory.newLatLng(currentUserPosition))
            }
        }
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        val permissions = arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)

        if( ContextCompat.checkSelfPermission(context!!, FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ){
            if( ContextCompat.checkSelfPermission(context!!, COURSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ){
                mLocationPermissionGranted = true
                initMap()
            } else{
                ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else{
            ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }


    private fun getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        try{
            if(mLocationPermissionGranted){
                val location = mFusedLocationProviderClient.lastLocation
                location.addOnCompleteListener{
                    if(it.isSuccessful){
                        val currentLocation: Location = it.result as Location
                        moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude))
                    }
                }
            }
        } catch (e: SecurityException){
            Log.e("deviceLocation", e.message)

        }

    }

    fun moveCamera(currentUserPosition: LatLng, zoom: Float = DEFAULT_ZOOM){
        this.map.addMarker(MarkerOptions().position(currentUserPosition).title("User Position"))
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition, zoom))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty()){
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

}
