package com.example.familymapclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    public static synchronized ServerProxy getInstance(Context context) {
        if (instance == null) {
            instance = new ServerProxy(context);
        }
        return instance;
    }

    // Constructor to initialize the server URL and context
    public ServerProxy(Context context, String serverHost, String serverPort) {
        requestQueue = Volley.newRequestQueue(context);
//        BASE_URL = "http://" + serverHost + ":" + serverPort;
        BASE_URL = "http://" + "10.0.2.2" + ":" + "3000";
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
        void onCacheEventSuccess(String message);

        void onCacheEventError(String error);

        default void onCacheEventCompleted(Event[] events) {
        } // Add default implementation
    }

    public interface cachePersonListener {
        void onCachePersonSuccess(Person person);

        void onCachePersonError(String error);

        default void onCachePersonCompleted(Person[] persons) {
        } // Add default implementation
    }

    // Constructor to initialize the context
    public ServerProxy(Context context) {
        requestQueue = Volley.newRequestQueue(context);
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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                LoginResult loginResult = new LoginResult();

                try {
                    loginResult.setAuthtoken(response.getString("authtoken"));
                    loginResult.setUsername(response.getString("username"));
                    loginResult.setPersonID(response.getString("personID"));
                    loginResult.setSuccess(response.getBoolean("success"));
                    saveAuthTokenToStorage(context, loginResult.getAuthtoken());

                    // Add LoginResult to cache
                    String cacheKey = loginResult.getUsername();
                    Cache.Entry cacheEntry = new Cache.Entry();
                    cacheEntry.data = response.toString().getBytes();
                    cacheEntry.responseHeaders = new HashMap<>();
                    cacheEntry.responseHeaders.put("Content-Type", "application/json");
                    cacheEntry.ttl = 15 * 60 * 1000; // 15 minutes
                    requestQueue.getCache().put(cacheKey, cacheEntry);
                    Log.d("ServerProxy", "onResponse: " + response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                listener.onLoginSuccess(loginResult);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onLoginError(error.getMessage());
            }
        });

        requestQueue.add(request);
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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                RegisterResult registerResult = new RegisterResult();

                try {
                    registerResult.setAuthtoken(response.getString("authtoken"));
                    registerResult.setUsername(response.getString("username"));
                    registerResult.setPersonID(response.getString("personID"));
                    registerResult.setSuccess(response.getBoolean("success"));
                    saveAuthTokenToStorage(context, registerResult.getAuthtoken());

                    // Add RegisterResult to cache
                    String cacheKey = registerResult.getUsername();
                    Cache.Entry cacheEntry = new Cache.Entry();
                    cacheEntry.data = response.toString().getBytes();
                    cacheEntry.responseHeaders = new HashMap<>();
                    cacheEntry.responseHeaders.put("Content-Type", "application/json");
                    cacheEntry.ttl = 15 * 60 * 1000; // 15 minutes
                    requestQueue.getCache().put(cacheKey, cacheEntry);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                listener.onRegisterSuccess(registerResult);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onRegisterError(error.getMessage());
            }
        });

        requestQueue.add(request);
    }

    // Method to fetch family data for a given authToken and personID
    public void fetchFamilyData(final String authToken, String personID, final cachePersonListener personListener) {
        String url = BASE_URL + "/person/" + personID; // Include the personID in the URL
        Log.d("ServerProxy", "fetchFamilyData: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String firstName = response.getString("firstName");
                    String lastName = response.getString("lastName");
                    String gender = response.getString("gender");
                    String personID = response.getString("personID");
                    String fatherID = response.getString("fatherID");
                    String motherID = response.getString("motherID");
                    String spouseID = response.has("spouseID") ? response.getString("spouseID") : ""; // Check if "spouseID" is present
                    String associatedUsername = response.getString("associatedUsername");

                    Person person = new Person(firstName, lastName, gender, personID, fatherID, motherID, spouseID, associatedUsername);

                    // Add Person to cache
                    String cacheKey = authToken;
                    Cache.Entry cacheEntry = new Cache.Entry();
                    cacheEntry.data = new Gson().toJson(person).getBytes();
                    cacheEntry.responseHeaders = new HashMap<>();
                    cacheEntry.responseHeaders.put("Content-Type", "application/json");
                    cacheEntry.ttl = 15 * 60 * 1000; // 15 minutes
                    requestQueue.getCache().put(cacheKey, cacheEntry);

                    personListener.onCachePersonSuccess(person);
                } catch (JSONException e) {
                    e.printStackTrace();
                    personListener.onCachePersonError("Error parsing family data");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                personListener.onCachePersonError(error.getMessage());
            }
        }) {
            // Add the authToken in the request header using an anonymous class
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", authToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // Method to save authToken to SharedPreferences
    public void saveAuthTokenToStorage(Context context, String authToken) {
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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean success = response.getBoolean("success");
                    if (success) {
                        JSONArray data = response.getJSONArray("data");
                        Person[] persons = new Person[data.length()];

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject personJson = data.getJSONObject(i);
                            Person person = new Person(personJson.getString("firstName"), personJson.getString("lastName"), personJson.getString("gender"), personJson.getString("personID"), personJson.optString("fatherID", ""), personJson.optString("motherID", ""), personJson.optString("spouseID", ""), personJson.getString("associatedUsername"));
                            persons[i] = person;
                        }
                        // Log the persons data before caching
                        Log.d("ServerProxy", "personsToBeCached: persons = " + new Gson().toJson(persons));

                        // Add persons data to cache
                        String cacheKey = "persons";
                        Cache.Entry cacheEntry = new Cache.Entry();
                        cacheEntry.data = new Gson().toJson(persons).getBytes();
                        cacheEntry.responseHeaders = new HashMap<>();
                        cacheEntry.responseHeaders.put("Content-Type", "application/json");
                        cacheEntry.ttl = 15 * 60 * 1000; // 15 minutes
                        requestQueue.getCache().put(cacheKey, cacheEntry);

                        if (persons.length > 0) {
                            listener.onCachePersonSuccess(persons[0]);
                        } else {
                            listener.onCachePersonError("No persons found");
                        }
                    } else {
                        String message = response.getString("message");
                        listener.onCachePersonError(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onCachePersonError("Error parsing persons data");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onCachePersonError(error.getMessage());
            }
        }) {
            // Add the authToken in the request header using an anonymous class
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", authToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // Method to cache all events for a given authToken
    public void cacheEvents(String authToken, final cacheEventListener listener) {
        String url = BASE_URL + "/event";
        Log.d("ServerProxy", "cacheEvents: authToken = " + authToken);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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

                            events[i] = event;  // Add event to events array
                        }
                        // Log the events data before caching
                        Log.d("ServerProxy", "eventsToBeCached: events = " + new Gson().toJson(events));

                        // Store the eventArray in cache
                        String cacheKey = "events";
                        Cache.Entry cacheEntry = new Cache.Entry();
                        cacheEntry.data = new Gson().toJson(events).getBytes();
                        cacheEntry.responseHeaders = new HashMap<>();
                        cacheEntry.responseHeaders.put("Content-Type", "application/json");
                        cacheEntry.ttl = 15 * 60 * 1000; // 15 minutes
                        requestQueue.getCache().put(cacheKey, cacheEntry);

                        listener.onCacheEventSuccess("Events cached successfully!!");
                    } else {
                        String message = response.getString("message");
                        listener.onCacheEventError(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onCacheEventError("Error parsing family data");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onCacheEventError(error.getMessage());
            }
        }) {
            // Add the authToken in the request header using an anonymous class
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", authToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // Method to get a single Event from the cache
    public Event getEventFromCache(String eventID) {
        Log.d("ServerProxy", "getEventFromCache: ");
        String cacheKey = "events";
        Cache.Entry entry = requestQueue.getCache().get(cacheKey);
        if (entry != null) {
            String data = new String(entry.data);
            Event[] events = new Gson().fromJson(data, Event[].class);
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
        Log.d("ServerProxy", "getEventsFromCache: ");
        String cacheKey = "events";
        Cache.Entry entry = requestQueue.getCache().get(cacheKey);
        if (entry != null) {
            String data = new String(entry.data);
            Event[] events = new Gson().fromJson(data, Event[].class);
            Log.d("ServerProxy", "getEventsFromCache: events = " + Arrays.toString(events));
            return events;
        }
        Log.d("ServerProxy", "getEventsFromCache: events = null");
        return null;
    }

    // Method to get a single Person from the cache
    public Person getPersonFromCache(String personID) {
        Log.d("ServerProxy", "getPersonFromCache: personID = " + personID);
        String cacheKey = "persons";
        Cache.Entry entry = requestQueue.getCache().get(cacheKey);
        if (entry != null) {
            String data = new String(entry.data);
            Person[] persons = new Gson().fromJson(data, Person[].class);
            Log.d("ServerProxy", "getPersonFromCache: persons = " + Arrays.toString(persons));
            for (Person person : persons) {
                if (person.getPersonID().equals(personID)) {
                    Log.d("ServerProxy", "getPersonFromCache: person = " + person.toString());
                    return person;
                }
            }
        }
        Log.d("ServerProxy", "getPersonFromCache: person = null");
        return null;
    }

    // Method to get all Persons from the cache
    public Person[] getPersonsFromCache() {
        Log.d("ServerProxy", "getPersonsFromCache: ");
        String cacheKey = "persons";
        Cache.Entry entry = requestQueue.getCache().get(cacheKey);
        if (entry != null) {
            String data = new String(entry.data);
            Person[] persons = new Gson().fromJson(data, Person[].class);
            Log.d("ServerProxy", "getPersonsFromCache: persons = " + Arrays.toString(persons));
            return persons;
        }
        Log.d("ServerProxy", "getPersonsFromCache: persons = null");
        return null;
    }

    public void clearCache() {
        if (requestQueue != null) {
            requestQueue.getCache().clear();
        }
    }
}