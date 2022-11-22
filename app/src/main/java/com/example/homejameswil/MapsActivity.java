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
import com.twilio.rest.chat.v1.service.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    ArrayList<Double> cHomeLatitude;
    ArrayList<Double> cHomeLongitude;
    private String API_KEY = "";
    String originAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String API = getString(R.string.MAPS_API_KEY);

        API_KEY = API;

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
        cHomeLatitude = new ArrayList();
        cHomeLongitude = new ArrayList();

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
                                if(document.getId().matches(uid) && document.getString("Status").matches("Client"))
                                {
                                    cName.add(document.get("UserName").toString());
                                    cHomeLatitude.add(document.getDouble("HomeLatitude"));
                                    cHomeLongitude.add(document.getDouble("HomeLongitude"));
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
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
                user.put("HomeLatitude", cHomeLatitude.get(0));
                user.put("HomeLongitude", cHomeLongitude.get(0));
                user.put("DriverUID", "");
                user.put("SMS", "No");
                user.put("PickUpLocation", originAddress);

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


    public void Json(LatLng start, LatLng end) throws IOException
    {
        Log.d(TAG, "Json: " + start.toString() + end.toString());

        //Convert variable start from LatLng to a string
        String startString = start.toString();
        //Remove the brackets from the string
        startString = startString.replace("(", "");
        //Remove lat/lng from the string
        startString = startString.replace("lat/lng: ", "");
        //Remove the bracket and the end of the string
        startString = startString.replace(")", "");
        Log.d(TAG, "Json: " + startString);

        //Convert variable end from LatLng to a string
        String endString = end.toString();
        //Remove the brackets from the string
        endString = endString.replace("(", "");
        //Remove lat/lng from the string
        endString = endString.replace("lat/lng: ", "");
        //Remove the bracket and the end of the string
        endString = endString.replace(")", "");
        //Split up the two strings into two separate strings
        String[] startArray = startString.split(",");
        String[] endArray = endString.split(",");

        HttpUrl mySearchUrl = new HttpUrl.Builder().scheme("https").host("maps.googleapis.com").addPathSegment("maps").addPathSegment("api").addPathSegment("distancematrix").addPathSegment("json").addQueryParameter("origins", startArray[0] + "," + startArray[1] + "&destinations=" + endString + "&units=" + "metric" + "&key=" + API_KEY).build();
        String afterDecode = URLDecoder.decode(mySearchUrl.toString(), "UTF-8");

        Log.d(TAG, "Json: " + mySearchUrl.toString());

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder().url(afterDecode).method("GET", null).build();
        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {}
            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                Log.d(TAG, "onResponse: " + response.body().string());
                Thread thread = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Response response = client.newCall(request).execute();
                            JSONObject jsonObject = new JSONObject(response.body().string());// parse response into json object
                            //Pull origin_addresses from JSON
                            JSONArray destinationAddresses = jsonObject.getJSONArray("origin_addresses");
                                    //Format origin_addresses
                                    originAddress = destinationAddresses.toString();
                            originAddress = originAddress.replace("[", "");
                            originAddress = originAddress.replace("]", "");
                            originAddress = originAddress.replace("\"", "");
                        }
                        catch (JSONException | IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                thread.setPriority(Thread.MAX_PRIORITY);
                thread.start();

            }
        });
    }

    LatLng start = new LatLng(53.3498, -6.2603);







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