package javapns.notification;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javapns.communication.*;

import javax.net.ssl.*;

/**
 * Connection details specific to the Notification Service.
 * 
 * @author Sylvain Pedneault
 */
public class ConnectionToNotificationServer extends ConnectionToAppleServer {

	// TrustManager class that simply trusts all servers. Obviously this isn't very secure!!!
	public static class ServerTrustingTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			throw new CertificateException("Client is not trusted.");
		}


		public void checkServerTrusted(X509Certificate[] chain, String authType) {
			// trust all servers
			System.out.println("Trusting all servers, including " + chain);
		}


		public X509Certificate[] getAcceptedIssuers() {
			return null;//new X509Certificate[0];
		}
	}


	public ConnectionToNotificationServer(AppleNotificationServer server) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		super(server);
		loadKeystore();
	}


	@Override
	public String getServerHost() {
		return ((AppleNotificationServer) getServer()).getNotificationServerHost();
	}


	@Override
	public int getServerPort() {
		return ((AppleNotificationServer) getServer()).getNotificationServerPort();
	}


	@Override
	public SSLSocketFactory createSSLSocketFactory() throws KeyStoreException, NoSuchProviderException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException, Exception {
		return createSSLSocketFactoryWithTrustManagers(new TrustManager[] { new ServerTrustingTrustManager() });
	}

}
