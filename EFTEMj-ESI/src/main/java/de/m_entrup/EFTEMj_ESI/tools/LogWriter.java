
package de.m_entrup.EFTEMj_ESI.tools;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import ij.IJ;

/**
 * The {@link LogWriter} will handle two kind of logs.<br />
 * The first one will be displayed at the gui. It will be a formated
 * {@link String} on which new logs are appended. This string is cleared and
 * date and time are added if a new calculation is started. The contend of this
 * log is related to the calculation.<br />
 * the second log is written into a file. It contains all the information that
 * is displayed at the gui log. Additionally each warning message is logged to
 * simplify the search for errors and problems. Some other functions will write
 * logs, too.
 */
public class LogWriter {

	public static final int DRIFT = 0;
	public static final int MAP = 1;
	private static boolean newSession = false;

	/**
	 * This {@link Vector} is used to store all logs that are made during a
	 * process. If a new process starts this {@link Vector} is cleared.
	 */
	private static Vector<String> processLog = new Vector<String>();
	private static Vector<String> statistics = new Vector<String>();

	/**
	 * This method creates a new processLog {@link Vector}. Only logs at the GUI
	 * are cleared. This does not influence the logfile.
	 */
	public static void clearProcessLog() {
		processLog = new Vector<String>();
	}

	public static void clearStatistics() {
		statistics = new Vector<String>();
	}

	/**
	 * @return the {@link Vector} that contains all process-logs that were stored
	 *         after the last call of <code>clearProcessLog()</code>.
	 */
	public static Vector<String> getProcessLog() {
		return processLog;
	}

	/**
	 * This method is called when starting the plugin with IJ. It will result in
	 * an empty line before the first entry of the log file. This is to split up
	 * entries of different sessions.
	 */
	public static void newSession() {
		newSession = true;
	}

	/**
	 * This method writes a {@link String} to the logfile of the plugin.
	 * Additionally the {@link String} is displayed as a MessageDialog.
	 *
	 * @param text A text that is written to the logfile and displayed as a
	 *          MessageDialog.
	 */
	public static void showWarningAndWriteLog(final String text) {
		IJ.showMessage(PluginMessages.getString("Titel.Warning"), text);
		writeLog("Warning: " + text);
	}

	/**
	 * This method writes a {@link String} to the logfile of the plugin.
	 *
	 * @param text A text that is written to the logfile.
	 */
	public static void writeLog(final String text) {
		if (newSession) {
			new LogFileHandler("").writeToLogFile();
			newSession = false;
		}
		DateFormat df;
		df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
			Locale.ENGLISH);
		final String dateAndTime = df.format(new Date());
		new LogFileHandler(dateAndTime + ": " + text).writeToLogFile();

	}

	/**
	 * This method writes a {@link String} to the logfile of the plugin.
	 * Additionally the {@link String} is added to the processLog {@link Vector}
	 * to display the logs at the GUI.
	 *
	 * @param text A text that is written to the logfile and added to the
	 *          processLog {@link Vector}.
	 * @param process The type of process (e.g. {@link LogWriter}.DRIFT)
	 */
	public static void writeProcessLog(final String text, final int process) {
		processLog.add(text);
		switch (process) {
			case DRIFT:
				writeLog("Drift correction: " + text);
				break;
			case MAP:
				writeLog("Elemental mapping: " + text);
				break;
			default:
				break;
		}

	}

	/**
	 * This method writes a {@link String} to the logfile of the plugin.
	 * Additionally the {@link String} is added to the statistics {@link Vector}
	 * to display the statistics at the GUI.
	 *
	 * @param text A text that is written to the logfile and added to the
	 *          statistics {@link Vector}.
	 */
	public static void writeStatistics(final String text) {
		statistics.add(text);
		writeLog("Statistics: " + text);
	}

}
