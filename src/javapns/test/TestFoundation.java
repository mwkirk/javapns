package javapns.test;

import org.apache.log4j.*;

public class TestFoundation {

	static boolean verifyCorrectUsage(Class testClass, String[] argsProvided, String... argsRequired) {
		if (argsProvided == null) return true;
		int numberOfArgsRequired = 0;
		for (String argRequired : argsRequired) {
			if (argRequired.startsWith("[")) break;
			numberOfArgsRequired++;
		}
		if (argsProvided.length < numberOfArgsRequired) {
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
			System.out.println(message);
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
