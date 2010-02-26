package javapns.back;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javapns.data.Device;
import javapns.data.PayLoad;
import javapns.exceptions.DuplicateDeviceException;
import javapns.exceptions.NullDeviceTokenException;
import javapns.exceptions.NullIdException;
import javapns.exceptions.UnknownDeviceException;

import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

/**
 * The main class used to send notification and handle a connection to Apple SSLServerSocket
 * @author Maxime Peron
 *
 */
public class PushNotificationManager {
	
    protected static final Logger logger = Logger.getLogger( PushNotificationManager.class );

	/* Default retries for a connection */
	public static final int DEFAULT_RETRIES = 3;
	
	/* Singleton pattern */
	private static PushNotificationManager instance;
	
	/* Connection helper */
	private SSLConnectionHelper connectionHelper;
	
	/* The always connected SSLSocket */
	private SSLSocket socket;

	/* Default retry attempts */
	private int retryAttempts = DEFAULT_RETRIES;
	
	/**
	 * Singleton pattern implementation
	 * @return the PushNotificationManager instance
	 */
	public static PushNotificationManager getInstance(){
		if (instance == null){
			instance = new PushNotificationManager();
		}
		return instance;
	}

	/**
	 * Private constructor
	 */
	private PushNotificationManager(){}
	
	/**
	 * Initialize the connection and create a SSLSocket
	 * @param appleHost the Apple ServerSocket host
	 * @param applePort the Apple ServerSocket port
	 * @param keyStorePath the path to the keystore
	 * @param keyStorePass the keystore password
	 * @param keyStoreType the keystore type
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchProviderException 
	 */
	public void initializeConnection(String appleHost, int applePort, String keyStorePath, String keyStorePass, String keyStoreType) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, NoSuchProviderException{
		logger.debug( "Initializing Connection to Host: [" + appleHost + "] Port: [" + applePort + "] with KeyStorePath [" + keyStorePath + "]/[" + keyStoreType + "]" );
		this.connectionHelper = new SSLConnectionHelper(appleHost, applePort, keyStorePath, keyStorePass, keyStoreType);
		this.socket = connectionHelper.getSSLSocket();
	}
	
	/**
	 * Initialize the connection and create a SSLSocket
	 * @param appleHost the Apple ServerSocket host
	 * @param applePort the Apple ServerSocket port
	 * @param keyStoreStream the keystore stream
	 * @param keyStorePass the keystore password
	 * @param keyStoreType the keystore type
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchProviderException 
	 */
	public void initializeConnection(String appleHost, int applePort, InputStream keyStoreStream, String keyStorePass, String keyStoreType) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, NoSuchProviderException{
		logger.debug( "Initializing Connection to Host: [" + appleHost + "] Port: [" + applePort + "] with KeyStoreStream/[" + keyStoreType + "]" );
		this.connectionHelper = new SSLConnectionHelper(appleHost, applePort, keyStoreStream, keyStorePass, keyStoreType);
		this.socket = connectionHelper.getSSLSocket();
	}
	
	/**
	 * Close the SSLSocket connection
	 * @throws IOException
	 */
	public void stopConnection() throws IOException{
		logger.debug( "Closing connection" );
		this.socket.close();
	}

	/**
	 * Send a notification (Payload) to the given device
	 * @param device the device to be notified
	 * @param payload the payload to send
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void sendNotification(Device device, PayLoad payload) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, Exception {
		byte[] message = getMessage(device.getToken(), payload);
		boolean success = false;
		
		int attempts = 0;
		// Keep trying until we have a success
		while( !success ){
			try {
				logger.debug( "Attempting to send Notification [" + payload.toString() + "]" );
				attempts++;
				this.socket.getOutputStream().write(message);
				this.socket.getOutputStream().flush();
				success = true;
				logger.debug( "Notification sent" );
				
			} catch (IOException e) {
				// throw exception if we surpassed the valid number of retry attempts
				if ( attempts >= retryAttempts ){
					logger.error( "Attempt to send Notification failed and beyond the maximum number of attempts permitted" );
					throw e;
					
				} else {
					logger.info( "Attempt failed... trying again" );
					//Try again
					try{
						this.socket.close();
					} catch( Exception e2 ){
						// do nothing
					}
					//e.printStackTrace();
					this.socket = connectionHelper.getSSLSocket();
				}
			}
		}
	}

	/**
	 * Add a device
	 * @param id The device id
	 * @param token The device token
	 * @throws DuplicateDeviceException
	 * @throws NullDeviceTokenException 
	 * @throws NullIdException 
	 */
	public void addDevice(String id, String token) throws DuplicateDeviceException, NullIdException, NullDeviceTokenException{
		logger.debug( "Adding Token [" + token + "] to Device [" + id + "]" );
		DeviceFactory.getInstance().addDevice(id, token);
	}

	/**
	 * Get a device according to his id
	 * @param id The device id
	 * @return The device
	 * @throws UnknownDeviceException
	 * @throws NullIdException 
	 */
	public Device getDevice(String id) throws UnknownDeviceException, NullIdException{
		logger.debug( "Getting Token from Device [" + id + "]" );
		return DeviceFactory.getInstance().getDevice(id);
	}

	/**
	 * Remove a device
	 * @param id The device id
	 * @throws UnknownDeviceException
	 * @throws NullIdException
	 */
	public void removeDevice(String id) throws UnknownDeviceException, NullIdException{
		logger.debug( "Removing Token from Device [" + id + "]" );
		DeviceFactory.getInstance().removeDevice(id);
	}
	
	/**
	 * Set the proxy if needed
	 * @param host the proxyHost
	 * @param port the proxyPort
	 */
	public void setProxy(String host, String port){
		System.setProperty("http.proxyHost", host);
		System.setProperty("http.proxyPort", port);

		System.setProperty("https.proxyHost", host);
		System.setProperty("https.proxyPort", port);
	}

//	public List<Device> checkFeedback(String appleHost, int applePort, String keyStorePath, String keyStorePass, String keyStoreType) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException{
//		SSLConnectionHelper feedbackConnHelper = new SSLConnectionHelper(appleHost, applePort, keyStorePath, keyStorePass, keyStoreType);
//		SSLSocket feedbackSocket = feedbackConnHelper.getSSLSocket();
//		
//		try{
//			BufferedReader stdIn = new BufferedReader(new InputStreamReader(feedbackSocket.getInputStream()));
//			String feedbackMessage;
//			while ((feedbackMessage = stdIn.readLine()) != null) {
//				System.out.println("echo: " + feedbackMessage);
//				//TODO process message
//			}
//		}finally{
//			feedbackSocket.close();	
//		}
//		
//		//FIXME we also need to return the time of the failure	
//		return new ArrayList<Device>();
//	}
	
	/**
	 * Compose the Raw Interface that will be sent through the SSLSocket
	 * A notification message is
	 * COMMAND | TOKENLENGTH | DEVICETOKEN | PAYLOADLENGTH | PAYLOAD
	 * See page 30 of Apple Push Notification Service Programming Guide
	 * @param deviceToken the deviceToken
	 * @param payload the payload
	 * @return the byteArray to write to the SSLSocket OutputStream
	 * @throws IOException
	 */
	private static byte[] getMessage(String deviceToken, PayLoad payload) throws IOException, Exception {
		logger.debug( "Building Raw message from deviceToken and payload" );
		// First convert the deviceToken (in hexa form) to a binary format
		byte[] deviceTokenAsBytes = new byte[deviceToken.length() / 2];
		deviceToken = deviceToken.toUpperCase();
		int j = 0;
		for (int i = 0; i < deviceToken.length(); i+=2) {
			String t = deviceToken.substring(i, i+2);
			int tmp = Integer.parseInt(t, 16);
			deviceTokenAsBytes[j++] = (byte)tmp;
		}

		// Create the ByteArrayOutputStream which will contain the raw interface
		int size = (Byte.SIZE/Byte.SIZE) + (Character.SIZE/Byte.SIZE) + deviceTokenAsBytes.length + (Character.SIZE/Byte.SIZE) + payload.getPayloadAsBytes().length; 
		ByteArrayOutputStream bao = new ByteArrayOutputStream(size);

		// Write command to ByteArrayOutputStream
		byte b = 0;
		bao.write(b);

		// Write the TokenLength as a 16bits unsigned int, in big endian
		int tl = deviceTokenAsBytes.length;
		bao.write((byte) (tl & 0xFF00) >> 8);
		bao.write((byte) (tl & 0xFF));

		// Write the Token in bytes
		bao.write(deviceTokenAsBytes);

		// Write the PayloadLength as a 16bits unsigned int, in big endian
		int pl = payload.getPayloadAsBytes().length;
		bao.write((byte) (pl & 0xFF00) >> 8);
		bao.write((byte) (pl & 0xFF));

		// Finally write the Payload
		bao.write(payload.getPayloadAsBytes());

		// Return the ByteArrayOutputStream as a Byte Array
		return bao.toByteArray();
	}

	/**
	 * Get the number of retry attempts
	 * @return
	 */
	public int getRetryAttempts() {
		return this.retryAttempts;
	}

	/**
	 * Set the number of retry attempts
	 * @param retryAttempts
	 */
	public void setRetryAttempts(int retryAttempts) {
		this.retryAttempts = retryAttempts;
	}
}
