package javapns.communication.exceptions;

/**
 * Thrown when we try to contact Apple with an invalid keystore format.
 * @author Sylvain Pedneault
 *
 */
@SuppressWarnings("serial")
public class InvalidKeystoreFormatException extends Exception {

	/* Custom message for this exception */
	private String message;


	/**
	 * Constructor
	 */
	public InvalidKeystoreFormatException() {
		this.message = "Invalid keystore format!  Make sure it is PKCS12...";
	}


	/**
	 * Constructor with custom message
	 * @param message
	 */
	public InvalidKeystoreFormatException(String message) {
		this.message = message;
	}


	/**
	 * String representation
	 */
	public String toString() {
		return this.message;
	}
}
