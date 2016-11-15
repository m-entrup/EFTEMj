package de.m_entrup.EFTEMj_SR_EELS.characterisation;

public class SR_EELS_CharacterisationSubResults {

	/**
	 * This is the coordinate along the lateral axis.
	 */
	double x;
	/**
	 * This is the error of the coordinate along the lateral axis.
	 */
	double xError;
	/**
	 * This is the coordinate along the energy dispersive axis.
	 */
	double y;
	/**
	 * This is the error of the coordinate along the energy dispersive axis.
	 */
	double yError;
	/**
	 * This is the the left one of the detected borders (The top one of the
	 * saved data sets).
	 */
	double left;
	double leftError;
	/**
	 * This is the the right one of the detected borders (The lower one of the
	 * saved data sets).
	 */
	double right;
	double rightError;
	/**
	 * this is the width of the spectrum at the given energy loss (x
	 * coordinate).
	 */
	double width;
	double widthError;
	/**
	 * Only when not performing thresholding this value is used. The value of a
	 * pixel is considered as signal and not noise, when the limit to exceeded.
	 */
	double limit;
}
