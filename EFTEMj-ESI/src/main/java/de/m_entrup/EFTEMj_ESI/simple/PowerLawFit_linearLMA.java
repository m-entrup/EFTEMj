/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2016, Michael Entrup b. Epping
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

package de.m_entrup.EFTEMj_ESI.simple;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

/**
 * @author Michael Entrup b. Epping
 */
public class PowerLawFit_linearLMA extends PowerLawFit {

	private static LMACurveFitter fitter = new LMACurveFitter();;
	private ArrayList<WeightedObservedPoint> points;

	public PowerLawFit_linearLMA(final double[] xValues, final double[] yValues, final double epsilon) {
		super(xValues, yValues, epsilon);
		points = new ArrayList<WeightedObservedPoint>();
		this.xValues = new double[xValues.length];
		this.yValues = new double[yValues.length];
		for (int i = 0; i < xValues.length; i++) {
			this.xValues[i] = Math.log(xValues[i]);
			this.yValues[i] = Math.log(yValues[i]);
			points.add(new WeightedObservedPoint(Math.sqrt(yValues[i]), this.xValues[i], this.yValues[i]));
		}
	}

	@Override
	public void doFit() {
		if (checkPoints(points)) {
			final double[] coeffs = fitter.fit(points);
			errorCode = ERROR_NONE;
			if (Double.isNaN(coeffs[0]))
				errorCode = ERROR_A_NAN;
			if (Double.isInfinite(coeffs[0]))
				errorCode = ERROR_A_INFINITE;
			if (Double.isNaN(coeffs[1]))
				errorCode = ERROR_R_NAN;
			if (Double.isInfinite(coeffs[1]))
				errorCode = ERROR_R_INFINITE;
			if (errorCode == ERROR_NONE) {
				a = Math.exp(coeffs[0]);
				r = coeffs[1];
			} else {
				a = Double.NaN;
				r = Double.NaN;
			}
		} else {
			errorCode = ERROR_CONVERGE;
			a = Double.NaN;
			r = Double.NaN;
		}
		done = true;
	}

	private boolean checkPoints(ArrayList<WeightedObservedPoint> points2Check) {
		for (WeightedObservedPoint point : points2Check) {
			if (Double.isInfinite(point.getY()) | Double.isNaN(point.getY()))
				return false;
		}
		return true;
	}

}

class PowerLawFunction implements ParametricUnivariateFunction {

	@Override
	public double value(double t, double... parameters) {
		return parameters[0] - t * parameters[1];
	}

	@Override
	public double[] gradient(double t, double... parameters) {
		return new double[] { 1, -t };
	}
}

class LMACurveFitter extends AbstractCurveFitter {
	@Override
	protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
		final int len = points.size();
		final double[] target = new double[len];
		final double[] weights = new double[len];
		final double[] initialGuess = { 1, 1 };

		int i = 0;
		for (WeightedObservedPoint point : points) {
			target[i] = point.getY();
			weights[i] = point.getWeight();
			i += 1;
		}

		final AbstractCurveFitter.TheoreticalValuesFunction model = new AbstractCurveFitter.TheoreticalValuesFunction(
				new PowerLawFunction(), points);

		return new LeastSquaresBuilder().maxEvaluations(Integer.MAX_VALUE).maxIterations(Integer.MAX_VALUE)
				.start(initialGuess).target(target).weight(new DiagonalMatrix(weights))
				.model(model.getModelFunction(), model.getModelFunctionJacobian()).build();
	}
}
