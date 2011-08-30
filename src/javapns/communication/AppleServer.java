package javapns.communication;

import java.io.*;

/**
 * Common interface of all classes representing a connection to any Apple server.
 * Use AppleNotificationServer and AppleFeedbackServer interfaces for specific connections.
 * 
 * @author Sylvain Pedneault
 */
public interface AppleServer {

	public InputStream getKeystoreStream();


	public String getKeystorePassword();


	public String getKeystoreType();

}
