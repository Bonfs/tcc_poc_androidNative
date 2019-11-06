package com.pocnative.bonfim.pocnativeandroid.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.pocnative.bonfim.pocnativeandroid.R
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.location.Location
import android.widget.Button
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pocnative.bonfim.pocnativeandroid.PAActivity


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
        view.findViewById<Button>(R.id.btnStart).setOnClickListener {
            val intent = Intent(context, PAActivity::class.java)
            activity?.startActivity(intent)
        }
        getLocationPermission()
        return view
    }

    private fun initMap() {
        val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap?) {
        if(map != null){
            this.map = map
            if(mLocationPermissionGranted) {
                getDeviceLocation()

                if(ActivityCompat.checkSelfPermission(context!!, FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context!!, COURSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return
                }

                this.map.isMyLocationEnabled = true
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

        if(ContextCompat.checkSelfPermission(context!!, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(context!!, COURSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true
                initMap()
            } else {
                ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else{
            ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }



    private fun getDeviceLocation() {
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

    private fun moveCamera(currentUserPosition: LatLng, zoom: Float = DEFAULT_ZOOM) {
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
