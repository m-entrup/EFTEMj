
package de.m_entrup.EFTEMj_ESI.dataset;

import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.tools.ImagePlusTool;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.RGBStackMerge;
import ij.process.FloatProcessor;

/**
 * This class is used to create {@link ImagePlus} objects from the results of
 * the elemental map calculation. Composite images are used that display the
 * data as a grey scale image and the error map as a red overlay.
 */
public class MapResultToImagePlus {

	/**
	 * A shortcut to access the instance of {@link DatasetAPI}.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * A string containing all pre-edge energy losses separated by "-".
	 */
	private String strOfPreELoss;

	protected MapResultToImagePlus() {
		super();
		strOfPreELoss = "[";
		for (int i = 0; i < datasetAPI.getEdgeIndex(); i++) {
			strOfPreELoss += datasetAPI.getEFTEMImage(i).getELoss() + "eV-";
		}
		strOfPreELoss = strOfPreELoss.substring(0, strOfPreELoss.length() - 1) +
			"]";
	}

	/**
	 * @return A composite image containing the error map and the map of the
	 *         parameter <code>a</code>.
	 */
	protected ImagePlus getA() {
		ImagePlus imp;
		final int width = datasetAPI.getWidth();
		final int height = datasetAPI.getHeight();
		final FloatProcessor fp = new FloatProcessor(width, height, datasetAPI
			.getAMap(), null);
		fp.resetMinAndMax();
		imp = ImagePlusTool.createImagePlus("a-Map" + strOfPreELoss + " " +
			datasetAPI.getImagePlusShortTitle(), fp, true);
		final ImagePlus[] images = new ImagePlus[7];
		images[0] = getErrorMap();
		images[3] = imp;
		final RGBStackMerge rgbMerge = new RGBStackMerge();
		final ImagePlus composite = rgbMerge.mergeHyperstacks(images, true);
		composite.setTitle(images[3].getTitle());
		composite.setSliceWithoutUpdate(2);
		return composite;
	}

	protected ImagePlus getBG() {
		ImagePlus imp;
		final EFTEMImage[] array_Background = datasetAPI.getBackgroundImages();
		final int width = array_Background[0].getWidth();
		final int height = array_Background[0].getHeight();
		final int length = array_Background.length;
		final ImageStack stack = new ImageStack(width, height);
		for (int i = 0; i < length; i++) {
			final FloatProcessor fp = new FloatProcessor(width, height,
				array_Background[i].getPixels(), null);
			fp.resetMinAndMax();
			stack.addSlice("BG[" + array_Background[i].getELoss() + "eV] " +
				datasetAPI.getImagePlusShortTitle(), fp);
		}

		imp = ImagePlusTool.createImagePlus("BG-Maps " + datasetAPI
			.getImagePlusShortTitle(), stack, true);
		return imp;
	}

	/**
	 * @return A composite image containing the error map and the map of the
	 *         <code>chi�</code>.
	 */
	protected ImagePlus getChi2() {
		ImagePlus imp;
		final int width = datasetAPI.getWidth();
		final int height = datasetAPI.getHeight();
		final FloatProcessor fp = new FloatProcessor(width, height, datasetAPI
			.getChi2(), null);
		fp.resetMinAndMax();
		imp = ImagePlusTool.createImagePlus("Chi^2-Map" + strOfPreELoss + " " +
			datasetAPI.getImagePlusShortTitle(), fp, true);
		final ImagePlus[] images = new ImagePlus[7];
		images[0] = getErrorMap();
		images[3] = imp;
		final RGBStackMerge rgbMerge = new RGBStackMerge();
		final ImagePlus composite = rgbMerge.mergeHyperstacks(images, true);
		composite.setTitle(images[3].getTitle());
		composite.setSliceWithoutUpdate(2);
		return composite;
	}

	/**
	 * @return A composite image containing the error map and the map of the
	 *         coefficient of determination.
	 */
	protected ImagePlus getCoeffOfDet() {
		ImagePlus imp;
		final int width = datasetAPI.getWidth();
		final int height = datasetAPI.getHeight();
		final FloatProcessor fp = new FloatProcessor(width, height, datasetAPI
			.getCoeffOFDet(), null);
		fp.resetMinAndMax();
		imp = ImagePlusTool.createImagePlus("COD-Map" + strOfPreELoss + " " +
			datasetAPI.getImagePlusShortTitle(), fp, true);
		final ImagePlus[] images = new ImagePlus[7];
		images[0] = getErrorMap();
		images[3] = imp;
		final RGBStackMerge rgbMerge = new RGBStackMerge();
		final ImagePlus composite = rgbMerge.mergeHyperstacks(images, true);
		composite.setTitle(images[3].getTitle());
		composite.setSliceWithoutUpdate(2);
		return composite;
	}

	/**
	 * @return A image showing the error map.
	 */
	protected ImagePlus getErrorMap() {
		ImagePlus imp;
		final int width = datasetAPI.getWidth();
		final int height = datasetAPI.getHeight();
		final FloatProcessor fp = new FloatProcessor(width, height, datasetAPI
			.getErrorMap(), null);
		fp.resetMinAndMax();
		imp = ImagePlusTool.createImagePlus("Error-Map" + strOfPreELoss + " " +
			datasetAPI.getImagePlusShortTitle(), fp, false);
		imp.setDisplayRange(0, 255);
		return imp;
	}

	protected ImagePlus getMap() {
		ImagePlus imp;
		final EFTEMImage[] array_Map = datasetAPI.getMap();
		final int width = array_Map[0].getWidth();
		final int height = array_Map[0].getHeight();
		final int length = array_Map.length;
		final ImageStack stack = new ImageStack(width, height);
		for (int i = 0; i < length; i++) {
			final FloatProcessor fp = new FloatProcessor(width, height, array_Map[i]
				.getPixels(), null);
			fp.resetMinAndMax();
			stack.addSlice("Map[" + array_Map[i].getELoss() + "eV] " + datasetAPI
				.getImagePlusShortTitle(), fp);
		}
		if (length == 1) {
			imp = ImagePlusTool.createImagePlus("Map[" + array_Map[0].getELoss() +
				"eV] " + datasetAPI.getImagePlusShortTitle(), stack, true);
		}
		else {
			imp = ImagePlusTool.createImagePlus("Elemental-Maps " + datasetAPI
				.getImagePlusShortTitle(), stack, true);
		}
		if (length < 2) {
			final ImagePlus[] images = new ImagePlus[7];
			images[0] = getErrorMap();
			images[3] = imp;
			final RGBStackMerge rgbMerge = new RGBStackMerge();
			final ImagePlus composite = rgbMerge.mergeHyperstacks(images, true);
			composite.setTitle(images[3].getTitle());
			composite.setSliceWithoutUpdate(2);
			return composite;
		}
		return imp;
	}

	/**
	 * @return A composite image containing the error map and the map of the
	 *         parameter <code>r</code>.
	 */
	protected ImagePlus getR() {
		ImagePlus imp;
		final int width = datasetAPI.getWidth();
		final int height = datasetAPI.getHeight();
		final FloatProcessor fp = new FloatProcessor(width, height, datasetAPI
			.getRMap(), null);
		fp.resetMinAndMax();
		imp = ImagePlusTool.createImagePlus("r-Map" + strOfPreELoss + " " +
			datasetAPI.getImagePlusShortTitle(), fp, true);
		final ImagePlus[] images = new ImagePlus[7];
		images[0] = getErrorMap();
		images[3] = imp;
		final RGBStackMerge rgbMerge = new RGBStackMerge();
		final ImagePlus composite = rgbMerge.mergeHyperstacks(images, true);
		composite.setTitle(images[3].getTitle());
		composite.setSliceWithoutUpdate(2);
		return composite;
	}

	protected ImagePlus getRelBG() {
		ImagePlus imp;
		final EFTEMImage[] array_RelBackground = datasetAPI
			.getRelBackgroundImages();
		final int width = array_RelBackground[0].getWidth();
		final int height = array_RelBackground[0].getHeight();
		final int length = array_RelBackground.length;
		final ImageStack stack = new ImageStack(width, height);
		for (int i = 0; i < length; i++) {
			final FloatProcessor fp = new FloatProcessor(width, height,
				array_RelBackground[i].getPixels(), null);
			fp.resetMinAndMax();
			stack.addSlice("relative BG[" + array_RelBackground[i].getELoss() +
				"eV] " + datasetAPI.getImagePlusShortTitle(), fp);
		}

		imp = ImagePlusTool.createImagePlus("relative BG-Maps " + datasetAPI
			.getImagePlusShortTitle(), stack, true);
		if (length < 2) {
			final ImagePlus[] images = new ImagePlus[7];
			images[0] = getErrorMap();
			images[3] = imp;
			final RGBStackMerge rgbMerge = new RGBStackMerge();
			final ImagePlus composite = rgbMerge.mergeHyperstacks(images, true);
			composite.setTitle(images[3].getTitle());
			composite.setSliceWithoutUpdate(2);
			return composite;
		}
		return imp;
	}

	protected ImagePlus getSigma2() {
		ImagePlus imp;
		final EFTEMImage[] array_Sigma2 = datasetAPI.getSigma2();
		final int width = array_Sigma2[0].getWidth();
		final int height = array_Sigma2[0].getHeight();
		final int length = array_Sigma2.length;
		final ImageStack stack = new ImageStack(width, height);
		for (int i = 0; i < length; i++) {
			final FloatProcessor fp = new FloatProcessor(width, height,
				array_Sigma2[i].getPixels(), null);
			fp.resetMinAndMax();
			stack.addSlice("Sigma^2[" + array_Sigma2[i].getELoss() + "eV] " +
				datasetAPI.getImagePlusShortTitle(), fp);
		}
		if (length == 1) {
			imp = ImagePlusTool.createImagePlus("Sigma^2[" + array_Sigma2[0]
				.getELoss() + "eV] " + datasetAPI.getImagePlusShortTitle(), stack,
				true);
		}
		else {
			imp = ImagePlusTool.createImagePlus("Sigma^2-Maps " + datasetAPI
				.getImagePlusShortTitle(), stack, true);
		}
		if (length < 2) {
			final ImagePlus[] images = new ImagePlus[7];
			images[0] = getErrorMap();
			images[3] = imp;
			final RGBStackMerge rgbMerge = new RGBStackMerge();
			final ImagePlus composite = rgbMerge.mergeHyperstacks(images, true);
			composite.setTitle(images[3].getTitle());
			composite.setSliceWithoutUpdate(2);
			return composite;
		}
		return imp;
	}

	protected ImagePlus getSNR(final float dqe) {
		ImagePlus imp;
		final EFTEMImage[] array_SNR = datasetAPI.getSNR();
		final int width = array_SNR[0].getWidth();
		final int height = array_SNR[0].getHeight();
		final int length = array_SNR.length;
		final float[][] array_SNR_Out = new float[array_SNR.length][];
		if (dqe != 1) {
			for (int i = 0; i < array_SNR.length; i++) {
				array_SNR_Out[i] = new float[array_SNR[i].pixels.length];
				for (int j = 0; j < array_SNR[i].pixels.length; j++) {
					array_SNR_Out[i][j] = (float) (Math.sqrt(dqe) *
						array_SNR[i].pixels[j]);
				}
			}
		}
		else {
			for (int i = 0; i < array_SNR.length; i++) {
				array_SNR_Out[i] = array_SNR[i].pixels;
			}
		}
		final ImageStack stack = new ImageStack(width, height);
		for (int i = 0; i < length; i++) {
			final FloatProcessor fp = new FloatProcessor(width, height,
				array_SNR_Out[i], null);
			fp.resetMinAndMax();
			stack.addSlice("SNR(DQE=" + dqe + ")[" + array_SNR[i].getELoss() +
				"eV] " + datasetAPI.getImagePlusShortTitle(), fp);
		}
		if (length == 1) {
			imp = ImagePlusTool.createImagePlus("SNR[" + array_SNR[0].getELoss() +
				"eV] " + datasetAPI.getImagePlusShortTitle(), stack, true);
		}
		else {
			imp = ImagePlusTool.createImagePlus("SNR-Maps " + datasetAPI
				.getImagePlusShortTitle(), stack, true);
		}
		if (length < 2) {
			final ImagePlus[] images = new ImagePlus[7];
			images[0] = getErrorMap();
			images[3] = imp;
			final RGBStackMerge rgbMerge = new RGBStackMerge();
			final ImagePlus composite = rgbMerge.mergeHyperstacks(images, true);
			composite.setTitle(images[3].getTitle());
			composite.setSliceWithoutUpdate(2);
			return composite;
		}
		return imp;
	}
}
