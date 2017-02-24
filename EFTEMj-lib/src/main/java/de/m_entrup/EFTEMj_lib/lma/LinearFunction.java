
package de.m_entrup.EFTEMj_lib.lma;

/**
 * There is no physical reason for using a linear background model. There are
 * acceptable results only for high energy losses (>1000eV).
 *
 * @author Michael Entrup b. Epping
 */
public class LinearFunction implements EELS_BackgroundFunction {

	@Override
	public double value(final double t, final double... parameters) {
		return parameters[0] + t * parameters[1];
	}

	@Override
	public double[] gradient(final double t, final double... parameters) {
		return new double[] { 1, t };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.m_entrup.EFTEMj_lib.lma.EELS_BackgroundFunction#getInitialParameters()
	 */
	@Override
	public double[] getInitialParameters() {
		final double[] initialParameters = { 1, 1 };
		return initialParameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.m_entrup.EFTEMj_lib.lma.EELS_BackgroundFunction#getFunctionName()
	 */
	@Override
	public String getFunctionName() {
		return "Linear function";
	}
}
