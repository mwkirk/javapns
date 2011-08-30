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


	public AppleServerBasicImpl(InputStream input, String password, String type) {
		this.input = input;
		this.password = password;
		this.type = type;
	}


	public AppleServerBasicImpl(byte[] bytes, String password, String type) {
		this.input = new ByteArrayInputStream(bytes);
		this.password = password;
		this.type = type;
	}


	public AppleServerBasicImpl(File file, String password, String type) throws FileNotFoundException {
		this.input = new BufferedInputStream(new FileInputStream(file));
		this.password = password;
		this.type = type;
	}


	public AppleServerBasicImpl(String filePath, String password, String type) throws FileNotFoundException {
		this.input = new BufferedInputStream(new FileInputStream(filePath));
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
