package com.example.familymapclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import Model.Event;

public class EventActivity extends AppCompatActivity {
    private TextView eventIDTextView;
    private TextView personIDTextView;
    private TextView eventTypeTextView;
    private TextView yearTextView;
    private TextView cityTextView;
    private TextView countryTextView;
    private ServerProxy serverProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // Set up the toolbar with an Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the ServerProxy instance
        serverProxy = ServerProxy.getInstance(getApplicationContext());

        // Get eventID from the intent
        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventID");

        // Retrieve the event object from the cache based on the eventID
        Event event = serverProxy.getEventFromCache(eventID);

        // Initialize MapFragment with the eventID as an argument
        MapFragment mapFragment = new MapFragment();
        Bundle arguments = new Bundle();
        arguments.putString("initialEventID", event.getEventID());
        mapFragment.setArguments(arguments);

        // Replace the existing MapFragment in the layout with the new instance
        getSupportFragmentManager().beginTransaction().replace(R.id.event_map_fragment_container, mapFragment).commit();
    }
}