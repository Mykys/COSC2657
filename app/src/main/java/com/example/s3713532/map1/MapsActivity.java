package com.example.s3713532.map1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
    private double latitude;
    private double longitude;
    private String json;
    private int state;
    private LatLngBounds previousCamerBounds;
    private List<Shop> currDisplayShops;
    private List<String> inputString = new ArrayList<>();

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
        currDisplayShops = new ArrayList<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng RMITVN = new LatLng(10.729339, 106.694286);
        mMap.addMarker(new MarkerOptions().position(RMITVN).title("Marker in RMIT Vietnam"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(RMITVN));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(RMITVN, 12f));

        // Get initial boundary values
        previousCamerBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

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

        final Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MapsActivity.this, button1);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Toast.makeText(MapsActivity.this, "You clicked : " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        int priceRange = Integer.parseInt(menuItem.getTitle().toString());
                        List<Shop> shopsWithinPriceRange = findShopWithinPriceRange(currDisplayShops, priceRange);
                        mMap.clear();
                        ShowNearbyShops(shopsWithinPriceRange);


                        return true;
                    }
                });
                popup.show(); // showing popup menu
            }
        });



        final EditText input = new EditText(MapsActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // This button should only be enabled when user clicks on the map and a marker is added
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


                builder.setView(layout);
                builder.setTitle("Add details about shop")
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                inputString.add(nameBox.getText().toString());
                                inputString.add(priceBox.getText().toString());
                                inputString.add(impressionBox.getText().toString());
                                inputString.add(addressBox.getText().toString());
                                inputString.add(styleBox.getText().toString());

                                Map<String, String> postData = new LinkedHashMap<String, String>();

                                try {
                                    postData.put("name", nameBox.getText().toString());
                                    postData.put("price", priceBox.getText().toString());
                                    postData.put("impression", impressionBox.getText().toString());
                                    postData.put("address", addressBox.getText().toString());
                                    postData.put("lat", String.format("%f", latitude));
                                    postData.put("lon", String.format("%f", longitude));
                                    postData.put("style", styleBox.getText().toString());
                                    postData.put("photo1", "");
                                    postData.put("photo2", "");

                                    Toast.makeText(MapsActivity.this, postData.toString(), Toast.LENGTH_LONG).show();
                                    new SendShopDetails().execute(wwwEncodeHashMap(postData));
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

            // Appen kraschar när den försöker öppna en Toast i bakgrunden. / R
            //Toast.makeText(MapsActivity.this, json, Toast.LENGTH_SHORT).show();

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Gson gson = new Gson();

            Shop shop = gson.fromJson(json, Shop.class);

            Toast.makeText(MapsActivity.this, shop.getName(), Toast.LENGTH_SHORT).show();

            LatLng newShop = new LatLng(shop.getLat(), shop.getLon());
            mMap.addMarker(new MarkerOptions().position(newShop).title(shop.getName()));
        }
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
            markerOptions.title(shop.getName());
            mMap.addMarker(markerOptions);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
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

    private String wwwEncodeHashMap(Map<String, String> params) throws UnsupportedEncodingException {
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
