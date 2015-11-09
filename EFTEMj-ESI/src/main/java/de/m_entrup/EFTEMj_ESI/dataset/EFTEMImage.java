
package de.m_entrup.EFTEMj_ESI.dataset;

import ij.IJ;

/**
 * An {@link EFTEMImage} is part of an EFTEM stack. The EFTEM stack is realised
 * by an array of {@link EFTEMImage}s. The {@link EFTEMImage} consists of an
 * array of pixels, the energy loss and the label of the image. You can compare
 * {@link EFTEMImage}s to each other. The energy loss is used for the
 * comparison.
 */
public class EFTEMImage implements Comparable<EFTEMImage> {

	/**
	 * The energy loss of the {@link EFTEMImage}.
	 */
	protected float eLoss;
	/**
	 * The label of the {@link EFTEMImage}.
	 */
	protected String label;
	/**
	 * The image saved as an 1D-array
	 */
	protected float[] pixels;
	/**
	 * The width of the image. The height can be calculated from<br>
	 * <code>pixels.length()/width</code>.
	 */
	protected int width;

	/**
	 * Creates a new {@link EFTEMImage}.
	 *
	 * @param eLoss The energy loss of the image.
	 * @param label The label of the image. Use the short label.
	 * @param pixels A float array representing the pixels of the image.
	 * @param width The width of the image.
	 */
	public EFTEMImage(final float eLoss, final String label, final float[] pixels,
		final int width)
	{
		super();
		this.eLoss = eLoss;
		this.label = label;
		this.pixels = pixels;
		this.width = width;
	}

	@Override
	public int compareTo(final EFTEMImage o) {
		if (this.eLoss > o.eLoss) return 1;
		if (this.eLoss < o.eLoss) return -1;
		return 0;
	}

	/**
	 * @return The energy loss of the {@link EFTEMImage}.
	 */
	public float getELoss() {
		return eLoss;
	}

	/**
	 * @return The height of the {@link EFTEMImage}.
	 */
	public int getHeight() {
		return pixels.length / width;
	}

	/**
	 * @return The label of the {@link EFTEMImage}. This should be the same a the
	 *         shortLabel of the initial {@link IJ} image.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return The values of the {@link EFTEMImage} as a float array.
	 */
	public float[] getPixels() {
		return pixels;
	}

	/**
	 * @return The width of the {@link EFTEMImage}.
	 */
	public int getWidth() {
		return width;
	}

}
