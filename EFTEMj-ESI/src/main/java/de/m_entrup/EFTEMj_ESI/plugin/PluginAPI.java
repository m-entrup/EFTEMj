
package de.m_entrup.EFTEMj_ESI.plugin;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.gui.MainMenu;
import ij.ImagePlus;

/**
 * The {@link PluginAPI} is a class using the singleton pattern. There is only
 * one instance of it. You can access it by using the method
 * <code>getInstance()</code>.<br>
 * {@link PluginAPI} is used to get access to the {@link MainMenu} and the
 * {@link DatasetAPI}.
 */
public class PluginAPI {

	/**
	 * There is only one instance of {@link PluginAPI}. That is why the Singleton
	 * pattern is used.
	 */
	private static final PluginAPI INSTANCE = new PluginAPI();

	/**
	 * Instead of using the constructor you can get an instance of
	 * {@link PluginAPI} by using this method.
	 *
	 * @return The only instance of {@link PluginAPI}
	 */
	public static PluginAPI getInstance() {
		return INSTANCE;
	}

	/**
	 * Through the {@link DatasetAPI} all calculations get access to the different
	 * data classes. If the image is changed the current instance if
	 * {@link DatasetAPI} is replaced by a new one and all collected data is lost.
	 */
	private DatasetAPI datasetAPI;
	/**
	 * The only instance of {@link MainMenu}.
	 */
	private MainMenu mainMenu;

	/**
	 * A private default constructor.
	 */
	private PluginAPI() {
		super();
	}

	/**
	 * This method is used to disable the buttons at the {@link MainMenu}.
	 *
	 * @param disableStackSelection If a process is started this value should be
	 *          <code>true</code> . <code>false</code> is used if no image is
	 *          selected and starting a process would make no sense.
	 */
	public void disableMainMenuButtons(final boolean disableStackSelection) {
		mainMenu.disableMainMenuButtons(disableStackSelection);
	}

	/**
	 * This method enables all buttons at the {@link MainMenu}.
	 */
	public void enableMainMenuButtons() {
		mainMenu.enableMainMenuButtons();
	}

	/**
	 * @return The {@link DatasetAPI} instance that gives access to all datasets
	 *         that are used for the calculations. If no image is selected, then
	 *         the return value is <code>null</code>.
	 */
	public DatasetAPI getDatasetAPI() {
		return datasetAPI;
	}

	/**
	 * @return The only instance of {@link MainMenu}.
	 */
	public MainMenu getMainMenu() {
		return mainMenu;
	}

	/**
	 * If an {@link ImagePlus} object is selected a instance of {@link DatasetAPI}
	 * is created.
	 *
	 * @param imagePlus A {@link ImagePlus} object that is selected by the plugin.
	 *          The parameter can be <code>null</code>. This results in setting
	 *          datasetAPI to <code>null</code>.
	 */
	public void initDatasetAPI(final ImagePlus imagePlus) {
		if (imagePlus == null) {
			datasetAPI = null;
			return;
		}
		datasetAPI = new DatasetAPI(imagePlus);
		mainMenu.updateMainMenu();
	}

	/**
	 * A new instance of {@link MainMenu} is created. This is the only instance of
	 * {@link MainMenu}.
	 */
	public void initMainMenu() {
		mainMenu = new MainMenu();
	}

	/**
	 * The progress bar at the {@link MainMenu} is set to 0.
	 */
	public void resetProgressBar() {
		mainMenu.setProgress(0);
	}

	/**
	 * The method <code>updateMainMenu</code> at the instance of {@link MainMenu}
	 * is called.
	 */
	public void updateMainMenu() {
		mainMenu.updateMainMenu();
	}

	/**
	 * The progress bar at the {@link MainMenu} is set to a new value.
	 *
	 * @param progress A new value between 0 and 100.
	 */
	public void updateProgrssbar(final int progress) {
		mainMenu.setProgress(progress);
	}
}
