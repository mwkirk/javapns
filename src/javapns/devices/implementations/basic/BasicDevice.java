package javapns.devices.implementations.basic;

import java.sql.Timestamp;

import javapns.devices.*;

/**
 * This class is used to represent a Device (iPhone)
 * @author Maxime Peron
 *
 */
public class BasicDevice implements Device {

	/* An id representing a particular device */
	private String deviceId;

	/* The device token given by Apple Server, hexadecimal form, 64bits length */
	private String token;

	/* The last time a device registered */
	private Timestamp lastRegister;


	/**
	 * Constructor
	 * @param id The device id
	 * @param token The device token
	 */
	public BasicDevice(String id, String token, Timestamp register) {
		super();
		this.deviceId = id;
		this.token = token;
		this.lastRegister = register;
	}


	/**
	 * Getter
	 * @return the device id
	 */
	public String getDeviceId() {
		return deviceId;
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


	/**
	 * Setter
	 * @param id the device id
	 */
	public void setDeviceId(String id) {
		this.deviceId = id;
	}

	/**
	 * Setter the device token
	 * @param token
	 */
	public void setToken(String token) {
		this.token = token;
	}


	public void setLastRegister(Timestamp lastRegister) {
		this.lastRegister = lastRegister;
	}

}
