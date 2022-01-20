package com.example.petroles;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";
    private static final int FINE_LOCATION_ACCESS_CODE = 1;
    private static final int COARSE_LOCATION_ACCESS_CODE = 2;
    private static final int LOCATION_ACCESS_CODES = 3;
    private static final float ZOOM = 15f;
    private static boolean fineLocationPermission = false;
    private static boolean coarseLocationPermission = false;

    MapView mapView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap gMap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the layout file as the content view.
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button_openMap);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
//        Places.initialize(getApplicationContext(), "${GMP_KEY}");
//        PlacesClient placesClient = Places.createClient(this);
//
//        mapView=findViewById(R.id.mapView);
//        mapView.getMapAsync(this);
//        mapView.onCreate(savedInstanceState);
//
//        // Button to show nearest charging station
//        Button button = (Button) findViewById(R.id.button_findStation);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //TODO: Implement google maps to find nearest electric charging station.
//                List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);
//
//                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
//
//                // Check if user have granted permission to access current location
//                if (checkLocationPermission()) {
//                    Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
//                    placeResponse.addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            FindCurrentPlaceResponse response = task.getResult();
//                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
//                                Log.i(TAG, String.format("Place '%s' has likelihood: %f",
//                                        placeLikelihood.getPlace().getName(),
//                                        placeLikelihood.getLikelihood()));
//                            }
//                        } else {
//                            Exception exception = task.getException();
//                            if (exception instanceof ApiException) {
//                                ApiException apiException = (ApiException) exception;
//                                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
//                            }
//                        }
//                    });
//                } else {
//                    requestLocationPermissions();
//                }
//            }
//        });
    }

//    // Method to check location permissions.
//    // Returns true if both fine and coarse location permissions are granted.
//    // Returns false otherwise.
//    private boolean checkLocationPermission () {
//        if ((ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
//        && (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
//            Log.d(TAG, "checkLocationPermission: Fine and coarse location permission granted.");
//            fineLocationPermission = true;
//            coarseLocationPermission = true;
//        } else {
//            Log.d(TAG, "checkLocationPermission: Fine or coarse location permission not granted." +
//                    " Possibly both are not granted.");
//            fineLocationPermission = false;
//            coarseLocationPermission = false;
//        }
//
//        return fineLocationPermission && coarseLocationPermission;
//    }
//
//    // Method to show the get the device's current location
//    private void getDeviceLocation() {
//        Log.d(TAG, "getDeviceLocation: getting device's current location");
//
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        try {
//            if (checkLocationPermission()) {
//                Task location = fusedLocationProviderClient.getLastLocation();
//                location.addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if(task.isSuccessful()) {
//                            Log.d(TAG, "onComplete: found location.");
//                            Location currentLocation = (Location) task.getResult();
//
//                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                            // TODO: The lat and lng returned is lat: 37.4219983, lng: -122.084, which is Google USA
//                            Log.d(TAG, "onComplete: moving camera to lat: " + latLng.latitude +
//                                    ", lng: " + latLng.longitude);
//                            // TODO: Solve moveCamera java.lang.NullPointerException 'void com.google.android.gms.maps.GoogleMap.moveCamera(com.google.android.gms.maps.CameraUpdate)' on a null object reference
//                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
//
//                        } else {
//                            Log.d(TAG, "onComplete: current location is null.");
//                            Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        } catch (SecurityException e) {
//            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
//        }
//    }
//
//    // Request the permission to access location.
//    // Request permission code is based on Google Developers Tutorial
//    private void requestLocationPermissions() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)
//        || ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)) {
//            new AlertDialog.Builder(this)
//                    .setTitle("Permission needed")
//                    .setMessage("This permission is needed because we need to know where you are to find" +
//                            " the nearest charging station.")
//                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_CODE);
//                        }
//                    })
//                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//                        }
//                    })
//                    .create().show();
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[] {
//                            ACCESS_FINE_LOCATION,
//                            ACCESS_COARSE_LOCATION
//                    },
//                    LOCATION_ACCESS_CODES);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        // super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_ACCESS_CODES) {
//            if (grantResults.length > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        if (ContextCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            getDeviceLocation();
//
//            gMap.setMyLocationEnabled(true);
//
//        }
//    }
//
//    //Lifecycle of the map
//    @Override
//    protected void onStart() {
//        super.onStart();
//        mapView.onStart();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mapView.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mapView.onStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mapView.onDestroy();
//    }
//
//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        mapView.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        mapView.onLowMemory();
//    }
}