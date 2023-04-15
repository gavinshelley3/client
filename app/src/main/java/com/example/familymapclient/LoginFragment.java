package com.example.familymapclient;

import android.os.Bundle;
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
import androidx.fragment.app.FragmentTransaction;

import Request.LoginRequest;
import Request.RegisterRequest;
import Result.LoginResult;
import Result.RegisterResult;

// LoginFragment class that handles user login and registration
public class LoginFragment extends Fragment {
    // Declare UI components
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

    // Required empty public constructor
    public LoginFragment() {
    }

    // Inflate the layout for this fragment and initialize UI components
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        // Get the MainActivity instance and access the ServerProxy
        MainActivity mainActivity = (MainActivity) getActivity();
        ServerProxy serverProxy = mainActivity.getServerProxy();

        // Set up interaction listeners for the Login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput(true)) {
                    performLogin();
                }
            }
        });

        // Set up interaction listeners for the Register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput(false)) {
                    performRegistration();
                }
            }
        });

        // Set up TextWatchers to enable and disable the Login button based on input fields
        setupLoginButtonWatcher(usernameEditText);
        setupLoginButtonWatcher(passwordEditText);
        setupLoginButtonWatcher(serverHostEditText);
        setupLoginButtonWatcher(serverPortEditText);

        // Set up TextWatchers to enable and disable the Register button based on input fields
        setupRegisterButtonWatcher(emailEditText);
        setupRegisterButtonWatcher(firstNameEditText);
        setupRegisterButtonWatcher(lastNameEditText);
        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkFields();
            }
        });

        return view;
    }

    // Set up TextWatcher for Login button
    private void setupLoginButtonWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Set up TextWatcher for Register button
    private void setupRegisterButtonWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Check if all fields are filled and enable the Login and Register buttons if so
    private void checkFields() {
        // Enable or disable Login button based on input fields
        boolean loginEnabled = !usernameEditText.getText().toString().isEmpty() && !passwordEditText.getText().toString().isEmpty() && !serverHostEditText.getText().toString().isEmpty() && !serverPortEditText.getText().toString().isEmpty();
        loginButton.setEnabled(loginEnabled);
        loginButton.setAlpha(loginEnabled ? 1.0f : 0.5f);

        // Enable or disable Register button based on input fields
        boolean registerEnabled = !emailEditText.getText().toString().isEmpty() && !firstNameEditText.getText().toString().isEmpty() && !lastNameEditText.getText().toString().isEmpty() && (maleRadioButton.isChecked() || femaleRadioButton.isChecked());
        registerButton.setEnabled(registerEnabled);
        registerButton.setAlpha(registerEnabled ? 1.0f : 0.5f);
    }

    // Validate input fields before performing login or registration
    private boolean validateInput(boolean isLogin) {
        // Check required fields for login
        if (isLogin) {
            if (usernameEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty() || serverHostEditText.getText().toString().isEmpty() || serverPortEditText.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        // Check required fields for registration
        else {
            if (usernameEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty() || emailEditText.getText().toString().isEmpty() || firstNameEditText.getText().toString().isEmpty() || lastNameEditText.getText().toString().isEmpty() || serverHostEditText.getText().toString().isEmpty() || serverPortEditText.getText().toString().isEmpty() || (!maleRadioButton.isChecked() && !femaleRadioButton.isChecked())) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    // Initialize ServerProxy with server host and port
    private void initializeServerProxy() {
        String serverHost = serverHostEditText.getText().toString();
        String serverPort = serverPortEditText.getText().toString();
        serverProxy = new ServerProxy(getActivity(), serverHost, serverPort);
    }

    // Perform login by sending a LoginRequest to the server
    private void performLogin() {
        initializeServerProxy();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Create a LoginRequest object
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Send the loginRequest to the server and handle the response
        serverProxy.login(loginRequest, new ServerProxy.LoginListener() {
            // Handle successful login
            @Override
            public void onLoginSuccess(LoginResult loginResult) {
                if (loginResult.isSuccess()) {
                    Log.d("LoginFragment", "Login successful");
                    serverProxy.fetchFamilyData(loginResult.getAuthtoken(), loginResult.getPersonID(), new ServerProxy.FamilyDataListener() {
                        // Handle successful family data fetch
                        @Override
                        public void onFamilyDataSuccess(String firstName, String lastName) {
                            Toast.makeText(getActivity(), "Welcome, " + firstName + " " + lastName, Toast.LENGTH_SHORT).show();

                            // Navigate to the MapFragment
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
                        }

                        // Handle error in family data fetch
                        @Override
                        public void onFamilyDataError(String error) {
                            Toast.makeText(getActivity(), "Error fetching family data: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Show an error message for failed login
                    Toast.makeText(getActivity(), "Login failed: " + loginResult.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            // Handle login error
            @Override
            public void onLoginError(String error) {
                Toast.makeText(getActivity(), "Login error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Perform registration by sending a RegisterRequest to the server
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
            // Handle successful registration
            @Override
            public void onRegisterSuccess(RegisterResult registerResult) {
                if (registerResult.isSuccess()) {
                    Log.d("RegisterFragment", "Register success");
                    serverProxy.fetchFamilyData(registerResult.getAuthtoken(), registerResult.getPersonID(), new ServerProxy.FamilyDataListener() {
                        // Handle successful family data fetch
                        @Override
                        public void onFamilyDataSuccess(String firstName, String lastName) {
                            Toast.makeText(getActivity(), "Welcome, " + firstName + " " + lastName, Toast.LENGTH_SHORT).show();

                            // Navigate to the MapFragment
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, new MapFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }

                        // Handle error in family data fetch
                        @Override
                        public void onFamilyDataError(String error) {
                            Toast.makeText(getActivity(), "Error fetching family data: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Show an error message for failed registration
                    Toast.makeText(getActivity(), registerResult.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            // Handle registration error
            @Override
            public void onRegisterError(String error) {
                Toast.makeText(getActivity(), "Registration error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}