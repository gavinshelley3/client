package com.example.familymapclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


import Model.Event;
import Model.Person;

public class MapDataTest {

    private MapData mapData;
    private ServerProxy serverProxy;
    private Context context;

    @Before
    public void setUp() {
        // Set up a mocked context for the ServerProxy instance
        Context mockContext = mock(Context.class);
        serverProxy = ServerProxy.getInstance(mockContext);
        mapData = MapData.getInstance();
        mapData.init(context);

        // Set up persons and logged-in user in ServerProxy
        Person[] persons = new Person[]{new Person("firstName", "lastName", "m", "firstName_lastName", "fatherID", "motherID", "spouseID", "firstName"), new Person("firstName2", "lastName2", "f", "firstName2_lastName2", "fatherID2", "motherID2", "spouseID2", "firstName2")};
        Event[] events = new Event[]{new Event("birth", "firstName_lastName", "city", "country", 0, 0, 2000, "firstName_birth", "firstName"), new Event("birth", "firstName2_lastName2", "city", "country", 0, 0, 2000, "firstName2_birth", "firstName2")};
    }

    @After
    public void tearDown() {
        mapData.setEvents(null);
        mapData.setPersons(null);
    }

    @Test
    public void testGetSetEvents() {
        assertNull(mapData.getEvents());
        Event[] events = new Event[]{new Event(), new Event()};
        mapData.setEvents(events);
        assertEquals(events, mapData.getEvents());
    }

    @Test
    public void testGetSetPersons() {
        assertNull(mapData.getPersons());
        Person[] persons = new Person[]{new Person(), new Person()};
        mapData.setPersons(persons);
        assertEquals(persons, mapData.getPersons());
    }

    @Test
    public void testGetEvent() {
        assertNull(mapData.getEvent("firstName_birth"));
        Event[] events = new Event[]{new Event("birth", "firstName_lastName", "city", "country", 0, 0, 2000, "firstName_birth", "firstName"), new Event("birth", "firstName2_lastName2", "city", "country", 0, 0, 2000, "firstName2_birth", "firstName2")};
        mapData.setEvents(events);
        assertEquals(events[0], mapData.getEvent("firstName_birth"));
        assertNull(mapData.getEvent("non_existent_event_id"));
    }

    @Test
    public void testGetPerson() {
        assertNull(mapData.getPerson("firstName_lastName"));
        Person[] persons = new Person[]{new Person("firstName", "lastName", "m", "firstName_lastName", "fatherID", "motherID", "spouseID", "firstName"), new Person("firstName2", "lastName2", "f", "firstName2_lastName2", "fatherID2", "motherID2", "spouseID2", "firstName2")};
        mapData.setPersons(persons);
        assertEquals(persons[0], mapData.getPerson("firstName_lastName"));
        assertNull(mapData.getPerson("non_existent_person_id"));
    }

    @Test
    public void testGetMotherSideAncestors() {
        // Set up mother side ancestors in ServerProxy
        // serverProxy.setMotherAncestorPersonsFromCache(new Person[]{new Person("mother_id_1"), new Person("mother_id_2")});
        Person[] motherSideAncestors = mapData.getMotherSideAncestors();
        assertEquals("mother_id_1", motherSideAncestors[0].getPersonID());
    }

    @Test
    public void testGetFatherSideAncestors() {
        // Set up father side ancestors in ServerProxy
        // serverProxy.setFatherAncestorPersonsFromCache(new Person[]{new Person("father_id_1"), new Person("father_id_2")});
        Person[] fatherSideAncestors = mapData.getFatherSideAncestors();
        assertEquals("father_id_1", fatherSideAncestors[0].getPersonID());
    }

    @Test
    public void findEventById() {
        Event[] events = serverProxy.getEventsFromCache();
        assertNotNull(events);

        Event event = new MapHelper().findEventById(events, "event_id");
        assertNotNull(event);
        assertEquals("event_id", event.getEventID());
    }

    @Test
    public void findEventByIdNotFound() {
        Event[] events = serverProxy.getEventsFromCache();
        assertNotNull(events);

        Event event = new MapHelper().findEventById(events, "non_existent_event_id");
        assertNull(event);
    }

    @Test
    public void findPersonBirthEvent() {
        Event[] events = serverProxy.getEventsFromCache();
        assertNotNull(events);

        Event event = new MapHelper().findPersonBirthEvent(events, "person_id");
        assertNotNull(event);
        assertEquals("person_id", event.getPersonID());
    }


    public void findPersonBirthEventNotFound() {
        Event[] events = serverProxy.getEventsFromCache();
        assertNotNull(events);

        Event event = new MapHelper().findPersonBirthEvent(events, "non_existent_person_id");
        assertNull(event);
    }
}