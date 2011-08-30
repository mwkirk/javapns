package javapns.test;

import java.util.*;

import javapns.communication.*;
import javapns.devices.*;
import javapns.notification.*;

public class NotificationTest {

	private static final String PUSH_SERVER_HOST = "gateway.sandbox.push.apple.com";
	private static final int PUSH_SERVER_PORT = 2195;
	private static final String KEYSTORE_FILE_PATH = "/Volumes/HereMe/projects/hereme-server/src/com/hereme/helper/HereMe_Development_Push_Cert_Jan_21.p12";
	private static final String KEYSTORE_PASSWORD = "here123";
	private static final String DEVICE_TOKEN = "2ed202ac08ea9033665d853a3dc8bc4c5e78f7c6cf8d55910df290567037dcc4";

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try { 
			// Get PushNotification Instance
			PushNotificationManager pushManager = new PushNotificationManager();
			// Link iPhone's UDID (64-char device token) to a stringName 
			pushManager.addDevice("my_iPhone", DEVICE_TOKEN);
			
			// Create a simple PayLoad with a simple alert
			PayLoad simplePayLoad = new PayLoad();
			simplePayLoad.addAlert("My alert message");
			simplePayLoad.addBadge(49);
			simplePayLoad.addSound("default");
			
			// Or create a complex PayLoad with a custom alert
			PayLoad complexPayLoad = new PayLoad();
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
			
			Device client = pushManager.getDevice("my_iPhone");

			AppleNotificationServer server = new AppleNotificationServerBasicImpl(KEYSTORE_FILE_PATH, KEYSTORE_PASSWORD, ConnectionToAppleServer.KEYSTORE_TYPE_PKCS12, PUSH_SERVER_HOST, PUSH_SERVER_PORT);
			
			pushManager.initializeConnection(server);
			pushManager.sendNotification(client, simplePayLoad);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}