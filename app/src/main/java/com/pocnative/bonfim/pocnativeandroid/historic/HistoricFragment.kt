package com.pocnative.bonfim.pocnativeandroid.historic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.pocnative.bonfim.pocnativeandroid.R
import com.pocnative.bonfim.pocnativeandroid.adapters.HistoricAdapter
import com.pocnative.bonfim.pocnativeandroid.models.PhysicalActivity

class HistoricFragment : androidx.fragment.app.Fragment() {
    private lateinit var rvHistoric: RecyclerView
    private lateinit var viewAdapter: HistoricAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val database = FirebaseDatabase.getInstance()
    private val uid: String? = FirebaseAuth.getInstance().currentUser?.uid
    private val activities: ArrayList<PhysicalActivity> = arrayListOf()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_historic, container, false)

        viewManager = LinearLayoutManager(context)
        viewAdapter = HistoricAdapter(activities, context!!)

        rvHistoric = view.findViewById<RecyclerView>(R.id.rvHistoric).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        getPhysicalActivityFromFirebase()

        return view
    }

    private fun getPhysicalActivityFromFirebase() {
        val paRef = database.getReference("debug/users/${uid}/activities")


        paRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    Log.d("getPhysicalActivity", "aaaaaaaaaaa")
                    val physicalActivity: PhysicalActivity? = it.getValue(PhysicalActivity::class.java)

                    if (physicalActivity !== null) activities.add(physicalActivity)
                }

                viewAdapter.notifyDataSetChanged()
            }
        })
    }

    companion object {
        @JvmStatic
        fun getInstance(): HistoricFragment{
            val progressFragment = HistoricFragment()

            return progressFragment
        }
    }
}
