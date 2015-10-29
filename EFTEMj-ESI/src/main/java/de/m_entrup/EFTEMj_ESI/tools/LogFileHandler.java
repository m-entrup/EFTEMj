
package de.m_entrup.EFTEMj_ESI.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import ij.IJ;

public class LogFileHandler {

	private final File logFile;
	private final String logEntry;

	/**
	 * Creates an {@link File}-object. The used path is<br />
	 * <code>IJ.getDirectory("imagej")</code>.
	 */
	public LogFileHandler(final String logEntry) {
		logFile = new File(IJ.getDirectory("imagej") + PluginMessages.getString(
			"File.Log"));
		this.logEntry = logEntry;
	}

	/**
	 * When no configFile exits a new one is created.
	 */
	private void createFile() {
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
				writeLog();
			}
			catch (final IOException e) {
				IJ.showMessage(PluginMessages.getString("Titel.Warning"), PluginMessages
					.getString("Error.logFileCreate") + "<p>" + e.getMessage() +
					"</p></html>");
			}
		}
		else {
			IJ.showMessage(PluginMessages.getString("Titel.Warning"), PluginMessages
				.getString("Error.logFileExists"));
		}
	}

	/**
	 * A {@link FileWriter} is used to write the log file line by line.
	 *
	 * @throws IOException If the {@link FileWriter} can not write into the log
	 *           file.
	 */
	private void writeLog() throws IOException {
		FileWriter fw;
		fw = new FileWriter(logFile, true);
		fw.write(PluginConstants.LINE_SEPARATOR);
		fw.write(logEntry);
		fw.flush();
		fw.close();
	}

	/**
	 * It is checked if the log file exists. If it exists <code>writeLog()</code>
	 * is called. If the file does not exist a new one is created.<br />
	 * Exceptions are handled by this method.
	 */
	public void writeToLogFile() {
		if (logFile.exists()) {
			try {
				writeLog();
			}
			catch (final NumberFormatException e) {
				IJ.showMessage(PluginMessages.getString("Titel.Warning"), PluginMessages
					.getString("MapSetup.formatError") + "<p>" + e.getMessage() +
					"</p></html>");

			}
			catch (final IOException e) {
				IJ.showMessage(PluginMessages.getString("Titel.Warning"), PluginMessages
					.getString("MapSetup.readError") + "<p>" + e.getMessage() +
					"</p></html>");
			}
		}
		else {
			createFile();
		}
	}
}
