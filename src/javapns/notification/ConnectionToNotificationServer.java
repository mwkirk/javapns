package javapns.notification;

import javapns.communication.*;
import javapns.communication.exceptions.*;

/**
 * Connection details specific to the Notification Service.
 * 
 * @author Sylvain Pedneault
 */
public class ConnectionToNotificationServer extends ConnectionToAppleServer {

	public ConnectionToNotificationServer(AppleNotificationServer server) throws KeystoreException {
		super(server);
	}


	@Override
	public String getServerHost() {
		return ((AppleNotificationServer) getServer()).getNotificationServerHost();
	}


	@Override
	public int getServerPort() {
		return ((AppleNotificationServer) getServer()).getNotificationServerPort();
	}

}
