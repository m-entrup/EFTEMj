
package de.m_entrup.EFTEMj_lib.lma;

public class PowerLawFunction implements EELS_BackgroundFunction {

	@Override
	public double value(double t, double... parameters) {
		return parameters[0] * Math.pow(t, parameters[1]);
	}

	@Override
	public double[] gradient(double t, double... parameters) {
		return new double[] { Math.pow(t, parameters[1]), parameters[0] * Math.pow(
			t, parameters[1]) * Math.log(t) };
	}

	/* (non-Javadoc)
	 * @see de.m_entrup.EFTEMj_lib.lma.EELS_BackgroundFunction#getInitialParameters()
	 */
	@Override
	public double[] getInitialParameters() {
		final double[] initialParameters = { 1, 1 };
		return initialParameters;
	}

	/* (non-Javadoc)
	 * @see de.m_entrup.EFTEMj_lib.lma.EELS_BackgroundFunction#getFunctionName()
	 */
	@Override
	public String getFunctionName() {
		return "Power law function";
	}
}
