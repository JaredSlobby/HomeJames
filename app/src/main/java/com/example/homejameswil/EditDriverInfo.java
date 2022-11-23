package com.example.homejameswil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class EditDriverInfo extends Fragment
{
    EditText txtName, txtSurname, txtIDNumber, txtCellNumber, txtVehType, txtColour, txtVehNumPlate;
    String uid;
    String TAG = "Firebase";
    FirebaseUser user;
    ImageButton btnBack;
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_driver_info, container, false);

        retrieveDriverInfo();
        GoBack();

        return view;
    }

    private void GoBack()
    {

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Bundle bundle = getArguments();
                String docID = bundle.getString("docID");

                Bundle b = new Bundle();
                b.putString("docID",docID );

                Fragment fragment = new AccountDetailsDriver();
                fragment.setArguments(b);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.commit();
            }
        });
    }

    private void retrieveDriverInfo()
    {

        Bundle bundle = getArguments();
        bundle.getString("docID");

        txtName = view.findViewById(R.id.txtDriverName);
        txtSurname = view.findViewById(R.id.txtDriverSurname);
        txtIDNumber = view.findViewById(R.id.txtDriverIDNumber);
        txtCellNumber = view.findViewById(R.id.txtDriverCellNumber);
        txtVehType = view.findViewById(R.id.txtDriverVehType);
        txtColour = view.findViewById(R.id.txtDriverVehColour);
        txtVehNumPlate = view.findViewById(R.id.txtDriverVehNumPlate);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        String bundleUID = bundle.getString("docID");

        //Read from database specifying with collection
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        if(document.getId().matches(bundleUID))
                        {
                            txtName.setText(document.getString("UserName"));
                            txtSurname.setText(document.getString("UserSurname"));
                            txtIDNumber.setText(document.getString("UserIDNumber"));
                            txtCellNumber.setText(document.getString("UserCellNumber"));
                            txtVehType.setText(document.getString("VehicleType"));
                            txtColour.setText(document.getString("Colour"));
                            txtVehNumPlate.setText(document.getString("VehicleNumPlate"));
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
}