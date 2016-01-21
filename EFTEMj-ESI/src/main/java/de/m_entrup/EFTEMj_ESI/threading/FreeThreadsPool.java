/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2015, Michael Entrup b. Epping
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
		return false;
	}
}
