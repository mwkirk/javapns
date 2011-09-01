package javapns.feedback;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javapns.communication.*;
import javapns.notification.ConnectionToNotificationServer.*;

import javax.net.ssl.*;

public class ConnectionToFeedbackServer extends ConnectionToAppleServer {

	public ConnectionToFeedbackServer(AppleFeedbackServer feedbackServer) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		super(feedbackServer);
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
	public SSLSocketFactory createSSLSocketFactory() throws KeyStoreException, NoSuchProviderException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException, Exception {
		return createSSLSocketFactoryWithTrustManagers(new TrustManager[] { new ServerTrustingTrustManager() });
	}


	@Override
	public String getServerHost() {
		return ((AppleFeedbackServer) getServer()).getFeedbackServerHost();
	}


	@Override
	public int getServerPort() {
		return ((AppleFeedbackServer) getServer()).getFeedbackServerPort();
	}

}
