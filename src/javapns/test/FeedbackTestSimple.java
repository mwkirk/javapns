package javapns.test;

import java.util.*;

import javapns.*;
import javapns.devices.*;

public class FeedbackTestSimple extends TestFoundation {

	public static void main(String[] args) {

		/* Verify that the test is being invoked  */
		if (!verifyCorrectUsage(FeedbackTestSimple.class, args, "keystore-path", "keystore-password")) return;

		/* Initialize Log4j to print logs to console */
		configureBasicLogging();

		/* Get a list of inactive devices */
		String keystore = args[0];
		String password = args[1];
		List<Device> devices = Push.feedback(keystore, password, false);

		for (Device device : devices) {
			System.out.println("Inactive device: " + device.getToken());
		}
	}

}
