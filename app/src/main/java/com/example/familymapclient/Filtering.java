package com.example.familymapclient;

import android.content.Context;
import android.content.SharedPreferences;

import Model.Event;
import Model.Person;

import java.util.ArrayList;

public class Filtering {

    private Context context;
    private ServerProxy serverProxy;
    private boolean[] filterSettings;
    private Person[] motherSideAncestors;
    private Person[] fatherSideAncestors;
    private MapData mapData;

    public Filtering(Context context) {
        this.context = context;
        mapData = MapData.getInstance();
        mapData.init(context);
        filterSettings = getFilterSettingsFromPreferences();
        motherSideAncestors = mapData.getMotherSideAncestors();
        fatherSideAncestors = mapData.getFatherSideAncestors();
    }

    // Method to filter the search results based on the search string
    public Object[] filterSearchResults(Object[] searchResults, String searchString) {
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

    public Object[] finalFilterSearchResults(Object[] searchResults) {
        ArrayList<Object> finalSearchResults = new ArrayList<>();

        for (Object searchResult : searchResults) {
            if (searchResult instanceof Event) {
                Event event = (Event) searchResult;
                Person associatedPerson = mapData.getPerson(event.getPersonID());
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

    private boolean isAncestor(String personID, Person[] ancestors) {
        for (Person ancestor : ancestors) {
            if (ancestor.getPersonID().equals(personID)) {
                return true;
            }
        }
        return false;
    }

    public boolean[] getFilterSettings() {
        return filterSettings;
    }

    public void saveFilterSetting(int index, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(getFilterKey(index), value).apply();
        filterSettings[index] = value;
    }

    private String getFilterKey(int index) {
        switch (index) {
            case 0:
                return "spouseLinesEnabled";
            case 1:
                return "familyTreeLinesEnabled";
            case 2:
                return "lifeStoryLineEnabled";
            case 3:
                return "filterGenderMale";
            case 4:
                return "filterGenderFemale";
            case 5:
                return "filterSideMother";
            case 6:
                return "filterSideFather";
            default:
                return "";
        }
    }

    private boolean[] getFilterSettingsFromPreferences() {
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
}