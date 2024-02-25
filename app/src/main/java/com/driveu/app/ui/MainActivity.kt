package com.driveu.app.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.driveu.app.R
import com.driveu.app.databinding.ActivityMainBinding
import com.driveu.app.db.RunDao
import com.driveu.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setUpNavHost()
        setSupportActionBar(mBinding.toolbar)
        navigateToTrackingFragmentIfNeeded(intent)



    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent!!)
    }

    private fun setUpNavHost() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

            }
        }
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent) {
        if (intent.action == Constants.ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate(R.id.action_global_tracking_fragment)
        }
    }
}