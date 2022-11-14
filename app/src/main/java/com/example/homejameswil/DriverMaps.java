package com.example.homejameswil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import com.twilio.Twilio;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ctc.wstx.dtd.DTDNmTokenAttr;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.homejameswil.databinding.ActivityDriverMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.checkerframework.common.subtyping.qual.Bottom;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import com.twilio.Twilio;
import com.twilio.converter.Promoter;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.net.URI;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class DriverMaps extends FragmentActivity implements OnMapReadyCallback, RoutingListener
{
    private GoogleMap map;
    private ActivityDriverMapsBinding binding;
    private static String TAG = "DriverMaps";
    private List<Polyline> polylines = null;
    protected LatLng start = null;
    protected LatLng CustomerHome = null;
    protected LatLng end = null;
    private boolean PickedUp = false;




    private String Units = "metric";

    Double driverLat, driverLng;
    protected LatLng home = null;
    Location myLocation = null;


    ArrayList<String> clientDate;
    ArrayList<Double> clientLatitude;
    ArrayList<Double> clientLongitude;
    ArrayList<String> clientTime;
    ArrayList<String> clientUID;
    ArrayList<String> clientStatus;
    ArrayList<String> clientDocID;


    private String distanceStringHome;
    private String durationStringHome;

    public static final String ACCOUNT_SID = "ACa6b4b584706f176b13e7cb879b3281e4";
    public static final String AUTH_TOKEN = "51d57850572b7fc944331ad74295add2";


    String cDate;
    Double cLatitude;
    Double cLongitude;
    String cTime;
    String cUID;
    String cStatus;


    BottomSheetDialog bottomSheetDialog;
    View bottomSheetView;
    DatabaseReference dbRef;
    FirebaseUser user;
    String uid;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        googleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
        map = googleMap;
        getMyLocation();
        getDriverOrderLocation();
        Log.d(TAG, "onMapReady: " + start + "End: "+ end);
        getCustomerHome();




    }

    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End)
    {
        if (Start == null || End == null)
        {
            Toast.makeText(DriverMaps.this, "Unable to get location", Toast.LENGTH_LONG).show();
        }
        else
        {
            Routing routing = new Routing.Builder().travelMode(AbstractRouting.TravelMode.DRIVING).withListener(this).alternativeRoutes(true).waypoints(Start, End).key("AIzaSyANNxd0f6zOf_VvWwI-B3bY0rOblhUW410").build();
            routing.execute();

        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e)
    {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void onRoutingStart()
    {
        Log.d(TAG, "Routing Started");
        map.clear();
    }

    //If Route finding success..

    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex)
    {
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if (polylines != null)
        {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng = null;
        LatLng polylineEndLatLng = null;

        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i < route.size(); i++)
        {
            if (i == shortestRouteIndex)
            {
                polyOptions.color(getResources().getColor(R.color.homeJames));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = map.addPolyline(polyOptions);
                polylineStartLatLng = polyline.getPoints().get(0);
                int k = polyline.getPoints().size();
                polylineEndLatLng = polyline.getPoints().get(k - 1);
                polylines.add(polyline);
            }
            else
            {

            }
        }
        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        map.addMarker(endMarker);
        //Animate to destination
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(start)      // Sets the center of the map to Mountain View
                .zoom(16)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    @Override
    public void onRoutingCancelled()
    {
        Findroutes(start, end);
    }

    boolean myLocationFound = false;


    //Suppress warning
    @SuppressWarnings("MissingPermission")
    //to get user location
    private void getMyLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        //Get my current location

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener()
        {
            @Override
            public void onMyLocationChange(Location location)
            {
                myLocation = location;
                //Convert location to LatLong
                start = new LatLng(location.getLatitude(), location.getLongitude());

                driverLat = location.getLatitude();
                driverLng = location.getLongitude();

                //region Doesn't work
                /*FirebaseFirestore db = FirebaseFirestore.getInstance();

                clientDate = new ArrayList();
                clientLatitude = new ArrayList();
                clientLongitude = new ArrayList();
                clientTime = new ArrayList();
                clientUID = new ArrayList();
                clientStatus = new ArrayList();



                //Read from database specifying with collection
                db.collection("Orders")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task)
                            {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult())
                                    {
                                        //if()
                                        {
                                            clientDocID.add(document.getId());
                                            clientDate.add(document.getString("Date"));
                                            clientLatitude.add(document.getDouble("Latitude"));
                                            clientLongitude.add(document.getDouble("Longitude"));
                                            clientTime.add(document.getString("Time"));
                                            clientUID.add(document.getString("UID"));
                                            clientStatus.add(document.getString("Status"));
                                        }
                                    }
                                }
                                else
                                {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });


                user = FirebaseAuth.getInstance().getCurrentUser();
                uid = user.getUid();

                //Creating user object
                //Setting text boxes to user object defining node names
                Map<String, Object> user = new HashMap<>();


                user.put("Date", clientDate.get(0));
                user.put("Latitude", clientLatitude.get(0));
                user.put("Longitude", clientLongitude.get(0));
                user.put("Time", clientTime.get(0) );
                user.put("UID", clientUID.get(0));
                user.put("DriverUID", uid);
                user.put("Status", "PickedUp");




                //Writing to Firestore specifying collection path with custom set Document reference
                db.collection("Orders").document(clientDocID.get(0))
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
                        });*/
                //endregion

                /*while (!PickedUp)
                {
                    driverRealTimeLocation();
                    //OnDataChange is called on Status in database
                    dbRef.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            for(DataSnapshot ds : snapshot.getChildren())
                            {

                                if(ds.child("uid").equals(uid) || ds.child("status").equals("PickedUp"))
                                {
                                    DriverLocation dl = ds.getValue(DriverLocation.class);

                                    PickedUp = true;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error)
                        {

                        }
                    });
                }*/
                Log.d(TAG, "MyLocation: TEST2.0" + start);

                LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ltlng, 16f);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(start)      // Sets the center of the map to Mountain View
                        .zoom(17)                   // Sets the zoom
                        .bearing(location.getBearing())                // Sets the orientation of the camera to east
                        .tilt(30)
                        // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                if (myLocationFound == false)
                {
                    myLocationFound = true;
                }
            }
        });
    }

    private void getDriverOrderLocation()
    {
        //Connection to database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        clientDocID = new ArrayList();
        clientLatitude = new ArrayList();
        clientLongitude = new ArrayList();
        clientUID = new ArrayList();
        //sleep

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
                        Log.d(TAG, "DRIVERS ID"+ user.getUid());
                        if(document.getString("DriverUID").matches(user.getUid()) && document.getString("Status").matches("Active"))
                        {
                            clientDocID.add(document.getId());
                            clientLatitude.add(document.getDouble("Latitude"));
                            clientLongitude.add(document.getDouble("Longitude"));
                            clientUID.add(document.getString("UID"));


                            Log.d(TAG, "Client ordered lat test: " + clientLatitude + "Client Long: " + clientLongitude);

                            //Convert location to LatLong
                            end = new LatLng(clientLatitude.get(0), clientLongitude.get(0));

                            Findroutes(start, end);

                            map.addCircle(new CircleOptions()
                                    .center(end)
                                    .radius(500)
                                    .strokeColor(Color.RED)
                                    .fillColor(Color.RED));

                        }

                    }
                }
                else
                {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }


                Thread thread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            //sendSms();
                            InsideCircle();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        });
    }


    private void driverRealTimeLocation()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference().child("Location");

        DriverLocation dl = new DriverLocation(user.getUid(), driverLat, driverLng, "Active");
        dbRef.child(dl.getUID()+"").setValue(dl);
    }

    private void InsideCircle()
    {
        float distanceInMeters = 0;
        while(!PickedUp)
        {
            //Run every 5 seconds
            try
            {
                Thread.sleep(5000);
                //Check distance between driver and client
                float[] results = new float[1];
                Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
                Log.d(TAG, "Distance between driver and client: " + start.latitude + " " + start.longitude + " " + end.latitude + " " + end.longitude + " " + results[0]);
                distanceInMeters = results[0];
                Log.d(TAG, "Distance between driver and client: " + distanceInMeters);

                if (distanceInMeters <= 500)
                {
                    Log.d(TAG, "Distance Driver HAS ARRIVED" + distanceInMeters);

                    PickedUp = true;
                    if(PickedUp)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                bottomSheetDialog = new BottomSheetDialog(DriverMaps.this, R.style.BottomSheetDialogTheme);
                                bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_sheet_driver_home, findViewById(R.id.bottomSheetTripHome));
                                try
                                {
                                    JsonFileHome(start, CustomerHome);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }

                                Button button = bottomSheetView.findViewById(R.id.btnTripHome);
                                button.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        Findroutes(start, CustomerHome);
                                    }
                                });
                                bottomSheetDialog.setContentView(bottomSheetView);
                                bottomSheetDialog.show();
                            }
                        });
                    }
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void getCustomerHome()
    {
        //Connection to database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get logged in user UID
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        //Read from database specifying with collection
        db.collection("UserHome").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        Log.d(TAG, "DRIVERS ID"+ user.getUid());
                        if(document.getId().matches(clientUID.get(0)))
                        {
                            CustomerHome = new LatLng(document.getDouble("HomeLatitude"), document.getDouble("HomeLongitude"));
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



    private void sendSms()
    {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.twilio.com/2010-04-01/Accounts/"+ACCOUNT_SID+"/SMS/Messages";
        String base64EncodedCredentials = "Basic " + Base64.encodeToString((ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(), Base64.NO_WRAP);

        RequestBody body = new FormBody.Builder()
                .add("From", "+17123723289")
                .add("To", "+27765663773")
                .add("Body", "Your driver is near POES")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", base64EncodedCredentials)
                .build();
        try
        {
            Response response = client.newCall(request).execute();
            Log.d(TAG, "sendSms: "+ response.body().string());
        }
        catch (IOException e) { e.printStackTrace(); }

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
                        try {
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
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //Update UI
                                    //Set distanceString and durationString to textview
                                    TextView distance = bottomSheetView.findViewById(R.id.DistanceOfHome);
                                    TextView duration = bottomSheetView.findViewById(R.id.TimeOfArrivalHome);
                                    distance.setText(distanceStringHome);
                                    duration.setText(durationStringHome);
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