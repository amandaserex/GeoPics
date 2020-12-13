package edu.ucsb.cs.cs184.amandaserex.aserexgeopics

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.e("DEBUG", "${it.key} = ${it.value}")
                if(it.key == "android.permission.ACCESS_FINE_LOCATION"){
                    if(it.value){
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else{
                    if(it.value){
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
        ) {requestMultiplePermissions.launch(
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
                                                        postSnapshot.child("latitude").getValue() as Double,
                                                        postSnapshot.child(
                                                                "longitude"
                                                        ).getValue() as Double
                                                )
                                        )
                                        .title(GlobalVars.comment)
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


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val fab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab)


        val database = Firebase.database
        val myRef = database.getReference("points")


        val fremont = LatLng(37.5485, -121.9886)
        mMap.addMarker(MarkerOptions().position(fremont).title("Marker in Fremont"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fremont, 14.0f))



        fab.setOnClickListener { view ->

            val alert = AlertDialog.Builder(this)

            alert.setTitle("New Post")
            alert.setMessage("Add your comment!")

            val input = EditText(this)
            alert.setView(input)

            alert.setPositiveButton("Ok") { dialog, whichButton ->
                GlobalVars.comment = input.text.toString()
//            }
//
//            alert.setNegativeButton("Cancel") { dialog, whichButton ->
//                // Canceled.
//            }
//
//            alert.show()

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
                        val newPoint = myRef.push()
                        val lat = newPoint.child("latitude")
                        lat.setValue(location.latitude)
                        val long = newPoint.child("longitude")
                        long.setValue(location.longitude)
                        val name = newPoint.child("name")
                        name.setValue(Login.GlobalVars.userName)
                        val comment = newPoint.child("comment")
                        comment.setValue(GlobalVars.comment)
                        mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                                location.latitude,
                                                location.longitude
                                        ), 16.0f
                                )
                        )
                        mMap.addMarker(
                                MarkerOptions()
                                        .position(LatLng(location.latitude, location.longitude))
                                        .title(GlobalVars.comment)
                        )
                    }
                }
            }

            alert.setNegativeButton("Cancel") { dialog, whichButton ->
                // Canceled.
            }

            alert.show()
        }


    }
}