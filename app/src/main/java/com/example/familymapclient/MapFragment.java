package com.example.familymapclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap map;
    private HashMap<String, Float> eventTypeColors = new HashMap<>();
    private float[] colorArray = {BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_VIOLET, BitmapDescriptorFactory.HUE_YELLOW};
    private int colorIndex = 0;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // This line is important to display the options menu
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.map_options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // Handle search action
                return true;
            case R.id.action_settings:
                // Handle settings action
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        MapsInitializer.initialize(getActivity());

        // Set a default location and zoom level
        LatLng defaultLocation = new LatLng(40.2338, -111.6585);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5));

        // Cache data and setup event markers
        String authToken = getAuthTokenFromStorage();
        ServerProxy.cacheEventListener cacheListener = new ServerProxy.cacheEventListener() {
            @Override
            public void onCacheEventSuccess(String message) {
                Log.d("ServerProxy", "onCacheEventSuccess: " + message);
            }

            @Override
            public void onCacheEventError(String error) {
                Log.d("ServerProxy", "onCacheEventError: " + error);
            }
        };
        ServerProxy serverProxy = new ServerProxy(requireContext());
        new CacheDataTask(serverProxy, cacheListener).execute();
    }

    private void setupEventMarkers(JSONObject eventsData) {
        // Get the events from the cache
        String authToken = getAuthTokenFromStorage();

        if (eventsData != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            try {
                JSONArray eventsArray = eventsData.getJSONArray("data");
                for (int i = 0; i < eventsArray.length(); i++) {
                    JSONObject event = eventsArray.getJSONObject(i);
                    // Retrieve event details
                    String eventType = event.getString("eventType");
                    double latitude = event.getDouble("latitude");
                    double longitude = event.getDouble("longitude");
                    String eventId = event.getString("eventID");

                    // Get color for the event type
                    float markerColor = getMarkerColor(eventType);

                    // Add a marker on the map for this event
                    LatLng eventLocation = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions().position(eventLocation).title(eventId).icon(BitmapDescriptorFactory.defaultMarker(markerColor));
                    map.addMarker(markerOptions);

                    // Include the event location in the bounds
                    builder.include(eventLocation);
                }

                // Move the camera to fit all markers with a padding
                LatLngBounds bounds = builder.build();
                int padding = 100; // Padding in pixels
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                map.moveCamera(cameraUpdate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private float getMarkerColor(String eventType) {
        if (!eventTypeColors.containsKey(eventType)) {
            eventTypeColors.put(eventType, colorArray[colorIndex]);
            colorIndex = (colorIndex + 1) % colorArray.length;
        }
        return eventTypeColors.get(eventType);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Retrieve the authToken from where you stored it
        String authToken = getAuthTokenFromStorage();

        // Get the cached family data
        ServerProxy serverProxy = new ServerProxy(requireContext());
        JSONObject familyData = serverProxy.getFamilyDataFromCache(authToken);

        if (familyData != null) {
            // Update the TextView with the person's information
            updateEventInfoTextView(familyData);
        }

        // Execute CacheDataTask
        CacheDataTask cacheDataTask = new CacheDataTask(serverProxy, new ServerProxy.cacheEventListener() {
            @Override
            public void onCacheEventSuccess(String message) {
            }

            @Override
            public void onCacheEventError(String error) {
            }

            @Override
            public void onCacheEventCompleted(JSONObject jsonObject) {
                // Update the map with the cached data
                setupEventMarkers(jsonObject);
            }
        });
        cacheDataTask.execute(authToken);
    }

    private String getAuthTokenFromStorage() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("authToken", null);
    }

    private void updateEventInfoTextView(JSONObject familyData) {
        TextView eventInfoTextView = requireView().findViewById(R.id.event_info_text_view);

        try {
            String firstName = familyData.getString("firstName");
            String lastName = familyData.getString("lastName");
            eventInfoTextView.setText("Name: " + firstName + " " + lastName);
            Log.d("MapFragment", "Family data: " + familyData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}