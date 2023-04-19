package com.example.familymapclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private ImageButton searchButton;
    private RecyclerView searchResultsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view);

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set a custom adapter for the RecyclerView
        // Assuming you have a SearchResultsAdapter class
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
        // Perform the search logic and get the results as an ArrayList<Object> containing Person and Event objects
        ArrayList<Object> searchResults = new ArrayList<>();

        // Update the RecyclerView with the results
        SearchResultsAdapter searchResultsAdapter = (SearchResultsAdapter) searchResultsRecyclerView.getAdapter();
        searchResultsAdapter.setSearchResults(searchResults);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}