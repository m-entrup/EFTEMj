/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2015, Michael Entrup b. Epping
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.m_entrup.EFTEMj_ESI.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import de.m_entrup.EFTEMj_ESI.map.PowerLawFitCalculationExecutor;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
import de.m_entrup.EFTEMj_lib.EFTEMj_Prefs;
import ij.Prefs;

/**
 * This Frame is used to setup the elemental map calculation.
 */
@SuppressWarnings("serial")
public class MapSetupDialog extends EFTEMFrame {

	/**
	 * The {@link LoadAndSaveConfig} reads and writes settings of the
	 * {@link MapSetupDialog}. The settings of the last calculation are restored
	 * instead of using the default settings.
	 */
	private class LoadAndSaveConfig {

		private final static String ESI_PREFIX = "ESI.";

		/**
		 * Read the settings of {@link MapSetupDialog} from IJ_Prefs.txt.
		 */
		private void readSettings() {
			epsilon = (float) Prefs.get(EFTEMj_Prefs.PREFS_PREFIX + ESI_PREFIX + "epsilon", epsilon);
		}

		/**
		 * Write the settings of {@link MapSetupDialog} to IJ_Prefs.txt.
		 */
		private void writeSettings() {
			Prefs.set(EFTEMj_Prefs.PREFS_PREFIX + ESI_PREFIX + "epsilon", epsilon);
			Prefs.savePreferences();
		}

	} // END ConfigFileHandler

	/**
	 * The {@link MapSetupListener} extends {@link OkCancelListener} and
	 * implements the OK- and Cancel-operations.
	 */
	private class MapSetupListener extends OkCancelListener {

		@Override
		protected void cancelOperation() {
			MapSetupDialog.this.dispose();
			PluginAPI.getInstance().enableMainMenuButtons();
		}

		@Override
		protected void okOperation() {
			eLoss = Float.valueOf(eLossField.getText());
			epsilon = Float.valueOf(epsilonField.getText());
			try {
				PluginAPI.getInstance().getDatasetAPI().createDatasetMapInput(eLoss, epsilon);
			} catch (final Exception e) {
				LogWriter.showWarningAndWriteLog(e.getMessage());
				return;
			}
			new LoadAndSaveConfig().writeSettings();
			MapSetupDialog.this.dispose();
			try {
				final PowerLawFitCalculationExecutor executor = new PowerLawFitCalculationExecutor();
				executor.execute();
				PluginAPI.getInstance().getMainMenu().showMapResultPanel();
			} catch (final Exception e) {
				LogWriter.showWarningAndWriteLog(e.getMessage());
			}
		}
	} // END MapSetupListener

	/**
	 * Detector quantum efficiency. A camera specific value that lowers the
	 * signal to noise ratio.
	 */
	// private double dqe = 1.0f;
	/**
	 * The energy loss the element signal starts with.
	 */
	private float eLoss = 0;
	/**
	 * A {@link JFormattedTextField} to edit the value of eLoss.
	 */
	private JFormattedTextField eLossField;
	/**
	 * The precision of the calculation. If the difference of <code>r</code>
	 * between 2 iterations is smaller as epsilon the calculation stops.
	 */
	private float epsilon = PluginConstants.EPSILON;
	/**
	 * A {@link JFormattedTextField} to edit the value of dqe.
	 */
	// private JFormattedTextField dqeField;
	/**
	 * A {@link JFormattedTextField} to edit the value of epsilon.
	 */
	private JFormattedTextField epsilonField;
	/**
	 * This is the Layout used by the optionPanel.
	 */
	private GridBagLayout gridBagLayout;

	/**
	 * The constructor creates a new {@link Frame} using the constructor of
	 * {@link EFTEMFrame}. The class {@link DescriptionPanel} is used for the
	 * description. A {@link Panel} using the {@link GridBagLayout} is placed at
	 * the CENTER and the {@link OkCancelPanel} is used at the SOUTH panel.
	 */
	public MapSetupDialog() {
		super(PluginMessages.getString("Titel.MapSetupDialog"));
		final LoadAndSaveConfig fileHandler = new LoadAndSaveConfig();
		fileHandler.readSettings();
		// NORTH: Description
		final DescriptionPanel northPanel = new DescriptionPanel(PluginMessages.getString("Label.MapSetupInfo"));
		northPanel.setDetailedDescription(PluginMessages.getString("Label.MapSetupDetailedInfo"));
		super.addToNorthPanel(northPanel);
		// CENTER: stackChooser
		super.addToCenterPanel(createOptionPanel());
		// SOUTH: OkCancelPanel
		final MapSetupListener listener = new MapSetupListener();
		super.addToSouthPanel(new OkCancelPanel(listener));
		// Frame setup
		this.addWindowListener(listener);
		this.pack();
		this.setResizable(false);
		this.setVisible(true);
	}

	/**
	 * This method combines all setting for adding a new {@link Component} to a
	 * {@link Panel}.
	 *
	 * @param comp
	 *            The {@link Component} you want to add.
	 * @param panel
	 *            The {@link Panel} where you add the {@link Component}.
	 * @param x
	 *            Column, starts at 0.
	 * @param y
	 *            Row, starts at 0.
	 * @param width
	 *            Width in rows.
	 * @param height
	 *            Height in columns.
	 */
	private void addElement(final Component comp, final Panel panel, final int x, final int y, final int width,
			final int height) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.ipadx = 10;
		gbc.ipady = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gridBagLayout.setConstraints(comp, gbc);
		panel.add(comp);
	}

	/**
	 * Sets the number of columns to 8 and the alignment to RIGHT.
	 *
	 * @param field
	 *            The {@link JFormattedTextField} that gets configured
	 */
	private void configField(final JFormattedTextField field) {
		field.setColumns(8);
		field.setHorizontalAlignment(SwingConstants.RIGHT);
	}

	/**
	 * The {@link Panel} at the position CENTER uses the {@link GridBagLayout}.
	 * Each row contains of a {@link JFormattedTextField} to setup a parameter
	 * for the map calculation and the corresponding {@link Label}.
	 */
	private Panel createOptionPanel() {
		final Panel optionPanel = new Panel();
		gridBagLayout = new GridBagLayout();
		optionPanel.setLayout(gridBagLayout);
		int pos = 0;
		// Title of the image that is used
		addElement(
				new Label(PluginMessages.getString("Label.SelectedStack")
						+ PluginAPI.getInstance().getDatasetAPI().getImagePlus().getTitle()),
				optionPanel, 0, pos, 2, 1);
		pos++;
		// Edge energy loss and related label
		final Label edgeLabel = new Label(PluginMessages.getString("Label.EdgeELoss"));
		final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		df.applyPattern("####.##");
		eLossField = new JFormattedTextField(df);
		this.configField(eLossField);
		final float edgeELoss = PluginAPI.getInstance().getDatasetAPI().getPredictedEdgeELoss();
		eLossField.setValue(edgeELoss);
		final String edgeString = PluginAPI.getInstance().getDatasetAPI().getPredictedEdgeLabel(Math.round(edgeELoss));
		addElement(new JLabel("<html>" + PluginMessages.getString("Label.EdgeString") + " " + edgeString + "</html>"),
				optionPanel, 0, pos, 2, 1);
		pos++;
		addElement(edgeLabel, optionPanel, 0, pos, 1, 1);
		addElement(eLossField, optionPanel, 1, pos, 1, 1);
		pos++;
		// Detector quantum efficiency
		/*
		 * pos++; JLabel dqeLabel = new
		 * JLabel(PluginMessages.getString("Label.DQE")); addElement(dqeLabel,
		 * optionPanel, 0, pos, 1, 1); DecimalFormat df1 = (DecimalFormat)
		 * DecimalFormat.getInstance(Locale.ENGLISH);
		 * df1.applyPattern("0.0###"); dqeField = new JFormattedTextField(df1);
		 * this.configField(dqeField); dqeField.setValue(dqe);
		 * addElement(dqeField, optionPanel, 1, pos, 1, 1);
		 */
		// Exit condition for the MLE
		final Label epsilonLabel = new Label(PluginMessages.getString("Label.MapPrecision"));
		addElement(epsilonLabel, optionPanel, 0, pos, 1, 1);
		final DecimalFormat df2 = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		df2.applyPattern("0.0E0#");
		epsilonField = new JFormattedTextField(df2);
		this.configField(epsilonField);
		epsilonField.setValue(epsilon);
		addElement(epsilonField, optionPanel, 1, pos, 1, 1);
		return optionPanel;
	}
}
