package javapns.back;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Logger;

import javapns.data.Device;

import javax.net.ssl.SSLSocket;

/**
 * An implementation of the feedback service (beta version)
 * @author kljajo, dgardon
 *
 */
public class FeedbackServiceManager {

	/* Singleton pattern */
	private static FeedbackServiceManager instance;
	
	/* Length of the tuple sent by Apple */
	private static final int FEEDBACK_TUPLE_SIZE = 38;
	
	/**
	 * Private constructor
	 */
	private FeedbackServiceManager(){}
	
	/**
	 * Singleton pattern implementation
	 * @return
	 */
	public static FeedbackServiceManager getInstance(){
		if (instance == null){
			instance = new FeedbackServiceManager();
		}
		return instance;
	}

	/**
	 * Retrieve all devices which uninstalled the application
	 * @param appleHost the Apple ServerSocket host
	 * @param applePort the Apple ServerSocket port
	 * @param keyStorePath the path to the keystore
	 * @param keyStorePass the keystore password
	 * @param keyStoreType the keystore type
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 * @throws KeyManagementException 
	 * @throws UnrecoverableKeyException 
	 */
	public LinkedList<Device> getDevices(String appleHost, int applePort, String keyStorePath, String keyStorePass, String keyStoreType) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException{
		// Create the connection and open a socket
        SSLConnectionHelper connectionHelper = new SSLConnectionHelper(appleHost, applePort, keyStorePath, keyStorePass, keyStoreType);
        SSLSocket socket = connectionHelper.getSSLSocket();
        
        // Read bytes        
        byte[] b = new byte[1024];
        ByteArrayOutputStream message = new ByteArrayOutputStream();
        int nbBytes = 0;
        while ( (nbBytes = socket.getInputStream().read(b, 0, 1024))!= -1) {
            message.write(b, 0, nbBytes);
        }
        
        // Compute
        LinkedList<Device> listDev = new LinkedList<Device>();
        byte[] listOfDevices = message.toByteArray();    
        int nbTuples = listOfDevices.length / FEEDBACK_TUPLE_SIZE;
        for(int i=0;i<nbTuples;i++) {
             int offset = i*FEEDBACK_TUPLE_SIZE;

             // Build date
             int index = 0;
             int firstByte = 0;
             int secondByte = 0;
             int thirdByte = 0;
             int fourthByte = 0;
             long anUnsignedInt = 0;
             
             firstByte = (0x000000FF & ((int)listOfDevices[offset]));
             secondByte = (0x000000FF & ((int)listOfDevices[offset+1]));
             thirdByte = (0x000000FF & ((int)listOfDevices[offset+2]));
             fourthByte = (0x000000FF & ((int)listOfDevices[offset+3]));
             index = index+4;
             anUnsignedInt  = ((long) (firstByte << 24
                            | secondByte << 16
                                | thirdByte << 8
                                | fourthByte))
                               & 0xFFFFFFFFL;

             // Build device token length
             int deviceTokenLength = listOfDevices[offset+4]<<8 | listOfDevices[offset+5];             
             
             // Build device token
             String deviceToken = "";
             int octet = 0;
             for (int j = 0; j < 32; j++) {                 
                octet = (0x000000FF & ((int)listOfDevices[offset+6+j]));
                deviceToken  = deviceToken.concat(String.format("%02x", octet));
             }

             // Build device and add to list
             Device device = new Device(null, deviceToken, new Timestamp(anUnsignedInt*1000));
             listDev.add(device);
             Logger.getAnonymousLogger().info("FeedbackManager retrieves one device :  "+new Date(anUnsignedInt*1000)+";"+deviceTokenLength+";"+deviceToken+".");
        }
        
        // Close the socket and return the list
        socket.close();
        return listDev;
	}
	
}
