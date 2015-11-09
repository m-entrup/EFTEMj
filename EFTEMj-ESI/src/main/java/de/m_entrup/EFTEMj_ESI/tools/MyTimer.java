
package de.m_entrup.EFTEMj_ESI.tools;

/**
 * A small class with static methods to measure the time of a process.
 */
public class MyTimer {

	private static long start = 0;
	private static long interval = 0;

	/**
	 * @return The elapsed time of the current interval.
	 */
	public static long interval() {
		final long temp = interval;
		interval = System.currentTimeMillis();
		return System.currentTimeMillis() - temp;
	}

	/**
	 * Sets the current time as reference point.
	 */
	public static void start() {
		start = System.currentTimeMillis();
		interval = start;
	}

	/**
	 * @return The time elapsed since the call of start.
	 */
	public static long stop() {
		return System.currentTimeMillis() - start;
	}

}
