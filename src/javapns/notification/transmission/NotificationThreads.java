package javapns.notification.transmission;

import java.util.*;

import javapns.devices.*;
import javapns.notification.*;

/**
 * <h1>Pushes a payload to a large number of devices using multiple threads</h1>
 * 
 * <p>The list of devices is spread into multiple {@link javapns.notification.transmission.NotificationThread}s.</p>
 * 
 * <p>Usage: once a NotificationThreads is created, invoke {@code start()} to start all {@link javapns.notification.transmission.NotificationThread} threads.</p>
 * 
 * @author Sylvain Pedneault
 */
public class NotificationThreads {

	private List<NotificationThread> threads = new Vector<NotificationThread>();


	public NotificationThreads(PushNotificationManager notificationManager, AppleNotificationServer server, Payload payload, List<Device> devices, int threads) {
		int total = devices.size();
		int devicesPerThread = (total % threads) + 1;
		for (int i = 0; i < threads; i++) {
			int firstDevice = i * devicesPerThread;
			int lastDevice = firstDevice + devicesPerThread;
			if (lastDevice >= total) lastDevice = total - 1;
			List<Device> threadDevices = devices.subList(firstDevice, lastDevice + 1);
			NotificationThread thread = new NotificationThread(notificationManager, server, payload, threadDevices);
			this.threads.add(thread);
		}
	}


	public List<NotificationThread> getThreads() {
		return threads;
	}


	public void start() {
		for (NotificationThread thread : threads)
			thread.run();
	}

}
