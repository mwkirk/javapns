package javapns.notification;

import javapns.communication.*;


public interface AppleNotificationServer extends AppleServer {

	public String getNotificationServerHost();
	
	public int getNotificationServerPort();
	
}
