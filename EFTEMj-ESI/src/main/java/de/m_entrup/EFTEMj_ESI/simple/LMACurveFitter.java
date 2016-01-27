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

import java.util.Collection;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem.Evaluation;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.optim.ConvergenceChecker;

/**
 * @author Michael Entrup b. Epping
 *
 */
public class LMACurveFitter extends AbstractCurveFitter {

	private ParametricUnivariateFunction func;
	private static double limit = 0.001;
	private static double[] initialGuess;

	public LMACurveFitter(ParametricUnivariateFunction function) {
		func = function;
	}

	public double getLimit() {
		return limit;
	}

	public void setLimit(double epsilon) {
		limit = epsilon;
	}

	public LMACurveFitter setInitialGuess(double[] init) {
		if (init.length >= 2) {
			initialGuess = new double[2];
			initialGuess[0] = init[0];
			initialGuess[1] = init[1];
		}
		return this;
	}

	@Override
	protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
		final int len = points.size();
		final double[] target = new double[len];
		final double[] weights = new double[len];
		if (initialGuess == null) {
			initialGuess = new double[2];
			initialGuess[0] = 20;
			initialGuess[1] = -2;
		}

		int i = 0;
		for (WeightedObservedPoint point : points) {
			target[i] = point.getY();
			weights[i] = point.getWeight();
			i += 1;
		}

		final AbstractCurveFitter.TheoreticalValuesFunction model = new AbstractCurveFitter.TheoreticalValuesFunction(
				func, points);

		return new LeastSquaresBuilder().checker(new ConvergenceChecker<LeastSquaresProblem.Evaluation>() {

			@Override
			public boolean converged(int iteration, Evaluation previous, Evaluation current) {
				if (Math.abs(current.getRMS() - previous.getRMS()) < limit)
					return true;
				return false;
			}
		}).maxEvaluations(Integer.MAX_VALUE).maxIterations(Integer.MAX_VALUE).start(initialGuess).target(target)
				.weight(new DiagonalMatrix(weights)).model(model.getModelFunction(), model.getModelFunctionJacobian())
				.build();
	}
}
