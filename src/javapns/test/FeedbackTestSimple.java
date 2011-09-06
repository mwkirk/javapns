package javapns.test;

import java.util.*;

import javapns.*;
import javapns.devices.*;

/**
 * A test facility for the Feedback Service.
 * Example:  java -cp "[required libraries]" javapns.test.FeedbackTestSimple keystore.p12 mypass
 * 
 * By default, this test uses the sandbox service.  To switch, add "production" as a third parameter:
 * Example:  java -cp "[required libraries]" javapns.test.FeedbackTestSimple keystore.p12 mypass production
 * 
 * @author Sylvain Pedneault
 */
public class FeedbackTestSimple extends TestFoundation {

	public static void main(String[] args) {

		/* Verify that the test is being invoked  */
		if (!verifyCorrectUsage(FeedbackTestSimple.class, args, "keystore-path", "keystore-password", "[production]")) return;

		/* Initialize Log4j to print logs to console */
		configureBasicLogging();

		/* Get a list of inactive devices */
		String keystore = args[0];
		String password = args[1];
		boolean production = args.length >= 3 ? args[2].equalsIgnoreCase("production") : false;
		List<Device> devices = Push.feedback(keystore, password, production);

		for (Device device : devices) {
			System.out.println("Inactive device: " + device.getToken());
		}
	}

}
