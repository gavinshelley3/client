package com.example.familymapclient;

import android.content.Context;

import java.util.List;

import Model.Event;
import Model.Person;

public class PersonHelper {
    private MapData mapData;

    public PersonHelper(Context context) {
        mapData = MapData.getInstance();
        mapData.init(context);
    }

    public Person getPerson(String personID) {
        return mapData.getPerson(personID);
    }

    public Person[] getFamilyMembers(Person person) {
        Person[] familyMembers = mapData.getPersons();
        return familyMembers;
    }

    public Event[] getLifeEvents(Person person) {
        Event[] lifeEvents = mapData.getEvents();
        return lifeEvents;
    }
}