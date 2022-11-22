package com.example.homejameswil;

import static androidx.core.content.ContextCompat.getSystemService;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.Loader;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.homejameswil.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    boolean isPermissionGranted;
    Location myLocation = null;
    protected LatLng customerLocation = null;
    private Double customerLat = null;
    private Double customerLong = null;
    private static String TAG = "MapsActivity";
    TextView mDisplayDate, mDisplayTime;
    BottomSheetDialog bottomSheetDialog;
    View bottomSheetView;
    FloatingActionButton fab;
    NumberPicker numberPicker1, numberPicker2;
    DatePicker datePicker;
    Button btnTrip;
    FirebaseUser user;
    String uid;

    ArrayList<String> cName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestPermission();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Connection to database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        cName = new ArrayList();

        //Read from database specifying with collection
        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                if(document.getId().matches(uid))
                                {
                                    cName.add(document.getString("UserName"));

                                }
                            }
                        }
                        else
                        {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        buttonPress();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));
        mMap = googleMap;

        fab = findViewById(R.id.requestTrip);
        int color = Color.parseColor("#FFFFFF");
        fab.setColorFilter(color);

        fab.setTooltipText("Request Trip");


        if (!success)
        {
            Log.e(TAG, "Style parsing failed.");
        }
        // Add a marker in Sydney and move the camera
        getCurrentLocation();
        //notification();
    }

    //Request permission for location
    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    //Check if permission is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionGranted = true;
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                getCurrentLocation();
            }
            else
            {
                requestPermission();
            }
        }
    }

    //Supress warning for location

    boolean myLocationFound = false;
    @SuppressLint("MissingPermission")
    //Get users current location and have UI
    private void getCurrentLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        //Get my current location
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener()
        {
            @Override
            public void onMyLocationChange(Location location)
            {
                myLocation = location;
                //Convert location to LatLong
                customerLocation = new LatLng(location.getLatitude(), location.getLongitude());

                //Convert customerLocation to separate strings
                customerLat = Double.valueOf(customerLocation.latitude);
                customerLong = Double.valueOf(customerLocation.longitude);



                Log.d(TAG, "MyLocation: TEST2.0" + customerLocation);

                LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ltlng, 16f);

                if (!myLocationFound)
                {
                    mMap.animateCamera(cameraUpdate);
                    myLocationFound = true;
                }
            }
        });
    }

    @SuppressLint("WrongConstant")
    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_MAX;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void notification()
    {
        createNotificationChannel();
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.clubs)
                .setContentTitle("My notification")
                .setContentText("Hello World!")
                //.setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        //Display notification
        notificationManager.notify(1, builder.build());
    }

    private void DateAndTime()
    {
        mDisplayDate = bottomSheetView.findViewById(R.id.Date_pickerAction);
        mDisplayTime = bottomSheetView.findViewById(R.id.Time_picker);

        numberPicker1 = bottomSheetView.findViewById(R.id.numberPicker1);
        numberPicker2 = bottomSheetView.findViewById(R.id.numberPicker2);

        //Set number picker values
        numberPicker1.setMinValue(0);
        numberPicker1.setMaxValue(23);
        numberPicker1.setWrapSelectorWheel(true);
        numberPicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker2.setMinValue(0);
        numberPicker2.setMaxValue(59);
        numberPicker2.setWrapSelectorWheel(true);
        numberPicker2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        //Get current time and convert to a int
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        numberPicker1.setValue(hour);
        numberPicker2.setValue(minute);

        //Declare Date Picker
        datePicker = bottomSheetView.findViewById(R.id.datePicker);
        //Set Date Picker values
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        //Set Date Picker to current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        datePicker.init(year, month, day, null);
        mDisplayDate.setText(day + "/" + (month + 1) + "/" + year);
        mDisplayTime.setText(hour + ":" + minute);

        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener()
        {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2)
            {
                //Update text view
                mDisplayDate.setText(i2 + "/" + (i1 + 1) + "/" + i);
            }
        });
        //Change mDisplayTime and mDisplayDate based on number picker
        numberPicker1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mDisplayTime.setText(String.format("%02d", newVal) + ":" + String.format("%02d", numberPicker2.getValue()));
            }
        });
        numberPicker2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                mDisplayTime.setText(String.format("%02d", numberPicker1.getValue()) + ":" + String.format("%02d", newVal));
            }
        });
    }

    private void BottomSheetDialog()
    {
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
        DateAndTime();
        setBtnTrip();
    }

    private void setBtnTrip()
    {
        btnTrip = bottomSheetView.findViewById(R.id.btnTrip);
        btnTrip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String Time = mDisplayTime.getText().toString();
                String Date = mDisplayDate.getText().toString();
                Toast.makeText(MapsActivity.this, "Trip Request Sent" + Time + ": "+ Date, Toast.LENGTH_SHORT).show();


                // Get logged in user UID
                user = FirebaseAuth.getInstance().getCurrentUser();
                uid = user.getUid();

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                //Creating user object
                Map<String, Object> user = new HashMap<>();
                user.put("UID", uid);
                user.put("Time", Time);
                user.put("Date", Date);
                user.put("Latitude" , customerLat);
                user.put("Longitude", customerLong);
                user.put("Status", "Pending");
                user.put("Name", cName.get(0));
                user.put("HomeLatitude", );
                user.put("HomeLongitude", );

                //Writing to Firestore specifying collection path with custom set Document reference
                // Add a new document with a generated ID
                db.collection("Orders").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                        {
                            @Override
                            public void onSuccess(DocumentReference documentReference)
                            {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });

            }
        });
    }

    private void buttonPress()
    {
        bottomSheetDialog = new BottomSheetDialog(MapsActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_sheet, (LinearLayout) findViewById(R.id.bottomSheet));

        fab = findViewById(R.id.requestTrip);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BottomSheetDialog();
            }
        });
    }


    private void trackDriver()
    {
        //Check realtime database for driver location
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("DriverLocation");
        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //Get driver location
                String driverLat = dataSnapshot.child("Latitude").getValue().toString();
                String driverLong = dataSnapshot.child("Longitude").getValue().toString();
                //Convert to double
                double dLat = Double.parseDouble(driverLat);
                double dLong = Double.parseDouble(driverLong);
                //Add marker to map
                LatLng driverLocation = new LatLng(dLat, dLong);
                mMap.addMarker(new MarkerOptions().position(driverLocation).title("Driver Location"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}