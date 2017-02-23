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

package de.m_entrup.EFTEMj_ESI.dqe;

import ij.plugin.Binner;
import ij.process.FloatProcessor;
import ij.process.ImageStatistics;

public class DQE_Calc extends Thread {

	protected static DQE_ByBinning parent;
	private int binning;
	private DatasetDQE dataset;

	private void calcDQE() {
		final FloatProcessor fp = (FloatProcessor) new Binner().shrink(DatasetDQE.fp, binning, binning, Binner.SUM);
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
		System.out.println(String.format("The DQE of Binning %d is %f.", binning, dataset.dqe));
	}

	@Override
	public void run() {
		while (!parent.isInputEmpty()) {
			dataset = parent.getInput();
			if (dataset == null)
				return;
			binning = dataset.binning;
			dataset.mean = 0;
			dataset.var = 0;
			System.out.println(String.format("%s starts the calculation for binning %d.", this.toString(), binning));
			calcDQE();
			parent.saveOutput(dataset);
		}
		parent.removeThread();
	}
}
