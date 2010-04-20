package javapns.test;

import java.util.ArrayList;

import javapns.back.PushNotificationManager;
import javapns.back.SSLConnectionHelper;
import javapns.data.Device;
import javapns.data.PayLoad;
import javapns.data.PayLoadCustomAlert;

public class Test {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try { 
			// Get PushNotification Instance
			PushNotificationManager pushManager = PushNotificationManager.getInstance();
			// Link iPhone's UDID (64-char device token) to a stringName 
			pushManager.addDevice("my_iPhone", "2ed202ac08ea9033665d853a3dc8bc4c5e78f7c6cf8d55910df290567037dcc4");
			
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
			
			Device client = PushNotificationManager.getInstance().getDevice("my_iPhone");
//			PushNotificationManager.getInstance().setProxy("my_proxy_host", "my_proxy_port");
			//PushNotificationManager.getInstance().initializeConnection("gateway.sandbox.push.apple.com", 2195, "my_cert_path", "my_cert_password", SSLConnectionHelper.KEYSTORE_TYPE_PKCS12);
			PushNotificationManager.getInstance().initializeConnection("gateway.sandbox.push.apple.com", 2195, "/Volumes/HereMe/projects/hereme-server/src/com/hereme/helper/HereMe_Development_Push_Cert_Jan_21.p12", "here123", SSLConnectionHelper.KEYSTORE_TYPE_PKCS12);
			PushNotificationManager.getInstance().sendNotification(client, simplePayLoad);
//			PushNotificationManager.getInstance().sendNotification(client, complexPayLoad);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}