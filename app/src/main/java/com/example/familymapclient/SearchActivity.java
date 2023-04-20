package com.example.familymapclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

import Model.Event;
import Model.Person;

public class SearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private ImageButton searchButton;
    private RecyclerView searchResultsRecyclerView;
    private ServerProxy serverProxy;
    private Context context;
    private boolean[] filterSettings;
    private Person[] motherSideAncestors;
    private Person[] fatherSideAncestors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view);

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(new SearchResultsAdapter());

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });
    }

    private void performSearch() {
        String searchString = searchInput.getText().toString().toLowerCase();
        Log.d("SearchActivity", "Search string: " + searchString);
        // TODO: Perform search
        serverProxy = ServerProxy.getInstance(getApplicationContext());
        filterSettings = getFilterSettingsFromPreferences();

        // Get the search results from the cache
        Object[] searchResults = getSearchResultsFromCache();
        motherSideAncestors = serverProxy.getMotherAncestorPersonsFromCache();
        fatherSideAncestors = serverProxy.getFatherAncestorPersonsFromCache();

        // Filter the search results based on the search string
        Object[] filteredSearchResults = filterSearchResults(searchResults, searchString);

        // Filter the search results based on the filter settings
        Object[] finalSearchResults = finalFilterSearchResults(filteredSearchResults);

        // Update the RecyclerView with the results
        SearchResultsAdapter searchResultsAdapter = (SearchResultsAdapter) searchResultsRecyclerView.getAdapter();
        if (searchResultsAdapter != null) {
            searchResultsAdapter.setSearchResults(finalSearchResults);
            searchResultsAdapter.notifyDataSetChanged();
            Log.d("SearchActivity", "Search results: " + finalSearchResults.length);
        }
    }

    // Method to retrieve the Events and Persons from the cache
    private Object[] getSearchResultsFromCache() {
        serverProxy = ServerProxy.getInstance(getApplicationContext());
        Event[] events = serverProxy.getEventsFromCache();
        Person[] people = serverProxy.getPersonsFromCache();


        // Combine the events and people arrays into one array
        Object[] searchResults = new Object[events.length + people.length];

        for (int i = 0; i < events.length; i++) {
            searchResults[i] = events[i];
        }

        for (int i = 0; i < people.length; i++) {
            searchResults[i + events.length] = people[i];
        }

        return searchResults;
    }

    // Method to filter the search results based on the search string
    private Object[] filterSearchResults(Object[] searchResults, String searchString) {
        ArrayList<Object> filteredSearchResults = new ArrayList<>();

        for (Object searchResult : searchResults) {
            if (searchResult instanceof Event) {
                Event event = (Event) searchResult;
                // Cast event.getYear() to a string to compare it to the search string
                Integer eventYearInt = (Integer) event.getYear();
                String eventYear = eventYearInt.toString();
                if (event.getEventType().toLowerCase().contains(searchString) || event.getCity().toLowerCase().contains(searchString) || event.getCountry().toLowerCase().contains(searchString) || eventYear.contains(searchString)) {
                    filteredSearchResults.add(event);
                }
            } else if (searchResult instanceof Person) {
                Person person = (Person) searchResult;
                if (person.getFirstName().toLowerCase().contains(searchString) || person.getLastName().toLowerCase().contains(searchString)) {
                    filteredSearchResults.add(person);
                }
            }
        }

        return filteredSearchResults.toArray();
    }

    // Method to filter the search results based on the filter settings
    private Object[] finalFilterSearchResults(Object[] searchResults) {
        ArrayList<Object> finalSearchResults = new ArrayList<>();

        for (Object searchResult : searchResults) {
            if (searchResult instanceof Event) {
                Event event = (Event) searchResult;
                Person associatedPerson = serverProxy.getPersonFromCache(event.getPersonID());
                if (!filterSettings[3] && associatedPerson.getGender().equals("m")) {

                } else if (!filterSettings[4] && associatedPerson.getGender().equals("f")) {

                } else if (!filterSettings[5] && isAncestor(associatedPerson.getPersonID(), motherSideAncestors)) {

                } else if (!filterSettings[6] && isAncestor(associatedPerson.getPersonID(), fatherSideAncestors)) {

                } else {
                    finalSearchResults.add(searchResult);
                }
            } else if (searchResult instanceof Person) {
                finalSearchResults.add(searchResult);
            }
        }

        return finalSearchResults.toArray();
    }

    // Method to check if a person is an ancestor of the current user
    private boolean isAncestor(String personID, Person[] ancestors) {
        for (Person ancestor : ancestors) {
            if (ancestor.getPersonID().equals(personID)) {
                return true;
            }
        }
        return false;
    }

    private boolean[] getFilterSettingsFromPreferences() {
        context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        boolean[] filterSettings = new boolean[7];

        filterSettings[0] = sharedPreferences.getBoolean("spouseLinesEnabled", true);
        filterSettings[1] = sharedPreferences.getBoolean("familyTreeLinesEnabled", true);
        filterSettings[2] = sharedPreferences.getBoolean("lifeStoryLineEnabled", true);
        filterSettings[3] = sharedPreferences.getBoolean("filterGenderMale", true);
        filterSettings[4] = sharedPreferences.getBoolean("filterGenderFemale", true);
        filterSettings[5] = sharedPreferences.getBoolean("filterSideMother", true);
        filterSettings[6] = sharedPreferences.getBoolean("filterSideFather", true);

        return filterSettings;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSearchButtonClicked(View view) {
        performSearch();
    }
}