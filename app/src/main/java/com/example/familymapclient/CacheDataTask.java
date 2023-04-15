package com.example.familymapclient;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;

import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CacheDataTask {
    private ServerProxy.cacheEventListener listener;
    private ServerProxy serverProxy;
    private Executor executor;
    private Handler mainThreadHandler;

    // Constructor to initialize server proxy, cache event listener and executor
    public CacheDataTask(ServerProxy serverProxy, ServerProxy.cacheEventListener listener) {
        this.serverProxy = serverProxy;
        this.listener = listener;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    }

    // This method performs the caching task in the background thread
    public void execute(String authToken) {
        Log.d("CacheDataTaskDebug", "execute: started");

        executor.execute(() -> {
            // Cache the events data using the server proxy
            try {
                serverProxy.cacheEvents(authToken, new ServerProxy.cacheEventListener() {
                    // Handle the success of the cache event
                    @Override
                    public void onCacheEventSuccess(String message) {
                        Log.d("CacheDataTaskDebug", "onCacheEventSuccess: " + message);
                        // Retrieve the cached data
                        JSONObject jsonObject = serverProxy.getEventsFromCache("events");
                        Log.d("CacheDataTaskDebug", "getEventsFromCacheAfter: " + jsonObject.toString());
                        // Notify the listener about the completion of the cache event task
                        mainThreadHandler.post(() -> {
                            if (listener != null) {
                                listener.onCacheEventCompleted(jsonObject);
                            }
                        });
                    }

                    // Handle the error of the cache event
                    @Override
                    public void onCacheEventError(String error) {
                        Log.d("ServerProxy", "onCacheEventError: " + error);
                    }
                });
            } catch (Exception e) {
                Log.d("CacheDataTaskDebug", "getEventsError: " + e.getMessage());
            }

            // Retrieve the cached data
            JSONObject jsonObject = serverProxy.getEventsFromCache("events");
            Log.d("CacheDataTaskDebug", "getEventsFromCacheAfter: " + jsonObject.toString());

            Log.d("CacheDataTaskDebug", "execute:finished");

            // Notify the listener about the completion of the cache event task
            mainThreadHandler.post(() -> {
                if (listener != null) {
                    listener.onCacheEventCompleted(jsonObject);
                }
            });

            // Cache the persons data using the server proxy
            try {
                serverProxy.cachePersons(authToken, new ServerProxy.cacheEventListener() {
                    // Handle the success of the cache persons
                    @Override
                    public void onCacheEventSuccess(String message) {
                        Log.d("CacheDataTaskDebug", "onCachePersonsSuccess: " + message);
                    }

                    // Handle the error of the cache persons
                    @Override
                    public void onCacheEventError(String error) {
                        Log.d("CacheDataTaskDebug", "onCachePersonsError: " + error);
                    }
                });
            } catch (Exception e) {
                Log.d("CacheDataTaskDebug", "getPersonsError: " + e.getMessage());
            }
        });
    }
}

