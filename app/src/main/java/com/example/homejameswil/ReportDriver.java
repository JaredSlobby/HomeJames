package com.example.homejameswil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReportDriver extends Fragment
{
    View view;
    AutoCompleteTextView ReportDriver;
    EditText info;
    Button btnReportDriver;
    FirebaseUser user;
    String uid, DriverUID, docID;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_report_driver, container, false);

        DropDown();
        submitDriverReport();


        return view;
    }

    private void DropDown()
    {
        ReportDriver = view.findViewById(R.id.DropDown);

        String[] Reason = new String[]{"Sexual Assault", "Bad Driving", "Inappropriate Behaviour", "Late","Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, Reason);
        ReportDriver.setAdapter(adapter);

        ReportDriver.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Toast.makeText(getActivity(), "" + ReportDriver.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void submitDriverReport()
    {
        Bundle bundleReport = this.getArguments();
        if(bundleReport != null)
        {
            DriverUID = bundleReport.getString("DriverUID");
            docID = bundleReport.getString("docID");

        }

        info = view.findViewById(R.id.Info);
        btnReportDriver = view.findViewById(R.id.btnReportDriver);
        btnReportDriver.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Get logged in user UID
                user = FirebaseAuth.getInstance().getCurrentUser();
                uid = user.getUid();

                //Creating report object
                Map<String, Object> report = new HashMap<>();
                report.put("CustomerUID", uid);
                report.put("DriverUID", DriverUID);
                report.put("Reason", ReportDriver.getText().toString());
                report.put("Info", info.getText().toString());
                report.put("TripID", docID);

                //Writing to Firestore specifying collection path with custom set Document reference
                // Add a new document with a generated ID
                db.collection("Reports").add(report).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                        {
                            @Override
                            public void onSuccess(DocumentReference documentReference)
                            {
                                Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Log.w("TAG", "Error adding document", e);
                            }
                        });

                Toast.makeText(getContext(), "Thank you for your report!", Toast.LENGTH_SHORT).show();

                Fragment fragment = new LandingPage();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.commit();
            }
        });
    }
}