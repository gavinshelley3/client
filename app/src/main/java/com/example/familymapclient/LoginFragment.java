package com.example.familymapclient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import Request.LoginRequest;
import Result.LoginResult;

public class LoginFragment extends Fragment {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText serverHostEditText;
    private EditText serverPortEditText;
    private Button loginButton;
    private Button registerButton;
    private ServerProxy serverProxy;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize views
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        serverHostEditText = view.findViewById(R.id.serverHostEditText);
        serverPortEditText = view.findViewById(R.id.serverPortEditText);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);

        // Initialize the ServerProxy
        serverProxy = new ServerProxy(getActivity());

        // Set up interaction listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    performLogin();
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    performRegistration();
                }
            }
        });

        return view;
    }

    private boolean validateInput() {
        if (usernameEditText.getText().toString().isEmpty() ||
                passwordEditText.getText().toString().isEmpty() ||
                serverHostEditText.getText().toString().isEmpty() ||
                serverPortEditText.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String serverHost = serverHostEditText.getText().toString();
        int serverPort = Integer.parseInt(serverPortEditText.getText().toString());

        // Create a LoginRequest object
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Send the loginRequest to the server and handle the response
        serverProxy.login(loginRequest, new ServerProxy.LoginListener() {
            @Override
            public void onLoginSuccess(LoginResult loginResult) {
                if (loginResult.isSuccess()) {
                    // Navigate to MapFragment
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new MapFragment())
                            .commit();
                } else {
                    // Show an error message
                    Toast.makeText(getActivity(), loginResult.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLoginError(String error) {
                Toast.makeText(getActivity(), "Login error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performRegistration() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String serverHost = serverHostEditText.getText().toString();
        int serverPort = Integer.parseInt(serverPortEditText.getText().toString());

        // Create a LoginRequest object for registration
        LoginRequest registerRequest = new LoginRequest(username, password);

        // TODO: Implement the logic to send a registration request to the back-end server and handle the response

        // For example, if the response is a successful LoginResult (after successful registration)
        // LoginResult loginResult = ...
        // if (loginResult.isSuccess()) {
        //     // Proceed to the next activity or fragment
        // } else {
        //     // Show an error message
        //     Toast.makeText(getActivity(), loginResult.getMessage(), Toast.LENGTH_SHORT).show();
        // }
    }
}