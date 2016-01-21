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

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Panel;

import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;

/**
 * This class extends from {@link Panel}. It uses the {@link FlowLayout} to
 * arrange a OK- and a Cancel-button.
 */
@SuppressWarnings("serial")
public class OkCancelPanel extends Panel {

	/**
	 * An instance of {@link OkCancelPanel} is created, that uses a
	 * {@link OkCancelListener} to react on pressing the OK- or the Cancel-button.
	 *
	 * @param actionListener Your implementation of the abstract class
	 *          {@link OkCancelListener}.
	 */
	public OkCancelPanel(final OkCancelListener actionListener) {
		this.setLayout(new FlowLayout(FlowLayout.CENTER,
			PluginConstants.LAYOUT__GAP, PluginConstants.LAYOUT__GAP));
		final Button okButton = new Button(PluginMessages.getString("Button.OK"));
		okButton.addActionListener(actionListener);
		this.add(okButton);
		final Button cancelButton = new Button(PluginMessages.getString(
			"Button.Cancel"));
		cancelButton.addActionListener(actionListener);
		this.add(cancelButton);
	}
}
