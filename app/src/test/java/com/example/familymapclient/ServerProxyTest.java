package com.example.familymapclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import Model.Event;
import Model.Person;
import Request.LoginRequest;
import Request.RegisterRequest;
import Result.LoginResult;
import Result.RegisterResult;

@RunWith(MockitoJUnitRunner.class)
public class ServerProxyTest {
    private ServerProxy serverProxy;
    private Context context;


    @Before
    public void setUp() {
        // Set up a mocked context for the ServerProxy instance
        Context mockContext = mock(Context.class);
        serverProxy = ServerProxy.getInstance(mockContext);
    }

    @After
    public void tearDown() {
        serverProxy = null;
    }

    @Test
    public void testLogin() {
        String username = "testUsername";
        String password = "testPassword";
        String authToken = "testAuthToken";
        LoginRequest loginRequest = new LoginRequest(username, password);

        serverProxy.login(loginRequest, new ServerProxy.LoginListener() {
            @Override
            public void onLoginSuccess(LoginResult loginResult) {
                assertEquals(authToken, loginResult.getAuthtoken());
            }

            @Override
            public void onLoginError(String error) {
                // Do nothing
            }
        });
    }

    @Test
    public void testRegister() {
        RegisterRequest registerRequest = new RegisterRequest("testUsername", "testPassword", "testEmail", "testFirstName", "testLastName", "m", "testPersonID");

        serverProxy.register(registerRequest, new ServerProxy.RegisterListener() {
            @Override
            public void onRegisterSuccess(RegisterResult registerResult) {
                assertEquals("testUsername", registerResult.getUsername());
                assertEquals("testPersonID", registerResult.getPersonID());
            }

            @Override
            public void onRegisterError(String error) {
                // Do nothing
            }
        });
    }

    @Test
    public void testFetchFamilyData() {
        String authToken = "testAuthToken";
        String personID = "testPersonID";

        serverProxy.fetchFamilyData(authToken, personID, new ServerProxy.cachePersonListener() {
            @Override
            public void onCacheSinglePersonSuccess(Person person) {
                assertEquals(personID, person.getPersonID());
            }

            @Override
            public void onCacheMultiplePersonsSuccess(Person[] persons) {
                // Do nothing
            }

            @Override
            public void onCachePersonError(String error) {
                // Do nothing
            }
        });
    }

    @Test
    public void testCacheEvents() {
        String authToken = "testAuthToken";

        serverProxy.cacheEvents(authToken, new ServerProxy.cacheEventListener() {
            @Override
            public void onCacheEventSuccess(Event[] events) {
                assertEquals(1, events.length);
                assertEquals("testEventID", events[0].getEventID());
            }

            @Override
            public void onCacheEventError(String error) {
                // Do nothing
            }
        });
    }
}