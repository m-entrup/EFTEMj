
package de.m_entrup.EFTEMj_SR_EELS.spectrum;

import java.awt.Window;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;
import de.m_entrup.EFTEMj_lib.lma.PowerLawFunction;
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

    private Plot newPlot;
    private PlotWindow newPlotWin;
    private Plot oldPlot;
    private PointList pl;
    private GenericDialog gd;
    private int startX;
    private int stopX;

    @Override
    public int setup(final String arg0, final ImagePlus arg1) {
	if (arg1 != null) {
	    final Window win = arg1.getWindow();
	    if (win instanceof PlotWindow && win.isVisible()) {
		PlotWindow plotWin = (PlotWindow) win;
		oldPlot = plotWin.getPlot();
		pl = new PointList(oldPlot.getXValues(), oldPlot.getYValues());
		return DOES_ALL;
	    }
	}
	return DONE;
    }

    @Override
    public void run(final ImageProcessor arg0) {
	double temp = gd.getNextNumber();
	startX = (int) ((temp >= pl.getMin()) ? temp : pl.getMin());
	temp = gd.getNextNumber();
	stopX = (int) ((temp <= pl.getMax()) ? temp : pl.getMax());
	if (startX >= stopX)
	    return;
	pl.filterXValues();
	pl.filterYValues();
	newPlotCopy();
	pl.performFit();
	newPlot.setColor("red");
	newPlot.addPoints(pl.getXValues(), pl.getFittedY(), Plot.CIRCLE);
	newPlot.setColor("green");
	newPlot.addPoints(pl.getSignalX(), pl.getSignalY(), Plot.LINE);
	newPlot.setColor("blue");
	newPlot.addPoints(pl.getFilteredXValues(), pl.getFilteredYValues(), Plot.BOX);
	newPlot.setColor("black"); // to get a black line for the main curve
	newPlot.setLimitsToFit(false);
	if (newPlotWin != null) {
	    newPlotWin.drawPlot(newPlot);
	} else {
	    newPlotWin = newPlot.show();
	}

    }

    @Override
    public void setNPasses(final int arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public int showDialog(final ImagePlus arg0, final String arg1, final PlugInFilterRunner arg2) {
	gd = new GenericDialog("EELS background fit");
	gd.addSlider("Lower limit", pl.getMin(), pl.getMax(), pl.getMin());
	gd.addSlider("Upper limit", pl.getMin(), pl.getMax(), pl.getMax());
	gd.addPreviewCheckbox(arg2);
	gd.showDialog();
	if (gd.wasCanceled())
	    return DONE;
	return DOES_ALL;
    }

    public static void main(final String[] args) throws URISyntaxException {
	new ImageJ();
	final URI uri = EELS_SpectrumFitPlugin.class.getResource("/examples/EELS_C_K-edge.msa").toURI();
	final Plot plot = new EELS_SpectrumFromMsaPlugin().getPlot(new File(uri));
	plot.show();
	IJ.runPlugIn(EELS_SpectrumFitPlugin.class.getName(), "");
    }

    private void newPlotCopy() {
	newPlot = new Plot("Fit of " + oldPlot.getTitle(), "", "", oldPlot.getXValues(), oldPlot.getYValues());
	newPlot.useTemplate(oldPlot, Plot.COPY_LABELS);
    }

    private class PointList {

	private final ArrayList<Double> valuesX;
	private final ArrayList<Double> valuesY;
	private ArrayList<Double> filteredX;
	private ArrayList<Double> filteredY;
	private ArrayList<Double> fittedY;
	private ArrayList<Double> signalX;
	private ArrayList<Double> signalY;
	private final float min;
	private final float max;

	public PointList(final float[] xVals, final float[] yVals) {
	    valuesX = new ArrayList<Double>();
	    valuesY = new ArrayList<Double>();
	    filteredX = new ArrayList<Double>();
	    filteredY = new ArrayList<Double>();
	    for (int i = 0; i < xVals.length; i++) {
		valuesX.add((double) xVals[i]);
		valuesY.add((double) yVals[i]);
		filteredX.add((double) xVals[i]);
		filteredY.add((double) yVals[i]);
	    }
	    final float[] sorted = Arrays.copyOf(xVals, xVals.length);
	    Arrays.sort(sorted);
	    this.min = sorted[0];
	    this.max = sorted[sorted.length - 1];
	}

	public ArrayList<Double> getSignalX() {
	    return signalX;
	}

	public ArrayList<Double> getSignalY() {
	    return signalY;
	}

	public float getMin() {
	    return min;
	}

	public float getMax() {
	    return max;
	}

	public ArrayList<Double> getXValues() {
	    return valuesX;
	}

	public ArrayList<Double> getFilteredXValues() {
	    return filteredX;
	}

	public void filterXValues() {
	    filteredX = new ArrayList<Double>();
	    final Iterator<Double> it = valuesX.iterator();
	    while (it.hasNext()) {
		final double val = it.next();
		if (val >= startX & val <= stopX) {
		    filteredX.add(val);
		}
	    }
	}

	public ArrayList<Double> getFilteredYValues() {
	    return filteredY;
	}

	public void filterYValues() {
	    filteredY = new ArrayList<Double>();
	    for (int i = 0; i < valuesY.size(); i++) {
		final double val = valuesX.get(i);
		if (val >= startX & val <= stopX) {
		    filteredY.add((double) valuesY.get(i));
		}
	    }

	}

	public void performFit() {
	    ArrayList<WeightedObservedPoint> points = new ArrayList<WeightedObservedPoint>();
	    for (int i = 0; i < filteredX.size(); i++) {
		points.add(new WeightedObservedPoint(Math.sqrt(filteredY.get(i)), filteredX.get(i), filteredY.get(i)));
	    }
	    if (points.size() < 2)
		return;
	    SpectrumFitter fitter = new SpectrumFitter();
	    double[] coeffs = fitter.fit(points);
	    fittedY = new ArrayList<Double>();
	    signalX = new ArrayList<Double>();
	    signalY = new ArrayList<Double>();
	    for (int i = 0; i < valuesX.size(); i++) {
		fittedY.add(coeffs[0] * Math.pow(valuesX.get(i), coeffs[1]));
		signalX.add(valuesX.get(i));
		signalY.add(valuesY.get(i) - fittedY.get(i));
	    }

	}

	public ArrayList<Double> getFittedY() {
	    return fittedY;
	}

	private class SpectrumFitter extends AbstractCurveFitter {

	    @Override
	    protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
		final int len = points.size();
		final double[] target = new double[len];
		final double[] weights = new double[len];
		double[] initialGuess = { 20, -2 };

		int i = 0;
		for (WeightedObservedPoint point : points) {
		    target[i] = point.getY();
		    weights[i] = point.getWeight();
		    i += 1;
		}

		PowerLawFunction func = new PowerLawFunction();
		final AbstractCurveFitter.TheoreticalValuesFunction model = new AbstractCurveFitter.TheoreticalValuesFunction(
			func, points);

		return new LeastSquaresBuilder().maxEvaluations(Integer.MAX_VALUE).maxIterations(Integer.MAX_VALUE)
			.start(initialGuess).target(target).weight(new DiagonalMatrix(weights))
			.model(model.getModelFunction(), model.getModelFunctionJacobian()).build();
	    }
	}
    }

}
