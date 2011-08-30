package javapns.test;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;

import javapns.communication.*;
import javapns.devices.*;
import javapns.feedback.*;

public class FeedbackTest {

	private static final String FEEDBACK_SERVER_HOST = "feedback.push.apple.com";
	private static final int FEEDBACK_SERVER_PORT = 2196;
	private static final String KEYSTORE_FILE_PATH = "/tmp/HereMePushProd.p12";
	private static final String KEYSTORE_PASSWORD = "here123";


	public static void main(String[] args) throws Exception {
		try {
			BasicConfigurator.configure();
		} catch (Exception e) {
		}

		String keystorePath = args != null && args.length >= 1 ? args[0] : KEYSTORE_FILE_PATH;
		String keystorePassword = args != null && args.length >= 2 ? args[1] : KEYSTORE_PASSWORD;

		feedback(keystorePath, keystorePassword);
	}


	private static void feedback(String keystorePath, String keystorePassword) {
		System.out.println("Setting up feedback request...");
		System.out.println("Using keystore: "+new File(keystorePath).getAbsolutePath());
		System.out.println(" with password: "+keystorePassword);

		try {

			// Get FeedbackServiceManager Instance
			FeedbackServiceManager feedbackManager = new FeedbackServiceManager();

			// Initialize connection
			AppleFeedbackServer server = new AppleFeedbackServerBasicImpl(keystorePath, keystorePassword, ConnectionToAppleServer.KEYSTORE_TYPE_PKCS12, FEEDBACK_SERVER_HOST, FEEDBACK_SERVER_PORT);

			System.out.println("Connection initialized...");
			LinkedList<Device> list = feedbackManager.getDevices(server);
			System.out.println("List is: "+list);

			ListIterator<Device> itr = list.listIterator();

			while (itr.hasNext()) {
				Device device = itr.next();
				System.out.println("Device: id=[" + device.getDeviceId() + " token=[" + device.getToken() + "]");
			}

			System.out.println("done");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}