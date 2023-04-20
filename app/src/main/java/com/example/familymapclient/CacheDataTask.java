package com.example.familymapclient;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import Model.Event;
import Model.Person;

public class CacheDataTask {
    private static CacheDataTask instance;

    private ServerProxy.cacheEventListener eventListener;
    private ServerProxy.cachePersonListener personListener;
    private ServerProxy serverProxy;
    private Executor executor;
    private Handler mainThreadHandler;
    private AtomicInteger tasksCompleted = new AtomicInteger(0);

    private CacheDataTask(ServerProxy serverProxy, ServerProxy.cacheEventListener eventListener, ServerProxy.cachePersonListener personListener) {
        this.serverProxy = serverProxy;
        this.eventListener = eventListener;
        this.personListener = personListener;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    }

    public static CacheDataTask getInstance(ServerProxy serverProxy, ServerProxy.cacheEventListener eventListener, ServerProxy.cachePersonListener personListener) {
        if (instance == null) {
            instance = new CacheDataTask(serverProxy, eventListener, personListener);
        } else {
            instance.eventListener = eventListener;
            instance.personListener = personListener;
        }
        return instance;
    }

    public void execute(String authToken) {
        executor.execute(() -> {
            serverProxy.cacheEvents(authToken, new ServerProxy.cacheEventListener() {
                @Override
                public void onCacheEventSuccess(Event[] events) {
                    checkIfBothTasksCompleted();
                }

                @Override
                public void onCacheEventError(String error) {
                    eventListener.onCacheEventError(error);
                }
            });

            serverProxy.cachePersons(authToken, new ServerProxy.cachePersonListener() {
                @Override
                public void onCacheSinglePersonSuccess(Person person) {
                    // This method is not used in this context
                }

                @Override
                public void onCacheMultiplePersonsSuccess(Person[] persons) {
                    checkIfBothTasksCompleted();
                }

                @Override
                public void onCachePersonError(String error) {
                    personListener.onCachePersonError(error);
                }
            });
        });
    }

    private void checkIfBothTasksCompleted() {
        if (tasksCompleted.incrementAndGet() == 2) {
            Event[] events = serverProxy.getEventsFromCache();
            mainThreadHandler.post(() -> eventListener.onCacheEventSuccess(events));

            Person[] persons = serverProxy.getPersonsFromCache();
            mainThreadHandler.post(() -> personListener.onCacheMultiplePersonsSuccess(persons));
        }
    }
}