package javapns.test;

import javapns.*;

public class NotificationTestSimple extends TestFoundation {

	public static void main(String[] args) {

		/* Verify that the test is being invoked  */
		if (!verifyCorrectUsage(NotificationTestSimple.class, args, "keystore-path", "keystore-password", "device-token")) return;

		/* Initialize Log4j to print logs to console */
		configureBasicLogging();

		/* Push an alert */
		String token = args[2];
		String keystore = args[0];
		String password = args[1];
		Push.alert("Hello World!", keystore, password, false, token);
	}

}
