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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;

/**
 * The {@link OkCancelListener} is a small abstract class that implements the
 * method <code>actionPerformed(ActionEvent e)</code> and handles buttons with
 * the text <code>PluginMessages.getString("Button.OK")</code> and
 * <code>PluginMessages.getString("Button.Cancel")</code>.<br>
 * Additionally it implements {@link WindowListener} to handle the closing of
 * the window. If the window is closed the Cancel-operation is called. Use the
 * following methods:<br>
 * <code>this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 * 		this.addWindowListener(listener);</code><br>
 * <code>listener</code> has to extend {@link OkCancelListener}.
 */
public abstract class OkCancelListener implements ActionListener,
	WindowListener
{

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand() == PluginMessages.getString("Button.OK")) {
			okOperation();
		}
		if (e.getActionCommand() == PluginMessages.getString("Button.Cancel")) {
			cancelOperation();
		}
	}

	/**
	 * The method that is called if the Cancel-button is pressed.
	 */
	protected abstract void cancelOperation();

	/**
	 * The method that is called if the OK-button is pressed.
	 */
	protected abstract void okOperation();

	@Override
	public void windowActivated(final WindowEvent e) {
		return;
	}

	@Override
	public void windowClosed(final WindowEvent e) {
		return;
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		cancelOperation();
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
		return;
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
		return;
	}

	@Override
	public void windowIconified(final WindowEvent e) {
		return;
	}

	@Override
	public void windowOpened(final WindowEvent e) {
		return;
	}

}
