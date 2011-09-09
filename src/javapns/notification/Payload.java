package javapns.notification;

import java.util.*;

import javapns.devices.*;

import org.apache.log4j.*;
import org.json.*;

/**
 * Abstract class representing a payload that can be transmitted to Apple.
 * 
 * By default, this class has no payload content at all.  Subclasses are
 * responsible for imposing specific content based on the specifications
 * they are intended to implement (such as the 'aps' dictionnary for APS 
 * payloads).
 * 
 * @author Sylvain Pedneault
 */
public abstract class Payload {

	protected static final Logger logger = Logger.getLogger(DeviceFactory.class);

	/* The root Payload */
	private JSONObject payload;


	/**
	 * Constructor, instantiate the two JSONObjects
	 */
	public Payload() {
		super();
		this.payload = new JSONObject();
	}


	public JSONObject getPayload() {
		return this.payload;
	}


	/**
	 * Add a custom dictionnary with a string value
	 * @param name
	 * @param value
	 * @throws JSONException
	 */
	public void addCustomDictionary(String name, String value) throws JSONException {
		logger.debug("Adding custom Dictionary [" + name + "] = [" + value + "]");
		this.payload.put(name, value);
	}


	/**
	 * Add a custom dictionnary with a int value
	 * @param name
	 * @param value
	 * @throws JSONException
	 */
	public void addCustomDictionary(String name, int value) throws JSONException {
		logger.debug("Adding custom Dictionary [" + name + "] = [" + value + "]");
		this.payload.put(name, value);
	}


	/**
	 * Add a custom dictionnary with multiple values
	 * @param name
	 * @param values
	 * @throws JSONException
	 */
	public void addCustomDictionary(String name, List values) throws JSONException {
		logger.debug("Adding custom Dictionary [" + name + "] = (list)");
		this.payload.put(name, values);
	}


	/**
	 * Get the string representation
	 */
	public String toString() {
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

		if (payload.length > 256) {
			throw new Exception("Payload too large...[256 Bytes is the limit]");
		}

		return payload;
	}

}