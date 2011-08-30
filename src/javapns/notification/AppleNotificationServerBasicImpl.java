package javapns.notification;

import java.io.*;

import javapns.communication.*;

/**
 * Basic implementation of the AppleNotificationServer interface,
 * intended to facilitate rapid deployment.
 * 
 * @author Sylvain Pedneault
 */
public class AppleNotificationServerBasicImpl extends AppleServerBasicImpl implements AppleNotificationServer {

	private String host = "gateway.sandbox.push.apple.com";
	private int port = 2195;


	public AppleNotificationServerBasicImpl(InputStream input, String password, String type, String host, int port) {
		super(input, password, type);
		this.host = host;
		this.port = port;
	}


	public AppleNotificationServerBasicImpl(byte[] bytes, String password, String type, String host, int port) {
		super(bytes, password, type);
		this.host = host;
		this.port = port;
	}


	public AppleNotificationServerBasicImpl(File file, String password, String type, String host, int port) throws FileNotFoundException {
		super(file, password, type);
		this.host = host;
		this.port = port;
	}


	public AppleNotificationServerBasicImpl(String filePath, String password, String type, String host, int port) throws FileNotFoundException {
		super(filePath, password, type);
		this.host = host;
		this.port = port;
	}


	public String getNotificationServerHost() {
		return host;
	}


	public int getNotificationServerPort() {
		return port;
	}

}
