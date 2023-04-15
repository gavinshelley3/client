package com.example.familymapclient;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import Model.Event;

public class ClusterMarker implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;
    private float markerColor;
    private Event event; // Add this line

    public ClusterMarker(LatLng position, String title, String snippet, Event event) { // Update this line
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.event = event;
    }

    // Add a getter for the Event property
    public Event getEvent() {
        return event;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public float getMarkerColor() {
        return markerColor;
    }

    public void setMarkerColor(float markerColor) {
        this.markerColor = markerColor;
    }
}