package com.example.familymapclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    // Add switches for filters
    private Switch spouseLinesSwitch;
    private Switch familyTreeLinesSwitch;
    private Switch lifeStoryLineSwitch;
    private Switch filterGenderMaleSwitch;
    private Switch filterGenderFemaleSwitch;
    private Switch filterSideMotherSwitch;
    private Switch filterSideFatherSwitch;
    private Button saveSettingsButton;
    private Button logoutButton;

    private Filtering filtering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Enable the up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize switches
        spouseLinesSwitch = findViewById(R.id.spouse_lines_switch);
        familyTreeLinesSwitch = findViewById(R.id.family_tree_lines_switch);
        lifeStoryLineSwitch = findViewById(R.id.life_story_line_switch);
        filterGenderMaleSwitch = findViewById(R.id.filter_gender_male_switch);
        filterGenderFemaleSwitch = findViewById(R.id.filter_gender_female_switch);
        filterSideFatherSwitch = findViewById(R.id.filter_side_father_switch);
        filterSideMotherSwitch = findViewById(R.id.filter_side_mother_switch);

        // Initialize other switches
        saveSettingsButton = findViewById(R.id.save_settings_button);
        logoutButton = findViewById(R.id.logout_button);

        // Get Filtering instance
        filtering = new Filtering(this);

        // Load saved settings and set switch states
        boolean[] filterSettings = filtering.getFilterSettings();
        spouseLinesSwitch.setChecked(filterSettings[0]);
        familyTreeLinesSwitch.setChecked(filterSettings[1]);
        lifeStoryLineSwitch.setChecked(filterSettings[2]);
        filterGenderMaleSwitch.setChecked(filterSettings[3]);
        filterGenderFemaleSwitch.setChecked(filterSettings[4]);
        filterSideMotherSwitch.setChecked(filterSettings[5]);
        filterSideFatherSwitch.setChecked(filterSettings[6]);

        // Set up listeners for switches
        spouseLinesSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save spouse lines setting
                filtering.saveFilterSetting(0, isChecked);
            }
        });

        familyTreeLinesSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save family tree lines setting
                filtering.saveFilterSetting(1, isChecked);
            }
        });

        lifeStoryLineSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save life story line setting
                filtering.saveFilterSetting(2, isChecked);
            }
        });

        filterGenderMaleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save filter gender male setting
                filtering.saveFilterSetting(3, isChecked);
            }
        });

        filterGenderFemaleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save filter gender female setting
                filtering.saveFilterSetting(4, isChecked);
            }
        });

        filterSideMotherSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save filter side mother setting
                filtering.saveFilterSetting(5, isChecked);
            }
        });

        filterSideFatherSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save filter side father setting
                filtering.saveFilterSetting(6, isChecked);
            }
        });

        // Set up listener for save settings button
        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous activity/fragment
                onBackPressed();
            }
        });

        // Set up listener for logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout action
                // Destroy the user's session and return to the Main Activity
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
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