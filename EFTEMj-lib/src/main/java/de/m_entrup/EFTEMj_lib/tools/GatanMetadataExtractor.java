package de.m_entrup.EFTEMj_lib.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * A class that is used to extract information from the metadata of Gatan image
 * files.
 */
public class GatanMetadataExtractor {

	private final HashMap<String, String> metadata = new HashMap<>();
	private final HashMap<String, String> metadataExtracted = new HashMap<>();

	private final String patternExposure = ".*Exposure Time \\(s\\)$";
	private final String patternExposure2 = ".*Level\\.Exposure \\(s\\)$";
	private final String patternMagnification = ".*Indicated Magnification$";
	private final String patternMagnificationActual = ".*Actual Magnification$";
	private final String patternEnergyloss = ".*Filter energy \\(eV\\)$";
	private final String patternDateAndTime = ".*Acquisition Start Time \\(epoch\\)$";
	private final String patternDate = ".*DataBar\\.Acquisition Date$";
	private final String patternTime = ".*DataBar\\.Acquisition Time$";
	private final String patternBrightnessScale = ".*Brightness.Scale$";
	private final String patternBrightnessUnit = ".*Brightness.Units$";
	private final String patternName = ".*ImageList\\.1\\.Name$";
	private final String patternXOrigin = ".*1\\.ImageData\\.Calibrations\\.Dimension\\.0\\.Origin$";
	private final String patternXScale = ".*1\\.ImageData\\.Calibrations\\.Dimension\\.0\\.Scale$";
	private final String patternXUnit = ".*1\\.ImageData\\.Calibrations\\.Dimension\\.0\\.Units$";
	private final String patternYOrigin = ".*1\\.ImageData\\.Calibrations\\.Dimension\\.1\\.Origin$";
	private final String patternYScale = ".*1\\.ImageData\\.Calibrations\\.Dimension\\.1\\.Scale$";
	private final String patternYUnit = ".*1\\.ImageData\\.Calibrations\\.Dimension\\.1\\.Units$";

	/**
	 * Create an instance of {@link GatanMetadataExtractor} that uses the
	 * metadata of a given {@link ImagePlus} object.
	 *
	 * @param imp
	 *            to extract the metadata from. this can be a DM3 file or a TIFF
	 *            file that contains the metadate of the DM3 file it originates
	 *            from.
	 */
	public GatanMetadataExtractor(final ImagePlus imp) {
		final String fileInfo = (String) imp.getProperty("Info");
		if (fileInfo == null) {
			return;
		}
		final BufferedReader bufReader = new BufferedReader(new StringReader(fileInfo));
		String line = null;
		try {
			while ((line = bufReader.readLine()) != null) {
				final String[] lineSplitted = line.split("\\s*=\\s*");
				if (lineSplitted.length == 2) {
					metadata.put(lineSplitted[0], lineSplitted[1]);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Search the extracted metadata for the given key. Only the first match is
	 * considered. Results are cached in a {@link HashMap} for faster access.
	 *
	 * @param pattern
	 *            to search for.
	 * @return <code>true</code> if the given pattern was found.
	 */
	private boolean findPattern(final String pattern) {
		if (metadataExtracted.get(pattern) == null) {
			for (final String key : metadata.keySet()) {
				if (key.matches(pattern)) {
					metadataExtracted.put(pattern, metadata.get(key));
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * @return the exposure time in seconds extracted from the metadata.
	 */
	public double getExposure() {
		if (findPattern(patternExposure)) {
			return Double.parseDouble(metadataExtracted.get(patternExposure));
		}
		if (findPattern(patternExposure2)) {
			return Double.parseDouble(metadataExtracted.get(patternExposure2));
		}
		// Return NaN if the exposure is not listed at the metadata.
		return Double.NaN;
	}

	/**
	 * @return the magnification that was selected using the microscope
	 *         controls. This value differs from the actual magnification of the
	 *         image.
	 */
	public double getMagnification() {
		if (findPattern(patternMagnification)) {
			return Double.parseDouble(metadataExtracted.get(patternMagnification));
		}
		return Double.NaN;
	}

	/**
	 * @return the magnification of the recorded image. This value is calculated
	 *         from the pixel size of the camera and the calibration.
	 */
	public double getActualMagnification() {
		if (findPattern(patternMagnificationActual)) {
			return Double.parseDouble(metadataExtracted.get(patternMagnificationActual));
		}
		return Double.NaN;
	}

	/**
	 * @return the energy loss of the given image. Many images will not cntain
	 *         the energy loss in their metadata.
	 */
	public double getEnergyloss() {
		if (findPattern(patternEnergyloss)) {
			return Double.parseDouble(metadataExtracted.get(patternEnergyloss));
		}
		return Double.NaN;
	}

	/**
	 * @return the date and time the image was acquired. This method will return
	 *         <code>null</code>, if no acquisition time can be found at the
	 *         metadata.
	 */
	public Date getDateAndTime() {
		if (findPattern(patternDateAndTime)) {
			return new Date((long) Double.parseDouble(metadataExtracted.get(patternDateAndTime)));
		}
		if (findPattern(patternDate) & findPattern(patternTime)) {
			final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			try {
				return dateFormat.parse(metadataExtracted.get(patternDate) + " " + metadataExtracted.get(patternTime));
			} catch (final ParseException e) {
				IJ.log(e.getMessage());
				return null;
			}
		}
		return null;
	}

	/**
	 * @return the scale factor for calibrating the intensity of each pixel.
	 */
	public double getIntensityScale() {
		if (findPattern(patternBrightnessScale)) {
			return Double.parseDouble(metadataExtracted.get(patternBrightnessScale));
		}
		return Double.NaN;
	}

	/**
	 * @return the unit of the intensity. If the intensity is not calibrated, an
	 *         empty {@link String} is returned.
	 */
	public String getIntensityUnit() {
		if (findPattern(patternBrightnessUnit)) {
			return metadataExtracted.get(patternBrightnessUnit);
		}
		return "";
	}

	/**
	 * @return the name given to the DM3 file when saving for the first time.
	 */
	public String getName() {
		if (findPattern(patternName)) {
			return metadataExtracted.get(patternName);
		}
		return "";
	}

	/**
	 * @return the origin of the x axis in pixel.
	 */
	public double getXOrigin() {
		if (findPattern(patternXOrigin)) {
			return Double.parseDouble(metadataExtracted.get(patternXOrigin));
		}
		return Double.NaN;
	}

	/**
	 * @return the scale of the x axis (width of each pixel).
	 */
	public double getXScale() {
		if (findPattern(patternXScale)) {
			return Double.parseDouble(metadataExtracted.get(patternXScale));
		}
		return Double.NaN;
	}

	/**
	 * @return the unit of the x axis calibration.
	 */
	public String getXUnit() {
		if (findPattern(patternXUnit)) {
			return metadataExtracted.get(patternXUnit);
		}
		return "";
	}

	/**
	 * @return the origin of the y axis in pixel.
	 */
	public double getYOrigin() {
		if (findPattern(patternYOrigin)) {
			return Double.parseDouble(metadataExtracted.get(patternYOrigin));
		}
		return Double.NaN;
	}

	/**
	 * @return the scale of the y axis (height of each pixel).
	 */
	public double getYScale() {
		if (findPattern(patternYScale)) {
			return Double.parseDouble(metadataExtracted.get(patternYScale));
		}
		return Double.NaN;
	}

	/**
	 * @return the unit of the y axis calibration.
	 */
	public String getYUnit() {
		if (findPattern(patternYUnit)) {
			return metadataExtracted.get(patternYUnit);
		}
		return "";
	}

	/**
	 * the main method is for testing purposes.
	 */
	public static void main(final String[] args) {
		// start ImageJ
		new ImageJ();

		// open the sample stack
		final ImagePlus image = IJ.openImage();
		if (image == null)
			return;
		image.show();

		final GatanMetadataExtractor extractor = new GatanMetadataExtractor(image);
		IJ.log("Count of keys: " + extractor.metadata.size());
		IJ.log("Exposure time: " + extractor.getExposure());
		IJ.log("Magnification: " + extractor.getMagnification());
		IJ.log("Actual magnification: " + extractor.getActualMagnification());
		IJ.log("Energy loss: " + extractor.getEnergyloss());
		IJ.log("Date: " + extractor.getDateAndTime().toString());
		IJ.log("Intensity scale: " + extractor.getIntensityScale());
		IJ.log("Intensity unit: " + extractor.getIntensityUnit());
		IJ.log("Original name: " + extractor.getName());
	}

}
