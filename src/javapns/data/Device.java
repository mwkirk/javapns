package javapns.data;

import java.sql.*;

public interface Device {

	/**
	 * Getter
	 * @return the device id
	 */
	public String getId();


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
	public void setId(String id);


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