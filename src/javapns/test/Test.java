package javapns.test;

import javapns.back.PushNotificationManager;
import javapns.back.SSLConnectionHelper;
import javapns.data.Device;
import javapns.data.PayLoad;

public class Test {

	public static void main(String[] args) {
		try { 
			PayLoad payload = new PayLoad("Hello World", "1", "default");
			Device client = PushNotificationManager.getInstance().getDevice("my_iPhone");
//			PushNotificationManager.getInstance().setProxy("my_proxy_host", "my_proxy_port");
			PushNotificationManager.getInstance().initializeConnection("gateway.sandbox.push.apple.com", 2195, "my_cert_path", "my_cert_password", SSLConnectionHelper.KEYSTORE_TYPE_PKCS12);
			PushNotificationManager.getInstance().sendNotification(client, payload);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
