
package de.m_entrup.EFTEMj_SR_EELS.spectrum;

import java.awt.Window;
import java.util.Arrays;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;

public class EELS_SpectrumFitPlugin implements ExtendedPlugInFilter {

	private Plot plot;
	private boolean secondSignal = false;
	private GenericDialog gd;

	@Override
	public int setup(final String arg0, final ImagePlus arg1) {
		if (arg1 != null) {
			final Window win = arg1.getWindow();
			if (win instanceof PlotWindow && win.isVisible()) {
				final PlotWindow plotWin = (PlotWindow) win;
				plot = plotWin.getPlot();
				return DOES_ALL;
			}
		}
		return DONE;
	}

	@Override
	public void run(final ImageProcessor arg0) {
		secondSignal = gd.getNextBoolean();
		if (secondSignal == true) {
			final float[] oldY = plot.getYValues();
			final float[] newY = Arrays.copyOf(oldY, oldY.length);
			for (int i = 0; i < newY.length; i++) {
				newY[i] *= 1.1;
			}
			plot.addPoints(plot.getXValues(), newY, Plot.CIRCLE);
			plot.updateImage();
		}
	}

	@Override
	public void setNPasses(final int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int showDialog(final ImagePlus arg0, final String arg1,
		final PlugInFilterRunner arg2)
	{
		gd = new GenericDialog("EELS background fit");
		gd.addCheckbox("2nd Signal", false);
		gd.addPreviewCheckbox(arg2);
		gd.showDialog();
		if (gd.wasCanceled()) return DONE;
		return DOES_ALL;
	}

	public static void main(final String[] args) {
		new ImageJ();
		final ImagePlus imp = IJ.openImage(
			"http://imagej.nih.gov/ij/images/boats.gif");
		imp.setRoi(157, 212, 373, 52);
		imp.show();
		IJ.run(imp, "Plot Profile", "");
		imp.close();
		IJ.runPlugIn(EELS_SpectrumFitPlugin.class.getName(), "");
	}

}
