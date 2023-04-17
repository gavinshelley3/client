package com.example.familymapclient;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import Model.Event;
import Model.Person;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> groupList;
    private HashMap<String, List<?>> childMap;

    public CustomExpandableListAdapter(Context context, List<String> groupList, HashMap<String, List<?>> childMap) {
        this.context = context;
        this.groupList = groupList;
        this.childMap = childMap;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childMap.get(groupList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childMap.get(groupList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
        }
        TextView groupTextView = convertView.findViewById(R.id.listGroupText);
        groupTextView.setTypeface(null, Typeface.BOLD);
        groupTextView.setText(groupTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Object child = getChild(groupPosition, childPosition);
        String childText;
        String gender = "";

        if (child instanceof Person) {
            Person childPerson = (Person) child;
            childText = childPerson.getFirstName() + " " + childPerson.getLastName();
            gender = childPerson.getGender();
        } else if (child instanceof Event) {
            Event childEvent = (Event) child;
            childText = childEvent.getEventType() + ": " + childEvent.getCity() + ", " + childEvent.getCountry() + " (" + childEvent.getYear() + ")";
        } else {
            // Handle the case when the child object is null
            childText = "Unknown";
        }

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }

        ImageView childIcon = convertView.findViewById(R.id.listItemIcon);
        if (groupPosition == 0) { // Family Members
            if (gender.equalsIgnoreCase("m")) {
                childIcon.setImageResource(R.drawable.ic_male_lg);
            } else if (gender.equalsIgnoreCase("f")) {
                childIcon.setImageResource(R.drawable.ic_female_lg);
            }
        } else if (groupPosition == 1) { // Life Events
            childIcon.setImageResource(R.drawable.ic_event_icon);
        }

        TextView childTextView = convertView.findViewById(R.id.listItemText);
        childTextView.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}