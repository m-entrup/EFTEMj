package de.m_entrup.EFTEMj_SR_EELS.characterisation;

import java.io.File;
import java.util.ArrayList;

public class SR_EELS_CharacterisationSettings {
	public int stepSize = 64;
	public double filterRadius = Math.sqrt(stepSize);
	public int energyBorderLow = 2 * stepSize;
	public int energyBorderHigh = 2 * stepSize;
	public float energyPosition = 0.5f;
	public float sigmaWeight = 3f;
	public int polynomialOrder = 3;
	public boolean useThresholding = true;
	public String threshold = "Li";
	public File path = null;
	public ArrayList<String> images;

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer();
		if (useThresholding) {
			str.append(threshold);
		} else {
			str.append("Limit");
		}
		str.append(",");
		str.append(stepSize);
		str.append(",");
		str.append(energyBorderLow);
		str.append(",");
		str.append(energyBorderHigh);
		str.append(",");
		str.append("poly");
		str.append(polynomialOrder);
		str.append("/");
		return str.toString();
	}
}
