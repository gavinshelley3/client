package com.example.familymapclient;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import Model.Event;
import Model.Person;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private Object[] searchResults;

    public SearchResultsAdapter() {
        this.searchResults = new Object[0];
    }

    public void setSearchResults(Object[] newSearchResults) {
        int oldLength = searchResults.length;
        int newLength = newSearchResults.length;

        searchResults = newSearchResults;

        if (newLength > oldLength) {
            notifyItemRangeChanged(0, oldLength);
            notifyItemRangeInserted(oldLength, newLength - oldLength);
        } else if (newLength < oldLength) {
            notifyItemRangeChanged(0, newLength);
            notifyItemRangeRemoved(newLength, oldLength - newLength);
        } else {
            notifyItemRangeChanged(0, newLength);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = searchResults[position];

        if (item instanceof Person) {
            Person person = (Person) item;
            holder.personName.setText(person.getFirstName() + " " + person.getLastName());
            holder.personDetails.setText("PersonID: " + person.getPersonID());

            holder.personName.setVisibility(View.VISIBLE);
            holder.personDetails.setVisibility(View.VISIBLE);
            holder.eventName.setVisibility(View.GONE);
            holder.eventDetails.setVisibility(View.GONE);
            holder.personLayout.setVisibility(View.VISIBLE);
            holder.eventLayout.setVisibility(View.GONE);
            Log.d("SearchResultsAdapter", "onBindViewHolder: Person - " + person.getFirstName() + " " + person.getLastName());
        } else if (item instanceof Event) {
            Event event = (Event) item;
            holder.eventName.setText(event.getEventType() + ": " + event.getCity() + ", " + event.getCountry() + " (" + event.getYear() + ")");
            holder.eventDetails.setText("EventID: " + event.getEventID());

            holder.eventName.setVisibility(View.VISIBLE);
            holder.eventDetails.setVisibility(View.VISIBLE);
            holder.personName.setVisibility(View.GONE);
            holder.personDetails.setVisibility(View.GONE);
            holder.personLayout.setVisibility(View.GONE);
            holder.eventLayout.setVisibility(View.VISIBLE);
            Log.d("SearchResultsAdapter", "onBindViewHolder: Event - " + event.getEventType() + ": " + event.getCity() + ", " + event.getCountry() + " (" + event.getYear() + ")");
        }
    }

    @Override
    public int getItemCount() {
        return searchResults.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // Declare views for Person and Event items
        TextView personName;
        TextView personDetails;
        TextView eventName;
        TextView eventDetails;
        LinearLayout personLayout;
        LinearLayout eventLayout;

        ViewHolder(View itemView) {
            super(itemView);
            // Initialize views for Person and Event items
            personName = itemView.findViewById(R.id.person_name);
            personDetails = itemView.findViewById(R.id.person_details);
            eventName = itemView.findViewById(R.id.event_name);
            eventDetails = itemView.findViewById(R.id.event_details);
            personLayout = itemView.findViewById(R.id.person_layout);
            eventLayout = itemView.findViewById(R.id.event_layout);
        }
    }
}