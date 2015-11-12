
package de.m_entrup.EFTEMj_ESI.gui;

import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import de.m_entrup.EFTEMj_ESI.tools.ImagePlusTool;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
import ij.ImagePlus;
import ij.WindowManager;

/**
 * This is a simple frame that allows you to select an image from a
 * {@link Choice}.
 */
@SuppressWarnings("serial")
public class ChangeStackDialog extends EFTEMFrame {

	/**
	 * The {@link ChangeStackListener} extends {@link OkCancelListener} and
	 * implements the OK- and Cancel-operations.
	 */
	private class ChangeStackListener extends OkCancelListener {

		@Override
		protected void cancelOperation() {
			ChangeStackDialog.this.dispose();
			PluginAPI.getInstance().enableMainMenuButtons();
		}

		/**
		 * A new instance of {@link DatasetAPI} is created using the selected
		 * {@link ImagePlus}.
		 */
		private void changeToSelectedStack() {
			final String titleSelectedImage = stackChooser.getSelectedItem();
			// If 2 images contain the same title, always the first one will be
			// selected.
			final ImagePlus imagePlus = WindowManager.getImage(titleSelectedImage);
			if (imagePlus == null) {
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.UnableToSelectImage"));
				return;
			}
			PluginAPI.getInstance().initDatasetAPI(imagePlus);
			PluginAPI.getInstance().enableMainMenuButtons();
		}

		@Override
		protected void okOperation() {
			ChangeStackDialog.this.dispose();
			changeToSelectedStack();
		}
	} // END ChangeStackListener

	/**
	 * This field is used to gain access to the {@link Choice} component.
	 */
	private final Choice stackChooser;

	/**
	 * The constructor creates a new {@link Frame} using the constructor of
	 * {@link EFTEMFrame}. The class {@link DescriptionPanel} is used for the
	 * description. The {@link Choice} is placed at the CENTER and the
	 * {@link OkCancelPanel} is used at the SOUTH panel.
	 */
	public ChangeStackDialog() {
		super(PluginMessages.getString("Title.ChangeStackDialog"));
		// NORTH: Description
		final DescriptionPanel northPanel = new DescriptionPanel(PluginMessages
			.getString("Label.ChangeStackInfo"));
		northPanel.setDetailedDescription(PluginMessages.getString(
			"Label.ChangeStackDetailedInfo"));
		super.addToNorthPanel(northPanel);
		// CENTER: stackChooser
		final Panel centerPanel = new Panel(new FlowLayout(FlowLayout.CENTER,
			PluginConstants.LAYOUT__GAP, PluginConstants.LAYOUT__GAP));
		stackChooser = new Choice();
		for (int i = 0; i < ImagePlusTool.getImagePlusTitels().length; i++) {
			stackChooser.add(ImagePlusTool.getImagePlusTitels()[i]);
		}
		centerPanel.add(stackChooser);
		super.addToCenterPanel(centerPanel);
		// SOUTH: OkCancelPanel
		final ChangeStackListener listener = new ChangeStackListener();
		super.addToSouthPanel(new OkCancelPanel(listener));
		// Frame setup
		this.addWindowListener(listener);
		this.pack();
		this.setResizable(false);
		this.setVisible(true);
	}
}
