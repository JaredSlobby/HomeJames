package com.example.homejameswil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;


public class LandingPage extends Fragment
{
    private static String TAG = "LandingPage";
    Location myLocation = null;
    protected LatLng customerLocation = null;
    boolean myLocationFound = false;
    SupportMapFragment supportMapFragment;
    View view;
    TextView welcome, workingHours;
    FirebaseUser user;
    Button btnPinMyHome;
    String uid;
    private static final String ONESIGNAL_APP_ID = "556bf015-31aa-42d9-a448-4642ce2fb4b7";

    ArrayList<Double> HomeLatitude;
    ArrayList<Double> HomeLongitude;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_landing_page, container, false);

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        HomeLatitude = new ArrayList<>();
        HomeLongitude = new ArrayList<>();



        //CheckIfHomeSet();
        Welcome();
        location();


        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(getContext());
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        OneSignal.promptForPushNotifications();

        currentActiveTrip();

        //OneSignal.addTrigger("User_ID", "True");

        // Return view
        //setExternalUserID(user.getUid());
        return view;
    }


    private void location()
    {
        // Initialize map fragment
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);



        // Async map

        supportMapFragment.getMapAsync(new OnMapReadyCallback()
        {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap)
            {
                boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
                if (!success)
                {
                    Log.e(TAG, "Style parsing failed.");
                }
                //Setting my location and hiding gestures test
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setAllGesturesEnabled(false);

                googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener()
                {
                    @Override
                    public void onMyLocationChange(Location location)
                    {
                        myLocation = location;
                        //Convert location to LatLong
                        customerLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        Log.d(TAG, "MyLocation: TEST2.0" + customerLocation);

                        LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ltlng, 15f);

                        if (myLocationFound == false)
                        {
                            googleMap.moveCamera(cameraUpdate);
                            myLocationFound = true;
                        }
                    }
                });




                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
                {
                    @Override
                    public void onMapClick(LatLng latLng)
                    {
                        int x = 0;
                        if(HomeLongitude.size() == 0 && HomeLatitude.size() == 0)
                        {
                            Toast.makeText(getContext(), "Please pin your home before ordering a driver!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {

                            Log.d(TAG, "Message:" + HomeLatitude.size() + " " + HomeLongitude.size());
                            Intent intent = new Intent(getActivity(), MapsActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
    }


    @Override
    public void onResume()
    {
        super.onResume();
        Welcome();
    }


    private void Welcome()
    {
        //Welcome message
        user = FirebaseAuth.getInstance().getCurrentUser();
        welcome = view.findViewById(R.id.welcome);



        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Read from database specifying with collection
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                if(document.getId().matches(user.getUid()))
                                {
                                    welcome.setText("Welcome " + document.getString("UserName") + " " + document.getString("UserSurname"));

                                    if(document.getDouble("HomeLatitude") == null && document.getDouble("HomeLongitude") == null )
                                    {
                                        Log.d(TAG, "Testing check home" +  HomeLatitude + " " +  HomeLongitude);
                                    }
                                    else
                                    {
                                        HomeLatitude.add(document.getDouble("HomeLatitude"));
                                        HomeLongitude.add(document.getDouble("HomeLongitude"));
                                        Log.d(TAG, "Testing check home" +  HomeLatitude + " " +  HomeLongitude);
                                    }
                                }
                            }
                        }
                        else
                        {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        btnPinMyHome = view.findViewById(R.id.PinHome);

        btnPinMyHome.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), HomeMaps.class);
                startActivity(intent);
            }
        });


        workingHours = view.findViewById(R.id.workingHours);

        String MondayToSaturday = "6pm - 2am";
        String Sunday = "6pm - 11pm";

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day)
        {
            case Calendar.SUNDAY:
                workingHours.setText(Sunday);
                break;
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
            case Calendar.THURSDAY:
            case Calendar.FRIDAY:
            case Calendar.SATURDAY:
                workingHours.setText(MondayToSaturday);
                break;
        }



    }


    private void currentActiveTrip()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Read from database specifying with collection
        db.collection("Orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        if (document.getString("UID").matches(user.getUid()) && document.getString("Status").matches("Active"))
                        {
                            //Do What I want for now
                            Toast.makeText(getContext(), "You have an active trip", Toast.LENGTH_SHORT).show();
                            
                        }
                    }
                }
                else
                {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }


}