package javapns.communication;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import javapns.devices.exceptions.*;

public class KeystoreManager {

	public static KeyStore loadKeystore(AppleServer server) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		return loadKeystore(server, server.getKeystoreStream());
	}


	public static KeyStore loadKeystore(AppleServer server, Object keystore) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Exception {
		InputStream keystoreStream = streamKeystore(keystore);
		KeyStore keyStore = KeyStore.getInstance(server.getKeystoreType());
		if (server.getKeystorePassword() == null) {
			keyStore.load(server.getKeystoreStream(), null);

		} else {
			try {
				keyStore.load(keystoreStream, server.getKeystorePassword().toCharArray());
			} catch (Exception e) {
				if (e != null) {
					if (e.toString().contains("javax.crypto.BadPaddingException")) {
						throw new InvalidKeystorePasswordException();
					}
				}
				throw e;
			}
		}
		return keyStore;
	}


	/**
	 * Given an object representing a keystore, returns an actual stream for that keystore.
	 * Allows you to provide an actual keystore as an InputStream or a byte[] array,
	 * or a reference to a keystore file as a File object or a String path.
	 * 
	 * 
	 * @param keystore InputStream, File, byte[] or String (as a file path)
	 * @return A stream to the keystore.
	 * @throws FileNotFoundException
	 */
	public static InputStream streamKeystore(Object keystore) throws FileNotFoundException {
		if (keystore instanceof InputStream) return (InputStream) keystore;
		else if (keystore instanceof File) return new BufferedInputStream(new FileInputStream((File) keystore));
		else if (keystore instanceof String) return new BufferedInputStream(new FileInputStream((String) keystore));
		else if (keystore instanceof byte[]) return new ByteArrayInputStream((byte[]) keystore);
		else throw new IllegalArgumentException("Unsupported keystore reference: " + keystore);
	}

}
