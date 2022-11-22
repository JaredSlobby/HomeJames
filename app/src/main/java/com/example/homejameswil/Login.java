package com.example.homejameswil;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity
{
    private EditText user_name, pass_word;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String uid;
    private static final String ONESIGNAL_APP_ID = "556bf015-31aa-42d9-a448-4642ce2fb4b7";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //Can david see this
        //Can jared see this
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(getApplicationContext());
        OneSignal.setAppId(ONESIGNAL_APP_ID);



        user_name = findViewById(R.id.email);
        pass_word = findViewById(R.id.password);
        Button btn_login = findViewById(R.id.btnLogin);
        Button btn_sign = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener()
                                   {
                                       @Override
                                       public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth)
                                       {
                                           if (firebaseAuth.getCurrentUser() != null)
                                           {
                                               Log.d("TAG", "onComplete: " + firebaseAuth.getCurrentUser().getUid());
                                               //Check if users role is Driver
                                               db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                                                       {
                                                           @Override
                                                           public void onComplete(@NonNull Task<QuerySnapshot> task)
                                                           {
                                                               if (task.isSuccessful())
                                                               {
                                                                   for (QueryDocumentSnapshot document : task.getResult())
                                                                   {
                                                                       if(document.getId().matches(firebaseAuth.getCurrentUser().getUid()) && document.getString("Status").matches("Driver"))
                                                                       {
                                                                           Intent it = new Intent(Login.this, DriverMainActivity.class);
                                                                           startActivity(it);
                                                                       }
                                                                       else if(document.getId().matches(firebaseAuth.getCurrentUser().getUid()) && document.getString("Status").matches("Client"))
                                                                       {
                                                                           setExternalUserID(firebaseAuth.getCurrentUser().getUid());
                                                                           Intent intent = new Intent(Login.this, MainActivity.class);
                                                                           startActivity(intent);
                                                                           Log.d("TAG", "External User ID: " + firebaseAuth.getCurrentUser().getUid());


                                                                       }
                                                                       else if (document.getId().matches(firebaseAuth.getCurrentUser().getUid()) && document.getString("Status").matches("Admin"))
                                                                       {
                                                                           Intent intent = new Intent(Login.this, Admin_Dashboard.class);
                                                                           startActivity(intent);
                                                                       }
                                                                   }
                                                               }
                                                               else
                                                               {
                                                                   //Log.w(TAG, "Error getting documents.", task.getException());
                                                               }
                                                           }
                                                       });
                                           }
                                           else
                                           {
                                               // initiate sign-in
                                           }
                                       }
                                   });

        btn_login.setOnClickListener(v ->
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
                user_name.setError("Enter the valid email");
                user_name.requestFocus();
                return;
            }
            if (password.isEmpty())
            {
                pass_word.setError("Password is empty");
                pass_word.requestFocus();
                return;
            }
            if (password.length() < 6)
            {
                pass_word.setError("Length of password is more than 6");
                pass_word.requestFocus();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("message");

                    myRef.setValue("Hello, World!");

                    user = FirebaseAuth.getInstance().getCurrentUser();
                    uid = user.getUid();

                    Log.d("TAG","Testing UID Print:" + uid);

                    db.collection("UserStatus")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        for (QueryDocumentSnapshot document : task.getResult())
                                        {
                                            if(document.getId().matches(uid) && document.getString("Status").matches("Driver"))
                                            {
                                                Intent it = new Intent(Login.this, DriverMainActivity.class);
                                                startActivity(it);
                                            }
                                            else if(document.getId().matches(uid) && document.getString("Status").matches("Client"))
                                            {
                                                Intent intent = new Intent(Login.this, MainActivity.class);

                                                startActivity(intent);

                                            }
                                            else if(document.getId().matches(uid) && document.getString("Status").matches("Admin"))
                                            {
                                                Intent intent = new Intent(Login.this, Admin_Dashboard.class);
                                                //intent.putExtra("UID", uid);
                                                startActivity(intent);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        //Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                }
                            });

                }
                else
                {
                    Toast.makeText(Login.this, "Please Check Your login Credentials", Toast.LENGTH_SHORT).show();
                }

            });
        });
        btn_sign.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));
    }


    private void setExternalUserID(String uid)
    {
        String externalUserId = uid; // You will supply the external user id to the OneSignal SDK

        Log.d("TAG", "External User ID Inside Method " + externalUserId);


// Setting External User Id with Callback Available in SDK Version 4.0.0+
        OneSignal.setExternalUserId(externalUserId, new OneSignal.OSExternalUserIdUpdateCompletionHandler() {
            @Override
            public void onSuccess(JSONObject results)
            {
                try {
                    if (results.has("push") && results.getJSONObject("push").has("success")) {
                        boolean isPushSuccess = results.getJSONObject("push").getBoolean("success");
                        OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "Set external user id for push status: " + isPushSuccess);
                        Log.d("TAG", "ExternalUserID = " + externalUserId);
                        Toast.makeText(getApplicationContext(), "ExternalUserID = " + externalUserId, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(OneSignal.ExternalIdError error)
            {
                // The results will contain channel failure statuses
                // Use this to detect if external_user_id was not set and retry when a better network connection is made
                OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "Set external user id done with error: " + error.toString());
            }
        });
    }


}
