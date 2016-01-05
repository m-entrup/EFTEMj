/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2016, Michael Entrup b. Epping
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

package de.m_entrup.EFTEMj_lib.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import ij.gui.Plot;

/**
 * <p>
 * This class represents the data of an Electron Energy Loss Spectrum (EELS).
 * </p>
 * <p>
 * The data is stored as a {@link ArrayList} of {@link EnergyLossPoint}s and can
 * be converted to single arrays for x- and y-values. The later is necessary to
 * use the data for creating a {@link Plot}.
 * </p>
 *
 * @author Michael Entrup b. Epping
 */
@SuppressWarnings("serial")
public class EnergyLossData extends ArrayList<Point2D> {

	/**
	 * A method to convert all x- or y-values of the point-list to an array.
	 *
	 * @param start is used to set a lower limit for the energy loss (including
	 *          equality).
	 * @param stop is used to set a upper limit for the energy loss (including
	 *          equality).
	 * @param getYValue is set to <code>true</code> to get y-values instead of
	 *          x-values.
	 * @return an array of energy loss values (x-values) or counts (y-values).
	 */
	private float[] getArray(final float start, final float stop,
		final boolean getYValue)
	{
		final ArrayList<Float> out = new ArrayList<Float>();
		for (final Point2D point : this) {
			if (point.getX() >= start & point.getX() <= stop) {
				if (getYValue) {
					out.add((float) point.getY());
				}
				else {
					out.add((float) point.getX());
				}
			}
		}
		final float[] array = new float[out.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = out.get(i);
		}
		return array;
	}

	/**
	 * @param start is used to set a lower limit for the energy loss (including
	 *          equality).
	 * @param stop is used to set a upper limit for the energy loss (including
	 *          equality).
	 * @return an array of energy loss values limited to the given interval (start
	 *         and stop are included).
	 */
	public float[] getEnergyArray(final float start, final float stop) {
		return getArray(start, stop, false);
	}

	/**
	 * @return an array of energy loss values.
	 */
	public float[] getEnergyArray() {
		return getEnergyArray(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	}

	/**
	 * @param start is used to set a lower limit for the energy loss (including
	 *          equality).
	 * @param stop is used to set a upper limit for the energy loss (including
	 *          equality).
	 * @return an array of counts limited to the given energy loss interval (start
	 *         and stop are included).
	 */
	public float[] getCountArray(final float start, final float stop) {
		return getArray(start, stop, true);
	}

	/**
	 * @return an array of counts.
	 */
	public float[] getCountArray() {
		return getCountArray(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	@Override
	public String toString() {
		String s = "";
		for (final Point2D point : this) {
			s += String.format("%1g\t%2g\n", point.getX(), point.getY());
		}
		return s;
	}
}
