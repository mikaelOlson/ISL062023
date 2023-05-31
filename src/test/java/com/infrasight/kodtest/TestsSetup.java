package com.infrasight.kodtest;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

/**
 * Foundation for all tests towards kodtest-server
 */
public abstract class TestsSetup {

	/** Port which Kodtest API is will run on */
	private final static int PORT = 8080;

	/** Indicates if server is up and responding or not */
	protected static boolean serverUp = false;

	/** Thread running the KodtestServer */
	private static Thread serverThread;

	/** Kodtest Server */
	private static KodtestServer server;

	/**
	 * @return Builder for {@link OkHttpClient} which can be used for API access.
	 *         Comes configured with 5 second timeouts for read and connect.
	 */
	protected Builder getHttpClientBuilder() {
		okhttp3.OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder = builder.readTimeout(5, TimeUnit.SECONDS);
		builder = builder.connectTimeout(5, TimeUnit.SECONDS);
		return builder;
	}

	/**
	 * Setup JUnit tests by spinning up Kodtest server and attempting to access
	 * specific API endpoint. Returns when server is responding throws exception
	 * after ~10 seconds if the server is not responding.
	 */
	@BeforeClass
	public static void setup() throws InterruptedException, IOException, URISyntaxException {
		// Start thread which will run KodtestServer
		final CountDownLatch latch = new CountDownLatch(1);
		serverThread = new Thread("Kodtest-server") {
			public void run() {
				server = new KodtestServer(PORT, true);
				server.start();
				latch.countDown();
				server.join();
			};
		};
		serverThread.start();
		// Wait until server is up
		latch.await();

		// Attempt to connect towards API endpoint until KodtestServer is up and running
		for (int c = 0; c < 19; c++) {
			HttpURLConnection con = (HttpURLConnection) new URI("http://localhost:" + PORT + "/api/accounts").toURL()
					.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			try {
				con.connect();
				con.getResponseCode();
				if (con.getResponseCode() == 200 || con.getResponseCode() == 401) {
					serverUp = true;
					System.out.println(serverThread.getName() + " seems to be up and responding on port " + PORT
							+ ". Ready for test execution!");
					return;
				} else
					Thread.sleep(500);
			} catch (ConnectException e) {
				Thread.sleep(500);
				continue;
			}
		}

		serverUp = false;
		throw new RuntimeException(serverThread.getName() + " not responding on port " + PORT + " after 10 seconds");
	}

	/**
	 * @return Kodtest Server API user
	 */
	protected String getApiUser() {
		return "apiUser";
	}

	/**
	 * @return Kodtest Server API password
	 */
	protected String getApiPassword() {
		return "apiPassword!";
	}

	/**
	 * Tears down test environment by interrupting the thread running the Kodtest
	 * API closing it down.
	 */
	@AfterClass
	public static void teardownOnce() throws InterruptedException {
		server.shutdown();
		serverThread.interrupt();
		serverThread.join();
		serverUp = false;
	}
}
