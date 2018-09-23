package com.pocnative.bonfim.pocnativeandroid.profile.fragments


import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import com.pocnative.bonfim.pocnativeandroid.MainActivity
import com.pocnative.bonfim.pocnativeandroid.R
import com.pocnative.bonfim.pocnativeandroid.profile.dao.createUserProfile
import com.pocnative.bonfim.pocnativeandroid.profile.model.Gender
import com.pocnative.bonfim.pocnativeandroid.profile.model.User
import com.pocnative.bonfim.pocnativeandroid.utils.toActivity
import kotlinx.android.synthetic.main.fragment_form_profile.*

class FormProfileFragment : Fragment() {

    var showButtonContinue = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_form_profile, container, false)
        // check if is first time login
        if (!showButtonContinue) view.findViewById<Button>(R.id.btnContinue).visibility = View.GONE

        view.findViewById<Button>(R.id.btnContinue).setOnClickListener{
            val weight: Long = view.findViewById<TextInputEditText>(R.id.etWeight).text.toString().toLong()
            val height: Int = view.findViewById<TextInputEditText>(R.id.etHeight).text.toString().toInt()
            val age: Int = view.findViewById<TextInputEditText>(R.id.etAge).text.toString().toInt()
            val gender: Gender = Gender.MALE//Gender.valueOf(view.findViewById<Spinner>(R.id.spnGender).selectedItem.toString())
            val newUser = User(weight, height, age, gender)

            createUserProfile(newUser) {
                this.activity?.toActivity(MainActivity::class.java)
            }

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
