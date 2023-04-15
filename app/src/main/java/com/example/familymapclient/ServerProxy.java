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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import Request.LoginRequest;
import Request.RegisterRequest;
import Result.LoginResult;
import Result.RegisterResult;

public class ServerProxy {
    private String BASE_URL = "http://" + "10.0.2.2" + ":" + "3000";
    private RequestQueue requestQueue;
    private Context context;

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

        default void onCacheEventCompleted(JSONObject jsonObject) {
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
    public void fetchFamilyData(final String authToken, String personID, final FamilyDataListener listener) {
        String url = BASE_URL + "/person/" + personID; // Include the personID in the URL
        Log.d("ServerProxy", "fetchFamilyData: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String firstName = response.getString("firstName");
                    String lastName = response.getString("lastName");
                    // Add FamilyData to cache
                    String cacheKey = authToken;
                    Cache.Entry cacheEntry = new Cache.Entry();
                    cacheEntry.data = response.toString().getBytes();
                    cacheEntry.responseHeaders = new HashMap<>();
                    cacheEntry.responseHeaders.put("Content-Type", "application/json");
                    cacheEntry.ttl = 15 * 60 * 1000; // 15 minutes
                    requestQueue.getCache().put(cacheKey, cacheEntry);
                    // Call cacheEvents
                    cacheEventListener cacheEventListener = new cacheEventListener() {
                        @Override
                        public void onCacheEventSuccess(String message) {
                            Log.d("ServerProxy", "onCacheFamilyDataSuccess: " + message);
                            Log.d("ServerProxy", "onCacheFamilyDataSuccess: " + response);
                        }

                        @Override
                        public void onCacheEventError(String error) {
                            Log.d("ServerProxy", "onCacheEventError: " + error);
                        }
                    };
//                    cacheEvents(authToken, cacheEventListener);

                    listener.onFamilyDataSuccess(firstName, lastName);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onFamilyDataError("Error parsing family data");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onFamilyDataError(error.getMessage());
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
    public JSONObject getFamilyDataFromCache(String authToken) {
        Cache.Entry cacheEntry = requestQueue.getCache().get(authToken);
        if (cacheEntry != null) {
            try {
                String jsonString = new String(cacheEntry.data, "UTF-8");
                return new JSONObject(jsonString);
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Method to cache persons data
    public void cachePersons(String authToken, final cacheEventListener listener) {
        String url = BASE_URL + "/person";
        Log.d("ServerProxy", "cachePersons: authToken = " + authToken);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    boolean success = response.getBoolean("success");
                    if (success) {
                        JSONArray data = response.getJSONArray("data");

                        Log.d("ServerProxy", "onResponse: " + response.toString());
                        // Add persons data to cache
                        String cacheKey = "persons";
                        Cache.Entry cacheEntry = new Cache.Entry();
                        cacheEntry.data = response.toString().getBytes();
                        cacheEntry.responseHeaders = new HashMap<>();
                        cacheEntry.responseHeaders.put("Content-Type", "application/json");
                        cacheEntry.ttl = 15 * 60 * 1000; // 15 minutes
                        requestQueue.getCache().put(cacheKey, cacheEntry);

                        listener.onCacheEventSuccess("Persons cached successfully!!");
                    } else {
                        listener.onCacheEventError(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onCacheEventError("Error parsing persons data");
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

    // Method to cache events data
    public void cacheEvents(String authToken, final cacheEventListener listener) {
        String url = BASE_URL + "/event";
        Log.d("ServerProxy", "cacheEvents: authToken = " + authToken);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    boolean success = response.getBoolean("success");
                    if (success) {
                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject event = data.getJSONObject(i);
//                            String eventType = event.getString("eventType");
//                            String personID = event.getString("personID");
//                            String city = event.getString("city");
//                            String country = event.getString("country");
//                            Double latitude = event.getDouble("latitude");
//                            Double longitude = event.getDouble("longitude");
//                            Integer year = event.getInt("year");
//                            String eventID = event.getString("eventID");
//                            String associatedUsername = event.getString("associatedUsername");
                        }
                        Log.d("ServerProxy", "onResponse: " + response.toString());
                        // Add events data to cache
                        String cacheKey = "events";
                        Cache.Entry cacheEntry = new Cache.Entry();
                        cacheEntry.data = response.toString().getBytes();
                        cacheEntry.responseHeaders = new HashMap<>();
                        cacheEntry.responseHeaders.put("Content-Type", "application/json");
                        cacheEntry.ttl = 15 * 60 * 1000; // 15 minutes
                        requestQueue.getCache().put(cacheKey, cacheEntry);


                        // Retrieve events data from cache to verify that it was cached successfully and to log it
                        Cache.Entry cacheEntry2 = requestQueue.getCache().get(cacheKey);
                        if (cacheEntry2 != null) {
                            try {
                                String jsonString = new String(cacheEntry2.data, "UTF-8");
                                Log.d("ServerProxy", "onResponseTest: " + jsonString);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }


                        listener.onCacheEventSuccess("Events cached successfully!!");
                    } else {
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

    // Method to get single event from cache
    public JSONObject getEventFromCache(String cacheKey, String eventID) {
        Log.d("ServerProxy", "getEventFromCache: " + cacheKey);
        Cache.Entry cacheEntry = requestQueue.getCache().get(cacheKey);
        Log.d("ServerProxy", "getEventFromCache: " + cacheEntry);
        if (cacheEntry != null) {
            try {
                String jsonString = new String(cacheEntry.data, "UTF-8");
                JSONObject cachedEvents = new JSONObject(jsonString);
                Log.d("ServerProxy", "getEventFromCache: " + cachedEvents);
                JSONArray events = cachedEvents.getJSONArray("data");
                for (int i = 0; i < events.length(); i++) {
                    JSONObject event = events.getJSONObject(i);
                    if (event.getString("eventID").equals(eventID)) {
                        return event;
                    }
                }
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Method to get events data from cache
    public JSONObject getEventsFromCache(String cacheKey) {
        Log.d("ServerProxy", "getEventsFromCache: " + cacheKey);
        Cache.Entry cacheEntry = requestQueue.getCache().get(cacheKey);
        Log.d("ServerProxy", "getEventsFromCache: " + cacheEntry);
        if (cacheEntry != null) {
            try {
                String jsonString = new String(cacheEntry.data, "UTF-8");
                JSONObject cachedEvents = new JSONObject(jsonString);
                Log.d("ServerProxy", "getEventsFromCache: " + cachedEvents);
                return cachedEvents;
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Method to get events data from cache in JSONARRAY format
    public JSONArray getEventsFromCacheAsJSONArray(String cacheKey) {
        Log.d("ServerProxy", "getEventsFromCache: " + cacheKey);
        Cache.Entry cacheEntry = requestQueue.getCache().get(cacheKey);
        Log.d("ServerProxy", "getEventsFromCache: " + cacheEntry);
        if (cacheEntry != null) {
            try {
                String jsonString = new String(cacheEntry.data, "UTF-8");
                JSONObject cachedEvents = new JSONObject(jsonString);
                Log.d("ServerProxy", "getEventsFromCache: " + cachedEvents);
                JSONArray eventsArray = cachedEvents.getJSONArray("data");
                return eventsArray;
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Method to get persons data from cache
    public JSONObject getPersonFromCache(String personID) {
        Cache.Entry cacheEntry = requestQueue.getCache().get("persons");
        if (cacheEntry != null) {
            try {
                String jsonString = new String(cacheEntry.data, "UTF-8");
                JSONObject personsData = new JSONObject(jsonString);
                JSONArray personsArray = personsData.getJSONArray("data");
                for (int i = 0; i < personsArray.length(); i++) {
                    JSONObject person = personsArray.getJSONObject(i);
                    if (person.getString("personID").equals(personID)) {
                        return person;
                    }
                }
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void clearCache() {
        if (requestQueue != null) {
            requestQueue.getCache().clear();
        }
    }
}