package javapns.notification.transmission;

import java.util.*;

import javapns.communication.exceptions.*;
import javapns.devices.*;
import javapns.devices.exceptions.*;
import javapns.notification.*;

/**
 * <h1>Pushes payloads asynchroneously using a dedicated thread.</h1>
 * 
 * <p>A NotificationThread is created with one of two modes:  LIST or QUEUE.
 * In LIST mode, the thread is given a predefined list of devices and pushes all notifications as soon as it is started.  Its work is complete and the thread ends as soon as all notifications have been sent.
 * In QUEUE mode, the thread is started with no notification to send.  It opens a connection and waits for messages to be added to its queue using the addMessageToQueue(..) method.  This lifecyle is useful for creating connection pools.</p>

 * <p>No more than {@code maxNotificationsPerConnection} are pushed over a single connection.
 * When that maximum is reached, the connection is restarted automatically and push continues.
 * This is intended to avoid an undocumented notification-per-connection limit observed 
 * occasionnally with Apple servers.</p>
 * 
 * <p>Usage (LIST): once a NotificationThread is created using any LIST-mode constructor, invoke {@code start()} to push the payload to all devices in a separate thread.</p>
 * 
 * <p>Usage (QUEUE): once a NotificationThread is created using any QUEUE-mode constructor, invoke {@code start()} to push the payload to all devices in a separate thread.</p>
 * 
 * @author Sylvain Pedneault
 */
public class NotificationThread extends Thread {

	/**
	 * Working modes supported by Notification Threads.
	 */
	public static enum MODE {
		/**
		 * In LIST mode, the thread is given a predefined list of devices and pushes all notifications as soon as it is started.  Its work is complete and the thread ends as soon as all notifications have been sent.
		 */
		LIST,
		/**
		 * In QUEUE mode, the thread is started with no notification to send.  It opens a connection and waits for messages to be added to its queue using the addMessageToQueue(..) method.  This lifecyle is useful for creating connection pools.
		 */
		QUEUE
	};

	private static final int DEFAULT_MAXNOTIFICATIONSPERCONNECTION = 200;

	private PushNotificationManager notificationManager;
	private AppleNotificationServer server;
	private int maxNotificationsPerConnection = DEFAULT_MAXNOTIFICATIONSPERCONNECTION;
	private long sleepBetweenNotifications = 0;
	private NotificationProgressListener listener;
	private int threadNumber = 1;
	private int nextMessageIdentifier = 1;
	private List<PushedNotification> notifications = new Vector<PushedNotification>();
	private MODE mode = MODE.LIST;
	private boolean busy = false;

	/* Single payload to multiple devices */
	private Payload payload;
	private List<Device> devices;

	/* Individual payload per device */
	private List<PayloadPerDevice> messages = new Vector<PayloadPerDevice>();

	private Exception exception;


	/**
	 * Create a grouped thread in LIST mode for pushing a single payload to a list of devices
	 * and coordinating with a parent NotificationThreads object.
	 * 
	 * @param threads the parent NotificationThreads object that is coordinating multiple threads
	 * @param notificationManager the notification manager to use
	 * @param server the server to communicate with
	 * @param payload a payload to push
	 * @param devices a list of devices to push to
	 */
	public NotificationThread(NotificationThreads threads, PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<Device> devices) {
		super(threads, "javapns notification thread (" + devices.size() + ")");
		this.notificationManager = notificationManager == null ? new PushNotificationManager() : notificationManager;
		this.server = server;
		this.payload = payload;
		this.devices = devices;
	}


	/**
	 * Create a grouped thread in LIST mode for pushing a single payload to an array of devices
	 * and coordinating with a parent NotificationThreads object.
	 * 
	 * @param threads the parent NotificationThreads object that is coordinating multiple threads
	 * @param notificationManager the notification manager to use
	 * @param server the server to communicate with
	 * @param payload a payload to push
	 * @param devices a list of devices to push to
	 */
	public NotificationThread(NotificationThreads threads, PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, Device... devices) {
		this(threads, notificationManager, server, payload, Arrays.asList(devices));
	}


	/**
	 * Create a grouped thread in LIST mode for pushing individual payloads to a list of devices
	 * and coordinating with a parent NotificationThreads object.
	 * 
	 * @param threads the parent NotificationThreads object that is coordinating multiple threads
	 * @param notificationManager the notification manager to use
	 * @param server the server to communicate with
	 * @param messages the messages (payload/device pairs) to push
	 */
	public NotificationThread(NotificationThreads threads, PushNotificationManager notificationManager, AppleNotificationServer server, List<PayloadPerDevice> messages) {
		super(threads, "javapns notification thread (" + messages.size() + ")");
		this.notificationManager = notificationManager == null ? new PushNotificationManager() : notificationManager;
		this.server = server;
		this.messages = messages;
	}


	/**
	 * Create a grouped thread in LIST mode for pushing individual payloads to an array of devices
	 * and coordinating with a parent NotificationThreads object.
	 * 
	 * @param threads the parent NotificationThreads object that is coordinating multiple threads
	 * @param notificationManager the notification manager to use
	 * @param server the server to communicate with
	 * @param messages the messages (payload/device pairs) to push
	 */
	public NotificationThread(NotificationThreads threads, PushNotificationManager notificationManager, AppleNotificationServer server, PayloadPerDevice... messages) {
		this(threads, notificationManager, server, Arrays.asList(messages));
	}


	/**
	 * Create a standalone thread in LIST mode for pushing a single payload to a list of devices.
	 * 
	 * @param notificationManager the notification manager to use
	 * @param server the server to communicate with
	 * @param payload a payload to push
	 * @param devices a list of devices to push to
	 */
	public NotificationThread(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<Device> devices) {
		this(null, notificationManager, server, payload, devices);
	}


	/**
	 * Create a standalone thread in LIST mode for pushing a single payload to an array of devices.
	 * 
	 * @param notificationManager the notification manager to use
	 * @param server the server to communicate with
	 * @param payload a payload to push
	 * @param devices a list of devices to push to
	 */
	public NotificationThread(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, Device... devices) {
		this(notificationManager, server, payload, Arrays.asList(devices));
	}


	/**
	 * Create a standalone thread in LIST mode for pushing individual payloads to a list of devices.
	 * 
	 * @param notificationManager the notification manager to use
	 * @param server the server to communicate with
	 * @param messages the messages (payload/device pairs) to push
	 */
	public NotificationThread(PushNotificationManager notificationManager, AppleNotificationServer server, List<PayloadPerDevice> messages) {
		super("javapns notification thread (" + messages.size() + ")");
		this.notificationManager = notificationManager == null ? new PushNotificationManager() : notificationManager;
		this.server = server;
		this.messages = messages;
	}


	/**
	 * Create a grouped thread in QUEUE mode, awaiting messages to push.
	 * 
	 * @param threads the parent NotificationThreads object that is coordinating multiple threads
	 * @param notificationManager the notification manager to use
	 * @param server the server to communicate with
	 */
	public NotificationThread(NotificationThreads threads, PushNotificationManager notificationManager, AppleNotificationServer server) {
		super(threads, "javapns notification thread pool");
		this.notificationManager = notificationManager == null ? new PushNotificationManager() : notificationManager;
		this.server = server;
		this.mode = MODE.QUEUE;
		setDaemon(true);
	}


	/**
	 * Create a standalone thread in QUEUE mode, awaiting messages to push.
	 * 
	 * @param notificationManager the notification manager to use
	 * @param server the server to communicate with
	 */
	public NotificationThread(PushNotificationManager notificationManager, AppleNotificationServer server) {
		super("javapns notification thread");
		this.notificationManager = notificationManager == null ? new PushNotificationManager() : notificationManager;
		this.server = server;
		this.mode = MODE.QUEUE;
		setDaemon(true);
	}


	public void run() {
		switch (mode) {
			case LIST:
				runList();
				break;
			case QUEUE:
				runQueue();
				break;
			default:
				break;
		}
	}


	private void runList() {
		int total = size();
		if (listener != null) listener.eventThreadStarted(this);
		busy = true;
		try {
			notificationManager.initializeConnection(server);
			for (int i = 0; i < total; i++) {
				Device device;
				Payload payload;
				if (devices != null) {
					device = devices.get(i);
					payload = this.payload;
				} else {
					PayloadPerDevice message = messages.get(i);
					device = message.getDevice();
					payload = message.getPayload();
				}
				int message = newMessageIdentifier();
				PushedNotification notification = notificationManager.sendNotification(device, payload, false, message);
				notifications.add(notification);
				try {
					if (sleepBetweenNotifications > 0) sleep(sleepBetweenNotifications);
				} catch (InterruptedException e) {
				}
				if (i != 0 && i % maxNotificationsPerConnection == 0) {
					if (listener != null) listener.eventConnectionRestarted(this);
					notificationManager.restartConnection(server);
				}
			}
			notificationManager.stopConnection();
		} catch (KeystoreException e) {
			this.exception = e;
			if (listener != null) listener.eventCriticalException(this, e);
		} catch (CommunicationException e) {
			this.exception = e;
			if (listener != null) listener.eventCriticalException(this, e);
		}
		busy = false;
		if (listener != null) listener.eventThreadFinished(this);
		/* Also notify the parent NotificationThreads, so that it can determine when all threads have finished working */
		if (getThreadGroup() instanceof NotificationThreads) ((NotificationThreads) getThreadGroup()).threadFinished(this);
	}


	private void runQueue() {
		if (listener != null) listener.eventThreadStarted(this);
		try {
			notificationManager.initializeConnection(server);
			int notificationsPushed = 0;
			while (mode == MODE.QUEUE) {
				while (!messages.isEmpty()) {
					busy = true;
					PayloadPerDevice message = messages.get(0);
					messages.remove(message);
					notificationsPushed++;
					int messageId = newMessageIdentifier();
					PushedNotification notification = notificationManager.sendNotification(message.getDevice(), message.getPayload(), false, messageId);
					notifications.add(notification);
					try {
						if (sleepBetweenNotifications > 0) sleep(sleepBetweenNotifications);
					} catch (InterruptedException e) {
					}
					if (notificationsPushed != 0 && notificationsPushed % maxNotificationsPerConnection == 0) {
						if (listener != null) listener.eventConnectionRestarted(this);
						notificationManager.restartConnection(server);
					}
					busy = false;
				}
				try {
					sleep(10 * 1000);
				} catch (Exception e) {
				}
			}
			notificationManager.stopConnection();
		} catch (KeystoreException e) {
			this.exception = e;
			if (listener != null) listener.eventCriticalException(this, e);
		} catch (CommunicationException e) {
			this.exception = e;
			if (listener != null) listener.eventCriticalException(this, e);
		}
		if (listener != null) listener.eventThreadFinished(this);
		/* Also notify the parent NotificationThreads, so that it can determine when all threads have finished working */
		if (getThreadGroup() instanceof NotificationThreads) ((NotificationThreads) getThreadGroup()).threadFinished(this);
	}


	/**
	 * Add a message to this thread's queue.  The thread will pick it up and push it asynchroneously.
	 * 
	 * This method has no effect if the thread is not in QUEUE mode.
	 * 
	 * @param payload a payload
	 * @param token a device token
	 * @throws InvalidDeviceTokenFormatException 
	 */
	public void queue(Payload payload, String token) throws InvalidDeviceTokenFormatException {
		queue(new PayloadPerDevice(payload, token));
	}


	/**
	 * Add a message to this thread's queue.  The thread will pick it up and push it asynchroneously.
	 * 
	 * This method has no effect if the thread is not in QUEUE mode.
	 * 
	 * @param payload a payload
	 * @param device a device
	 */
	public void queue(Payload payload, Device device) {
		queue(new PayloadPerDevice(payload, device));
	}


	/**
	 * Add a message to this thread's queue.  The thread will pick it up and push it asynchroneously.
	 * 
	 * This method has no effect if the thread is not in QUEUE mode.
	 * 
	 * @param message a payload/device pair
	 */
	public void queue(PayloadPerDevice message) {
		if (mode != MODE.QUEUE) return;
		try {
			messages.add(message);
			this.interrupt();
		} catch (Exception e) {
		}
	}


	/**
	 * Set a maximum number of notifications that should be streamed over a continuous connection
	 * to an Apple server.  When that maximum is reached, the thread automatically closes and
	 * reopens a fresh new connection to the server and continues streaming notifications.
	 * 
	 * Default is 200 (recommended).
	 * 
	 * @param maxNotificationsPerConnection
	 */
	public void setMaxNotificationsPerConnection(int maxNotificationsPerConnection) {
		this.maxNotificationsPerConnection = maxNotificationsPerConnection;
	}


	public int getMaxNotificationsPerConnection() {
		return maxNotificationsPerConnection;
	}


	/**
	 * Set a delay the thread should sleep between each notification.
	 * This is sometimes useful when communication with Apple servers is
	 * unreliable and notifications are streaming too fast.
	 * 
	 * Default is 0.
	 * 
	 * @param milliseconds
	 */
	public void setSleepBetweenNotifications(long milliseconds) {
		this.sleepBetweenNotifications = milliseconds;
	}


	public long getSleepBetweenNotifications() {
		return sleepBetweenNotifications;
	}


	void setDevices(List<Device> devices) {
		this.devices = devices;
	}


	/**
	 * Get the list of devices associated with this thread.
	 * 
	 * @return a list of devices
	 */
	public List<Device> getDevices() {
		return devices;
	}


	/**
	 * Get the number of devices that this thread pushes to.
	 * 
	 * @return the number of devices registered with this thread
	 */
	public int size() {
		return devices != null ? devices.size() : messages.size();
	}


	/**
	 * Provide an event listener which will be notified of this thread's progress.
	 * 
	 * @param listener any object implementing the NotificationProgressListener interface
	 */
	public void setListener(NotificationProgressListener listener) {
		this.listener = listener;
	}


	public NotificationProgressListener getListener() {
		return listener;
	}


	/**
	 * Set the thread number so that generated message identifiers can be made 
	 * unique across all threads.
	 * 
	 * @param threadNumber
	 */
	protected void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}


	/**
	 * Return the thread number assigned by the parent NotificationThreads object, if any.
	 * 
	 * @return the unique number assigned to this thread by the parent group
	 */
	public int getThreadNumber() {
		return threadNumber;
	}


	/**
	 * Return a new sequential message identifier.
	 * 
	 * @return a message identifier unique to all NotificationThread objects
	 */
	public int newMessageIdentifier() {
		return (threadNumber << 24) | nextMessageIdentifier++;
	}


	/**
	 * Returns the first message identifier generated by this thread.
	 * 
	 * @return a message identifier unique to all NotificationThread objects
	 */
	public int getFirstMessageIdentifier() {
		return (threadNumber << 24) | 1;
	}


	/**
	 * Returns the last message identifier generated by this thread.
	 * 
	 * @return a message identifier unique to all NotificationThread objects
	 */
	public int getLastMessageIdentifier() {
		return (threadNumber << 24) | size();
	}


	/**
	 * Returns list of all notifications pushed by this thread (successful or not).
	 * 
	 * @return a list of pushed notifications
	 */
	public List<PushedNotification> getPushedNotifications() {
		return notifications;
	}


	/**
	 * Returns list of all notifications that this thread attempted to push but that failed.
	 * 
	 * @return a list of failed notifications
	 */
	public List<PushedNotification> getFailedNotifications() {
		return PushedNotification.findFailedNotifications(getPushedNotifications());
	}


	/**
	 * Returns list of all notifications that this thread attempted to push and succeeded.
	 * 
	 * @return a list of failed notifications
	 */
	public List<PushedNotification> getSuccessfulNotifications() {
		return PushedNotification.findSuccessfulNotifications(getPushedNotifications());
	}


	/**
	 * Set the messages associated with this thread.
	 * @param messages
	 */
	void setMessages(List<PayloadPerDevice> messages) {
		this.messages = messages;
	}


	/**
	 * Get the messages associated with this thread, if any.
	 * 
	 * @return messages
	 */
	public List<PayloadPerDevice> getMessages() {
		return messages;
	}


	/**
	 * Determine if this thread is busy.
	 * @return if the thread is busy or not
	 */
	public boolean isBusy() {
		return busy;
	}


	/**
	 * If this thread experienced a critical exception (communication error, keystore issue, etc.), this method returns the exception.
	 * @return a critical exception, if one occurred in this thread
	 */
	public Exception getCriticalException() {
		return exception;
	}

}
