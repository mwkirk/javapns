package javapns.notification.transmission;

import java.util.*;

import javapns.devices.*;
import javapns.notification.*;

/**
 * <h1>Pushes a payload to a large number of devices in a single separate thread</h1>
 * 
 * <p>No more than {@code maxNotificationsPerConnection} are pushed over a single connection.
 * When that maximum is reached, the connection is restarted automatically and push continues.
 * This is intended to avoid an undocumented notification-per-connection limit observed 
 * occasionnally with Apple servers.</p>
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
	private NotificationProgressListener listener;


	public NotificationThread(NotificationThreads threads, PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<Device> devices) {
		super(threads, "javapns notification thread (" + devices.size() + ")");
		this.notificationManager = notificationManager;
		this.server = server;
		this.payload = payload;
		this.devices = devices;
	}


	public NotificationThread(NotificationThreads threads, PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, Device... devices) {
		this(null, notificationManager, server, payload, Arrays.asList(devices));
	}


	public NotificationThread(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<Device> devices) {
		this(null, notificationManager, server, payload, devices);
	}


	public NotificationThread(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, Device... devices) {
		this(notificationManager, server, payload, Arrays.asList(devices));
	}


	public void run() {
		int total = devices.size();
		if (listener != null) listener.eventThreadStarted(this);
		try {
			notificationManager.initializeConnection(server);
			for (int i = 0; i < total; i++) {
				Device device = devices.get(i);
				notificationManager.sendNotification(device, payload, false);
				if (sleepBetweenNotifications > 0) sleep(sleepBetweenNotifications);
				if (i != 0 && i % maxNotificationsPerConnection == 0) {
					if (listener != null) listener.eventConnectionRestarted(this);
					notificationManager.restartConnection(server);
				}
			}
			notificationManager.stopConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (listener != null) listener.eventThreadFinished(this);
		/* Also notify the parent NotificationThreads, so that it can determine when all threads have finished working */
		if (getThreadGroup() instanceof NotificationThreads) ((NotificationThreads) getThreadGroup()).threadFinished(this);
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


	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}


	public List<Device> getDevices() {
		return devices;
	}


	public void setListener(NotificationProgressListener listener) {
		this.listener = listener;
	}


	public NotificationProgressListener getListener() {
		return listener;
	}

}
