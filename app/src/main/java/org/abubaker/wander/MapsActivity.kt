package org.abubaker.wander

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.abubaker.wander.databinding.ActivityMapsBinding
import org.abubaker.wander.utils.Constants.REQUEST_LOCATION_PERMISSION
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val TAG_STYLEFILE = MapsActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        /**
         * supportFragmentManager:
         * A Map component in an app. This fragment is the simplest way to place a map in an
         * application. It's a wrapper around a view of a map to automatically handle the necessary
         * life cycle needs.
         */

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // SupportMapFragment: It is a subclass of Fragment that displays a Google map.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        /**
         * A GoogleMap must be acquired using getMapAsync(OnMapReadyCallback).
         * This class automatically initializes the maps system and the view.
         */
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Custom Coordinates
        val latitude = 31.34201831781176
        val longitude = 74.1404663519627
        val homeLatLng = LatLng(latitude, longitude)

        // Custom Overlay .png icon
        val overlaySize = 100f

        val androidOverlay =
            GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
                .position(homeLatLng, overlaySize)

        map.addGroundOverlay(androidOverlay)

        /**
         * 1: World
         * 5: Landmass/continent
         * 10: City
         * 15: Streets
         * 20: Buildings
         */
        val zoomLevel = 15f

        // Move the Camera to our custom geo-location
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))

        // Add a marker @ our home location
        map.addMarker(
            MarkerOptions()
                .position(homeLatLng)
                .title("Abubaker's Home")
        )

        // Allow the user to create new markers on the map
        setMapLongClick(map)

        // Display info about POI when the user clicks on it
        setPoiClick(map)

        // Set the map style using the JSON file
        setMapStyle(map)

        // Enable the My Location layer if the fine location permission has been granted.
        enableMyLocation()

    }

    // Load the JSON file for styling the map
    private fun setMapStyle(map: GoogleMap) {
        try {

            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(

                    // Context
                    this,

                    // JSON File
                    R.raw.map_style

                )
            )

            // If the styling is unsuccessful, print a log that the parsing has failed.
            if (!success) {
                Log.e(TAG_STYLEFILE, "Style parsing failed.")
            }


        } catch (e: Resources.NotFoundException) {

            // If the file is missing
            Log.e(TAG_STYLEFILE, "Can't find style. Error: ", e)

        }
    }

    // Configuration for the new marker
    private fun setMapLongClick(map: GoogleMap) {

        map.setOnMapLongClickListener { userSelectedLatLng ->

            // A snippet is additional text that's displayed below the title.
            val formatCoordinates = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                userSelectedLatLng.latitude,
                userSelectedLatLng.longitude
            )

            map.addMarker(
                MarkerOptions()

                    // Set the position of the marker
                    .position(userSelectedLatLng)

                    // Title
                    .title(getString(R.string.dropped_pin))

                    // Display coordinates of the new marker
                    .snippet(formatCoordinates)

                    // Custom Icon color
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            )

        }

    }

    /**
     * Display information about POI (Point of Interest) on the map
     */
    private fun setPoiClick(map: GoogleMap) {

        // 1. This click listener places a marker on the map immediately when the user clicks a POI.
        // 2. The click listener also displays an info window that contains the POI name.
        map.setOnPoiClickListener { pointOfInterest ->

            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
            )

            // To immediately display the info window
            poiMarker!!.showInfoWindow()

        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()
        ) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    // This method is called when the menu is created.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    // This method is called when the user clicks on a menu item.
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        // Change the map type based on the user's selection.

        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }

        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }

        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

}
