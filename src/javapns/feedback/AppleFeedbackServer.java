package javapns.feedback;

import javapns.communication.*;

/**
 * Interface representing a connection to an Apple Feedback Server
 * 
 * @author Sylvain Pedneault
 */
public interface AppleFeedbackServer extends AppleServer {

	public String getFeedbackServerHost();


	public int getFeedbackServerPort();

}
