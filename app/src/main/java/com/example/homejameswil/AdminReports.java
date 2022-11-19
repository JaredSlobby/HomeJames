package com.example.homejameswil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


        listReports = view.findViewById(R.id.listReports);

        adapter = new ArrayAdapter(view.getContext(), R.layout.whitetext, docID);
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
                    }
                }
                else
                {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}