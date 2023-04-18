package com.example.familymapclient;

import java.io.Serializable;

import Model.Event;
import Model.Person;

public class MapData implements Serializable {
    private String currentPersonID;
    private Event currentEvent;
    private Person currentPerson;

    public MapData(String currentPersonID, Event currentEvent, Person currentPerson) {
        this.currentPersonID = currentPersonID;
        this.currentEvent = currentEvent;
        this.currentPerson = currentPerson;
    }

    public String getCurrentPersonID() {
        return currentPersonID;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public Person getCurrentPerson() {
        return currentPerson;
    }
}