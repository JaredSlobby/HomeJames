package com.example.homejameswil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

public class PreviousTrips extends Fragment
{
    View view;
    TextView  TripTimes, ReportDriver, DriverName, orderLocation, home, tripdate, triptime, triptimedropoff;
    Button btnReport;
    FirebaseUser user;
    String uid, DriverUID, docID;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_previous_trips, container, false);

        TripTimes = view.findViewById(R.id.TripTimes);
        DriverName = view.findViewById(R.id.nameDriver);
        orderLocation = view.findViewById(R.id.LocationInformation);
        home = view.findViewById(R.id.LocationHome);
        tripdate = view.findViewById(R.id.TripDate);
        triptime = view.findViewById(R.id.TripTimes);
        triptimedropoff = view.findViewById(R.id.TripTimesDropOff);
        btnReport = view.findViewById(R.id.btnReport);
        ReportDriver = view.findViewById(R.id.btnReport);

        ReportDriver();

        tripInformation();


        return view;
    }

    private void tripInformation()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        Bundle bundle = this.getArguments();

        if(bundle != null)
        {
            //testing.setText(bundle.getString("docID"));
            //testing.setText(bundle.getString("DateOfPickUp"));
            //testing.setText(bundle.getString("DriverUID"));
            //TripTimes.setText(bundle.getString("TimeOfPickUp"));


            DriverUID = bundle.getString("DriverUID");
            docID = bundle.getString("docID");

            db.collection("Orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
            {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task)
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : task.getResult())
                        {
                            if(document.getId().matches(docID))
                            {
                                //testing.setText(document.getString("UserName") + " " + document.getString("UserSurname"));
                                orderLocation.setText(document.getString("PickUpLocation"));
                                tripdate.setText(document.getString("Date"));
                                triptime.setText("Pickup Time: " + document.getString("TimeOfPickUp"));
                                triptimedropoff.setText("Drop off Time: " + document.getString("TimeDroppedOff"));
                            }
                        }
                    }
                    else
                    {
                        Log.w("TAG", "Error getting documents.", task.getException());
                    }
                }
            });

            //Check database for DriverUID
            db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
            {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task)
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : task.getResult())
                        {
                            if(document.getId().matches(DriverUID))
                            {
                                //testing.setText(document.getString("UserName") + " " + document.getString("UserSurname"));
                                DriverName.setText(document.getString("UserName") + " " + document.getString("UserSurname"));

                            }
                            if(document.getId().matches(user.getUid()))
                            {
                                home.setText(document.getString("UserStreetName") + " " + document.getString("UserSuburb"));
                            }
                        }
                    }
                    else
                    {
                        Log.w("TAG", "Error getting documents.", task.getException());
                    }
                }
            });
        }
    }


    private void ReportDriver()
    {
        btnReport.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Bundle bundleReport = new Bundle();
                bundleReport.putString("docID", docID);
                bundleReport.putString("DriverUID", DriverUID);

                ReportDriver();
                Fragment fragment = new ReportDriver();
                fragment.setArguments(bundleReport);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.commit();
            }
        });

    }


}