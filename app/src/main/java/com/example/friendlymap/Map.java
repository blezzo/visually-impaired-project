package com.example.friendlymap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Map extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, TextToSpeech.OnInitListener,
        GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener, View.OnClickListener /*,GoogleMap.OnGroundOverlayClickListener*/   {

    //DirectionsAPI
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";

    private static final long FASTEST_INTERVAL = 10_000;
    private static final long INTERVAL = 15_000;
    private static final long DIRECTION_TURN_INTERVAL = 5_000;

    //ACTUAL intervals
    //private static final long FASTEST_INTERVAL = 10_000;
    //private static final long INTERVAL = 15_000;
    private Timer timer;
    private TimerTask distTask;
    private static final int RC_LOC_AUDIO = 9;
    private static final float ZOOM_DEFAULT_LEVEL_ = 15;
    private static final int RC_LOC_TURN_ON = 5;
    private static final String TAG = "TAG";
    private static final int RC_RECOGNIZER = 8;
    private static final String SPEAK_NOW = "Speak now", BANK = "bank", ATM = "atm", HOSPITAL = "hospital", SCHOOL = "school", BUS_STOP = "bus%20stop";
    private static final CharSequence MY_COLLEGE_NAME = staticCollegeData.getName();

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocServices;
    private GoogleApiClient gApiClient;
    private LocationRequest locationRequest;
    private boolean isConnected;
    private Marker markerCurrent;
    private Location currentLocationObj;
    private TextToSpeech tts;
    private boolean isTTSready;
    private String command;
    private boolean flagCurrentLocSet;
    private String recognizedSpeech;
    private int tts_id = 0;
    private static String utteranceId;
    private boolean includesMyColl;
    private double destLat, destLng;
    private String urlFullLink;
    private boolean noInputNeeded;
    private boolean recognized, fabLocOn;
    private List<LatLng> latlongList;
    private FloatingActionButton fab;
    private ImageView currentLocZoom;
    private Location nextLocStepObj;
    private ArrayList<directionResultFuta.Steps> steps;
    private boolean recognize1Or2;
    private directionResultFuta resultPojo;
    private ProgressListener progressListener;
    private GroundOverlayOptions overlayBlueprint;
    private ArrayList<String> listWhere;
    private ArrayList<String> listHere;
    private int crossed = 0;
    private int spoken;
    private boolean cmdModeOn;
    private CoordinatorLayout coordinatorLay;
    private Switch switchNearby;
    private ProgressDialog pDialog;
    private LatLng latLngNearby;
    private ArrayList<Marker> markerList;
    private LocationCallback mLocCallback;
    private boolean mockMode;
    private boolean zoomOnce;
    private boolean restartNavigation;
    private Dialog dialog;
    private ArrayList<NearbyMarker> nearbyMarkerList;
    private String COLL_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=disabled%20school" +
            "&location=7.2972,5.1461&type=school&radius=2000&key=AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U";
    private static final String BASEURL_SEARCH = "https://maps.googleapis.com/maps/api/place/textsearch/json?";



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fab = findViewById(R.id.on_off);
        currentLocZoom = findViewById(R.id.current_zoom_fab);
        currentLocZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animate currentloc zoom
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocationObj.getLatitude(), currentLocationObj.getLongitude()),
                        mMap.getMaxZoomLevel()));

            }
        });
        currentLocZoom.setVisibility(View.GONE);
        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabLocOn) {
                    mockMode = true;
                    fab.setImageResource(R.drawable.fab_location_off);
                    fusedLocServices.removeLocationUpdates(mLocCallback);
                    Snackbar.make(coordinatorLay, "Mocking location is on", Snackbar.LENGTH_LONG).show();
                } else {
                    permission();
                    turnFabLocOn();
                    mockMode = false;    //currentLocOn
                    zoomOnce = true;
                }
                fabLocOn = !fabLocOn;

            }
        });
        mLocCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                List<Location> location = locationResult.getLocations();
                if (location.size() > 0) {
                    Location loc = location.get(location.size() - 1);
                    if (isConnected) {
                        if (!mockMode) {
                            setMap(new LatLng(loc.getLatitude(), loc.getLongitude()));
                            Log.d(TAG, "onLocationResult() called with: locationResult = [" + loc + "]");
                            if (restartNavigation) {

                                currentLocSpeak();
                                restartNavigation = false;
                            }
                        }
                    }
                }
            }
        };

        coordinatorLay = findViewById(R.id.coordinatorLay);
        initData();
        apiClient();
        fabLocOn = true;
        zoomOnce = true;
        //showDialogAndSpeak();

    }

    private void apiClient() {
        fusedLocServices = LocationServices.getFusedLocationProviderClient(this);
        gApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        gApiClient.connect();
        if (!gApiClient.isConnecting() || !gApiClient.isConnected())
            Log.i(TAG, "apiClient: connected");
        locationRequest = new LocationRequest();
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
         /*if (!gApiClient.isConnecting() || !gApiClient.isConnected()) {
            gApiClient.connect();
            Log.i(TAG, "onResume: connected");
        }*/
        permission();
    }

    private void initData() {
        currentLocationObj = new Location("");
        tts = new TextToSpeech(this, this);
        progressListener = new ProgressListener();
        tts.setOnUtteranceProgressListener(progressListener);

    }


    private void turnFabLocOn() {
        fab.setImageResource(R.drawable.fab_location_on);
        //fabLocOn=true;
        if (ActivityCompat.checkSelfPermission(Map.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Map.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocServices.requestLocationUpdates(locationRequest, mLocCallback, null);
        Snackbar.make(coordinatorLay, "Current location is on", Snackbar.LENGTH_LONG).show();
    }

    private void permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPer();
            } else {
                locTurnOnReq();
            }
        } else locTurnOnReq();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    private void currentLocSpeak() {
        if (currentLocationObj != null && isTTSready) {
            if (Geocoder.isPresent()) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addressList = null;
                StringBuilder currentAddress = new StringBuilder();
                try {
                    addressList = geocoder.getFromLocation(currentLocationObj.getLatitude(), currentLocationObj.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, "getFromLocation: exception " + e.getMessage());
                    Toast.makeText(this, "getFromLocation " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                if (addressList != null && addressList.size() > 0) {
                    Address address = addressList.get(0);
                    if (address.getAddressLine(0) != null) {
                        currentAddress.append(address.getAddressLine(0));
                    } else {
                        if (address.getFeatureName() != null)
                            currentAddress.append(address.getFeatureName());
                        else if (address.getSubLocality() != null)
                            currentAddress.append(address.getSubLocality());
                        else if (address.getLocality() != null)
                            currentAddress.append(address.getLocality());
                    }
                    String formattedAdd = String.valueOf(currentAddress);
                    formattedAdd = formattedAdd.replaceAll("-", " ");
                    command = "You are currently at " + formattedAdd + ".\nwhere would you like to go?.   " +    //testing
                            " Kindly speak your destination complete address now!";
                    recognize1Or2 = false;
                    Log.i(TAG, "currentLocSpeak: " + command);
                    speakOut(command, false);
                }
            } else {
                Toast.makeText(this, "GeoCoder not present", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "guideHimToInputDestination: geocoder not present");
            }
        }
    }

    private void recognizeSpeech(String utteranceId) {
        //STT
        if (Map.utteranceId.equals(utteranceId)) {
            Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            if (includesMyColl)
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            else
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, SPEAK_NOW);
            try {
                startActivityForResult(recognizerIntent, RC_RECOGNIZER);
            } catch (ActivityNotFoundException ex) {
                String cmd = "Sorry, your device does not support speech recognition";
                Toast.makeText(this, cmd, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void speakOut(String s, boolean noInputNeeded) {
        tts_id++;
        this.noInputNeeded = noInputNeeded;
        HashMap<String, String> mapTTSid = new HashMap<>();
        mapTTSid.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(tts_id));
        Log.i(TAG, "speakOut: isSpeaking " + tts.isSpeaking());
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, mapTTSid);
    }

    private void speakCollege(String cmdReplyColl, boolean noInputNeeded) {
        tts_id++;
        this.noInputNeeded = noInputNeeded;
        HashMap<String, String> mapTTSid = new HashMap<>();
        mapTTSid.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(tts_id));
        Log.i(TAG, "speakCollege: isSpeaking? " + tts.isSpeaking());
        //tts.speak(cmdReplyColl, TextToSpeech.QUEUE_ADD, mapTTSid);
        tts.speak(cmdReplyColl, TextToSpeech.QUEUE_FLUSH, mapTTSid);
    }

    private void requestPer() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECORD_AUDIO}, RC_LOC_AUDIO);
        Log.d(TAG, "requestPer() called");
    }

    private void locTurnOnReq() {
        //Log.d(TAG, "locTurnOnReq() called");
        LocationRequest locationReq = LocationRequest.create();
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationReq.setFastestInterval(FASTEST_INTERVAL);
        LocationSettingsRequest.Builder locSettingReq = new LocationSettingsRequest.Builder();
        locSettingReq.addLocationRequest(locationReq);
        locSettingReq.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(gApiClient, locSettingReq.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                int statuscode = status.getStatusCode();
                Log.d(TAG, "onResult: " + statuscode);
                switch (statuscode) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        fusedServices();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(Map.this, RC_LOC_TURN_ON);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            Toast.makeText(Map.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
    }

    private void fusedServices() {
        if (ActivityCompat.checkSelfPermission(Map.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(Map.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocServices.requestLocationUpdates(locationRequest, mLocCallback, null);
        Log.d(TAG, "fusedServices() called fabLocon " + fabLocOn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_LOC_TURN_ON) {
            if (resultCode == RESULT_OK) {
                fusedServices();
            } else if (resultCode == RESULT_CANCELED) {
                locTurnOnReq();
            }
        } else if (requestCode == RC_RECOGNIZER) {
            if (resultCode == RESULT_OK && data != null) {
                recognized = false;
                ArrayList<String> list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (includesMyColl && cmdModeOn) {
                    String cmdReply;
                    if (list.get(0).equalsIgnoreCase("no")) {
                        timer.cancel();
                        finish();
                        return;
                    } else if (list.get(0).equalsIgnoreCase("restart")) {
                        restartNavigation();
                        return;
                    } else if ((cmdReply = matchCmdCollege(list.get(0))) != null) {
                        speakCollege(cmdReply + "\n Speak any other place of the College You would like to know the information about or go there ? " +
                                "\n or Say no to end this navigation. " +
                                ", or Say restart to navigate to any other destination", false);
                    } else {
                        //no match found
                        speakCollege("place not found, try again!", false);
                    }
                } else {
                    //last after timer.cancel - normal destination complete
                    if (list.get(0).equalsIgnoreCase("no")) {
                        timer.cancel();
                        finish();
                        return;
                    } else if (list.get(0).equalsIgnoreCase("restart")) {
                        restartNavigation();
                        return;
                    }
                }
                //test
                //differentiate btwn 1/2 cmd and destination cmd
                for (String eachName :
                        list) {
                    Log.i(TAG, "onActivityResult: " + eachName + " cmdModeON, includesMyColl? " + cmdModeOn + includesMyColl);

                    /*} else {*/
                    if (recognizedSpeech != null && recognize1Or2) {
                        confirmDestination(eachName);
                        if (recognized) {
                            break;
                        }
                    } else
                        //select best whichever hits the result
                        if (getFromLocName(eachName)) {
                            recognizedSpeech = eachName;
                            recognized = true;
                            //confirm 1/2
                            //selectionModeOn = true;
                            speakOut(recognizedSpeech + " destination selected. Say one to start navigation. Say two to change destination", false);
                            recognize1Or2 = true;
                            break;
                        }
                    //}
                }
                if (!recognized) {
                    if (cmdModeOn) {
                        speakOut("Wrong input. Place not found in this college." +
                                " please try again!", false);
                    } else
                        speakOut("Didn't catch that, please try again!", false);
                }
            } else {
                Log.i(TAG, "onActivityResult: data null");
            }
        }
    }

    private String matchCmdCollege(String eachCmd) {
        for (int i = 0; i < listWhere.size(); i++) {
            if (eachCmd.contains(listWhere.get(i)) || eachCmd.equalsIgnoreCase(listWhere.get(i))) {
                recognized = true;
                Log.i(TAG, "matchCmdCollege: " + eachCmd + listWhere.get(i));
                return listHere.get(i);
            }
        }
        return null;
    }

    private void confirmDestination(String commands) {
        if (commands.equals("one") || commands.equals("1")) {
            recognized = true;
            if (this.recognizedSpeech != null) {
                takeMeToThisDesti(this.recognizedSpeech);
                Log.i(TAG, "onActivityResult: in confirm desti" + this.recognizedSpeech);
            }
        } else if (commands.equals("two") || commands.equals("2")) {
            speakOut("ok Changing destination. Where would you like to go from current location?", false);
            //reset process
            recognized = true;
            recognize1Or2 = false;
            currentLocSpeak();
        }
    }

    private void restartNavigation() {
        resultPojo = null;
        zoomOnce = true;
        switchNearby.setChecked(false);
        includesMyColl = false;
        cmdModeOn = false;
        if (mMap != null)
            mMap.clear();
        if (timer != null)
            timer.cancel();
        //if off then only turnlocOn
        Log.d(TAG, "restartNavigation() loc was " + fabLocOn);
        if (!fabLocOn) {
            fabLocOn = true;
            fab.setImageResource(R.drawable.fab_location_on);
            mockMode = false;
            if (ActivityCompat.checkSelfPermission(Map.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Map.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Turn on locations", Toast.LENGTH_SHORT).show();
            }
            fusedLocServices.requestLocationUpdates(locationRequest, mLocCallback, null);
        }
        Log.d(TAG, "restartNavigation() loc now " + fabLocOn);
        cmdModeOn = false;
        restartNavigation = true;
    }

    private void takeMeToThisDesti(String recognizedSpeech) {
        includesMyColl = recognizedSpeech.contains(MY_COLLEGE_NAME);
        Log.i(TAG, "takeMeToThisDesti: includesMyColl " + includesMyColl);
        if (includesMyColl) {
            listHere = staticCollegeData.initListHere();
            listWhere = staticCollegeData.initListWhere();
        }
        guideHimToInputDestination(includesMyColl);
    }

    private void guideHimToInputDestination(boolean includesMyColl) {
        //directions api, tts on turns
        if (includesMyColl) {
            destLat = staticCollegeData.getLat();
            destLng = staticCollegeData.getLng();
            Log.d(TAG, "guideHimToInputDestination() called with: includesMyColl = [" + includesMyColl + "]");
        }
        //call directions api and use tts on turns until reaching destination
        directionAPI();
    }

    private boolean getFromLocName(String recognizedSpeech) {
        Geocoder gc = new Geocoder(this);
        List<Address> addressList = null;
        try {
            addressList = gc.getFromLocationName(recognizedSpeech, 1);
        } catch (IOException e) {
            e.printStackTrace();
            speakOut("location not found, please try again", false);
        }
        if (addressList != null && addressList.size() > 0) {
            Address address = addressList.get(0);
            destLat = address.getLatitude();
            destLng = address.getLongitude();
            // Log.i(TAG, "getFromLocName: " + destLat + ", " + destLng);
            return true;
        } else
            return false;
    }

    private void directionAPI() {
        String origin = currentLocationObj.getLatitude() + "," + currentLocationObj.getLongitude(),
                destination = destLat + "," + destLng;
        urlFullLink = DIRECTION_URL_API + "origin=" + origin + "&destination=" + destination + "&key=" + getString(R.string.google_maps_key);
        Log.d(TAG, "directionAPI() called " + urlFullLink);
        new GetDirectionsTask().execute(urlFullLink);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_start_again, menu);
        MenuItem mi = menu.findItem(R.id.mi_nearby);
        mi.setActionView(R.layout.switch_nearby);
        switchNearby = (Switch) mi.getActionView();
        switchNearby.setEnabled(false);
        switchNearby.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showPDialog();
                    showNearbyPlaces();
                } else {
                    removeNearbyPlaces();
                }

            }
        });
        return true;
    }

    private void showPDialog() {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("searching for nearby places...");
        pDialog.show();
    }

    private void removeNearbyPlaces() {
        for (int i = 0; i < markerList.size(); i++) {
            markerList.get(i).remove();
        }
    }

    private void showNearbyPlaces() {
        markerList = new ArrayList<Marker>();
        String FULL_URL_BANK = null,
                FULL_URL_SCHOOL = null,
                FULL_URL_BUS = null,
                FULL_URL_HOSP = null,
                FULL_URL_ATM = null;
        latLngNearby = null;
        if (resultPojo != null) {
            if (includesMyColl) {
                //static loc
                latLngNearby = staticCollegeData.getLatLong();
            } else {
                //destination loc
                latLngNearby = resultPojo.getEnd_loc();
            }
        } else {
            //use current loc
            latLngNearby = new LatLng(currentLocationObj.getLatitude(), currentLocationObj.getLongitude());
        }
        FULL_URL_SCHOOL = UrlBuilder(latLngNearby, SCHOOL);
        FULL_URL_HOSP = UrlBuilder(latLngNearby, HOSPITAL);
        FULL_URL_ATM = UrlBuilder(latLngNearby, ATM);
        FULL_URL_BANK = UrlBuilder(latLngNearby, BANK);
        FULL_URL_BUS = UrlBuilder(latLngNearby, BUS_STOP);
        Log.i(TAG, "showNearbyPlaces: SCHOOL URL " + UrlBuilder(latLngNearby, SCHOOL));
        //atm
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, FULL_URL_ATM, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response);
                        try {
                            if (response.getString("status").equalsIgnoreCase("ok")) {
                                parseNearby(response);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: " + error.networkResponse);


                    }
                });
        RequestQueue queue = Volley.newRequestQueue(Map.this);
        queue.add(jsonObjectRequest);
        //bank
        queue.add(new JsonObjectRequest(Request.Method.GET, FULL_URL_BANK, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "onResponse: " + response);
                try {
                    if (response.getString("status").equalsIgnoreCase("ok")) {
                        parseNearby(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setAllNearbyMarkers(BANK);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.networkResponse);
            }
            }
        ));
        //school
        queue.add(new JsonObjectRequest(Request.Method.GET, FULL_URL_SCHOOL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "onResponse: " + response);
                try {
                    if (response.getString("status").equalsIgnoreCase("ok")) {
                        parseNearby(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setAllNearbyMarkers(SCHOOL);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.networkResponse);

            }
        }

    ));
        //hosp
        queue.add(new JsonObjectRequest(Request.Method.GET, FULL_URL_HOSP, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "onResponse: " + response);
                try {
                    if (response.getString("status").equalsIgnoreCase("ok")) {
                        parseNearby(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setAllNearbyMarkers(HOSPITAL);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.networkResponse);
            }
        }
        ));
//bus
        queue.add(new JsonObjectRequest(Request.Method.GET, FULL_URL_BUS, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "onResponse: " + response);
                try {
                    if (response.getString("status").equalsIgnoreCase("ok")) {
                        parseNearby(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setAllNearbyMarkers(BUS_STOP);
                dialogDismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.networkResponse);
                dialogDismiss();
            }
        }
        ));
    }

    private void setAllNearbyMarkers(String type) {
        for (NearbyMarker nearbymarker :
                nearbyMarkerList) {
            Log.i(TAG, "setAllNearbyMarkers: ");
            MarkerOptions options;
            options = new MarkerOptions();
            options.position(nearbymarker.latLng);
            if (type.equalsIgnoreCase(BANK))
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bank_marker));
            else if (type.equalsIgnoreCase(ATM))
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.atm_marker));
            else if (type.equalsIgnoreCase(SCHOOL))
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.school_marker));
            else if (type.equalsIgnoreCase(HOSPITAL))
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.hos_marker));
            else options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker));
            options.draggable(false);
            options.title(nearbymarker.name);
            Marker marker = mMap.addMarker(options);
            markerList.add(marker);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngNearby, ZOOM_DEFAULT_LEVEL_));
    }

    private void parseNearby(JSONObject response) {
        nearbyMarkerList = new ArrayList<>();
        try {
            JSONArray array = response.getJSONArray("results");
            for (int i = 0; i < 2; i++) {
                JSONObject eachResult = array.getJSONObject(i);
                JSONObject jsonGeometry = eachResult.getJSONObject("geometry");
                JSONObject loc = jsonGeometry.getJSONObject("location");
                nearbyMarkerList.add(new NearbyMarker(new LatLng(loc.getDouble("lat"), loc.getDouble("lng")),
                        eachResult.getString("name")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void dialogDismiss() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    private String UrlBuilder(LatLng latLng, String QUERY) {
        String query = null;
        switch (QUERY) {
            case BANK:
                query = BANK;
                break;
            case ATM:
                query = ATM;
                break;
            case HOSPITAL:
                query = HOSPITAL;
                break;
            case SCHOOL:
                query = SCHOOL;
                return BASEURL_SEARCH + "query=disabled%20school&location=" + latLng.latitude + "," + latLng.longitude + "&type=" + query + "&radius=2000&key=AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U";
            case BUS_STOP:
                query = BUS_STOP;
                break;
        }
        return BASEURL_SEARCH + "query=" + query + "&location=" + latLng.latitude + "," + latLng.longitude + "&radius=2000&key=AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U";
    }


        private void setMap(LatLng latLng) {
        Log.i(TAG, "setMap: isConnected " + isConnected);
        currentLocationObj.setLatitude(latLng.latitude);
        currentLocationObj.setLongitude(latLng.longitude);
        if (!switchNearby.isEnabled()) {
            switchNearby.setEnabled(true);
        }
        if (!flagCurrentLocSet) {
            flagCurrentLocSet = true;
            currentLocSpeak();
        }
        Log.i(TAG, "setMap: " + currentLocationObj);
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        options.icon(BitmapDescriptorFactory.defaultMarker());
        options.draggable(true);
        //options.title("you are here");
        if (markerCurrent != null)
            markerCurrent.remove();
        markerCurrent = mMap.addMarker(options);
        if (!flagCurrentLocSet) markerCurrent.showInfoWindow();
        if (fabLocOn) {
            if (zoomOnce) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getMaxZoomLevel()));
                zoomOnce = false;
            }
        }/* else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_DEFAULT_LEVEL_));*/
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int resultLang = tts.setLanguage(Locale.getDefault());
            Log.i(TAG, "onInit: " + resultLang);
            if (resultLang == TextToSpeech.LANG_MISSING_DATA ||
                    resultLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                // missing data, install it
                Intent install = new Intent();
                install.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            } else {
                isTTSready = true;
            }
        }

    }

    private class GetDirectionsTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            //makecall
            String ans = "";
            InputStream istream = null;
            try {
                URL url = new URL(strings[0]);
                istream = url.openConnection().getInputStream();
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                istream.close();
                ans = builder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ans;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Log.d(TAG, "onPostExecute() called with: s = [" + s + "]");
            parseJson(s);
        }
    }

    private void parseJson(String s) {
        try {
            if (s == null) return;
            String DURA = "duration", TEXT = "text", VALUE = "value", DIST = "distance", END_LOC = "end_location", MANEUV = "maneuver",
                    START_LOC = "start_location", LAT = "lat", LNG = "lng", POINTS = "points", HTML_INSTRUC = "html_instructions", POLY = "polyline";
            PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLUE).width(8);
            polylineOptions.endCap(new SquareCap());
            polylineOptions.startCap(new RoundCap());
            polylineOptions.jointType(JointType.ROUND);
            //check for has(name)
            JSONObject object = new JSONObject(s);
            JSONArray routes = object.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);
            JSONArray legs = route.getJSONArray("legs");
            JSONObject leg = legs.getJSONObject(0);
            JSONObject duration = leg.getJSONObject(DURA);
            resultPojo = new directionResultFuta(duration.getString(TEXT).replace("mins", "minutes")); //duration
            JSONObject distance = leg.getJSONObject(DIST);
            resultPojo.setDistance(distance.getString(TEXT));
            JSONObject endLoc = leg.getJSONObject(END_LOC);
            resultPojo.setEnd_loc(new LatLng(endLoc.getDouble(LAT), endLoc.getDouble(LNG)));
            JSONObject startLoc = leg.getJSONObject(START_LOC);
            resultPojo.setStart_loc(new LatLng(startLoc.getDouble(LAT), startLoc.getDouble(LNG)));
            JSONArray stepJsonObj = leg.getJSONArray("steps");
            steps = new ArrayList<>();
            latlongList = new ArrayList<>();
            for (int i = 0; i < stepJsonObj.length(); i++) {
                //add to stepList, each step array
                //steps-start, endloc, dist, dura, html instruc, maneuver, polyline
                directionResultFuta.Steps tempStep = null;
                tempStep = new directionResultFuta().new Steps();
                JSONObject eachStep = stepJsonObj.getJSONObject(i);
                JSONObject dist = eachStep.getJSONObject(DIST);
                tempStep.setHtml_instruc(Html.fromHtml(eachStep.getString(HTML_INSTRUC)).toString());
                tempStep.setDistance(dist.getString(TEXT));
                // Log.i(TAG, "parseJson: dist " + tempStep.getDistance());
                JSONObject dura = eachStep.getJSONObject(DURA);
                tempStep.setDuration(dura.getString(TEXT));
                JSONObject end = eachStep.getJSONObject(END_LOC);
                //Location tempEnd = new Location(LocationManager.GPS_PROVIDER);
                Location tempEnd = new Location("");
                tempEnd.setLongitude(end.getDouble(LNG));
                tempEnd.setLatitude(end.getDouble(LAT));
                tempStep.setEnd_loc(tempEnd);
                JSONObject start = eachStep.getJSONObject(START_LOC);
                Location temp = new Location("");
                temp.setLongitude(start.getDouble(LNG));
                temp.setLatitude(start.getDouble(LAT));
                tempStep.setStart_loc(temp);
                markEachTurn(temp);
                JSONObject poly = eachStep.getJSONObject(POLY);
                if (eachStep.has(MANEUV)) {
                    tempStep.setManeuver(eachStep.getString(MANEUV));
                } else {
                    tempStep.setManeuver(Html.fromHtml(eachStep.getString(HTML_INSTRUC)).toString());
                }
                //Log.i(TAG, "parseJson: " + tempStep.getHtml_instruc()());
                tempStep.setPolyline(poly.getString(POINTS));
                latlongList.addAll(decodePolyLine(poly.getString(POINTS)));
                steps.add(tempStep);
            }
            //draw
            //po.add all latlong
            polylineOptions.addAll(latlongList);
            JSONObject overview_poly = route.getJSONObject("overview_polyline");
            resultPojo.setOverview_poly(overview_poly.getString(POINTS));
            Log.i(TAG, "parseJson: steps list " + steps.size());
            mMap.addPolyline(polylineOptions);
            //start, end markers
            MarkerOptions moStart = new MarkerOptions();
            moStart.position(resultPojo.getStart_loc());
            moStart.draggable(false);
            moStart.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mMap.addMarker(moStart);
            //end loc
            MarkerOptions moEnd = new MarkerOptions();
            moEnd.position(resultPojo.getEnd_loc());
            moEnd.draggable(false);
            moEnd.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mMap.addMarker(moEnd);
            //show fab
            fab.show();
            currentLocZoom.setVisibility(View.VISIBLE);
            if (includesMyColl) {
                speakOut("Journey duration is " + resultPojo.getDuration() + ". Welcome to the Federal University of Technology, Akure!" +
                        "Now, " + steps.get(0).getHtml_instruc(), true);
            } else
                speakOut("Journey duration is " + resultPojo.getDuration() + ". Starting navigation!" +
                        "Now, " + steps.get(0).getHtml_instruc(), true);
            for (directionResultFuta.Steps step :
                    steps) {
                Log.i(TAG, "maneuver step: " + step.getHtml_instruc() + ", " + step.getDistance());
            }
            Log.i(TAG, "parseJson: steps" + steps.size());
            steps.remove(0);
            timer = new Timer();
            initTimer();
            timer.schedule(distTask, 15_000, DIRECTION_TURN_INTERVAL + 3_000);
            drawBluePrint();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "parseJson: " + e.getMessage());
        }
    }

    private void drawBluePrint() {
        overlayBlueprint = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.blueprint))
                .positionFromBounds(new LatLngBounds(
                        //N>S
                        new LatLng(7.2972, 5.1461), new LatLng(7.2972, 5.1461)))
                .transparency(0.5f)
                .bearing(16);
        //mMap.setOnGroundOverlayClickListener(this);
        mMap.addGroundOverlay(overlayBlueprint);

    }

    private void markEachTurn(Location endloc) {
        mMap.addCircle(new CircleOptions().radius(1).strokeColor(getResources().getColor(R.color.transparent))
                .center(new LatLng(endloc.getLatitude(), endloc.getLongitude()))
                .clickable(true).fillColor(Color.BLACK));
    }

    private void initTimer() {
        distTask = new TimerTask() {
            int dist;
            //int index;
            directionResultFuta.Steps nextStep;

            @Override
            public void run() {
                if (steps.size() == 0) {
                    Location locEnding = new Location("");
                    locEnding.setLatitude(resultPojo.getEnd_loc().latitude);
                    locEnding.setLongitude(resultPojo.getEnd_loc().longitude);
                    dist = (int) currentLocationObj.distanceTo(locEnding);
                    if (includesMyColl) {
                        //speak with queue,
                        Log.i(TAG, "last step includesMycoll" + tts.isSpeaking());
                        if (dist >= 0 && dist <= 10) {
                            timer.cancel();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDialogAndSpeak();

                                }
                            });
            }else if (dist < 50 && dist > 10) {
                            if (crossed == 1) return;
                            speakCollege("You will reach the destination in 50 metres.",
                                    true);
                            cmdModeOn = true;
                            crossed = 1;
                        }
                    } else {
                        Log.i(TAG, "last step destination not coll" + tts.isSpeaking());
                        if (dist >= 0 && dist <= 10) {
                            timer.cancel();
                            /*
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDialogAndSpeak();
                                }
                            });*/
                            cmdModeOn = false;
                            speakCollege("You have reached.   If you want to navigate to some other destination say restart.   " +
                                            " Else Say no to end this navigation.",
                                    false);
                        } else if (dist < 50 && dist > 10) {
                            if (crossed == 1) return;
                            speakCollege("You will reach the destination in 50 metres.",
                                    true);
                            cmdModeOn = false;
                            crossed = 1;
                        }
                    }
                    return;
                }

                nextStep = steps.get(0);
                nextLocStepObj = nextStep.getStart_loc();
                dist = (int) currentLocationObj.distanceTo(nextLocStepObj);
                Log.i(TAG, "run: testing, how many metres away? " + dist + " nextstep: " + nextStep.getHtml_instruc() +
                        " covered size" + steps.size());    //lasthere
                if (dist < 50) {
                    if (crossed == 1) {
                        crossed = 2;
                        spoken = 2;
                        steps.remove(0);
                    } else {
                        //direct cross without covering 200 in mock
                        crossed = 2;
                        spoken = 2;
                        steps.remove(0);
                    }
                    Log.i(TAG, "run: testing, 50 metres away, " + nextStep.getHtml_instruc() + dist);
                    speakOut("In 50 metres, " + nextStep.getHtml_instruc(), true);
                } else if (dist < 200) {
                    if (spoken == 1) {
                        return;
                    }
                    Log.i(TAG, "run: testing, 200 metres away, " + nextStep.getHtml_instruc() + dist);
                    speakOut("In 200 metres, " + nextStep.getHtml_instruc(), true);
                    spoken = 1;
                    crossed = 1;
                }
            }
        };
    }

    private void showDialogAndSpeak() {
        speakCollege("You have reached", true);
        //custom dia
        dialog = new Dialog(Map.this);
        View view = LayoutInflater.from(this).inflate(R.layout.futa_info_layout, null);
        Button btnExit, btnTour;
        btnTour = view.findViewById(R.id.btn_tour);
        btnExit = view.findViewById(R.id.btn_exit);
        btnTour.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.show();
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (gApiClient != null)
            gApiClient.disconnect();*/
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        isConnected = true;
        Log.i(TAG, "onConnected: " + isConnected);

    }

    @Override
    public void onConnectionSuspended(int i) {
        isConnected = false;
        Log.i(TAG, "onConnectionSuspended: " + isConnected);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //                if (isConnected)
        if (!fabLocOn) {
            setMap(latLng);
        }
        Log.d(TAG, "onMapLongClick() called with: latLngNearby = [" + latLng + "] fab" + fabLocOn);

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);

    }
    private class ProgressListener extends UtteranceProgressListener{
        @Override
        public void onStart(String utteranceId) {
            Log.i(TAG, "onStart utteranceId: " + utteranceId);
        }

        @Override
        public void onDone(String utteranceId) {
            //pause or some indication to inputnext
            if (!noInputNeeded) {
                Map.utteranceId = utteranceId;
                recognizeSpeech(utteranceId);
                Log.i(TAG, "onDone: utteranceId " + utteranceId);
            }
        }

        @Override
        public void onError(String utteranceId) {

        }

        @Override
        public void onError(String utteranceId, int errorCode) {
            Log.i(TAG, "onError: utteranceId" + utteranceId);
        }
    } //[innerclass]

    private class NearbyMarker {
        private LatLng latLng;
        private String name;

        public NearbyMarker(LatLng latLng, String name) {
            this.latLng = latLng;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mi_start_again) {
            restartNavigation();
        }
        if (item.getItemId() == R.id.launch_on_boot) {
            Intent k= new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=news.androidtv.launchonboot"));
            startActivity(k);
        }
        return super.onOptionsItemSelected(item);
    }

}
