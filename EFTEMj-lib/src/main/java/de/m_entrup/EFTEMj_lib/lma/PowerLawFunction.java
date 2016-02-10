package de.m_entrup.EFTEMj_lib.lma;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

public class PowerLawFunction implements ParametricUnivariateFunction {

    @Override
    public double value(double t, double... parameters) {
	return parameters[0] * Math.pow(t, parameters[1]);
    }

    @Override
    public double[] gradient(double t, double... parameters) {
	return new double[] { Math.pow(t, parameters[1]), parameters[0] * Math.pow(t, parameters[1]) * Math.log(t) };
    }
}