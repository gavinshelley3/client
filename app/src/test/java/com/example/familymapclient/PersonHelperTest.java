package com.example.familymapclient;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import Model.Event;
import Model.Person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class PersonHelperTest {
    private PersonHelper personHelper;
    private ServerProxy serverProxy;
    private Context context;

    @Before
    public void setUp() {
        // Set up a mocked context for the ServerProxy instance
        Context mockContext = mock(Context.class);
        serverProxy = ServerProxy.getInstance(mockContext);
    }


    @Test
    public void testGetPerson() {
        String personID = "testPersonID";
        Person person = personHelper.getPerson(personID);
        assertNotNull(person);
        assertEquals(personID, person.getPersonID());
    }

    @Test
    public void testGetFamilyMembers() {
        String personID = "testPersonID";
        Person person = personHelper.getPerson(personID);
        Person[] familyMembers = personHelper.getFamilyMembers(person);
        assertNotNull(familyMembers);
    }

    @Test
    public void testGetLifeEvents() {
        String personID = "testPersonID";
        Person person = personHelper.getPerson(personID);
        Event[] lifeEvents = personHelper.getLifeEvents(person);
        assertNotNull(lifeEvents);
    }
}