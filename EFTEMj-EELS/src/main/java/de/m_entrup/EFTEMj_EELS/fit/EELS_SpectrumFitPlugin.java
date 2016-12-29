
package de.m_entrup.EFTEMj_EELS.fit;

import java.awt.Window;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

import de.m_entrup.EFTEMj_EELS.importer.EELS_SpectrumFromMsaPlugin;
import de.m_entrup.EFTEMj_lib.lma.EELS_BackgroundFunction;
import de.m_entrup.EFTEMj_lib.lma.FunctionList;
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
	private double prvLowerLimit = Double.POSITIVE_INFINITY;
	private double prvUpperLimit = Double.NEGATIVE_INFINITY;
	private String prvFitFunctionName = "";
	private HashMap<String, EELS_BackgroundFunction> functions;
	private String[] functionsNames;

	@Override
	public int setup(final String arg0, final ImagePlus imp) {
		if (imp != null) {
			final Window win = imp.getWindow();
			if (win instanceof PlotWindow && win.isVisible()) {
				final PlotWindow plotWin = (PlotWindow) win;
				oldPlot = plotWin.getPlot();
				pl = new PointList(oldPlot.getXValues(), oldPlot.getYValues(), oldPlot.getLimits());
				final FunctionList funcList = new FunctionList();
				functions = funcList.getFunctions();
				functionsNames = funcList.getKeys();
				return DOES_ALL;
			}
		}
		return DONE;
	}

	@Override
	public void run(final ImageProcessor arg0) {
		final String fitFunctionName = gd.getNextChoice();
		final double lowerLimit = gd.getNextNumber();
		final double upperLimit = gd.getNextNumber();
		final boolean showNevativeSignal = gd.getNextBoolean();
		if (!fitFunctionName.equals(prvFitFunctionName) | lowerLimit != prvLowerLimit | upperLimit != prvUpperLimit) {
			startX = (int) ((lowerLimit >= pl.getMin()) ? lowerLimit : pl.getMin());
			stopX = (int) ((upperLimit <= pl.getMax()) ? upperLimit : pl.getMax());
			if (startX >= stopX)
				return;
			pl.filterXValues();
			pl.filterYValues();
			newPlotCopy();
			pl.performFit(functions.get(fitFunctionName));
			newPlot.setColor("red");
			newPlot.addPoints(pl.getXValues(), pl.getFittedY(), Plot.CIRCLE);
			newPlot.setColor("green");
			newPlot.addPoints(pl.getSignalX(), pl.getSignalY(), Plot.LINE);
			newPlot.setColor("blue");
			newPlot.addPoints(pl.getFilteredXValues(), pl.getFilteredYValues(), Plot.BOX);
			newPlot.setColor("black"); // to get a black line for the main curve
			prvFitFunctionName = fitFunctionName;
			prvLowerLimit = lowerLimit;
			prvUpperLimit = upperLimit;
			if (newPlotWin != null) {
				final double[] limits = oldPlot.getLimits();
				newPlot.setLimits(pl.getMin(), pl.getMax(), 0, limits[3]);
				newPlotWin.drawPlot(newPlot);
			} else {
				newPlotWin = newPlot.show();
			}
		}
		/**
		 * Without this try-catch-block it can happen, that IJ shows a
		 * NullPointerException at the log and the preview is cancelled.
		 */
		try {
			if (!showNevativeSignal) {
				newPlot.setLimitsToFit(false);
				final double[] limits = newPlot.getLimits();
				newPlot.setLimits(pl.getMin(), pl.getMax(), 0, limits[3]);
			}

			else {
				newPlot.setLimitsToFit(false);
			}
		} catch (final NullPointerException e) {
			return;
		}
		newPlot.updateImage();
	}

	@Override
	public void setNPasses(final int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int showDialog(final ImagePlus arg0, final String arg1, final PlugInFilterRunner arg2) {
		gd = new GenericDialog("EELS background fit");
		gd.addChoice("Fit method", functionsNames, functionsNames[0]);
		gd.addMessage("Background fit intervall");
		gd.addSlider("Lower limit", pl.getMin(), pl.getMax(), pl.getMin());
		gd.addSlider("Upper limit", pl.getMin(), pl.getMax(), pl.getMax());
		gd.addCheckbox("Show_negative Signal", false);
		gd.addPreviewCheckbox(arg2);
		gd.showDialog();
		if (gd.wasCanceled()) {
			if (newPlotWin != null) {
				newPlotWin.close();
			}
			return DONE;
		}
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
		newPlot = new Plot("Fit of " + oldPlot.getTitle(), "", "", pl.getXArray(), pl.getYArray());
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
		private final Double min;
		private final Double max;

		public PointList(final float[] xVals, final float[] yVals, final double[] limits) {
			valuesX = new ArrayList<>();
			valuesY = new ArrayList<>();
			filteredX = new ArrayList<>();
			filteredY = new ArrayList<>();
			fittedY = new ArrayList<>();
			signalX = new ArrayList<>();
			signalY = new ArrayList<>();
			for (int i = 0; i < xVals.length; i++) {
				if (xVals[i] > limits[0] & xVals[i] < limits[1]) {
					valuesX.add((double) xVals[i]);
					valuesY.add((double) yVals[i]);
					filteredX.add((double) xVals[i]);
					filteredY.add((double) yVals[i]);
					fittedY.add(0.);
					signalX.add((double) xVals[i]);
					signalY.add((double) yVals[i]);
				}
			}
			this.min = Collections.min(valuesX);
			this.max = Collections.max(valuesX);
		}

		public ArrayList<Double> getSignalX() {
			return signalX;
		}

		public ArrayList<Double> getSignalY() {
			return signalY;
		}

		public float getMin() {
			return min.floatValue();
		}

		public float getMax() {
			return max.floatValue();
		}

		public ArrayList<Double> getXValues() {
			return valuesX;
		}

		public double[] getXArray() {
			return ArrayUtils.toPrimitive(valuesX.toArray(new Double[valuesX.size()]));
		}

		public double[] getYArray() {
			return ArrayUtils.toPrimitive(valuesY.toArray(new Double[valuesY.size()]));
		}

		public ArrayList<Double> getFilteredXValues() {
			return filteredX;
		}

		public void filterXValues() {
			filteredX = new ArrayList<>();
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
			filteredY = new ArrayList<>();
			for (int i = 0; i < valuesY.size(); i++) {
				final double val = valuesX.get(i);
				if (val >= startX & val <= stopX) {
					filteredY.add((double) valuesY.get(i));
				}
			}

		}

		public void performFit(final EELS_BackgroundFunction function) {
			final ArrayList<WeightedObservedPoint> points = new ArrayList<>();
			for (int i = 0; i < filteredX.size(); i++) {
				points.add(new WeightedObservedPoint(Math.sqrt(filteredY.get(i)), filteredX.get(i), filteredY.get(i)));
			}
			if (points.size() < function.getInitialParameters().length)
				return;
			final SpectrumFitter fitter = new SpectrumFitter(function);
			try {
				final double[] coeffs = fitter.fit(points);
				fittedY = new ArrayList<>();
				signalX = new ArrayList<>();
				signalY = new ArrayList<>();
				for (int i = 0; i < valuesX.size(); i++) {
					fittedY.add(function.value(valuesX.get(i), coeffs));
					signalX.add(valuesX.get(i));
					signalY.add(valuesY.get(i) - fittedY.get(i));
				}
			} catch (final ConvergenceException e) {
				IJ.log(e.getLocalizedMessage());
				return;
			}

		}

		public ArrayList<Double> getFittedY() {
			return fittedY;
		}

		private class SpectrumFitter extends AbstractCurveFitter {

			private final EELS_BackgroundFunction function;

			public SpectrumFitter(final EELS_BackgroundFunction func) {
				super();
				this.function = func;
			}

			@Override
			protected LeastSquaresProblem getProblem(final Collection<WeightedObservedPoint> points) {
				final int len = points.size();
				final double[] target = new double[len];
				final double[] weights = new double[len];
				final double[] initialGuess = function.getInitialParameters();

				int i = 0;
				for (final WeightedObservedPoint point : points) {
					target[i] = point.getY();
					weights[i] = point.getWeight();
					i += 1;
				}
				final AbstractCurveFitter.TheoreticalValuesFunction model = new AbstractCurveFitter.TheoreticalValuesFunction(
						function, points);

				return new LeastSquaresBuilder().maxEvaluations(Integer.MAX_VALUE).maxIterations(Integer.MAX_VALUE)
						.start(initialGuess).target(target).weight(new DiagonalMatrix(weights))
						.model(model.getModelFunction(), model.getModelFunctionJacobian()).build();
			}
		}
	}

}
