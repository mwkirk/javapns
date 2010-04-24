package javapns.test;

import java.util.LinkedList;
import java.util.ListIterator;

import javapns.back.FeedbackServiceManager;
import javapns.back.SSLConnectionHelper;
import javapns.data.Device;

public class FeedbackTest {

	// APNs Server Host & port
	private static final String HOST = "feedback.push.apple.com";
	private static final int PORT = 2196;

	// Vars that will be set from system.properties
	private static String certificate = "/tmp/HereMePushProd.p12";
	private static String passwd = "here123";

	public static void main( String[] args ) throws Exception {

		System.out.println( "Setting up Push notification" );

		try {	

			// Get PushNotification Instance
			FeedbackServiceManager feedbackManager = FeedbackServiceManager.getInstance();

			// Initialize connection
			LinkedList<Device> list = feedbackManager.getDevices( HOST, PORT, certificate, passwd, SSLConnectionHelper.KEYSTORE_TYPE_PKCS12 );
			System.out.println( "Connection initialized..." );

			ListIterator<Device> itr = list.listIterator();

			while( itr.hasNext() ) {
				Device device = itr.next();
				System.out.println( "Device: id=[" + device.getId() + " token=[" + device.getToken() + "]" );
			}

			System.out.println( "done" );

		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
}