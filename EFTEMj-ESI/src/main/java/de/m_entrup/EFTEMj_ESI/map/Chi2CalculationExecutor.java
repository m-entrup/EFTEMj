
package de.m_entrup.EFTEMj_ESI.map;

import java.util.Locale;

import javax.swing.SwingWorker;

import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;
import de.m_entrup.EFTEMj_ESI.tools.DisplyProcessLogTool;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
import de.m_entrup.EFTEMj_ESI.tools.MyTimer;
import ij.IJ;

/**
 * The Chi² gives information about the quality of the background calculation.
 * The input values are the measured pre-edge images and the calculated pre-edge
 * background. The aim of {@link Chi2CalculationExecutor} is to split the
 * calculation of Chi² into subtasks. Each task processes 1 image row.
 */
public class Chi2CalculationExecutor extends SwingWorker<Void, Void> {

	/**
	 * The height of the processed images. This corresponds to the number of
	 * subtasks the calculation is split up.
	 */
	private int height;

	/**
	 * the calculation of Chi² does only start if there are more than 2 post-edge
	 * images. For 2 post-edge images the coefficient is always 1. The constructor
	 * transmits the number of subtasks to the {@link ThreadInterface}.
	 *
	 * @throws Exception
	 */
	public Chi2CalculationExecutor() throws Exception {
		super();
		if (PluginAPI.getInstance().getDatasetAPI().getEdgeIndex() > 2) {
			height = PluginAPI.getInstance().getDatasetAPI().getHeight();
			ThreadInterface.getInstance().configureThreadChecker(height);
		}
	}

	@Override
	protected Void doInBackground() throws Exception {
		IJ.showStatus("Starting Chi² calculation...");
		if (PluginAPI.getInstance().getDatasetAPI().getEdgeIndex() > 2) {
			for (int y = 0; y < height; y++) {
				final Chi2Calculation chi2 = new Chi2Calculation(y);
				chi2.run();
			}
		}
		else {
			// TODO Create a finished() method to reduce redundancy.
			// if chi² can be calculated the log is created by an instance of
			// the class Chi2Finisher
			final Float timeInSeconds = (float) (MyTimer.stop()) / 1000;
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"Time required (total): %.2f s", timeInSeconds), LogWriter.MAP);
			// TODO add "show log" button to the MapResultPanel
			DisplyProcessLogTool.showExportDialog("Map_" + PluginAPI.getInstance()
				.getDatasetAPI().getImagePlusShortTitle());
		}
		return null;
	}

}
