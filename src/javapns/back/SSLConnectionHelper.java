package javapns.back;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * This class help create a connection to the Apple Server with a SLL Socket
 * @author Maxime Peron
 *
 */
public class SSLConnectionHelper {

	/* The path to the keystore used to authenticate this server */
	private String keyStorePath;
	/* The password used to load the keystore */
	private String keyStorePass;
	/* The type of keystore used (JKS or PKCS12) */
	private String keystoreType;
	/* The apple host (for dev : gateway.sandbox.push.apple.com) (for prod : gateway.push.apple.com) */
	private String appleHost;
	/* The apple port (for now : 2195)*/
	private int applePort;

	/* The algorithm used by KeyManagerFactory */
	private static final String ALGORITHM = "sunx509";
	/* The protocol used to create the SSLSocket */
	private static final String PROTOCOL = "TLS";
	
	/* PKCS12 */
	public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";
	/* JKS */
	public static final String KEYSTORE_TYPE_JKS = "JKS";
	
	/**
	 * Constructor
	 * @param appleHost the Apple ServerSocket host
	 * @param applePort the Apple ServerSocket port
	 * @param keyStorePath the path to the keystore
	 * @param keyStorePass the keystore password
	 * @param keystoreType the keystore type
	 */
	public SSLConnectionHelper(String appleHost, int applePort, String keyStorePath, String keyStorePass, String keystoreType) {
		this.appleHost = appleHost;
		this.applePort = applePort;
		this.keyStorePath = keyStorePath;
		this.keyStorePass = keyStorePass;
		this.keystoreType = keystoreType;
	}

	/**
	 * Create a SSLSocket which will be used to send data to Apple
	 * @return the SSLSocket
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 */
	public SSLSocket getSSLSocket()throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException{
		// Load the Keystore
		KeyStore ks = KeyStore.getInstance(keystoreType);
		ks.load(new FileInputStream(this.keyStorePath), this.keyStorePass.toCharArray());
		
		// Get a KeyManager and initialize it 
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(ALGORITHM);
		kmf.init(ks, this.keyStorePass.toCharArray());

		// Get a TrustManagerFactory and init with KeyStore
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(ALGORITHM);
		tmf.init(ks);

		// Get the SSLContext to help create SSLSocketFactory			
		SSLContext sslc = SSLContext.getInstance(PROTOCOL);
		sslc.init(kmf.getKeyManagers(), null, null);
		
		// Get SSLSocketFactory and get a SSLSocket
		SSLSocketFactory sslsf = sslc.getSocketFactory();
		SSLSocket socket = (SSLSocket) sslsf.createSocket(appleHost, applePort);
		return socket;
	}
	
}
