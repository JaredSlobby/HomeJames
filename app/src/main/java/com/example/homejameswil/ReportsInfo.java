package com.example.homejameswil;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class ReportsInfo extends Fragment
{
    View view;
    String TAG = "Firebase";
    TextView DriverName, ReportReason, ReportInfo;

    ArrayList<String> list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.fragment_reports_info, container, false);
         getInfo();
         return view;
    }

    private void getInfo()
    {

        DriverName = view.findViewById(R.id.nameDriver);
        ReportReason = view.findViewById(R.id.ReportReason);
        ReportInfo = view.findViewById(R.id.ReportInfo);



        Bundle bundle = this.getArguments();

        if(bundle != null)
        {
            String docID = bundle.getString("docID");
            String CustomerUID = bundle.getString("CustomerUID");
            String DriverUID = bundle.getString("DriverUID");
            String Info = bundle.getString("Info");
            String Reason = bundle.getString("Reason");
            String TripID = bundle.getString("TripID");

            Log.d(TAG,"Admin Complaints" + docID + CustomerUID + DriverUID + Info + Reason + TripID);
        }
    }
}