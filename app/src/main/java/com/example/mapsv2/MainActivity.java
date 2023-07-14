package com.example.mapsv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private final int FINE_PERMISSION_CODE = 1;
    private GoogleMap gMap;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private SearchView sMap;

    Marker m = null;
    Marker mySelf;
    Marker to;
    Marker to2;
    Polyline p;
    Polyline p2;
    String location;
    String userLocation;
    String userDestination;
    List<Address> addressList;
    LatLng latLng;
    Address address;
    private Button btnGetDirection;
    private Button btnGetWaypoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        sMap = findViewById(R.id.mapSearch);
        btnGetDirection = findViewById(R.id.btnDirection);
        btnGetWaypoint = findViewById(R.id.btnWaypoint);


        sMap.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                location = sMap.getQuery().toString();
                addressList = null;

                if (location != null) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    address = addressList.get(0);

                    m.remove();

                    latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    m = gMap.addMarker(new MarkerOptions().position(latLng).title(location.toString()));
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                    to2.setTitle(location);

                    //---------------------------------------------------------------
                    btnGetWaypoint.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btnGetDirection.setVisibility(View.VISIBLE);

                            if(to != null){
                                to.remove();
                            }
                            if(p != null){
                                p.remove();
                            }

                            to = gMap.addMarker(new MarkerOptions()
                                    .position(m.getPosition())
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .title(sMap.getQuery().toString()));

                            p = gMap.addPolyline(new PolylineOptions()
                                    .clickable(true)
                                    .add(
                                            new LatLng(mySelf.getPosition().latitude, mySelf.getPosition().longitude),
                                            new LatLng(to.getPosition().latitude, to.getPosition().longitude))
                                    .color(Color.RED)
                                    .geodesic(true));
                            p.setVisible(true);
                    //-------------------------------------------------------------
                        }
                    });
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        btnGetDirection.setOnClickListener(view -> {
            userLocation = mySelf.getTitle();
            userDestination = sMap.getQuery().toString();

            try {
                if (userLocation.equals("") || userDestination.equals("")){
                    Toast.makeText(this, "Please enter the two cities", Toast.LENGTH_SHORT).show();
                }else {
                    getDestination(userLocation, userDestination);
                }
            }catch (Exception e){
                Toast.makeText(this, "Errore nel try, o currentLocation o location sono o null o non lo so", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void getDestination(String from, String to){
        try {
            Uri uri = Uri.parse("https://www.google.com/maps/dir/" + from + "/" + to);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps&hl=it&gl=US");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    // METODO PER I PERMESSI DELLA POSIZIONE
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MainActivity.this);
                }
            }
        });
    }

    public String getUserDestination() {
        return userDestination;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        float z = 10.0f;

        LatLng latLng0 = new LatLng(0, 0);
        m = gMap.addMarker(new MarkerOptions().position(latLng0).title("Location"));
        m.setVisible(false);

        LatLng me = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mySelf = gMap.addMarker(new MarkerOptions()
                .position(me)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("My Location"));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(me, z));

        googleMap.setOnMapClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == FINE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }else{
                Toast.makeText(this, "Location permission in denied, please allow the permission to access your position", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        gMap.clear();

        LatLng me = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mySelf = gMap.addMarker(new MarkerOptions()
                .position(me)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("My Location"));

        LatLng l = new LatLng(latitude, longitude);
        to2 = gMap.addMarker(new MarkerOptions().position(l).title(getUserDestination()));

        p2 = gMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(mySelf.getPosition().latitude, mySelf.getPosition().longitude),
                        new LatLng(to2.getPosition().latitude, to2.getPosition().longitude))
                .color(Color.RED)
                .geodesic(true));



        CameraUpdate newPos = CameraUpdateFactory.newLatLng(l);
        gMap.animateCamera(newPos);
    }


}