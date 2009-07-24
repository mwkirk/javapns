package javapns.data;

import java.sql.Timestamp;

/**
 * This class is used to represent a Device (iPhone)
 * @author Maxime Peron
 *
 */
public class Device {

	/* An id representing a particular device */
	private String id;
	
	/* The device token given by Apple Server, hexadecimal form, 64bits length */
	private String token;
	
	/* The last time a device registered */
	private Timestamp lastRegister;
	
	/**
	 * Constructor
	 * @param id The device id
	 * @param token The device token
	 */
	public Device(String id, String token, Timestamp register) {
		super();
		this.id = id;
		this.token = token;
		this.lastRegister = register;
	}
	
	/**
	 * Getter
	 * @return the device id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Getter
	 * @return the device token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Getter
	 * @return the last register
	 */
	public Timestamp getLastRegister() {
		return lastRegister;
	}	
	
}
