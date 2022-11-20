package com.example.homejameswil;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class EditPersonalDetails extends Fragment
{
    EditText txtName, txtSurname, txtIDNumber, txtCellNumber, txtStreetName, txtSuburb, txtEmail;
    String TAG = "Firebase";
    FirebaseUser user;
    FirebaseAuth auth;
    ImageButton btnBack;
    Button btnUpdate;

    String uid;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_personal_details, container, false);

        RetrieveUserDetails();



        GoBack();
        Update();

        return view;
    }

    private void RetrieveUserDetails()
    {
        txtName = view.findViewById(R.id.Name);
        txtSurname = view.findViewById(R.id.Surname);
        txtIDNumber = view.findViewById(R.id.IDNumber);
        txtCellNumber = view.findViewById(R.id.CellNumber);
        txtStreetName = view.findViewById(R.id.Address);
        txtSuburb = view.findViewById(R.id.Suburb);
        txtEmail = view.findViewById(R.id.email);


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        /*Read from database specifying with collection
        db.collection("UserStatus").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        if(document.getId().matches(uid))
                        {
                            Email.setText(document.getString("Email"));
                        }
                    }
                }
                else
                {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });*/



        //Read from database specifying with collection
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                if(document.getId().matches(uid))
                                {
                                    txtName.setText(document.getString("UserName"));
                                    txtSurname.setText(document.getString("UserSurname"));
                                    txtIDNumber.setText(document.getString("UserIDNumber"));
                                    txtCellNumber.setText(document.getString("UserCellNumber"));
                                    txtStreetName.setText(document.getString("UserStreetName"));
                                    txtSuburb.setText(document.getString("UserSuburb"));
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

    private void EditPersonalDetailsCustomer()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Get logged in users email
        String email = user.getEmail();
        Map<String, Object> user = new HashMap<>();
        //Map<String, Object> userStatus = new HashMap<>();

        user.put("Email", email);
        user.put("Status", "Client");
        user.put("UserName", txtName.getText().toString());
        user.put("UserSurname", txtSurname.getText().toString());
        user.put("UserIDNumber", txtIDNumber.getText().toString());
        user.put("UserCellNumber", txtCellNumber.getText().toString());
        user.put("UserStreetName", txtStreetName.getText().toString());
        user.put("UserSuburb", txtSuburb.getText().toString());

        /*userStatus.put("Email", Email.getText().toString());


        db.collection("UserStatus").document(uid)
                .set(userStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot added with ID:" );
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });*/


        db.collection("Users").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Log.d(TAG, "DocumentSnapshot added with ID:" );
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });


        /*FirebaseUser user_ = FirebaseAuth.getInstance().getCurrentUser();

        String email = "";
        final String password = "";

        // Get auth credentials from the user for re-authentication
        AuthCredential credential = EmailAuthProvider.getCredential(email, password); // Current Login Credentials

        // Prompt the user to re-provide their sign-in credentials
        user_.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Log.d("value", "User re-authenticated.");

                // Now change your email address \\
                //----------------Code for Changing Email Address----------\\
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.updateEmail(Email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Email Changed" + " Current Email is " + Email.getText().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });*/
    }


    private void GoBack()
    {

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Fragment fragment = new AccountDetails();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.commit();
            }
        });
    }

    private void Update()
    {
        btnUpdate = view.findViewById(R.id.btnSave);

        btnUpdate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EditPersonalDetailsCustomer();
                Fragment fragment = new AccountDetails();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.commit();
            }
        });
    }

}