package com.example.homejameswil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ReportsInfo extends Fragment {
    View view;
    String TAG = "Firebase";
    TextView DriverName, ReportReason, ReportInfo;
    ImageButton goBack;

    ArrayList<String> list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reports_info, container, false);
        getInfo();
        goBack();
        return view;
    }

    private void goBack()
    {
        goBack = view.findViewById(R.id.btnBack);
        goBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Fragment fragment = new AdminReports();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.commit();
            }
        });

    }

    private void getInfo() {
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            String docID = bundle.getString("docID");
            String CustomerUID = bundle.getString("CustomerUID");
            String DriverUID = bundle.getString("DriverUID");
            String Info = bundle.getString("Info");
            String Reason = bundle.getString("Reason");
            String TripID = bundle.getString("TripID");
            String driverName = bundle.getString("DriverName");


            DriverName = view.findViewById(R.id.nameDriver);
            ReportReason = view.findViewById(R.id.ReportReason);
            ReportInfo = view.findViewById(R.id.ReportInfo);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            //Read from database specifying with collection



            ReportReason.setText("Report Reason: " + Reason);
            ReportInfo.setText("Report Info: " + Info);
            DriverName.setText("Driver Name: " + driverName);

            Log.d(TAG, "Admin Complaints" + docID + CustomerUID + DriverUID + Info + Reason + TripID);


        }
    }
}