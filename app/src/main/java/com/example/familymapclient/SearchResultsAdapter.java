package com.example.familymapclient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private ArrayList<Object> searchResults;

    public SearchResultsAdapter() {
        this.searchResults = new ArrayList<>();
    }

    public void setSearchResults(ArrayList<Object> searchResults) {
        this.searchResults = searchResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = searchResults.get(position);
        // Populate the view based on the object type (Person or Event)
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // Declare views for Person and Event items

        ViewHolder(View itemView) {
            super(itemView);
            // Initialize views for Person and Event items
        }
    }
}