package com.example.homejameswil;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AdminReports extends Fragment
{
    View view;
    ArrayList<String> docID;
    ArrayList<String> CustomerUID;
    ArrayList<String> driverUID;
    ArrayList<String> info;
    ArrayList<String> reason;
    ArrayList<String> tripID;

    ListView listReports;
    ArrayAdapter<String> adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin_reports, container, false);


        getReports();
        return view;
    }


    private void getReports()
    {

        docID = new ArrayList<>();
        CustomerUID = new ArrayList<>();
        driverUID= new ArrayList<>();
        info = new ArrayList<>();
        reason = new ArrayList<>();
        tripID = new ArrayList<>();


        listReports = view.findViewById(R.id.listReports);
        @SuppressLint("ResourceType") ColorDrawable white = new ColorDrawable(this.getResources().getColor(R.drawable.white));
        listReports.setDivider(white);
        adapter = new ArrayAdapter(view.getContext(), R.layout.lists, R.id.CategoryNameListView, docID);
        listReports.setAdapter(adapter);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Reports").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot document : task.getResult())
                    {
                        docID.add(document.getId());
                        CustomerUID.add(document.getString("CustomerUID"));
                        driverUID.add(document.getString("DriverUID"));
                        info.add(document.getString("Info"));
                        reason.add(document.getString("Reason"));
                        tripID.add(document.getString("TripID"));
                    }
                }
                else
                {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
                adapter.notifyDataSetChanged();
            }
        });

        listReports.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //How to communicate between fragments
                Bundle bundle = new Bundle();
                bundle.putString("docID", docID.get(position));
                bundle.putString("CustomerUID", CustomerUID.get(position));
                bundle.putString("DriverUID", driverUID.get(position));
                bundle.putString("Info", info.get(position));
                bundle.putString("Reason", reason.get(position));
                bundle.putString("TripID", tripID.get(position));


                /*Fragment fragment = new ReportsInfo();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.AdminReportList, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();*/

                Fragment fragment = new ReportsInfo();
                fragment.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.commit();
            }
        });
    }
}