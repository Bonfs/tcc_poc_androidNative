package com.pocnative.bonfim.pocnativeandroid.activity


import android.os.Bundle
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
class ActivityFragment : androidx.fragment.app.Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_activity, container, false)
        val mapFragment: SupportMapFragment = childFragmentManager?.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(map: GoogleMap?) {
        if(map != null){
            this.map = map

            val sydney = LatLng(-34.0, 151.0)
            this.map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            this.map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }
}
