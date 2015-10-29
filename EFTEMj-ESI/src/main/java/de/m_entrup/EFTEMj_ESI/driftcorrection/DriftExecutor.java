
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
	 * The height of the crosscorrelation coefficient map. The calculation will be
	 * split up into subtasks, each processes a single image row.
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
		ThreadInterface.getInstance().configureThreadChecker((datasetAPI
			.getStackSize() - 1) * height);
		LogWriter.clearProcessLog();
		DateFormat df;
		df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
			Locale.ENGLISH);
		final String dateAndTime = df.format(new Date());
		LogWriter.writeProcessLog("Starting drift correction (" + dateAndTime +
			"):", LogWriter.DRIFT);
		LogWriter.writeProcessLog("The template image is \"" + datasetAPI
			.getShortSliceLabel(datasetAPI.getTemplateIndex()) + "\"",
			LogWriter.DRIFT);
		LogWriter.writeProcessLog("The max drift checked is " + datasetAPI
			.getDelta() + " pixels", LogWriter.DRIFT);
		final Rectangle roi = datasetAPI.getRoi();
		LogWriter.writeProcessLog("The ROI is: x=" + roi.x + ", y=" + roi.y +
			", w=" + roi.width + ", h=" + roi.height, LogWriter.DRIFT);
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
