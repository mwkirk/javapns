package javapns.notification.transmission;

import java.util.*;

import javapns.devices.*;
import javapns.notification.*;

/**
 * <h1>Pushes a payload to a large number of devices in a single separate thread</h1>
 * 
 * <p>The list of devices is divided into groups no larger than {@link maxNotificationsPerConnection},
 * and a connection is created for each group sequentially.  This is intended to avoid a vague
 * notification-per-connection limit observed occasionnally with Apple servers.</p>
 * 
 * <p>Usage: once a NotificationThread is created, invoke {@code start()} to push the payload to all devices in a separate thread.</p>
 * 
 * <p>To run this code unthreaded, invoke {@code run()} directly (rarely used).</p>
 * 
 * @author Sylvain Pedneault
 */
public class NotificationThread extends Thread {

	private static final int DEFAULT_MAXNOTIFICATIONSPERCONNECTION = 200;

	private PushNotificationManager notificationManager;
	private AppleNotificationServer server;
	private Payload payload;
	private List<Device> devices;
	private int maxNotificationsPerConnection = DEFAULT_MAXNOTIFICATIONSPERCONNECTION;
	private long sleepBetweenNotifications = 0;


	public NotificationThread(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<Device> devices) {
		super("javapns notification thread (" + devices.size() + ")");
		this.notificationManager = notificationManager;
		this.server = server;
		this.payload = payload;
		this.devices = devices;
	}


	public NotificationThread(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, Device... devices) {
		this(notificationManager, server, payload, Arrays.asList(devices));
	}


	public void run() {
		int total = devices.size();
		int runs = (total % maxNotificationsPerConnection) + 1;
		try {
			for (int i = 0; i < runs; i++) {
				int firstDevice = i * maxNotificationsPerConnection;
				int lastDevice = firstDevice + maxNotificationsPerConnection;
				if (lastDevice >= total) lastDevice = total - 1;
				notificationManager.initializeConnection(server);
				for (int d = firstDevice; d <= lastDevice; d++) {
					Device device = devices.get(d);
					notificationManager.sendNotification(device, payload, false);
					if (sleepBetweenNotifications > 0) sleep(sleepBetweenNotifications);
				}
				notificationManager.stopConnection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void setMaxNotificationsPerConnection(int maxNotificationsPerConnection) {
		this.maxNotificationsPerConnection = maxNotificationsPerConnection;
	}


	public int getMaxNotificationsPerConnection() {
		return maxNotificationsPerConnection;
	}


	public void setSleepBetweenNotifications(long sleepBetweenNotifications) {
		this.sleepBetweenNotifications = sleepBetweenNotifications;
	}


	public long getSleepBetweenNotifications() {
		return sleepBetweenNotifications;
	}

}
