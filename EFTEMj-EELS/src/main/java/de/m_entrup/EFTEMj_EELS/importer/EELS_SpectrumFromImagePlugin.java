package de.m_entrup.EFTEMj_EELS.importer;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import de.m_entrup.EFTEMj_lib.tools.GatanMetadataExtractor;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import ij.plugin.Profiler;

public class EELS_SpectrumFromImagePlugin extends Profiler {

	private ImagePlus sourceImage;
	private GatanMetadataExtractor extractor;

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.plugin.Profiler#getPlot()
	 */
	@Override
	public Plot getPlot() {
		if (sourceImage == null) {
			return null;
		}
		final float[] xValues = getEnergyAxis();
		final float[] yValues = getEELSFromImage(false);
		String x_label = extractor.getXUnit();
		if (x_label == null)
			x_label = "eV";
		final String xLabel = "Energy loss (" + x_label + ")";
		String y_label = null;
		if (y_label == null)
			y_label = "a.u.";
		final String yLabel = "Intensity (" + y_label + ")";
		final Plot plot = new Plot("Plot of " + sourceImage.getTitle(), xLabel, yLabel, xValues, yValues);
		return plot;
	}

	private float[] getEELSFromImage(boolean calcMean) {
		float[] eels = new float[sourceImage.getWidth()];
		Arrays.fill(eels, 0);
		int yMin = 0;
		int yMax = sourceImage.getHeight();
		if (sourceImage.getRoi() != null) {
			Rectangle selection = sourceImage.getRoi().getBounds();
			yMin = selection.y;
			if (yMin < 0)
				yMin = 0;
			yMax = selection.y + selection.height;
			if (yMax > sourceImage.getHeight())
				yMax = sourceImage.getHeight();
		}
		for (int y = yMin; y < yMax; y++) {
			for (int x = 0; x < sourceImage.getWidth(); x++) {
				eels[x] += sourceImage.getProcessor().getf(x, y);
			}
		}
		if (calcMean) {
			for (int i = 0; i < eels.length; i++) {
				eels[i] /= yMax - yMin;
			}
		}
		return eels;
	}

	private float[] getEnergyAxis() {
		float[] energies = new float[sourceImage.getWidth()];
		double origin = extractor.getXOrigin();
		double scale = extractor.getXScale();
		System.out.println(origin);
		System.out.println(scale);
		if (origin == 0 & scale == 1) {
			Pattern patternEnergy = Pattern.compile("[^\\d]*(\\d+(?:[,\\.]\\d+)?)eV.*");
			Matcher matchEnergy = patternEnergy.matcher(sourceImage.getTitle());
			Pattern patternSpecMag = Pattern.compile(".*SM(\\d{2,3}).*");
			Matcher matchSpecMag = patternSpecMag.matcher(sourceImage.getTitle());
			if (matchEnergy.find()) {
				float energy = Float.parseFloat(matchEnergy.group(1));
				IJ.log("" + energy);
			}
			if (matchSpecMag.find()) {
				float specMag = Float.parseFloat(matchSpecMag.group(1));
				IJ.log("" + specMag);
			}
			origin = 0;
			scale = 1;
		}
		for (int i = 0; i < energies.length; i++) {
			energies[i] = (float) (scale * (i - origin));
		}
		return energies;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.plugin.Profiler#getSourceImage()
	 */
	@Override
	public ImagePlus getSourceImage() {
		return sourceImage;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.plugin.Profiler#run(java.lang.String)
	 */
	@Override
	public void run(final String arg) {
		sourceImage = IJ.getImage();
		extractor = new GatanMetadataExtractor(sourceImage);
		final Plot plot = getPlot();
		if (plot == null)
			return;
		plot.setPlotMaker(this);
		plot.show();
	}

	/**
	 * Main method for debugging. For debugging, it is convenient to have a
	 * method that starts ImageJ, loads an image and calls the {@link PlugIn},
	 * e.g. after setting breakpoints.
	 *
	 * @param args
	 *            unused
	 */
	public static void main(final String[] args) {
		ImagePlus imp = IJ.openImage();
		imp.show();
		EFTEMj_Debug.debug(EELS_SpectrumFromImagePlugin.class);
	}
}
