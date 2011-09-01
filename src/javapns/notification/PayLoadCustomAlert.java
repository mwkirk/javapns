package javapns.notification;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A custom Payload alert helps you customize the way the popup behaves
 * when the iPhone receives and displays the alert message
 * @author Maxime Peron
 *
 */
public class PayLoadCustomAlert extends JSONObject {

	/**
	 * Constructor
	 */
	public PayLoadCustomAlert() {
		super();
	}


	/**
	 * Add a body, ie the alert message
	 * @param body
	 * @throws JSONException
	 */
	public void addBody(String body) throws JSONException {
		this.put("body", body);
	}


	/**
	 * Add a custom text for the right button of the popup
	 * @param actionLocKey
	 * @throws JSONException
	 */
	public void addActionLocKey(String actionLocKey) throws JSONException {
		this.put("action-loc-key", actionLocKey);
	}


	/**
	 * Add a custom alert message with possible parameters
	 * See the Apple Push Notification Service programming guide page 21
	 * @param locKey
	 * @throws JSONException
	 */
	public void addLocKey(String locKey) throws JSONException {
		this.put("loc-key", locKey);
	}


	/**
	 * Add the parameters for the loc-key key
	 * @param args
	 * @throws JSONException
	 */
	public void addLocArgs(List args) throws JSONException {
		this.put("loc-args", args);
	}

}
