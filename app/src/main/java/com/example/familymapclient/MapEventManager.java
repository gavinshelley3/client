package com.example.familymapclient;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import Model.Event;
import Model.Person;

public class MapEventManager {
    private GoogleMap map;
    private Event[] events;
    private HashMap<String, Float> eventTypeColors;
    private float[] colorArray;
    private int colorIndex = 0;
    private ClusterManager<ClusterMarker> clusterManager;
    private Executor executor;
    private Handler mainThreadHandler;
    private boolean[] filterSettings;
    private ServerProxy serverProxy;

    public MapEventManager(GoogleMap map, Event[] events, HashMap<String, Float> eventTypeColors, float[] colorArray, ClusterManager<ClusterMarker> clusterManager, boolean[] filterSettings, Context context) {
        this.map = map;
        this.events = events;
        this.eventTypeColors = eventTypeColors;
        this.colorArray = colorArray;
        this.clusterManager = clusterManager;
        this.filterSettings = filterSettings;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
        this.serverProxy = ServerProxy.getInstance(context);
    }

    public void execute(Person selectedPerson) {
        executor.execute(() -> {
            if (events != null) {
                Log.d("MapEventManagerDebug", "execute: adding markers and lines for events - " + Arrays.toString(events));
                addMarkers();
                drawLinesBetweenSameTypeEvents(selectedPerson);
                Log.d("MapEventManagerDebug", "execute: finished adding markers and lines for events - " + Arrays.toString(events));
            }
        });
    }

    private void addMarkers() {
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
    }

    private float getMarkerColor(String eventType) {
        return eventTypeColors.computeIfAbsent(eventType, k -> {
            float color = colorArray[colorIndex];
            colorIndex = (colorIndex + 1) % colorArray.length;
            return color;
        });
    }

    public float getMarkerColorForEventType(String eventType) {
        return getMarkerColor(eventType);
    }

    public void setMarkerColor(String eventType, float color) {
        eventTypeColors.put(eventType, color);
    }

    private void drawLinesBetweenSameTypeEvents(Person selectedPerson) {
        HashMap<String, List<Event>> eventsByType = groupEventsByType(events);
        drawLinesBetweenEventsOfType(eventsByType, selectedPerson);
    }

    private HashMap<String, List<Event>> groupEventsByType(Event[] events) {
        HashMap<String, List<Event>> eventsByType = new HashMap<>();

        for (Event event : events) {
            String eventType = event.getEventType().toLowerCase();
            eventsByType.putIfAbsent(eventType, new ArrayList<>());
            eventsByType.get(eventType).add(event);
        }

        return eventsByType;
    }

    private void drawLinesBetweenEventsOfType(HashMap<String, List<Event>> eventsByType, Person selectedPerson) {
        eventsByType.forEach((eventType, eventsList) -> {
            float lineColor = getMarkerColorForEventType(eventType);
            int lineColorInt = floatToColor(lineColor);

            if (eventsList.size() > 1) {
                for (int i = 0; i < eventsList.size() - 1; i++) {
                    Event event1 = eventsList.get(i);
                    Event event2 = eventsList.get(i + 1);

                    if (!shouldDrawLine(event1, selectedPerson) || !shouldDrawLine(event2, selectedPerson)) {
                        continue;
                    }

                    LatLng latLng1 = new LatLng(event1.getLatitude(), event1.getLongitude());
                    LatLng latLng2 = new LatLng(event2.getLatitude(), event2.getLongitude());

                    PolylineOptions polylineOptions = new PolylineOptions()
                            .add(latLng1)
                            .add(latLng2)
                            .width(5)
                            .color(lineColorInt);

                    mainThreadHandler.post(() -> {
                        map.addPolyline(polylineOptions);
                    });
                }
            }
        });
    }

    private boolean shouldDrawLine(Event event, Person selectedPerson) {
        Person person = serverProxy.getPersonFromCache(event.getPersonID());
        if (person == null) {
            return false;
        }

        boolean spouseLinesEnabled = filterSettings[0];
        boolean familyTreeLinesEnabled = filterSettings[1];
        boolean lifeStoryLineEnabled = filterSettings[2];
        boolean filterGenderMaleEnabled = filterSettings[3];
        boolean filterGenderFemaleEnabled = filterSettings[4];
        boolean filterSideMotherEnabled = filterSettings[5];
        boolean filterSideFatherEnabled = filterSettings[6];

        if (person.getGender().equalsIgnoreCase("m") && !filterGenderMaleEnabled ||
                person.getGender().equalsIgnoreCase("f") && !filterGenderFemaleEnabled) {
            return false;
        }

        if (!filterSideMotherEnabled || !filterSideFatherEnabled) {
            Person rootPerson = serverProxy.getLoggedInUser();
            if (rootPerson != null) {
                boolean isMotherSideEvent = isMotherSideEvent(event, rootPerson);
                boolean isFatherSideEvent = isFatherSideEvent(event, rootPerson);

                if (isMotherSideEvent && !filterSideMotherEnabled ||
                        isFatherSideEvent && !filterSideFatherEnabled) {
                    return false;
                }
            }
        }

        return !(!spouseLinesEnabled && isSpouseEvent(event, selectedPerson)) &&
                !(!familyTreeLinesEnabled && isFamilyTreeEvent(event, selectedPerson)) &&
                !(!lifeStoryLineEnabled && isLifeStoryEvent(event, selectedPerson));
    }

    private boolean isSpouseEvent(Event event, Person rootPerson) {
        return event.getPersonID().equals(rootPerson.getSpouseID());
    }

    private boolean isFamilyTreeEvent(Event event, Person selectedPerson) {
        return isMotherSideEvent(event, selectedPerson) || isFatherSideEvent(event, selectedPerson);
    }

    private boolean isLifeStoryEvent(Event event, Person selectedPerson) {
        return event.getPersonID().equals(selectedPerson.getPersonID());
    }

    private boolean isMotherSideEvent(Event event, Person rootPerson) {
        String personID = event.getPersonID();
        String motherID = rootPerson.getMotherID();

        while (motherID != null) {
            if (personID.equals(motherID)) {
                return true;
            }
            Person mother = serverProxy.getPersonFromCache(motherID);
            motherID = mother != null ? mother.getMotherID() : null;
        }

        return false;
    }

    private boolean isFatherSideEvent(Event event, Person rootPerson) {
        String personID = event.getPersonID();
        String fatherID = rootPerson.getFatherID();

        while (fatherID != null) {
            if (personID.equals(fatherID)) {
                return true;
            }
            Person father = serverProxy.getPersonFromCache(fatherID);
            fatherID = father != null ? father.getFatherID() : null;
        }

        return false;
    }

    private int floatToColor(float markerHue) {
        float[] hsv = new float[3];
        hsv[0] = markerHue;
        hsv[1] = 1f;
        hsv[2] = 1f;
        int argbColor = Color.HSVToColor(hsv);

        int alpha = 255;
        return Color.argb(alpha, Color.red(argbColor), Color.green(argbColor), Color.blue(argbColor));
    }
}