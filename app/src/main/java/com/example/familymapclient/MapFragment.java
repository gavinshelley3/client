package com.example.familymapclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.collections.MarkerManager;

import java.util.Arrays;
import java.util.HashMap;

import Model.Event;
import Model.Person;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private MapView mapView;
    private GoogleMap map;
    private HashMap<String, Float> eventTypeColors = new HashMap<>();
    private float[] colorArray = {BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_VIOLET, BitmapDescriptorFactory.HUE_YELLOW};
    private ClusterManager<ClusterMarker> clusterManager;
    private String currentPersonID;
    private ServerProxy serverProxy;
    private Context context;
    private Event currentEvent;
    private Person loggedInPerson;
    private ServerProxy.cacheEventListener cacheEventListener;
    private ServerProxy.cachePersonListener cachePersonListener;
    private MapEventManager mapEventManager;
    private boolean isMapReady = false;
    private MapData mapData;
    private Person displayedPerson;
    private static final int SETTINGS_REQUEST_CODE = 1;
    private static final int SEARCH_REQUEST_CODE = 2;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Instantiate the server proxy
        context = getActivity();
        serverProxy = ServerProxy.getInstance(context);

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        TextView eventInfoTextView = view.findViewById(R.id.event_info_text_view);
        eventInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPersonActivity();
            }
        });

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
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivityForResult(searchIntent, SEARCH_REQUEST_CODE);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        MapsInitializer.initialize(getActivity());

        // Setup the ClusterManager
        setupClusterManager();

        // Cache data and setup event markers
        String authToken = getAuthTokenFromStorage();

        // Instantiate the server proxy
        context = getActivity();
        serverProxy = ServerProxy.getInstance(context);

        // Initialize the cacheListener for events and persons
        setupCacheListeners();

        // Create and execute the CacheDataTask
        String initialEventID = getArguments() != null ? getArguments().getString("initialEventID") : null;
        CacheDataTask cacheDataTask = new CacheDataTask(serverProxy, cacheEventListener, cachePersonListener);
        cacheDataTask.execute(authToken);

        // Set the map ready flag
        isMapReady = true;

        // Set onMapLoadedCallback
        map.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        // If initialEventID is passed as an argument and the map is ready, focus on the cached event's location
        if (getArguments() != null && isMapReady) {
            String initialEventID = getArguments().getString("initialEventID");
            if (initialEventID != null) {
                focusOnInitialEvent(initialEventID);
            } else {
                String personID = getArguments().getString("personID");
                if (personID != null) {
                    Person person = serverProxy.getPersonFromCache(personID);
                    focusOnCachedPerson(person);
                }
            }
        }
    }

    private void setupClusterManager() {
        clusterManager = new ClusterManager<>(getActivity(), map);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        map.setOnInfoWindowClickListener(clusterManager);

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
    }

    private void setupCacheListeners() {
        cacheEventListener = new ServerProxy.cacheEventListener() {
            @Override
            public void onCacheEventSuccess(Event[] events) {
                // Update the map with the cached data
                Log.d("MapFragment", "onCacheEventCompleted: " + Arrays.toString(events));
                setupEventMarkers(events);

                // If initialEventID is passed as an argument, focus on the cached event's location
                if (getArguments() != null) {
                    String initialEventID = getArguments().getString("initialEventID");
                    if (initialEventID != null) {
                        focusOnInitialEvent(initialEventID);
                    }
                }
            }

            @Override
            public void onCacheEventError(String error) {
                Log.d("ServerProxy", "onResponse: " + error);
            }
        };

        cachePersonListener = new ServerProxy.cachePersonListener() {
            @Override
            public void onCacheSinglePersonSuccess(Person person) {
                // Update the map with the cached data
                Log.d("MapFragment", "onCacheSinglePersonSuccess: " + person);
                focusOnCachedPerson(person);
            }

            @Override
            public void onCacheMultiplePersonsSuccess(Person[] persons) {
                // Navigate to person's birth location
                Bundle args = getArguments();
                if (args != null) {
                    String personID = args.getString("personID");
                    if (personID != null) {
                        Person person = serverProxy.getPersonFromCache(personID);
                        focusOnCachedPerson(person);
                    }
                }
            }

            @Override
            public void onCachePersonError(String error) {
                Log.d("MapFragment", "onCachePersonError: " + error);
            }
        };
    }

    private void setupEventMarkers(Event[] events) {
        mapEventManager = new MapEventManager(map, events, eventTypeColors, colorArray, clusterManager, getFilterSettings(), getActivity());
        displayedPerson = getDisplayedPerson();
        mapEventManager.execute(displayedPerson);
    }

    private void updateEventInfoTextView(ClusterMarker item) {
        TextView eventInfoTextView = requireView().findViewById(R.id.event_info_text_view);

        Event event = item.getEvent();
        String personID = event.getPersonID();
        Log.d("MapFragment", "updateEventInfoTextView: " + personID + "\n" + event.getEventType() + ": " + event.getCity() + ", " + event.getCountry() + "(" + event.getYear() + ")");

        // Instantiate the server proxy
        context = getActivity();
        serverProxy = ServerProxy.getInstance(context);

        // Retrieve the person object from the cache based on the personID
        Person person = serverProxy.getPersonFromCache(personID);
        Log.d("MapFragment", "updateEventInfoTextView: " + person);

        if (person != null) {
            currentPersonID = personID;
            loggedInPerson = serverProxy.getLoggedInUser();
            setDisplayedPerson(person);
            String firstName = person.getFirstName();
            String lastName = person.getLastName();
            String gender = person.getGender();
            String eventInfo = firstName + " " + lastName + "\n" + event.getEventType().toUpperCase() + ": " + event.getCity() + ", " + event.getCountry() + "(" + event.getYear() + ")";
            eventInfoTextView.setText(eventInfo);

            // Change the TextView background color to match the marker color
            float markerColor = item.getMarkerColor();
            int markerColorInt = floatToColor(markerColor);
            eventInfoTextView.setBackgroundColor(markerColorInt);
            // Log the marker color
            Log.d("MapFragment", "updateEventInfoTextViewMarkerColor: " + markerColorInt);


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
            // Refresh the map
            mapEventManager.refresh(getDisplayedPerson());
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

        // Set the displayedPerson
        setDisplayedPerson(serverProxy.getLoggedInUser());

        // Retrieve the authToken from cache
        String authToken = getAuthTokenFromStorage();

        // If initialEventID is passed as an argument and the map is ready, focus on the cached event's location
        if (getArguments() != null && isMapReady) {
            String initialEventID = getArguments().getString("initialEventID");
            if (initialEventID != null) {
                focusOnInitialEvent(initialEventID);
            } else {
                String personID = getArguments().getString("personID");
                if (personID != null) {
                    Person person = serverProxy.getPersonFromCache(personID);
                    focusOnCachedPerson(person);
                }
            }
        }

        Log.d("MapFragmentDebug", "onActivityCreated: finished");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            refreshMap();
        }
    }

    public String getAuthTokenFromStorage() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", null);
        Log.d("MapFragmentDebug", "getAuthTokenFromStorage: authToken = " + authToken);
        return authToken;
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

    private void focusOnInitialEvent(String eventID) {
        if (eventID != null && map != null) {
            Log.d("MapFragment", "focusOnInitialEvent: eventID = " + eventID);
            // Instantiate the server proxy
            context = getActivity();
            serverProxy = ServerProxy.getInstance(context);
            Event[] events = serverProxy.getEventsFromCache();

            if (events != null) {
                for (Event event : events) {
                    if (eventID.equals(event.getEventID())) {
                        LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 5));

                        // Create a ClusterMarker object for the event
                        ClusterMarker clusterMarker = new ClusterMarker(eventLocation, event.getEventType().toUpperCase(), event.getCity() + ", " + event.getCountry() + "(" + event.getYear() + ")", event);
                        clusterManager.addItem(clusterMarker);
                        clusterManager.cluster();
                        updateEventInfoTextView(clusterMarker);
                        break;
                    }
                }
            }
        }
    }

    private void focusOnCachedPerson(Person person) {
        if (person != null && map != null) {
            Log.d("MapFragment", "focusOnCachedPerson: personID = " + person.getPersonID());
            // Instantiate the server proxy
            context = getActivity();
            serverProxy = ServerProxy.getInstance(context);
            Event[] events = serverProxy.getEventsFromCache();

            if (events != null) {
                for (Event event : events) {
                    if (person.getPersonID().equals(event.getPersonID())) {
                        LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 5));

                        // Create a ClusterMarker object for the event
                        ClusterMarker clusterMarker = new ClusterMarker(eventLocation, event.getEventType().toUpperCase(), event.getCity() + ", " + event.getCountry() + "(" + event.getYear() + ")", event);
                        clusterManager.addItem(clusterMarker);
                        clusterManager.cluster();
                        updateEventInfoTextView(clusterMarker);
                        break;
                    }
                }
            }
        }
    }

    private boolean[] getFilterSettings() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        boolean[] filterSettings = new boolean[7];

        filterSettings[0] = sharedPreferences.getBoolean("spouseLinesEnabled", true);
        filterSettings[1] = sharedPreferences.getBoolean("familyTreeLinesEnabled", true);
        filterSettings[2] = sharedPreferences.getBoolean("lifeStoryLineEnabled", true);
        filterSettings[3] = sharedPreferences.getBoolean("filterGenderMale", true);
        filterSettings[4] = sharedPreferences.getBoolean("filterGenderFemale", true);
        filterSettings[5] = sharedPreferences.getBoolean("filterSideMother", true);
        filterSettings[6] = sharedPreferences.getBoolean("filterSideFather", true);

        return filterSettings;
    }

    private void openPersonActivity() {
        Intent intent = new Intent(getActivity(), PersonActivity.class);
        intent.putExtra("personID", currentPersonID);
        startActivity(intent);
    }

    // Method to refresh the map and filter lines
    public void refreshMap() {
        // Clear the map
        if (clusterManager != null && map != null) {
            clusterManager.clearItems();
        }

        // Add the filtered events to the map
        if (mapEventManager != null && map != null) {
            mapEventManager.execute(displayedPerson);
        }

        // Refresh the map
        if (clusterManager != null && map != null) {
            clusterManager.cluster();
        }
    }

    private Person getDisplayedPerson() {
        serverProxy = ServerProxy.getInstance(getActivity());
        if (displayedPerson == null) {
            displayedPerson = serverProxy.getLoggedInUser();
        }
        return displayedPerson;
    }

    private void setDisplayedPerson(Person person) {
        displayedPerson = person;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // Refresh the map
        if (mapEventManager != null) {
            mapEventManager.refresh(getDisplayedPerson());
        }
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