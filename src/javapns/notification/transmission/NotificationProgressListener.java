package javapns.notification.transmission;

public interface NotificationProgressListener {

	public void eventAllThreadsStarted(NotificationThreads notificationThreads);

	public void eventThreadStarted(NotificationThread notificationThread);

	public void eventThreadFinished(NotificationThread notificationThread);

	public void eventConnectionRestarted(NotificationThread notificationThread);

	public void eventAllThreadsFinished(NotificationThreads notificationThreads);

}
