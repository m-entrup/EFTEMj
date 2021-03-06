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
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Panel;

import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import ij.ImageJ;

/**
 * This is the the GUI template i use for each frame of the plugin. It uses the
 * {@link BorderLayout} and is divided into 3 sections. The NORTH contains the
 * description (except for the {@link MainMenu}). The CENTER is the main part of
 * the frame and the SOUTH includes the OK- and the Cancel-button. At the
 * {@link MainMenu} the progress bar is located at the SOUTH.
 */
@SuppressWarnings("serial")
public abstract class EFTEMFrame extends Frame {

	/**
	 * This {@link Panel} contains the main components of the {@link Frame}.
	 */
	private final Panel centerPanel;
	/**
	 * This {@link Panel} contains a description.
	 */
	private final Panel northPanel;
	/**
	 * This {@link Panel} contains the OK- and the Cancel-button.
	 */
	private final Panel southPanel;

	/**
	 * The 3 {@link Panel} are placed at NORTH, CENTER and SOUTH of a
	 * {@link BorderLayout}. Gaps and the border are assigned.
	 *
	 * @param title
	 *            The title of the Frame.
	 * @throws HeadlessException
	 */
	public EFTEMFrame(final String title) throws HeadlessException {
		super(title);
		PluginAPI.getInstance().disableMainMenuButtons(true);
		final BorderLayout borderLayout = new BorderLayout(PluginConstants.LAYOUT__BORDER_LAYOUT_GAP,
				PluginConstants.LAYOUT__BORDER_LAYOUT_GAP);
		this.setLayout(borderLayout);
		this.setBackground(ImageJ.backgroundColor);
		northPanel = new Panel();
		centerPanel = new Panel();
		southPanel = new Panel();
		this.add(northPanel, BorderLayout.NORTH);
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);
	}

	/**
	 * Adds a {@link Component} to the centerPanel.
	 *
	 * @param comp
	 *            The {@link Component} you want to add.
	 */
	public void addToCenterPanel(final Component comp) {
		centerPanel.add(comp);
	}

	/**
	 * Adds a {@link Component} to the northPanel.
	 *
	 * @param comp
	 *            The {@link Component} you want to add.
	 */
	public void addToNorthPanel(final Component comp) {
		northPanel.add(comp);
	}

	/**
	 * Adds a {@link Component} to the southPanel.
	 *
	 * @param comp
	 *            The {@link Component} you want to add.
	 */
	public void addToSouthPanel(final Component comp) {
		southPanel.add(comp);
	}
}
