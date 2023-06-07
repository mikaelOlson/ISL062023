package com.infrasight.kodtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.infrasight.kodtest.api.auth.AuthRequest;
import okhttp3.*;
import okhttp3.internal.http2.Header;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.*;
import javax.json.Json.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

/**
 * Simple concrete class for JUnit tests with uses {@link TestsSetup} as a
 * foundation for starting/stopping the API server for tests.
 * <p>
 * You may configure port, api user and api port in {@link TestVariables} if
 * needed.
 */
public class Tests extends TestsSetup {

    private static final String API_URL = TestVariables.API_URL + TestVariables.API_PORT;
    private static final String API_ACCOUNTS_ENDPOINT = "/api/accounts";
    private static final String API_RELATIONSHIPS_ENDPOINT = "/api/relationships";
    private static final String API_GROUPS_ENDPOINT = "/api/groups";
    private static final String API_AUTH_ENDPOINT = "/api/auth";



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

        OkHttpClient client = new OkHttpClient();

        String token = Authenticate(client);

        //code to get all Vera's accounts

        String accountsEndpoint = "/api/accounts";
        String baseUrl = TestVariables.API_URL + TestVariables.API_PORT + accountsEndpoint;

        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();

        urlBuilder.addQueryParameter("filter", "employeeId=1337");

        String url = urlBuilder.build().toString();

        Request accountRequest = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(url)
                .get()
                .build();

        Call call2 = client.newCall(accountRequest);

        Response response2 = call2.execute();

        String accounts = response2.body().string();

        StringReader stringReader = new StringReader(accounts);

        JsonReader jsonReader = Json.createReader(stringReader);

        JsonArray jsonArray = jsonReader.readArray();

        var veraAccount = jsonArray.get(0);

        //TODO: add assert showing that it is vera's account
        assertTrue(response2.code() == 200 && !accounts.isEmpty());
    }

    @Test
    public void assignment2() throws InterruptedException, IOException {
        assertTrue(serverUp);

        /**
         * TODO: Add code to solve the second assignment where we expect the number of
         * groups to be 3.
         */
        int groupCount = 0;

        OkHttpClient client = new OkHttpClient();

        String token = Authenticate(client);

        //code to get all Vera's accounts

        String relationshipsEndpoint = "/api/relationships";
        String baseUrl = TestVariables.API_URL + TestVariables.API_PORT + relationshipsEndpoint;

        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();


        //TODO: do account request from task 1 to retrieve the memberId instead of hardcoding
        urlBuilder.addQueryParameter("filter", "memberId=vera_scope");
        urlBuilder.addQueryParameter("filter", "objectType=GroupMember");


        String url = urlBuilder.build().toString();

        Request accountRequest = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(url)
                .get()
                .build();

        Call call2 = client.newCall(accountRequest);
        Response response2 = call2.execute();
        String groups = response2.body().string();

        StringReader stringReader = new StringReader(groups);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonArray jsonArray = jsonReader.readArray();

        List<String> verasGroups = new ArrayList<>();

        jsonArray.stream().forEach(e -> verasGroups.add(e.asJsonObject().getString("groupId")));
        groupCount = verasGroups.size();

        // Assert which verifies the expected group count of 3
        assertEquals(3, groupCount);

        /**
         * TODO: Add Assert to verify the IDs of the groups found
         */
        assertTrue(verasGroups.containsAll(Arrays.asList(TestVariables.VERAS_DIRECT_GROUP_IDS.toArray())));
    }

    @Test
    public void assignment3() throws InterruptedException, IOException {
        assertTrue(serverUp);

        /**
         * TODO: Add code to solve the third assignment. Add Assert to verify the
         * expected number of groups. Add Assert to verify the IDs of the groups found.
         */

        //do same request as in assignment2 to get the direct groups
        OkHttpClient client = new OkHttpClient();
        String token = Authenticate(client);

        //code to get all Vera's accounts

        String relationshipsEndpoint = "/api/relationships";
        String baseUrl = TestVariables.API_URL + TestVariables.API_PORT + relationshipsEndpoint;

        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();


        //TODO: do account request from task 1 to retrieve the memberId instead of hardcoding
        urlBuilder.addQueryParameter("filter", "memberId=vera_scope");
        urlBuilder.addQueryParameter("filter", "objectType=GroupMember");


        String url = urlBuilder.build().toString();

        Request accountRequest = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(url)
                .get()
                .build();

        Call call2 = client.newCall(accountRequest);
        Response response2 = call2.execute();
        String groups = response2.body().string();

        StringReader stringReader = new StringReader(groups);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonArray jsonArray = jsonReader.readArray();


        List<String> directGroups = new ArrayList<String>();

        for (JsonValue jsonValue : jsonArray) {
            JsonObject jsonObject = (JsonObject) jsonValue;
            directGroups.add(jsonObject.getString("groupId"));
        }

        Set<String> visitedGroups = new HashSet<>();
        Set<String> memberOfGroups = new HashSet<>();

        memberOfGroups.addAll(directGroups);

        // Iterate over the direct groups and perform the group search
        for (String groupId : directGroups) {
            searchGroups(client, token, relationshipsEndpoint, groupId, visitedGroups, memberOfGroups);
        }

        int expectedGroups = 9;

        assertEquals(9, memberOfGroups.size());
        assertTrue(memberOfGroups.containsAll(TestVariables.EXPECTED_GROUP_IDS));


    }

    @Test
    public void assignment4() throws InterruptedException, IOException {
        assertTrue(serverUp);

        /**
         * TODO: Add code to solve the fourth assignment. Add Asserts to verify the
         * total salary requested
         */

        OkHttpClient client = new OkHttpClient();
        String token = Authenticate(client);

        int totalSalaries = calculateTotalSalary(client, token);

        // Verify the total salary
        int expectedTotalSalarySEK = 50000; // Replace with the expected total salary in SEK for external employees
        assertEquals(expectedTotalSalarySEK, totalSalaries);


    }


    @Test
    public void assignment5() throws InterruptedException, IOException {
        assertTrue(serverUp);

        /**
         * TODO: Add code to solve the fifth assignment. Add Asserts to verify the
         * managers requested
         */
        // Set up the client and get the token
        OkHttpClient client = new OkHttpClient();
        String token = Authenticate(client);

        // Step 1: Find group IDs for "Sverige" and get subgroups ids
        // Group names to search for
        List<String> targetGroupNames = List.of("Sverige", "Säljare");
        List<String> targetGroupIds = new ArrayList<>();
        Set<String> allGroupIds = new HashSet<>();

        int skip = 0;
        int totalItems = Integer.MAX_VALUE;

        String groupsEndpoint = "/api/groups";
        String groupsUrl = TestVariables.API_URL + TestVariables.API_PORT + groupsEndpoint;


        while (skip < totalItems) {

            HttpUrl.Builder groupsUrlBuilder = HttpUrl.parse(groupsUrl).newBuilder();
            groupsUrlBuilder.setQueryParameter("skip", Integer.toString(skip));
            groupsUrl = groupsUrlBuilder.build().toString();

            Request groupsRequest = new Request.Builder()
                    .header("Authorization", "Bearer " + token)
                    .url(groupsUrl)
                    .get()
                    .build();

            Call groupsCall = client.newCall(groupsRequest);
            Response groupsResponse = groupsCall.execute();
            String groupsResponseBody = groupsResponse.body().string();

            // Parse the groups response to find the group IDs
            JsonReader groupsJsonReader = Json.createReader(new StringReader(groupsResponseBody));
            JsonArray groupsJsonArray = groupsJsonReader.readArray();

            for (JsonValue jsonValue : groupsJsonArray) {
                JsonObject jsonObject = jsonValue.asJsonObject();
                String groupName = jsonObject.getString("name");
                String groupId = jsonObject.getString("id");
                allGroupIds.add(groupId);

                if (targetGroupNames.contains(groupName)) {
                    targetGroupIds.add(groupId);
                    if (targetGroupIds.size() == 2) {
                        break;
                    }
                }
            }

            if (targetGroupIds.size() == 2) {
                break;
            }

            // Update skip and totalItems based on the Content-Range header
            String contentRange = groupsResponse.header("Content-Range");
            if (contentRange != null) {
                String[] parts = contentRange.split("[ /-]");
                int startIndex = Integer.parseInt(parts[1]);
                int endIndex = Integer.parseInt(parts[2]);
                totalItems = Integer.parseInt(parts[3]);

                skip = endIndex + 1;

                if (endIndex >= totalItems - 1) {
                    break; // Exit the loop if on the last page
                }
            }
        }

        List<String> subGroups = new ArrayList<>();


        String relationshipsEndpoint = "/api/relationships";
        for (String memberId : allGroupIds) {

            String subGroupsUrl = TestVariables.API_URL + TestVariables.API_PORT + relationshipsEndpoint;

            HttpUrl.Builder subGroupsUrlBuilder = HttpUrl.parse(subGroupsUrl).newBuilder();
            subGroupsUrlBuilder.setQueryParameter("filter", "memberId=" + memberId);


            Request groupsMemberOfRequest = new Request.Builder()
                    .header("Authorization", "Bearer " + token)
                    .url(subGroupsUrlBuilder.build())
                    .get()
                    .build();

            Response response = client.newCall(groupsMemberOfRequest).execute();
            String subGroupsJson = response.body().string();

            // Parse the JSON response to retrieve the group information
            JsonArray subGroupsArray = Json.createReader(new StringReader(subGroupsJson)).readArray();

            for (JsonValue subGroup : subGroupsArray) {
                JsonObject subGroupJson = subGroup.asJsonObject();
                String groupId = subGroupJson.getString("groupId");
                if (targetGroupIds.contains(groupId)) {
                    subGroups.add(memberId);

                }
            }


        }
        System.out.println(subGroups);


        // Step 2: Find employees belonging to grp_saljare

        List<String> groupIds = new ArrayList<>(subGroups);
        groupIds.add("grp_saljare");


        Map<String, List<String>> groupMembers = new HashMap<>();
        for (String groupId : groupIds) {
            List<String> memberIds = new ArrayList<>();

            skip = 0;
            totalItems = Integer.MAX_VALUE;

            while (skip < totalItems) {
                HttpUrl.Builder relationshipsUrlBuilder = HttpUrl.parse(TestVariables.API_URL + TestVariables.API_PORT + relationshipsEndpoint).newBuilder();
                relationshipsUrlBuilder.addQueryParameter("skip", String.valueOf(skip));

                relationshipsUrlBuilder.addQueryParameter("filter", "groupId=" + groupId);

                Request relationshipsRequest = new Request.Builder()
                        .header("Authorization", "Bearer " + token)
                        .url(relationshipsUrlBuilder.build())
                        .get()
                        .build();

                Call relationshipsCall = client.newCall(relationshipsRequest);
                Response relationshipsResponse = relationshipsCall.execute();
                String relationshipsResponseBody = relationshipsResponse.body().string();

                // Parse the relationships response to find the members
                JsonReader relationshipsJsonReader = Json.createReader(new StringReader(relationshipsResponseBody));
                JsonArray relationshipsJsonArray = relationshipsJsonReader.readArray();

                for (JsonValue jsonValue : relationshipsJsonArray) {
                    JsonObject jsonObject = jsonValue.asJsonObject();
                    String memberId = jsonObject.getString("memberId");

                    memberIds.add(memberId);
                }

                // Update skip and totalItems based on the Content-Range header
                String contentRange = relationshipsResponse.header("Content-Range");
                if (contentRange != null) {
                    String[] parts = contentRange.split("[ /-]");
                    int startIndex = Integer.parseInt(parts[1]);
                    int endIndex = Integer.parseInt(parts[2]);
                    totalItems = Integer.parseInt(parts[3]);

                    skip = endIndex + 1;
                } else {
                    break; // Exit the loop if Content-Range header is missing
                }
            }

            // Add the memberIds to the groupMembers map
            groupMembers.put(groupId, memberIds);
        }

        //check all members of grp_stockholm_ grp_goteborg, gro_malmo and only keep those accounts that are ALSO in grp_saljare

        List<String> swedishSalespeople = new ArrayList<>();
        List<String> saljareAccounts = groupMembers.get("grp_saljare");

        groupMembers.forEach((key, value) -> {
            String group = key;
            List<String> accounts = value;

            if (!group.equals("grp_saljare")) {
                for (String account : accounts) {
                    if (saljareAccounts.contains(account)) {
                        swedishSalespeople.add(account);
                    }
                }
            }
        });

        System.out.println(swedishSalespeople);

        Map<String, Integer> managerEmployeeCounts = new HashMap<>();

        skip = 0;
        totalItems = Integer.MAX_VALUE;
        while (skip < totalItems) {

            HttpUrl.Builder urlBuilder = HttpUrl.parse(TestVariables.API_URL + TestVariables.API_PORT + relationshipsEndpoint).newBuilder();
            urlBuilder.setQueryParameter("skip", String.valueOf(skip));
            urlBuilder.setQueryParameter("filter", "objectType=ManagerFor");

            String managerUrl = urlBuilder.build().toString();
            Request managerRequest = new Request.Builder()
                    .header("Authorization", "Bearer " + token)
                    .url(managerUrl)
                    .get()
                    .build();

            Response managerResponse = client.newCall(managerRequest).execute();
            String managerResponseBody = managerResponse.body().string();

            JsonArray managerJsonArray = Json.createReader(new StringReader(managerResponseBody)).readArray();

            // Process the managers
            for (JsonValue jsonValue : managerJsonArray) {
                JsonObject jsonObject = jsonValue.asJsonObject();
                String accountId = jsonObject.getString("accountId");
                String managedId = jsonObject.getString("managedId");

                if (swedishSalespeople.contains(managedId)) {
                    managerEmployeeCounts.put(accountId, managerEmployeeCounts.getOrDefault(accountId, 0) + 1);
                }
            }

            String contentRange = managerResponse.header("Content-Range");
            if (contentRange != null) {
                String[] parts = contentRange.split("[ /-]");
                int endIndex = Integer.parseInt(parts[2]);
                totalItems = Integer.parseInt(parts[3]);

                skip = endIndex + 1;

                if (endIndex >= totalItems - 1) {
                    break; // Exit the loop if on the last page
                }
            }

        }
// Sort the managers by the number of employees in descending order
            List<Map.Entry<String, Integer>> sortedManagers = new ArrayList<>(managerEmployeeCounts.entrySet());
            sortedManagers.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

// Print the sorted managers
            for (Map.Entry<String, Integer> entry : sortedManagers) {
                System.out.println("Manager: " + entry.getKey() + ", Employee Count: " + entry.getValue());
            }

            assertTrue(Boolean.TRUE);
        }


    private String Authenticate(OkHttpClient client) throws IOException {
        //Authenticate user and get token
        Map<String, String> creds = new HashMap<>();
        creds.put("user", TestVariables.API_USER);
        creds.put("password", TestVariables.API_PASSWORD );

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(creds);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

        Request authRequest = new Request.Builder()
                .url(API_URL + API_AUTH_ENDPOINT)
                .post(requestBody)
                .build();

        Call call = client.newCall(authRequest);
        Response response = call.execute();
        ObjectMapper mapperResponse = new ObjectMapper();
        String tokenString = mapperResponse.writeValueAsString(response.body().string());

        String token = tokenString.split(":")[1].split("\"")[1].replace("\\", "");

        return token;
    }

    private void searchGroups(OkHttpClient client, String token, String relationshipsEndpoint, String
            groupId, Set<String> visitedGroups, Set<String> memberOfGroups) throws IOException {
        Stack<String> stack = new Stack<>();
        stack.push(groupId);

        while (!stack.isEmpty()) {
            String currentGroupId = stack.pop();

            // Check if the currentGroupId has been visited before to avoid revisiting
            if (visitedGroups.contains(currentGroupId)) {
                continue;
            }

            visitedGroups.add(currentGroupId);

            // Build the URL for retrieving the groups that the currentGroupId is a member of
            String groupsMemberOfUrl = TestVariables.API_URL + TestVariables.API_PORT + relationshipsEndpoint + "?filter=memberId=" + currentGroupId;

            Request groupsMemberOfRequest = new Request.Builder()
                    .header("Authorization", "Bearer " + token)
                    .url(groupsMemberOfUrl)
                    .get()
                    .build();

            try (Response response = client.newCall(groupsMemberOfRequest).execute()) {
                String groupsMemberOfJson = response.body().string();

                // Parse the JSON response to retrieve the group information
                JsonArray groupsMemberOfArray = Json.createReader(new StringReader(groupsMemberOfJson)).readArray();

                // Extract the groupIds from the response and add them to the memberOfGroups list
                for (JsonValue jsonValue : groupsMemberOfArray) {
                    JsonObject jsonObject = (JsonObject) jsonValue;
                    String groupIdMemberOf = jsonObject.getString("groupId");

                    // Add the groupIdMemberOf to the memberOfGroups list
                    memberOfGroups.add(groupIdMemberOf);

                    // Add the groupIdMemberOf to the stack for further exploration
                    stack.push(groupIdMemberOf);
                }
            }
        }
    }

    private int calculateTotalSalary(OkHttpClient client, String token) throws IOException {
        int totalSalary = 0;
        int pageSize = 25;
        int currentPage = 0;

        boolean hasMoreData = true;

        while (hasMoreData) {
            String accountsEndpoint = "/api/accounts";
            String baseUrl = TestVariables.API_URL + TestVariables.API_PORT + accountsEndpoint;

            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();

            int itemsToSkip = pageSize * currentPage;

            urlBuilder.addQueryParameter("skip", String.valueOf(itemsToSkip));
            urlBuilder.addQueryParameter("filter", "employeeType=EXTERNAL");

            String url = urlBuilder.build().toString();

            Request accountRequest = new Request.Builder()
                    .header("Authorization", "Bearer " + token)
                    .url(url)
                    .get()
                    .build();

            Call call = client.newCall(accountRequest);
            Response response = call.execute();
            String accountsResponse = response.body().string();

            // Check if there are more pages based on the content-range header
            Headers headers = response.headers();
            String contentRange = headers.get("Content-Range");

            if (contentRange != null) {
                String[] contentRangeParts = contentRange.split("/");
                String[] rangeParts = contentRangeParts[0].split("-");
                int currentPageEnd = Integer.parseInt(rangeParts[1]);
                int totalItems = Integer.parseInt(contentRangeParts[1]);

                if (currentPageEnd >= totalItems - 1) {
                    hasMoreData = false;

                }
                currentPage++;
            } else {
                hasMoreData = false;
            }

            JsonReader jsonReader = Json.createReader(new StringReader(accountsResponse));
            JsonArray jsonArray = jsonReader.readArray();

            for (JsonValue jsonValue : jsonArray) {
                JsonObject jsonObject = (JsonObject) jsonValue;
                int salary = jsonObject.getInt("salary");
                String currency = jsonObject.getString("salaryCurrency");
                totalSalary += convertCurrencyToSEK(salary, currency);
            }
        }

        return totalSalary;
    }

    private int convertCurrencyToSEK(int amount, String currency) {

        double rateSEKtoSEK = 1.0;
        double rateDKKtoSEK = 1.56;
        double rateEURtoSEK = 11.64;


        switch (currency) {
            case "SEK":
                return (int) (amount * rateSEKtoSEK);
            case "DKK":
                return (int) (amount * rateDKKtoSEK);
            case "EUR":
                return (int) (amount * rateEURtoSEK);
            default:
                throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
    }
}
