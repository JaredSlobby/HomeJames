package com.example.homejameswil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
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

public class Fragment_rides extends Fragment
{

    String TAG = "Firebase";

    FirebaseUser user;
    protected LatLng start = null;
    protected LatLng home = null;
    protected LatLng end = null;
    String uid;
    ListView listview;
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter1;
    ArrayList<String> listTime;
    ArrayList<String> list;
    ArrayList<String> docID;
    ArrayList<String> printList;
    ArrayList<String> time;
    ArrayList<Double> latitude;
    ArrayList<Double> longitude;

    ArrayList<String> clientDate;
    ArrayList<Double> clientLatitude;
    ArrayList<Double> clientLongitude;
    ArrayList<String> clientTime;
    ArrayList<String> clientUID;
    ArrayList<String> clientStatus;
    ArrayList<String> clientName;

    String UID;
    private BottomSheetDialog bottomSheetDialog;
    private View bottomSheetView;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    private String distanceString;
    private String durationString;
    private double distanceHome;
    private double durationHome;
    private String distanceStringHome;
    private String durationStringHome;
    TextView TotalTime;
    TextView TotalDistance;
    private String Units = "metric";

    String cDate;
    Double cLatitude;
    Double cLongitude;
    String cTime;
    String cUID;
    String cStatus;
    String cName;


    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_rides, container, false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        PrintOrders();
        return view;
    }

    private void PrintOrders()
    {
        //Print list of orders
        //Connection to database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        list = new ArrayList();
        printList = new ArrayList();
        latitude = new ArrayList();
        longitude = new ArrayList();
        time = new ArrayList();
        docID = new ArrayList();

        clientDate = new ArrayList();
        clientLatitude = new ArrayList();
        clientLongitude = new ArrayList();
        clientTime = new ArrayList();
        clientUID = new ArrayList();
        clientStatus = new ArrayList();
        clientName = new ArrayList();

        listview = view.findViewById(R.id.ridesList);

        @SuppressLint("ResourceType") ColorDrawable white = new ColorDrawable(this.getResources().getColor(R.drawable.white));
        listview.setDivider(white);
        listview.setDividerHeight(3);

        adapter = new ArrayAdapter(view.getContext(), R.layout.whitetext, printList);
        listview.setAdapter(adapter);


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
                                if(document.getString("Status").matches("Pending"))
                                {
                                    list.add(document.getString("UID"));
                                    printList.add("Client Name: " + document.getString("Name") + "\n" + "Date: " + document.getString("Date") + "\n" +
                                            "Pickup Time: " + document.getString("Time"));
                                    time.add(document.getString("Time"));
                                    docID.add(document.getId());

                                    clientDate.add(document.getString("Date"));
                                    clientLatitude.add(document.getDouble("Latitude"));
                                    clientLongitude.add(document.getDouble("Longitude"));
                                    clientTime.add(document.getString("Time"));
                                    clientUID.add(document.getString("UID"));
                                    clientStatus.add(document.getString("Status"));
                                    clientName.add(document.getString("Name"));

                                    latitude.add(document.getDouble("Latitude"));
                                    longitude.add(document.getDouble("Longitude"));
                                    //list.add(document.getString("Time"));
                                    Log.d(TAG, "Please print: " + list);
                                    Log.d(TAG, "Please print Lat: " + clientLatitude + "Long" + clientLongitude);
                                }
                            }
                        }
                        else
                        {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        //[Cursor parking]
        //[              ]
        //[              ]
        //[              ]
        //[              ]

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
                bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_bottom_sheet_driver, view.findViewById(R.id.bottomSheetTrip));

                //Call method from fragment DriverLandingPage getCurrentLocation

                getLastLocation();

                String UID = list.get(position);

                String DocID = docID.get(position);

                 cDate = clientDate.get(position);
                 cLatitude = clientLatitude.get(position);
                 cLongitude = clientLongitude.get(position);
                 cTime = clientTime.get(position);
                 cUID = clientUID.get(position);
                 cStatus = clientStatus.get(position);
                 cName = clientName.get(position);

                Log.d(TAG, "DocID: " + DocID);

                //Get latitude and longitude from list view
                String lat = latitude.get(position).toString();
                String lon = longitude.get(position).toString();

                //Convert latitude and longitude to LatLong
                end = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));

                ReadUserHome(UID);

                    Log.d(TAG, "Start Driver: " + start);
                    Log.d(TAG, "Start Driver END: " + end);
                    Log.d(TAG, "Start Driver HOME: " + home);

                Log.d(TAG, "Distance JSON: " + distanceString);
                Log.d(TAG, "Duration JSON: " + durationString);


                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();


                Button btn = bottomSheetView.findViewById(R.id.btnTrip);
                btn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        Intent intent = new Intent(getActivity(), DriverMaps.class);
                        startActivity(intent);
                        user.getUid();

                        //Read from database specifying with collection
                        db.collection("Orders")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult())
                                            {
                                                if(document.getId().matches(DocID))
                                                {

                                                    cDate = document.getString("Date");
                                                    cLatitude = document.getDouble("Latitude");
                                                    cLongitude = document.getDouble("Longitude");
                                                    cTime = document.getString("Time");
                                                    cUID = document.getString("UID");
                                                    cStatus = document.getString("Status");
                                                    cName = document.getString("Name");
                                                }
                                            }
                                        }
                                        else
                                        {
                                            Log.w(TAG, "Error getting documents.", task.getException());
                                        }
                                    }
                                });

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        user = FirebaseAuth.getInstance().getCurrentUser();
                        uid = user.getUid();

                        //Creating user object
                        //Setting text boxes to user object defining node names
                        Map<String, Object> user = new HashMap<>();

                        Log.d(TAG, "Driver UID: " + uid);


                        user.put("Date", cDate );
                        user.put("Latitude", cLatitude );
                        user.put("Longitude", cLongitude );
                        user.put("Time", cTime );
                        user.put("UID", cUID );
                        user.put("DriverUID", uid );
                        user.put("Status", "Active");
                        user.put("Name", cName);

                        //Writing to Firestore specifying collection path with custom set Document reference
                        db.collection("Orders").document(DocID)
                                .set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>()
                                {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot added with ID:" );
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
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });
    }



    private void ReadUserHome(String UID)
    {
        //Connection to database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();


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
                        Log.d(TAG, "DocumentSnapshot data: " + UID);
                        Log.d(TAG, "DocumentSnapshot data: " + document.getId());
                            if(document.getId().matches(UID))
                            {
                                double latitude = document.getDouble("HomeLatitude");
                                double longitude = document.getDouble("HomeLongitude");

                                home = new LatLng(latitude, longitude);
                                Log.d(TAG, "Home Location " + home);

                                try
                                {
                                    JsonFile(start, end);
                                    JsonFileHome(end, home);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }



                                Log.d(TAG,"Saved Latitude: " + latitude + " Saved Longitude: " + longitude);
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

    /*private void SaveUserHome()
    {
        //Connection to database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        //Creating user object
        //Setting text boxes to user object defining node names
        Map<String, Object> user = new HashMap<>();
        user.put("Latitude", );
        user.put("Longitude", );


        //Writing to Firestore specifying collection path with custom set Document reference
        db.collection("UserHome").document(uid)
                .set(user)
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
                });
    }*/



    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        }
                        else
                        {
                            Log.d(TAG, "Location Working: " + location.getLatitude() + " " + location.getLongitude());
                            start = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
            } else {
                Toast.makeText(getActivity(), "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult)
        {
            Location mLastLocation = locationResult.getLastLocation();
            Log.d(TAG, "Location: " + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }



    public void JsonFile(LatLng start, LatLng end) throws IOException
    {
        Log.d(TAG, "Json: " + start.toString());
        Log.d(TAG, "Json END LATLONG: " + end.toString());

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

        HttpUrl mySearchUrl = new HttpUrl.Builder().scheme("https").host("maps.googleapis.com").addPathSegment("maps").addPathSegment("api").addPathSegment("distancematrix").addPathSegment("json").addQueryParameter("origins", startArray[0] + "," + startArray[1] + "&destinations=" + endString + "&units=" + Units + "&key=AIzaSyANNxd0f6zOf_VvWwI-B3bY0rOblhUW410").build();
        String afterDecode = URLDecoder.decode(mySearchUrl.toString(), "UTF-8");

        Log.d(TAG, "Json: " + mySearchUrl.toString());

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder().url(afterDecode).method("GET", null).build();
        client.newCall(request).enqueue(new Callback() {
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
                            //Pull our duration and distance from the json object
                            JSONArray rows = jsonObject.getJSONArray("rows");
                            JSONObject elements = rows.getJSONObject(0);
                            JSONArray elementsArray = elements.getJSONArray("elements");
                            JSONObject distance = elementsArray.getJSONObject(0);
                            JSONObject duration = elementsArray.getJSONObject(0);
                            JSONObject distanceObject = distance.getJSONObject("distance");
                            JSONObject durationObject = duration.getJSONObject("duration");
                            distanceString = distanceObject.getString("text");
                            durationString = durationObject.getString("text");
                            Log.d(TAG, "Distance To Customer: " + distanceString + " Duration To Customer: " + durationString);
                            //Update UI using method


                            //format distance string and change to double
                            String distanceStringHomeFormatted = distanceString.replace(" km", "");
                            distanceHome = Double.parseDouble(distanceStringHomeFormatted);
                            //format duration string and change to double
                            String durationStringHomeFormatted = durationString.replace(" mins", "");
                            durationHome = Double.parseDouble(durationStringHomeFormatted);




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

    public void JsonFileHome(LatLng start, LatLng end) throws IOException
    {
        Log.d(TAG, "JsonFileHome: " + start.toString());
        Log.d(TAG, "JsonFileHome: " + end.toString());

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

        HttpUrl mySearchUrl = new HttpUrl.Builder().scheme("https").host("maps.googleapis.com").addPathSegment("maps").addPathSegment("api").addPathSegment("distancematrix").addPathSegment("json").addQueryParameter("origins", startArray[0] + "," + startArray[1] + "&destinations=" + endString + "&units=" + Units + "&key=AIzaSyANNxd0f6zOf_VvWwI-B3bY0rOblhUW410").build();
        String afterDecode = URLDecoder.decode(mySearchUrl.toString(), "UTF-8");

        Log.d(TAG, "Json: " + mySearchUrl.toString());

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder().url(afterDecode).method("GET", null).build();
        client.newCall(request).enqueue(new Callback() {
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
                            //Pull our duration and distance from the json object
                            JSONArray rows = jsonObject.getJSONArray("rows");
                            JSONObject elements = rows.getJSONObject(0);
                            JSONArray elementsArray = elements.getJSONArray("elements");
                            JSONObject distance = elementsArray.getJSONObject(0);
                            JSONObject duration = elementsArray.getJSONObject(0);
                            JSONObject distanceObject = distance.getJSONObject("distance");
                            JSONObject durationObject = duration.getJSONObject("duration");
                            distanceStringHome = distanceObject.getString("text");
                            durationStringHome = durationObject.getString("text");
                            Log.d(TAG, "Distance Home: " + distanceStringHome + " Duration Home: " + durationStringHome);
                            //Update UI using method

                            getActivity().runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //Update UI
                                    //Set distanceString and durationString to textview
                                    TextView distance = bottomSheetView.findViewById(R.id.Lat);
                                    TextView duration = bottomSheetView.findViewById(R.id.Long);

                                    //format distance string and change to double
                                    String distanceStringHomeFormatted = distanceStringHome.replace(" km", "");
                                    double distanceDoubleHome = Double.parseDouble(distanceStringHomeFormatted);
                                    //format duration string and change to int
                                    String durationStringHomeFormatted = durationStringHome.replace(" mins", "");
                                    int durationIntHome = Integer.parseInt(durationStringHomeFormatted);


                                    //Sleep
                                    try
                                    {
                                        Thread.sleep(250);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    double total = distanceHome + distanceDoubleHome;
                                    //Convert total to string
                                    String totalString = Double.toString(total) + " km";
                                    //Convert duration to string

                                    //Convert durationHome to int
                                    int durationInt = (int) durationHome;

                                     int totalTime = durationInt + durationIntHome;
                                    String totalTimeString = (totalTime) + " mins";

                                    Log.d(TAG, "Duration home: " + durationInt + " Duration Customer: " + durationIntHome);
                                    Log.d(TAG, "Total Time: " + totalTimeString);



                                    Log.d(TAG, "run: " + totalString);



                                    distance.setText(totalString);
                                    duration.setText(totalTimeString);
                                }
                            });


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



}