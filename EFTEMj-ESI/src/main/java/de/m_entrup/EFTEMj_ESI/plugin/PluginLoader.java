
package de.m_entrup.EFTEMj_ESI.plugin;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import de.m_entrup.EFTEMj_ESI.gui.MainMenu;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
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

}
