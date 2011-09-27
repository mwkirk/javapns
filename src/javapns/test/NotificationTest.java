package javapns.test;

import java.util.*;

import javapns.*;
import javapns.devices.*;
import javapns.devices.implementations.basic.*;
import javapns.notification.*;
import javapns.notification.transmission.*;

import org.json.*;

/**
 * A command-line test facility for the Push Notification Service.
 * <p>Example:  <code>java -cp "[required libraries]" javapns.test.NotificationTest keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4</code></p>
 * 
 * <p>By default, this test uses the sandbox service.  To switch, add "production" as a fourth parameter:</p>
 * <p>Example:  <code>java -cp "[required libraries]" javapns.test.NotificationTest keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4 production</code></p>
 * 
 * <p>Also by default, this test pushes a simple alert.  To send a complex payload, add "complex" as a fifth parameter:</p>
 * <p>Example:  <code>java -cp "[required libraries]" javapns.test.NotificationTest keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4 production complex</code></p>
 * 
 * <p>To send a simple payload to a large number of fake devices, add "threads" as a fifth parameter, the number of fake devices to construct, and the number of threads to use:</p>
 * <p>Example:  <code>java -cp "[required libraries]" javapns.test.NotificationTest keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4 sandbox threads 1000 5</code></p>
 * 
 * @author Sylvain Pedneault
 */
public class NotificationTest extends TestFoundation {

	public static void main(String[] args) {

		/* Verify that the test is being invoked  */
		if (!verifyCorrectUsage(NotificationTest.class, args, "keystore-path", "keystore-password", "device-token", "[production|sandbox]", "[complex|simple|threads]", "[#devices]", "[#threads]")) return;

		/* Initialize Log4j to print logs to console */
		configureBasicLogging();

		/* Push an alert */
		pushTest(args);
	}


	/**
	 * Pushes a test notification to a device, given command-line parameters.
	 * 
	 * @param args
	 */
	private static void pushTest(String[] args) {
		String keystore = args[0];
		String password = args[1];
		String token = args[2];
		boolean production = args.length >= 4 ? args[3].equalsIgnoreCase("production") : false;
		boolean simulation = args.length >= 4 ? args[3].equalsIgnoreCase("simulation") : false;
		boolean complex = args.length >= 5 ? args[4].equalsIgnoreCase("complex") : false;
		boolean threads = args.length >= 5 ? args[4].equalsIgnoreCase("threads") : false;
		int threadDevices = args.length >= 6 ? Integer.parseInt(args[5]) : 100;
		int threadThreads = args.length >= 7 ? Integer.parseInt(args[6]) : 10;
		boolean simple = !complex && !threads;

		if (simple) {

			/* Push a Hello World! alert */
			List<PushedNotification> notifications = Push.alert("Hello World!", keystore, password, production, token);
			printPushedNotifications(notifications);

		} else if (complex) {

			/* Push a more complex payload */
			List<PushedNotification> notifications = Push.payload(createComplexPayload(), keystore, password, production, token);
			printPushedNotifications(notifications);

		} else if (threads) {

			/* Push a Hello World! alert repetitively using NotificationThreads */
			pushSimplePayloadUsingThreads(keystore, password, production, token, simulation, threadDevices, threadThreads);

		}
	}


	/**
	 * Creates a complex payload for test purposes.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Payload createComplexPayload() {
		PushNotificationPayload complexPayLoad = new PushNotificationPayload();
		try {
			// You can use addBody to add simple message, but we'll use
			// a more complex alert message so let's comment it
			complexPayLoad.addCustomAlertBody("My alert message");
			complexPayLoad.addCustomAlertActionLocKey("Open App");
			complexPayLoad.addCustomAlertLocKey("javapns rocks %@ %@%@");
			ArrayList parameters = new ArrayList();
			parameters.add("Test1");
			parameters.add("Test");
			parameters.add(2);
			complexPayLoad.addCustomAlertLocArgs(parameters);
			complexPayLoad.addBadge(45);
			complexPayLoad.addSound("default");
			complexPayLoad.addCustomDictionary("acme", "foo");
			complexPayLoad.addCustomDictionary("acme2", 42);
			ArrayList values = new ArrayList();
			values.add("value1");
			values.add(2);
			complexPayLoad.addCustomDictionary("acme3", values);
		} catch (JSONException e) {
			System.out.println("Error creating complex payload:");
			e.printStackTrace();
		}
		return complexPayLoad;
	}


	private static void pushSimplePayloadUsingThreads(String keystore, String password, boolean production, String token, boolean simulation, int devices, int threads) {
		try {
			if (token == null || token.length() != 64) token = "1234567890123456789012345678901234567890123456789012345678901234";

			System.out.println("Creating PushNotificationManager and AppleNotificationServer");
			AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
			System.out.println("Creating payload (simulation mode)");
			Payload payload = PushNotificationPayload.alert("Hello World!");

			System.out.println("Generating " + devices + " fake devices");
			List<Device> deviceList = new ArrayList<Device>(devices);
			for (int i = 0; i < devices; i++)
				deviceList.add(new BasicDevice(token));

			System.out.println("Creating " + threads + " notification threads");
			NotificationThreads work = new NotificationThreads(server, simulation ? payload.asSimulationOnly() : payload, deviceList, threads);
			//work.setMaxNotificationsPerConnection(10000);
			System.out.println("Linking notification work debugging listener");
			work.setListener(DEBUGGING_PROGRESS_LISTENER);

			System.out.println("Starting all threads...");
			long timestamp1 = System.currentTimeMillis();
			work.start();
			System.out.println("All threads started, waiting for them...");
			work.waitForAllThreads();
			long timestamp2 = System.currentTimeMillis();
			System.out.println("All threads finished in " + (timestamp2 - timestamp1) + " milliseconds");
			List<PushedNotification> failedNotifications = work.getFailedNotifications();
			if (failedNotifications.size() == 0) {
				System.out.println("All notifications pushed successfully!");
			} else {
				System.out.println("Some notifications failed (" + failedNotifications.size() + "):");
				for (PushedNotification failedNotification : failedNotifications) {
					System.out.println("  " + failedNotification.toString());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final NotificationProgressListener DEBUGGING_PROGRESS_LISTENER = new NotificationProgressListener() {

		public void eventThreadStarted(NotificationThread notificationThread) {
			System.out.println("   [EVENT]: thread #" + notificationThread.getThreadNumber() + " started with " + notificationThread.getDevices().size() + " devices beginning at message id #" + notificationThread.getFirstMessageIdentifier());
		}


		public void eventThreadFinished(NotificationThread thread) {
			System.out.println("   [EVENT]: thread #" + thread.getThreadNumber() + " finished: pushed messages #" + thread.getFirstMessageIdentifier() + " to " + thread.getLastMessageIdentifier() + " toward " + thread.getDevices().size() + " devices");
		}


		public void eventConnectionRestarted(NotificationThread thread) {
			System.out.println("   [EVENT]: connection restarted in thread #" + thread.getThreadNumber() + " because it reached " + thread.getMaxNotificationsPerConnection() + " notifications per connection");
		}


		public void eventAllThreadsStarted(NotificationThreads notificationThreads) {
			System.out.println("   [EVENT]: all threads started: " + notificationThreads.getThreads().size());
		}


		public void eventAllThreadsFinished(NotificationThreads notificationThreads) {
			System.out.println("   [EVENT]: all threads finished: " + notificationThreads.getThreads().size());
		}
	};


	public static void printPushedNotifications(List<PushedNotification> notifications) {
		System.out.println("Pushed notifications:");
		for (PushedNotification notification : notifications) {
			try {
				System.out.println("  " + notification.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
