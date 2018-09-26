package com.pocnative.bonfim.pocnativeandroid.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pocnative.bonfim.pocnativeandroid.R
import com.pocnative.bonfim.pocnativeandroid.profile.fragments.FormProfileFragment
import kotlinx.android.synthetic.main.toolbar.*

class ProfileActivity : AppCompatActivity() {
    var isFirstTime = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val bundle = intent.extras
        toolbar.title = "Profile"

        isFirstTime = bundle?.getBoolean("firstLogin") ?: isFirstTime
        val formFragment = FormProfileFragment.getInstance(isFirstTime)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, formFragment)
            .commit()
    }
}
