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

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.util.Hashtable;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.driftcorrection.DriftExecutor;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
import ij.IJ;
import ij.ImagePlus;

/**
 * This Frame is used to setup the drift correction.
 */
@SuppressWarnings("serial")
public class DriftSetupDialog extends EFTEMFrame implements ChangeListener {

	private Label sliderSelectionLabel;

	/**
	 * The {@link DriftSetupListener} extends {@link OkCancelListener} and
	 * implements the OK- and Cancel-operations.
	 */
	private class DriftSetupListener extends OkCancelListener {

		@Override
		protected void cancelOperation() {
			DriftSetupDialog.this.dispose();
			PluginAPI.getInstance().enableMainMenuButtons();
			return;
		}

		@Override
		protected void okOperation() {
			// int delta = new Integer(deltaField.getText());
			final int delta = deltaSlider.getValue();
			// int index = new Integer(templateField.getText());
			final int index = templateMenu.getSelectedIndex() + 1;
			if (index < 1 | index > datasetAPI.getStackSize()) {
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.TemplateIndex"));
				return;
			}
			if (datasetAPI.createDatasetDriftInput(delta, index) == false) {
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.Delta"));
				return;
			}
			DriftSetupDialog.this.dispose();
			try {
				final DriftExecutor executor = new DriftExecutor();
				executor.execute();
			}
			catch (final Exception e) {
				IJ.showMessage(e.getMessage());
			}
		}
	} // END DriftSetupListener

	/**
	 * A shortcut to access the DatasetAPI.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * At this field the max image shift is set. It can be accessed by the inner
	 * ActionListener class.
	 */
	// private JFormattedTextField deltaField;
	/**
	 * At this slider the max image shift is set. It can be accessed by the inner
	 * ActionListener class.
	 */
	private JSlider deltaSlider;

	/**
	 * At this field the index of the reference image is set. It can be accessed
	 * by the inner ActionListener class.
	 */
	// private JFormattedTextField templateField;
	/**
	 * At this pop-up menu the reference image is set. It can be accessed by the
	 * inner ActionListener class.
	 */
	private Choice templateMenu;

	/**
	 * The constructor creates a new {@link Frame} using the constructor of
	 * {@link EFTEMFrame}. The class {@link DescriptionPanel} is used for the
	 * description. A {@link Panel} using the {@link GridLayout} is placed at the
	 * CENTER and the {@link OkCancelPanel} is used at the SOUTH panel.
	 */
	public DriftSetupDialog() {
		super(PluginMessages.getString("Titel.DriftSetupDialog"));
		// NORTH: description
		final DescriptionPanel northPanel = new DescriptionPanel(PluginMessages
			.getString("Label.DriftSetupInfo"));
		northPanel.setDetailedDescription(PluginMessages.getString(
			"Label.DriftSetupDetailedInfo"));
		super.addToNorthPanel(northPanel);
		// CENTER: variable table to edit the energy loss
		super.addToCenterPanel(createOptionPanel());
		// SOUTH: OkCancelPanel
		final DriftSetupListener listener = new DriftSetupListener();
		super.addToSouthPanel(new OkCancelPanel(listener));
		// Frame setup
		this.addWindowListener(listener);
		this.pack();
		this.setResizable(false);
		this.setVisible(true);
	}

	/**
	 * Sets the number of columns to 3 and the alignment to RIGHT.
	 *
	 * @param field The {@link JFormattedTextField} that gets configured
	 */
	/*
	 * private void configField(JFormattedTextField field) {
	 * field.setColumns(3); field.setHorizontalAlignment(SwingConstants.RIGHT);
	 * }
	 */

	/**
	 * At the CENTER of the {@link BorderLayout} an {@link GridLayout} is added
	 * that contains the {@link JFormattedTextField}s for delta and templateIndex.
	 * For both {@link JFormattedTextField}s an {@link Label} shows a short
	 * description.
	 */
	private Panel createOptionPanel() {
		// Configuration delta
		final int maxDelta = getRoiBorderDist();
		// The largest value should be a multiple of 10. But this only counts if
		// larger than 10.
		deltaSlider = new JSlider(0, Math.min(Math.max(maxDelta / 10 * 10, 10),
			maxDelta));
		final Hashtable<Integer, JLabel> labelTable =
			new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel(Integer.toString(0)));
		labelTable.put(new Integer(deltaSlider.getMaximum() / 2), new JLabel(Integer
			.toString(deltaSlider.getMaximum() / 2)));
		labelTable.put(new Integer(deltaSlider.getMaximum()), new JLabel(Integer
			.toString(deltaSlider.getMaximum())));
		deltaSlider.setValue(Math.min(maxDelta / 2, 20));
		deltaSlider.setLabelTable(labelTable);
		deltaSlider.setMinorTickSpacing(Math.min(Math.max(maxDelta / 10, 1), 10));
		deltaSlider.setPaintLabels(true);
		deltaSlider.setPaintTicks(true);
		deltaSlider.setBackground(this.getBackground());
		deltaSlider.addChangeListener(this);

		sliderSelectionLabel = new Label(String.format("%d", deltaSlider
			.getValue()));
		/*
		 * deltaField = new JFormattedTextField(new NumberFormatter( new
		 * DecimalFormat("####")));
		 * deltaField.setValue(PluginConstants.DRIFT__DEFAULT_DELTA);
		 * this.configField(deltaField);
		 */
		// Configuration templateIndex
		templateMenu = new Choice();
		for (int i = 0; i < datasetAPI.getStackSize(); i++) {
			templateMenu.add(datasetAPI.getShortSliceLabel(i));
		}
		/*
		 * templateField = new JFormattedTextField(new NumberFormatter( new
		 * DecimalFormat("####"))); templateField.setValue(1);
		 * this.configField(templateField);
		 */
		// Create a GridLayout
		final Panel panel = new Panel();
		final GridLayout gLayout = new GridLayout(4, 2, PluginConstants.LAYOUT__GAP,
			PluginConstants.LAYOUT__GAP);
		panel.setLayout(gLayout);
		// Title of the image that is used
		panel.add(new Label(PluginMessages.getString("Label.SelectedStack")));
		panel.add(new Label(PluginAPI.getInstance().getDatasetAPI().getImagePlus()
			.getTitle()));
		// Add delta to GridLayout
		panel.add(new Label(PluginMessages.getString("Label.Delta")));
		panel.add(deltaSlider);
		panel.add(new Label(PluginMessages.getString("Label.DeltaSelected")));
		panel.add(sliderSelectionLabel);
		// panel.add(deltaField);
		// Add templateIndex to GridLayout
		panel.add(new Label(PluginMessages.getString("Label.TemplateImage")));
		panel.add(templateMenu);
		// panel.add(templateField);
		return panel;
	}

	private int getRoiBorderDist() {
		final ImagePlus imp = datasetAPI.getImagePlus();
		int dist = Math.max(imp.getWidth(), imp.getHeight());
		final Rectangle roi = imp.getRoi().getBounds();
		if (roi.x < dist) dist = roi.x;
		if (roi.y < dist) dist = roi.y;
		final int spacingRight = imp.getWidth() - roi.x - roi.width;
		if (spacingRight < dist) dist = spacingRight;
		final int spacingBot = imp.getHeight() - roi.y - roi.height;
		if (spacingBot < dist) dist = spacingBot;
		return dist;
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		sliderSelectionLabel.setText(String.format("%d", deltaSlider.getValue()));
	}

}
