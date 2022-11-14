package com.example.homejameswil;

import android.Manifest;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_driver_landing_page, container, false);
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
            if (!success) {
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
                workingHours.setText(MondayToSaturday);
                break;
            case Calendar.TUESDAY:
                workingHours.setText(MondayToSaturday);
                break;
            case Calendar.WEDNESDAY:
                workingHours.setText(MondayToSaturday);
                break;
            case Calendar.THURSDAY:
                workingHours.setText(MondayToSaturday);
                break;
            case Calendar.FRIDAY:
                workingHours.setText(MondayToSaturday);
                break;
            case Calendar.SATURDAY:
                workingHours.setText(MondayToSaturday);
                break;
        }
    }
}