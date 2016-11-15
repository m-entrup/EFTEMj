package de.m_entrup.EFTEMj_lib.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class GatanMetadataExtractor {

	private HashMap<String, String> metadata = new HashMap<>();

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

	public static void main(String[] args) {
		// start ImageJ
		new ImageJ();

		// open the sample stack
		final ImagePlus image = IJ.openImage();
		image.show();

		GatanMetadataExtractor extractor = new GatanMetadataExtractor(image);
		IJ.log("Count of keys: " + extractor.metadata.size());
	}

}
