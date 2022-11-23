package com.example.homejameswil;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class Trips extends Fragment
{
    FirebaseUser user;
    String uid;
    ListView printPrevOrders;
    ArrayAdapter<String> adapter;
    ArrayList<String> list;
    ArrayList<String> DriverName;
    String TAG = "Firebase";
    ArrayList<String> DocID;
    ArrayList<String> DateOfPickUp;
    ArrayList<String> DriverUID;
    ArrayList<String> TimeOfPickUp;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_trips, container, false);
        LoadPreviousTrips();
        return view;
    }

    private void LoadPreviousTrips()
    {
        //Connection to database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        printPrevOrders = view.findViewById(R.id.printPrevOrders);

        @SuppressLint("ResourceType") ColorDrawable white = new ColorDrawable(this.getResources().getColor(R.drawable.white));
        printPrevOrders.setDivider(white);
        printPrevOrders.setDividerHeight(3);

        list = new ArrayList();
        DocID = new ArrayList();
        DateOfPickUp = new ArrayList();
        DriverUID = new ArrayList();
        TimeOfPickUp = new ArrayList();

        adapter = new ArrayAdapter(view.getContext(), R.layout.lists, R.id.CategoryNameListView, list);
        printPrevOrders.setAdapter(adapter);

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        //Read from database specifying with collection
        db.collection("Orders").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                if(document.getString("UID").matches(uid) && document.getString("Status").matches("Completed"))
                                {
                                    list.add(document.getString("Date") + "\n" + document.getString("Time"));
                                    DocID.add(document.getId());
                                    DateOfPickUp.add(document.getString("Date"));
                                    DriverUID.add(document.getString("DriverUID"));
                                    TimeOfPickUp.add(document.getString("Time"));
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


        printPrevOrders.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //How to communicate between fragments
                Bundle bundle = new Bundle();
                bundle.putString("docID", DocID.get(position));
                bundle.putString("DateOfPickUp", DateOfPickUp.get(position));
                bundle.putString("DriverUID", DriverUID.get(position));
                bundle.putString("TimeOfPickUp", TimeOfPickUp.get(position));


                Fragment fragment = new PreviousTrips();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.tripsFragment, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

}