package javapns.test;

import javapns.communication.*;
import javapns.devices.*;
import javapns.notification.*;

import org.apache.log4j.*;

public class NotificationTestSimple {

	private static final String PUSH_SERVER_HOST = "gateway.sandbox.push.apple.com";
	private static final int PUSH_SERVER_PORT = 2195;

	public static void main(String[] args) {
		if (args==null || args.length < 3) {
			System.out.println("Usage: javapns.test.NotificationTestSimple <keystore-path> <keystore-password> <device-token>");
			return;
		}
		try {
			BasicConfigurator.configure();
		} catch (Exception e) {
		}
		push(args[0], args[1], args[2]);
	}
	
	public static void push(String path, String pass, String token) {
		try {
			
			
			// Get PushNotification Instance
			PushNotificationManager pushManager = new PushNotificationManager();
			// Link iPhone's UDID (64-char device token) to a stringName 
			pushManager.addDevice("my_iPhone", token);
			
			// Create a simple PayLoad with a simple alert
			PayLoad simplePayLoad = new PayLoad();
			simplePayLoad.addAlert("Hello World!");
			
			
			Device client = pushManager.getDevice("my_iPhone");
			AppleNotificationServer server = new AppleNotificationServerBasicImpl(path, pass, ConnectionToAppleServer.KEYSTORE_TYPE_PKCS12, PUSH_SERVER_HOST, PUSH_SERVER_PORT);
			
			System.out.println("Initializing SSL connection:");
			pushManager.initializeConnection(server);
			
			System.out.println("Pushing notification:");
			pushManager.sendNotification(client, simplePayLoad);
			
			System.out.println("Done.");

		} catch (Exception e) {
			System.out.println("Error pushing notification:");
			e.printStackTrace();
		}
	}
	
}
