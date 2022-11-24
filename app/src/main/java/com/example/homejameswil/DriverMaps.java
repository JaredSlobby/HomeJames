package com.example.homejameswil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;

import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.SphericalUtil;
import com.twilio.Twilio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.twilio.Twilio;
import com.twilio.converter.Promoter;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.net.URI;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Scanner;
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


public class DriverMaps extends FragmentActivity implements OnMapReadyCallback, RoutingListener {
    private GoogleMap map;
    private ActivityDriverMapsBinding binding;
    private static String TAG = "DriverMaps";
    private List<Polyline> polylines = null;
    protected LatLng start = null;
    protected LatLng CustomerHome = null;
    protected LatLng end = null;
    private boolean radius = false;
    String originAddress;


    private String Units = "metric";

    Double driverLat, driverLng;
    protected LatLng home = null;
    Location myLocation = null;


    ArrayList<String> clientDate;
    ArrayList<Double> clientLatitude;
    ArrayList<Double> clientLongitude;
    ArrayList<Double> clientHomeLatitude;
    ArrayList<Double> clientHomeLongitude;
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
    DatabaseReference dbLocation;
    private FirebaseDatabase rootNode;
    FirebaseUser user;
    String uid;
    Boolean ToCustomer = false;
    Boolean ToHome = false;
    Boolean bottomSheet = false;
    LatLng endLocation;
    Marker marker;
    LatLng endLocation2;
    Bitmap bitmapIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Testing to see if David can see
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        bitmapIcon = BitmapFactory.decodeResource(getResources(), R.drawable.block);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");


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
        Log.d(TAG, "onMapReady: " + start + "End: " + end);

        Drawable circleDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_up_arrow_circle);
        //BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable, 150, 150);

        //double bearing = bearingBetweenLocations()

        //hide current location icon
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);
    }

    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End)
    {
        if (Start == null || End == null) {
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
                .tilt(30)// Sets the tilt of the camera to 30 degrees
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
        //Get location every second

        endLocation2 = new LatLng(-33.9717, 25.6240);


        bitmapIcon = Bitmap.createScaledBitmap(bitmapIcon, 1, 1, false);

        //Get my current location

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {

                Location location = map.getMyLocation();
                if (location != null)
                {
                myLocation = location;
                //Convert location to LatLong
                start = new LatLng(location.getLatitude(), location.getLongitude());

                marker = map.addMarker(new MarkerOptions().position(start).title("My Location").icon(BitmapDescriptorFactory.fromBitmap(bitmapIcon)).flat(true));


                driverLat = location.getLatitude();
                driverLng = location.getLongitude();

                endLocation = new LatLng(start.latitude, start.longitude);

                animateMarkerNew(endLocation2, start, marker);
                getBearing(start, endLocation);



                if (!myLocationFound)
                {
                    myLocationFound = true;
                    //Find routes
                    Findroutes(start, end);
                }



                driverRealTimeLocation();
                //InsideCircle(start, end);
                //region Doesn't work

                Log.d(TAG, "MyLocation: TEST2.0" + start);

                LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());

                /*CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(start)
                        // Sets the center of the map to Mountain View
                        .zoom(20)                   // Sets the zoom
                        .bearing(location.getBearing())                // Sets the orientation of the camera to east
                        .tilt(55)
                        // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/

                SphericalUtil.computeOffsetOrigin(ltlng, 100, 90);


                    handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            marker.remove();
                        }
                    }, 1000);
            }
                handler.postDelayed(this, 1000);
                endLocation2 = endLocation;
        }
        }, 1000);

    }

    private void getDriverOrderLocation()
    {
        orderUpdating();
    }


    private void driverRealTimeLocation()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        dbLocation = FirebaseDatabase.getInstance().getReference();
        rootNode = FirebaseDatabase.getInstance();
        dbLocation = rootNode.getReference("Location");

        DriverLocation dl = new DriverLocation(user.getUid(), driverLat, driverLng, "Active");
        Log.d(TAG, "Testing Realtime" + user.getUid() + driverLat + driverLng+ "Active");
        dbLocation.child(dl.getUID()+"").setValue(dl);
        Log.d(TAG, "Testing Realtime" + dl);
    }



    private void ToCustomer()
    {
        if (!ToCustomer)
        {
            Thread thread = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Log.d(TAG, "Is this running? Customer");
                        JsonFileHome(start, end);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }

    private void ToHome()
    {
        if (!ToHome)
        {
            Thread thread2 = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Log.d(TAG, "Is this running? Home");
                        JsonFileHome(start, CustomerHome);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            thread2.start();
        }
    }



    private void InsideCircle(LatLng ending)
    {
        float distanceInMeters = 0;
        while(!radius)
        {
            //Run every 10 seconds
            try
            {
                Log.d(TAG, "InsideCircle: " + radius);
                Thread.sleep(10000);
                //Check distance between driver and client
                float[] results = new float[1];
                Location.distanceBetween(start.latitude, start.longitude, ending.latitude, ending.longitude, results);
                Log.d(TAG, "Distance between driver and client: " + start.latitude + " " + start.longitude + " " + ending.latitude + " " + ending.longitude + " " + results[0]);
                distanceInMeters = results[0];

                Log.d(TAG, "To Home Value" + ToHome);
                ToCustomer();
                ToHome();

                Log.d(TAG, "Distance between driver and client: " + distanceInMeters);

                //Findroutes(starting, ending);

                if (distanceInMeters <= 200)
                {
                    Log.d(TAG, "Distance Driver HAS ARRIVED" + distanceInMeters);
                    radius = true;
                    Log.d(TAG, "Distance Driver HAS ARRIVED: " + radius);
                    Log.d(TAG, "Ending value: " + ending);
                    if(radius)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (ending == end)
                                {
                                    bottomSheetDialog = new BottomSheetDialog(DriverMaps.this, R.style.BottomSheetDialogTheme);
                                    bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_sheet_driver_home, findViewById(R.id.bottomSheetTripHome));
                                    try
                                    {
                                        ToHome = true;
                                        ToCustomer = false;

                                        //Sleep for 10 seconds
                                        Thread.sleep(300);
                                        Log.d(TAG, "InsideCircle: " + radius);
                                        JsonFileHome(start, CustomerHome);
                                        bottomSheet = true;
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    Button button = bottomSheetView.findViewById(R.id.btnTripHome);
                                    button.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            Findroutes(start, CustomerHome);
                                            bottomSheetDialog.dismiss();
                                            updateOrder("PickedUp");
                                            radius = false;
                                            bottomSheet = false;
                                        }
                                    });

                                }
                                else if(ending == CustomerHome)
                                {
                                    Log.d(TAG, "It is making it to else if statement?");
                                    bottomSheetDialog = new BottomSheetDialog(DriverMaps.this, R.style.BottomSheetDialogTheme);
                                    bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_sheet_driver_dropped_off, findViewById(R.id.bottomSheetTripDroppedOff));

                                    bottomSheet = false;
                                    ToHome = false;
                                    ToCustomer = true;
                                    try
                                    {
                                        JsonFileHome(start, CustomerHome);
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    Button button = bottomSheetView.findViewById(R.id.btnTripDroppedOf);
                                    button.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            bottomSheetDialog.dismiss();
                                            updateOrder("Completed");
                                            map.clear();
                                        }
                                    });
                                }
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


    public void JsonFileHome(LatLng start, LatLng end) throws IOException
    {
        Log.d(TAG, "JsonFileHome: Start" + start.toString());
        Log.d(TAG, "JsonFileHome: End" + end.toString());

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
                        try {
                            Response response = client.newCall(request).execute();
                            JSONObject jsonObject = new JSONObject(response.body().string());// parse response into json object

                            JSONArray destinationAddresses = jsonObject.getJSONArray("origin_addresses");

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

                            originAddress = destinationAddresses.toString();
                            originAddress = originAddress.replace("[", "");
                            originAddress = originAddress.replace("]", "");
                            originAddress = originAddress.replace("\"", "");


                            //format distance string to double
                            String[] distanceArray = distanceStringHome.split(" ");
                            double distanceDouble = Double.parseDouble(distanceArray[0]);



                            float[] results = new float[1];
                            Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
                            Log.d(TAG, "Distance between driver and client: " + start.latitude + " " + start.longitude + " " + end.latitude + " " + end.longitude + " " + results[0]);
                            final float distanceInMeters = results[0];



                            //Update UI using method
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //Update UI
                                    //Set distanceString and durationString to textview


                                    if (bottomSheet)
                                    {
                                    TextView distance = bottomSheetView.findViewById(R.id.DistanceOfHome);
                                    TextView duration = bottomSheetView.findViewById(R.id.TimeOfArrivalHome);
                                    distance.setText(distanceStringHome);
                                    duration.setText(durationStringHome);
                                    }
                                    if(ToHome == false)
                                    {
                                        TextView address = findViewById(R.id.distance_map);
                                        address.setText("Distance" +"\n" + distanceStringHome);
                                        TextView time = findViewById(R.id.time_map);
                                        time.setText("Distance" + "\n" + durationStringHome);
                                        if (distanceInMeters < 100 && distanceInMeters > 0)
                                        {
                                            bottomSheetDialog = new BottomSheetDialog(DriverMaps.this, R.style.BottomSheetDialogTheme);
                                            bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_sheet_driver_dropped_off, findViewById(R.id.bottomSheetTripDroppedOff));
                                            Button button = bottomSheetView.findViewById(R.id.btnTripDroppedOf);
                                            button.setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View view)
                                                {
                                                    bottomSheetDialog.dismiss();
                                                    updateOrder("Completed");
                                                    map.clear();
                                                }
                                            });
                                            bottomSheetDialog.setContentView(bottomSheetView);
                                            bottomSheetDialog.show();
                                        }

                                    }
                                    if (ToCustomer == false)
                                    {
                                        TextView address = findViewById(R.id.distance_map);
                                        address.setText("Distance" + "\n" + distanceStringHome);
                                        TextView time = findViewById(R.id.time_map);
                                        time.setText("Duration" + "\n" + durationStringHome);
                                    }
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
    //Cursor Parking for David
    //[                         ]
    //[                         ]
    //[                         ]
    //[                         ]
    //[                         ]

    private void sendNotification() throws IOException
    {
        try
        {
            String jsonResponse;

            URL url = new URL("https://onesignal.com/api/v1/notifications");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Authorization", "Basic ZWVkOTViOTctYTlmOC00MTQzLTk0MGYtMmI0YzlkNGQxNmE2");
            con.setRequestMethod("POST");

            String strJsonBody = "{"
                    +   "\"app_id\": \"556bf015-31aa-42d9-a448-4642ce2fb4b7\","
                    +   "\"include_external_user_ids\": " + "[\"" + clientUID.get(0) + "\"],"
                    +   "\"channel_for_external_user_ids\": \"push\","
                    +   "\"data\": {\"foo\": \"bar\"},"
                    +   "\"contents\": {\"en\": \"You have a driver!\"}"
                    + "}";

            Log.d("TAG","strJsonBody:\n" + strJsonBody);

            byte[] sendBytes = strJsonBody.getBytes("UTF-8");
            con.setFixedLengthStreamingMode(sendBytes.length);

            OutputStream outputStream = con.getOutputStream();
            outputStream.write(sendBytes);
            Log.d("TAG","outputStream:\n" + outputStream);
            Log.d("TAG", "sendNotification: " + sendBytes);

            int httpResponse = con.getResponseCode();
            Log.d("TAG","httpResponse: " + httpResponse);

            //See error log from httpResponse
            if (  httpResponse >= HttpURLConnection.HTTP_OK
                    && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST)
            {
                Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
            }
            else
            {
                Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
            }
            Log.d("TAG","jsonResponse:\n" + jsonResponse);

        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }

    private void orderUpdating()
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
        clientHomeLatitude = new ArrayList();
        clientHomeLongitude = new ArrayList();

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
                        if(document.getString("DriverUID").matches(user.getUid()) && (document.getString("Status").matches("Active") || document.getString("Status").matches("PickedUp")))
                        {
                            clientDocID.add(document.getId());
                            clientLatitude.add(document.getDouble("Latitude"));
                            clientLongitude.add(document.getDouble("Longitude"));
                            clientUID.add(document.getString("UID"));
                            clientHomeLatitude.add(document.getDouble("HomeLatitude"));
                            clientHomeLongitude.add(document.getDouble("HomeLongitude"));
                            String Status = document.getString("Status");

                            Log.d(TAG, "Client ordered lat test: " + clientLatitude + "Client Long: " + clientLongitude);
                            if(Status.matches("Active"))
                            {
                                end = new LatLng(clientLatitude.get(0), clientLongitude.get(0));
                                CustomerHome = new LatLng(clientHomeLatitude.get(0), clientHomeLongitude.get(0));
                                ToHome = true;
                                ToCustomer = false;
                                Findroutes(start, end);

                            }

                            else if(Status.matches("PickedUp"))
                            {
                                CustomerHome = new LatLng(clientHomeLatitude.get(0), clientHomeLongitude.get(0));

                                try
                                {
                                    Thread thread2 = new Thread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                Log.d("TAG", "Are you running right now?");
                                                Log.d("TAG", "Is it running?");
                                                CustomerHome = new LatLng(clientHomeLatitude.get(0), clientHomeLongitude.get(0));
                                                ToHome = false;
                                                ToCustomer = true;
                                                Log.d("TAG", start + " Findroutes" + CustomerHome);
                                                InsideCircle(CustomerHome);
                                                Log.d("TAG", start + " Inside Circle" + CustomerHome);

                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    thread2.start();
                                    Findroutes(start, CustomerHome);



                                } catch
                                (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            if(document.getString("SMS").matches("Yes"))
                            {
                                try
                                {
                                    Thread thread1 = new Thread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                InsideCircle(end);
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    thread1.start();

                                }
                                catch
                                (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            if(document.getString("SMS").matches("No"))
                            {
                                try
                                {
                                    Thread thread = new Thread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                sendNotification();
                                                InsideCircle(end);
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    thread.start();
                                    document.getReference().update("SMS", "Yes");

                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
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

    private void updateOrder(String Update)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                        if(document.getString("DriverUID").matches(user.getUid()) && (document.getString("Status").matches("Active") || document.getString("Status").matches("PickedUp")))
                        {
                            document.getReference().update("Status", Update);

                            if(Update.matches("PickedUp"))
                            {
                                //Get current time
                                ToHome = false;
                                ToCustomer = true;
                                //InsideCircle(CustomerHome);

                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm");
                                String strDate = mdformat.format(calendar.getTime());
                                document.getReference().update("TimeOfPickUp", strDate);
                                document.getReference().update("PickUpLocation", originAddress);

                                try {
                                    Thread thread = new Thread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                sendNotification();
                                                InsideCircle(end);
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    thread.start();
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }


                            }
                            if (Update.matches("Completed"))
                            {
                                //Get current time
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm");
                                String strDate = mdformat.format(calendar.getTime());
                                ToHome = true;
                                document.getReference().update("TimeDroppedOff", strDate);
                                Intent intent = new Intent(DriverMaps.this, DriverMainActivity.class);
                                startActivity(intent);
                            }
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

    private void animateMarkerNew(final LatLng startPosition, final LatLng destination, final Marker marker) {
        if (marker != null)
        {

            final LatLng endPosition = new LatLng(destination.latitude, destination.longitude);
            final float startRotation = marker.getRotation();
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1005); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation)
                {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
                        Log.d("newPosition", newPosition.toString());
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                .target(newPosition)
                                .zoom(18f)
                                .tilt(35)
                                .bearing(getBearing(startPosition, endPosition))
                                .build()));
                        marker.setRotation(getBearing(startPosition, new LatLng(destination.latitude, destination.longitude)));
                    }
                    catch (Exception ex)
                    {
                        //I don't care atm.. I am tired
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    super.onAnimationEnd(animation);
                    // if (mMarker != null) {
                    // mMarker.remove();
                    // }
                    // mMarker = googleMap.addMarker(new MarkerOptions().position(endPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                }
            });
            valueAnimator.start();
        }
    }


    private float getBearing(LatLng begin, LatLng end)
    {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }
}