package net.madmanmarkau.Silence;

public class Util {
	public static String newLine() {
		return System.getProperty("line.separator");
	}

	public static String joinString(String string[], String delimiter) {
		StringBuilder builder = new StringBuilder();
		
		for (int index = 0; index < string.length; index++) {
			builder.append(string[index]);
			
			if (index < string.length - 1) {
				builder.append(delimiter);
			}
		}

		return builder.toString();
	}

	public static String joinString(String string[], int startLocation, String delimiter) {
		StringBuilder builder = new StringBuilder();
		
		for (int index = startLocation; index < string.length; index++) {
			builder.append(string[index]);
			
			if (index < string.length - 1) {
				builder.append(delimiter);
			}
		}

		return builder.toString();
	}

	public static String joinString(String string[], int startLocation, int endLocation, String delimiter) {
		StringBuilder builder = new StringBuilder();
		
		for (int index = startLocation; index < string.length && index <= endLocation; index++) {
			builder.append(string[index]);
			
			if (index < string.length - 1) {
				builder.append(delimiter);
			}
		}

		return builder.toString();
	}

	/**
	 * Decodes a duration string in any of the following formats:
	 * "1d2h3m4s"
	 * "1:02:03:04"
	 * "93784"
	 * @param time String denoting duration to decode.
	 * @return Integer specifying the number of seconds in this duration.
	 */
	public static int decodeTime(String time) {
		try {
			int silenceTime = Integer.parseInt(time);

			if (silenceTime >= 0) return silenceTime;
		} catch (Exception e) {}

		try {
			int dayIndex = time.indexOf("d");
			int hourIndex = time.indexOf("h");
			int minuteIndex = time.indexOf("m");
			int secondIndex = time.indexOf("s");
			int lastIndex = 0;

			if (dayIndex > -1 || hourIndex > -1 || minuteIndex > -1 || secondIndex > -1) {
				int timeInSeconds = 0;
				
				if (dayIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, dayIndex)) * 60 * 60 * 24;
					lastIndex = dayIndex + 1;
				}
				
				if (hourIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, hourIndex)) * 60 * 60;
					lastIndex = hourIndex + 1;
				}
				
				if (minuteIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, minuteIndex)) * 60;
					lastIndex = minuteIndex + 1;
				}
				
				if (secondIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, secondIndex));
					lastIndex = secondIndex + 1;
				}
	
				if (timeInSeconds >= 0) return timeInSeconds;
			}
		} catch (Exception e) {}

		try {
			int timeInSeconds = 0;
			int lastIndex = 0;

			int thisIndex;
			
			thisIndex = time.indexOf(":", lastIndex);

			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}

			thisIndex = time.indexOf(":", lastIndex);
			timeInSeconds *= 60;
			
			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}

			thisIndex = time.indexOf(":", lastIndex);
			timeInSeconds *= 60;
			
			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}

			thisIndex = time.indexOf(":", lastIndex);
			timeInSeconds *= 24;
			
			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}
			
		} catch (Exception e) {}

		return -1;
	}
}
