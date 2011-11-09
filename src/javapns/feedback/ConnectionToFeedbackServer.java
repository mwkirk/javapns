package javapns.feedback;

import javapns.communication.*;
import javapns.communication.exceptions.*;

/**
 * Class representing a connection to a specific Feedback Server.
 * 
 * @author Sylvain Pedneault
 */
public class ConnectionToFeedbackServer extends ConnectionToAppleServer {

	public ConnectionToFeedbackServer(AppleFeedbackServer feedbackServer) throws KeystoreException {
		super(feedbackServer);
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
