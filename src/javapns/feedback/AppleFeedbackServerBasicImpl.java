package javapns.feedback;

import java.io.*;

import javapns.communication.*;

/**
 * Basic implementation of the AppleFeedbackServer interface,
 * intended to facilitate rapid deployment.
 * 
 * @author Sylvain Pedneault
 */
public class AppleFeedbackServerBasicImpl extends AppleServerBasicImpl implements AppleFeedbackServer {

	private String host = "gateway.sandbox.feedback.apple.com";
	private int port = 2196;


	public AppleFeedbackServerBasicImpl(Object keystore, String password, String type, String host, int port) throws FileNotFoundException {
		super(keystore, password, type);
		this.host = host;
		this.port = port;
	}


	public String getFeedbackServerHost() {
		return host;
	}


	public int getFeedbackServerPort() {
		return port;
	}

}
