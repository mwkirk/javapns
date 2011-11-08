package javapns.test;

import org.apache.log4j.*;

class TestFoundation {

	static boolean verifyCorrectUsage(Class testClass, String[] argsProvided, String... argsRequired) {
		if (argsProvided == null) return true;
		int numberOfArgsRequired = countArgumentsRequired(argsRequired);
		if (argsProvided.length < numberOfArgsRequired) {
			String message = getUsageMessage(testClass, argsRequired);
			System.out.println(message);
			return false;
		}
		return true;
	}


	private static String getUsageMessage(Class testClass, String... argsRequired) {
		StringBuilder message = new StringBuilder("Usage: ");
		message.append("java -cp \"<required libraries>\" ");
		message.append(testClass.getName());
		for (String argRequired : argsRequired) {
			boolean optional = argRequired.startsWith("[");
			if (optional) {
				message.append(" [");
				message.append(argRequired.substring(1, argRequired.length() - 1));
				message.append("]");
			} else {
				message.append(" <");
				message.append(argRequired);
				message.append(">");
			}
		}
		return message.toString();
	}


	private static int countArgumentsRequired(String... argsRequired) {
		int numberOfArgsRequired = 0;
		for (String argRequired : argsRequired) {
			if (argRequired.startsWith("[")) break;
			numberOfArgsRequired++;
		}
		return numberOfArgsRequired;
	}


	/**
	 * Enable Log4J with a basic default configuration (console only).
	 */
	public static void configureBasicLogging() {
		try {
			BasicConfigurator.configure();
		} catch (Exception e) {
		}
	}

}
