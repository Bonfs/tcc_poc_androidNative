package com.pocnative.bonfim.pocnativeandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.pocnative.bonfim.pocnativeandroid.auth.AuthManager
import com.pocnative.bonfim.pocnativeandroid.profile.ProfileActivity
import com.pocnative.bonfim.pocnativeandroid.utils.toActivity

class SplashActivity : AppCompatActivity() {

    private val TAG: String = "SplashScreen"
    private val SPLASH_TIME_OUT: Long = 1500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    override fun onStart() {
        super.onStart()

        Handler().postDelayed({
            if (AuthManager.isLogged()) {
                this.toActivity(MainActivity::class.java)
                Log.d(TAG, "main activity")
            } else {
                AuthManager.createAnonymousUser({
                    Log.d(TAG, "profile activity")
                    val intent = Intent(this, ProfileActivity::class.java)
                    val bundle = Bundle()
                    bundle.putBoolean("firstLogin", true)
                    intent.putExtras(bundle)
                    this.toActivity(ProfileActivity::class.java, intent)
                },
                {
                    Log.d(TAG, "deu ruim")
                })

            }
        }, SPLASH_TIME_OUT)
    }
}
