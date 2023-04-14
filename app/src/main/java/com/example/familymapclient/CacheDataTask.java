package com.example.familymapclient;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

// This class is responsible for caching data from the server in a background thread
public class CacheDataTask extends AsyncTask<String, Void, JSONObject> {
    private ServerProxy.cacheEventListener listener;
    private ServerProxy serverProxy;

    // Constructor to initialize server proxy and cache event listener
    public CacheDataTask(ServerProxy serverProxy, ServerProxy.cacheEventListener listener) {
        this.serverProxy = serverProxy;
        this.listener = listener;
    }

    // This method performs the caching task in the background thread
    @Override
    protected JSONObject doInBackground(String... authTokens) {
        Log.d("CacheDataTaskDebug", "doInBackground: started");

        if (authTokens.length == 0) {
            return null;
        }

        String authToken = authTokens[0];

        // Cache the data using the server proxy
        serverProxy.cacheEvents(authToken, new ServerProxy.cacheEventListener() {
            // Handle the success of the cache event
            @Override
            public void onCacheEventSuccess(String message) {
                Log.d("ServerProxy", "onCacheEventSuccess: " + message);
                // Retrieve the cached data
                JSONObject jsonObject = serverProxy.getEventsFromCache("events");
                // Notify the listener about the completion of the cache event task
                if (listener != null) {
                    listener.onCacheEventCompleted(jsonObject);
                }
            }

            // Handle the error of the cache event
            @Override
            public void onCacheEventError(String error) {
                Log.d("ServerProxy", "onCacheEventError: " + error);
            }
        });

        // Retrieve the cached data
        JSONObject jsonObject = serverProxy.getEventsFromCache(authToken);

        Log.d("CacheDataTaskDebug", "doInBackground: finished");
        return jsonObject;
    }

    // This method is called after the caching task is completed to return the result
    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        Log.d("CacheDataTaskDebug", "onPostExecute: started");

        // Notify the listener about the completion of the cache event task
        if (listener != null) {
            listener.onCacheEventCompleted(jsonObject);
        }

        Log.d("CacheDataTaskDebug", "onPostExecute: finished");
    }
}