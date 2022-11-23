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
import android.widget.Button;
import android.widget.ListView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
public class Admin_Drivers extends Fragment
{
    View view;
    FirebaseUser user;
    String uid;
    ArrayList<String> docID;
    ArrayList<String> driverName;
    ListView listDrivers;
    ArrayAdapter<String> adapter;
    Button btnRegister;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin__drivers, container, false);
        driversAccounts();
        registerDriver();
        return view;
    }

    private void driversAccounts()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        docID = new ArrayList<>();
        driverName = new ArrayList<>();

        listDrivers = view.findViewById(R.id.listDrivers);

        adapter = new ArrayAdapter(view.getContext(), R.layout.whitetext, driverName);
        listDrivers.setAdapter(adapter);

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
                        if(document.getString("Status").matches("Driver"))
                        {
                            docID.add(document.getId());
                            driverName.add(document.getString("UserName") + " " + document.getString("UserSurname"));
                        }
                    }
                }
                else
                {
                    Log.w("TAG", "Error getting documents.", task.getException());
                }
                adapter.notifyDataSetChanged();
            }
        });
        //set on click listener
        listDrivers.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Bundle bundle = new Bundle();
                bundle.putString("docID", docID.get(position));
                bundle.putString("userID", "True");

                //String DocumentID = docID.get(position);

                Fragment fragment = new AccountDetailsDriver();
                fragment.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.commit();
            }
        });
    }

    private void registerDriver()
    {
        btnRegister = view.findViewById(R.id.btnRegisterDriver);

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Fragment fragment = new RegisterDriver();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.commit();
            }
        });
    }
}