package com.example.familymapclient;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private Person loggedInPerson;
    private List<Person> motherSideAncestors;
    private List<Person> fatherSideAncestors;
    private Context context;
    private Person displayedPerson;

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
        this.loggedInPerson = serverProxy.getLoggedInUser();
        this.context = context;
    }

    public void execute(Person displayedPersonInput) {
        executor.execute(() -> {
            serverProxy = ServerProxy.getInstance(context);
            filterSettings = getFilterSettingsFromPreferences();
            loggedInPerson = serverProxy.getLoggedInUser();
            Log.d("MapEventManagerDebug", "execute: loggedInPerson is " + loggedInPerson);
            motherSideAncestors = new ArrayList<>();
            fatherSideAncestors = new ArrayList<>();
            findMotherSideAncestors(loggedInPerson, motherSideAncestors);
            findFatherSideAncestors(loggedInPerson, fatherSideAncestors);
            displayedPerson = displayedPersonInput;
            if (events != null) {
                Log.d("MapEventManagerDebug", "execute: adding markers and lines for events - " + Arrays.toString(events));
                // Clear the map of all markers and lines
                mainThreadHandler.post(() -> {
                    map.clear();
                    clusterManager.clearItems();
                    clusterManager.cluster();
                });
                addMarkers();
                if (displayedPerson != null) {
                    Log.d("MapEventManagerDebug", "execute: displayedPerson is not null");
                    drawSpouseLines(displayedPerson);
                    drawFamilyTreeLines(displayedPerson, 5);
                    drawLifeStoryLines(displayedPerson);
                }
                else {
                    Log.d("MapEventManagerDebug", "execute: displayedPerson is null");
                }
                Log.d("MapEventManagerDebug", "execute: finished adding markers and lines for events - " + Arrays.toString(events));
            }
        });
    }

    private void addMarkers() {
        for (Event event : events) {
            Person person = serverProxy.getPersonFromCache(event.getPersonID());
            if (!shouldShowEventBasedOnFilters(person)) {
                Log.d("MapEventManagerDebug", "addMarkers: should not show event - " + event);
                continue;
            }
            Log.d("MapEventManagerDebug", "addMarkers: should show event - " + event);

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

    private void findMotherSideAncestors(Person person, List<Person> ancestorsList) {
        if (person == null || person.getMotherID() == null) {
            return;
        }
        Person mother = serverProxy.getPersonFromCache(person.getMotherID());
        if (mother != null) {
            ancestorsList.add(mother);
            findMotherSideAncestors(mother, ancestorsList);
            findFatherSideAncestors(mother, ancestorsList);
        }
    }

    private void findFatherSideAncestors(Person person, List<Person> ancestorsList) {
        if (person == null || person.getFatherID() == null) {
            return;
        }
        Person father = serverProxy.getPersonFromCache(person.getFatherID());
        if (father != null) {
            ancestorsList.add(father);
            findMotherSideAncestors(father, ancestorsList);
            findFatherSideAncestors(father, ancestorsList);
        }
    }

    private boolean shouldShowEventBasedOnFilters(Person person) {
        filterSettings = getFilterSettingsFromPreferences();
        loggedInPerson = serverProxy.getLoggedInUser();
        if (person == null) {
            Log.d("Filter", "Event not shown: person is null");
            return false;
        }

        if (loggedInPerson == null) {
            Log.d("Filter", "Event not shown: loggedInPerson is null");
            return false;
        }

        if (person.getPersonID().equals(loggedInPerson.getPersonID())) {
            return true;
        }

        boolean maleFilter = person.getGender().equals("m") && filterSettings[3];
        boolean femaleFilter = person.getGender().equals("f") && filterSettings[4];

        if (!maleFilter && !femaleFilter) {
            Log.d("Filter", "Event not shown: gender filter not matched");
            return false;
        }

        boolean mothersSideAncestor = isAncestorOnMotherSide(person.getPersonID());
        boolean fathersSideAncestor = isAncestorOnFatherSide(person.getPersonID());

        if (!mothersSideAncestor && !fathersSideAncestor && !person.getPersonID().equals(loggedInPerson.getPersonID())) {
            Log.d("Filter", "Event not shown: person is not an ancestor on either side");
            return false;
        }

        if (mothersSideAncestor && !filterSettings[5]) {
            Log.d("Filter", "Event not shown: mothers side ancestor filter not enabled");
            return false;
        }

        if (fathersSideAncestor && !filterSettings[6]) {
            Log.d("Filter", "Event not shown: fathers side ancestor filter not enabled");
            return false;
        }

        return true;
    }

    private boolean isAncestorOnMotherSide(String personID) {
        return motherSideAncestors.stream().anyMatch(person -> person.getPersonID().equals(personID));
    }

    private boolean isAncestorOnFatherSide(String personID) {
        return fatherSideAncestors.stream().anyMatch(person -> person.getPersonID().equals(personID));
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

    private int getLineColorForRelationship(String relationshipType) {
        float lineColor;
        switch (relationshipType.toLowerCase()) {
            case "spouse":
                lineColor = getMarkerColorForEventType("spouse");
                break;
            case "mother":
                lineColor = getMarkerColorForEventType("mother");
                break;
            case "father":
                lineColor = getMarkerColorForEventType("father");
                break;
            default:
                lineColor = getMarkerColorForEventType(relationshipType);
                break;
        }
        return floatToColor(lineColor);
    }

    private void drawSpouseLines(Person displayedPerson) {
        if (!filterSettings[0]) {
            return;
        }

        Event displayedPersonBirthEvent = getBirthEvent(displayedPerson.getPersonID());
        if (displayedPersonBirthEvent == null) {
            return;
        }

        String spouseID = displayedPerson.getSpouseID();
        if (spouseID == null) {
            return;
        }

        Person spouse = serverProxy.getPersonFromCache(spouseID);
        if (spouse == null) {
            return;
        }

        Event spouseBirthEvent = getBirthEvent(spouse.getPersonID());
        if (spouseBirthEvent == null) {
            return;
        }

        LatLng displayedPersonLatLng = new LatLng(displayedPersonBirthEvent.getLatitude(), displayedPersonBirthEvent.getLongitude());
        LatLng spouseLatLng = new LatLng(spouseBirthEvent.getLatitude(), spouseBirthEvent.getLongitude());

        int lineColorInt = getLineColorForRelationship("spouse");

        PolylineOptions polylineOptions = new PolylineOptions()
                .add(displayedPersonLatLng)
                .add(spouseLatLng)
                .width(5)
                .color(lineColorInt);

        mainThreadHandler.post(() -> {
            map.addPolyline(polylineOptions);
        });
    }

    private void drawFamilyTreeLines(Person displayedPerson, int lineWidth) {
        if (!filterSettings[1]) {
            return;
        }

        Event displayedPersonBirthEvent = getBirthEvent(displayedPerson.getPersonID());
        if (displayedPersonBirthEvent == null) {
            return;
        }

        LatLng displayedPersonLatLng = new LatLng(displayedPersonBirthEvent.getLatitude(), displayedPersonBirthEvent.getLongitude());

        String fatherID = displayedPerson.getFatherID();
        if (fatherID != null) {
            Event fatherBirthEvent = getBirthEvent(fatherID);
            if (fatherBirthEvent == null) {
                fatherBirthEvent = getEarliestEvent(fatherID);
            }
            if (fatherBirthEvent != null) {
                LatLng fatherLatLng = new LatLng(fatherBirthEvent.getLatitude(), fatherBirthEvent.getLongitude());

                int lineColorInt = getLineColorForRelationship("father");

                PolylineOptions polylineOptions = new PolylineOptions()
                        .add(displayedPersonLatLng)
                        .add(fatherLatLng)
                        .width(lineWidth)
                        .color(lineColorInt);

                mainThreadHandler.post(() -> {
                    map.addPolyline(polylineOptions);
                });

                Person father = serverProxy.getPersonFromCache(fatherID);
                if (father != null) {
                    drawFamilyTreeLines(father, lineWidth / 2);
                }
            }
        }

        String motherID = displayedPerson.getMotherID();
        if (motherID != null) {
            Event motherBirthEvent = getBirthEvent(motherID);
            if (motherBirthEvent == null) {
                motherBirthEvent = getEarliestEvent(motherID);
            }
            if (motherBirthEvent != null) {
                LatLng motherLatLng = new LatLng(motherBirthEvent.getLatitude(), motherBirthEvent.getLongitude());

                int lineColorInt = getLineColorForRelationship("mother");

                PolylineOptions polylineOptions = new PolylineOptions()
                        .add(displayedPersonLatLng)
                        .add(motherLatLng)
                        .width(lineWidth)
                        .color(lineColorInt);

                mainThreadHandler.post(() -> {
                    map.addPolyline(polylineOptions);
                });

                Person mother = serverProxy.getPersonFromCache(motherID);
                if (mother != null) {
                    drawFamilyTreeLines(mother, lineWidth / 2);
                }
            }
        }
    }

    private Event getEarliestEvent(String personID) {
        return Arrays.stream(events)
                .filter(event -> event.getPersonID().equals(personID))
                .min(Comparator.comparing(Event::getYear))
                .orElse(null);
    }

    private void drawLifeStoryLines(Person displayedPerson) {
        if (!filterSettings[2]) {
            return;
        }

        List<Event> personEvents = Arrays.stream(events)
                .filter(event -> event.getPersonID().equals(displayedPerson.getPersonID()))
                .sorted(Comparator.comparing(Event::getYear))
                .collect(Collectors.toList());

        for (int i = 0; i < personEvents.size() - 1; i++) {
            Event event1 = personEvents.get(i);
            Event event2 = personEvents.get(i + 1);

            LatLng latLng1 = new LatLng(event1.getLatitude(), event1.getLongitude());
            LatLng latLng2 = new LatLng(event2.getLatitude(), event2.getLongitude());

            int lineColorInt = getLineColorForRelationship(event1.getEventType());

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

    private Event getBirthEvent(String personID) {
        return Arrays.stream(events)
                .filter(event -> event.getPersonID().equals(personID) && event.getEventType().equalsIgnoreCase("birth"))
                .findFirst()
                .orElse(null);
    }

    public void refresh(Person displayedPerson) {
        filterSettings = getFilterSettingsFromPreferences();
        execute(displayedPerson);
    }

    private boolean[] getFilterSettingsFromPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
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