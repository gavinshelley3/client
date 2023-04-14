// This class extends AsyncTask to allow adding markers to a GoogleMap in the background
package com.example.familymapclient;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddMarkersTask extends AsyncTask<Void, Void, List<MarkerOptions>> {
    // Define class variables
    private GoogleMap map;
    private JSONObject eventsData;
    private HashMap<String, Float> eventTypeColors;
    private float[] colorArray;
    private int colorIndex = 0;

    // Constructor to initialize class variables
    public AddMarkersTask(GoogleMap map, JSONObject eventsData, HashMap<String, Float> eventTypeColors, float[] colorArray) {
        this.map = map;
        this.eventsData = eventsData;
        this.eventTypeColors = eventTypeColors;
        this.colorArray = colorArray;
    }

    // doInBackground method to create a list of MarkerOptions from eventsData
    @Override
    protected List<MarkerOptions> doInBackground(Void... voids) {
        List<MarkerOptions> markerOptionsList = new ArrayList<>();

        // Check if eventsData is not null
        if (eventsData != null) {
            try {
                // Get eventsArray from eventsData
                JSONArray eventsArray = eventsData.getJSONArray("data");
                // Loop through eventsArray
                for (int i = 0; i < eventsArray.length(); i++) {
                    // Get event object from eventsArray
                    JSONObject event = eventsArray.getJSONObject(i);

                    // Retrieve event details
                    String eventType = event.optString("eventType");
                    double latitude = event.optDouble("latitude");
                    double longitude = event.optDouble("longitude");
                    String eventId = event.optString("eventID");

                    // Get color for the event type
                    float markerColor = getMarkerColor(eventType);

                    // Add a marker on the map for this event
                    LatLng eventLocation = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions().position(eventLocation).title(eventId).icon(BitmapDescriptorFactory.defaultMarker(markerColor));
                    markerOptionsList.add(markerOptions);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return markerOptionsList;
    }

    // Method to get the color for a marker based on the eventType
    private float getMarkerColor(String eventType) {
        // Check if eventTypeColors contains eventType
        if (!eventTypeColors.containsKey(eventType)) {
            // Add eventType to eventTypeColors with the next color in colorArray
            eventTypeColors.put(eventType, colorArray[colorIndex]);
            // Update colorIndex
            colorIndex = (colorIndex + 1) % colorArray.length;
        }
        // Return color for eventType
        return eventTypeColors.get(eventType);
    }

    // onPostExecute method to add the MarkerOptions to the map
    @Override
    protected void onPostExecute(List<MarkerOptions> markerOptionsList) {
        super.onPostExecute(markerOptionsList);
        // Loop through markerOptionsList and add each marker to the map
        for (MarkerOptions markerOptions : markerOptionsList) {
            map.addMarker(markerOptions);
        }
    }
}