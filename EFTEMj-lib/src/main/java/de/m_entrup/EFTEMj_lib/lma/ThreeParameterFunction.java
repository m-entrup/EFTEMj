
package de.m_entrup.EFTEMj_lib.lma;

/**
 * This function results in a power law function, if the third parameter is
 * zero. When used near a plasmon peak, the three parameter function will take
 * into account the additional non power law background, that is introduced by
 * the plasmon peak.
 * 
 * @author Michael Entrup b. Epping
 */
public class ThreeParameterFunction implements EELS_BackgroundFunction {

	@Override
	public double value(double t, double... parameters) {
		return Math.exp(parameters[0] * Math.log(t) + parameters[1] +
			parameters[2] / t);
	}

	@Override
	public double[] gradient(double t, double... parameters) {
		return new double[] { value(t, parameters) * Math.log(t), value(t,
			parameters), value(t, parameters) / t };
	}

	/* (non-Javadoc)
	 * @see de.m_entrup.EFTEMj_lib.lma.EELS_BackgroundFunction#getInitialParameters()
	 */
	@Override
	public double[] getInitialParameters() {
		final double[] initialParameters = { 1, 1, 1 };
		return initialParameters;
	}

	/* (non-Javadoc)
	 * @see de.m_entrup.EFTEMj_lib.lma.EELS_BackgroundFunction#getFunctionName()
	 */
	@Override
	public String getFunctionName() {
		return "Three parameter function";
	}
}
