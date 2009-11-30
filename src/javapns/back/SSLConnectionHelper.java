package javapns.back;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
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
	/* Proxy is set */
	private boolean proxySet;

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
	public SSLConnectionHelper(String appleHost, int applePort, String keyStorePath, String keyStorePass, String keystoreType, boolean proxySet) {
		this.appleHost = appleHost;
		this.applePort = applePort;
		this.keyStorePath = keyStorePath;
		this.keyStorePass = keyStorePass;
		this.keystoreType = keystoreType;
		this.proxySet = proxySet;
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
		SSLSocket socket; 

		if (proxySet){
			// If a proxy was set, tunnel through the proxy to create the connection
			String tunnelHost = System.getProperty("https.proxyHost");
			Integer tunnelPort = Integer.getInteger("https.proxyPort").intValue();

			Socket tunnel = new Socket(tunnelHost, tunnelPort);
			doTunnelHandshake(tunnel, appleHost, applePort);

			/*
			 * Ok, let's overlay the tunnel socket with SSL.
			 */
			socket = (SSLSocket)sslsf.createSocket(tunnel, appleHost, applePort, true);
			/*
			 * register a callback for handshaking completion event
			 */
			socket.addHandshakeCompletedListener(
					new HandshakeCompletedListener() {
						public void handshakeCompleted(
								HandshakeCompletedEvent event) {
							System.out.println("Handshake finished!");
							System.out.println(
									"\t CipherSuite:" + event.getCipherSuite());
							System.out.println(
									"\t SessionId " + event.getSession());
							System.out.println(
									"\t PeerHost " + event.getSession().getPeerHost());
						}
					}
			);
		}else{
			socket = (SSLSocket) sslsf.createSocket(appleHost, applePort);			
		}		
		return socket;
	}

	private void doTunnelHandshake(Socket tunnel, String host, int port)throws IOException {
		OutputStream out = tunnel.getOutputStream();
		String msg = "CONNECT " + host + ":" + port + " HTTP/1.0\n"
		+ "User-Agent: "
		+ sun.net.www.protocol.http.HttpURLConnection.userAgent
		+ "\r\n\r\n";
		byte b[];
		try {
			/*
			 * We really do want ASCII7 -- the http protocol doesn't change
			 * with locale.
			 */
			b = msg.getBytes("ASCII7");
		} catch (UnsupportedEncodingException ignored) {
			/*
			 * If ASCII7 isn't there, something serious is wrong, but
			 * Paranoia Is Good (tm)
			 */
			b = msg.getBytes();
		}
		out.write(b);
		out.flush();

		/*
		 * We need to store the reply so we can create a detailed
		 * error message to the user.*/

		byte           reply[] = new byte[200];
		int            replyLen = 0;
		int            newlinesSeen = 0;
		boolean        headerDone = false;     /* Done on first newline */

		InputStream    in = tunnel.getInputStream();
		//boolean        error = false;

		while (newlinesSeen < 2) {
			int i = in.read();
			if (i < 0) {
				throw new IOException("Unexpected EOF from proxy");
			}
			if (i == '\n') {
				headerDone = true;
				++newlinesSeen;
			} else if (i != '\r') {
				newlinesSeen = 0;
				if (!headerDone && replyLen < reply.length) {
					reply[replyLen++] = (byte) i;
				}
			}
		}

		/*
		 * Converting the byte array to a string is slightly wasteful
		 * in the case where the connection was successful, but it's
		 * insignificant compared to the network overhead.
		 */
		String replyStr;
		try {
			replyStr = new String(reply, 0, replyLen, "ASCII7");
		} catch (UnsupportedEncodingException ignored) {
			replyStr = new String(reply, 0, replyLen);
		}

		/* We check for Connection Established because our proxy returns 
		 * HTTP/1.1 instead of 1.0 */
		//if (!replyStr.startsWith("HTTP/1.0 200")) {
		if(replyStr.toLowerCase().indexOf(
		"200 connection established") == -1){
			throw new IOException("Unable to tunnel through "

					+ ".  Proxy returns \"" + replyStr + "\"");
		}
		/* tunneling Handshake was successful! */
	}

}
