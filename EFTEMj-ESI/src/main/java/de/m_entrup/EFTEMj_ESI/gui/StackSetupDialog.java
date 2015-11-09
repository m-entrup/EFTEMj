
package de.m_entrup.EFTEMj_ESI.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import de.m_entrup.EFTEMj_ESI.tools.ELossTool;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
import ij.ImagePlus;
import ij.ImageStack;

/**
 * At this frame you can change the energy loss of each image from the selected
 * {@link ImageStack}.
 */
@SuppressWarnings("serial")
public class StackSetupDialog extends EFTEMFrame {

	/**
	 * The {@link StackSetupListener} extends {@link OkCancelListener} and
	 * implements the OK- and Cancel-operations.
	 */
	private class StackSetupListener extends OkCancelListener {

		@Override
		protected void cancelOperation() {
			StackSetupDialog.this.dispose();
			PluginAPI.getInstance().enableMainMenuButtons();
		}

		@Override
		protected void okOperation() {
			setELoss(StackSetupDialog.this.textFields);
			PluginAPI.getInstance().updateMainMenu();
			StackSetupDialog.this.dispose();
			PluginAPI.getInstance().enableMainMenuButtons();
		}

		/**
		 * For each image the value of the related {@link JFormattedTextField} is
		 * saved as energy loss. The title of each image is changed to match the new
		 * energy loss.
		 *
		 * @param textFields An array of {@link JFormattedTextField} that uses the
		 *          same index as the selected {@link ImagePlus}. The
		 *          {@link JFormattedTextField} has to contain Integers only.
		 */
		private void setELoss(final JFormattedTextField[] textFields) {
			// new in v0.6:
			// An error is reported if invalid values are entered.
			// If the eLoss is not changed for a slice, the code inside the
			// for-loop is skipped.
			boolean isChanged = false;
			for (int i = 0; i < textFields.length; i++) {
				final Float value = new Float(textFields[i].getText());
				if (value < 0) {
					LogWriter.showWarningAndWriteLog(PluginMessages.getString(
						"Error.eLossLessThanZero"));
				}
				else if (Float.isNaN(value)) {
					LogWriter.showWarningAndWriteLog(PluginMessages.getString(
						"Error.eLossIsNAN"));
				}
				else if (Float.isInfinite(value)) {
					LogWriter.showWarningAndWriteLog(PluginMessages.getString(
						"Error.eLossIsInfinite"));
				}
				else {
					if (datasetAPI.getELossArray()[i] != value) {
						datasetAPI.setELoss(i, value);
						String label;
						// Labels of stacks and single images are handled
						// different.
						if (datasetAPI.getStackSize() == 1) {
							label = datasetAPI.getImagePlus().getTitle();
						}
						else {
							label = datasetAPI.getSliceLabel(i);
						}
						// A search for a arbitrary count of numbers (including
						// '.' and ',') and the string "eV" that can be found
						// between box brackets.
						final Matcher matcher1 = Pattern.compile(
							PluginConstants.PATTERN_ELOSS_LONG).matcher(label);
						if (matcher1.find()) {
							String s1;
							String s2;
							if (matcher1.start() == 0) {
								s1 = "";
							}
							else {
								s1 = label.substring(0, matcher1.start());
							}
							if (matcher1.end() > label.length() - 3) {
								s2 = "";
							}
							else {
								s2 = label.substring(matcher1.end());
							}

							if (datasetAPI.getELossArray()[i] != 0) {
								label = s1 + "[" + datasetAPI.getELossAsString(i) + "eV]" + s2;
							}
							else {
								label = s1 + s2;
							}
						}
						else {
							// A second search without box brackets
							final Matcher matcher2 = Pattern.compile(
								PluginConstants.PATTERN_ELOSS_SHORT).matcher(label);
							if (matcher2.find()) {
								String s1;
								String s2;
								if (matcher2.start() == 0) {
									s1 = "";
								}
								else {
									s1 = label.substring(0, matcher2.start());
								}
								if (matcher2.end() > label.length() - 2) {
									s2 = "";
								}
								else {
									s2 = label.substring(matcher2.end());
								}

								if (datasetAPI.getELossArray()[i] != 0) {
									label = s1 + datasetAPI.getELossAsString(i) + "eV" + s2;
								}
								else {
									label = s1 + s2;
								}
							}
							else {
								/*
								 * If the slice label does not contain an energy
								 * loss, this part is used: The slice label
								 * contains the image name including the
								 * appendix and an image description. Both parts
								 * are separated by a '\n'. The energy loss is
								 * placed behind the filename, but before the
								 * appendix. If the slice label does not contain
								 * an appendix the energy loss is placed before
								 * the '\n'.
								 */
								if (datasetAPI.getELossArray()[i] != 0) {
									String s1;
									String s2;
									// Search for '.' and '\n'
									if (label.indexOf('.') != -1 & label.indexOf("\n") != -1) {
										// If the '.' is before the '\n' a
										// appendix
										// has
										// been found
										if (label.lastIndexOf('.', label.indexOf("\n")) != -1) {
											s1 = label.substring(0, label.lastIndexOf('.', label
												.indexOf("\n")));
											s2 = label.substring(label.lastIndexOf('.', label.indexOf(
												"\n")));
											// The '.' contains to the image
											// description
										}
										else {
											s1 = label.substring(0, label.indexOf("\n"));
											s2 = label.substring(label.indexOf("\n"));
										}
										// Only a '.' but no image description
									}
									else if (label.lastIndexOf('.') != -1) {
										s1 = label.substring(0, label.lastIndexOf('.'));
										s2 = label.substring(label.lastIndexOf('.'));
										// No '.' but an image description
									}
									else if (label.indexOf("\n") != -1) {
										s1 = label.substring(0, label.indexOf("\n"));
										s2 = label.substring(label.indexOf("\n"));
										// No '.' and no image description
									}
									else {
										s1 = label;
										s2 = "";
									}
									label = s1 + "_[" + datasetAPI.getELossAsString(i) + "eV]" +
										s2;
								}
							}
						}
						// If it's an single image the title of the ImagePlus
						// has to
						// be
						// changed.
						// If it's an stack, each slice has to be changed, but
						// the
						// ImagePlus title is still the same.
						if (i == 0 & datasetAPI.getStackSize() == 1) {
							datasetAPI.getImagePlus().setTitle(label);
						}
						else {
							datasetAPI.setSliceLabel(i, label);
						}
						isChanged = true;
					}
				}
			} // end for-loop
				// Mark the ImagePlus as changed and repaint it
			if (isChanged == true) {
				datasetAPI.getImagePlus().changes = true;
				datasetAPI.getImagePlus().updateAndRepaintWindow();
			}
		}

	} // END StackSetupListener

	/**
	 * A shortcut to access the datasetAPI.
	 */
	private final DatasetAPI datasetAPI = PluginAPI.getInstance().getDatasetAPI();
	/**
	 * This is the Layout used by the gridBagPanel.
	 */
	private final GridBagLayout gridBagLayout;
	/**
	 * The gridBagPanel of the {@link StackSetupDialog}, it uses the
	 * {@link GridBagLayout} to display a variable table showing a row for each
	 * image of the stack.
	 */
	private final Panel gridBagPanel;

	/**
	 * All {@link JFormattedTextField} used to set the energy loss are stored in
	 * this array. {@link JFormattedTextField} is used to allow the input of
	 * numbers only.
	 */
	private final JFormattedTextField[] textFields;

	/**
	 * The constructor creates a new {@link Frame} using the constructor of
	 * {@link EFTEMFrame}. The class {@link DescriptionPanel} is used for the
	 * description. The gridBagPanel is placed at the CENTER and the
	 * {@link OkCancelPanel} is used at the SOUTH panel.
	 */
	public StackSetupDialog() {
		super(PluginMessages.getString("Title.StackSetupDialog"));
		// NORTH: description
		final DescriptionPanel northPanel = new DescriptionPanel(PluginMessages
			.getString("Label.StackSetupInfo"));
		northPanel.setDetailedDescription(PluginMessages.getString(
			"Label.StackSetupDetailedInfo"));
		super.addToNorthPanel(northPanel);
		// CENTER: variable table to edit the energy loss
		textFields = new JFormattedTextField[datasetAPI.getStackSize()];
		gridBagPanel = new Panel();
		gridBagLayout = new GridBagLayout();
		gridBagPanel.setLayout(gridBagLayout);
		addELossFields();
		super.addToCenterPanel(gridBagPanel);
		// SOUTH: OkCancelPanel
		final StackSetupListener listener = new StackSetupListener();
		super.addToSouthPanel(new OkCancelPanel(listener));
		// Frame setup
		this.addWindowListener(listener);
		this.pack();
		this.setResizable(false);
		this.setVisible(true);
	}

	/**
	 * This method collects all setting for adding a new {@link Component} to the
	 * gridBagPanel of {@link StackSetupDialog}.
	 *
	 * @param comp The {@link Component} you want to add
	 * @param x Column, starts at 0
	 * @param y Row, starts at 0
	 * @param width Width in rows
	 * @param height Height in columns
	 */
	private void addElement(final Component comp, final int x, final int y,
		final int width, final int height)
	{
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.ipadx = 10;
		gbc.ipady = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gridBagLayout.setConstraints(comp, gbc);
		gridBagPanel.add(comp);
	}

	/**
	 * A table like layout is used with 2 rows and a variable number of columns.
	 * The first row shows the current name of all images that are stored in the
	 * selected {@link ImageStack}. The second row contains one or more
	 * {@link JFormattedTextField} that lists the current energy loss of the image
	 * and and it allows to edit the energy loss.<br>
	 * The first row has a width of 2 and the second one a width of 1.
	 */
	private void addELossFields() {
		// Title of the image that is used
		addElement(new Label(PluginMessages.getString("Label.SelectedStack") +
			PluginAPI.getInstance().getDatasetAPI().getImagePlus().getTitle()), 0, 0,
			3, 1);
		// Title of the first row
		addElement(new Label(PluginMessages.getString("Label.ImageTitle")), 0, 1, 2,
			1);
		// Title of the second row
		addElement(new Label(PluginMessages.getString("Label.ELoss")), 2, 1, 1, 1);
		final Panel placeholder = new Panel();
		placeholder.setSize(new Dimension(this.getWidth(), 1));
		addElement(placeholder, 0, 2, 3, 1);
		// For each image of the stack a new column is added
		for (int i = 0; i < datasetAPI.getStackSize(); i++) {
			if (datasetAPI.getStackSize() > 1) {
				addElement(new Label(datasetAPI.getShortSliceLabel(i)), 0, i + 3, 2, 1);
			}
			else {
				addElement(new Label(datasetAPI.getImagePlusShortTitle()), 0, i + 3, 2,
					1);
			}
			final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
				Locale.ENGLISH);
			df.applyPattern("####.##");
			final JFormattedTextField eLossField = new JFormattedTextField(df);
			this.configField(eLossField);
			if (datasetAPI.getELossArray()[i] != 0) {
				eLossField.setValue(datasetAPI.getELossArray()[i]);
			}
			else {
				eLossField.setValue(ELossTool.eLossFromTitle(datasetAPI.getImagePlus(),
					i));
			}
			textFields[i] = eLossField;
			addElement(eLossField, 2, i + 3, 1, 1);
		}
	}

	/**
	 * Sets the number of columns to 7 and the alignment to RIGHT.
	 *
	 * @param field The {@link JFormattedTextField} that gets configured
	 */
	private void configField(final JFormattedTextField field) {
		field.setColumns(7);
		field.setHorizontalAlignment(SwingConstants.RIGHT);
	}

}
