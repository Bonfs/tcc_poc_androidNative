package com.pocnative.bonfim.pocnativeandroid

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.pocnative.bonfim.pocnativeandroid.profile.ProfileActivity
import com.pocnative.bonfim.pocnativeandroid.utils.toActivity

class SplashActivity : AppCompatActivity() {

    private val TAG: String = "SplashScreen"
    private val SPLASH_TIME_OUT: Long = 1500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            val intent = Intent(this, ProfileActivity::class.java)
            val bundle = Bundle()
            bundle.putBoolean("firstLogin", true)
            intent.putExtras(bundle)
            this.toActivity(ProfileActivity::class.java, intent)
        }, SPLASH_TIME_OUT)
    }
}
