package com.example.familymapclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import Request.LoginRequest;
import Request.RegisterRequest;
import Result.LoginResult;
import Result.PersonResult;
import Result.RegisterResult;

public class LoginFragment extends Fragment {
    private EditText serverHostEditText;
    private EditText serverPortEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;
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
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup);
        maleRadioButton = view.findViewById(R.id.maleRadioButton);
        femaleRadioButton = view.findViewById(R.id.femaleRadioButton);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);
        serverHostEditText = view.findViewById(R.id.serverHostEditText);
        serverPortEditText = view.findViewById(R.id.serverPortEditText);

        // Initialize the ServerProxy
        serverProxy = new ServerProxy(getActivity());

        // Set up interaction listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput(true)) {
                    performLogin();
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput(false)) {
                    performRegistration();
                }
            }
        });

        // Fade and disable the Login button until all fields are filled
        loginButton.setEnabled(false);
        loginButton.setAlpha(0.5f);
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        serverHostEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        serverPortEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Fade and disable the Register button until all fields are filled
        registerButton.setEnabled(false);
        registerButton.setAlpha(0.5f);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        firstNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        lastNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkFields();
            }
        });

        return view;
    }

    // Check if all fields are filled and enable the Login and Register buttons if so
    private void checkFields() {
        boolean loginEnabled = !usernameEditText.getText().toString().isEmpty() &&
                !passwordEditText.getText().toString().isEmpty() &&
                !serverHostEditText.getText().toString().isEmpty() &&
                !serverPortEditText.getText().toString().isEmpty();
        loginButton.setEnabled(loginEnabled);
        loginButton.setAlpha(loginEnabled ? 1.0f : 0.5f);

        boolean registerEnabled = !emailEditText.getText().toString().isEmpty() &&
                !firstNameEditText.getText().toString().isEmpty() &&
                !lastNameEditText.getText().toString().isEmpty() &&
                (maleRadioButton.isChecked() || femaleRadioButton.isChecked());
        registerButton.setEnabled(registerEnabled);
        registerButton.setAlpha(registerEnabled ? 1.0f : 0.5f);
    }

    private boolean validateInput(boolean isLogin) {
        if (isLogin) {
            if (usernameEditText.getText().toString().isEmpty() ||
                    passwordEditText.getText().toString().isEmpty() ||
                    serverHostEditText.getText().toString().isEmpty() ||
                    serverPortEditText.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (usernameEditText.getText().toString().isEmpty() ||
                    passwordEditText.getText().toString().isEmpty() ||
                    emailEditText.getText().toString().isEmpty() ||
                    firstNameEditText.getText().toString().isEmpty() ||
                    lastNameEditText.getText().toString().isEmpty() ||
                    serverHostEditText.getText().toString().isEmpty() ||
                    serverPortEditText.getText().toString().isEmpty() ||
                    (!maleRadioButton.isChecked() && !femaleRadioButton.isChecked())) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void initializeServerProxy() {
        String serverHost = serverHostEditText.getText().toString();
        String serverPort = serverPortEditText.getText().toString();
        serverProxy = new ServerProxy(getActivity(), serverHost, serverPort);
    }

    private void performLogin() {
        initializeServerProxy();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();


        // Create a LoginRequest object
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Send the loginRequest to the server and handle the response
        serverProxy.login(loginRequest, new ServerProxy.LoginListener() {
            @Override
            public void onLoginSuccess(LoginResult loginResult) {
                if (loginResult.isSuccess()) {
                    Log.d("LoginFragment", "Login successful");
                    serverProxy.fetchFamilyData(loginResult.getAuthtoken(), loginResult.getPersonID(), new ServerProxy.FamilyDataListener() {
                        @Override
                        public void onFamilyDataSuccess(String firstName, String lastName) {
                            Toast.makeText(getActivity(), "Welcome, " + firstName + " " + lastName, Toast.LENGTH_SHORT).show();

                            // Navigate to the MapFragment
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new MapFragment())
                                    .commit();
                        }

                        @Override
                        public void onFamilyDataError(String error) {
                            Toast.makeText(getActivity(), "Error fetching family data: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Show an error message
                    Toast.makeText(getActivity(), "Login failed: " + loginResult.getMessage(), Toast.LENGTH_SHORT).show();
                }

//                serverProxy.fetchPersonData(loginResult.getAuthtoken(), loginResult.getPersonID(), new ServerProxy.PersonDataListener() {
//                    @Override
//                    public void onPersonDataSuccess(PersonResult personResult) {
//                        // Pass the person data to the PersonActivity using intents
//                        Intent personIntent = new Intent(getActivity(), PersonActivity.class);
//                        personIntent.putExtra("firstName", personResult.getFirstName());
//                        personIntent.putExtra("lastName", personResult.getLastName());
//                        personIntent.putExtra("gender", personResult.getGender());
////                        startActivity(personIntent);
//                    }
//
//                    @Override
//                    public void onPersonDataError(String error) {
//                        Toast.makeText(getActivity(), "Error fetching person data: " + error, Toast.LENGTH_SHORT).show();
//                    }
//                });
            }

            @Override
            public void onLoginError(String error) {
                Toast.makeText(getActivity(), "Login error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void performRegistration() {
        initializeServerProxy();
        // Get the values from the EditTexts
        String serverHost = serverHostEditText.getText().toString();
        String serverPort = serverPortEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String gender = maleRadioButton.isChecked() ? "m" : "f";

        // Create a RegisterRequest object
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setPassword(password);
        registerRequest.setEmail(email);
        registerRequest.setFirstName(firstName);
        registerRequest.setLastName(lastName);
        registerRequest.setGender(gender);
        registerRequest.setPersonID(registerRequest.generatePersonID());

        // Send the registerRequest to the server and handle the response
        serverProxy.register(registerRequest, new ServerProxy.RegisterListener() {
            @Override
            public void onRegisterSuccess(RegisterResult registerResult) {
                if (registerResult.isSuccess()) {
                    Log.d("RegisterFragment", "Register success");
                    serverProxy.fetchFamilyData(registerResult.getAuthtoken(), registerResult.getPersonID(), new ServerProxy.FamilyDataListener() {
                        @Override
                        public void onFamilyDataSuccess(String firstName, String lastName) {
                            Toast.makeText(getActivity(), "Welcome, " + firstName + " " + lastName, Toast.LENGTH_SHORT).show();

                            // Navigate to the MapFragment
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, new MapFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }

                        @Override
                        public void onFamilyDataError(String error) {
                            Toast.makeText(getActivity(), "Error fetching family data: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
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