
package de.m_entrup.EFTEMj_ESI.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;

import de.m_entrup.EFTEMj_ESI.dataset.DatasetAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import de.m_entrup.EFTEMj_ESI.threading.ThreadInterface;
import de.m_entrup.EFTEMj_ESI.tools.ImagePlusTool;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ProgressBar;
import ij.plugin.ImagesToStack;

/**
 * This is the main menu of the Elemental-Map-PlugIn. It extends {@link Frame}.
 * It uses the Singleton pattern.
 */
@SuppressWarnings("serial")
public class MainMenu extends Frame {

	/**
	 * The {@link GridBagPanel} contains the the functions provided by the plugin.
	 * For each function there is a {@link Button} to start it and a {@link Label}
	 * for a short description. The class extends from {@link Label}.
	 */
	private class GridBagPanel extends Panel {

		/**
		 * The {@link Button}s at this {@link Vector} will be disabled if no image
		 * is selected.
		 */
		private final Vector<Button> disabledButtons;
		/**
		 * This is the Layout of the {@link GridBagPanel}.
		 */
		private final GridBagLayout gLayout;

		/**
		 * Creates an instance of {@link GridBagPanel} using the
		 * {@link GridBagLayout}.
		 */
		public GridBagPanel() {
			super();
			gLayout = new GridBagLayout();
			this.setLayout(gLayout);
			disabledButtons = new Vector<Button>();
			this.fillPane();
		}

		/**
		 * Adds a {@link Button} to the GridBagPane. It's width is 1 columns.
		 *
		 * @param button Text of the {@link Button}
		 * @param pos Position (row) at the GridBagPane. Counting starts at 0.
		 * @param disabled This parameter determines if the {@link Button} is
		 *          disabled when no image is selected.
		 */
		private void addButton(final Button button, final int pos,
			final boolean disabled)
		{
			if (disabled == true) {
				disabledButtons.add(button);
			}
			button.addActionListener(mainMenuListener);
			addToGridBagPane(button, 1, pos, 1, 1);
		}

		/**
		 * Adds a {@link Label} to the {@link GridBagPanel}. It's width is 2
		 * columns.
		 *
		 * @param label Text of the {@link Label}
		 * @param pos Position (row) at the {@link GridBagPanel}. Counting starts at
		 *          0.
		 */
		private void addLabel(final String label, final int pos) {
			final Label awtLabel = new Label(label);
			addToGridBagPane(awtLabel, 0, pos, 1, 1);
		}

		/**
		 * This method combines all setting for adding a new {@link Component} to
		 * the {@link GridBagPanel}.
		 *
		 * @param comp The {@link Component} you want to add
		 * @param x Column, starts at 0
		 * @param y Row, starts at 0
		 * @param width Width in rows
		 * @param height Height in columns
		 */
		private void addToGridBagPane(final Component comp, final int x,
			final int y, final int width, final int height)
		{
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = x;
			gbc.gridy = y;
			gbc.gridwidth = width;
			gbc.gridheight = height;
			gbc.ipadx = 10;
			gbc.insets = new Insets(3, 3, 3, 3);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			((GridBagLayout) this.getLayout()).setConstraints(comp, gbc);
			this.add(comp);
		}

		/**
		 * For each line at the {@link GridBagPanel} a {@link Label} and a
		 * {@link Button} are created.
		 */
		private void fillPane() {
			int pos = 0;
			// Create a stack
			addLabel(PluginMessages.getString("Label.CreateStack"), pos);
			final Button createStack = new Button(PluginMessages.getString(
				"Button.CreateStack"));
			addButton(createStack, pos, false);
			pos++;
			// Open the StackSetup
			addLabel(PluginMessages.getString("Label.StackSetup"), pos);
			final Button stackSetup = new Button(PluginMessages.getString(
				"Button.StackSetup"));
			addButton(stackSetup, pos, true);
			pos++;
			// Open the drift correction
			addLabel(PluginMessages.getString("Label.DriftSetup"), pos);
			final Button driftCorrection = new Button(PluginMessages.getString(
				"Button.DriftSetup"));
			addButton(driftCorrection, pos, true);
			pos++;
			// Open the Elemental-Map tool
			addLabel(PluginMessages.getString("Label.MapSetup"), pos);
			final Button elementalMap = new Button(PluginMessages.getString(
				"Button.MapSetup"));
			addButton(elementalMap, pos, true);
		}
	} // END GridBagPane

	/**
	 * This {@link ActionListener} handles any {@link ActionEvent} generated by
	 * pressing a {@link Button} at {@link MainMenu}.
	 */
	private class MainMenuListener implements ActionListener {

		/**
		 * Creates an instance of {@link MainMenuListener} using the constructor of
		 * {@link ActionListener}.
		 */
		public MainMenuListener() {
			super();
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (e.getActionCommand() == PluginMessages.getString(
				"Button.ChangeStack") | e.getActionCommand() == PluginMessages
					.getString("Button.SelectStack"))
			{
				changeStack();
				return;
			}
			if (e.getActionCommand() == PluginMessages.getString(
				"Button.CreateStack"))
			{
				createStack();
				return;
			}
			if (e.getActionCommand() == PluginMessages.getString(
				"Button.StackSetup"))
			{
				openStackSetupDialog();
				return;
			}
			if (e.getActionCommand() == PluginMessages.getString(
				"Button.DriftSetup"))
			{
				openDriftSetupDialog();
				return;
			}
			if (e.getActionCommand() == PluginMessages.getString("Button.MapSetup")) {
				openMapSetupDialog();
				return;
			}
		}

		/**
		 * A dialog is opened to choose the {@link ImagePlus} that is used by the
		 * plugIn. If no {@link ImagePlus} is opened an message dialog is displayed.
		 * If only 1 {@link ImagePlus} is opened this {@link ImagePlus} is assigned.
		 */
		private void changeStack() {
			if (WindowManager.getCurrentImage() == null) {
				pluginAPI.initDatasetAPI(null);
				stackSelectionPanel.updateStackSelection();
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.NoImage"));
			}
			else if (WindowManager.getImageCount() == 1) {
				pluginAPI.initDatasetAPI(WindowManager.getCurrentImage());
				stackSelectionPanel.updateStackSelection();
			}
			else {
				new ChangeStackDialog();
			}
		}

		/**
		 * All open {@link ImagePlus} are combined to one {@link ImagePlus}. If less
		 * than 2 {@link ImagePlus} are opened a message dialog is displayed.
		 */
		private void createStack() {
			if (WindowManager.getImageCount() < 2) {
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.InsufficientImages"));
			}
			else {
				// This is a build in function of IJ
				// new since v0.6:
				// I can't catch errors of ImageToStack, that is why i check
				// if there is only 1 image left. For example, if there are 2
				// stacks ImageToStack won't work.
				new ImagesToStack().run(null);
				if (WindowManager.getImageCount() == 1) {
					final ImagePlus imp = WindowManager.getCurrentImage();
					// check if a stack has been created (eg. the user can
					// select to cancel the creation)
					if (imp.getStackSize() > 1) {
						pluginAPI.initDatasetAPI(imp);
						pluginAPI.getDatasetAPI().getImagePlus().changes = true;
						stackSelectionPanel.updateStackSelection();
						LogWriter.writeLog("The stack \"" + imp.getShortTitle() +
							"\" has been created.");
						ImagePlusTool.saveImagePlus(pluginAPI.getDatasetAPI()
							.getImagePlus(), true);
					}
				}
			}
		}

		/**
		 * A {@link DriftSetupDialog} is displayed. If no {@link ImageStack} is
		 * opened or the stack size is less than 2 a message Dialog is displayed.
		 */
		private void openDriftSetupDialog() {
			if (PluginAPI.getInstance().getDatasetAPI() == null) {
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.NoImage"));
			}
			else {
				if (PluginAPI.getInstance().getDatasetAPI().getStackSize() < 2) {
					LogWriter.showWarningAndWriteLog(PluginMessages.getString(
						"Error.NoStack"));
				}
				else if (PluginAPI.getInstance().getDatasetAPI().getImagePlus()
					.getRoi() == null)
				{
					LogWriter.showWarningAndWriteLog(PluginMessages.getString(
						"Error.NoROI"));
				}
				else {
					MainMenu.this.disableMainMenuButtons(true);
					new DriftSetupDialog();
				}
			}
		}

		/**
		 * A {@link MapSetupDialog} is displayed. If no {@link ImageStack} is opened
		 * or the stack size is less than 3 a message Dialog is displayed.
		 */
		private void openMapSetupDialog() {
			if (PluginAPI.getInstance().getDatasetAPI() == null) {
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.NoImage"));
			}
			else if (PluginAPI.getInstance().getDatasetAPI().getStackSize() < 2) {
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.NoStack"));
			}
			else if (PluginAPI.getInstance().getDatasetAPI().getStackSize() < 3) {
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.StackToSmall"));
			}
			else {
				MainMenu.this.disableMainMenuButtons(true);
				new MapSetupDialog();
			}
		}

		/**
		 * A {@link StackSetupDialog} is displayed. If no {@link ImageStack} is
		 * opened an message dialog is displayed.
		 */
		private void openStackSetupDialog() {
			if (PluginAPI.getInstance().getDatasetAPI() == null) {
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.NoImage"));
			}
			else if (PluginAPI.getInstance().getDatasetAPI().getStackSize() < 2) {
				LogWriter.showWarningAndWriteLog(PluginMessages.getString(
					"Error.NoStack"));
			}
			else {
				new StackSetupDialog();
			}
		}
	} // END MainMenuListener

	/**
	 * The {@link StackSelectionPanel} contains a {@link Label} that shows the
	 * selected stack. A {@link Button} is used to trigger the stack selection.
	 */
	private class StackSelectionPanel extends Panel {

		/**
		 * Border gap of the {@link FlowLayout} used by the NothPane.
		 */
		private static final int NORTH_PANE_GAP = 10;
		/**
		 * This field is used to access the {@link Button}. It will be disabled
		 * during a calculation.
		 */
		private final Button changeStackButton;
		/**
		 * This label show the selected stack. The text of this label will be
		 * changed if necessary.
		 */
		private final Label usedStackLabel;

		/**
		 * Creates an instance of the {@link StackSelectionPanel}. It uses the
		 * {@link FlowLayout}.
		 */
		public StackSelectionPanel() {
			super();
			final FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING);
			flowLayout.setHgap(NORTH_PANE_GAP);
			this.setLayout(flowLayout);
			usedStackLabel = new Label(PluginMessages.getString(
				"Label.NoStackSelected"));
			changeStackButton = new Button(PluginMessages.getString(
				"Button.SelectStack"));
			changeStackButton.addActionListener(mainMenuListener);
			this.add(changeStackButton);
			this.add(usedStackLabel);
		}

		/**
		 * The changeStackButton is disabled.
		 */
		public void disableStackSelection() {
			changeStackButton.setEnabled(false);
		}

		/**
		 * The changeStackButton is enabled.
		 */
		public void enableStackSelection() {
			changeStackButton.setEnabled(true);
		}

		/**
		 * The value of the label is changed to the short title of the selected
		 * {@link ImagePlus}.
		 */
		public void updateStackSelection() {
			if (PluginAPI.getInstance().getDatasetAPI() == null) {
				usedStackLabel.setText(PluginMessages.getString(
					"Label.NoStackSelected"));
				changeStackButton.setLabel(PluginMessages.getString(
					"Button.SelectStack"));
				MainMenu.this.disableMainMenuButtons(false);
			}
			else {
				usedStackLabel.setText(PluginAPI.getInstance().getDatasetAPI()
					.getImagePlus().getTitle());
				changeStackButton.setLabel(PluginMessages.getString(
					"Button.ChangeStack"));
				MainMenu.this.enableMainMenuButtons();
			}
		}
	} // END StackSelectionPane

	/**
	 * This {@link Panel} contains the main components of the {@link JFrame}.
	 */
	private final Panel centerPanel;
	/**
	 * The CENTER of contentPanel contains this {@link GridBagPanel}, it extends
	 * from {@link Panel}.
	 */
	private GridBagPanel gridBagPane;
	/**
	 * An instance of the inner ActionListener class.
	 */
	private final MainMenuListener mainMenuListener;
	/**
	 * This {@link DescriptionPanel} shows information about the elements of the
	 * {@link MapResultPanel}. It is displayed togester with the
	 * {@link MapResultPanel} and will be removed at the same time. This panel
	 * replaces the {@link StackSelectionPanel}.
	 */
	private DescriptionPanel mapResultDescription;
	/**
	 * The {@link MapResultPanel} will replace the {@link GridBagPanel} at the
	 * CENTER of the contentPanel to access the results of the elemental-map
	 * calculation.
	 */
	private MapResultPanel mapResultPanel;
	/**
	 * This {@link Panel} contains a description.
	 */
	private final Panel northPanel;
	/**
	 * The {@link PluginAPI} gives access to the {@link DatasetAPI}.
	 */
	private final PluginAPI pluginAPI = PluginAPI.getInstance();
	/**
	 * This {@link Panel} contains the OK- and the Cancel-button.
	 */
	private final Panel southPanel;
	/**
	 * The NORTH of the contentPanel contains this {@link StackSelectionPanel}, it
	 * extends from {@link Label}.
	 */
	private StackSelectionPanel stackSelectionPanel;

	/**
	 * Creates a new instance of {@link MainMenu} with all components and sets it
	 * visible.
	 */
	public MainMenu() {
		super(PluginMessages.getString("Title.MainMenu") + " (Version " +
			PluginConstants.VERSION + ")");
		mainMenuListener = new MainMenuListener();
		this.setLayout(new BorderLayout(PluginConstants.LAYOUT__BORDER_LAYOUT_GAP,
			PluginConstants.LAYOUT__BORDER_LAYOUT_GAP));
		this.setBackground(ImageJ.backgroundColor);
		northPanel = new Panel();
		// I use a GridLayout because the inserted components use the complete
		// available space.
		northPanel.setLayout(new GridLayout());
		this.add(northPanel, BorderLayout.NORTH);

		centerPanel = new Panel();
		this.add(centerPanel, BorderLayout.CENTER);

		southPanel = new Panel();
		// I use a GridLayout because the inserted components use the complete
		// available space.
		southPanel.setLayout(new GridLayout());
		this.add(southPanel, BorderLayout.SOUTH);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				MainMenu.this.dispose();
			}
		});
		this.addComponents();
		this.disableMainMenuButtons(false);
		this.pack();
		this.setResizable(false);
		this.setVisible(true);
	} // END Constructor

	/**
	 * This method fills the {@link MainMenu} with UI elements and sets the
	 * layout.
	 */
	private void addComponents() {
		// Different inner classes with components of the MainMenu
		gridBagPane = new GridBagPanel();
		centerPanel.add(gridBagPane);
		stackSelectionPanel = new StackSelectionPanel();
		northPanel.add(stackSelectionPanel);
	}

	/**
	 * Replaces the instances of the {@link DescriptionPanel} and
	 * {@link MapResultPanel} by the old instances of the
	 * {@link StackSelectionPanel} and the {@link GridBagPanel}. The instances of
	 * {@link DescriptionPanel} and {@link MapResultPanel} are deleted by
	 * assigning <code>null</code> to it.
	 */
	public void closeMapResultPanel() {
		if (mapResultPanel != null) {
			this.setTitle(PluginMessages.getString("Title.MainMenu"));
			northPanel.remove(mapResultDescription);
			northPanel.add(stackSelectionPanel, BorderLayout.NORTH);
			mapResultDescription = null;
			centerPanel.remove(mapResultPanel);
			centerPanel.add(gridBagPane, BorderLayout.CENTER);
			mapResultPanel = null;
			this.pack();
			PluginAPI.getInstance().getDatasetAPI().deleteMapDataset();
			ThreadInterface.getInstance().deleteThreadChecker();
			this.enableMainMenuButtons();
		}
	}

	/**
	 * Disables all {@link Button} at the {@link MainMenu}.
	 *
	 * @param disableStackSelection If false, all {@link Button}s that allow a
	 *          stack selection will stay enabled.
	 */
	public void disableMainMenuButtons(final boolean disableStackSelection) {
		if (disableStackSelection == true) {
			final Component[] components = gridBagPane.getComponents();
			for (final Component element : components) {
				if (element.getClass() == Button.class) {
					element.setEnabled(false);
				}
			}
			stackSelectionPanel.disableStackSelection();
		}
		else {
			for (final Button element : gridBagPane.disabledButtons) {
				element.setEnabled(false);
			}
		}
	}

	/**
	 * Enables all {@link Button} at the {@link MainMenu}.
	 */
	public void enableMainMenuButtons() {
		final Component[] components = gridBagPane.getComponents();
		for (final Component element : components) {
			if (element.getClass() == Button.class) {
				element.setEnabled(true);
			}
		}
		if (pluginAPI.getDatasetAPI() == null) {
			disableMainMenuButtons(false);
		}
		stackSelectionPanel.enableStackSelection();
	}

	/**
	 * Enables a specific {@link Button} at the instance of the
	 * {@link MapResultPanel}.
	 *
	 * @param key The key that is used to map the {@link Button} at the
	 *          {@link Hashtable} buttonTable in {@link MapResultPanel}.
	 */
	public void enableMapResultButton(final String key) {
		if (key.startsWith("key_show")) {
			mapResultPanel.enableButton(key);
			final String key_result = key.substring(8);
			mapResultPanel.enableButton("key_export" + key_result);
		}
		else {
			mapResultPanel.enableButton(key);
		}
	}

	/**
	 * Updates the {@link ProgressBar} at the {@link MainMenu}.
	 *
	 * @param value A new value between 0 and 100
	 */
	public void setProgress(final int value) {
		IJ.showProgress(value, 100);
	}

	/**
	 * Replaces the instance of the {@link GridBagPanel} by a new instance of the
	 * {@link MapResultPanel} and replaces the instance of
	 * {@link StackSelectionPanel} by a new instance of {@link DescriptionPanel} .
	 */
	public void showMapResultPanel() {
		this.setTitle(PluginMessages.getString("Titel.MapResultDialog"));
		mapResultDescription = new DescriptionPanel(PluginMessages.getString(
			"Label.MapResultInfo"));
		mapResultDescription.setDetailedDescription(String.format(PluginMessages
			.getString("Label.MapResultDetailedInfo"), PluginConstants.ERROR__NON,
			PluginConstants.ERROR__SIGNAL_LESS_THAN_ZERO,
			PluginConstants.ERROR__R_LESS_THAN_LIMIT,
			PluginConstants.ERROR__A_NOT_POSSIBLE_TO_CALCULATE,
			PluginConstants.ERROR__CONVERGENCE, PluginConstants.ERROR__NAN));
		mapResultPanel = new MapResultPanel();
		northPanel.remove(stackSelectionPanel);
		northPanel.add(mapResultDescription);
		centerPanel.remove(gridBagPane);
		centerPanel.add(mapResultPanel);
		this.pack();
	}

	/**
	 * Updates the components of the {@link MainMenu}:<br />
	 * 1. Update of the {@link StackSelectionPanel}
	 */
	public void updateMainMenu() {
		stackSelectionPanel.updateStackSelection();
	}
}
