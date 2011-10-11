package javapns.communication.exceptions;

/**
 * Thrown when we try to contact Apple with an invalid password for the keystore.
 * @author Sylvain Pedneault
 *
 */
@SuppressWarnings("serial")
public class InvalidKeystorePasswordException extends Exception {

	/* Custom message for this exception */
	private String message;


	/**
	 * Constructor
	 */
	public InvalidKeystorePasswordException() {
		this.message = "Invalid keystore password!  Verify settings for connecting to Apple...";
	}


	/**
	 * Constructor with custom message
	 * @param message
	 */
	public InvalidKeystorePasswordException(String message) {
		this.message = message;
	}


	/**
	 * String representation
	 */
	public String toString() {
		return this.message;
	}
}
