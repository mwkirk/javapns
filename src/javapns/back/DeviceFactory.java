package javapns.back;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javapns.data.Device;
import javapns.exceptions.DuplicateDeviceException;
import javapns.exceptions.NullDeviceTokenException;
import javapns.exceptions.NullIdException;
import javapns.exceptions.UnknownDeviceException;

import org.apache.commons.lang.StringUtils;


/**
 * This class help manage the devices
 * It implements the singleton pattern so only one class manages the devices,
 * avoiding problems with duplicate or lost devices
 * NB : Future Improvement :
 * 		 - Add a method to find a device knowing his token
 * 		 - Add a method to update a device (timestamp or token)
 *       - link with a database (JPA?) to store devices
 *       - method to compare two devices, and replace when the device token has changed
 * @author Maxime Peron
 *
 */
public class DeviceFactory {

	/* A map containing all the devices, identified with their id */
	private Map<String, Device> devices;
	
	/* Singleton pattern */
	private static DeviceFactory instance;

	/**
	 * Singleton pattern implementation
	 * @return the instance of DeviceFactory
	 */
	public static DeviceFactory getInstance(){
		if (instance == null){
			instance = new DeviceFactory();
		}
		return instance;
	}

	/**
	 * Private Constructor
	 */
	private DeviceFactory(){
		this.devices = new HashMap<String, Device>();
	}

	/**
	 * Add a device to the map
	 * @param id The device id
	 * @param token The device token
	 * @throws DuplicateDeviceException
	 * @throws NullIdException 
	 * @throws NullDeviceTokenException 
	 */
	public void addDevice(String id, String token) throws DuplicateDeviceException, NullIdException, NullDeviceTokenException{
		if ((id == null) || (id.trim().equals(""))){
			throw new NullIdException();
		} else if ((token == null) || (token.trim().equals(""))){
			throw new NullDeviceTokenException();
		} else {
			if (!this.devices.containsKey(id)){
				token = StringUtils.deleteWhitespace(token);
				this.devices.put(id, new Device(id, token, new Timestamp(Calendar.getInstance().getTime().getTime())));
			} else {
				throw new DuplicateDeviceException();
			}
		}
	}

	/**
	 * Get a device according to his id
	 * @param id The device id
	 * @return The device
	 * @throws UnknownDeviceException
	 * @throws NullIdException 
	 */
	public Device getDevice(String id) throws UnknownDeviceException, NullIdException{
		if ((id == null) || (id.trim().equals(""))){
			throw new NullIdException();
		} else {
			if (this.devices.containsKey(id)){
				return this.devices.get(id);
			} else {
				throw new UnknownDeviceException();
			}
		}
	}


	/**
	 * Remove a device
	 * @param id The device id
	 * @throws UnknownDeviceException
	 * @throws NullIdException 
	 */
	public void removeDevice(String id) throws UnknownDeviceException, NullIdException {
		if ((id == null) || (id.trim().equals(""))){
			throw new NullIdException();
		}
		if (this.devices.containsKey(id)){
			this.devices.remove(id);
		} else {
			throw new UnknownDeviceException();
		}
	}
}
