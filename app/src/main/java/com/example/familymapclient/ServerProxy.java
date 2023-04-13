package com.example.familymapclient;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import Request.LoginRequest;
import Result.LoginResult;
import Result.RegisterResult;
import Request.RegisterRequest;
import Request.PersonRequest;
import Result.PersonResult;

public class ServerProxy {
    private String BASE_URL;
    private RequestQueue requestQueue;

    public ServerProxy(Context context, String serverHost, String serverPort) {
        requestQueue = Volley.newRequestQueue(context);
//        BASE_URL = "http://" + serverHost + ":" + serverPort;
        BASE_URL = "http://" + "10.0.2.2" + ":" + "3000";

    }

    public interface LoginListener {
        void onLoginSuccess(LoginResult loginResult);
        void onLoginError(String error);
    }

    public interface RegisterListener {
        void onRegisterSuccess(RegisterResult registerResult);
        void onRegisterError(String error);
    }

    public interface FamilyDataListener {
        void onFamilyDataSuccess(String firstName, String lastName);
        void onFamilyDataError(String error);
    }

    public interface PersonDataListener {
        void onPersonDataSuccess(PersonResult personResult);
        void onPersonDataError(String error);
    }


    public ServerProxy(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }
    public void login(LoginRequest loginRequest, final LoginListener listener) {
        String url = BASE_URL + "/user/login";

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("username", loginRequest.getUsername());
            jsonRequest.put("password", loginRequest.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        LoginResult loginResult = new LoginResult();

                        try {
                            loginResult.setAuthtoken(response.getString("authtoken"));
                            loginResult.setUsername(response.getString("username"));
                            loginResult.setPersonID(response.getString("personID"));
                            loginResult.setSuccess(response.getBoolean("success"));
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
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onLoginError(error.getMessage());
                    }
                });

        requestQueue.add(request);
    }

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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        RegisterResult registerResult = new RegisterResult();

                        try {
                            registerResult.setAuthtoken(response.getString("authtoken"));
                            registerResult.setUsername(response.getString("username"));
                            registerResult.setPersonID(response.getString("personID"));
                            registerResult.setSuccess(response.getBoolean("success"));

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
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onRegisterError(error.getMessage());
                    }
                });

        requestQueue.add(request);
    }

    public void fetchFamilyData(final String authToken, String personID, final FamilyDataListener listener) {
        String url = BASE_URL + "/person/" + personID; // Include the personID in the URL
        Log.d("ServerProxy", "fetchFamilyData: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
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
                            listener.onFamilyDataSuccess(firstName, lastName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onFamilyDataError("Error parsing family data");
                        }
                    }
                },
                new Response.ErrorListener() {
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

//    public void fetchPersonData(String authToken, String personID, final PersonDataListener listener) {
//        String url = BASE_URL + "/person/" + personID;
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            PersonResult personResult = new PersonResult();
//                            personResult.setPersonID(response.getString("personID"));
//                            personResult.setFirstName(response.getString("firstName"));
//                            personResult.setLastName(response.getString("lastName"));
//                            personResult.setGender(response.getString("gender"));
//                            personResult.setFatherID(response.getString("fatherID"));
//                            personResult.setMotherID(response.getString("motherID"));
//                            personResult.setSpouseID(response.getString("spouseID"));
//
//                            listener.onPersonDataSuccess(personResult);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            listener.onPersonDataError("Error parsing person data");
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        listener.onPersonDataError(error.getMessage());
//                    }
//                }) {
//            // Add the authToken in the request header using an anonymous class
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", authToken);
//                return headers;
//            }
//        };
//
//        requestQueue.add(request);
//    }


}