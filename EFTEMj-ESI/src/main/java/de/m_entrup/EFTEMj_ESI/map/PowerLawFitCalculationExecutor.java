
package de.m_entrup.EFTEMj_ESI.map;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.SwingWorker;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.gui.MainMenu;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
import de.m_entrup.EFTEMj_ESI.tools.MyTimer;
import ij.IJ;

/**
 * The aim of the {@link PowerLawFitCalculationExecutor} is to split the
 * calculation of the MLE into subtasks. The calculation is independent for each
 * pixel. To limit the effort for thread handling each thread processes a
 * complete image row.
 */
public class PowerLawFitCalculationExecutor extends SwingWorker<Void, Void> {

	/**
	 * The height of the processed images. This corresponds to the number of
	 * subtasks the calculation is split up.
	 */
	private final int height;
	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();

	/**
	 * The constructor disables all {@link JButton} at the {@link MainMenu} and
	 * transmits the number of subtasks to the {@link ThreadInterface}.
	 *
	 * @throws Exception
	 */
	public PowerLawFitCalculationExecutor() throws Exception {
		super();
		height = PluginAPI.getInstance().getDatasetAPI().getHeight();
		ThreadInterface.getInstance().configureThreadChecker(height);
		LogWriter.clearProcessLog();
		DateFormat df;
		df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
			Locale.ENGLISH);
		final String dateAndTime = df.format(new Date());
		LogWriter.writeProcessLog("Starting elemental map calculation (" +
			dateAndTime + "):", LogWriter.MAP);
		DecimalFormat decF = new DecimalFormat("#.###", DecimalFormatSymbols
			.getInstance(Locale.ENGLISH));
		decF = new DecimalFormat("0.0##E0", DecimalFormatSymbols.getInstance(
			Locale.ENGLISH));
		LogWriter.writeProcessLog(
			"The break condition for r is a change of less than " + decF.format(
				PowerLawFitCalculation.getEpsilon()), LogWriter.MAP);
		String preEdgeImages = "";
		String postEdgeImages = "";
		for (int i = 0; i < datasetAPI.getStackSize(); i++) {
			if (i < datasetAPI.getEdgeIndex()) {
				preEdgeImages += datasetAPI.getEFTEMImage(i).getLabel() + ", ";
			}
			else {
				postEdgeImages += datasetAPI.getEFTEMImage(i).getLabel() + ", ";
			}
		}
		preEdgeImages = preEdgeImages.substring(0, preEdgeImages.length() - 2);
		postEdgeImages = postEdgeImages.substring(0, postEdgeImages.length() - 2);
		LogWriter.writeProcessLog("Pre edge images: " + preEdgeImages,
			LogWriter.MAP);
		LogWriter.writeProcessLog("Post edge images: " + postEdgeImages,
			LogWriter.MAP);
	}

	@Override
	protected Void doInBackground() throws Exception {
		IJ.showStatus("Starting power law fit calculation...");
		MyTimer.start();
		for (int y = 0; y < height; y++) {
			final PowerLawFitCalculation mle = new PowerLawFitCalculation(y);
			mle.start();
		}
		return null;
	}

}
