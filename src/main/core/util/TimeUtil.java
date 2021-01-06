package main.core.util;

/**
 * Found at: https://stackoverflow.com/questions/12125311/how-to-convert-milliseconds-into-hours-and-days
 */
public class TimeUtil
{
	private static final int SECOND = 1000;
	private static final int MINUTE = 60 * SECOND;
	private static final int HOUR = 60 * MINUTE;

	public static String printShortTimeDuration(long millis)
	{
		StringBuilder text = new StringBuilder();
		if(millis > HOUR){
			text.append(millis / HOUR).append(":");
			millis %= HOUR;
		}
		if (millis > MINUTE){
			text.append(millis / MINUTE).append(":");
			millis %= MINUTE;
		}
		if (millis > SECOND) {
			text.append(millis / SECOND);
			millis %= SECOND;
		}
		return text.toString();
	}
}
