package com.example.familymapclient;

import Model.Event;

public class MapHelper {

    public Event findEventById(Event[] events, String eventId) {
        for (Event event : events) {
            if (eventId.equals(event.getEventID())) {
                return event;
            }
        }
        return null;
    }

    public Event findPersonBirthEvent(Event[] events, String personId) {
        for (Event event : events) {
            if (personId.equals(event.getPersonID())) {
                return event;
            }
        }
        return null;
    }
}