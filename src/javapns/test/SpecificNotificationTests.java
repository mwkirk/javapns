package javapns.test;

import java.util.*;

import javapns.*;
import javapns.communication.exceptions.*;
import javapns.devices.*;
import javapns.devices.implementations.basic.*;
import javapns.notification.*;
import javapns.notification.transmission.*;

import org.json.*;

/**
 * Specific test cases intended for the project's developers.
 * 
 * @author Sylvain Pedneault
 */
public class SpecificNotificationTests extends TestFoundation {

	/**
	 * Execute this class from the command line to run tests.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		/* Verify that the test is being invoked  */
		if (!verifyCorrectUsage(NotificationTest.class, args, "keystore-path", "keystore-password", "device-token", "[production|sandbox]", "[test-name]")) return;

		/* Initialize Log4j to print logs to console */
		configureBasicLogging();

		/* Push an alert */
		runTest(args);
	}


	private SpecificNotificationTests() {
	}


	/**
	 * Push a test notification to a device, given command-line parameters.
	 * 
	 * @param args
	 */
	private static void runTest(String[] args) {
		String keystore = args[0];
		String password = args[1];
		String token = args[2];
		boolean production = args.length >= 4 ? args[3].equalsIgnoreCase("production") : false;
		boolean simulation = args.length >= 4 ? args[3].equalsIgnoreCase("simulation") : false;

		String testName = args.length >= 5 ? args[4] : null;
		if (testName == null || testName.length() == 0) testName = "default";

		try {
			SpecificNotificationTests.class.getDeclaredMethod("test_" + testName, String.class, String.class, String.class, boolean.class).invoke(null, keystore, password, token, production);
		} catch (NoSuchMethodException e) {
			System.out.println(String.format("Error: test '%s' not found.  Test names are case-sensitive", testName));
		} catch (Exception e) {
			(e.getCause() != null ? e.getCause() : e).printStackTrace();
		}
	}


	private static void test_PushHelloWorld(String keystore, String password, String token, boolean production) throws CommunicationException, KeystoreException {
		List<PushedNotification> notifications = Push.alert("Hello World!", keystore, password, production, token);
		NotificationTest.printPushedNotifications(notifications);
	}


	private static void test_Issue74(String keystore, String password, String token, boolean production) {
		try {
			System.out.println("");
			System.out.println("TESTING 257-BYTES PAYLOAD WITH SIZE ESTIMATION ENABLED");
			/* Expected result: PayloadMaxSizeProbablyExceededException when the alert is added to the payload */
			pushSpecificPayloadSize(keystore, password, token, production, true, 257);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			System.out.println("");
			System.out.println("TESTING 257-BYTES PAYLOAD WITH SIZE ESTIMATION DISABLED");
			/* Expected result: PayloadMaxSizeExceededException when the payload is pushed */
			pushSpecificPayloadSize(keystore, password, token, production, false, 257);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			System.out.println("");
			System.out.println("TESTING 256-BYTES PAYLOAD");
			/* Expected result: no exception */
			pushSpecificPayloadSize(keystore, password, token, production, false, 256);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void test_Issue75(String keystore, String password, String token, boolean production) {
		try {
			System.out.println("");
			System.out.println("TESTING 257-BYTES PAYLOAD WITH SIZE ESTIMATION ENABLED");
			NewsstandNotificationPayload payload = NewsstandNotificationPayload.contentAvailable();
			debugPayload(payload);

			List<PushedNotification> notifications = Push.payload(payload, keystore, password, production, token);
			NotificationTest.printPushedNotifications(notifications);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void test_ThreadPoolFeature(String keystore, String password, String token, boolean production) throws Exception {
		try {
			System.out.println("");
			System.out.println("TESTING THREAD POOL FEATURE");

			AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
			NotificationThreads pool = new NotificationThreads(server, 3).start();
			Device device = new BasicDevice(token);

			System.out.println("Thread pool started and waiting...");

			System.out.println("Sleeping 5 seconds before queuing payloads...");
			Thread.sleep(5 * 1000);

			for (int i = 1; i <= 4; i++) {
				Payload payload = PushNotificationPayload.alert("Test " + i);
				NotificationThread threadForPayload = (NotificationThread) pool.add(new PayloadPerDevice(payload, device));
				System.out.println("Queued payload " + i + " to " + threadForPayload.getThreadNumber());
				System.out.println("Sleeping 10 seconds before queuing another payload...");
				Thread.sleep(10 * 1000);
			}
			System.out.println("Sleeping 10 more seconds let threads enough times to push the latest payload...");
			Thread.sleep(10 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void pushSpecificPayloadSize(String keystore, String password, String token, boolean production, boolean checkWhenAdding, int targetPayloadSize) throws CommunicationException, KeystoreException, JSONException {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < targetPayloadSize - 20; i++)
			buf.append('x');

		String alertMessage = buf.toString();
		PushNotificationPayload payload = PushNotificationPayload.complex();
		if (checkWhenAdding) payload.setPayloadSizeEstimatedWhenAdding(true);
		debugPayload(payload);

		boolean estimateValid = payload.isEstimatedPayloadSizeAllowedAfterAdding("alert", alertMessage);
		System.out.println("Payload size estimated to be allowed: " + (estimateValid ? "yes" : "no"));
		payload.addAlert(alertMessage);
		debugPayload(payload);

		List<PushedNotification> notifications = Push.payload(payload, keystore, password, production, token);
		NotificationTest.printPushedNotifications(notifications);
	}


	private static void debugPayload(Payload payload) {
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		try {
			System.out.println("Payload size: " + payload.getPayloadSize());
		} catch (Exception e) {
		}
		try {
			System.out.println("Payload representation: " + payload);
		} catch (Exception e) {
		}
		System.out.println(payload.isPayloadSizeEstimatedWhenAdding() ? "Payload size is estimated when adding properties" : "Payload size is only checked when it is complete");
		System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
	}

}
