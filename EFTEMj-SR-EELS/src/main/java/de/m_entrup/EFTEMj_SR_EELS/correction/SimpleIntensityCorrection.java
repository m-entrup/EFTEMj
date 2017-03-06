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

package de.m_entrup.EFTEMj_SR_EELS.correction;

import ij.process.FloatProcessor;

/**
 * @author Michael Entrup b. Epping
 */
public class SimpleIntensityCorrection extends IntensityCorrector {

	public SimpleIntensityCorrection(final FloatProcessor inputImage, final CoordinateCorrector coordinateCorrector) {
		super(inputImage, coordinateCorrector);
	}

	@Override
	public float getIntensity(final int x1, final int x2) {
		final float[] point_0;
		final float[] point_1;
		try {
			point_0 = coordinateCorrector.transformCoordinate(x1, x2);
			point_1 = coordinateCorrector.transformCoordinate(x1 + 1, x2 + 1);
		} catch (final SR_EELS_Exception exc1) {
			return Float.NaN;
		}
		final float y1_0 = point_0[0];
		final int start1 = (int) Math.floor(y1_0);
		final float y2_0 = point_0[1];
		final int start2 = (int) Math.floor(y2_0);
		final float y1_1 = point_1[0];
		final int stop1 = (int) Math.floor(y1_1);
		final float y2_1 = point_1[1];
		final int stop2 = (int) Math.floor(y2_1);
		try {
			float sum = 0;
			for (int j = start2; j <= stop2; j++) {
				for (int i = start1; i <= stop1; i++) {
					float dx = 1;
					float dy = 1;
					if (i == start1)
						dx -= y1_0 - start1;
					if (i == stop1)
						dx -= (stop1 + 1) - y1_1;
					if (j == start2)
						dy -= y2_0 - start2;
					if (j == stop2)
						dy -= (stop2 + 1) - y2_1;
					sum += dx * dy * input.getf(i, j);
				}
			}
			return sum;
		} catch (final ArrayIndexOutOfBoundsException exc) {
			return Float.NaN;
		}
	}

}
