package com.example.homejameswil;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class AccountDetailsDriver extends Fragment
{
    TextView name, idNumber, numPlate, vehBrand, colour, cellNumber;
    View view;
    RatingBar rating;
    Button btnSignOut;

    String TAG = "Firebase";

    FirebaseUser user;
    String uid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_account_details_driver, container, false);
        SignOut();
        RetrieveDetails();
        rating();

        return view;
    }

    private void RetrieveDetails()
    {
        name = view.findViewById(R.id.nameDriver);
        idNumber = view.findViewById(R.id.idNumberDriver);
        rating = view.findViewById(R.id.ratingBar);
        numPlate = view.findViewById(R.id.numPlate);
        vehBrand = view.findViewById(R.id.vehBrand);
        colour = view.findViewById(R.id.vehColour);
        cellNumber = view.findViewById(R.id.cellNumber);

        //Connection to database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Bundle bundle = this.getArguments();

        //Jared

        if(bundle != null)
        {
            bundle.getString("docID");
            bundle.getString("userID");
        }

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(bundle.getString("userID").equals("True"))
        {
            uid = bundle.getString("docID");
            btnSignOut.setVisibility(View.GONE);
        }
        else {
            uid = user.getUid();
        }

                //Read from database specifying with collection
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                if(document.getId().matches(uid))
                                {
                                    name.setText(document.getString("UserName") + " " + document.getString("UserSurname"));
                                    idNumber.setText(document.getString("UserIDNumber"));
                                    cellNumber.setText(document.getString("UserCellNumber"));
                                    vehBrand.setText("Brand: " + document.getString("VehicleType"));
                                    numPlate.setText("Registration Plate: " + document.getString("VehicleNumPlate"));
                                    colour.setText("Colour: " + document.getString("Colour"));
                                }
                            }
                        }
                        else
                        {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void rating()
    {
        //Set ratingBar
        rating.setRating(3.25f);
    }

    private void getBundle()
    {

    }

    private void SignOut()
    {
        btnSignOut = view.findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
            }
        });
    }
}
