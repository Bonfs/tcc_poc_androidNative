package com.pocnative.bonfim.pocnativeandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import android.view.MenuItem
import com.pocnative.bonfim.pocnativeandroid.activity.ActivityFragment
import com.pocnative.bonfim.pocnativeandroid.goals.GoalsFragment
import com.pocnative.bonfim.pocnativeandroid.progress.ProgressFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar.title = getString(R.string.app_name)
        bottonMenu.setOnNavigationItemSelectedListener(this)

        switchFragment(ActivityFragment())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // select item in a bottom menu
        val selectedFragment = when (item.itemId){
            R.id.progress -> {
                ProgressFragment()
            }
            R.id.activity -> {
                ActivityFragment()
            }
//            R.id.goals -> {
//                GoalsFragment()
//            }
            else -> {
                ActivityFragment()
            }
        }

        switchFragment(selectedFragment)
        return true
    }

    private fun switchFragment(fragment: Fragment) =
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_in)
                .replace(R.id.container, fragment)
                .commit()
}
