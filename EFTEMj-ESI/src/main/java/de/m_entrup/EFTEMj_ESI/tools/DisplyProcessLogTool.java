
package de.m_entrup.EFTEMj_ESI.tools;

import java.awt.FileDialog;
import java.util.Calendar;
import java.util.Locale;
import java.util.Vector;

import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import ij.IJ;
import ij.gui.GenericDialog;

/**
 * This class contains a static method that creates a {@link GenericDialog} to
 * display the process logs.
 */
public class DisplyProcessLogTool {

	/**
	 * A dialog containing the current process logs is displayed. It is possible
	 * to save the log as txt file.
	 */
	public static void showExportDialog(final String fileName) {
		IJ.showStatus("Finished Elemental-Mapping");
		final GenericDialog gd = new GenericDialog(PluginMessages.getString(
			"Titel.ProcessLog"));
		final Vector<String> logs = LogWriter.getProcessLog();
		int maxLengt = 0;
		String text = "";
		for (int i = 0; i < logs.size(); i++) {
			if (logs.get(i).length() > maxLengt) {
				maxLengt = logs.get(i).length();
			}
			text += logs.get(i) + PluginConstants.LINE_SEPARATOR;
		}
		gd.addTextAreas(text, null, logs.size() + 1, maxLengt + 5);
		gd.addMessage(PluginMessages.getString("Label.SaveLog"));
		gd.setResizable(false);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		final FileDialog fDialog = new FileDialog(gd, PluginMessages.getString(
			"Titel.SaveProcessLog"), FileDialog.SAVE);
		/*
		 * MultiMode is not available in Java 6
		 * fDialog.setMultipleMode(false);
		 */
		fDialog.setDirectory(IJ.getDirectory("image"));
		// adds date and time to the file name
		final Calendar cal = Calendar.getInstance();
		String extendedFileName = fileName + "_" + String.format(Locale.ENGLISH,
			"%tF-%tR", cal, cal);
		// remove the ':' that is not allowed as at a file name
		final int pos = extendedFileName.indexOf(":");
		extendedFileName = extendedFileName.substring(0, pos) + extendedFileName
			.substring(pos + 1);
		fDialog.setFile(extendedFileName + ".txt");
		fDialog.setVisible(true);
		if (fDialog.getFile() != null) {
			final String path = fDialog.getDirectory() + System.getProperty(
				"file.separator") + fDialog.getFile();
			IJ.saveString(text, path);
		}
	}

}
