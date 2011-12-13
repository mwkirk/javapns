package javapns.communication;

import java.io.*;

import javapns.communication.exceptions.*;

/**
 * Common interface of all classes representing a connection to any Apple server.
 * Use AppleNotificationServer and AppleFeedbackServer interfaces for specific connections.
 * 
 * @author Sylvain Pedneault
 */
public interface AppleServer {

	/**
	 * Returns a stream to a keystore.
	 * @return an InputStream
	 */
	public InputStream getKeystoreStream() throws InvalidKeystoreReferenceException;


	/**
	 * Returns the keystore's password.
	 * @return a password matching the keystore
	 */
	public String getKeystorePassword();


	/**
	 * Returns the format used to produce the keystore (typically PKCS12).
	 * @return a valid keystore format identifier
	 */
	public String getKeystoreType();


	/**
	 * Get the proxy host address currently configured.
	 * @return a proxy host, or null if none is configured
	 */
	public String getProxyHost();


	/**
	 * Get the proxy port currently configured.
	 * @return a network port, or 0 if no proxy is configured
	 */
	public int getProxyPort();


	/**
	 * Configure a proxy to use
	 * @param proxyHost proxy host address
	 * @param proxyPort proxy host port
	 */
	public void setProxy(String proxyHost, int proxyPort);



}
