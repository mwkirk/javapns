package javapns.communication;

import java.io.*;

/**
 * A basic and abstract implementation of the AppleServer interface
 * intended to facilitate rapid deployment.
 * 
 * @author Sylvain Pedneault
 */
public abstract class AppleServerBasicImpl implements AppleServer {

	private final InputStream input;
	private final String password;
	private final String type;


	public AppleServerBasicImpl(Object keystore, String password, String type) throws FileNotFoundException {
		this.input = KeystoreManager.streamKeystore(keystore);
		this.password = password;
		this.type = type;
	}


	public InputStream getKeystoreStream() {
		return input;
	}


	public String getKeystorePassword() {
		return password;
	}


	public String getKeystoreType() {
		return type;
	}

}
