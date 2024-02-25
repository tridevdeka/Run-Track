package com.driveu.app.ui.framents

import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.driveu.app.R
import com.driveu.app.databinding.FragmentTrackingBinding
import com.driveu.app.db.Converters
import com.driveu.app.db.LatLngData
import com.driveu.app.db.Run
import com.driveu.app.services.POLYLINE
import com.driveu.app.services.TrackingService
import com.driveu.app.util.Constants
import com.driveu.app.util.Constants.ACTION_PAUSE_SERVICE
import com.driveu.app.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.driveu.app.util.Constants.ACTION_STOP_SERVICE
import com.driveu.app.util.Constants.BUNDLE_KEY
import com.driveu.app.util.Constants.POLYLINE_COLOR
import com.driveu.app.util.Constants.POLYLINE_WIDTH
import com.driveu.app.util.TrackingUtility
import com.driveu.app.viewmodels.MainViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Calendar
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val runCollection = Firebase.firestore.collection("runs")

    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private var mBinding: FragmentTrackingBinding? = null
    private var menu: Menu? = null

    private var isTracking = false
    private var pathPoints = mutableListOf<POLYLINE>()
    private var curTimeInMillis = 0L


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentTrackingBinding.inflate(layoutInflater)

        setHasOptionsMenu(true)

        return mBinding?.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tool_bar_tracking_menu, menu)
        this.menu = menu
        if (curTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cancelTracking -> {
                showCancelDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewBundle = savedInstanceState?.getBundle(BUNDLE_KEY)
        mBinding?.mapView?.onCreate(viewBundle)

        if (savedInstanceState != null) {
            val cancelDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_RUN_DIALOG
            ) as CancelTrackingDialog?

            cancelDialog?.setListener {
                stopRun()
            }
        }

        mBinding?.btnToggleRun?.setOnClickListener {
            if (isLocationEnabled()) {
                toggleRun()
            } else {
                requestDeviceLocationSettings()
            }
        }

        mBinding?.btnFinishRun?.setOnClickListener {

            try {
                zoomToSeeWholeTrack()
                saveRunToDB()
            } catch (e: Exception) {
                Toast.makeText(requireContext(),"Please Wait..",Toast.LENGTH_SHORT).show()
            }

        }


        mBinding?.mapView?.getMapAsync {
            map = it
            addAllPolyLines()
        }
        subscribeToObserver()


    }

    private fun subscribeToObserver() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyLine()
            moveCamera()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            mBinding?.tvTimer?.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }


    private fun showCancelDialog() {
        CancelTrackingDialog().apply {
            setListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_RUN_DIALOG)
    }

    private fun stopRun() {
        mBinding?.tvTimer?.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            mBinding?.btnToggleRun?.text = "Start"
            mBinding?.btnFinishRun?.isVisible = true
        } else if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            mBinding?.btnToggleRun?.text = "Stop"
            mBinding?.btnFinishRun?.isVisible = false
        }
    }


    private fun moveCamera() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    Constants.MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (position in polyline) {
                bounds.include(position)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mBinding?.mapView?.width ?: 0,
                mBinding?.mapView?.height ?: 0,
                ((mBinding?.mapView?.height ?: 0) * 0.05f).toInt()
            )
        )
    }

    private fun saveRunToDB() {
        map?.snapshot { bm ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            val polylineList = ArrayList<ArrayList<LatLngData>>()
            val polylines = ArrayList<LatLngData>()
            for (polyline in pathPoints) {
                for (data in polyline) {
                    val coordinates = LatLngData(data.latitude, data.longitude)
                    polylines.add(coordinates)
                }
                polylineList.add(polylines)
            }
            val avgSpeedInKm = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            /*Converters().fromBitmap(bm!!)*/
            val run = Run(TrackingUtility.encodeBitmap(bm!!), dateTimeStamp, avgSpeedInKm, distanceInMeters, curTimeInMillis, polylines = polylineList)
            val runRemote = Run(TrackingUtility.encodeBitmap(bm!!), dateTimeStamp, avgSpeedInKm, distanceInMeters, curTimeInMillis)


            uploadToFireStore(runRemote)
            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()

            stopRun()

        }

    }

    private fun addAllPolyLines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyLine() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions().color(Constants.POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }


    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }


    override fun onResume() {
        super.onResume()
        mBinding?.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mBinding?.mapView?.onStart()
    }


    override fun onStop() {
        super.onStop()
        mBinding?.mapView?.onStop()
    }


    override fun onPause() {
        super.onPause()
        mBinding?.mapView?.onPause()
    }


    override fun onDestroy() {
        super.onDestroy()
        mBinding?.mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mBinding?.mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val viewBundle = outState.getBundle(BUNDLE_KEY)
        viewBundle?.let {
            mBinding?.mapView?.onSaveInstanceState(it)
        }
    }

    companion object {
        const val CANCEL_RUN_DIALOG = "Cancel"
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestDeviceLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { _ ->

        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        100
                    )

                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }

    }


    private fun uploadToFireStore(run: Run) = CoroutineScope(Dispatchers.IO).launch {
        try {
            runCollection.add(run).await()
        } catch (e: Exception) {
            Timber.e(e.message)
        }
    }

}