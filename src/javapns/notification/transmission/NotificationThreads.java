package javapns.notification.transmission;

import java.util.*;

import javapns.devices.*;
import javapns.notification.*;

/**
 * <h1>Pushes a payload to a large number of devices using multiple threads</h1>
 * 
 * <p>The list of devices is spread evenly into multiple {@link javapns.notification.transmission.NotificationThread}s.</p>
 * 
 * <p>Usage: once a NotificationThreads is created, invoke {@code start()} to start all {@link javapns.notification.transmission.NotificationThread} threads.</p>
 * <p>You can provide a {@link javapns.notification.transmission.NotificationProgressListener} to receive events about the work being done.</p>

 * @author Sylvain Pedneault
 */
public class NotificationThreads extends ThreadGroup {

	private List<NotificationThread> threads = new Vector<NotificationThread>();
	private NotificationProgressListener listener;
	private int threadsRunning = 0;


	/**
	 * Create the specified number of notification threads and spreads the devices evenly between the threads.
	 * 
	 * @param notificationManager a notification manager
	 * @param server the server to push to
	 * @param payload the payload to push
	 * @param devices a very large list of devices
	 * @param numberOfThreads the number of threads to create to share the work
	 */
	public NotificationThreads(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<Device> devices, int numberOfThreads) {
		super("javapns notification threads (" + numberOfThreads + " threads)");
		for (List<Device> deviceGroup : groupDevices(devices, numberOfThreads))
			threads.add(new NotificationThread(notificationManager, server, payload, deviceGroup));
	}


	/**
	 * Spread the devices evenly between the provided threads.
	 * 
	 * @param notificationManager a notification manager
	 * @param server the server to push to
	 * @param payload the payload to push
	 * @param devices a very large list of devices
	 * @param threads a list of pre-built threads
	 */
	public NotificationThreads(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<Device> devices, List<NotificationThread> threads) {
		super("javapns notification threads (" + threads.size() + " threads)");
		this.threads = threads;
		List<List<Device>> groups = groupDevices(devices, threads.size());
		for (int i = 0; i < groups.size(); i++)
			threads.get(i).setDevices(groups.get(i));
	}


	/**
	 * Use the provided threads which should already each have their group of devices to work with.
	 * 
	 * @param notificationManager a notification manager
	 * @param server the server to push to
	 * @param payload the payload to push
	 * @param threads a list of pre-built threads
	 */
	public NotificationThreads(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<NotificationThread> threads) {
		super("javapns notification threads (" + threads.size() + " threads)");
		this.threads = threads;
	}


	/**
	 * Create group of devices ready to be dispatched to worker threads.
	 * 
	 * @param devices a large list of devices
	 * @param threads the number of threads to group devices for
	 * @return
	 */
	private List<List<Device>> groupDevices(List<Device> devices, int threads) {
		List<List<Device>> groups = new Vector<List<Device>>();
		int total = devices.size();
		int devicesPerThread = (total / threads);
		if (total % threads > 0) devicesPerThread++;
		for (int i = 0; i < threads; i++) {
			int firstDevice = i * devicesPerThread;
			int lastDevice = firstDevice + devicesPerThread;
			if (lastDevice >= total) lastDevice = total - 1;
			List<Device> threadDevices = devices.subList(firstDevice, lastDevice + 1);
			groups.add(threadDevices);
		}
		return groups;
	}


	/**
	 * Start all notification threads.
	 */
	public synchronized void start() {
		if (threadsRunning > 0) throw new IllegalStateException("NotificationThreads already started (" + threadsRunning + " still running)");
		for (NotificationThread thread : threads) {
			threadsRunning++;
			thread.run();
		}
		if (listener != null) listener.eventAllThreadsStarted(this);
	}


	/**
	 * Configure in all threads the maximum number of notifications per connection.
	 * 
	 * As soon as a thread reaches that maximum, it will automatically close the connection,
	 * initialize a new connection and continue pushing more notifications.
	 * 
	 * @param notifications the maximum number of notifications that threads will push in a single connection (default is 200)
	 */
	public void setMaxNotificationsPerConnection(int notifications) {
		for (NotificationThread thread : threads)
			thread.setMaxNotificationsPerConnection(notifications);
	}


	/**
	 * Configure the number of milliseconds that threads should wait between each notification.
	 * 
	 * This feature is intended to alleviate intense resource usage that can occur when
	 * sending large quantities of notifications very quickly.

	 * @param milliseconds the number of milliseconds threads should sleep between individual notifications (default is 0)
	 */
	public void setSleepBetweenNotifications(long milliseconds) {
		for (NotificationThread thread : threads)
			thread.setSleepBetweenNotifications(milliseconds);
	}


	public List<NotificationThread> getThreads() {
		return threads;
	}


	public NotificationProgressListener getListener() {
		return listener;
	}


	public void setListener(NotificationProgressListener listener) {
		this.listener = listener;
		for (NotificationThread thread : threads)
			thread.setListener(listener);
	}


	public void threadFinished(NotificationThread notificationThread) {
		threadsRunning--;
		if (threadsRunning == 0 && listener != null) listener.eventAllThreadsFinished(this);
	}

}
