package com.example.s3713532.map1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker mMarker;
    private double latitude;
    private double longitude;

    private LatLngBounds previousCamerBounds;

    private String json;
    private int state;
    private List<Shop> currDisplayShops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Start position
        latitude = 10.729339;
        longitude = 106.694286;
        currDisplayShops = new ArrayList<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng RMITVN = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(RMITVN).title("Marker in RMIT Vietnam").icon(BitmapDescriptorFactory.fromResource(R.drawable.kitty)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(RMITVN));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(RMITVN, 12f));

        // Get initial boundary values
        previousCamerBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        // Get the popular user buttons
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Toast.makeText(MapsActivity.this, latLng.toString(), Toast.LENGTH_SHORT).show();
                latitude = latLng.latitude;
                longitude = latLng.longitude;

                //mMarker

                String snippet = currentPlace(latitude, longitude);

                // Find current clicked place
                // When user clicks
                // Check through all the markers for the correct lat and lon
                // if it exist get the place to get all information
                // Then display it
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //mMap.clear();
                LatLngBounds currentCameraBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                if (isCameraViewChange(currentCameraBounds)) {
                    mMap.clear();
                    state = 1;
                    new GetNearbyShops().execute();
                }
            }
        });

        Button btnMilkTea = findViewById(R.id.btnMilkTea);
        btnMilkTea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                state = 2;
                new GetNearbyShops().execute();
            }
        });

        final Button priceBtn = findViewById(R.id.priceBtn);
        priceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPopupMenu(priceBtn, R.menu.popup_menu);
            }
        });

        final Button reviewBtn = findViewById(R.id.reviewBtn);
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPopupMenu(reviewBtn, R.menu.review_menu);

            }
        });

        final Button distanceBtn = findViewById(R.id.distanceBtn);
        distanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPopupMenu(distanceBtn, R.menu.popup_distance);
            }
        });

        final EditText input = new EditText(MapsActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        Button addShopBtn = findViewById(R.id.addShopBtn);
        addShopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                LinearLayout layout = new LinearLayout(MapsActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText nameBox = new EditText(MapsActivity.this);
                nameBox.setHint("Name");
                layout.addView(nameBox);

                final EditText priceBox = new EditText(MapsActivity.this);
                priceBox.setHint("Price");
                layout.addView(priceBox);

                final EditText impressionBox = new EditText(MapsActivity.this);
                impressionBox.setHint("Impression");
                layout.addView(impressionBox);

                final EditText addressBox = new EditText(MapsActivity.this);
                addressBox.setHint("Address");
                layout.addView(addressBox);

                final EditText styleBox = new EditText(MapsActivity.this);
                styleBox.setHint("Style");
                layout.addView(styleBox);

                final EditText photoBox1 = new EditText(MapsActivity.this);
                photoBox1.setHint("Photo");
                layout.addView(photoBox1);

                final EditText photoBox2 = new EditText(MapsActivity.this);
                photoBox2.setHint("Photo");
                layout.addView(photoBox2);


                builder.setView(layout);
                builder.setTitle("Add details about shop")
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Map<String, String> postData = new LinkedHashMap<>();

                                try {
                                    postData.put("name", nameBox.getText().toString());
                                    postData.put("price", priceBox.getText().toString());
                                    postData.put("impression", impressionBox.getText().toString());
                                    postData.put("address", addressBox.getText().toString());
                                    postData.put("lat", String.format("%.6f", latitude));
                                    postData.put("lon", String.format("%.6f", longitude));
                                    postData.put("style", styleBox.getText().toString());
                                    postData.put("photo1", "");
                                    postData.put("photo2", "");

                                    new SendShopDetails().execute(wwwEncodeMap(postData));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).create().show();
            }
        });

        final Button infoBtn = findViewById(R.id.infoBtn);
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (mMarker.isInfoWindowShown()) {
                        mMarker.hideInfoWindow();
                    } else {
                        mMarker.showInfoWindow();
                    }
                } catch (NullPointerException e) {
                    e.getMessage();
                }
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
                currDisplayShops = shopsWithinZoomLevel;
                ShowNearbyShops(shopsWithinZoomLevel);
            }

            if (state == 2) {
                currDisplayShops = nearbyShopList;
                ShowNearbyShops(nearbyShopList);
            }
        }
    }

    private class SendShopDetails extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            json = HttpHandler.post("http://bestlab.us:8080/places", strings[0]);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Gson gson = new Gson();

            Shop shop = gson.fromJson(json, Shop.class);

            Toast.makeText(MapsActivity.this, shop.getName(), Toast.LENGTH_SHORT).show();

            LatLng newShop = new LatLng(shop.getLat(), shop.getLon());
            mMap.addMarker(new MarkerOptions().position(newShop).title(shop.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.kitty)));
        }
    }

    private String currentPlace(double latitude, double longitude) {

        String snippet = null;
        for (Shop shop : currDisplayShops) {
            if (shop.getLat() == latitude && shop.getLon() == longitude) {
                snippet = "Address: " + shop.getAddress() + "\n" +
                        "Address: " + shop.getAddress() + "\n" +
                        "Price: " + shop.getPrice() + "\n" +
                        "Impression: " + shop.getImpression() + "\n" +
                        "Style: " + shop.getStyle() + "\n" +
                        "Photo1: " + shop.getPhoto1() + "\n" +
                        "Photo2: " + shop.getPhoto2() + "\n";

//                LatLng latLng = new LatLng(latitude, longitude);
//                MarkerOptions options = new MarkerOptions()
//                        .position(latLng)
//                        .title(shop.getName())
//                        .snippet(snippet);
//
//                mMarker = mMap.addMarker(options);
//                mMap.addMarker(options);
            }
            return snippet;
        }
    }

    // Popup menu
    private void createPopupMenu (Button button, int menuRes) {
        // Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(MapsActivity.this, button);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(menuRes, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int priceRange = Integer.parseInt(menuItem.getTitle().toString());
                List<Shop> shopsWithinPriceRange = findShopWithinPriceRange(currDisplayShops, priceRange);
                mMap.clear();
                ShowNearbyShops(shopsWithinPriceRange);

                return true;
            }
        });
        popup.show(); // showing popup menu
    }

    // If camera view changes
    private boolean isCameraViewChange (LatLngBounds currentCameraBounds) {
        double currX = currentCameraBounds.southwest.latitude;
        double currY = currentCameraBounds.northeast.longitude;

        double prevX = previousCamerBounds.southwest.latitude;
        double prevY = previousCamerBounds.northeast.longitude;

        if (currX != prevX || currY != prevY) {
            prevX = currX;
            prevY = currY;
            return true;
        }
        return false;
    }

    // Shops within price range
    private List<Shop> findShopWithinPriceRange (List<Shop> nearbyShopList, int priceRange) {

        List<Shop> shopsWithinPriceRange = new ArrayList<>();

        for (Shop shop : nearbyShopList) {
            if (shop.getPrice() <= priceRange) {
                shopsWithinPriceRange.add(shop);
            }
        }
        return shopsWithinPriceRange;

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
            if (shopLat >= x1 && shopLat <= x2 && shopLon <= y1 && shopLon >= y2) {
                shopWithinZoomLevel.add(shop);
            }
        }
        return shopWithinZoomLevel;
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
            markerOptions.title(shop.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.kitty));
            // Not sure if it should be placed here
            mMarker = mMap.addMarker(markerOptions);
            // move map camera
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
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

    private String wwwEncodeMap(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}

