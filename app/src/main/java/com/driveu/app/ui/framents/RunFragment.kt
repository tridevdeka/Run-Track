package com.driveu.app.ui.framents

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.driveu.app.R
import com.driveu.app.databinding.FragmentRunBinding
import com.driveu.app.db.Run
import com.driveu.app.ui.adapters.RunAdapter
import com.driveu.app.util.TrackingUtility
import com.driveu.app.viewmodels.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber


@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var mBinding: FragmentRunBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var runAdapter: RunAdapter
    private val runCollection = Firebase.firestore.collection("runs")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentRunBinding.inflate(layoutInflater)
        setupListeners()
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askNotificationPermissionForApi33orAbove()
        }

        setUpRecyclerView()
        /* viewModel.getAllRuns().observe(viewLifecycleOwner, Observer {
             runAdapter.submitList(it)
         })*/

        getAllRunsFromFireStore()

    }

    private fun setupListeners() {
        mBinding.fab.setOnClickListener {
            if (TrackingUtility.hasLocationPermissions(requireContext())) {
                findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
            } else {
                requestPermissions()
            }
        }
    }


    private fun setUpRecyclerView() {
        mBinding.rvRuns.apply {
            runAdapter = RunAdapter()
            adapter = runAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                Companion.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    private fun askNotificationPermissionForApi33orAbove() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION_PERMISSION
            )

        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    companion object {
        private val REQUEST_CODE_LOCATION_PERMISSION = 101
    }

    private fun getAllRunsFromFireStore() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val remoteList = ArrayList<Run>()
            val querySnapShot = runCollection.get().await()
            for (document in querySnapShot.documents) {
                document.toObject<Run>()?.let { remoteList.add(it) }
            }

            withContext(Dispatchers.Main) {
                runAdapter.submitList(remoteList)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}