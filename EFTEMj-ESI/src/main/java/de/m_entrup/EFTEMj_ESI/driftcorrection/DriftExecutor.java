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

package de.m_entrup.EFTEMj_ESI.driftcorrection;

import java.awt.Rectangle;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.SwingWorker;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.threading.ThreadChecker;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
import de.m_entrup.EFTEMj_ESI.tools.MyTimer;

/**
 * The {@link DriftExecutor} extends {@link SwingWorker} and allows to start the
 * calculation of the drift as a background task. A pair of images (one is the
 * reference image) is committed to {@link CrossCorrelation} to calculate the
 * cross correlation coefficients. This is repeated for all available images.
 */
public class DriftExecutor extends SwingWorker<Void, Void> {

	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * The height of the crosscorrelation coefficient map. The calculation will
	 * be split up into subtasks, each processes a single image row.
	 */
	private final int height;

	/**
	 * The constructor determinates the number of subtasks and configures the
	 * {@link ThreadChecker} using {@link ThreadInterface}.
	 *
	 * @throws Exception
	 */
	public DriftExecutor() throws Exception {
		super();
		height = (2 * datasetAPI.getDelta() + 1);
		ThreadInterface.getInstance().configureThreadChecker((datasetAPI.getStackSize() - 1) * height);
		LogWriter.clearProcessLog();
		DateFormat df;
		df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.ENGLISH);
		final String dateAndTime = df.format(new Date());
		LogWriter.writeProcessLog("Starting drift correction (" + dateAndTime + "):", LogWriter.DRIFT);
		LogWriter.writeProcessLog(
				"The template image is \"" + datasetAPI.getShortSliceLabel(datasetAPI.getTemplateIndex()) + "\"",
				LogWriter.DRIFT);
		LogWriter.writeProcessLog("The max drift checked is " + datasetAPI.getDelta() + " pixels", LogWriter.DRIFT);
		final Rectangle roi = datasetAPI.getRoi();
		LogWriter.writeProcessLog("The ROI is: x=" + roi.x + ", y=" + roi.y + ", w=" + roi.width + ", h=" + roi.height,
				LogWriter.DRIFT);
	}

	@Override
	protected Void doInBackground() throws Exception {
		MyTimer.start();
		for (int i = 0; i < datasetAPI.getStackSize(); i++) {
			if (i != datasetAPI.getTemplateIndex()) {
				for (int y = 0; y < height; y++) {
					final CrossCorrelation crossCorrelation = new CrossCorrelation(i, y);
					crossCorrelation.start();
				}
			}
		}
		return null;
	}
}
