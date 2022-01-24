package com.example.petroles;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Global variable declaration
    private static final String TAG = "MapActivity";
    private static final float ZOOM = 15f;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    private static final String WIFI_STATE = Manifest.permission.ACCESS_WIFI_STATE;
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int INTERNET_REQUEST_CODE = 200;
    private Boolean locationPermissionGranted = false;
    private boolean networkPermissionGranted = false;
    private boolean cameraSet = false;
    private static final String CHARGING_STATION = "Charging Station";

    // Object declaration
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap gMap;
    LocationRequest locationRequest;
    Marker currentLocationMarker;
    GeoApiContext geoApiContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Initialize Places SDK
        Places.initialize(getApplicationContext(), BuildConfig.GMP_KEY);
        PlacesClient placesClient = Places.createClient(this);

        // Initialize GeoApiContext
        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(BuildConfig.GMP_KEY)
                    .build();
        }

        // Creating a supportMapFragment
        getPermission();
        // If and only if the location permission is granted, we will initialize the map
        if (locationPermissionGranted && networkPermissionGranted) {
            Log.d(TAG, "onCreate: initializing map");
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            // A button to find stations
            Button button = (Button) findViewById(R.id.button_findStation);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    locateStation();
                }
            });
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop location update when MapActivity is not in use
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is ready");
        gMap = googleMap;

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (locationPermissionGranted) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                        locationCallback, Looper.myLooper());
                gMap.setMyLocationEnabled(true);
            } else {
                getPermission();
            }
        } else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper());
            gMap.setMyLocationEnabled(true);
        }
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                // Get the newest location on the list
                Location location = locationList.get(locationList.size() - 1);
                Log.i(TAG, "Location: " + location.getLatitude() + " " + location.getLongitude());

                if (currentLocationMarker != null) {
                    currentLocationMarker.remove();
                }

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                // Zoom into the place once only, so user is free to scroll around the map
                if (!cameraSet) {
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
                    cameraSet = true;
                }
            }
        }
    };

    // Function to ask for user's permission to access location
    private void getPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE};

        /*
            Nested if to check both fine location and coarse location permission
            If either one false, will request permission
         */
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;

                if (ContextCompat.checkSelfPermission(this.getApplicationContext(), NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this.getApplicationContext(), WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                        networkPermissionGranted = true;
                    } else {
                        ActivityCompat.requestPermissions(this, permissions, INTERNET_REQUEST_CODE);
                    }
                } else {
                    ActivityCompat.requestPermissions(this, permissions, INTERNET_REQUEST_CODE);
                }
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermission: called");
        locationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermission: location permission not granted");
                            return;
                        }
                    }

                    Log.d(TAG, "onRequestPermission: permission granted");
                    locationPermissionGranted = true;
                }
            }
            case INTERNET_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            networkPermissionGranted = false;
                            Log.d(TAG, "onRequestPermission: network permission not granted");
                            return;
                        }
                    }

                    Log.d(TAG, "onRequestPermission: permission granted");
                    networkPermissionGranted = true;
                }
            }
        }
    }

    private void locateStation() {
        Log.d(TAG, "locateStation: locating stations");

        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(geoApiContext, CHARGING_STATION).await();
            Log.d(TAG, "locateStation: number of stations located: " + response.results.length);

            // To store multiple locations of stations
            List<LatLng> locationList = new ArrayList<>();

            for (int i=0, c=0; i<response.results.length; i++) {
                // Make a maximum of 5 results
                if (c == 5) {
                    break;
                } else {
                    PlacesSearchResult result = response.results[i];
                    Log.d(TAG, "locateStation: Result of location located: lat: " +
                            result.geometry.location.lat + " lng: " + result.geometry.location.lng);
                    locationList.add(new LatLng(result.geometry.location.lat, result.geometry.location.lng));
                    gMap.addMarker(new MarkerOptions().position(locationList.get(i))
                            .title(result.name));
                    gMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM + 100.0f));
                    gMap.moveCamera(CameraUpdateFactory.newLatLng(locationList.get(i)));
                }
                c++;
            }
//            } else {
//                Toast.makeText(this, "No locations found.", Toast.LENGTH_SHORT).show();
//                Log.e(TAG, "locateStation: no charging stations located");
//            }

        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Geocoder geocoder = new Geocoder(MapActivity.this);
//        List<Address> list = new ArrayList<>();
//
//        if (isInternetAvailable() && isNetworkConnected())  {
//            try {
//                list = geocoder.getFromLocationName("Penang", 3);
//                if (list.size() > 0) {
//                    // Create an array of addresses found
//                    Address[] addresses = new Address[list.size()];
//                    for (int i = 0; i < list.size(); i++) {
//                        addresses[i] = list.get(i);
//                    }
//                    Log.d(TAG, "locateStation: number of locations found: " + list.size());
//                } else {
//                    Log.d(TAG, "locateStation: no locations were found");
//                }
//            } catch (IOException e) {
//                Log.e(TAG, "locateStation: IOException: " + e);
//            }
//
//        // Else if statement to know which error is occurring
//        } else if (!isInternetAvailable()) {
//            Log.e(TAG, "locateStation: isInternetAvailable: No internet available");
//        } else if (!isNetworkConnected()) {
//            Log.e(TAG, "locateStation: isInternetAvailable: No network connected");
//        } else {
//            Log.e(TAG, "locateStation: isInternetAvailable: Unidentified error");
//        }

    }

    // To check if device is connected to network
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    // To check if network connected has internet connection
    private boolean isInternetAvailable() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            Log.e(TAG, "isInternetAvailable: No internet available. " + e.getMessage());

        } catch (InterruptedException e) {
            Log.e(TAG, "isInternetAvailable: No internet available. " + e.getMessage());
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        new shutdownContext().execute();
    }

    class shutdownContext extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            geoApiContext.shutdown();
            return null;
        }
    }
}