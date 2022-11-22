package com.example.homejameswil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.onesignal.OSInAppMessageAction;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Driver;
import java.util.Calendar;

public class DriverLandingPage extends Fragment
{
    GoogleMap mapDriver;
    protected LatLng start = null;
    View view;
    SupportMapFragment supportMapFragment;
    FirebaseUser user;
    TextView welcome;
    String TAG = "Firebase";
    TextView workingHours;
    Boolean activeTrip = false;
    private static final String ONESIGNAL_APP_ID = "556bf015-31aa-42d9-a448-4642ce2fb4b7";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_driver_landing_page, container, false);

        //Sign out of firebase
        //Using it to test
        //FirebaseAuth.getInstance().signOut();


        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(getContext());
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        OneSignal.promptForPushNotifications();

        checkActiveTrip();

        //OneSignal.sendTag("Driver", "True");

        //OneSignal.addTrigger("User_ID", "True");





        OneSignal.setInAppMessageClickHandler(new OneSignal.OSInAppMessageClickHandler()
        {
            @Override
            public void inAppMessageClicked(OSInAppMessageAction result)
            {
                //Toast.makeText(getContext(), "In App Message Clicked!", Toast.LENGTH_SHORT).show();

            }
        });



        Welcome();
        LocationDriver();


        return view;
    }




    private void Welcome()
    {
        //Welcome message
        user = FirebaseAuth.getInstance().getCurrentUser();
        welcome = view.findViewById(R.id.welcome);




        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Read from database specifying with collection
        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                if(document.getId().matches(user.getUid()))
                                {
                                    welcome.setText("Welcome " + document.getString("UserName") + " " + document.getString("UserSurname"));

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

    Boolean didLoadActivity = false;
    @Override
    public void onResume()
    {
        super.onResume();
        if(didLoadActivity)
        {
            if (activeTrip)
            {
                Intent intent = new Intent(getContext(), DriverMaps.class);
                startActivity(intent);
            }
        }
        else
        {
            didLoadActivity = true;
        }
    }



    private void LocationDriver()
    {
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapDriver);

        // Async map

        supportMapFragment.getMapAsync(new OnMapReadyCallback()
        {
        @Override
        public void onMapReady (GoogleMap googleMap)
        {
            mapDriver = googleMap;
            boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                    .getString(R.string.style_json)));
            if (!success)
            {
                Log.e("TAG", "Style parsing failed.");
            }
            // Add a marker in Sydney and move the camera
            getCurrentLocation();
            //notification();
        }

        public void getCurrentLocation()
        {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mapDriver.setMyLocationEnabled(true);
            //Get my current location
            mapDriver.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener()
            {
                @Override
                public void onMyLocationChange(Location location)
                {
                    //Convert location to LatLong
                    start = new LatLng(location.getLatitude(), location.getLongitude());

                    Bundle bundle = new Bundle();

                    //Change start to string
                    bundle.putString("start", start.toString());

                    Fragment_rides fragment2 = new Fragment_rides();
                    fragment2.setArguments(bundle);

                    /*getFragmentManager()
                            .beginTransaction()
                            .commit();*/



                    Log.d("TAG", "MyLocation: TEST2.0" + start);

                }
            });
        }

    });

        workingHours = view.findViewById(R.id.workingHoursDriver);

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


    private void checkActiveTrip()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        Log.d(TAG, "DRIVERS ID" + user.getUid());
                        if (document.getString("DriverUID").matches(user.getUid()) && (document.getString("Status").matches("Active") || document.getString("Status").matches("PickedUp")))
                        {
                            Intent intent = new Intent(getContext(), DriverMaps.class);
                            startActivity(intent);
                            Log.d(TAG, "DRIVERS ID in IF statement" + user.getUid());
                            activeTrip = true;
                        }
                        else
                        {
                            activeTrip = false;
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