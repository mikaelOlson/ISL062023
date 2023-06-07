package com.infrasight.kodtest;

import java.util.*;

/**
 * Modifiable variables for tests. You may change these freely if needed.
 */
class TestVariables {

    public static final Set<String> EXPECTED_GROUP_IDS = new HashSet<>(Arrays.asList(
            "grp_inhyrda",
            "grp_malmo",
            "grp_choklad",
            "grp_itkonsulter",
            "grp_sverige",
            "grp_danmark",
            "grp_konfektyr",
            "grp_köpenhamn",
            "grp_chokladfabrik"
    ));
    public static final List<String> VERAS_DIRECT_GROUP_IDS = new ArrayList<>(Arrays.asList(
            "grp_köpenhamn", "grp_malmo", "grp_itkonsulter"));
    /**
     * Port which Kodtest Server API is will run on. API will be accessible at
     * http://localhost:PORT for tests
     */
    final static int API_PORT = 3000;
    /**
     * Kodtest Server API user (Used for /auth endpoint)
     */
    final static String API_USER = "apiUser";
    /**
     * Kodtest Server API password (Used for /auth endpoint)
     */
    final static String API_PASSWORD = "apiPassword";
    final static String API_URL = "http://localhost:";
}
