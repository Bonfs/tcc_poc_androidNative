package com.pocnative.bonfim.pocnativeandroid.progress

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.pocnative.bonfim.pocnativeandroid.R

class ProgressFragment : androidx.fragment.app.Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(): ProgressFragment{
            val progressFragment = ProgressFragment()

            return progressFragment
        }
    }
}
