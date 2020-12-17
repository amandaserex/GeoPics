package edu.ucsb.cs.cs184.amandaserex.aserexgeopics

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    object GlobalVars {
        var comment = ""
        var latitude = 0.0
        var longitude = 0.0
        var likes = "0.0"
        var image = ""
        var addNew = false
        var firebaseID = ""
        var tempLikes = 0
    }

    inner class CustomInfoWindowForGoogleMap(context: Context) : GoogleMap.InfoWindowAdapter {

        var mContext = context
        var mWindow = (context as Activity).layoutInflater.inflate(R.layout.infowindow, null)



        private fun rendowWindowText(marker: Marker, view: View) {
            GlobalVars.image = marker.snippet
            val tvTitle = view.findViewById<TextView>(R.id.title)
            tvTitle.text = marker.title
            val picture = view.findViewById<ImageView>(R.id.picture)
            picture.setImageBitmap(BitmapFactory.decodeFile(marker.snippet, BitmapFactory.Options()))
            val likeButton = view.findViewById<TextView>(R.id.likeButton)
            likeButton.text = marker.zIndex.toInt().toString()



            val database = Firebase.database
            val myRef = database.getReference("points").orderByKey()

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (postSnapshot in dataSnapshot.children) {
                        val options: BitmapFactory.Options = BitmapFactory.Options()
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        if (GlobalVars.image == postSnapshot.child("image").value){
                            likeButton.text = postSnapshot.child("likes").value.toString()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        override fun getInfoContents(marker: Marker): View {
            rendowWindowText(marker, mWindow)
            return mWindow
        }

        override fun getInfoWindow(marker: Marker): View? {
            rendowWindowText(marker, mWindow)
            return mWindow
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    Log.e("DEBUG", "${it.key} = ${it.value}")
                    if (it.key == "android.permission.ACCESS_FINE_LOCATION") {
                        if (it.value) {
                            mMap.setMyLocationEnabled(true)
                        }
                    } else {
                        if (it.value) {
                            Log.e("DEBUG", "yay")
                        }
                    }
                }
            }

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestMultiplePermissions.launch(
                    arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val database = Firebase.database
        val myRef = database.getReference("points").orderByKey()
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val name = postSnapshot.child("name").getValue(String::class.java)
                    val options: BitmapFactory.Options = BitmapFactory.Options()
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    if (name == Login.GlobalVars.userName){
                        if(postSnapshot.child("likes").value != null) {
                            mMap.addMarker(
                                    MarkerOptions()
                                            .position(
                                                    LatLng(
                                                            postSnapshot.child("latitude")
                                                                    .getValue() as Double,
                                                            postSnapshot.child(
                                                                    "longitude"
                                                            ).getValue() as Double
                                                    )
                                            )
                                            .snippet(postSnapshot.child("image").value.toString())
                                            .title(postSnapshot.child("comment").value as String?)
                                            .zIndex((postSnapshot.child("likes").value as String).toFloat())
                            )
                        }
                    }
                }
            }


            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (GlobalVars.addNew) {
            val database = Firebase.database
            val myRef = database.getReference("points")

            val newPoint = myRef.push()
            val fbId = newPoint.child("firebaseID")
            fbId.setValue(newPoint.key)
            GlobalVars.firebaseID = newPoint.key.toString()
            val lat = newPoint.child("latitude")
            lat.setValue(GlobalVars.latitude.toDouble())
            val long = newPoint.child("longitude")
            long.setValue(GlobalVars.longitude.toDouble())
            val name = newPoint.child("name")
            name.setValue(Login.GlobalVars.userName)
            val comment = newPoint.child("comment")
            comment.setValue(GlobalVars.comment)
            val likes = newPoint.child("likes")
            likes.setValue("0.0")
            GlobalVars.tempLikes = 0
            val image = newPoint.child("image")
            image.setValue(GlobalVars.image)
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if(GlobalVars.addNew){
            mMap.addMarker(
                    MarkerOptions()
                            .position(
                                    LatLng(
                                            GlobalVars.latitude,
                                            GlobalVars.longitude
                                    )
                            )
                            .snippet(GlobalVars.image)
                            .title(GlobalVars.comment)
                            .zIndex(0.0F)
            )
            GlobalVars.addNew = false
        }

        val heart: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fabHeart)

        val fab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab)

        heart.setOnClickListener { view ->
            if(GlobalVars.image == ""){

            }
            else{

                val database = Firebase.database
                val myRef = database.getReference("points").orderByKey()
                myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (postSnapshot in dataSnapshot.children) {
                            val options: BitmapFactory.Options = BitmapFactory.Options()
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            if (GlobalVars.image == postSnapshot.child("image").value){
                                GlobalVars.firebaseID = postSnapshot.key.toString()
                                GlobalVars.likes = postSnapshot.child("likes").value.toString()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                GlobalVars.tempLikes = (GlobalVars.likes.toFloat()+1).toInt()
                database.getReference("points").child(GlobalVars.firebaseID).child("likes").setValue((GlobalVars.likes.toFloat()+1).toString())
            }
        }



        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.5485, -121.9886), 14.0f))

        fab.setOnClickListener { view ->


            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                Snackbar.make(
                        view,
                        "You are unable to use this feature. Please allow access to your location and camera in settings",
                        Snackbar.LENGTH_LONG
                )
                    .setAction("Action", null).show()
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        GlobalVars.latitude = location.latitude
                        GlobalVars.longitude = location.longitude
                    }
                }
            val i = Intent(this, PostActivity::class.java)
            startActivity(i)
        }
        mMap.setInfoWindowAdapter(CustomInfoWindowForGoogleMap(this))
    }

}

