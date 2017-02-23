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

import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;

/**
 * Each calculation creates an object of this class to manage the number of
 * parallel working threads.
 */
public class ThreadChecker {

	/**
	 * I had some problems with <code>wait()</code> thats why i created this
	 * class. All threads will wait at an instance of it.
	 *
	 * @author Michael Epping
	 */
	private static class WaitRoom extends Thread {

		/**
		 * <code>wait()</code> and <code>notify()</code> cannont be called at an
		 * static method.
		 */
		private static final WaitRoom INSTANCE = new WaitRoom();

		/**
		 * <code>notify()</code> is called.
		 */
		private synchronized void doNotify() {
			notify();
		}

		/**
		 * <code>notifyAll()</code> is called.
		 */
		private synchronized void doNotifyAll() {
			notifyAll();
		}

		/**
		 * <code>wait()</code> is called.
		 *
		 * @throws InterruptedException
		 *             If <code>wait()</code> is interrupted
		 */
		private synchronized void doWait() throws InterruptedException {
			wait();
		}
	}

	/**
	 * The number of threads that are running.
	 */
	private int threadCount;
	/**
	 * The number of subtasks that have to be done.
	 */
	private final int fullSteps;
	/**
	 * The number of completed steps.
	 */
	private int step;

	/**
	 * If all subtasks are completed this field is switched from
	 * <code>false</code> to <code>true</code>.
	 */
	private boolean finished;

	/**
	 * The instance of {@link ThreadChecker} is initialised with the number of
	 * subtasks.
	 *
	 * @param fullSteps
	 *            The number of subtasks
	 */
	public ThreadChecker(final int fullSteps) {
		super();
		this.fullSteps = fullSteps;
		step = 0;
		threadCount = 0;
		finished = false;
	}

	/**
	 * The {@link ThreadChecker} reserves a thread at the
	 * {@link FreeThreadsPool}. If no thread is available
	 *
	 * @throws InterruptedException
	 *             If <code>wait()</code> is interrupted
	 */
	public void addThread() throws InterruptedException {
		// Other tasks can block all free threads. That's why each task always
		// has an extra task.
		while (threadCount != 0 & !FreeThreadsPool.reserveThread()) {
			WaitRoom.INSTANCE.doWait();
		}
		changethreadCount(1);
	}

	/**
	 * Increases or decreases the number of active threads.
	 *
	 * @param mode
	 *            1 to increase and -1 to degrease
	 */
	private synchronized void changethreadCount(final int mode) {
		switch (mode) {
		case 1:
			threadCount++;
			break;
		case -1:
			threadCount--;
			break;
		}
	}

	/**
	 * Returns the value of the field finished.
	 *
	 * @return <code>true</code>, if all sutasks have been completed
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * A thread is removed and a notify is called to start a new subtask. If
	 * this method is called by the last subtask it sets
	 * <code>finished = true</code>.
	 */
	public synchronized void removeThread() {
		changethreadCount(-1);
		FreeThreadsPool.freeThread();
		step++;
		PluginAPI.getInstance().updateProgrssbar((int) (step * 100.0 / fullSteps));
		if (threadCount == 0 & step == fullSteps) {
			finished = true;
		} else {
			// Another task can free many threads. If this happens
			// <code>doNotifyAll()</code> is called.
			if (FreeThreadsPool.getFreeThreads() > 1)
				WaitRoom.INSTANCE.doNotifyAll();
			else
				WaitRoom.INSTANCE.doNotify();
		}
	}
}
