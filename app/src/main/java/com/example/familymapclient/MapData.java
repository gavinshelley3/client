package com.example.familymapclient;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.HashMap;

import Model.Event;
import Model.Person;

public class MapData {
    private static MapData instance;
    private Context context;
    private ServerProxy serverProxy;
    private Event[] events;
    private Person[] persons;
    private HashMap<String, Float> eventTypeColors = new HashMap<>();
    private float[] colorArray = {BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_VIOLET, BitmapDescriptorFactory.HUE_YELLOW};
    private Person[] motherSideAncestors;
    private Person[] fatherSideAncestors;

    private MapData() {}

    public static MapData getInstance() {
        if (instance == null) {
            instance = new MapData();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        serverProxy = ServerProxy.getInstance(context);
    }

    public Event[] getEvents() {
        if (events == null) {
            events = serverProxy.getEventsFromCache();
        }
        return events;
    }

    public void setEvents(Event[] events) {
        this.events = events;
    }

    public Person[] getPersons() {
        if (persons == null) {
            persons = serverProxy.getPersonsFromCache();
        }
        return persons;
    }

    public void setPersons(Person[] persons) {
        this.persons = persons;
    }

    public Event getEvent(String eventID) {
        if (events == null) {
            events = serverProxy.getEventsFromCache();
        }
        for (Event event : events) {
            if (eventID.equals(event.getEventID())) {
                return event;
            }
        }
        return null;
    }

    public Person getPerson(String personID) {
        if (persons == null) {
            persons = serverProxy.getPersonsFromCache();
        }
        for (Person person : persons) {
            if (personID.equals(person.getPersonID())) {
                return person;
            }
        }
        return null;
    }

    public Person getLoggedInPerson() {
        if (persons == null) {
            persons = serverProxy.getPersonsFromCache();
        }
        for (Person person : persons) {
            if (person.getPersonID().equals(serverProxy.getLoggedInUser().getPersonID())) {
                return person;
            }
        }
        return null;
    }

    public Person[] getMotherSideAncestors() {
        if (motherSideAncestors == null) {
            motherSideAncestors = serverProxy.getMotherAncestorPersonsFromCache();
        }
        return motherSideAncestors;
    }

    public Person[] getFatherSideAncestors() {
        if (fatherSideAncestors == null) {
            fatherSideAncestors = serverProxy.getFatherAncestorPersonsFromCache();
        }
        return fatherSideAncestors;
    }

    public HashMap<String, Float> getEventTypeColors() {
        return eventTypeColors;
    }

    public void setEventTypeColors(HashMap<String, Float> eventTypeColors) {
        this.eventTypeColors = eventTypeColors;
    }

    public float[] getColorArray() {
        return colorArray;
    }

    public void setColorArray(float[] colorArray) {
        this.colorArray = colorArray;
    }
}