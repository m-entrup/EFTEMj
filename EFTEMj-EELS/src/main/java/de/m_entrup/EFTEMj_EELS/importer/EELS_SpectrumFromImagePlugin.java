package de.m_entrup.EFTEMj_EELS.importer;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.m_entrup.EFTEMj_lib.CameraSetup;
import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import de.m_entrup.EFTEMj_lib.data.EnergyDispersion;
import de.m_entrup.EFTEMj_lib.tools.GatanMetadataExtractor;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import ij.plugin.Profiler;

/**
 * A plugin that is used to create an EEL spectrum from an image.
 *
 * If the source image contains Gatan meta data, this data is used to determine
 * the energy loss interval of the spectrum.
 *
 * If there is no meta data and no calibration is stored, the plugin tries to
 * guess the correct settings by parsing the file name.
 */
public class EELS_SpectrumFromImagePlugin extends Profiler {

	/**
	 * The image to create the spectrum from.
	 */
	private ImagePlus sourceImage;
	/**
	 * The scale of the energy axis.
	 */
	private double scale = 1;
	/**
	 * The origin of the energy axis.
	 */
	private int origin = 0;
	/**
	 * An instance of {@link GatanMetadataExtractor} to get scale and origin.
	 */
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
		if (x_label == null || x_label.equals(""))
			x_label = "eV";
		final String xLabel = "Energy loss (" + x_label + ")";
		String y_label = extractor.getIntensityUnit();
		if (y_label == null || y_label.equals(""))
			y_label = "a.u.";
		final String yLabel = "Intensity (" + y_label + ")";
		final Plot plot = new Plot("Plot of " + sourceImage.getTitle(), xLabel, yLabel, xValues, yValues);
		return plot;
	}

	/**
	 * Extract the spectral date from the input image.
	 *
	 * @param calcMean
	 *            sets if per energy channel the mean is calculated. Using
	 *            <code>false</code>, the sum is calculated.
	 * @return an array that represents the EEL spectrum.
	 */
	private float[] getEELSFromImage(final boolean calcMean) {
		final float[] eels = new float[sourceImage.getWidth()];
		Arrays.fill(eels, 0);
		int yMin = 0;
		int yMax = sourceImage.getHeight();
		if (sourceImage.getRoi() != null) {
			final Rectangle selection = sourceImage.getRoi().getBounds();
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

	/**
	 * This method tries to create the energy axis by using meta data of the
	 * given image.
	 *
	 * @return an array that represents the energy losses of each channel of the
	 *         spectrum.
	 */
	private float[] getEnergyAxis() {
		final float[] energies = new float[sourceImage.getWidth()];
		if (sourceImage.getCalibration().getXUnit().toLowerCase().equals("ev")) {
			scale = sourceImage.getCalibration().getX(1) - sourceImage.getCalibration().getX(0);
			origin = (int) (-sourceImage.getCalibration().getX(0) / scale);
		} else if (extractor.getXUnit().toLowerCase().equals("ev")) {
			scale = extractor.getXScale();
			origin = (int) extractor.getXOrigin();
		}
		EFTEMj_Debug.log("scale: " + scale);
		EFTEMj_Debug.log("origin: " + origin);
		if (scale == 1) {
			final Pattern patternSpecMag = Pattern.compile(".*SM(\\d{2,3}).*");
			final Matcher matchSpecMag = patternSpecMag.matcher(sourceImage.getTitle());
			final Matcher matchSpecMagPath = patternSpecMag.matcher(sourceImage.getOriginalFileInfo().directory);
			boolean foundSpecMag = false;
			String specMag = "";
			if (matchSpecMag.find()) {
				specMag = matchSpecMag.group(1);
				EFTEMj_Debug.log(String.format("SpecMag from image title: %s", specMag));
				foundSpecMag = true;
			} else if (matchSpecMagPath.find()) {
				specMag = matchSpecMagPath.group(1);
				EFTEMj_Debug.log(String.format("SpecMag from file path: %s", specMag));
				foundSpecMag = true;
			}
			if (foundSpecMag) {
				final EnergyDispersion dispersion = new EnergyDispersion();
				final Double newScale = dispersion.dispersionStorage.get(specMag);
				if (newScale != null) {
					// EEL spectrum images are rotated by 90°.
					final int binning = CameraSetup.getFullHeight() / sourceImage.getWidth();
					scale = binning * newScale;
				}
			}
		}
		if (origin == 0) {
			final Pattern patternEnergy = Pattern.compile("[^\\d]*(\\d+(?:[,\\.]\\d+)?)eV.*");
			final Matcher matchEnergy = patternEnergy.matcher(sourceImage.getTitle());
			final Matcher matchEnergyPath = patternEnergy.matcher(sourceImage.getFileInfo().directory);
			boolean foundEnergy = false;
			float energy = 0;
			if (matchEnergy.find()) {
				energy = Float.parseFloat(matchEnergy.group(1));
				EFTEMj_Debug.log(String.format("Energy loss from image title: %geV", energy));
				foundEnergy = true;
			} else if (matchEnergyPath.find()) {
				energy = Float.parseFloat(matchEnergyPath.group(1));
				EFTEMj_Debug.log(String.format("Energy loss from file path: %geV", energy));
				foundEnergy = true;
			}
			if (foundEnergy) {
				final int offsetCenter = sourceImage.getWidth() / 2;
				final double offsetValue = energy / scale - offsetCenter;
				origin = (int) -offsetValue;
			}
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
		EFTEMj_Debug.log(String.format("Running %s with %s", this.getClass().getName(), sourceImage.getShortTitle()));
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
		final ImagePlus imp = IJ.openImage();
		imp.show();
		EFTEMj_Debug.debug(EELS_SpectrumFromImagePlugin.class);
	}
}
