package javapns.test;

import java.util.*;

import org.json.*;

import javapns.*;
import javapns.notification.*;

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
 * @author Sylvain Pedneault
 */
public class NotificationTest extends TestFoundation {

	public static void main(String[] args) {

		/* Verify that the test is being invoked  */
		if (!verifyCorrectUsage(NotificationTest.class, args, "keystore-path", "keystore-password", "device-token", "[production|sandbox]", "[complex|simple]")) return;

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
		boolean complex = args.length >= 5 ? args[4].equalsIgnoreCase("complex") : false;

		if (!complex) {

			/* Push a Hello World! alert */
			Push.alert("Hello World!", keystore, password, production, token);

		} else {

			/* Push a more complex payload */
			Push.payload(createComplexPayload(), keystore, password, production, token);

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
			PayLoadCustomAlert customAlert = new PayLoadCustomAlert();
			// You can use addBody to add simple message, but we'll use
			// a more complex alert message so let's comment it
			//			customAlert.addBody("My alert message");
			customAlert.addActionLocKey("Open App");
			customAlert.addLocKey("javapns rocks %@ %@%@");
			ArrayList parameters = new ArrayList();
			parameters.add("Test1");
			parameters.add("Test");
			parameters.add(2);
			customAlert.addLocArgs(parameters);
			complexPayLoad.addCustomAlert(customAlert);
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

}
