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

package de.m_entrup.EFTEMj_ESI.map;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;

/**
 * This class calculates the parameter of the background model pixel by pixel.
 * The used background model is the power law. Each instance of
 * {@link PowerLawFitCalculation} processes an image row and saves the result.
 * To calculate the background the maximum likelihood estimation is used. All
 * errors that occur during the calculation are classified and logged.
 */
public class PowerLawFitCalculation extends Thread {

	private final int DEBUGCODE = 1;
	private AbstractFitRoutine typeOfFit;
	/**
	 * The exit condition for the MLE. There is a static method to change this
	 * value.
	 */
	private static float epsilon = PluginConstants.EPSILON;

	/**
	 * @return The current value of epsilon.
	 */
	public static float getEpsilon() {
		return epsilon;
	}

	/**
	 * The exit condition is saved as a static field so each instance can access
	 * it. This method is used to change the value.
	 *
	 * @param newEpsilon
	 *            The new exit condition that is set by the GUI.
	 */
	public static void setEpsilon(final float newEpsilon) {
		epsilon = newEpsilon;
	}

	/**
	 * The second parameter of the power law. It is derived from r.
	 */
	private float[] a;
	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * This field stores the error type of each pixel before it is saved.
	 */
	short[] errorType;
	/**
	 * The parameter of the power law that is calculated. This field saves the
	 * result of each x-position of the processed image line.
	 */
	private float[] r;

	/**
	 * A shortcut to access the instance of {@link ThreadInterface}.
	 */
	private final ThreadInterface threadInterface = ThreadInterface.getInstance();
	/**
	 * The y coordinate of the processed pixel.
	 */
	private final int y;

	/**
	 * The constructor creates a new instance of {@link PowerLawFitCalculation}
	 * for the parameter calculation of each pixel at an image row.
	 *
	 * @param y
	 *            The image row that is processed by the new instance of
	 *            {@link PowerLawFitCalculation}.
	 */
	public PowerLawFitCalculation(final int y) {
		super();
		threadInterface.addThread();
		this.y = y;
	}

	@Override
	public void run() {
		selectTypeOfFit();
		final int width = datasetAPI.getWidth();
		r = new float[width];
		a = new float[width];
		errorType = new short[width];
		for (int x = 0; x < width; x++) {
			final int index = x + y * width;
			errorType[x] = typeOfFit.calculateByPixel(index);
			r[x] = typeOfFit.getR(index);
			a[x] = typeOfFit.getA(index);
		}
		// ToDo Call a instance of FitCalculation
		datasetAPI.saveR(y * width, r);
		datasetAPI.saveA(y * width, a);
		datasetAPI.saveError(y * width, errorType);
		threadInterface.removeThread(ThreadInterface.MLE);
	}

	private void selectTypeOfFit() {
		switch (DEBUGCODE) {
		case 1:
			typeOfFit = new MLERoutine(epsilon);
			break;
		default:
			typeOfFit = new MLERoutine(epsilon);
			break;
		}
	}

}
