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
	 * Creates an {@link File}-object. The used path is<br>
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
	 * is called. If the file does not exist a new one is created.<br>
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
