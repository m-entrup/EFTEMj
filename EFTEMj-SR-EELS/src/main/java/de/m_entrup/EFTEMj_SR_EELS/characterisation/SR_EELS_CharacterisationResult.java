package de.m_entrup.EFTEMj_SR_EELS.characterisation;

import java.util.ArrayList;
import java.util.HashMap;

import ij.ImagePlus;
import ij.measure.CurveFitter;

public class SR_EELS_CharacterisationResult extends ArrayList<SR_EELS_CharacterisationSubResults> {
	/**
	 *
	 */
	private static final long serialVersionUID = 3442370607845131346L;
	public HashMap<String, ImagePlus> imp;
	public CurveFitter leftFit;
	public CurveFitter centreFit;
	public CurveFitter rightFit;
	public int width;
	public int height;
	public float binY;
	public float binX;

	public SR_EELS_CharacterisationResult() {
		super();
		imp = new HashMap<>();
	}
}
