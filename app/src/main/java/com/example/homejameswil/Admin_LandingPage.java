package com.example.homejameswil;

import android.content.Intent;
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

import java.util.Calendar;

public class Admin_LandingPage extends Fragment
{

    View view;
    String TAG = "Firebase";
    FirebaseUser user;
    String uid;
    TextView welcome, workingHours;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin__landing_page, container, false);

        Welcome();

        return view;
    }


    private void Welcome()
    {
        //Welcome message
        user = FirebaseAuth.getInstance().getCurrentUser();
        welcome = view.findViewById(R.id.welcome);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
}