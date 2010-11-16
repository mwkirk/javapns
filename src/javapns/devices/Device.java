package javapns.devices;

import java.sql.*;

/**
 * This is the common interface for all Devices.
 * It allows the DeviceFactory to support multiple
 * implementations of Device (in-memory, JPA-backed, etc.)
 * 
 * @author Sylvain Pedneault
 */
public interface Device {

	/**
	 * Getter
	 * @return the device id
	 */
	public String getDeviceId();


	/**
	 * Getter
	 * @return the device token
	 */
	public String getToken();


	/**
	 * Getter
	 * @return the last register
	 */
	public Timestamp getLastRegister();


	/**
	 * Setter
	 * @param id the device id
	 */
	public void setDeviceId(String id);


	/**
	 * Setter the device token
	 * @param token
	 */
	public void setToken(String token);


	/**
	 * Setter
	 * @param lastRegister the last register
	 */
	public void setLastRegister(Timestamp lastRegister);

}