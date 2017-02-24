/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2015, Michael Entrup b. Epping <mail@m-entrup.de>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package de.m_entrup.EFTEMj_lib.lma;

import java.util.Arrays;

import lma.LMAMultiDimFunction;

/**
 * This class represents a polynomial in 2D: y(x1,x2). It implements all
 * necessary methods to be used in a Levenberg-Marquardt algorithm.
 *
 * @author Michael Entrup b. Epping
 */
public class Polynomial_2D extends LMAMultiDimFunction {

	/**
	 * The order of the polynomial in x1.
	 */
	protected final int m;
	/**
	 * The order of the polynomial in x2.
	 */
	protected final int n;
	/**
	 * The (m+1)x(n+1) parameters of the polynomial.
	 * <p>
	 * <code>a<sub>00</sub>, a<sub>01</sub>, ... a<sub>0n</sub>, ... a<sub>10</sub>, ... a<sub>mn</sub></code>
	 */
	protected double[] params;

	/**
	 * This constructor creates a new 2D polynomial with given orders and all
	 * parameters = 1.<br />
	 * Use the second constructor to define all parameters yourself.
	 *
	 * @param m
	 *            is the maximal order of x1.
	 * @param n
	 *            is the maximal order of x2.
	 */
	public Polynomial_2D(final int m, final int n) {
		this.m = m;
		this.n = n;
		this.params = new double[(m + 1) * (n + 1)];
		Arrays.fill(params, 1.0);

	}

	/**
	 * @param m
	 *            is the maximal order of x1.
	 * @param n
	 *            is the maximal order of x2.
	 * @param params
	 *            is an array that contains all the necessary parameters. The
	 *            order f the parameters is:
	 *            <code>a<sub>00</sub>, a<sub>01</sub>, ... a<sub>0n</sub>, ... a<sub>10</sub>, ... a<sub>mn</sub></code>
	 */
	public Polynomial_2D(final int m, final int n, final double[] params) {
		assert params.length == (m + 1) * (n + 1);
		this.m = m;
		this.n = n;
		this.params = params;
	}

	public double getParam(final int i, final int j) {
		return params[(n + 1) * i + j];
	}

	/**
	 * @param x
	 *            is the coordinate (x1,x2).
	 * @return the value y(x1,x2).
	 */
	public double val(final double[] x) {
		assert x.length == 2;
		double value = 0.;
		for (int i = 0; i <= m; i++) {
			for (int j = 0; j <= n; j++) {
				value += params[(n + 1) * i + j] * Math.pow(x[0], i) * Math.pow(x[1], j);
			}
		}
		return value;
	}

	/**
	 * @param x
	 *            is a list of coordinates ( x1<SUB>i</SUB> ,x2<SUB>i</SUB> ).
	 * @return a list of the values y<SUB>i</SUB> ( x1<SUB>i</SUB>
	 *         ,x2<SUB>i</SUB> ).
	 */
	public double[] val(final double[][] x) {
		final double[] values = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			values[i] = val(x[i]);
		}
		return values;
	}

	/**
	 * @param x
	 *            is the coordinate (x1,x2).
	 * @param paramIndex
	 *            is the index of the parameter.
	 * @return the element of the gradient vector with the given index.
	 */
	public double grad(final double[] x, final int paramIndex) {
		assert x.length == 2;
		assert paramIndex < (m + 1) * (n + 1);
		for (int i = 0; i <= m; i++) {
			for (int j = 0; j <= n; j++) {
				if (paramIndex == (n + 1) * i + j)
					return Math.pow(x[0], i) * Math.pow(x[1], j);
			}
		}
		return 0.;
	}

	/**
	 * @param x
	 *            is the coordinate (x1,x2).
	 * @return the gradient vector as an array.
	 */
	public double[] grad(final double[] x) {
		assert x.length == 2;
		final double[] grads = new double[params.length];
		for (int i = 0; i < grads.length; i++) {
			grads[i] = grad(x, i);
		}
		return grads;
	}

	private void updateParams(final double[] paramsNew) {
		assert params.length == paramsNew.length;
		params = Arrays.copyOf(paramsNew, paramsNew.length);
	}

	@Override
	public double getY(final double[] x, final double[] a) {
		updateParams(a);
		return val(x);
	}

	@Override
	public double getPartialDerivate(final double[] x, final double[] a, final int parameterIndex) {
		updateParams(a);
		return grad(x, parameterIndex);
	}
}
