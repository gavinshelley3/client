// This class extends Executor to allow adding markers to a GoogleMap in the background
package com.example.familymapclient;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import Model.Event;

public class AddMarkersTask {
    private GoogleMap map;
    private JSONObject eventsData;
    private HashMap<String, Float> eventTypeColors;
    private float[] colorArray;
    private int colorIndex = 0;
    private ClusterManager<ClusterMarker> clusterManager;
    private Executor executor;
    private Handler mainThreadHandler;

    public AddMarkersTask(GoogleMap map, JSONObject eventsData, HashMap<String, Float> eventTypeColors, float[] colorArray, ClusterManager<ClusterMarker> clusterManager) {
        this.map = map;
        this.eventsData = eventsData;
        this.eventTypeColors = eventTypeColors;
        this.colorArray = colorArray;
        this.clusterManager = clusterManager;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    }

    public void execute() {
        executor.execute(() -> {
            if (eventsData != null) {
                try {
                    JSONArray eventsArray = eventsData.getJSONArray("data");
                    for (int i = 0; i < eventsArray.length(); i++) {
                        JSONObject eventJson = eventsArray.getJSONObject(i);

                        Event event = new Event();
                        event.setEventType(eventJson.optString("eventType"));
                        event.setPersonID(eventJson.optString("personID"));
                        event.setCity(eventJson.optString("city"));
                        event.setCountry(eventJson.optString("country"));
                        event.setLatitude((float) eventJson.optDouble("latitude"));
                        event.setLongitude((float) eventJson.optDouble("longitude"));
                        event.setYear(eventJson.optInt("year"));
                        event.setEventID(eventJson.optString("eventID"));
                        event.setAssociatedUsername(eventJson.optString("associatedUsername"));

                        float markerColor = getMarkerColor(event.getEventType());

                        LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
                        ClusterMarker clusterMarker = new ClusterMarker(eventLocation, event.getEventID(), event.getEventType(), event); // Update this line
                        clusterMarker.setMarkerColor(markerColor);

                        mainThreadHandler.post(() -> {
                            clusterManager.addItem(clusterMarker);
                            clusterManager.cluster();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private float getMarkerColor(String eventType) {
        if (!eventTypeColors.containsKey(eventType)) {
            eventTypeColors.put(eventType, colorArray[colorIndex]);
            colorIndex = (colorIndex + 1) % colorArray.length;
        }
        return eventTypeColors.get(eventType);
    }

    public void setMarkerColor(String eventType, float color) {
        eventTypeColors.put(eventType, color);
    }
}