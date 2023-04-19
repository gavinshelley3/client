package com.example.familymapclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

        // Get SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);

        // Load saved settings and set switch states
        spouseLinesSwitch.setChecked(sharedPreferences.getBoolean("spouseLinesEnabled", true));
        familyTreeLinesSwitch.setChecked(sharedPreferences.getBoolean("familyTreeLinesEnabled", true));
        lifeStoryLineSwitch.setChecked(sharedPreferences.getBoolean("lifeStoryLineEnabled", true));
        filterGenderMaleSwitch.setChecked(sharedPreferences.getBoolean("filterGenderMale", true));
        filterGenderFemaleSwitch.setChecked(sharedPreferences.getBoolean("filterGenderFemale", true));
        filterSideMotherSwitch.setChecked(sharedPreferences.getBoolean("filterSideMother", true));
        filterSideFatherSwitch.setChecked(sharedPreferences.getBoolean("filterSideFather", true));

        // Set up listeners for switches
        spouseLinesSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save spouse lines setting
                sharedPreferences.edit().putBoolean("spouseLinesEnabled", isChecked).apply();
            }
        });

        familyTreeLinesSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save family tree lines setting
                sharedPreferences.edit().putBoolean("familyTreeLinesEnabled", isChecked).apply();
            }
        });

        lifeStoryLineSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save life story line setting
                sharedPreferences.edit().putBoolean("lifeStoryLineEnabled", isChecked).apply();
            }
        });

        filterGenderMaleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save filter gender male setting
                sharedPreferences.edit().putBoolean("filterGenderMale", isChecked).apply();
            }
        });

        filterGenderFemaleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save filter gender female setting
                sharedPreferences.edit().putBoolean("filterGenderFemale", isChecked).apply();
            }
        });

        filterSideMotherSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save filter side mother setting
                sharedPreferences.edit().putBoolean("filterSideMother", isChecked).apply();
            }
        });

        filterSideFatherSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save filter side father setting
                sharedPreferences.edit().putBoolean("filterSideFather", isChecked).apply();
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