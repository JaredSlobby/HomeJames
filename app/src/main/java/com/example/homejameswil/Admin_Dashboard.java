package com.example.homejameswil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationBarView;

public class Admin_Dashboard extends AppCompatActivity
{
    NavigationBarView navigationBarView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);


        navigationBarView = findViewById(R.id.bottom_navigation_admin);


        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.Home:
                        replaceFragment(new LandingPage());
                        return true;
                    case R.id.Drivers:
                        replaceFragment(new Admin_Drivers());
                        return true;
                    case R.id.Account:
                        replaceFragment(new AccountDetails());
                        return true;
                }
                return false;
            }
        });
    }

    private void replaceFragment(Admin_Drivers admin_drivers)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, admin_drivers);
        fragmentTransaction.commit();
    }

    private void replaceFragment(LandingPage landingPage)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, landingPage);
        fragmentTransaction.commit();
    }
    private void replaceFragment(AccountDetails accountDetails)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, accountDetails);
        fragmentTransaction.commit();
    }


}