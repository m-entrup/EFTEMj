
package de.m_entrup.EFTEMj_ESI.tools;

import java.text.MessageFormat;

import ij.IJ;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

public class OptimizeAcqTimes implements PlugIn {

	private double q;
	private double eLoss_S;
	private double eLoss_B1;
	private double eLoss_B2;
	private double counts_S;
	private double counts_B1;
	private double counts_B2;
	private double tau_S;
	private double tau_B1;
	private double tau_B2;
	private final String[] output = { "Table", "Text window" };

	@Override
	public void run(final String arg) {
		final GenericDialog gd = new GenericDialog("Optimize Acquisition Times");
		gd.addMessage("Signal Image");
		gd.addNumericField("eLoss:", Prefs.get("OAT.eLossS", 0), 0);
		gd.addNumericField("counts:", Prefs.get("OAT.countS", 0), 0);
		gd.addMessage("BG Image 1");
		gd.addNumericField("eLoss:", Prefs.get("OAT.eLossB1", 0), 0);
		gd.addNumericField("counts:", Prefs.get("OAT.countB1", 0), 0);
		gd.addMessage("BG Image 2");
		gd.addNumericField("eLoss:", Prefs.get("OAT.eLossB2", 0), 0);
		gd.addNumericField("counts:", Prefs.get("OAT.countB2", 0), 0);
		gd.addNumericField("longest acq. time:", Prefs.get("OAT.acq.time", 1), 3);
		gd.addChoice("Output:", output, output[0]);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		eLoss_S = (int) gd.getNextNumber();
		counts_S = (int) gd.getNextNumber();
		eLoss_B1 = (int) gd.getNextNumber();
		counts_B1 = (int) gd.getNextNumber();
		eLoss_B2 = (int) gd.getNextNumber();
		counts_B2 = (int) gd.getNextNumber();
		if (eLoss_B2 < eLoss_B1) {
			double temp = eLoss_B1;
			eLoss_B1 = eLoss_B2;
			eLoss_B2 = temp;
			temp = counts_B1;
			counts_B1 = counts_B2;
			counts_B2 = temp;
		}
		final double acqTime = gd.getNextNumber();
		Prefs.set("OAT.eLossS", eLoss_S);
		Prefs.set("OAT.countS", counts_S);
		Prefs.set("OAT.eLossB1", eLoss_B1);
		Prefs.set("OAT.countB1", counts_B1);
		Prefs.set("OAT.eLossB2", eLoss_B2);
		Prefs.set("OAT.countB2", counts_B2);
		Prefs.set("OAT.acq.time", acqTime);
		calculate();
		final double maxTau = Math.max(tau_S, Math.max(tau_B1, tau_B2));
		if (gd.getNextChoiceIndex() == 0) {
			final ResultsTable table = new ResultsTable();
			table.setPrecision(3);
			table.incrementCounter();
			table.addValue("loss", eLoss_S);
			table.addValue("tau", tau_S);
			table.addValue("time", tau_S / maxTau * acqTime);
			table.incrementCounter();
			table.addValue("loss", eLoss_B1);
			table.addValue("tau", tau_B1);
			table.addValue("time", tau_B1 / maxTau * acqTime);
			table.incrementCounter();
			table.addValue("loss", eLoss_B2);
			table.addValue("tau", tau_B2);
			table.addValue("time", tau_B2 / maxTau * acqTime);
			table.show("Optimized Acquisition Times");
		}
		else {
			final MessageFormat form = new MessageFormat(
				"loss={0}eV; tau={1,number,#.###}; t={2,number,#.###}s");
			final Object[] para_S = { eLoss_S, tau_S, tau_S / maxTau * acqTime };
			final Object[] para_B1 = { eLoss_B1, tau_B1, tau_B1 / maxTau * acqTime };
			final Object[] para_B2 = { eLoss_B2, tau_B2, tau_B2 / maxTau * acqTime };
			IJ.log(form.format(para_S));
			IJ.log(form.format(para_B1));
			IJ.log(form.format(para_B2));
		}
	}

	private void calculate() {
		final double denum = Math.log(eLoss_S) - 0.5 * (Math.log(eLoss_B2) + Math
			.log(eLoss_B1));
		double num = Math.log(eLoss_B2) - Math.log(eLoss_B1);
		q = denum / num;
		final double a = getBG() / counts_S;
		num = (1 - 4 * Math.pow(q, 2) * a);
		tau_S = (float) (3 * (1 - 2 * q * Math.sqrt(a)) / num);
		tau_B1 = (float) (3 * (q - 0.5) * (Math.sqrt(a) - 2 * q * a) / num);
		tau_B2 = (float) (3 * (q + 0.5) * (Math.sqrt(a) - 2 * q * a) / num);
	}

	private double getBG() {
		final double counts_B = Math.sqrt(counts_B1 * counts_B2) * Math.pow(
			counts_B2 / counts_B1, q);
		return counts_B;
	}
}
