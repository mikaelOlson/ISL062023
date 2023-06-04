package com.infrasight.kodtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.infrasight.kodtest.api.auth.AuthRequest;
import okhttp3.*;
import okhttp3.internal.http2.Header;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.*;
import javax.json.Json.*;

/**
 * Simple concrete class for JUnit tests with uses {@link TestsSetup} as a
 * foundation for starting/stopping the API server for tests.
 * <p>
 * You may configure port, api user and api port in {@link TestVariables} if
 * needed.
 */
public class Tests extends TestsSetup {

    /**
     * Simple example test which asserts that the Kodtest API is up and running.
     */
    @Test
    public void connectionTest() throws InterruptedException {
        assertTrue(serverUp);
    }

    @Test
    public void assignment1() throws InterruptedException, IOException {
        assertTrue(serverUp);

        /**
         * TODO: Add code to solve the first assignment. Add Assert to show that you
         * found the account for Vera
         */

        //Authenticate user and get token
        String authEndpoint = "/api/auth";

        Map<String, String> creds = new HashMap<>();
        creds.put("password", "apiPassword");
        creds.put("user", "apiUser");


        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(creds);

        System.out.println(json);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);


        Request authRequest = new Request.Builder()
                .url(TestVariables.API_URL + TestVariables.API_PORT + authEndpoint)
                .post(requestBody)
                .build();


        //create client
        OkHttpClient client = getHttpClientBuilder().build();

        Call call = client.newCall(authRequest);

        Response response = call.execute();

        ObjectMapper mapperResponse = new ObjectMapper();
        String tokenString = mapperResponse.writeValueAsString(response.body().string());

        String token = tokenString.split(":")[1].split("\"")[1].replace("\\", "");


        System.out.println("Token: " + token);


        //code to get all Vera's accounts

        String accountsEndpoint = "/api/accounts";
        String url = TestVariables.API_URL+TestVariables.API_PORT+accountsEndpoint;



        Request accountRequest = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(url)
                .get()
                .build();

        Call call2 = client.newCall(accountRequest);

        Response response2 = call2.execute();


        assertTrue(response.code() == 200);


    }

    @Test
    public void assignment2() throws InterruptedException {
        assertTrue(serverUp);

        /**
         * TODO: Add code to solve the second assignment where we expect the number of
         * groups to be 3.
         */
        int groupCount = 0;

        // Assert which verifies the expected group count of 3
        assertEquals(3, groupCount);

        /**
         * TODO: Add Assert to verify the IDs of the groups found
         */
    }

    @Test
    public void assignment3() throws InterruptedException {
        assertTrue(serverUp);

        /**
         * TODO: Add code to solve the third assignment. Add Assert to verify the
         * expected number of groups. Add Assert to verify the IDs of the groups found.
         */
    }

    @Test
    public void assignment4() throws InterruptedException {
        assertTrue(serverUp);

        /**
         * TODO: Add code to solve the fourth assignment. Add Asserts to verify the
         * total salary requested
         */
    }

    @Test
    public void assignment5() throws InterruptedException {
        assertTrue(serverUp);

        /**
         * TODO: Add code to solve the fifth assignment. Add Asserts to verify the
         * managers requested
         */
    }
}
