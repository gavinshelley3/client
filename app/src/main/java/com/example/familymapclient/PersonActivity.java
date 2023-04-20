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

import Model.Event;
import Model.Person;

public class PersonActivity extends AppCompatActivity {
    private Context context;
    private MapData mapData;
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

        // Get the MapData instance
        mapData = MapData.getInstance();
        mapData.init(context);

        // Retrieve the person object from the cache based on the personID
        person = mapData.getPerson(personID);

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
        Person father = mapData.getPerson(person.getFatherID());
        Person mother = mapData.getPerson(person.getMotherID());
        Person spouse = mapData.getPerson(person.getSpouseID());

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
        Event[] events = mapData.getEvents();

        if (events != null) {
            for (Event event : events) {
                if (event != null && event.getPersonID().equals(person.getPersonID())) {
                    lifeEvents.add(event);
                }
            }
        }
        childMap.put(groupList.get(1), lifeEvents);
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
}