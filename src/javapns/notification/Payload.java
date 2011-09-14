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

	/* Maximum total length (serialized) of a payload */
	private static final int MAXIMUM_PAYLOAD_LENGTH = 256;

	/* Character encoding specified by Apple documentation */
	private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";

	protected static final Logger logger = Logger.getLogger(DeviceFactory.class);

	/* The root Payload */
	private JSONObject payload;

	/* Character encoding to use for streaming the payload (should be UTF-8) */
	private String characterEncoding = DEFAULT_CHARACTER_ENCODING;


	/**
	 * Constructor, instantiate the the root JSONObject
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
	 * Get this payload as a byte array using the preconfigured character encoding.
	 * 
	 * @return byte[] bytes ready to be streamed directly to Apple servers
	 */
	public byte[] getPayloadAsBytes() throws Exception {
		byte[] payload = null;
		try {
			payload = toString().getBytes(characterEncoding);
		} catch (Exception ex) {
			payload = toString().getBytes();
		}

		if (payload.length > MAXIMUM_PAYLOAD_LENGTH) {
			throw new Exception("Payload too large...[256 Bytes is the limit]");
		}

		return payload;
	}


	/**
	 * Changes the character encoding for streaming the payload.
	 * Character encoding is preset to UTF-8, as Apple documentation specifies.
	 * Therefore, unless you are working on a special project, you should leave it as is.
	 * 
	 * @param characterEncoding a valid character encoding that String.getBytes(encoding) will accept
	 */
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}


	/**
	 * Returns the character encoding that will be used by getPayloadAsBytes().
	 * Default is UTF-8, as per Apple documentation.
	 * 
	 * @return a character encoding
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

}