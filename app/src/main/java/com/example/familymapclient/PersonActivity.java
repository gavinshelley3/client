package com.example.familymapclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.example.familymapclient.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PersonActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;
    private List<String> groupList;
    private HashMap<String, List<String>> childMap;
    private ExpandableListAdapter expandableListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        // Set up the toolbar with an Up button
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the person's information from the intent
        Intent intent = getIntent();
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String gender = intent.getStringExtra("gender");

        // Display the person's information in the TextViews
        TextView firstNameTextView = findViewById(R.id.firstNameTextView);
        firstNameTextView.setText("First Name: " + firstName);
        TextView lastNameTextView = findViewById(R.id.lastNameTextView);
        lastNameTextView.setText("Last Name: " + lastName);
        TextView genderTextView = findViewById(R.id.genderTextView);
        genderTextView.setText("Gender: " + gender);

        // Initialize the ExpandableListView and populate it with data
        expandableListView = findViewById(R.id.expandableListView);
        populateData();
        expandableListAdapter = new CustomExpandableListAdapter(this, groupList, childMap);
        expandableListView.setAdapter(expandableListAdapter);
    }

    private void populateData() {
        // Populate the group data
        groupList = new ArrayList<>();
        groupList.add("Family Members");
        groupList.add("Life Events");

        // Populate the child data
        childMap = new HashMap<>();
        List<String> familyMembersList = new ArrayList<>();
        familyMembersList.add("Father");
        familyMembersList.add("Mother");
        familyMembersList.add("Spouse");
        familyMembersList.add("Child 1");
        familyMembersList.add("Child 2");
        childMap.put("Family Members", familyMembersList);

        List<String> lifeEventsList = new ArrayList<>();
        lifeEventsList.add("Birth");
        lifeEventsList.add("Marriage");
        lifeEventsList.add("Death");
        childMap.put("Life Events", lifeEventsList);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Close the current activity and return to the Main Activity
        onBackPressed();
        return true;
    }
}