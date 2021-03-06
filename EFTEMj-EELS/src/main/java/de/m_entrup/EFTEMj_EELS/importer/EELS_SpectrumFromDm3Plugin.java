package de.m_entrup.EFTEMj_EELS.importer;

import java.io.File;
import java.util.Arrays;

import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import de.m_entrup.EFTEMj_lib.tools.GatanMetadataExtractor;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.plugin.Profiler;
import sc.fiji.io.DM3_Reader;

public class EELS_SpectrumFromDm3Plugin extends Profiler {

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.plugin.Profiler#getPlot()
	 */
	@Override
	public Plot getPlot() {
		final File file = getFile();
		return getPlot(file);
	}

	public Plot getPlot(final String path) {
		final File file = new File(path);
		if (!file.exists())
			return null;
		if (file.isDirectory())
			return null;
		return getPlot(file);
	}

	public Plot getPlot(final File file) {
		if (file == null) {
			return null;
		}
		final DM3_Reader reader = new DM3_Reader();
		final ImagePlus dm3Spec = reader.load(file.getParent(), file.getName());
		final GatanMetadataExtractor extractor = new GatanMetadataExtractor(dm3Spec);
		final float[] yValues = new float[dm3Spec.getWidth()];
		if (dm3Spec.getHeight() == 1) {
			for (int x = 0; x < dm3Spec.getWidth(); x++) {
				yValues[x] = dm3Spec.getProcessor().getf(x, 0);
			}
		} else {
			Arrays.fill(yValues, 0);
			for (int y = 0; y < dm3Spec.getHeight(); y++) {
				for (int x = 0; x < dm3Spec.getWidth(); x++) {
					yValues[x] += dm3Spec.getProcessor().getf(x, y);
				}
			}
		}
		final float origin = (float) extractor.getXOrigin();
		final float scale = (float) extractor.getXScale();
		final float[] xValues = new float[yValues.length];
		for (int i = 0; i < xValues.length; i++) {
			xValues[i] = scale * (i - origin);
		}
		String x_label = extractor.getXUnit();
		if (x_label == null)
			x_label = "eV";
		final String xLabel = "Energy loss (" + x_label + ")";
		String y_label = extractor.getIntensityUnit();
		if (y_label == null)
			y_label = "a.u.";
		final String yLabel = "Intensity (" + y_label + ")";
		final Plot plot = new Plot("Plot of " + file.getName(), xLabel, yLabel, xValues, yValues);
		dm3Spec.close();
		return plot;
	}

	private File getFile() {
		final OpenDialog od = new OpenDialog("Select a dm3 file...");
		if (od.getFileName() == null)
			return null;
		final File file = new File(od.getPath());
		if (file.getName().toLowerCase().endsWith(".dm3")) {
			return file;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.plugin.Profiler#getSourceImage()
	 */
	@Override
	public ImagePlus getSourceImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.plugin.Profiler#run(java.lang.String)
	 */
	@Override
	public void run(final String arg) {
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
		EFTEMj_Debug.debug(EELS_SpectrumFromDm3Plugin.class);
	}

}
