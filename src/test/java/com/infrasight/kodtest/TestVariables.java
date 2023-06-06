package com.infrasight.kodtest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Modifiable variables for tests. You may change these freely if needed.
 */
class TestVariables {

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
}
