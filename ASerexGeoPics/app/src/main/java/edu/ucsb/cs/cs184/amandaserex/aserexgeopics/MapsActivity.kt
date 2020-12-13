package edu.ucsb.cs.cs184.amandaserex.aserexgeopics

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
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
        var likes = 0
        var image = ""
        var addNew = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    Log.e("DEBUG", "${it.key} = ${it.value}")
                    if (it.key == "android.permission.ACCESS_FINE_LOCATION") {
                        if (it.value) {
                            mMap.setMyLocationEnabled(true);
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
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION
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
                    if (name == Login.GlobalVars.userName) {
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
                                .title(postSnapshot.child("comment").getValue() as String?)
                        )
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
            val lat = newPoint.child("latitude")
            lat.setValue(GlobalVars.latitude.toDouble())
            val long = newPoint.child("longitude")
            long.setValue(GlobalVars.longitude.toDouble())
            val name = newPoint.child("name")
            name.setValue(Login.GlobalVars.userName)
            val comment = newPoint.child("comment")
            comment.setValue(GlobalVars.comment)
            val likes = newPoint.child("likes")
            likes.setValue(0)
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
                    .title(GlobalVars.comment)
            )
            GlobalVars.addNew = false
        }

        val fab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab)


        val database = Firebase.database
        val myRef = database.getReference("points")


        val fremont = LatLng(37.5485, -121.9886)
        mMap.addMarker(MarkerOptions().position(fremont).title("Marker in Fremont"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fremont, 14.0f))


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
    }
}
