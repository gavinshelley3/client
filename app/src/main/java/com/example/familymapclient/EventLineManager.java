package com.example.familymapclient;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Model.Event;

public class EventLineManager {
    private GoogleMap map;
    private AddMarkersTask addMarkersTask;

    public EventLineManager(GoogleMap map, AddMarkersTask addMarkersTask) {
        this.map = map;
        this.addMarkersTask = addMarkersTask;
    }

    public void drawLinesBetweenSameTypeEvents(Event[] events) {
        HashMap<String, List<Event>> eventsByType = groupEventsByType(events);
        drawLinesBetweenEventsOfType(eventsByType);
    }

    private HashMap<String, List<Event>> groupEventsByType(Event[] events) {
        HashMap<String, List<Event>> eventsByType = new HashMap<>();

        for (Event event : events) {
            String eventType = event.getEventType().toLowerCase();
            List<Event> eventsList;

            if (eventsByType.containsKey(eventType)) {
                eventsList = eventsByType.get(eventType);
            } else {
                eventsList = new ArrayList<>();
                eventsByType.put(eventType, eventsList);
            }

            eventsList.add(event);
        }

        return eventsByType;
    }

    private void drawLinesBetweenEventsOfType(HashMap<String, List<Event>> eventsByType) {
        for (String eventType : eventsByType.keySet()) {
            List<Event> events = eventsByType.get(eventType);
            float lineColor = addMarkersTask.getMarkerColorForEventType(eventType); // Get the color for this event type
            int lineColorInt = floatToColor(lineColor); // Convert the float to an integer color

            if (events.size() > 1) {
                for (int i = 0; i < events.size() - 1; i++) {
                    Event event1 = events.get(i);
                    Event event2 = events.get(i + 1);

                    LatLng latLng1 = new LatLng(event1.getLatitude(), event1.getLongitude());
                    LatLng latLng2 = new LatLng(event2.getLatitude(), event2.getLongitude());

                    PolylineOptions polylineOptions = new PolylineOptions()
                            .add(latLng1)
                            .add(latLng2)
                            .width(5)
                            .color(lineColorInt); // Use the integer color for this event type

                    map.addPolyline(polylineOptions);
                }
            }
        }
    }

    private int floatToColor(float markerHue) {
        float[] hsv = new float[3];
        hsv[0] = markerHue;
        hsv[1] = 1f; // Saturation
        hsv[2] = 1f; // Value
        int argbColor = Color.HSVToColor(hsv);

        // Set the alpha value to 255 (fully opaque)
        int alpha = 255;
        argbColor = Color.argb(alpha, Color.red(argbColor), Color.green(argbColor), Color.blue(argbColor));

        return argbColor;
    }
}