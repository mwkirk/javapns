package javapns.notification;

import javapns.communication.*;

/**
 * Interface representing a connection to an Apple Notification Server
 * 
 * @author Sylvain Pedneault
 */
public interface AppleNotificationServer extends AppleServer {

	public String getNotificationServerHost();


	public int getNotificationServerPort();

}
