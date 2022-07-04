package com.example.brightspot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.fragment_maps.*
import java.lang.Exception
import java.util.*


class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var pDatabase : DatabaseReference
    private lateinit var rDatabase : DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 1
    private var previousPointList = mutableListOf<LatLng>()
    private var distance : Double = 0.0


    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        mMap = googleMap
        mMap.setPadding(0, 0, 0, 120)
        Log.d("MapsActivity", "OnMapReady1")
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapLongClickListener(this)
        Log.d("MapsActivity", "OnMapReady2")
        setUpMap()
        allMarkersOnMap()

        pDatabase = FirebaseDatabase.getInstance().getReference("Photo")
        rDatabase = FirebaseDatabase.getInstance().getReference("Routes")

        val routeID = arguments?.get("routeID")
        Log.d("MapsActivity", "Route Id: $routeID")
        if (routeID != null){
            val route = rDatabase.child("$routeID/latLngList").get().addOnSuccessListener {
                var listLatLng = mutableListOf<LatLng>()
                Log.d("MapsFragment", "1: $it")
                Log.d("MapsFragment", "2: ${it.children}")
                for (point in it.children){
                    Log.d("MapsFragment", "Точка: $point")
                    listLatLng.add(
                        LatLng(
                            point.child("latitude").value.toString().toDouble(),
                            point.child("longitude").value.toString().toDouble()))
                }
                Log.d("MapsFragment", "Список: $listLatLng")
                for ((i, point) in listLatLng.withIndex()){
                    if (i == (listLatLng.count() - 1)) break
                    mMap.addPolyline(PolylineOptions()
                        .color(0xffFB8C55.toInt())
                        .add(
                            point,
                            listLatLng[i+1]
                        ))
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(listLatLng[0], 14f))
            }
        }

        pDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear()
                for (photo in snapshot.children){
                    val lat = photo.child("latLng").child("latitude").value
                    val lng = photo.child("latLng").child("longitude").value
                    val latLng = LatLng(lat.toString().toDouble(), lng.toString().toDouble())
                    val url = photo.child("photoUrl").value.toString()
                    val user = photo.child("userId").value.toString()
                    placeMarkerOnMap(latLng, url, user)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        cancelButton.setOnClickListener {
            mMap.clear()
            allMarkersOnMap()
            cancelButton.visibility = View.INVISIBLE
            acceptButton.visibility = View.INVISIBLE
            previousPointList.clear()
        }

        acceptButton.setOnClickListener{
            val uuid = UUID.randomUUID().toString()
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val route = Route(previousPointList, userId, distance.toInt())
            rDatabase.child(uuid).setValue(route).addOnSuccessListener {
                Toast.makeText(activity, "Запись добавлена", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(activity, "Не добавлена", Toast.LENGTH_SHORT).show()
            }
            cancelButton.callOnClick()
        }
    }


        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        // Inflate the layout for this fragment
            Log.d("MapsActivity", "onCreateView")
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        Log.d("MapsActivity", "1")
        fusedLocationClient.lastLocation.addOnSuccessListener(activity!!) { location ->
            Log.d("MapsActivity", "2")
            if(location != null){
                Log.d("MapsActivity", "3")
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                Log.d("MapsActivity", "Current location: $currentLatLong")
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 15f))
            }
        }
    }

    private fun allMarkersOnMap(){
        pDatabase = FirebaseDatabase.getInstance().getReference("Photo")
        pDatabase.get().addOnSuccessListener {
            for (photo in it.children) {
                val lat = photo.child("latLng").child("latitude").value
                val lng = photo.child("latLng").child("longitude").value
                val latLng = LatLng(lat.toString().toDouble(), lng.toString().toDouble())
                val url = photo.child("photoUrl").value.toString()
                val user = photo.child("userId").value.toString()
                placeMarkerOnMap(latLng, url, user)
            }
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng, photoUrl: String, user: String) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        var marker = mMap.addMarker(markerOptions)
        marker?.setTag(listOf(photoUrl, user))
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        val tag = p0.tag as List<String>
//        val intent = Intent(context, PhotoActivity::class.java)
//        intent.putExtra("photoUrl", url)
//        startActivity(intent)

//        val photoFragment = PhotoFragment()
//        val transaction : FragmentTransaction = fragmentManager!!.beginTransaction()
//        transaction.replace(R.id.main_activity, photoFragment)
//        transaction.commit()
        Log.d("MainActivity", "${tag}")
        findNavController().navigate(
            R.id.action_navigation_graph_map_to_photoFragment,
            bundleOf("urlTag" to tag[0], "userTag" to tag[1])
        )
        return false
    }

    override fun onMapLongClick(p0: LatLng) {
        if (previousPointList.isEmpty()){
            cancelButton.visibility = View.VISIBLE
            acceptButton.visibility = View.VISIBLE

            mMap.addPolyline(PolylineOptions()
                .color(0xffFB8C55.toInt())
                .add(
                    LatLng(lastLocation.latitude, lastLocation.longitude),
                    p0))
        }
        else {
            mMap.addPolyline(PolylineOptions()
                .color(0xffFB8C55.toInt())
                .add(
                    previousPointList.last(),
                    p0))
//            val distanceIntermediate = floatArrayOf()
//            Location.distanceBetween(
//                previousPointList.last().latitude,
//                previousPointList.last().longitude,
//                p0.latitude,
//                p0.longitude,
//                distanceIntermediate)
//            Log.d("MapsActivity", "${distanceIntermediate}")

            val distanceTest = SphericalUtil.computeDistanceBetween(previousPointList.last(), p0)
            Log.d("MapsActivity", "$distanceTest")
            distance += distanceTest
            Log.d("MapsActivity", "$distance")
        }
        previousPointList.add(p0)
        Log.d("MapsActivity", "${previousPointList}")
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }
}