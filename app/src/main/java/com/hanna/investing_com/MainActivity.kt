package com.hanna.investing_com

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.hanna.investing_com.LocationUpdatesService.LocalBinder
import com.hanna.investing_com.network.Status
import com.hanna.investing_com.ui.PlacesAdapter
import com.hanna.investing_com.viewmodel.LocationViewModel
import com.hanna.investing_com.viewmodel.LocationViewModelFactory
import com.microsoft.maps.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: LocationViewModelFactory
    val viewModel: LocationViewModel by viewModels { factory }

    private val myReceiver = MyReceiver()

    private val adapter = PlacesAdapter()

    // A reference to the service used to get location updates.
    private var mService: LocationUpdatesService? = null

    // Tracks the bound state of the service.
    private var mBound = false

    lateinit var mMapView: MapView

    private val currentLocationLayer = MapElementLayer()
    private val pointsOfInterestLayer = MapElementLayer()

    // Monitors the state of the connection to the service.
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocalBinder
            mService = binder.service
            mBound = true
            if (!checkPermissions()) {
                requestPermissions()
            } else {
                mService?.requestLocationUpdates()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService?.removeLocationUpdates()
            mService = null
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mMapView = MapView(this, MapRenderMode.RASTER).also {
            it.setCredentialsKey(BuildConfig.CREDENTIALS_KEY)
            map_view.addView(it)
            it.onCreate(savedInstanceState)
            it.layers.also { layers ->
                layers.add(pointsOfInterestLayer)
                layers.add(currentLocationLayer)
            }
        }

        points_interest_list.adapter = adapter

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions()
            }
        }

        viewModel.locationMutableLiveData.observe(this, {
            val point = Geopoint(it)
            mMapView.zoomLevel
            mMapView.mapRenderMode
            mMapView.setScene(
                MapScene.createFromLocationAndZoomLevel(point, 15.toDouble()),
                MapAnimationKind.NONE
            )

            val icon =
                AppCompatResources.getDrawable(baseContext, R.drawable.ic_baseline_location_on_24)
                    ?.toBitmap()
            MapIcon().apply {
                location = point
                title = "Current Location"
                icon?.run {
                    image = MapImage(this)
                }
                currentLocationLayer.elements.let { elements ->
                    elements.clear()
                    elements.add(this)
                }
            }
        })

        viewModel.nearbyPointsOfInterest.observe(this, {
            Log.d(TAG, "http create23  ${it.status}")
            when (it.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    it.data?.take(5).run {
                        adapter.submitData(this.orEmpty(), viewModel.locationMutableLiveData.value)
                        //remove old icons
                        pointsOfInterestLayer.elements.clear()
                        //add icons to map
                        this?.forEach { place ->
                            MapIcon().apply {
                                location = Geopoint(place.location.lat, place.location.lng)
                                title = place.name
                                pointsOfInterestLayer.elements.add(this)
                            }
                        }

                    }
                }
                Status.ERROR -> {
                    Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mMapView.onStart()
        bindService(
            Intent(this, LocationUpdatesService::class.java), mServiceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            myReceiver, IntentFilter(LocationUpdatesService.ACTION_BROADCAST)
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver)
        super.onPause()
        mMapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        if (mBound) {
            unbindService(mServiceConnection)
            mBound = false
        }
        super.onStop()
        mMapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                findViewById(R.id.activity_main),
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.ok) { // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.i(TAG, "Requesting permission")
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                }//canceled
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    mService?.requestLocationUpdates()
                }
                else -> {
                    // Permission denied.
                    Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(R.string.settings) { // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    /**
     * Receiver for broadcasts sent by [LocationUpdatesService].
     */
    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location =
                intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
            viewModel.setCurrentLocationValue(location)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}