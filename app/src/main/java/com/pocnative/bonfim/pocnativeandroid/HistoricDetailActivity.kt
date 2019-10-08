package com.pocnative.bonfim.pocnativeandroid

import android.content.Intent
import android.graphics.PorterDuff
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pocnative.bonfim.pocnativeandroid.models.PhysicalActivity
import com.pocnative.bonfim.pocnativeandroid.profile.model.User
import com.pocnative.bonfim.pocnativeandroid.utils.toActivity
import kotlinx.android.synthetic.main.activity_historic_detail.*

class HistoricDetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var physicalActivity: PhysicalActivity
    private lateinit var user: User
    private val uid: String = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historic_detail)

        val mIntent = intent
        physicalActivity = mIntent.getSerializableExtra("phisycalACtivity") as PhysicalActivity
        Log.d("onCreate", physicalActivity.date)

        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("debug/users/${uid}/infos")
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                user = dataSnapshot.getValue(User::class.java)!!
                tvCalories.text = "${String.format("%.2f", calcCalories())}"
            }

        })

        toolbar.title = "Walking"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        btnInfo.setOnClickListener {
            val intent = Intent()
            intent.putExtra("weight", user.weight)
            this.toActivity(GeneralInfoActivity::class.java, intent)
        }

        tvSteps.text = physicalActivity.steps.toString()
        tvDuration.text = "${physicalActivity.duration.toString()} sec"
        tvDistance.text= "${String.format("%.2f", calcDistance())}"

        initMap()
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        if (physicalActivity.locations.isNotEmpty()) {
            moveCamera(
                LatLng(physicalActivity.startPosition["latitude"]!!,
                        physicalActivity.startPosition["longitude"]!!))
            drawRoute()
        }
    }

    private fun initMap() {
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun moveCamera(positon: LatLng, zoom: Float = 12f) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(positon, zoom)
        this.map.animateCamera(cameraUpdate)
    }

    private fun drawRoute() {
        val polyline: Polyline = this.map.addPolyline(
                PolylineOptions()
                    .clickable(false)
                    .addAll(getAllLatLng())

        )
        polyline.color = resources.getColor(R.color.colorPrimary)
    }

    private fun getAllLatLng(): ArrayList<LatLng> {
        val locations: ArrayList<LatLng> = arrayListOf()
        physicalActivity.locations.forEach{
            locations.add(LatLng(it["latitude"]!!, it["longitude"]!!))
        }

        return  locations
    }

    private fun calcCalories(): Double {
        val durationInMinutes = physicalActivity.duration / 60
        val caloriesSpent = (0.5 * user!!.weight) * durationInMinutes

        return caloriesSpent
    }

    private fun calcDistance(): Double {
        var distance = 0.0
        val locations = physicalActivity.locations
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
