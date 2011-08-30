package javapns.feedback;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javapns.communication.*;

import javax.net.ssl.*;

public class ConnectionToFeedbackServer extends ConnectionToAppleServer  {

	
	public ConnectionToFeedbackServer(AppleFeedbackServer feedbackServer) {
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
		
		
			if ( getKeystore() == null ) {
				setKeystore(FetchAppleSSLCertificate.fetch( getServerHost(), getServerPort() ));
			}
			
//			KeyStore ks2 = KeyStore.getInstance("JKS");
//			ks2.load(new FileInputStream(new File("/tmp/feedback.cert")),"changeme".toCharArray());
								
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(ALGORITHM);			
			tmf.init( getKeystore() );

			// Get a TrustManagerFactory and init with KeyStore
//			TrustManagerFactory tmf2 = TrustManagerFactory.getInstance(ALGORITHM);
//			tmf2.init(keyStore);
	
			SSLSocketFactory feedbackSSLSocketFactory = createSSLSocketFactoryWithTrustManagers( tmf.getTrustManagers() );
		return feedbackSSLSocketFactory;
	}

	@Override
	public String getServerHost() {
		return ((AppleFeedbackServer)getServer()).getFeedbackServerHost();
	}

	@Override
	public int getServerPort() {
		return ((AppleFeedbackServer)getServer()).getFeedbackServerPort();
	}


	
}
