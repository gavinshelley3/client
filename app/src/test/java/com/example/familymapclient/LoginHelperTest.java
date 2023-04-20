package com.example.familymapclient;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginHelperTest {
    private LoginHelper loginHelper;

    @Before
    public void setUp() {
        loginHelper = new LoginHelper();
    }

    // Test cases for login validation
    @Test
    public void validateInput_login_success() {
        assertTrue(loginHelper.validateInput(true, "username", "password", "localhost", "8080", "", "", "", ""));
    }

    @Test
    public void validateInput_login_emptyUsername() {
        assertFalse(loginHelper.validateInput(true, "", "password", "localhost", "8080", "", "", "", ""));
    }

    // Test cases for registration validation
    @Test
    public void validateInput_registration_success() {
        assertTrue(loginHelper.validateInput(false, "username", "password", "localhost", "8080", "test@example.com", "John", "Doe", "m"));
    }

    @Test
    public void validateInput_registration_emptyFirstName() {
        assertFalse(loginHelper.validateInput(false, "username", "password", "localhost", "8080", "test@example.com", "", "Doe", "m"));
    }
}