package com.example.familymapclient;

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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Arrays;
import java.util.HashMap;

import Model.Event;
import Model.Person;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap map;
    private HashMap<String, Float> eventTypeColors = new HashMap<>();
    private float[] colorArray = {BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_VIOLET, BitmapDescriptorFactory.HUE_YELLOW};
    private ClusterManager<ClusterMarker> clusterManager;
    private String currentPersonID;
    private ServerProxy serverProxy;
    private Context context;
    private Event currentEvent;
    private Person currentPerson;
    private ServerProxy.cacheEventListener cacheListener;

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

        // Add the following two lines
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

        // Cache data and setup event markers
        String authToken = getAuthTokenFromStorage();
        // Instantiate the server proxy
        context = getActivity();
        serverProxy = ServerProxy.getInstance(context);

        // Initialize the cacheListener for events
        ServerProxy.cacheEventListener eventListener = new ServerProxy.cacheEventListener() {
            @Override
            public void onCacheEventSuccess(String message) {
                Log.d("ServerProxy", "onResponse: " + message);
            }

            @Override
            public void onCacheEventError(String error) {
                Log.d("ServerProxy", "onResponse: " + error);
            }

            @Override
            public void onCacheEventCompleted(Event[] events) {
                // Update the map with the cached data
                Log.d("MapFragment", "onCacheEventCompleted: " + Arrays.toString(events));
                setupEventMarkers(events);
            }
        };

        // Initialize the cacheListener for persons
        ServerProxy.cachePersonListener personListener = new ServerProxy.cachePersonListener() {
            @Override
            public void onCachePersonSuccess(Person person) {
                // Update the map with the cached data
                focusOnCachedPerson(person);
            }

            @Override
            public void onCachePersonError(String error) {
                Log.d("MapFragment", "onCachePersonError: " + error);
            }

            @Override
            public void onCachePersonCompleted(Person[] persons) {
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
        };

        // Create and execute the CacheDataTask
        CacheDataTask cacheDataTask = new CacheDataTask(serverProxy, eventListener, personListener);
        cacheDataTask.execute(authToken);

        // If personID is passed as an argument, focus on the cached person's birth location
        if (getArguments() != null) {
            String personID = getArguments().getString("personID");
            if (personID != null) {
                Person person = serverProxy.getPersonFromCache(personID);
                focusOnCachedPerson(person);
            }
        }
    }

    private void setupEventMarkers(Event[] events) {
        AddMarkersTask addMarkersTask = new AddMarkersTask(map, events, eventTypeColors, colorArray, clusterManager);
        addMarkersTask.execute();
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
            String firstName = person.getFirstName();
            String lastName = person.getLastName();
            String gender = person.getGender();
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
        Person person = serverProxy.getPersonFromCache(authToken);

        Log.d("MapFragmentDebug", "onActivityCreated: finished");
    }

    private String getAuthTokenFromStorage() {
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
            Event event = serverProxy.getEventFromCache(eventID);

            if (event != null) {
                LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 10));

                for (ClusterMarker item : clusterManager.getAlgorithm().getItems()) {
                    if (eventID.equals(item.getEvent().getEventID())) {
                        updateEventInfoTextView(item);
                        break;
                    }
                }
            }
        }
    }

    private void focusOnCachedPerson(Person person) {
        if (person != null && map != null) {
            String personID = person.getPersonID();
            // Instantiate the server proxy
            context = getActivity();
            serverProxy = ServerProxy.getInstance(context);
            Event[] events = serverProxy.getEventsFromCache();

            if (events != null) {
                for (Event event : events) {
                    if (personID.equals(event.getPersonID())) {
                        LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 5));

                        for (ClusterMarker item : clusterManager.getAlgorithm().getItems()) {
                            if (event.getEventID().equals(item.getEvent().getEventID())) {
                                updateEventInfoTextView(item);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private void openPersonActivity() {
        Intent intent = new Intent(getActivity(), PersonActivity.class);
        intent.putExtra("personID", currentPersonID);
        startActivity(intent);
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