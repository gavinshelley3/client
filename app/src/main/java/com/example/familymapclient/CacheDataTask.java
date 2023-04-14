package com.example.familymapclient;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

public class CacheDataTask extends AsyncTask<String, Void, JSONObject> {
    private ServerProxy.cacheEventListener listener;
    private ServerProxy serverProxy;

    public CacheDataTask(ServerProxy serverProxy, ServerProxy.cacheEventListener listener) {
        this.serverProxy = serverProxy;
        this.listener = listener;
    }

    @Override
    protected JSONObject doInBackground(String... authTokens) {
        String authToken = authTokens[0];

        // Cache the data here
        serverProxy.cacheEvents(authToken, new ServerProxy.cacheEventListener() {
            @Override
            public void onCacheEventSuccess(String message) {
                Log.d("ServerProxy", "onCacheEventSuccess: " + message);
            }

            @Override
            public void onCacheEventError(String error) {
                Log.d("ServerProxy", "onCacheEventError: " + error);
            }
        });

        // Return the cached data
        return serverProxy.getEventsFromCache(authToken);
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if (listener != null) {
            listener.onCacheEventCompleted(jsonObject);
        }
    }
}