package com.example.homejameswil;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.homejameswil.databinding.ActivityHomeMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HomeMaps extends FragmentActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private ActivityHomeMapsBinding binding;
    Location myLocation = null;
    private Double customerLat = null;
    private Double customerLong = null;
    protected LatLng customerLocation = null;
    FloatingActionButton btnPinMyHome;
    private LatLng myHome = null;
    FirebaseUser user;
    String uid;
    String TAG = "Firebase";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));
        mMap = googleMap;

        getCurrentLocation();


        btnPinMyHome = findViewById(R.id.setHome);
        int color = Color.parseColor("#FFFFFF");
        btnPinMyHome.setColorFilter(color);

        btnPinMyHome.setTooltipText("Set Home Location");

        //Set map on click listener
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                //Clear all markers
                mMap.clear();
                //Add marker at position
                mMap.addMarker(new MarkerOptions().position(latLng));

                myHome = latLng;
                Log.d("TAG", "onMapClick COORDINATES: " + latLng.latitude + " " + latLng.longitude);
            }
        });

        btnPinMyHome.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Clear all markers
                mMap.clear();
                //Add marker at position
                //Separate myHome to Lat and Long double
                double lat = myHome.latitude;
                double lng = myHome.longitude;

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                // Get logged in user UID
                user = FirebaseAuth.getInstance().getCurrentUser();
                uid = user.getUid();

                //Creating user object
                //Setting text boxes to user object defining node names
                Map<String, Object> user = new HashMap<>();

                user.put("HomeLatitude", lat);
                user.put("HomeLongitude", lng);


                //Writing to Firestore specifying collection path with custom set Document reference
                db.collection("Users").document(uid).update(user).addOnSuccessListener(new OnSuccessListener<Void>()
                        {
                            @Override
                            public void onSuccess(Void aVoid)
                            {
                                Toast.makeText(getApplicationContext(), "Your home has been saved!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "DocumentSnapshot added with ID:" );
                            }
                        })
                        .addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(getApplicationContext(), "Oh no, there was an error saving your home!", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
                Log.d("TAG", "onMapClick COORDINATES: " + customerLocation.latitude + " " + customerLocation.longitude);
            }

        });

    }

    boolean myLocationFound = false;
    private void getCurrentLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mMap.setMyLocationEnabled(true);
        //Get my current location
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener()
        {
            @Override
            public void onMyLocationChange(Location location)
            {
                myLocation = location;
                //Convert location to LatLong
                customerLocation = new LatLng(location.getLatitude(), location.getLongitude());

                //Convert customerLocation to separate strings
                customerLat = Double.valueOf(customerLocation.latitude);
                customerLong = Double.valueOf(customerLocation.longitude);

                Log.d("TAG", "MyLocation: TEST2.0" + customerLocation);

                LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ltlng, 16f);

                if (!myLocationFound)
                {
                    mMap.animateCamera(cameraUpdate);
                    myLocationFound = true;
                }
            }
        });
    }
}