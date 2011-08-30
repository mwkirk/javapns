package javapns.devices.exceptions;

/**
 * Thrown when we try to retrieve a device that doesn't exist
 * @author SYPECom Inc.
 *
 */
@SuppressWarnings("serial")
public class InvalidCertificateChainException extends Exception{
	
	/* Custom message for this exception */
	private String message;
	
	/**
	 * Constructor
	 */
	public InvalidCertificateChainException(){
		this.message = "Invalid certificate chain!  Verify that the keystore you provided was produced according to specs...";
	}
	
	/**
	 * Constructor with custom message
	 * @param message
	 */
	public InvalidCertificateChainException(String message){
		this.message = "Invalid certificate chain ("+message+")!  Verify that the keystore you provided was produced according to specs...";
//		this.message = message;
	}
	
	/**
	 * String representation
	 */
	public String toString(){
		return this.message;
	}
}
