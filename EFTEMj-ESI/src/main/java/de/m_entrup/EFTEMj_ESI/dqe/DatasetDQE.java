
package de.m_entrup.EFTEMj_ESI.dqe;

import ij.process.FloatProcessor;

public class DatasetDQE {

	protected static FloatProcessor fp;
	protected static float sensitivity;
	protected int binning;
	protected double dqe;
	protected double mean;
	protected double var;

	public DatasetDQE(final int binning) {
		super();
		this.binning = binning;
	}

}
