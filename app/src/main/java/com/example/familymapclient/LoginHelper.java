package com.example.familymapclient;

public class LoginHelper {
    public boolean validateInput(boolean isLogin, String username, String password, String serverHost, String serverPort, String email, String firstName, String lastName, String gender) {
        if (isLogin) {
            return !username.isEmpty() && !password.isEmpty() && !serverHost.isEmpty() && !serverPort.isEmpty();
        } else {
            return !username.isEmpty() && !password.isEmpty() && !email.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty() && !serverHost.isEmpty() && !serverPort.isEmpty() && (gender.equals("m") || gender.equals("f"));
        }
    }
}