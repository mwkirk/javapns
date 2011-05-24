package javapns.back;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * This class help create a connection to the Apple Server with a SLL Socket
 * 
 * @author Maxime Peron
 * @author idbill (feedback code)
 * @author Nathan Hadfield (new InputStream code)
 *
 */
public class SSLConnectionHelper {

    protected static final Logger logger = Logger.getLogger( SSLConnectionHelper.class );

    /* The password used to load the keystore */
	private String keyStorePass;
	/* The apple host (for dev : gateway.sandbox.push.apple.com) (for prod : gateway.push.apple.com) */
	private String appleHost;
	/* The apple port (for now : 2195 - push / 2196 - feedback)*/
	private int applePort;

	private KeyStore keyStore;
	
	private KeyStore feedbackKeyStore;
	
	private SSLSocketFactory pushSSLSocketFactory;
	private SSLSocketFactory feedbackSSLSocketFactory;

	/* The algorithm used by KeyManagerFactory */
	//private static final String ALGORITHM = "sunx509";
	private static final String ALGORITHM = ( ( Security.getProperty("ssl.KeyManagerFactory.algorithm") == null ) ? "sunx509" : Security.getProperty("ssl.KeyManagerFactory.algorithm"));
	
	/* The protocol used to create the SSLSocket */
	private static final String PROTOCOL = "TLS";
	
	/* PKCS12 */
	public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";
	/* JKS */
	public static final String KEYSTORE_TYPE_JKS = "JKS";
	
	private boolean proxySet = false;
	
	static {
        Security.addProvider( new BouncyCastleProvider() );
	}
	
	/**
	 * Constructor w/path to keystore
	 * 
	 * @param appleHost the Apple ServerSocket host
	 * @param applePort the Apple ServerSocket port
	 * @param keyStorePath the path to the keystore
	 * @param keyStorePass the keystore password
	 * @param keystoreType the keystore type (JKS or PKCS12)
	 * @throws KeyStoreException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 */
	public SSLConnectionHelper(String appleHost, int applePort, String keyStorePath, String keyStorePass, String keystoreType, boolean proxySet ) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
		logger.debug( "Instantiate SSLConnectionHelper with Path to Keystore" );
		this.appleHost = appleHost;
		this.applePort = applePort;
		this.keyStorePass = keyStorePass;
		this.proxySet = proxySet;

		// Load the Keystore
		this.keyStore = KeyStore.getInstance(keystoreType);
		if ( this.keyStorePass == null ) {
			this.keyStore.load( new FileInputStream(keyStorePath), null );
		} else {
		    this.keyStore.load( new FileInputStream(keyStorePath), this.keyStorePass.toCharArray() );
		}
	}

	/**
	 * Constructor w/keystore as InputStream
	 * 
	 * @param appleHost the Apple ServerSocket host
	 * @param applePort the Apple ServerSocket port
	 * @param keyStoreInputStream the keystore inputStream
	 * @param keyStorePass the keystore password
	 * @param keystoreType the keystore type (JKS or PKCS12)
	 * @throws KeyStoreException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 */
	public SSLConnectionHelper(String appleHost, int applePort, InputStream keyStoreInputStream, String keyStorePass, String keystoreType, boolean proxySet ) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
		logger.debug( "Instantiate SSLConnectionHelper with Keystore as InputStream" );
		this.appleHost = appleHost;
		this.applePort = applePort;
		this.keyStorePass = keyStorePass;
		this.proxySet = proxySet;
		
		// Load the Keystore
		this.keyStore = KeyStore.getInstance(keystoreType);
		if ( this.keyStorePass == null ) {
			this.keyStore.load( keyStoreInputStream, null );
			
		} else {
			this.keyStore.load( keyStoreInputStream, this.keyStorePass.toCharArray() );	
		}
	}
	
	/**
	 * Return SSLSocketFactory for Push notifications
	 * 
	 * @return SSLSocketFactory
	 * @throws KeyStoreException
	 * @throws NoSuchProviderException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 */
	private SSLSocketFactory getPushSSLSocketFactory() throws KeyStoreException, NoSuchProviderException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException{
		if( pushSSLSocketFactory == null ) {
			pushSSLSocketFactory = createSSLSocketFactoryWithTrustManagers( null );
		}
		logger.debug( "Returning Push SSLSocketFactory" );
		return pushSSLSocketFactory;
	}

	/**
	 * Return SSLSocketFactory for Feedback notifications
	 * 
	 * @return SSLSocketFactory
	 * @throws KeyStoreException
	 * @throws NoSuchProviderException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws Exception
	 */
	private SSLSocketFactory getFeedbackSSLSocketFactory() throws KeyStoreException, NoSuchProviderException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException, Exception {
		if( feedbackSSLSocketFactory == null ) {
			
			if ( feedbackKeyStore == null ) {
				feedbackKeyStore = FetchAppleSSLCertificate.fetch( appleHost, applePort );
			}
			
//			KeyStore ks2 = KeyStore.getInstance("JKS");
//			ks2.load(new FileInputStream(new File("/tmp/feedback.cert")),"changeme".toCharArray());
								
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(ALGORITHM);			
			tmf.init( feedbackKeyStore );

			// Get a TrustManagerFactory and init with KeyStore
//			TrustManagerFactory tmf2 = TrustManagerFactory.getInstance(ALGORITHM);
//			tmf2.init(keyStore);
	
			feedbackSSLSocketFactory = createSSLSocketFactoryWithTrustManagers( tmf.getTrustManagers() );
		}
		logger.debug( "Returning Feedback SSLSocketFactory" );
		return feedbackSSLSocketFactory;
	}
	
	/**
	 * Generic SSLSocketFactory builder (called by 2 functions above)
	 * 
	 * @param trustManagers
	 * @return SSLSocketFactory
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 */
	private SSLSocketFactory createSSLSocketFactoryWithTrustManagers( TrustManager[] trustManagers ) throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException, KeyStoreException {

		logger.debug( "Creating SSLSocketFactory" );
		// Get a KeyManager and initialize it 
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(ALGORITHM);
		kmf.init(this.keyStore, this.keyStorePass.toCharArray());

		// Get the SSLContext to help create SSLSocketFactory			
		SSLContext sslc = SSLContext.getInstance(PROTOCOL);
        sslc.init(kmf.getKeyManagers(), trustManagers, null);
        
        return sslc.getSocketFactory();
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
	public SSLSocket getSSLSocket() throws IOException, UnknownHostException, KeyStoreException, NoSuchProviderException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
		SSLSocketFactory socketFactory = getPushSSLSocketFactory();
		logger.debug( "Returning Push SSLSocket" );
		
		if ( proxySet ) {
			return tunnelThroughProxy( socketFactory );
		} else {
			return (SSLSocket) socketFactory.createSocket(appleHost, applePort);
		}
	}
	
    private SSLSocket tunnelThroughProxy( SSLSocketFactory socketFactory ) throws UnknownHostException, IOException {
    	SSLSocket socket;

    	// If a proxy was set, tunnel through the proxy to create the connection
    	String tunnelHost = System.getProperty("https.proxyHost");
    	Integer tunnelPort = Integer.getInteger("https.proxyPort").intValue();

    	Socket tunnel = new Socket( tunnelHost, tunnelPort );
    	doTunnelHandshake( tunnel, appleHost, applePort );

    	/* overlay the tunnel socket with SSL */
    	socket = (SSLSocket)socketFactory.createSocket(tunnel, appleHost, applePort, true);

    	/* register a callback for handshaking completion event */
    	socket.addHandshakeCompletedListener(
    			new HandshakeCompletedListener() {
    				public void handshakeCompleted( HandshakeCompletedEvent event ) {
	                    logger.debug( "Handshake finished!" );
	                    logger.debug( "\t CipherSuite:" + event.getCipherSuite() );
	                    logger.debug( "\t SessionId " + event.getSession() );
	                    logger.debug( "\t PeerHost " + event.getSession().getPeerHost() );
    				}
    			}
    	);

    	return socket;
    }

    private void doTunnelHandshake(Socket tunnel, String host, int port) throws IOException {
        
        OutputStream out = tunnel.getOutputStream();
        
        String msg = "CONNECT " + host + ":" + port + " HTTP/1.0\n" + "User-Agent: BoardPad Server" + "\r\n\r\n";
        byte b[] = null;
        try { //We really do want ASCII7 -- the http protocol doesn't change with locale.
                b = msg.getBytes("ASCII7");
        } catch (UnsupportedEncodingException ignored) { //If ASCII7 isn't there, something serious is wrong, but Paranoia Is Good (tm)
                b = msg.getBytes();
        }
        out.write(b);
        out.flush();

        // We need to store the reply so we can create a detailed error message to the user.
        byte reply[] = new byte[200];
        int replyLen = 0;
        int newlinesSeen = 0;
        boolean headerDone = false; //Done on first newline

        InputStream in = tunnel.getInputStream();

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
        } catch ( UnsupportedEncodingException ignored) {
                replyStr = new String(reply, 0, replyLen);
        }

        /* We check for Connection Established because our proxy returns HTTP/1.1 instead of 1.0 */
        if(replyStr.toLowerCase().indexOf("200 connection established") == -1) {
                throw new IOException("Unable to tunnel through. Proxy returns \"" + replyStr + "\"");
        }

        /* tunneling Handshake was successful! */
    }       

	
	/**
	 * Create a SSLSocket which will be used to retrieve data from Apple
	 * @return the SSLSocket
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws NoSuchProviderException
	 * @throws Exception
	 */	
	public SSLSocket getFeedbackSSLSocket() throws Exception, IOException, UnknownHostException, KeyStoreException, NoSuchProviderException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
		SSLSocketFactory socketFactory = getFeedbackSSLSocketFactory();
		logger.debug( "Returning Feedback SSLSocket" );
		return (SSLSocket) socketFactory.createSocket(appleHost, applePort);
	}
	
	public void fetchAppleCert( String appleHost, int applePort ) throws Exception {
		feedbackKeyStore = FetchAppleSSLCertificate.fetch( appleHost, applePort );
	}
	
	public void fetchAppleCert( String keystore, String keystorePassword ) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException{
		feedbackKeyStore = KeyStore.getInstance("JKS");
		feedbackKeyStore.load(new FileInputStream( new File( keystore ) ), keystorePassword.toCharArray() );
	}
}