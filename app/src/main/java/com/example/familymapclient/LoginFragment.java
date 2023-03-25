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
import Request.RegisterRequest;
import Result.LoginResult;
import Result.RegisterResult;

public class LoginFragment extends Fragment {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText genderEditText;
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
        emailEditText = view.findViewById(R.id.emailEditText);
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        genderEditText = view.findViewById(R.id.genderEditText);
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
                emailEditText.getText().toString().isEmpty() ||
                firstNameEditText.getText().toString().isEmpty() ||
                lastNameEditText.getText().toString().isEmpty() ||
                genderEditText.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();


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
        String email = emailEditText.getText().toString();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String gender = genderEditText.getText().toString();

        // Create a RegisterRequest object
        // Create a RegisterRequest object
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setPassword(password);
        registerRequest.setEmail(email);
        registerRequest.setFirstName(firstName);
        registerRequest.setLastName(lastName);
        registerRequest.setGender(gender);

        // Send the registerRequest to the server and handle the response
        serverProxy.register(registerRequest, new ServerProxy.RegisterListener() {
            @Override
            public void onRegisterSuccess(RegisterResult registerResult) {
                if (registerResult.isSuccess()) {
                    // Navigate to MapFragment
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new MapFragment())
                            .commit();
                } else {
                    // Show an error message
                    Toast.makeText(getActivity(), registerResult.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRegisterError(String error) {
                Toast.makeText(getActivity(), "Registration error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}