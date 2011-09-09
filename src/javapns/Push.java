package javapns;

import java.util.*;

import javapns.devices.*;
import javapns.devices.implementations.basic.*;
import javapns.feedback.*;
import javapns.notification.*;

/**
 * Main class for easily interacting with the Apple Push Notification System.
 * 
 * This class uses basic (non-persistent) implementations of Device, AppleNotificationServer and AppleFeedbackServer.
 * To add persistence to these objects, see documentation about implementing those classes for JPA.
 * 
 * @author Sylvain Pedneault
 */
public class Push {

	/**
	 * Push a simple alert to one or more devices.
	 * 
	 * @param message The alert message to push.
	 * @param keystore A PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password The keystore's password.
	 * @param production True to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens One or more device tokens to push to.
	 * @return Returns a list of devices the notification was presumably sent to (see Feedback Service).
	 */
	public static List<Device> alert(String message, Object keystore, String password, boolean production, String... tokens) {
		return payload(PushNotificationPayload.alert(message), keystore, password, production, tokens);
	}


	/**
	 * Push a simple badge number to one or more devices.
	 * 
	 * @param badge The badge number to push.
	 * @param keystore A PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password The keystore's password.
	 * @param production True to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens One or more device tokens to push to.
	 * @return Returns a list of devices the notification was presumably sent to (see Feedback Service).
	 */
	public static List<Device> badge(int badge, Object keystore, String password, boolean production, String... tokens) {
		return payload(PushNotificationPayload.badge(badge), keystore, password, production, tokens);
	}


	/**
	 * Push a simple sound name to one or more devices.
	 * 
	 * @param sound The sound name (stored in the client app) to push.
	 * @param keystore A PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password The keystore's password.
	 * @param production True to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens One or more device tokens to push to.
	 * @return Returns a list of devices the notification was presumably sent to (see Feedback Service).
	 */
	public static List<Device> sound(String sound, Object keystore, String password, boolean production, String... tokens) {
		return payload(PushNotificationPayload.sound(sound), keystore, password, production, tokens);
	}


	/**
	 * Push a notification combining an alert, a badge and a sound. 
	 * 
	 * @param message The alert message to push (set to null to skip).
	 * @param badge The badge number to push (set to -1 to skip).
	 * @param sound The sound name to push (set to null to skip).
	 * @param keystore A PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password The keystore's password.
	 * @param production True to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens One or more device tokens to push to.
	 * @return Returns a list of devices the notification was presumably sent to (see Feedback Service).
	 */
	public static List<Device> combined(String message, int badge, String sound, Object keystore, String password, boolean production, String... tokens) {
		return payload(PushNotificationPayload.sound(sound), keystore, password, production, tokens);
	}


	/**
	 * Push a preformatted payload.
	 * This is a convenience method for passing a List of tokens instead of an array.
	 * 
	 * @param payload A simple or complex payload to push.
	 * @param keystore A PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password The keystore's password.
	 * @param production True to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens One or more device tokens to push to.
	 * @return Returns a list of devices the notification was presumably sent to (see Feedback Service).
	 */
	public static List<Device> payload(Payload payload, Object keystore, String password, boolean production, List<String> tokens) {
		return payload(payload, keystore, password, production, tokens.toArray(new String[0]));
	}


	/**
	 * Push a preformatted payload.
	 * 
	 * @param payload A simple or complex payload to push.
	 * @param keystore A PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password The keystore's password.
	 * @param production True to use Apple's production servers, false to use the sandbox servers.
	 * @param tokens One or more device tokens to push to.
	 * @return Returns a list of devices the notification was presumably sent to (see Feedback Service).
	 */
	public static List<Device> payload(Payload payload, Object keystore, String password, boolean production, String... tokens) {
		List<Device> devices = new Vector<Device>();
		if (payload==null) return devices;
		try {
			PushNotificationManager pushManager = new PushNotificationManager();
			AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
			pushManager.initializeConnection(server);
			for (String token : tokens) {
				Device device = new BasicDevice(token);
				pushManager.sendNotification(device, payload, false);
				devices.add(device);
			}
			pushManager.stopConnection();
		} catch (Exception e) {
			System.out.println("Error pushing notification(s):");
			e.printStackTrace();
		}
		return devices;
	}


	/**
	 * <p>Retrieve a list of devices that should be removed from future notification lists.</p>
	 * 
	 * <p>Devices in this list are ones that you previously tried to push a notification to,
	 * but to which Apple could not actually deliver because the device user has either
	 * opted out of notifications, has uninstalled your application, or some other conditions.</p>
	 * 
	 * <p>Important: Apple's Feedback Service always resets its list of inactive devices
	 * after each time you contact it.  Calling this method twice will not return the same
	 * list of devices!</p>
	 * 
	 * <p>Please be aware that Apple does not specify precisely when a device will be listed
	 * by the Feedback Service.  More specifically, it is unlikely that the device will
	 * be  listed immediately if you uninstall the application during testing.  It might
	 * get listed after some number of notifications couldn't reach it, or some amount of
	 * time has elapsed, or a combination of both.</p>
	 * 
	 * @param keystore A PKCS12 keystore provided by Apple (File, InputStream, byte[] or String for a file path)
	 * @param password The keystore's password.
	 * @param production True to use Apple's production servers, false to use the sandbox servers.
	 * @return Returns a list of devices that are inactive.
	 */
	public static List<Device> feedback(Object keystore, String password, boolean production) {
		List<Device> devices = new Vector<Device>();
		try {
			FeedbackServiceManager feedbackManager = new FeedbackServiceManager();
			AppleFeedbackServer server = new AppleFeedbackServerBasicImpl(keystore, password, production);
			devices.addAll(feedbackManager.getDevices(server));
		} catch (Exception e) {
			System.out.println("Error pushing notification(s):");
			e.printStackTrace();
		}
		return devices;
	}

}
