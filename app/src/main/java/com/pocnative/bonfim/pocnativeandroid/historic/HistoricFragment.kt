package com.pocnative.bonfim.pocnativeandroid.historic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.pocnative.bonfim.pocnativeandroid.R
import com.pocnative.bonfim.pocnativeandroid.adapters.HistoricAdapter

class HistoricFragment : androidx.fragment.app.Fragment() {
    private lateinit var rvHistoric: RecyclerView
    private lateinit var viewAdapter: HistoricAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_historic, container, false)

        val mockedData: Array<String> = arrayOf("1", "2", "3")
        viewManager = LinearLayoutManager(context)
        viewAdapter = HistoricAdapter(mockedData)

        rvHistoric = view.findViewById<RecyclerView>(R.id.rvHistoric).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        return view
    }

    companion object {
        @JvmStatic
        fun getInstance(): HistoricFragment{
            val progressFragment = HistoricFragment()

            return progressFragment
        }
    }
}
