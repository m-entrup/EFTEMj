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

public class GatanMetadataExtractor {

	private HashMap<String, String> metadata = new HashMap<>();
	private HashMap<String, String> metadataExtracted = new HashMap<>();

	private String patternExposure = ".*Exposure Time \\(s\\)$";
	private String patternExposure2 = ".*Level\\.Exposure \\(s\\)$";
	private String patternMagnification = ".*Indicated Magnification$";
	private String patternMagnificationActual = ".*Actual Magnification$";
	private String patternEnergyloss = ".*Filter energy \\(eV\\)$";
	private String patternDateAndTime = ".*Acquisition Start Time \\(epoch\\)$";
	private String patternDate = ".*DataBar\\.Acquisition Date$";
	private String patternTime = ".*DataBar\\.Acquisition Time$";
	private String patternBrightnessScale = ".*Brightness.Scale$";
	private String patternBrightnessUnit = ".*Brightness.Units$";

	/**
	 * Create an instance of {@link GatanMetadataExtractor} that uses the
	 * metadata of a given {@link ImagePlus} object.
	 * 
	 * @param imp
	 *            to extract the metadata from. this can be a DM3 file or a TIFF
	 *            file that contains the metadate of the DM3 file it originates
	 *            from.
	 */
	public GatanMetadataExtractor(ImagePlus imp) {
		String fileInfo = (String) imp.getProperty("Info");
		BufferedReader bufReader = new BufferedReader(new StringReader(fileInfo));
		String line = null;
		try {
			while ((line = bufReader.readLine()) != null) {
				String[] lineSplitted = line.split("\\s*=\\s*");
				if (lineSplitted.length == 2) {
					metadata.put(lineSplitted[0], lineSplitted[1]);
				}
			}
		} catch (IOException e) {
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
	private boolean findPattern(String pattern) {
		if (metadataExtracted.get(pattern) == null) {
			for (String key : metadata.keySet()) {
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
			DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			try {
				return dateFormat.parse(metadataExtracted.get(patternDate) + " " + metadataExtracted.get(patternTime));
			} catch (ParseException e) {
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
	 * the main method is for testing purposes.
	 */
	public static void main(String[] args) {
		// start ImageJ
		new ImageJ();

		// open the sample stack
		final ImagePlus image = IJ.openImage();
		image.show();

		GatanMetadataExtractor extractor = new GatanMetadataExtractor(image);
		IJ.log("Count of keys: " + extractor.metadata.size());
		IJ.log("Exposure time: " + extractor.getExposure());
		IJ.log("Magnification: " + extractor.getMagnification());
		IJ.log("Actual magnification: " + extractor.getActualMagnification());
		IJ.log("Energy loss: " + extractor.getEnergyloss());
		IJ.log("Date: " + extractor.getDateAndTime().toString());
		IJ.log("Intensity scale: " + extractor.getIntensityScale());
		IJ.log("Intensity unit: " + extractor.getIntensityUnit());
	}

}
