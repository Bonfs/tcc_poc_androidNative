package com.pocnative.bonfim.pocnativeandroid.profile.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.pocnative.bonfim.pocnativeandroid.MainActivity

import com.pocnative.bonfim.pocnativeandroid.R
import com.pocnative.bonfim.pocnativeandroid.utils.toActivity

class FormProfileFragment : Fragment() {

    var showButtonContinue = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_form_profile, container, false)
        // check if is first time login
        if (!showButtonContinue) view.findViewById<Button>(R.id.btnContinue).visibility = View.GONE

        view.findViewById<Button>(R.id.btnContinue).setOnClickListener{
            this.activity?.toActivity(MainActivity::class.java)
        }
        return view
    }

    companion object {
        @JvmStatic
        fun getInstance(isFirstTime: Boolean): FormProfileFragment{
            val fragment = FormProfileFragment()
            fragment.showButtonContinue = isFirstTime
            return fragment
        }
    }
}
