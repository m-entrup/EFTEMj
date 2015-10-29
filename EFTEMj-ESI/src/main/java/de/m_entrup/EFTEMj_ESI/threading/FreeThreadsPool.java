
package de.m_entrup.EFTEMj_ESI.threading;

/**
 * This class handles the number of threads that are used by calculations done
 * by the elemental map plug-In. All methods and fields are static.
 */
public class FreeThreadsPool {

	/**
	 * The maximum number of threads that is used for calculations.
	 */
	private static final int MAX_THREADS = Runtime.getRuntime()
		.availableProcessors();
	/**
	 * This field is used to handle the number of threads. freeThreads is
	 * initialised with the maximum number of Threads.
	 */
	private static int freeThreads = MAX_THREADS;

	/**
	 * The number of free threads is raised by one.
	 */
	public synchronized static void freeThread() {
		freeThreads++;
	}

	/**
	 * @return The number of free threads
	 */
	public synchronized static int getFreeThreads() {
		return freeThreads;
	}

	/**
	 * It is checked if there are free threads available. If there are free once a
	 * thread is reserved.
	 *
	 * @return <code>true</code> if the reservation was successful,
	 *         <code>false</code> else
	 */
	public synchronized static boolean reserveThread() {
		if (freeThreads > 0) {
			freeThreads--;
			return true;
		}
		else return false;
	}
}
