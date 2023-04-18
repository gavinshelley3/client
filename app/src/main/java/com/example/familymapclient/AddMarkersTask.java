// This class extends Executor to allow adding markers to a GoogleMap in the background
package com.example.familymapclient;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import Model.Event;

public class AddMarkersTask {
    private GoogleMap map;
    private Event[] events;
    private HashMap<String, Float> eventTypeColors;
    private float[] colorArray;
    private int colorIndex = 0;
    private ClusterManager<ClusterMarker> clusterManager;
    private Executor executor;
    private Handler mainThreadHandler;

    public AddMarkersTask(GoogleMap map, Event[] events, HashMap<String, Float> eventTypeColors, float[] colorArray, ClusterManager<ClusterMarker> clusterManager) {
        this.map = map;
        this.events = events;
        this.eventTypeColors = eventTypeColors;
        this.colorArray = colorArray;
        this.clusterManager = clusterManager;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    }

    public void execute() {
        executor.execute(() -> {
            if (events != null) {
                // Add a log statement here
                Log.d("AddMarkersTaskDebug", "execute: adding markers for events - " + Arrays.toString(events));
                for (Event event : events) {
                    float markerColor = getMarkerColor(event.getEventType().toUpperCase());

                    LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
                    ClusterMarker clusterMarker = new ClusterMarker(eventLocation, event.getEventID(), event.getEventType(), event);
                    clusterMarker.setMarkerColor(markerColor);

                    mainThreadHandler.post(() -> {
                        clusterManager.addItem(clusterMarker);
                        clusterManager.cluster();
                    });
                }
                // Add a log statement here
                Log.d("AddMarkersTaskDebug", "execute: finished adding markers for events - " + Arrays.toString(events));
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

    public float getMarkerColorForEventType(String eventType) {
        return getMarkerColor(eventType);
    }

    public void setMarkerColor(String eventType, float color) {
        eventTypeColors.put(eventType, color);
    }
}