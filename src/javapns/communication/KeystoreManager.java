package javapns.communication;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javapns.devices.exceptions.*;

public class KeystoreManager {

	public static KeyStore loadKeystore(AppleServer server, String path) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		return loadKeystore(server, new File(path));
	}
	
	public static KeyStore loadKeystore(AppleServer server, File file) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		return loadKeystore(server, new FileInputStream(file));
	}
	
	public static KeyStore loadKeystore(AppleServer server) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		return loadKeystore(server, server.getKeystoreStream());
	}
	
	public static KeyStore loadKeystore(AppleServer server, InputStream keystoreStream) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception{
		KeyStore keyStore = KeyStore.getInstance(server.getKeystoreType());
		if ( server.getKeystorePassword() == null ) {
			keyStore.load( server.getKeystoreStream(), null );
			
		} else {
			try {
				keyStore.load(keystoreStream , server.getKeystorePassword().toCharArray() );
			} catch (Exception e) {
				if (e!=null) {
					if (e.toString().contains("javax.crypto.BadPaddingException")) {
						throw new InvalidKeystorePasswordException();
					}
				}
				throw e;
			}	
		}
		return keyStore;
	}
	
//	protected void loadKeystore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
//		// Load the Keystore
//		this.keyStore = KeyStore.getInstance(server.getKeystoreType());
//		if ( this.server.getKeystorePassword() == null ) {
//			this.keyStore.load( server.getKeystoreStream(), null );
//			
//		} else {
//			try {
//				this.keyStore.load( server.getKeystoreStream(), this.server.getKeystorePassword().toCharArray() );
//			} catch (Exception e) {
//				if (e!=null) {
//					if (e.toString().contains("javax.crypto.BadPaddingException")) {
//						throw new InvalidKeystorePasswordException();
//					}
//				}
//				throw e;
//			}	
//		}
//
//	}

	
}
