package com.example.homejameswil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterDriver extends Fragment
{
    View view;

    Button btn2_signup;
    EditText user_name, pass_word,txtName, txtSurname, txtIDNumber, txtCellNumber, txtVehType, txtVehColour, txtVehNumPlate;
    FirebaseAuth mAuth;

    FirebaseUser user;
    String uid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_register_driver, container, false);

        BottomNavigationView navBar = getActivity().findViewById(R.id.bottom_navigation_admin);
        navBar.setVisibility(View.GONE);

        RegisterDriver();
        return view;
    }

    @SuppressLint("NotConstructor")
    private void RegisterDriver()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        user_name = view.findViewById(R.id.txtDriverEmail);
        pass_word = view.findViewById(R.id.txtDriverPassword);
        txtName = view.findViewById(R.id.txtDriverName);
        txtSurname = view.findViewById(R.id.txtDriverSurname);
        txtIDNumber = view.findViewById(R.id.txtDriverIDNumber);
        txtCellNumber = view.findViewById(R.id.txtDriverCellNumber);
        txtVehType = view.findViewById(R.id.txtDriverVehType);
        txtVehColour = view.findViewById(R.id.txtDriverVehColour);
        txtVehNumPlate = view.findViewById(R.id.txtDriverVehNumPlate);

        btn2_signup = view.findViewById(R.id.btnRegister);
        mAuth = FirebaseAuth.getInstance();
        btn2_signup.setOnClickListener(new View.OnClickListener()
        {
            //Validation

            @Override
            public void onClick(View v)
            {
                String email = user_name.getText().toString().trim();
                String password = pass_word.getText().toString().trim();
                if (email.isEmpty())
                {
                    user_name.setError("Email is empty");
                    user_name.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    user_name.setError("Enter the valid email address");
                    user_name.requestFocus();
                    return;
                }
                if (password.isEmpty())
                {
                    pass_word.setError("Enter the password");
                    pass_word.requestFocus();
                    return;
                }
                if (password.length() < 6)
                {
                    pass_word.setError("Length of the password should be more than 6");
                    pass_word.requestFocus();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {

                            user = FirebaseAuth.getInstance().getCurrentUser();
                            uid = user.getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("Status", "Driver");
                            user.put("Email", user_name.getText().toString());
                            user.put("UserName", txtName.getText().toString());
                            user.put("UserCellNumber", txtCellNumber.getText().toString());
                            user.put("UserSurname", txtSurname.getText().toString());
                            user.put("UserIDNumber", txtIDNumber.getText().toString());
                            user.put("VehicleType", txtVehType.getText().toString());
                            user.put("Colour", txtVehColour.getText().toString());
                            user.put("VehicleNumPlate", txtVehNumPlate.getText().toString());


                            db.collection("Users").document(uid).set(user).addOnSuccessListener(new OnSuccessListener<Void>()
                                    {
                                        @Override
                                        public void onSuccess(Void aVoid)
                                        {
                                            Fragment fragment = new Admin_Drivers();
                                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                                            ft.replace(R.id.frameLayout, fragment);
                                            ft.commit();

                                            //Log.d(TAG, "DocumentSnapshot added with ID:" );
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //Log.w(TAG, "Error adding document", e);
                                        }
                                    });


                            Log.d("TAG","Testing UID Print on Register:" + uid);

                            Toast.makeText(getContext(), "You are successfully Registered", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(getContext(), "You are not Registered! Try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}