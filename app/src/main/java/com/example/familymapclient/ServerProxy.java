package com.example.familymapclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import Model.Event;
import Model.Person;
import Request.LoginRequest;
import Request.RegisterRequest;
import Result.LoginResult;
import Result.RegisterResult;

public class ServerProxy {
    private String BASE_URL = "http://" + "10.0.2.2" + ":" + "3000";
    private RequestQueue requestQueue;
    private Context context;
    private static ServerProxy instance;
    private Person loggedInUser;

    public static synchronized ServerProxy getInstance(Context context) {
        if (instance == null) {
            instance = new ServerProxy(context);
        }
        return instance;
    }

    // Constructor to initialize the server URL and context
    private ServerProxy(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        this.context = context;
    }

    // Constructor to initialize the server URL and context when I reconnect serverHost and serverPort
    public ServerProxy(Context context, String serverHost, String serverPort) {
        requestQueue = Volley.newRequestQueue(context);
        BASE_URL = "http://" + serverHost + ":" + serverPort;
//        BASE_URL = "http://" + "10.0.2.2" + ":" + "3000";
        this.context = context;
    }

    // LoginListener interface for login callback methods
    public interface LoginListener {
        void onLoginSuccess(LoginResult loginResult);

        void onLoginError(String error);
    }

    // RegisterListener interface for register callback methods
    public interface RegisterListener {
        void onRegisterSuccess(RegisterResult registerResult);

        void onRegisterError(String error);
    }

    // FamilyDataListener interface for family data callback methods
    public interface FamilyDataListener {
        void onFamilyDataSuccess(String firstName, String lastName);

        void onFamilyDataError(String error);
    }

    // cacheEventListener interface for cache event callback methods
    public interface cacheEventListener {
        void onCacheEventSuccess(Event[] events);

        void onCacheEventError(String error);
    }

    // cachePersonListener interface for cache person callback methods
    public interface cachePersonListener {
        void onCacheSinglePersonSuccess(Person person);

        void onCacheMultiplePersonsSuccess(Person[] persons);

        void onCachePersonError(String error);
    }

    // Method to handle login request and response
    public void login(LoginRequest loginRequest, final LoginListener listener) {
        String url = BASE_URL + "/user/login";

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("username", loginRequest.getUsername());
            jsonRequest.put("password", loginRequest.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        handleRequest(Request.Method.POST, url, jsonRequest, response -> {
            LoginResult loginResult = new LoginResult();

            try {
                loginResult.setAuthtoken(response.getString("authtoken"));
                loginResult.setUsername(response.getString("username"));
                loginResult.setPersonID(response.getString("personID"));
                loginResult.setSuccess(response.getBoolean("success"));
                saveAuthTokenToStorage(context, loginResult.getAuthtoken());
            } catch (Exception e) {
                e.printStackTrace();
            }

            listener.onLoginSuccess(loginResult);
        }, error -> listener.onLoginError(error.getMessage()));
    }

    // Method to handle register request and response
    public void register(RegisterRequest registerRequest, final RegisterListener listener) {
        String url = BASE_URL + "/user/register";

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("username", registerRequest.getUsername());
            jsonRequest.put("password", registerRequest.getPassword());
            jsonRequest.put("email", registerRequest.getEmail());
            jsonRequest.put("firstName", registerRequest.getFirstName());
            jsonRequest.put("lastName", registerRequest.getLastName());
            jsonRequest.put("gender", registerRequest.getGender());
            jsonRequest.put("personID", registerRequest.getPersonID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        handleRequest(Request.Method.POST, url, jsonRequest, response -> {
            RegisterResult registerResult = new RegisterResult();

            try {
                registerResult.setAuthtoken(response.getString("authtoken"));
                registerResult.setUsername(response.getString("username"));
                registerResult.setPersonID(response.getString("personID"));
                registerResult.setSuccess(response.getBoolean("success"));
                saveAuthTokenToStorage(context, registerResult.getAuthtoken());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            listener.onRegisterSuccess(registerResult);
        }, error -> listener.onRegisterError(error.getMessage()));
    }

    // Method to fetch family data for a given authToken and personID
    public void fetchFamilyData(final String authToken, String personID, final cachePersonListener personListener) {
        String url = BASE_URL + "/person/" + personID; // Include the personID in the URL
        Log.d("ServerProxy", "fetchFamilyData: " + url);
        handleRequest(Request.Method.GET, url, null, response -> {
            try {
                String firstName = response.getString("firstName");
                String lastName = response.getString("lastName");
                String gender = response.getString("gender");
                String personID1 = response.getString("personID");
                String fatherID = response.getString("fatherID");
                String motherID = response.getString("motherID");
                String spouseID = response.has("spouseID") ? response.getString("spouseID") : ""; // Check if "spouseID" is present
                String associatedUsername = response.getString("associatedUsername");

                Person person = new Person(firstName, lastName, gender, personID1, fatherID, motherID, spouseID, associatedUsername);

                // Set person to loggedInUser
                setLoggedInUser(person);
                loggedInUser = person;

                // Add person to cache based on string "loggedInUser"
                addToCache("loggedInUser", person);

                // Add person to cache based on personID
                addToCache(authToken, person);

                personListener.onCacheSinglePersonSuccess(person);
            } catch (JSONException e) {
                e.printStackTrace();
                personListener.onCachePersonError("Error parsing family data");
            }
        }, error -> personListener.onCachePersonError(error.getMessage()), authToken);
    }

    // Method to save authToken to SharedPreferences
    private void saveAuthTokenToStorage(Context context, String authToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("authToken", authToken);
        editor.apply();
    }

    // Method to get family data from cache
    public Person getFamilyDataFromCache(String authToken) {
        Cache.Entry cacheEntry = requestQueue.getCache().get(authToken);
        if (cacheEntry != null) {
            try {
                String jsonString = new String(cacheEntry.data, StandardCharsets.UTF_8);
                return new Gson().fromJson(jsonString, Person.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Method to cache all persons for a given authToken
    public void cachePersons(String authToken, final cachePersonListener listener) {
        String url = BASE_URL + "/person";
        Log.d("ServerProxy", "cachePersons: authToken = " + authToken);

        handleRequest(Request.Method.GET, url, null, response -> {
            try {
                boolean success = response.getBoolean("success");
                if (success) {
                    JSONArray data = response.getJSONArray("data");
                    Person[] persons = new Person[data.length()];
                    Person[] motherAncestorPersons = new Person[0];
                    Person[] fatherAncestorPersons = new Person[0];

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject personJson = data.getJSONObject(i);
                        Person person = new Person(personJson.getString("firstName"), personJson.getString("lastName"), personJson.getString("gender"), personJson.getString("personID"), personJson.optString("fatherID", ""), personJson.optString("motherID", ""), personJson.optString("spouseID", ""), personJson.getString("associatedUsername"));
                        persons[i] = person;
                        // Add person to cache based on personID
                        addToCache(person.getPersonID(), person);
                    }

                    // Add persons data to cache
                    addToCache("persons", persons);

                    // Set person to loggedInUser
                    if (getLoggedInUser() == null) {
                        setLoggedInUser(persons[0]);
                        loggedInUser = persons[0];
                    }

                    // Call recursive method to get mother and father ancestors
                    motherAncestorPersons = getAncestorPersons(getLoggedInUser().getMotherID(), persons);
                    fatherAncestorPersons = getAncestorPersons(getLoggedInUser().getFatherID(), persons);

                    // Add motherAncestorPersons data to cache
                    addToCache("motherAncestorPersons", motherAncestorPersons);
                    // Add fatherAncestorPersons data to cache
                    addToCache("fatherAncestorPersons", fatherAncestorPersons);

                    listener.onCacheMultiplePersonsSuccess(persons);
                } else {
                    String message = response.getString("message");
                    listener.onCachePersonError(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                listener.onCachePersonError("Error parsing persons data");
            }
        }, error -> listener.onCachePersonError(error.getMessage()), authToken);
    }

    // Method to cache all events for a given authToken
    public void cacheEvents(String authToken, final cacheEventListener listener) {
        String url = BASE_URL + "/event";
        Log.d("ServerProxy", "cacheEvents: authToken = " + authToken);

        handleRequest(Request.Method.GET, url, null, response -> {
            try {
                boolean success = response.getBoolean("success");
                if (success) {
                    JSONArray data = response.getJSONArray("data");
                    Event[] events = new Event[data.length()];

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject eventJson = data.getJSONObject(i);
                        Event event = new Event();
                        event.setEventID(eventJson.getString("eventID"));
                        event.setAssociatedUsername(eventJson.getString("associatedUsername"));
                        event.setPersonID(eventJson.getString("personID"));
                        event.setLatitude((float) eventJson.getDouble("latitude"));
                        event.setLongitude((float) eventJson.getDouble("longitude"));
                        event.setCountry(eventJson.getString("country"));
                        event.setCity(eventJson.getString("city"));
                        event.setEventType(eventJson.getString("eventType"));
                        event.setYear(eventJson.getInt("year"));
                        events[i] = event;
                    }

                    // Store the eventArray in cache
                    addToCache("events", events);

                    listener.onCacheEventSuccess(events);
                } else {
                    String message = response.getString("message");
                    listener.onCacheEventError(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                listener.onCacheEventError("Error parsing family data");
            }
        }, error -> listener.onCacheEventError(error.getMessage()), authToken);
    }

    // Method to get a single Event from the cache
    public Event getEventFromCache(String eventID) {
        Log.d("ServerProxy", "getEventFromCache: ");
        Event[] events = getEventsFromCache();
        if (events != null) {
            for (Event event : events) {
                if (event.getEventID().equals(eventID)) {
                    Log.d("ServerProxy", "getEventFromCache: event = " + event);
                    return event;
                }
            }
        }
        Log.d("ServerProxy", "getEventFromCache: event = null");
        return null;
    }

    // Method to get all Events from the cache
    public Event[] getEventsFromCache() {
        Log.d("ServerProxy", "getEventsFromCache: starting");
        return getObjectFromCache("events", Event[].class);
    }

    // Method to get a single Person from the cache
    public Person getPersonFromCache(String personID) {
//        Log.d("ServerProxy", "getPersonFromCache: personID = " + personID);
        return getObjectFromCache(personID, Person.class);
    }

    // Method to get all Persons from the cache
    public Person[] getPersonsFromCache() {
        Log.d("ServerProxy", "getPersonsFromCache: ");
        return getObjectFromCache("persons", Person[].class);
    }

    private Person[] getAncestorPersons(String parentID, Person[] persons) {
        if (parentID == null || parentID.isEmpty()) {
            return new Person[0];
        }

        Person parent = null;
        for (Person person : persons) {
            if (person.getPersonID().equals(parentID)) {
                parent = person;
                break;
            }
        }

        if (parent == null) {
            return new Person[0];
        }

        Person[] motherAncestors = getAncestorPersons(parent.getMotherID(), persons);
        Person[] fatherAncestors = getAncestorPersons(parent.getFatherID(), persons);

        Person[] ancestors = new Person[motherAncestors.length + fatherAncestors.length + 1];
        ancestors[0] = parent;
        System.arraycopy(motherAncestors, 0, ancestors, 1, motherAncestors.length);
        System.arraycopy(fatherAncestors, 0, ancestors, 1 + motherAncestors.length, fatherAncestors.length);

        return ancestors;
    }

    // Method to get motherAncestorPersons from the cache
    public Person[] getMotherAncestorPersonsFromCache() {
        Log.d("ServerProxy", "getMotherAncestorPersonsFromCache: ");
        return getObjectFromCache("motherAncestorPersons", Person[].class);
    }

    // Method to get fatherAncestorPersons from the cache
    public Person[] getFatherAncestorPersonsFromCache() {
        Log.d("ServerProxy", "getFatherAncestorPersonsFromCache: ");
        return getObjectFromCache("fatherAncestorPersons", Person[].class);
    }

    // Getter and Setter methods to save the logged in user's Person
    public Person getLoggedInUser() {
        // If the loggedInUser is null, try to get it from cache
        if (loggedInUser == null) {
            Cache.Entry cacheEntry = requestQueue.getCache().get("loggedInUser");
            if (cacheEntry != null) {
                try {
                    String jsonString = new String(cacheEntry.data, StandardCharsets.UTF_8);
                    loggedInUser = new Gson().fromJson(jsonString, Person.class);
                    setLoggedInUser(loggedInUser);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d("ServerProxy", "getLoggedInUser: loggedInUser = " + loggedInUser);
        return loggedInUser;
    }

    public void setLoggedInUser(Person loggedInUser) {
        this.loggedInUser = loggedInUser;
    }


    public void clearCache() {
        if (requestQueue != null) {
            requestQueue.getCache().clear();
            // Log the cache after clearing
            Log.d("ServerProxy", "clearCache: cache = " + requestQueue.getCache().toString());
        }
    }

    private <T> T getObjectFromCache(String cacheKey, Class<T> clazz) {
        Cache.Entry entry = requestQueue.getCache().get(cacheKey);
        if (entry != null) {
            String data = new String(entry.data);
            return new Gson().fromJson(data, clazz);
        }
        return null;
    }

    private <T> void addToCache(String cacheKey, T object) {
        Cache.Entry cacheEntry = new Cache.Entry();
        cacheEntry.data = new Gson().toJson(object).getBytes();
        cacheEntry.responseHeaders = new HashMap<>();
        cacheEntry.responseHeaders.put("Content-Type", "application/json");
        cacheEntry.ttl = 15 * 60 * 1000; // 15 minutes
        requestQueue.getCache().put(cacheKey, cacheEntry);
    }

    private void handleRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> onResponse, Response.ErrorListener onErrorResponse) {
        handleRequest(method, url, jsonRequest, onResponse, onErrorResponse, null);
    }

    private void handleRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> onResponse, Response.ErrorListener onErrorResponse, String authToken) {
        JsonObjectRequest request = new JsonObjectRequest(method, url, jsonRequest, onResponse, onErrorResponse) {
            // Add the authToken in the request header using an anonymous class
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (authToken != null) {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", authToken);
                    return headers;
                }
                return super.getHeaders();
            }
        };
        requestQueue.add(request);
    }
}