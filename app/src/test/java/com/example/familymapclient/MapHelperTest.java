package com.example.familymapclient;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import Model.Event;

public class MapHelperTest {

    @Test
    public void findEventByIdTest() {
        MapHelper mapHelper = new MapHelper();
        Event[] events = createSampleEvents();
        Event event = mapHelper.findEventById(events, "sheila_parker_birth");
        assertEquals("sheila_parker_birth", event.getEventID());
    }

    @Test
    public void findEventByIdNotFoundTest() {
        MapHelper mapHelper = new MapHelper();
        Event[] events = createSampleEvents();
        Event event = mapHelper.findEventById(events, "nonexistentID");
        assertNull(event);
    }

    @Test
    public void findPersonBirthEventTest() {
        MapHelper mapHelper = new MapHelper();
        Event[] events = createSampleEvents();
        Event event = mapHelper.findPersonBirthEvent(events, "sheila_parker");
        assertEquals("sheila_parker", event.getPersonID());
    }

    @Test
    public void findPersonBirthEventNotFoundTest() {
        MapHelper mapHelper = new MapHelper();
        Event[] events = createSampleEvents();
        Event event = mapHelper.findPersonBirthEvent(events, "nonexistentID");
        assertNull(event);
    }

    private Event[] createSampleEvents() {
        Event event1 = new Event("eventID", "personID", "city", "country", 0, 0, 1, "eventType", "associatedUsername");
        Event event2 = new Event("eventID2", "personID2", "city", "country", 0, 0, 1, "eventType", "associatedUsername");
        Event event3 = new Event("eventID3", "personID3", "city", "country", 0, 0, 1, "eventType", "associatedUsername");
        Event event4 = new Event("birth", "sheila_parker", "city", "country", 0, 0, 1, "sheila_parker_birth", "sheila");

        return new Event[]{event1, event2, event3, event4};
    }
}