package com.example.homejameswil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationBarView;

public class DriverMainActivity extends AppCompatActivity {
    NavigationBarView navigationBarView;
    protected LatLng start = null;
    private GoogleMap mapDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_landing_page);

        //Set default fragment to load
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutDriver, new DriverLandingPage());
        fragmentTransaction.commit();



        navigationBarView = findViewById(R.id.bottom_navigation);

        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.Home:
                        replaceFragment(new DriverLandingPage());
                        return true;
                    case R.id.Rides:
                        replaceFragment(new Fragment_rides());
                        return true;
                    case R.id.Account:
                        replaceFragment(new AccountDetailsDriver());
                        return true;
                }
                return false;
            }
        });
    }

    //Declare fragment methods
    private void replaceFragment(AccountDetailsDriver accountDetailsDriver)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutDriver, accountDetailsDriver);
        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment_rides fragment_rides) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutDriver, fragment_rides);
        fragmentTransaction.commit();
    }

    private void replaceFragment(DriverLandingPage driverLandingPage) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutDriver, driverLandingPage);
        fragmentTransaction.commit();
    }






}