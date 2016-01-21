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

package de.m_entrup.EFTEMj_ESI.plugin;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import de.m_entrup.EFTEMj_ESI.gui.MainMenu;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
import de.m_entrup.EFTEMj_lib.EFTEMj_Debug;
import ij.plugin.PlugIn;
import ij.plugin.frame.PlugInFrame;

/*
 ImageJ Elemental-Map-PlugIn: This IJ PlugIn can be used to calculate
 elemental maps form energy filtered Transmission Electron Microscope images.
 Copyright (C) 2011  Michael Epping <michael.epping@uni-muenster.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This class is used by ImageJ to start the main menu of my
 * Elemental-Map-PlugIn. The {@link MainMenu} uses Swing instead of AWT (used by
 * ImageJ), thats why i don't use {@link PlugInFrame}.
 */
public class PluginLoader implements PlugIn {

	@Override
	public void run(final String arg) {
		// I change the default font of JButton an JLabel to remove the bold
		// text that is used at the look and feel is metal
		final UIDefaults uiDefaults = UIManager.getDefaults();
		uiDefaults.put("Label.font", uiDefaults.get("TextField.font"));
		uiDefaults.put("Button.font", uiDefaults.get("TextField.font"));
		if (PluginAPI.getInstance().getMainMenu() == null) {
			LogWriter.newSession();
			PluginAPI.getInstance().initMainMenu();
		}
		else {
			final MainMenu mainMenu = PluginAPI.getInstance().getMainMenu();
			mainMenu.setVisible(true);
			mainMenu.toFront();
		}
	}

	public static void main(final String[] args) {
		EFTEMj_Debug.debug(PluginLoader.class);
	}

}
