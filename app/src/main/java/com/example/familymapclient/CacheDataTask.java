package com.example.familymapclient;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import Model.Event;
import Model.Person;

public class CacheDataTask {
    private ServerProxy.cacheEventListener eventListener;
    private ServerProxy.cachePersonListener personListener;
    private ServerProxy serverProxy;
    private Executor executor;
    private Handler mainThreadHandler;
    private AtomicInteger tasksCompleted = new AtomicInteger(0);
    private String initialEventID;

    // Constructor to initialize server proxy, cache event listener, cache person listener, and executor
    public CacheDataTask(ServerProxy serverProxy, ServerProxy.cacheEventListener eventListener, ServerProxy.cachePersonListener personListener, String initialEventID) {
        this.serverProxy = serverProxy;
        this.eventListener = eventListener;
        this.personListener = personListener;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
        this.initialEventID = initialEventID;
    }

    public void execute(String authToken) {
        Log.d("CacheDataTaskDebug", "CacheDataTaskDebugExecute: started");
        Log.d("CacheDataTaskDebug", "CacheDataTaskDebugExecute: authToken - " + authToken);

        executor.execute(() -> {
            // Cache the events data
            serverProxy.cacheEvents(authToken, new ServerProxy.cacheEventListener() {
                @Override
                public void onCacheEventSuccess(String message) {
                    Log.d("CacheDataTaskDebug", "onCacheEventSuccess: " + message);
                    checkIfBothTasksCompleted();
                }

                @Override
                public void onCacheEventError(String error) {
                    Log.d("ServerProxy", "onCacheEventError: " + error);
                }
            });

            // Cache the persons data
            serverProxy.cachePersons(authToken, new ServerProxy.cachePersonListener() {
                @Override
                public void onCachePersonSuccess(Person person) {
                    Log.d("CacheDataTaskDebug", "onCachePersonsSuccess: ");
                    checkIfBothTasksCompleted();
                }

                @Override
                public void onCachePersonError(String error) {
                    Log.d("CacheDataTaskDebug", "onCachePersonsError: " + error);
                }

                @Override
                public void onCachePersonCompleted(Person[] persons) {
                    // Return the persons array
                    Log.d("CacheDataTaskDebug", "onCachePersonsCompleted: " + Arrays.toString(persons));
                }
            });
        });
    }

    private void checkIfBothTasksCompleted() {
        if (tasksCompleted.incrementAndGet() == 2) {
            Event[] events = serverProxy.getEventsFromCache();
            Log.d("CacheDataTaskDebug", "checkIfBothTasksCompleted: " + Arrays.toString(events));
            mainThreadHandler.post(() -> {
                // Add a log statement here
                Log.d("CacheDataTaskDebug", "checkIfBothTasksCompleted: " + Arrays.toString(events));
                eventListener.onCacheEventCompleted(events);
            });

            Person[] persons = serverProxy.getPersonsFromCache();
            Log.d("CacheDataTaskDebug", "checkIfBothTasksCompleted: " + Arrays.toString(persons));
            mainThreadHandler.post(() -> {
                // Add a log statement here
                Log.d("CacheDataTaskDebug", "checkIfBothTasksCompleted: " + Arrays.toString(persons));
                personListener.onCachePersonCompleted(persons);
            });
        } else {
            // Add a log statement here
            Log.d("CacheDataTaskDebug", "checkIfBothTasksCompleted: only " + tasksCompleted.get() + " tasks completed");
        }
    }
}