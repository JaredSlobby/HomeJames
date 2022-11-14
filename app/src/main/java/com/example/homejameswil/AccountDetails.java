package com.example.homejameswil;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
public class AccountDetails extends Fragment
{
    TextView name, idNumber, address, suburb, cellNumber, email;
    FirebaseUser user;
    String uid;
    Button btnEdit, btnSignOut;
    String TAG = "Firebase";
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_account_details, container, false);
        RetrieveDetails();
        SignOutAndEdit();
        return view;
    }

    private void RetrieveDetails()
    {
        name = view.findViewById(R.id.name);
        idNumber = view.findViewById(R.id.idNumber);
        address = view.findViewById(R.id.address);
        suburb = view.findViewById(R.id.suburb);
        cellNumber = view.findViewById(R.id.cellNumber);
        email = view.findViewById(R.id.email);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

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
                                    final DocumentReference docRef = db.collection("Users").document(uid);
                                    docRef.addSnapshotListener(new EventListener<DocumentSnapshot>()
                                    {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e)
                                        {
                                            if (e != null)
                                            {
                                                Log.w(TAG, "Listen failed.", e);
                                                return;
                                            }
                                            if (snapshot != null && snapshot.exists())
                                            {
                                                Log.d(TAG, "Current data: " + snapshot.getData());
                                            }
                                            else
                                            {
                                                Log.d(TAG, "Current data: null");
                                            }
                                        }
                                    });
                                    //textView.setText(document.getString("UserName"));

                                    email.setText(document.getString("Email"));
                                    name.setText(document.getString("UserName") + " " + document.getString("UserSurname"));
                                    idNumber.setText(document.getString("UserIDNumber"));
                                    cellNumber.setText(document.getString("UserCellNumber"));
                                    address.setText(document.getString("UserStreetName"));
                                    suburb.setText(document.getString("UserSuburb"));
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

    private void SignOutAndEdit()
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
        btnEdit = view.findViewById(R.id.btnEditCustomer);
        btnEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getFragmentManager().beginTransaction().replace(R.id.frameLayout, new EditPersonalDetails()).commit();
            }
        });
    }

}