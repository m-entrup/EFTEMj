package de.m_entrup.EFTEMj_SR_EELS.characterisation;

import java.util.HashMap;

import ij.ImagePlus;

public class SR_EELS_CharacterisationResults {
	public static final int CSV = 1;
	public static final int JPEG = 2;
	public static final int TIFF = 4;
	public static final int PLOTS = 8;

	public ImagePlus plots;
	SR_EELS_CharacterisationSettings settings;
	long timeStart;
	long timeStop;
	HashMap<String, SR_EELS_CharacterisationResult> subResults;

	public SR_EELS_CharacterisationResults() {
		this.subResults = new HashMap<>();
	}

	public SR_EELS_CharacterisationResults(final SR_EELS_CharacterisationSettings settings) {
		this();
		this.settings = settings;
	}
}
