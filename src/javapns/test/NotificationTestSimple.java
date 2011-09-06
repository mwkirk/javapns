package javapns.test;

import javapns.*;

/**
 * A test facility for the Push Notification Service.
 * Example:  java -cp "[required libraries]" javapns.test.NotificationTestSimple keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4
 * 
 * By default, this test uses the sandbox service.  To switch, add "production" as a fourth parameter:
 * Example:  java -cp "[required libraries]" javapns.test.NotificationTestSimple keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4 production
 * 
 * @author Sylvain Pedneault
 */
public class NotificationTestSimple extends TestFoundation {

	public static void main(String[] args) {

		/* Verify that the test is being invoked  */
		if (!verifyCorrectUsage(NotificationTestSimple.class, args, "keystore-path", "keystore-password", "device-token", "[production]")) return;

		/* Initialize Log4j to print logs to console */
		configureBasicLogging();

		/* Push an alert */
		String keystore = args[0];
		String password = args[1];
		String token = args[2];
		boolean production = args.length >= 4 ? args[3].equalsIgnoreCase("production") : false;
		Push.alert("Hello World!", keystore, password, production, token);
	}

}
