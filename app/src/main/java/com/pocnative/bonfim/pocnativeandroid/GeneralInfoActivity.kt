package com.pocnative.bonfim.pocnativeandroid

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pocnative.bonfim.pocnativeandroid.models.PhysicalActivity
import kotlinx.android.synthetic.main.activity_historic_detail.*

class GeneralInfoActivity : AppCompatActivity() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val uid: String = FirebaseAuth.getInstance().currentUser!!.uid
    private val physicalActivities: ArrayList<PhysicalActivity> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_info)

        toolbar.title = "Steps"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)



        getPhysicalActivitiesFromFB()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun getPhysicalActivitiesFromFB() {
        val paRef = database.getReference("debug/users/${uid}/activities")


        paRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    val physicalActivity: PhysicalActivity? = it.getValue(PhysicalActivity::class.java)

                    if (physicalActivity !== null) physicalActivities.add(physicalActivity)
                }
            }
        })
    }

    private fun calcDistance(locations: ArrayList<Map<String, Double>>): Double {
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

    private fun calcCalories(duration: Long): Double {
        val weight: Long = intent.getLongExtra("weight", 0)
        val durationInMinutes = duration / 60
        val caloriesSpent = (0.5 * weight) * durationInMinutes

        return caloriesSpent
    }
}
