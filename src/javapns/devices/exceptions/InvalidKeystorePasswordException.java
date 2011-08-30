package javapns.devices.exceptions;

/**
 * Thrown when we try to retrieve a device that doesn't exist
 * @author SYPECom Inc.
 *
 */
@SuppressWarnings("serial")
public class InvalidKeystorePasswordException extends Exception{
	
	/* Custom message for this exception */
	private String message;
	
	/**
	 * Constructor
	 */
	public InvalidKeystorePasswordException(){
		this.message = "Invalid keystore password!  Verify settings for connecting to Apple...";
	}
	
	/**
	 * Constructor with custom message
	 * @param message
	 */
	public InvalidKeystorePasswordException(String message){
		this.message = message;
	}
	
	/**
	 * String representation
	 */
	public String toString(){
		return this.message;
	}
}
