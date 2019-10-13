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
import kotlinx.android.synthetic.main.activity_general_info.*
import kotlinx.android.synthetic.main.activity_historic_detail.toolbar
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.pocnative.bonfim.pocnativeandroid.models.ChartType


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

        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(true)
        chart.description.isEnabled = false

        stepsContainer.setOnClickListener {
            setChart(ChartType.STEPS)
        }

        durationContainer.setOnClickListener {
            setChart(ChartType.DURATION)
        }

        caloriesContainer.setOnClickListener {
            setChart(ChartType.CALORIES)
        }

        distanceContainer.setOnClickListener {
            setChart(ChartType.DISTANCE)
        }
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

                Log.d("GeneralInfoActivity", physicalActivities.size.toString())
                setChart(ChartType.STEPS)
            }
        })
    }

    private fun setChart(type: ChartType) {
        tvTitle.text = type.toString()
        toolbar.title = type.toString().capitalize()
        val barEntries: ArrayList<BarEntry> = arrayListOf()
        when (type) {
            ChartType.STEPS -> {
                var cont = 1F
                physicalActivities.forEach {
                    barEntries.add(BarEntry(cont, it.steps.toFloat()))
                    cont++
                }
            }
            ChartType.DURATION -> {
                var cont = 1F
                physicalActivities.forEach {
                    barEntries.add(BarEntry(cont, it.duration.toFloat()))
                    cont++
                }
            }
            ChartType.CALORIES -> {
                var cont = 1f
                physicalActivities.forEach {
                    barEntries.add(BarEntry(cont, calcCalories(it.duration).toFloat()))
                    cont++
                }
            }
            ChartType.DISTANCE -> {
                var cont = 1f
                physicalActivities.forEach {
                    barEntries.add(BarEntry(cont, calcDistance(it.locations).toFloat()))
                    cont++
                }
            }
        }

        val barDataSet: BarDataSet = BarDataSet(barEntries, type.toString().capitalize())
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        val barData = BarData(barDataSet)
        barData.barWidth = 0.5f
        chart.data = barData
        chart.invalidate()
        /*val days: Array<String> = arrayOf("03/09", "04/09", "05/09", "06/09", "07/09", "08/09", "09/09", "10/09", "11/09", "12/09")
        val xAxisFormatter = MyXAxisFormater(days)

        val xAxis = chart.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = xAxisFormatter*/
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
