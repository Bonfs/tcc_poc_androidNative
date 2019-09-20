package com.pocnative.bonfim.pocnativeandroid.profile.fragments


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pocnative.bonfim.pocnativeandroid.MainActivity
import com.pocnative.bonfim.pocnativeandroid.R
import com.pocnative.bonfim.pocnativeandroid.profile.dao.createUserProfile
import com.pocnative.bonfim.pocnativeandroid.profile.model.Gender
import com.pocnative.bonfim.pocnativeandroid.profile.model.User
import com.pocnative.bonfim.pocnativeandroid.utils.toActivity

class FormProfileFragment : androidx.fragment.app.Fragment() {
    var height: Int = 0
    var weight: Long = 0
    var showButtonContinue = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_form_profile, container, false)
        // check if is first time login
        if (!showButtonContinue) view.findViewById<Button>(R.id.btnContinue).visibility = View.GONE

        view.findViewById<TextInputEditText>(R.id.etWeight).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                weight = try {
                    s.toString().toLong()
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }

                calculateBMI()
            }

        })

        view.findViewById<TextInputEditText>(R.id.etHeight).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                height = try {
                    s.toString().toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
                calculateBMI()
            }

        })

        view.findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val age: Int = view.findViewById<TextInputEditText>(R.id.etAge).text.toString().toInt()
            val gender: Gender = Gender.MALE//Gender.valueOf(view.findViewById<Spinner>(R.id.spnGender).selectedItem.toString())
            val newUser = User(weight, height, age, gender)

            createUserProfile(newUser) {
                //this.activity?.toActivity(MainActivity::class.java)
                val appCompatActivity = this.activity as AppCompatActivity
                appCompatActivity.toActivity(MainActivity::class.java)
            }
        }
        return view
    }

    fun calculateBMI() {
        val heightInMeters: Float = this.height.toFloat() / 100F
        val bmi: Float = this.weight / (heightInMeters * heightInMeters)

        view?.findViewById<TextView>(R.id.tvBmi)?.text = String.format("%.2f", bmi)

        when {
            bmi <= 18.5 -> view?.findViewById<TextView>(R.id.tvBmiCategory)?.text = "THIN"
            bmi in 18.6..24.9 -> view?.findViewById<TextView>(R.id.tvBmiCategory)?.text = "HEALTHY"
            bmi in 25.0..29.9 -> view?.findViewById<TextView>(R.id.tvBmiCategory)?.text = "OVERWEIGHT"
            else -> view?.findViewById<TextView>(R.id.tvBmiCategory)?.text = "OBESE"
        }
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
