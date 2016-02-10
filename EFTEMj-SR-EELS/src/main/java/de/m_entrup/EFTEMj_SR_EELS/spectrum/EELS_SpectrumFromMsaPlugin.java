/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2016, Michael Entrup b. Epping
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package de.m_entrup.EFTEMj_SR_EELS.spectrum;

import java.io.File;
import java.io.IOException;

import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import de.m_entrup.EFTEMj_lib.importer.LoadMsa;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.plugin.Profiler;

/**
 * <p>
 * A {@link PlugIn} to load msa files and display them as a {@link Plot}. The
 * full functionality of {@link Plot} is available (except for 'live' mode as
 * data does not change).
 * </p>
 * <p>
 * A msa file contains energy loss and counts of an Electron Energy Loss
 * Spectrum (EELS) as comma separated values. Meta data is included as comments
 * (lines starting with '#').
 * </p>
 *
 * @author Michael Entrup b. Epping
 */
public class EELS_SpectrumFromMsaPlugin extends Profiler {

    private LoadMsa loader;

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
	loader = null;
	try {
	    loader = new LoadMsa(file.getAbsolutePath());
	} catch (final IOException e) {
	    IJ.error(String.format("Error while loading %1s:\n%2s", (loader != null) ? file.getName() : "no file",
		    e.toString()));
	    return null;
	}
	final float[] xValues = loader.getEnergyArray();
	final float[] yValues = loader.getCountArray();
	String s = loader.getXUnit();
	if (s == null)
	    s = "eV";
	final String xLabel = "Energy loss (" + s + ")";
	s = loader.getYUnit();
	if (s == null)
	    s = "a.u.";
	final String yLabel = "Intensity (" + s + ")";
	final Plot plot = new Plot("Plot of " + file.getName(), xLabel, yLabel, xValues, yValues);
	return plot;
    }

    private File getFile() {
	final OpenDialog od = new OpenDialog("Select a msa file...");
	if (od.getFileName() == null)
	    return null;
	final File file = new File(od.getPath());
	if (file.getName().toLowerCase().endsWith(".msa")) {
	    return file;
	}
	final GenericDialog gd = new GenericDialog("Confirm loading...", IJ.getInstance());
	gd.addMessage("You have not selected a msa file.\n" + "Confirm to continue.");
	gd.addCheckbox("Load_non_msa file.", false);
	gd.showDialog();
	if (gd.wasOKed()) {
	    if (gd.getNextBoolean()) {
		return file;
	    }
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
	EFTEMj_Debug.debug(EELS_SpectrumFromMsaPlugin.class);
    }

}
