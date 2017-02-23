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
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import ij.gui.GUI;

/**
 * The {@link DescriptionPanel} uses the {@link BoxLayout} to display a
 * {@link Label} with a short description.<br>
 * If you set a detailed description, a {@link Button} is added to open a dialog
 * that displays this description.
 */
@SuppressWarnings("serial")
public class DescriptionPanel extends Panel {

	/**
	 * This {@link ActionListener} opens a new dialog if the detailButton is
	 * pressed and displays the {@link String} detailedDescription.
	 */
	private class DescriptionPanelActionListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (e.getActionCommand() == PluginMessages.getString("Button.MoreInfo")) {
				new InfoDialog(PluginMessages.getString("Titel.DetailedInfo"), detailedDescription);
			}
		}
	} // END DescriptionPanelActionListener

	/**
	 * The detailed description that will be displayed in a dialog.
	 */
	private String detailedDescription;

	/**
	 * Creates a {@link DescriptionPanel} using the {@link BoxLayout}. The only
	 * used component is a {@link Label}.
	 *
	 * @param text
	 *            The text of the {@link Label}.
	 */
	public DescriptionPanel(final String text) {
		super();
		final BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(boxLayout);
		final Panel descriptionPanel = new Panel();
		final JLabel description = new JLabel(text);
		descriptionPanel.add(description);
		this.add(descriptionPanel);
	}

	/**
	 * This method adds a {@link Button} to the {@link DescriptionPanel} that
	 * opens a dialog to display the detailed description.
	 *
	 * @param text
	 *            The detailed description.
	 */
	public void setDetailedDescription(final String text) {
		detailedDescription = text;
		final Button detailButton = new Button(PluginMessages.getString("Button.MoreInfo"));
		detailButton.addActionListener(new DescriptionPanelActionListener());
		final Panel buttonPanel = new Panel();
		buttonPanel.add(detailButton);
		this.add(buttonPanel);
	}

	/**
	 * This Class was created as a non modal Version of
	 * {@link ij.gui.HTMLDialog}.
	 */
	private class InfoDialog extends JDialog implements ActionListener {

		public InfoDialog(final String title, final String message) {
			super(ij.IJ.getInstance(), title, true);
			init(message);
		}

		private void init(final String message) {
			ij.util.Java2.setSystemLookAndFeel();
			final Container container = getContentPane();
			container.setLayout(new BorderLayout());
			String labelStr;
			if (message == null)
				labelStr = "";
			else
				labelStr = message;
			final JLabel label = new JLabel(labelStr);
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
			panel.add(label);
			container.add(panel, "Center");
			final JButton button = new JButton(PluginMessages.getString("Button.OK"));
			button.addActionListener(this);
			panel = new JPanel();
			panel.add(button);
			container.add(panel, "South");
			setForeground(Color.black);
			pack();
			GUI.center(this);
			setModal(false);
			setVisible(true);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			dispose();
		}
	}
}
