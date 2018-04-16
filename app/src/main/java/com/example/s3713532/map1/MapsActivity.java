package com.example.s3713532.map1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double latitude;
    double longitude;
    protected String json;
    //private Shop[] shops;
    private int state;
    //private LatLngBounds latLngBounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        latitude = 10.729339;
        longitude = 106.694286;

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng RMITVN = new LatLng(10.729339, 106.694286);
        mMap.addMarker(new MarkerOptions().position(RMITVN).title("Marker in RMIT Vietnam"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(RMITVN));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(RMITVN, 12f));

        // Get the popular user buttons
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(MapsActivity.this, latLng.toString(), Toast.LENGTH_SHORT).show();
                latitude = latLng.latitude;
                longitude = latLng.longitude;
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //mMap.clear();
                state = 1;
                Toast.makeText(MapsActivity.this, "Hello Vinh", Toast.LENGTH_SHORT).show();
                new GetNearbyShops().execute();
                //List<Shop> shopsWithinZoomLevel = findShopWithinZoomLevel(shops, latLngBounds);
                //ShowNearbyShops(shopsWithinZoomLevel);
            }
        });

        Button btnMilkTea = findViewById(R.id.btnMilkTea);
        btnMilkTea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                state = 2;
                new GetNearbyShops().execute();
                //GetNearbyShops getNearbyShops = new GetNearbyShops();
                //getNearbyShops.execute();
            }
        });
    }

    private class GetNearbyShops extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            json = HttpHandler.get("http://bestlab.us:8080/places");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Gson gson = new Gson();

            // Retrieves all shops in database
            Shop[] shops = gson.fromJson(json, Shop[].class);
            List<Shop> nearbyShopList = findNearbyShops(shops, latitude, longitude, 10);

            if (state == 1) {
                LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                List<Shop> shopsWithinZoomLevel = findShopWithinZoomLevel(nearbyShopList, latLngBounds);
                ShowNearbyShops(shopsWithinZoomLevel);
                Toast.makeText(MapsActivity.this, "Hello Trieu", Toast.LENGTH_SHORT).show();
            }

            if (state == 2) {
                ShowNearbyShops(nearbyShopList);
                Toast.makeText(MapsActivity.this, "Hello Quang", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Shop within zoom level
    private List<Shop> findShopWithinZoomLevel (List<Shop> nearbyShopList, LatLngBounds currentCameraBounds) {
        double x1 = currentCameraBounds.southwest.latitude;
        double x2 = currentCameraBounds.northeast.latitude;
        double y1 = currentCameraBounds.northeast.longitude;
        double y2 = currentCameraBounds.southwest.longitude;

        List<Shop> shopWithinZoomLevel = new ArrayList<>();

        for (Shop shop : nearbyShopList) {
            double shopLat = shop.getLat();
            double shopLon = shop.getLon();
            Toast.makeText(MapsActivity.this, "Hello Toan", Toast.LENGTH_SHORT).show();
            if (shopLat >= x1 && shopLat <= x2 && shopLon >= y1 && shopLon <= y2) {
                shopWithinZoomLevel.add(shop);
                Toast.makeText(MapsActivity.this, shop.getName(), Toast.LENGTH_SHORT).show();
            }
        }
        return shopWithinZoomLevel;
    }

    private boolean isBetween (double latitude, double longitude, LatLngBounds currentCameraBounds) {

    }

    // Find nearby places from chosen location
    private List<Shop> findNearbyShops(Shop[] shops, double currLat, double currLon, double radius) {
        List<Shop> nearbyShopList = new ArrayList<>();
        for (Shop shop : shops) {
            // Need to convert from miles to km
            double dist = distance(shop.getLat(), shop.getLon(), currLat, currLon) * 1.609344;
            if (dist <= radius) {
                nearbyShopList.add(shop);
            }
        }
        return nearbyShopList;
    }

    // Show nearby shops in map
    private void ShowNearbyShops(List<Shop> nearbyShopsList) {
        for (Shop shop : nearbyShopsList) {
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(shop.getLat(), shop.getLon());
            markerOptions.position(latLng);
            markerOptions.title(shop.getName());
            mMap.addMarker(markerOptions);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            // move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            //Toast.makeText(MapsActivity.this, "Hello", Toast.LENGTH_SHORT).show();
        }
    }


    // Calculate the distance between two locations
    private double getDistance (LatLng locA, LatLng locB) {
        return distance(locA.latitude, locA.longitude, locB.latitude, locB.longitude);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
