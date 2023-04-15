package com.example.familymapclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Model.Event;
import Model.Person;

public class PersonActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;
    private List<String> groupList;
    private HashMap<String, List<String>> childMap;
    private CustomExpandableListAdapter expandableListAdapter;
    private ServerProxy serverProxy;
    private Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        // Set up the toolbar with an Up button
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the person's information from the intent
        Intent intent = getIntent();
        String personID = intent.getStringExtra("personID");

        // Get the MainActivity instance and access the ServerProxy
        MainActivity mainActivity = (MainActivity) getActivity();
        serverProxy = mainActivity.getServerProxy();

        // Retrieve the person object from the cache based on the personID
        JSONObject personData = serverProxy.getPersonFromCache(personID);
        person = new Person();
        try {
            if (personData != null) {
                person.setPersonID(personData.getString("personID"));
                person.setAssociatedUsername(personData.getString("associatedUsername"));
                person.setFirstName(personData.getString("firstName"));
                person.setLastName(personData.getString("lastName"));
                person.setGender(personData.getString("gender"));
                person.setFatherID(personData.getString("fatherID"));
                person.setMotherID(personData.getString("motherID"));
                person.setSpouseID(personData.getString("spouseID"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


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
                String selectedChild = (String) expandableListAdapter.getChild(groupPosition, childPosition);

                if (groupPosition == 0) { // Family Members
                    Person relatedPerson = findRelatedPerson(selectedChild);

                    if (relatedPerson != null) {
                        Intent personIntent = new Intent(PersonActivity.this, PersonActivity.class);
                        personIntent.putExtra("personID", relatedPerson.getPersonID());
                        startActivity(personIntent);
                    }
                } else if (groupPosition == 1) { // Life Events
                    Event relatedEvent = findRelatedEvent(selectedChild);

                    if (relatedEvent != null) {
                        Intent eventIntent = new Intent(PersonActivity.this, Event.class);
                        eventIntent.putExtra("eventID", relatedEvent.getEventID());
                        startActivity(eventIntent);
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
        List<String> familyMembers = new ArrayList<>();
        familyMembers.add(person.getFatherID());
        familyMembers.add(person.getMotherID());
        familyMembers.add(person.getSpouseID());
        childMap.put(groupList.get(0), familyMembers);

        // Add the life events to the childMap
        List<String> lifeEvents = new ArrayList<>();
        JSONArray events = serverProxy.getEventsFromCacheAsJSONArray("events");
        for (int i = 0; i < events.length(); i++) {
            try {
                JSONObject event = events.getJSONObject(i);
                lifeEvents.add(event.getString("description"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        childMap.put(groupList.get(1), lifeEvents);
    }

    private Person findRelatedPerson(String selectedChild) {
        // Get the MainActivity instance and access the ServerProxy
        MainActivity mainActivity = (MainActivity) getActivity();
        serverProxy = mainActivity.getServerProxy();

        // Retrieve the person object from the cache based on the personID
        JSONObject personData = serverProxy.getPersonFromCache(selectedChild);
        Person relatedPerson = new Person();
        try {
            if (personData != null) {
                relatedPerson.setPersonID(personData.getString("personID"));
                relatedPerson.setAssociatedUsername(personData.getString("associatedUsername"));
                relatedPerson.setFirstName(personData.getString("firstName"));
                relatedPerson.setLastName(personData.getString("lastName"));
                relatedPerson.setGender(personData.getString("gender"));
                relatedPerson.setFatherID(personData.getString("fatherID"));
                relatedPerson.setMotherID(personData.getString("motherID"));
                relatedPerson.setSpouseID(personData.getString("spouseID"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return relatedPerson;
    }

    private Event findRelatedEvent(String selectedChild) {
        // Get the MainActivity instance and access the ServerProxy
        MainActivity mainActivity = (MainActivity) getActivity();
        serverProxy = mainActivity.getServerProxy();

        // Retrieve the event object from the cache based on the eventID
        JSONObject eventData = serverProxy.getEventFromCache("events", selectedChild);
        Event relatedEvent = new Event();
        try {
            if (eventData != null) {
                relatedEvent.setEventID(eventData.getString("eventID"));
                relatedEvent.setAssociatedUsername(eventData.getString("associatedUsername"));
                relatedEvent.setPersonID(eventData.getString("personID"));
                relatedEvent.setLatitude((float) eventData.getDouble("latitude"));
                relatedEvent.setLongitude((float) eventData.getDouble("longitude"));
                relatedEvent.setCountry(eventData.getString("country"));
                relatedEvent.setCity(eventData.getString("city"));
                relatedEvent.setEventType(eventData.getString("eventType"));
                relatedEvent.setYear(eventData.getInt("year"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return relatedEvent;
    }

    private AppCompatActivity getActivity() {
        return this;
    }
}