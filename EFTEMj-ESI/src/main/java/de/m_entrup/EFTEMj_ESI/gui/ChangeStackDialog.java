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
				LogWriter.showWarningAndWriteLog(PluginMessages.getString("Error.UnableToSelectImage"));
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
		final DescriptionPanel northPanel = new DescriptionPanel(PluginMessages.getString("Label.ChangeStackInfo"));
		northPanel.setDetailedDescription(PluginMessages.getString("Label.ChangeStackDetailedInfo"));
		super.addToNorthPanel(northPanel);
		// CENTER: stackChooser
		final Panel centerPanel = new Panel(
				new FlowLayout(FlowLayout.CENTER, PluginConstants.LAYOUT__GAP, PluginConstants.LAYOUT__GAP));
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
