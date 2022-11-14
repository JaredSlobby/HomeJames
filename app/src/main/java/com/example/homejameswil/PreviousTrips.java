package com.example.homejameswil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    TextView  TripTimes, ReportDriver;
    FirebaseUser user;
    String uid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_previous_trips, container, false);

        TripTimes = view.findViewById(R.id.TripTimes);
        ReportDriver = view.findViewById(R.id.ReportDriver);


        tripInformation();
        ReportDriver();

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
            TripTimes.setText(bundle.getString("TimeOfPickUp"));

            String DriverUID = bundle.getString("DriverUID");

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
        ReportDriver.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getFragmentManager().beginTransaction().replace(R.id.frameLayout, new ReportDriver()).commit();
            }
        });
    }


}