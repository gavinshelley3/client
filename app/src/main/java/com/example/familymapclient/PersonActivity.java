package com.example.familymapclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.Event;
import Model.Person;

public class PersonActivity extends AppCompatActivity {
    private Context context;
    private ServerProxy serverProxy;
    private ExpandableListView expandableListView;
    private CustomExpandableListAdapter expandableListAdapter;
    private List<String> groupList;
    private HashMap<String, List<?>> childMap;
    private Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        context = this;

        // Set up the toolbar with an Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the person's information from the intent
        Intent intent = getIntent();
        String personID = intent.getStringExtra("personID");

        // Get the ServerProxy instance
        serverProxy = ServerProxy.getInstance(context);

        // Retrieve the person object from the cache based on the personID
        person = serverProxy.getPersonFromCache(personID);

        // Display the person's information in the TextViews
        TextView firstNameTextView = findViewById(R.id.firstNameTextView);
        firstNameTextView.setText("First Name: " + person.getFirstName());
        TextView lastNameTextView = findViewById(R.id.lastNameTextView);
        lastNameTextView.setText("Last Name: " + person.getLastName());
        TextView genderTextView = findViewById(R.id.genderTextView);
        genderTextView.setText("Gender: " + person.getGender());

        // Initialize the ExpandableListView and populate it with data
        expandableListView = findViewById(R.id.expandableListView);
        populateData();
        expandableListAdapter = new CustomExpandableListAdapter(this, groupList, childMap);
        expandableListView.setAdapter(expandableListAdapter);

        // Set onClick listener for expandableListView
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Object selectedChild = expandableListAdapter.getChild(groupPosition, childPosition);

                if (groupPosition == 0) { // Family Members
                    if (selectedChild instanceof Person) {
                        Person relatedPerson = (Person) selectedChild;

                        if (relatedPerson != null) {
                            Intent personIntent = new Intent(PersonActivity.this, PersonActivity.class);
                            personIntent.putExtra("personID", relatedPerson.getPersonID());
                            startActivity(personIntent);
                        }
                    }
                } else if (groupPosition == 1) { // Life Events
                    if (selectedChild instanceof Event) {
                        Event selectedEvent = (Event) selectedChild;

                        if (selectedEvent != null) {
                            Intent eventIntent = new Intent(PersonActivity.this, EventActivity.class);
                            eventIntent.putExtra("eventID", selectedEvent.getEventID());
                            startActivity(eventIntent);
                        }
                    }
                }

                return false;
            }
        });
    }

    private void populateData() {
        groupList = new ArrayList<>();
        childMap = new HashMap<>();

        // Add the group names to the groupList
        groupList.add("Family Members");
        groupList.add("Life Events");

        // Add the family members to the childMap
        List<Person> familyMembers = new ArrayList<>();
        Person father = serverProxy.getPersonFromCache(person.getFatherID());
        Person mother = serverProxy.getPersonFromCache(person.getMotherID());
        Person spouse = serverProxy.getPersonFromCache(person.getSpouseID());

        if (father != null) {
            familyMembers.add(father);
        }
        if (mother != null) {
            familyMembers.add(mother);
        }
        if (spouse != null) {
            familyMembers.add(spouse);
        }
        childMap.put(groupList.get(0), familyMembers);

        // Add the life events to the childMap
        List<Event> lifeEvents = new ArrayList<>();
        Event[] events = serverProxy.getEventsFromCache();

        if (events != null) {
            for (Event event : events) {
                if (event != null && event.getPersonID().equals(person.getPersonID())) {
                    lifeEvents.add(event);
                }
            }
        }
        childMap.put(groupList.get(1), lifeEvents);
    }


    // This method finds the related persons based on the personID
    private Map<String, Person> findRelatedPersons(String personID) {
        // Instantiate the server proxy
        context = getActivity();
        serverProxy = ServerProxy.getInstance(context);

        // Retrieve the person object from the cache based on the personID
        Person person = serverProxy.getPersonFromCache(personID);

        if (person == null) {
            person = new Person();
        }

        // Get the person's father, mother, and spouse
        Person father = serverProxy.getPersonFromCache(person.getFatherID());
        Person mother = serverProxy.getPersonFromCache(person.getMotherID());
        Person spouse = serverProxy.getPersonFromCache(person.getSpouseID());

        // Get all persons from cache to find children
        Person[] allPersons = serverProxy.getPersonsFromCache();
        List<Person> children = new ArrayList<>();

        if (allPersons != null) {
            for (Person p : allPersons) {
                if (p.getFatherID() != null && p.getFatherID().equals(personID) || p.getMotherID() != null && p.getMotherID().equals(personID)) {
                    children.add(p);
                }
            }
        }

        // Store the related persons in a map
        Map<String, Person> relatedPersons = new HashMap<>();
        relatedPersons.put("father", father);
        relatedPersons.put("mother", mother);
        relatedPersons.put("spouse", spouse);
        for (int i = 0; i < children.size(); i++) {
            relatedPersons.put("child" + (i + 1), children.get(i));
        }

        return relatedPersons;
    }

    // This method finds all related events based on the personID
    private Event[] findRelatedEvents(String personID) {
        // Access the ServerProxy
        serverProxy = ServerProxy.getInstance(context);

        // Retrieve all event objects from the cache
        Event[] allEvents = serverProxy.getEventsFromCache();

        if (allEvents == null) {
            throw new RuntimeException("Events not found in cache");
        }

        // Count the number of related events
        int relatedEventCount = 0;
        for (Event event : allEvents) {
            if (event.getPersonID().equals(personID)) {
                relatedEventCount++;
            }
        }

        // Create an array to store the related events
        Event[] relatedEvents = new Event[relatedEventCount];

        // Filter events based on personID and store them in the relatedEvents array
        int index = 0;
        for (Event event : allEvents) {
            if (event.getPersonID().equals(personID)) {
                relatedEvents[index++] = event;
            }
        }

        return relatedEvents;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // If the Up button is pressed, navigate back to the MainActivity
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AppCompatActivity getActivity() {
        return this;
    }
}