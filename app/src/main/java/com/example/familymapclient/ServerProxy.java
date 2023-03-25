package com.example.familymapclient;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import Request.LoginRequest;
import Result.LoginResult;

public class ServerProxy {
    private static final String BASE_URL = "http://your-server-address:3000";
    private RequestQueue requestQueue;

    public interface LoginListener {
        void onLoginSuccess(LoginResult loginResult);
        void onLoginError(String error);
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
                        } catch (JSONException e) {
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
}
