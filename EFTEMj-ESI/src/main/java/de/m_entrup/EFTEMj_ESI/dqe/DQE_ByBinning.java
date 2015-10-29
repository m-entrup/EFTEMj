
package de.m_entrup.EFTEMj_ESI.dqe;

import java.awt.FileDialog;
import java.awt.Toolkit;
import java.util.Locale;
import java.util.Stack;

import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import ij.IJ;
import ij.WindowManager;
import ij.gui.Plot;
import ij.process.FloatProcessor;

public class DQE_ByBinning {

	private final String imageTitle;
	private final Stack<DatasetDQE> input;
	private final Stack<DatasetDQE> output;
	private final DatasetDQE[] outputSorted;
	private Plot plot;
	private int progress;
	private String resultAsText;
	private String resultAsTextDe;
	private int running;
	private final String summary;

	public DQE_ByBinning(final int maxBinning, final float sensitivity,
		final FloatProcessor fp, final String imageTitle, final String summary)
	{
		super();
		this.imageTitle = imageTitle;
		this.summary = summary;
		progress = 0;
		input = new Stack<>();
		output = new Stack<>();
		// Index 0 is not used.
		outputSorted = new DatasetDQE[maxBinning + 1];
		DatasetDQE.fp = fp;
		DatasetDQE.sensitivity = sensitivity;
		for (int binning = maxBinning; binning > 0; binning--) {
			input.add(new DatasetDQE(binning));
		}
		DQE_Calc.parent = this;
	}

	public synchronized DatasetDQE getInput() {
		if (input.isEmpty()) return null;
		return input.pop();
	}

	public synchronized boolean isInputEmpty() {
		return input.isEmpty();
	}

	private synchronized void processOutput() {
		DatasetDQE dataset;
		while (running > 0) {
			while (output.isEmpty()) {
				try {
					wait();
				}
				catch (final InterruptedException e) {
					IJ.showMessage("Error", "<html><p>" + e.toString() + "</p></html>");
					e.printStackTrace();
				}
			}
			dataset = output.pop();
			outputSorted[dataset.binning] = dataset;
		}
		for (int i = 1; i < outputSorted.length; i++) {
			System.out.println(String.format("DQE of binning %d: %.6f",
				outputSorted[i].binning, outputSorted[i].dqe));
		}
		// i have to shift the index by 1 because outputSorted is only filled
		// starting at index 1.
		final float xValues[] = new float[outputSorted.length - 1];
		final float yValues[] = new float[outputSorted.length - 1];
		resultAsText = summary;
		resultAsText += "binning ; mean ; stdv ; var ; dqe" +
			PluginConstants.LINE_SEPARATOR;
		resultAsTextDe = resultAsText;
		for (int i = 1; i < outputSorted.length; i++) {
			xValues[i - 1] = outputSorted[i].binning;
			yValues[i - 1] = (float) outputSorted[i].dqe;
			resultAsText += String.format(Locale.ENGLISH, "%d;",
				outputSorted[i].binning);
			resultAsText += String.format(Locale.ENGLISH, "%e;",
				outputSorted[i].mean);
			resultAsText += String.format(Locale.ENGLISH, "%e;", Math.sqrt(
				outputSorted[i].var));
			resultAsText += String.format(Locale.ENGLISH, "%e;", outputSorted[i].var);
			resultAsText += String.format(Locale.ENGLISH, "%e;", outputSorted[i].dqe);
			resultAsText += PluginConstants.LINE_SEPARATOR;
			// ',' instead of '.' to allow an easier evaluation by German users.
			resultAsTextDe += String.format(Locale.GERMAN, "%d;",
				outputSorted[i].binning);
			resultAsTextDe += String.format(Locale.GERMAN, "%e;",
				outputSorted[i].mean);
			resultAsTextDe += String.format(Locale.GERMAN, "%e;", Math.sqrt(
				outputSorted[i].var));
			resultAsTextDe += String.format(Locale.GERMAN, "%e;",
				outputSorted[i].var);
			resultAsTextDe += String.format(Locale.GERMAN, "%e;",
				outputSorted[i].dqe);
			resultAsTextDe += PluginConstants.LINE_SEPARATOR;
		}
		plot = new Plot("DQE", "binning", "DQE", xValues, yValues);
		plot.setFrameSize((int) (0.75 * Toolkit.getDefaultToolkit()
			.getScreenSize().width), (int) (0.75 * Toolkit.getDefaultToolkit()
				.getScreenSize().height));
	}

	public synchronized void removeThread() {
		running--;
	}

	public synchronized void saveOutput(final DatasetDQE result) {
		output.push(result);
		IJ.showProgress(++progress, outputSorted.length - 1);
		notifyAll();
	}

	public void saveResultAndShowPlot() {
		final FileDialog fDialog = new FileDialog(WindowManager.getFrontWindow(),
			"Save DQE results...", FileDialog.SAVE);
		fDialog.setMultipleMode(false);
		fDialog.setDirectory(IJ.getDirectory("image"));
		final String fileName = "DQE_" + imageTitle;
		fDialog.setFile(fileName + ".txt");
		fDialog.setVisible(true);
		if (fDialog.getFile() != null) {
			final String path = fDialog.getDirectory() + System.getProperty(
				"file.separator") + fDialog.getFile();
			IJ.saveString(resultAsText, path);
			final String pathDe = path.substring(0, path.length() - 4) + "_de.txt";
			IJ.saveString(resultAsTextDe, pathDe);
		}
		plot.show();
	}

	public synchronized void waitForResults() {
		final int cores = Runtime.getRuntime().availableProcessors();
		running = 0;
		for (int core = 0; core < cores; core++) {
			System.out.println(String.format("Initialising thread %d.", core));
			running++;
			new DQE_Calc().start();
		}
		System.out.println(String.format("%s starts to wait for the results.", this
			.toString()));
		DQE_ByBinning.this.processOutput();
		System.out.println("End of DQE calculation.");
	}

}
