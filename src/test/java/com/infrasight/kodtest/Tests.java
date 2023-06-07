package com.infrasight.kodtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;

import javax.json.*;

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
        String token = authenticate(client);

        //code to get all Vera's accounts
        String accountsUrl = API_URL + API_ACCOUNTS_ENDPOINT;
        HttpUrl.Builder urlBuilder = HttpUrl.parse(accountsUrl).newBuilder();
        urlBuilder.addQueryParameter("filter", "employeeId=1337");

        String url = urlBuilder.build().toString();

        Request accountRequest = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(url)
                .get()
                .build();

        Response response = executeRequest(accountRequest, client);
        String accounts = response.body().string();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode accountsNode = objectMapper.readTree(accounts);
        JsonNode veraAccount = accountsNode.get(0);

        assertTrue(response.code() == 200 && veraAccount.get("id").textValue().equals("vera_scope"));
    }

    @Test
    public void assignment2() throws InterruptedException, IOException {
        assertTrue(serverUp);

        /**
         * TODO: Add code to solve the second assignment where we expect the number of
         * groups to be 3.
         */

        OkHttpClient client = new OkHttpClient();
        String token = authenticate(client);

        int groupCount = 0;

        //code to get all Vera's accounts
        List<String> verasGroups = getVerasGroups(client, token);

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

        OkHttpClient client = new OkHttpClient();
        String token = authenticate(client);

        //code to get all Vera's accounts
        List<String> directGroups = getVerasGroups(client, token);

        Set<String> memberOfGroups = new HashSet<>(directGroups);
        Set<String> visitedGroups = new HashSet<>();

        memberOfGroups.addAll(directGroups);

        // Iterate over the direct groups and perform the group search
        for (String groupId : directGroups) {
            searchGroups(client, token, groupId, visitedGroups, memberOfGroups);
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
        String token = authenticate(client);

        int totalSalaries = calculateTotalSalary(client, token);



        assertTrue(totalSalaries>0); //TODO: Did not figure out how to verify total salary requested

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
        String token = authenticate(client);

        // Step 1: Find group IDs for "Sverige" and get subgroups ids
        List<String> targetGroupNames = List.of("Sverige", "Säljare");

        Map<String, List<String>> groupIds = findGroupIds(client, token, targetGroupNames);
        List<String> targetGroupIds = groupIds.get("targetGroupIds");
        List<String> allGroupIds = groupIds.get("allGroupIds");

        List<String> subGroups = new ArrayList<>();

        for (String memberId : allGroupIds) {
            String subGroupsUrl = API_URL + API_RELATIONSHIPS_ENDPOINT;
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
        // Step 2: Find employees belonging to grp_saljare

        List<String> groupIds2 = new ArrayList<>(subGroups);
        groupIds2.add("grp_saljare");


        Map<String, List<String>> groupMembers = new HashMap<>();
        for (String groupId : groupIds2) {
            List<String> memberIds = new ArrayList<>();

            int skip = 0;
            int totalItems = Integer.MAX_VALUE;

            while (skip < totalItems) {
                HttpUrl.Builder relationshipsUrlBuilder = HttpUrl.parse(API_URL + API_RELATIONSHIPS_ENDPOINT
                ).newBuilder();
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

        int skip = 0;
        int totalItems = Integer.MAX_VALUE;
        while (skip < totalItems) {

            HttpUrl.Builder urlBuilder = HttpUrl.parse(API_URL + API_RELATIONSHIPS_ENDPOINT
            ).newBuilder();
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

        assertTrue(!sortedManagers.isEmpty());//TODO did not figure out how to verify the managers
    }


    private String authenticate(OkHttpClient client) throws IOException {
        //Authenticate user and get token
        String authUrl = API_URL + API_AUTH_ENDPOINT;

        Map<String, String> creds = new HashMap<>();
        creds.put("user", TestVariables.API_USER);
        creds.put("password", TestVariables.API_PASSWORD);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(creds);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

        Request authRequest = new Request.Builder()
                .url(authUrl)
                .post(requestBody)
                .build();

        Response response = executeRequest(authRequest, client);
        String responseJson = response.body().string();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseNode = objectMapper.readTree(responseJson);
        String token = responseNode.get("token").asText();

        return token;
    }

    private void searchGroups(OkHttpClient client, String token, String groupId,
                              Set<String> visitedGroups, Set<String> memberOfGroups) throws IOException {
        Stack<String> stack = new Stack<>();
        stack.push(groupId);

        ObjectMapper objectMapper = new ObjectMapper();

        while (!stack.isEmpty()) {
            String currentGroupId = stack.pop();

            // Check if the currentGroupId has already been visited
            if (visitedGroups.contains(currentGroupId)) {
                continue;
            }

            visitedGroups.add(currentGroupId);

            // Build the URL for retrieving the groups that the currentGroupId is a member of
            String groupsMemberOfUrl = API_URL + API_RELATIONSHIPS_ENDPOINT;
            HttpUrl.Builder relationshipsUrlBuilder = HttpUrl.parse(groupsMemberOfUrl).newBuilder();
            relationshipsUrlBuilder.addQueryParameter("filter", "memberId=" + currentGroupId);

            Request groupsMemberOfRequest = new Request.Builder()
                    .header("Authorization", "Bearer " + token)
                    .url(groupsMemberOfUrl)
                    .get()
                    .build();

            try (Response response = executeRequest(groupsMemberOfRequest, client)) {
                String groupsMemberOfJson = response.body().string();

                // Parse the JSON response to retrieve the group information
                JsonNode groupsMemberOfNode = objectMapper.readTree(groupsMemberOfJson);
                if (groupsMemberOfNode.isArray()) {
                    for (JsonNode jsonNode : groupsMemberOfNode) {
                        String groupIdMemberOf = jsonNode.get("groupId").asText();

                        // Add the groupIdMemberOf to the memberOfGroups list
                        memberOfGroups.add(groupIdMemberOf);

                        // Add the groupIdMemberOf to the stack for further exploration
                        stack.push(groupIdMemberOf);
                    }
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

    private Response executeRequest(Request request, OkHttpClient client) throws IOException {
        Call call = client.newCall(request);
        return call.execute();
    }

    private List<String> getVerasGroups(OkHttpClient client, String token) throws IOException {
        String relationshipsUrl = API_URL + API_RELATIONSHIPS_ENDPOINT;
        HttpUrl.Builder urlBuilder = HttpUrl.parse(relationshipsUrl).newBuilder();
        urlBuilder.addQueryParameter("filter", "memberId=vera_scope");
        urlBuilder.addQueryParameter("filter", "objectType=GroupMember");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + token)
                .url(url)
                .get()
                .build();

        Response response = executeRequest(request, client);
        String relationshipsJson = response.body().string();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode relationshipsNode = objectMapper.readTree(relationshipsJson);

        List<String> verasGroups = new ArrayList<>();
        verasGroups.addAll(relationshipsNode.findValuesAsText("groupId"));

        return verasGroups;
    }

    private Map<String, List<String>> findGroupIds(OkHttpClient client, String token, List<String> targetGroupNames) throws IOException {

        List<String> targetGroupIds = new ArrayList<>();
        Set<String> allGroupIds = new HashSet<>();

        int skip = 0;
        int totalItems = Integer.MAX_VALUE;

        String groupsUrl = API_URL + API_GROUPS_ENDPOINT;

        ObjectMapper objectMapper = new ObjectMapper();

        while (skip < totalItems) {
            HttpUrl.Builder groupsUrlBuilder = HttpUrl.parse(groupsUrl).newBuilder();
            groupsUrlBuilder.setQueryParameter("skip", Integer.toString(skip));
            String url = groupsUrlBuilder.build().toString();

            Request groupsRequest = new Request.Builder()
                    .header("Authorization", "Bearer " + token)
                    .url(url)
                    .get()
                    .build();

            try (Response groupsResponse = executeRequest(groupsRequest, client)) {
                String groupsResponseBody = groupsResponse.body().string();

                // Parse the groups response to find the group IDs
                JsonNode groupsNode = objectMapper.readTree(groupsResponseBody);
                if (groupsNode.isArray()) {
                    for (JsonNode jsonNode : groupsNode) {
                        String groupName = jsonNode.get("name").asText();
                        String groupId = jsonNode.get("id").asText();
                        allGroupIds.add(groupId);

                        if (targetGroupNames.contains(groupName)) {
                            targetGroupIds.add(groupId);

                        }
                    }
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
        }

        Map<String, List<String>> result = new HashMap<>();
        result.put("targetGroupIds", targetGroupIds);
        result.put("allGroupIds", new ArrayList<>(allGroupIds));

        return result;
    }


}


