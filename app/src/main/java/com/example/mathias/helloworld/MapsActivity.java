package com.example.mathias.helloworld;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager mLocationManager; // Provides information on user location
    private boolean mRequestingUpdates = false;
    private double mLatitude;
    private double mLongitude;
    private int minUpdateTime = 0;
    private int minUpdateDist = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker").snippet("Snippet"));
        //Dummy marker?

        // Enable MyLocation Layer of Google Map
        //mMap.setMyLocationEnabled(true);

        // Create a criteria object to retrieve provider
        //Criteria criteria = new Criteria();

        // Get the name of the best provider
        //String provider = getLocationManager().getBestProvider(criteria, true);

        // Get Current Location from the best provider
        Location myLocation = getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //Fejlen ovenover angiver om vi har faaet adgang til last know location.
        //Vi kan godt indfoere tjekket eller droppe det
        //Den beholder vi simpelthen bare og dropper at checke efter noget.

        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Get latitude and longitude of the current location
        mLatitude = myLocation.getLatitude();
        mLongitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng myCoordinates = new LatLng(mLatitude, mLongitude);

        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myCoordinates));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        
        // Add marker showing your location
        mMap.addMarker(new MarkerOptions().position(new LatLng(mLatitude, mLongitude)).title("Dig").snippet("Her er jeg!"));

        // Function for zooming to current location
        //CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(myCoordinates, 12);
        //mMap.animateCamera(yourLocation);

        //Request continous updates
        requestUpdatesIfNeeded(provider);


    }


    private LocationManager getLocationManager() {
        if (mLocationManager == null)
            // Get our LocationManager object from System Service LOCATION_SERVICE
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return mLocationManager;
    }

    //Request continous updates
    private void requestUpdatesIfNeeded(String provider){

        if (!mRequestingUpdates) {
            // create locationListener, will receive location updates
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                    updateServerPosition();

                }
                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {}
                @Override
                public void onProviderEnabled(String s) {}
                @Override
                public void onProviderDisabled(String s) {}
            };

            //start receiving continous position updates
            mLocationManager.requestLocationUpdates(provider, minUpdateTime, minUpdateDist, locationListener);

            mRequestingUpdates = true;
        }

    }

    private boolean updateServerPosition() {
        String req_tag = "req_update_position";

        StringRequest req = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN,
                new Response.Listener<String>() {
                    @Override  //If succesfull, should move to next screen
                    public void onResponse(String response) {
                        Log.d("position update", "position update response: " + response);

                        try {
                            //Create JSONObject, easier to work with
                            JSONObject JResponse = new JSONObject(response);
                            boolean error = JResponse.getBoolean("error");
                            if (!error) {}
                            //Shows an error if we couldn't login and prints the error message from server
                            //TODO don't show the direct message but write a custom error message
                            else{
                                Log.e("position update", "position update error: " + response);
                                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override //If not succesfull, show user error message
            //only does it, if there's a network error, not login error
            public void onErrorResponse(VolleyError error) {
                Log.e("login", "Login error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        })  {
            @Override // Set all parameters for for server
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "updatePosition");
                params.put("email", UserStatic.getEmail());
                params.put("latitude", String.valueOf(mLatitude));
                params.put("longitude", String.valueOf(mLongitude));
                return params;
            }
        };

        req.addMarker(req_tag);
        //Adding request to request queue
        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
        return true;
    }
}
