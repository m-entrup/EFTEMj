
package de.m_entrup.EFTEMj_ESI.dqe;

import ij.plugin.Binner;
import ij.process.FloatProcessor;
import ij.process.ImageStatistics;

public class DQE_Calc extends Thread {

	protected static DQE_ByBinning parent;
	private int binning;
	private DatasetDQE dataset;

	private void calcDQE() {
		final FloatProcessor fp = (FloatProcessor) new Binner().shrink(
			DatasetDQE.fp, binning, binning, Binner.SUM);
		fp.findMinAndMax();
		final ImageStatistics statistics = fp.getStatistics();
		dataset.mean = statistics.mean;
		dataset.var = statistics.stdDev * statistics.stdDev;
		// float var = 0;
		// float value;
		// for (int y = 0; y < fp.getHeight(); y++) {
		// for (int x = 0; x < fp.getWidth(); x++) {
		// value = fp.getf(x, y);
		// var += (dataset.mean - value) * (dataset.mean - value)
		// / ((fp.getWidth() * fp.getHeight() - 1));
		// }
		// }
		// fp = null;
		// dataset.var = var;
		dataset.dqe = dataset.mean / dataset.var * DatasetDQE.sensitivity;
		System.out.println(String.format("The DQE of Binning %d is %f.", binning,
			dataset.dqe));
	}

	@Override
	public void run() {
		while (!parent.isInputEmpty()) {
			dataset = parent.getInput();
			if (dataset == null) return;
			binning = dataset.binning;
			dataset.mean = 0;
			dataset.var = 0;
			System.out.println(String.format(
				"%s starts the calculation for binning %d.", this.toString(), binning));
			calcDQE();
			parent.saveOutput(dataset);
		}
		parent.removeThread();
	}
}
