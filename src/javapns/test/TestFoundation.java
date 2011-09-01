package javapns.test;

import org.apache.log4j.*;

public class TestFoundation {

	
	
	static boolean verifyCorrectUsage(Class classe, String[] argumentsRecus, String ... argumentsAttendus) {
		if (argumentsRecus == null || argumentsRecus.length < argumentsAttendus.length) {
			StringBuilder message = new StringBuilder("Usage: ");
			message.append(classe.getName());
			for (String argumentsAttendu : argumentsAttendus) {
				message.append(" <");
				message.append(argumentsAttendu);
				message.append(">");
			}
			return false;
		}
		return true;
	}


	static void configureBasicLogging() {
		try {
			BasicConfigurator.configure();
		} catch (Exception e) {
		}
	}

	
	
}
