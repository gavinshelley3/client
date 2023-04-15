package com.example.familymapclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

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
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.Marker;

import Model.Event;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap map;
    private HashMap<String, Float> eventTypeColors = new HashMap<>();
    private float[] colorArray = {BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_VIOLET, BitmapDescriptorFactory.HUE_YELLOW};
    private ClusterManager<ClusterMarker> clusterManager;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Get the MainActivity instance and access the ServerProxy
        MainActivity mainActivity = (MainActivity) getActivity();
        ServerProxy serverProxy = mainActivity.getServerProxy();

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

        // Setup the ClusterManager
        clusterManager = new ClusterManager<>(getActivity(), map);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterMarker>() {
            @Override
            public boolean onClusterItemClick(ClusterMarker item) {
                updateEventInfoTextView(item);
                return false;
            }
        });

        // Set ClusterRenderer
        CustomClusterRenderer renderer = new CustomClusterRenderer(getActivity(), map, clusterManager);
        clusterManager.setRenderer(renderer);

        // Cache data and setup event markers
        String authToken = getAuthTokenFromStorage();
        MainActivity mainActivity = (MainActivity) getActivity();
        ServerProxy serverProxy = mainActivity.getServerProxy();
        ServerProxy.cacheEventListener cacheListener = new ServerProxy.cacheEventListener() {
            @Override
            public void onCacheEventSuccess(String message) {
                Log.d("ServerProxy", "onCacheEventSuccess: " + message);
            }

            @Override
            public void onCacheEventError(String error) {
                Log.d("ServerProxy", "onCacheEventError: " + error);
            }

            @Override
            public void onCacheEventCompleted(JSONObject jsonObject) {
                // Update the map with the cached data
                setupEventMarkers(jsonObject);
            }
        };
        CacheDataTask cacheDataTask = new CacheDataTask(serverProxy, cacheListener);
        cacheDataTask.execute(authToken);
    }

    private void setupEventMarkers(JSONObject eventsData) {
        AddMarkersTask addMarkersTask = new AddMarkersTask(map, eventsData, eventTypeColors, colorArray, clusterManager);
        addMarkersTask.execute();
    }

    private void updateEventInfoTextView(ClusterMarker item) {
        TextView eventInfoTextView = requireView().findViewById(R.id.event_info_text_view);

        Event event = item.getEvent();
        String personID = event.getPersonID();

        // Get the MainActivity instance and access the ServerProxy
        MainActivity mainActivity = (MainActivity) getActivity();
        ServerProxy serverProxy = mainActivity.getServerProxy();

        // Retrieve the person object from the cache based on the personID
        JSONObject personData = serverProxy.getPersonFromCache(personID);

        if (personData != null) {
            try {
                String firstName = personData.getString("firstName");
                String lastName = personData.getString("lastName");
                String gender = personData.getString("gender");
                String eventInfo = firstName + " " + lastName + "\n" + event.getEventType().toUpperCase() + ": " + event.getCity() + ", " + event.getCountry() + "(" + event.getYear() + ")";
                eventInfoTextView.setText(eventInfo);

                // Set the gender icon
                if (gender.equalsIgnoreCase("m")) {
                    Drawable maleIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_male_lg);
                    Drawable resizedMaleIcon = resizeDrawable(maleIcon, 48, 48); // Set the desired width and height
                    eventInfoTextView.setCompoundDrawablesWithIntrinsicBounds(resizedMaleIcon, null, null, null);
                } else if (gender.equalsIgnoreCase("f")) {
                    Drawable femaleIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_female_lg);
                    Drawable resizedFemaleIcon = resizeDrawable(femaleIcon, 48, 48); // Set the desired width and height
                    eventInfoTextView.setCompoundDrawablesWithIntrinsicBounds(resizedFemaleIcon, null, null, null);
                }
                // Add some padding between the icon and the text
                eventInfoTextView.setCompoundDrawablePadding(5);

                // Change the TextView background color to match the marker color
                float markerColor = item.getMarkerColor();
                int markerColorInt = floatToColor(markerColor);
                eventInfoTextView.setBackgroundColor(markerColorInt);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class CustomClusterRenderer extends DefaultClusterRenderer<ClusterMarker> {
        public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
            markerOptions.title(item.getTitle()).snippet(item.getSnippet()).icon(BitmapDescriptorFactory.defaultMarker(item.getMarkerColor()));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("MapFragmentDebug", "onActivityCreated: started");

        // Retrieve the authToken from cache
        String authToken = getAuthTokenFromStorage();

        // Get the cached family data
        ServerProxy serverProxy = new ServerProxy(requireContext());
        JSONObject familyData = serverProxy.getFamilyDataFromCache(authToken);

        if (familyData != null) {
            // Update the TextView with the person's information
            updateEventInfoTextView(familyData);
        }

        Log.d("MapFragmentDebug", "onActivityCreated: finished");
    }

    private String getAuthTokenFromStorage() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", null);
        Log.d("MapFragmentDebug", "getAuthTokenFromStorage: authToken = " + authToken);
        return authToken;
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

    private Drawable resizeDrawable(Drawable drawable, int width, int height) {
        if (drawable != null) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            return new BitmapDrawable(getResources(), resizedBitmap);
        }
        return null;
    }

    private int floatToColor(float markerColor) {
        float[] hsv = new float[3];
        hsv[0] = markerColor;
        hsv[1] = 1f; // Saturation
        hsv[2] = 1f; // Value
        return Color.HSVToColor(hsv);
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