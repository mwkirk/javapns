package javapns.data;

/**
 * This class is the JSON representation of the notification
 * See page 19 of Apple Push Notification Service Programming Guide
 * NB : Future improvement :
 * 		- add other features (body, action-loc-key, loc-key, loc-args...)
 * 		- use a library to help create JSON Object http://www.json.org/java/
 * @author Maxime Peron
 *
 */
public class PayLoad {

	/* The message that will be displayed in the popup */
	private String alert;
	/* The number displayed at top right side corner of the application icon */
	private String badge;
	/* The sound played when the notification is received */
	private String sound;
		
	/**
	 * Constructor
	 * @param alert The displayed message (can be null if no alert has to be displayed)
	 * @param badge The number (can be null if no badge has to be displayed)
	 * @param sound The played sound (can be null if no sound has to be played)
	 */
	public PayLoad(String alert, String badge, String sound) {
		super();
		this.alert = alert;
		this.badge = badge;
		this.sound = sound;
	}

	/**
	 * Getter
	 * @return the alert
	 */
	public String getAlert() {
		return alert;
	}

	/**
	 * Getter
	 * @return the badge
	 */
	public String getBadge() {
		return badge;
	}

	/**
	 * Getter
	 * @return the sound
	 */
	public String getSound() {
		return sound;
	}

	/**
	 * String representation of the payload
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("{\"aps\":{");
		if (this.alert != null){
			sb.append("\"alert\":\"").append(this.alert).append("\"");
		}
		if (this.badge != null){
			if (this.alert != null){
				sb.append(",");
			}
			sb.append("\"badge\":").append(this.badge);
		}
		if (this.sound != null){
			if ((this.alert != null) || (this.sound != null)){
				sb.append(",");
			}
			sb.append("\"sound\":\"").append(this.sound).append("\"");
		}
		sb.append("}}");
		return sb.toString();
	}
	
	/**
	 * Payload representation in bytes
	 * @return a byte array
	 */
	public byte[] getPayloadAsBytes(){
		return toString().getBytes();
	}
	
}
