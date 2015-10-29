
package de.m_entrup.EFTEMj_ESI.threading;

import java.awt.Point;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JOptionPane;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.driftcorrection.CrossCorrelation;
import de.m_entrup.EFTEMj_ESI.gui.MainMenu;
import de.m_entrup.EFTEMj_ESI.gui.MapResultPanel;
import de.m_entrup.EFTEMj_ESI.map.BGCalculation;
import de.m_entrup.EFTEMj_ESI.map.BGCalculationExecutor;
import de.m_entrup.EFTEMj_ESI.map.Chi2Calculation;
import de.m_entrup.EFTEMj_ESI.map.Chi2CalculationExecutor;
import de.m_entrup.EFTEMj_ESI.map.CoeffOfDetCalculation;
import de.m_entrup.EFTEMj_ESI.map.CoeffOfDetCalculationExecutor;
import de.m_entrup.EFTEMj_ESI.map.MapCalculation;
import de.m_entrup.EFTEMj_ESI.map.MapCalculationExecutor;
import de.m_entrup.EFTEMj_ESI.map.PowerLawFitCalculation;
import de.m_entrup.EFTEMj_ESI.map.SNRCalculation;
import de.m_entrup.EFTEMj_ESI.map.SNRCalculationExecutor;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import de.m_entrup.EFTEMj_ESI.tools.DisplyProcessLogTool;
import de.m_entrup.EFTEMj_ESI.tools.ImagePlusTool;
import de.m_entrup.EFTEMj_ESI.tools.ImageShifter;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
import de.m_entrup.EFTEMj_ESI.tools.MyTimer;
import de.m_entrup.EFTEMj_ESI.tools.Statistics;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.Blitter;
import ij.process.FloatProcessor;

/**
 * The {@link ThreadInterface} is the main part of my threading framework. Each
 * executor has to call the method <code>configureThreadChecker(int)</code> to
 * specify the number of threads he will create. the constructor of each thread
 * has to call <code>addThread()</code>. This ensures that the number of threads
 * does not exceed the number of CPU-cores. When a thread has finished it's
 * calculations <code>removeThread(int)</code> hat to be called. A waiting
 * thread will continue or, if all sub tasks are finished, a finisher thread
 * (implemented as a inner class of {@link ThreadInterface}) is started.
 */
public class ThreadInterface {

	/**
	 * The {@link BGFinisher} is started when the {@link BGCalculation} has been
	 * finished. The next step is initialised, the {@link MapCalculation}. At the
	 * MapResultPanel two buttons are enabled.
	 */
	private class BGFinisher implements Runnable {

		@Override
		public void run() {
			final DatasetAPI dataset = PluginAPI.getInstance().getDatasetAPI();
			final float[] errorMap = dataset.getErrorMap();
			Statistics statistics;
			for (int i = 0; i < dataset.getEdgeIndex(); i++) {
				statistics = new Statistics(dataset.getRelBackgroundImages()[i]
					.getPixels(), errorMap);
				LogWriter.writeProcessLog(dataset.getRelBackgroundImages()[i]
					.getLabel() + " " + statistics.getAllAsString(), LogWriter.MAP);
			}
			final Float timeInSeconds = (float) (MyTimer.interval()) / 1000;
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"Time required (BG calc.): %.2f s", timeInSeconds), LogWriter.MAP);
			MapCalculationExecutor executor;
			try {
				executor = new MapCalculationExecutor();
				executor.execute();
			}
			catch (final Exception e) {
				mainMenu.closeMapResultPanel();
				mainMenu.enableMainMenuButtons();
				JOptionPane.showMessageDialog(null, e);
			}
			mainMenu.enableMapResultButton("key_showBG");
			mainMenu.enableMapResultButton("key_showRelBG");
		}
	}

	/**
	 * The {@link Chi2Finisher} is used when the {@link Chi2Calculation} has
	 * finished. An instance of this class enables buttons at the
	 * {@link MapResultPanel}.
	 */
	private class Chi2Finisher implements Runnable {

		@Override
		public void run() {
			final DatasetAPI dataset = PluginAPI.getInstance().getDatasetAPI();
			final float[] errorMap = dataset.getErrorMap();
			final Statistics statistics = new Statistics(dataset.getChi2(), errorMap);
			LogWriter.writeProcessLog("Chi� " + statistics.getAllAsString(),
				LogWriter.MAP);
			Float timeInSeconds = (float) (MyTimer.interval()) / 1000;
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"Time required (Chi�): %.2f s", timeInSeconds), LogWriter.MAP);
			// TODO Create a finished() method to reduce redundancy.
			timeInSeconds = (float) (MyTimer.stop()) / 1000;
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"Time required (total): %.2f s", timeInSeconds), LogWriter.MAP);
			mainMenu.enableMapResultButton("key_showChi2");
			mainMenu.enableMapResultButton("key_closeMapResult");
			// TODO add "show log" button to the MapResultPanel
			DisplyProcessLogTool.showExportDialog("Map_" + dataset
				.getImagePlusShortTitle());
		}
	}

	/**
	 * The {@link CODFinisher} is used when the {@link CoeffOfDetCalculation} has
	 * finished. An instance of this class enables buttons at the
	 * {@link MapResultPanel}. Additionally the {@link Chi2CalculationExecutor} is
	 * initialised to start the {@link Chi2Calculation}.
	 */
	private class CODFinisher implements Runnable {

		@Override
		public void run() {
			final DatasetAPI dataset = PluginAPI.getInstance().getDatasetAPI();
			final float[] errorMap = dataset.getErrorMap();
			final Statistics statistics = new Statistics(dataset.getCoeffOFDet(),
				errorMap);
			LogWriter.writeProcessLog("coefficient of determination " + statistics
				.getAllAsString(), LogWriter.MAP);
			final Float timeInSeconds = (float) (MyTimer.interval()) / 1000;
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"Time required (COD): %.2f s", timeInSeconds), LogWriter.MAP);
			Chi2CalculationExecutor executor;
			try {
				executor = new Chi2CalculationExecutor();
				executor.execute();
			}
			catch (final Exception e) {
				mainMenu.closeMapResultPanel();
				mainMenu.enableMainMenuButtons();
				JOptionPane.showMessageDialog(null, e);
			}
			mainMenu.enableMapResultButton("key_showCoeffOfDet");
		}
	}

	/**
	 * {@link CROSSFinisher} is the finisher class of the drift correction. The
	 * first part of the drift correction is the calculation of the normalised
	 * crosscorrelation coefficient, this is done by {@link CrossCorrelation}. If
	 * the coefficients are calculated, this class is initialised and determines
	 * the drift from the coefficient map. Ongoing the images are shifted and
	 * finally displayed.
	 */
	private class CROSSFinisher implements Runnable {

		DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();

		/**
		 * this method is used to create a {@link ImageStack} from an array of
		 * {@link FloatProcessor}s.
		 *
		 * @param array An array of {@link FloatProcessor}s.
		 * @return The {@link ImageStack} that contains the {@link FloatProcessor}s
		 *         of the parameter array.
		 */
		private ImageStack arrayToStack(final FloatProcessor[] array) {
			final ImageStack stack = new ImageStack(array[0].getWidth(), array[0]
				.getHeight());
			for (int i = 0; i < array.length; i++) {
				stack.addSlice(datasetAPI.getSliceLabel(i), array[i]);
			}
			return stack;
		}

		@Override
		public void run() {
			final int templateIndex = datasetAPI.getTemplateIndex();
			final ImageStack imageStack = datasetAPI.getImagePlus().getStack();
			final FloatProcessor[] correctedImages = new FloatProcessor[datasetAPI
				.getStackSize()];
			correctedImages[templateIndex] = new FloatProcessor(datasetAPI.getWidth(),
				datasetAPI.getHeight());
			correctedImages[templateIndex].copyBits(imageStack.getProcessor(
				templateIndex + 1), 0, 0, Blitter.COPY);
			for (int i = 0; i < datasetAPI.getStackSize(); i++) {
				if (i != templateIndex) {
					final Point maxPos = ImageShifter.calcShift(datasetAPI
						.getCorrelationCoefficientsAsFP()[i]);
					// 'x' and 'y' are shift values.
					LogWriter.writeProcessLog("Drift of \"" + imageStack
						.getShortSliceLabel(i + 1) + "\" " + ": x=" + -maxPos.x + " y=" +
						-maxPos.y, LogWriter.DRIFT);
					correctedImages[i] = ImageShifter.moveImage(maxPos,
						(FloatProcessor) imageStack.getProcessor(i + 1));
					// When an image is changed you have to call this method to
					// get the
					// right min & max (used for display limits).
					imageStack.getProcessor(i + 1).resetMinAndMax();
				}
			}
			final ImageStack correctedStack = arrayToStack(correctedImages);
			final ImagePlus driftStackWin = new ImagePlus("DK-" + datasetAPI
				.getImagePlus().getTitle(), correctedStack);
			driftStackWin.setDisplayRange(WindowManager.getCurrentImage()
				.getDisplayRangeMin(), WindowManager.getCurrentImage()
					.getDisplayRangeMax());
			final Float timeInSeconds = (float) (MyTimer.stop()) / 1000;
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"Time required: %.2f s", timeInSeconds), LogWriter.DRIFT);
			driftStackWin.show();
			// TODO Add a non modal dialog that offers the options to save the
			// corrected stack, redo the correction, or cancel without saving.
			LogWriter.writeLog("The drift at stack \"" + driftStackWin
				.getShortTitle() + "\" has been corrected.");
			if (driftStackWin.getOriginalFileInfo() != null) {
				final String path = driftStackWin.getOriginalFileInfo().directory +
					driftStackWin.getOriginalFileInfo().fileName;
				LogWriter.writeProcessLog("The stack has been saved: " + path,
					LogWriter.DRIFT);
				datasetAPI.deleteDriftDataset();
			}
			PluginAPI.getInstance().enableMainMenuButtons();
			DisplyProcessLogTool.showExportDialog("DK_" + datasetAPI
				.getImagePlusShortTitle());
			ImagePlusTool.saveImagePlus(driftStackWin, true);
		}
	}

	/**
	 * The {@link MapFinisher} is started when the {@link MapCalculation} has been
	 * finished. The next step is initialised, the {@link SNRCalculation}. At the
	 * MapResultPanel another button is enabled.
	 */
	private class MapFinisher implements Runnable {

		@Override
		public void run() {
			final DatasetAPI dataset = PluginAPI.getInstance().getDatasetAPI();
			final float[] errorMap = dataset.getErrorMap();
			Statistics statistics;
			for (int i = 0; i < dataset.getMap().length; i++) {
				statistics = new Statistics(dataset.getMap()[i].getPixels(), errorMap);
				LogWriter.writeProcessLog(dataset.getMap()[i].getLabel() + " " +
					statistics.getAllAsString(), LogWriter.MAP);
			}
			final Float timeInSeconds = (float) (MyTimer.interval()) / 1000;
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"Time required (Map calc): %.2f s", timeInSeconds), LogWriter.MAP);
			SNRCalculationExecutor executor;
			try {
				executor = new SNRCalculationExecutor();
				executor.execute();
			}
			catch (final Exception e) {
				mainMenu.closeMapResultPanel();
				mainMenu.enableMainMenuButtons();
				JOptionPane.showMessageDialog(null, e);
			}
			mainMenu.enableMapResultButton("key_showMap");
		}
	}

	/**
	 * When the MLE calculation is finished, the next calculation
	 * {@link BGCalculation} is started. As the {@link PowerLawFitCalculation} is
	 * the first part of the elemental-map calculation the MapResultPanel will be
	 * displayed by this finisher Thread. The buttons of the already available
	 * results are enabled.
	 */
	private class MLEFinisher implements Runnable {

		@Override
		public void run() {
			final DatasetAPI dataset = PluginAPI.getInstance().getDatasetAPI();
			final float[] errorMap = dataset.getErrorMap();
			final long[] errorHistogram = new long[256];
			for (int i = 0; i < errorMap.length; i++) {
				errorHistogram[(int) Math.ceil(errorMap[i])]++;
			}
			final long pixelCount = errorMap.length;
			long errorCount = errorHistogram[PluginConstants.ERROR__NON];
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"no error: %d pixels (%s)", errorCount, NumberFormat
					.getPercentInstance().format((double) errorCount / pixelCount)),
				LogWriter.MAP);
			errorCount = errorHistogram[PluginConstants.ERROR__SIGNAL_LESS_THAN_ZERO];
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"signal is less than 0: %d pixels (%s)", errorCount, NumberFormat
					.getPercentInstance().format((double) errorCount / pixelCount)),
				LogWriter.MAP);
			errorCount = errorHistogram[PluginConstants.ERROR__R_LESS_THAN_LIMIT];
			final DecimalFormat df = new DecimalFormat("#.###", DecimalFormatSymbols
				.getInstance(Locale.ENGLISH));
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH, "r less than " +
				df.format(PowerLawFitCalculation.getRLimit()) + ": %d pixels (%s)",
				errorCount, NumberFormat.getPercentInstance().format(
					(double) errorCount / pixelCount)), LogWriter.MAP);
			errorCount =
				errorHistogram[PluginConstants.ERROR__A_NOT_POSSIBLE_TO_CALCULATE];
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"can't calculate a: %d pixels (%s)", errorCount, NumberFormat
					.getPercentInstance().format((double) errorCount / pixelCount)),
				LogWriter.MAP);
			errorCount = errorHistogram[PluginConstants.ERROR__CONVERGENCE];
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"no convergence at MLE calc.: %d pixels (%s)", errorCount, NumberFormat
					.getPercentInstance().format((double) errorCount / pixelCount)),
				LogWriter.MAP);
			errorCount = errorHistogram[PluginConstants.ERROR__NAN];
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"NAN error: %d pixels (%s)", errorCount, NumberFormat
					.getPercentInstance().format((double) errorCount / pixelCount)),
				LogWriter.MAP);
			Statistics statistics = new Statistics(dataset.getRMap(), errorMap);
			LogWriter.writeProcessLog("r " + statistics.getAllAsString(),
				LogWriter.MAP);
			statistics = new Statistics(dataset.getAMap(), errorMap);
			LogWriter.writeProcessLog("a " + statistics.getAllAsString(),
				LogWriter.MAP);
			final Float timeInSeconds = (float) (MyTimer.interval()) / 1000;
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"Time required (MLE): %.2f s", timeInSeconds), LogWriter.MAP);
			BGCalculationExecutor executor;
			try {
				executor = new BGCalculationExecutor();
				executor.execute();
			}
			catch (final Exception e) {
				mainMenu.closeMapResultPanel();
				mainMenu.enableMainMenuButtons();
				JOptionPane.showMessageDialog(null, e);
			}
			mainMenu.enableMapResultButton("key_showErrorMap");
			mainMenu.enableMapResultButton("key_showR");
			mainMenu.enableMapResultButton("key_showA");
		}
	}

	/**
	 * The {@link SNRFinisher} is started when the {@link SNRCalculation} has been
	 * finished. The next step is initialised, the {@link CoeffOfDetCalculation}.
	 * At the MapResultPanel two buttons are enabled.
	 */
	private class SNRFinisher implements Runnable {

		@Override
		public void run() {
			final DatasetAPI dataset = PluginAPI.getInstance().getDatasetAPI();
			final float[] errorMap = dataset.getErrorMap();
			Statistics statistics;
			for (int i = 0; i < dataset.getSNR().length; i++) {
				statistics = new Statistics(dataset.getSigma2()[i].getPixels(),
					errorMap);
				LogWriter.writeProcessLog(dataset.getSigma2()[i].getLabel() + " " +
					statistics.getAllAsString(), LogWriter.MAP);
				statistics = new Statistics(dataset.getSNR()[i].getPixels(), errorMap);
				LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
					"%s with DQE=1 %s", dataset.getSNR()[i].getLabel(), statistics
						.getAllAsString()), LogWriter.MAP);
			}
			final Float timeInSeconds = (float) (MyTimer.interval()) / 1000;
			LogWriter.writeProcessLog(String.format(Locale.ENGLISH,
				"Time required (SNR): %.2f s", timeInSeconds), LogWriter.MAP);
			CoeffOfDetCalculationExecutor executor;
			try {
				executor = new CoeffOfDetCalculationExecutor();
				executor.execute();
			}
			catch (final Exception e) {
				mainMenu.closeMapResultPanel();
				mainMenu.enableMainMenuButtons();
				JOptionPane.showMessageDialog(null, e);
			}
			mainMenu.enableMapResultButton("key_showSNR");
			mainMenu.enableMapResultButton("key_showSigma2");
		}
	}

	/**
	 * There is only one instance of {@link ThreadInterface}. That is why the
	 * Singleton pattern is used.
	 */
	private static final ThreadInterface INSTANCE = new ThreadInterface();
	/**
	 * A shortcut to access the instance of {@link MainMenu}. Some finisher
	 * threads are manipulationg the main menu.
	 */
	private final MainMenu mainMenu = PluginAPI.getInstance().getMainMenu();
	// This constants are used to determinate the finisher thread that has to be
	// used when a calculation ends.
	/**
	 * {@link MLEFinisher}, used by {@link PowerLawFitCalculation}.
	 */
	public static final int MLE = 1;
	/**
	 * {@link BGFinisher}, uses by {@link BGCalculation}.
	 */
	public static final int BG = 2;

	/**
	 * {@link MapFinisher}, used by {@link MapCalculation}.
	 */
	public static final int MAP = 3;

	/**
	 * {@link SNRFinisher}, used by {@link SNRCalculation}.
	 */
	public static final int SNR = 4;

	/**
	 * {@link CODFinisher}, used by {@link CoeffOfDetCalculation}.
	 */
	public static final int COD = 5;

	/**
	 * {@link CROSSFinisher}, used by {@link CrossCorrelation}.
	 */
	public static final int CROSS = 6;

	/**
	 * {@link Chi2Finisher}, used by {@link Chi2Calculation}.
	 */
	public static final int CHI2 = 7;

	/**
	 * Instead of using the constructor you can get an instance of
	 * {@link ThreadInterface} by using this method.
	 *
	 * @return The only instance of {@link ThreadInterface}
	 */
	public static ThreadInterface getInstance() {
		return INSTANCE;
	}

	/**
	 * An instance of {@link ThreadChecker} is used to control the number of
	 * parallel threads. Additionally it's used to check if the calculation has
	 * finished.
	 */
	private ThreadChecker threadChecker;

	/**
	 * A private constructor.
	 */
	private ThreadInterface() {
		super();
	}

	/**
	 * Uses <code>threadChecker.addThread()</code> and handles the exception.
	 */
	public void addThread() {
		try {
			threadChecker.addThread();
		}
		catch (final InterruptedException e) {
			// TODO export String
			LogWriter.writeLog("Fehler! (wait interrupted)");
			e.printStackTrace();
		}
	}

	/**
	 * A new instance of {@link ThreadChecker} is created that will handle the
	 * number of running threads.
	 *
	 * @param subTasks The number of subtasks.
	 * @throws Exception
	 */
	public void configureThreadChecker(final int subTasks) throws Exception {
		if (threadChecker == null) {
			threadChecker = new ThreadChecker(subTasks);
		}
		else {
			throw new Exception(PluginMessages.getString(
				"Error.ConfiguteThreadChecker"));
		}
	}

	public void deleteThreadChecker() {
		threadChecker = null;
	}

	/**
	 * Uses <code>threadChecker.removeThread()</code> and creates the
	 * {@link MapResultPanel} if <code>threadChecker.isFinished() == true</code>.
	 */
	public synchronized void removeThread(final int type) {
		threadChecker.removeThread();
		if (threadChecker.isFinished()) {
			threadChecker = null;
			switch (type) {
				case MLE:
					new Thread(new MLEFinisher()).run();
					break;
				case BG:
					new Thread(new BGFinisher()).run();
					break;
				case MAP:
					new Thread(new MapFinisher()).run();
					break;
				case SNR:
					new Thread(new SNRFinisher()).run();
					break;
				case COD:
					new Thread(new CODFinisher()).run();
					break;
				case CROSS:
					new Thread(new CROSSFinisher()).run();
					break;
				case CHI2:
					new Thread(new Chi2Finisher()).run();
					break;
				default:
					break;
			}
		}
	}
}
