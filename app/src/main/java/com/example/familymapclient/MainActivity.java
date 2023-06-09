package com.example.familymapclient;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import Result.EventResult;

public class MainActivity extends AppCompatActivity {
    private ServerProxy serverProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the ServerProxy instance
        serverProxy = ServerProxy.getInstance(this);

        // Replace the fragment_container with a new LoginFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, new LoginFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearCache();
    }

    private void clearCache() {
        if (serverProxy != null) {
            serverProxy.clearCache();
        }
    }

    public ServerProxy getServerProxy() {
        return serverProxy;
    }
}