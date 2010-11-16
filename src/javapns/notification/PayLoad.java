package javapns.notification;

import java.util.List;

import javapns.impl.basic.*;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is the JSON representation of the notification
 * See page 19 of Apple Push Notification Service Programming Guide
 * @author Maxime Peron
 *
 */
public class PayLoad {

    protected static final Logger logger = Logger.getLogger( BasicDeviceFactory.class );

    /* The root Payload */
	private JSONObject payload;
	/* The application Dictionnary */
	private JSONObject apsDictionary;

	/**
	 * Constructor, instantiate the two JSONObjects
	 */
	public PayLoad(){
		super();
		this.payload = new JSONObject();
		this.apsDictionary = new JSONObject();
		try {
			this.payload.put("aps", this.apsDictionary);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public PayLoad(String alert, int badge, String sound)  throws JSONException {
		this();
		addAlert(alert);
		addBadge(badge);
		if(sound != null){
			addSound(sound);
		}
	}
	
	/**
	 * Add a badge
	 * @param badge
	 * @throws JSONException
	 */
	public void addBadge (int badge) throws JSONException{
		logger.debug( "Adding badge [" + badge + "]" );
		this.apsDictionary.putOpt("badge", badge);
	}

	/**
	 * Add a sound
	 * @param sound
	 * @throws JSONException
	 */
	public void addSound (String sound) throws JSONException{
		logger.debug( "Adding sound [" + sound + "]" );
		this.apsDictionary.putOpt("sound", sound);
	}

	/**
	 * Add an alert message
	 * @param alert
	 * @throws JSONException
	 */
	public void addAlert (String alert) throws JSONException{
		logger.debug( "Adding alert [" + alert + "]" );
		this.apsDictionary.put("alert", alert);
	}

	/**
	 * Add a custom alert message
	 * @param alert
	 * @throws JSONException
	 */
	public void addCustomAlert (PayLoadCustomAlert alert) throws JSONException{
		logger.debug( "Adding custom Alert" );
		this.apsDictionary.put("alert", alert);
	}

	/**
	 * Add a custom dictionnary with a string value
	 * @param name
	 * @param value
	 * @throws JSONException
	 */
	public void addCustomDictionary (String name, String value) throws JSONException{
		logger.debug( "Adding custom Dictionary [" + name + "] = [" + value + "]" );
		this.payload.put(name,value);
	}

	/**
	 * Add a custom dictionnary with a int value
	 * @param name
	 * @param value
	 * @throws JSONException
	 */
	public void addCustomDictionary (String name, int value) throws JSONException{
		logger.debug( "Adding custom Dictionary [" + name + "] = [" + value + "]" );
		this.payload.put(name, value);
	}

	/**
	 * Add a custom dictionnary with multiple values
	 * @param name
	 * @param values
	 * @throws JSONException
	 */
	public void addCustomDictionary (String name, List values) throws JSONException{
		logger.debug( "Adding custom Dictionary [" + name + "] = (list)" );
		this.payload.put(name, values);
	}

	/**
	 * Get the string representation
	 */
	public String toString(){
		return this.payload.toString();
	}

	/**
	 * Get this payload as a byte array
	 * @return byte[]
	 */
	public byte[] getPayloadAsBytes() throws Exception {
		byte[] payload = null;
		try {
			payload = toString().getBytes("UTF-8");
		} catch (Exception ex) {
			payload = toString().getBytes();
		}
		
		if ( payload.length > 256 ) {
			throw new Exception( "Payload too large...[256 Bytes is the limit]" );
		}
		
		return payload;
	}

}
