
package de.m_entrup.EFTEMj_ESI.gui;

import java.awt.Button;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;

import de.m_entrup.EFTEMj_ESI.dataset.DisplayToolMapResult;
import de.m_entrup.EFTEMj_ESI.dataset.ExportToolMapResult;
import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import de.m_entrup.EFTEMj_ESI.tools.LogWriter;

/**
 * The {@link MapResultPanel} extends from {@link Panel} and can be added to the
 * {@link MainMenu}. It contains {@link Button}s that are used to display the
 * result of the elemental-map calculation.
 */
@SuppressWarnings("serial")
public class MapResultPanel extends Panel {

	/**
	 * This {@link ActionListener} handles any {@link ActionEvent} generated by
	 * pressing a {@link Button} at {@link MapResultPanel}.
	 */
	private class MapResultListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			final DisplayToolMapResult displayToolMapResult =
				new DisplayToolMapResult();
			final ExportToolMapResult exportToolMapResult = new ExportToolMapResult();
			if (e.getSource().equals(buttonTable.get("key_showMap"))) {
				displayToolMapResult.showMap();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_exportMap"))) {
				exportToolMapResult.exportMap();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_showSNR"))) {
				final float dqe = Float.valueOf(dqeField.getText());
				if (dqe < 0 | dqe > 1) {
					LogWriter.showWarningAndWriteLog(PluginMessages.getString(
						"Error.WrongDQE"));
					return;
				}
				displayToolMapResult.showSNR(dqe);
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_exportSNR"))) {
				final float dqe = Float.valueOf(dqeField.getText());
				if (dqe < 0 | dqe > 1) {
					LogWriter.showWarningAndWriteLog(PluginMessages.getString(
						"Error.WrongDQE"));
					return;
				}
				exportToolMapResult.exportSNR(dqe);
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_showSigma2"))) {
				displayToolMapResult.showSigma2();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_exportSigma2"))) {
				exportToolMapResult.exportSigma2();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_showBG"))) {
				displayToolMapResult.showBG();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_exportBG"))) {
				exportToolMapResult.exportBG();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_showRelBG"))) {
				displayToolMapResult.showRelBG();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_exportRelBG"))) {
				exportToolMapResult.exportRelBG();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_showCoeffOfDet"))) {
				displayToolMapResult.showCoeffOfDet();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_exportCoeffOfDet"))) {
				exportToolMapResult.exportCoeffOfDet();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_showChi2"))) {
				displayToolMapResult.showChi2();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_exportChi2"))) {
				exportToolMapResult.exportChi2();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_showR"))) {
				displayToolMapResult.showR();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_exportR"))) {
				exportToolMapResult.exportR();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_showA"))) {
				displayToolMapResult.showA();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_exportA"))) {
				exportToolMapResult.exportA();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_showErrorMap"))) {
				displayToolMapResult.showErrorMap();
				return;
			}
			if (e.getSource().equals(buttonTable.get("key_exportErrorMap"))) {
				exportToolMapResult.exportErrorMap();
				return;
			}
			if (e.getActionCommand() == PluginMessages.getString(
				"Button.CloseMapResult"))
			{
				PluginAPI.getInstance().getMainMenu().closeMapResultPanel();
				return;
			}
		}
	} // END MapResultListener

	/**
	 * Each button is assigned to a key. The key is used at the
	 * {@link ActionListener} to find the correct source. Additionally this keys
	 * are used at the method <code>enableButton(key)</code>.
	 */
	private Hashtable<String, Button> buttonTable;
	private JFormattedTextField dqeField;

	/**
	 * This is the Layout used by the contentPane.
	 */
	private final GridBagLayout gridBagLayout;
	/**
	 * An instance of the inner ActionListener class.
	 */
	private final MapResultListener mapResultListener;

	/**
	 * Creates an instance of the {@link MapResultPanel}. It uses the
	 * {@link GridBagLayout}.
	 */
	public MapResultPanel() {
		super();
		mapResultListener = new MapResultListener();
		gridBagLayout = new GridBagLayout();
		this.setLayout(gridBagLayout);
		addComponents();
	}

	/**
	 * The {@link Button} is placed at the second or third row of each column.
	 *
	 * @param key The {@link Button} is linked at the {@link Hashtable}
	 *          buttonTable with the given key.
	 * @param pos The index of the used column.
	 * @param exportButton <code>true</code> if it is an export-{@link Button}
	 *          that is placed at the third row.<br>
	 *          Otherwise it is a show-{@link Button} that is placed at the second
	 *          row.
	 */
	private void addButton(final String key, final int pos,
		final boolean exportButton)
	{
		Button jButton;
		if (exportButton) {
			jButton = new Button(PluginMessages.getString("Button.Export"));
			addToMapResultPanel(jButton, 2, pos, 1, 1);
		}
		else {
			jButton = new Button(PluginMessages.getString("Button.Show"));
			addToMapResultPanel(jButton, 1, pos, 1, 1);
		}
		buttonTable.put(key, jButton);
		jButton.setEnabled(false);
		jButton.addActionListener(mapResultListener);
	}

	/**
	 * The {@link MapResultPanel} is filled with a column for each result of the
	 * elemental-map calculation. A column consists of 3 rows: a {@link Label} and
	 * 2 {@link Button}. The first {@link Button} displays an image, the second
	 * will export the image.
	 */
	private void addComponents() {
		buttonTable = new Hashtable<String, Button>();
		int pos = 0;
		// Title of the image that is used
		addToMapResultPanel(new Label(PluginMessages.getString(
			"Label.SelectedStack") + PluginAPI.getInstance().getDatasetAPI()
				.getImagePlus().getTitle()), 0, pos, 3, 1);
		pos++;
		addLabel(PluginMessages.getString("Label.ShowMap"), pos, 1);
		addButton("key_showMap", pos, false);
		addButton("key_exportMap", pos, true);
		pos++;
		addLabel(PluginMessages.getString("Label.SetDQE"), pos, 2);
		addDQEFiled(pos);
		pos++;
		addLabel(PluginMessages.getString("Label.ShowSNR"), pos, 1);
		addButton("key_showSNR", pos, false);
		addButton("key_exportSNR", pos, true);
		pos++;
		addLabel(PluginMessages.getString("Label.ShowSigma2"), pos, 1);
		addButton("key_showSigma2", pos, false);
		addButton("key_exportSigma2", pos, true);
		pos++;
		addLabel(PluginMessages.getString("Label.ShowBG"), pos, 1);
		addButton("key_showBG", pos, false);
		addButton("key_exportBG", pos, true);
		pos++;
		addLabel(PluginMessages.getString("Label.ShowRelBG"), pos, 1);
		addButton("key_showRelBG", pos, false);
		addButton("key_exportRelBG", pos, true);
		pos++;
		addLabel(PluginMessages.getString("Label.ShowCoeffOfDet"), pos, 1);
		addButton("key_showCoeffOfDet", pos, false);
		addButton("key_exportCoeffOfDet", pos, true);
		pos++;

		addLabel(PluginMessages.getString("Label.ShowChi2"), pos, 1);
		addButton("key_showChi2", pos, false);
		addButton("key_exportChi2", pos, true);
		pos++;
		addLabel(PluginMessages.getString("Label.ShowR"), pos, 1);
		addButton("key_showR", pos, false);
		addButton("key_exportR", pos, true);
		pos++;
		addLabel(PluginMessages.getString("Label.ShowA"), pos, 1);
		addButton("key_showA", pos, false);
		addButton("key_exportA", pos, true);
		pos++;
		addLabel(PluginMessages.getString("Label.ShowErrorMap"), pos, 1);
		addButton("key_showErrorMap", pos, false);
		addButton("key_exportErrorMap", pos, true);
		pos++;
		// Panel spacer = new Panel();
		// spacer.setBackground(Color.LIGHT_GRAY);
		// addToMapResultPanel(spacer, 0, pos, 3, 1);
		// pos++;
		final Button closeButton = new Button(PluginMessages.getString(
			"Button.CloseMapResult"));
		buttonTable.put("key_closeMapResult", closeButton);
		closeButton.setEnabled(false);
		closeButton.addActionListener(mapResultListener);
		final Panel closePanel = new Panel();
		closePanel.add(closeButton);
		addToMapResultPanel(closePanel, 0, pos, 3, 1);
	}

	/**
	 * This method adds a {@link JFormattedTextField} to set the DQE to the
	 * {@link MapResultPanel}. It is placed at the third column.
	 *
	 * @param pos The row where it is placed.
	 */
	private void addDQEFiled(final int pos) {
		final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(
			Locale.ENGLISH);
		df.applyPattern("0.0###");
		dqeField = new JFormattedTextField(df);
		dqeField.setValue(1);
		dqeField.setHorizontalAlignment(SwingConstants.RIGHT);
		addToMapResultPanel(dqeField, 2, pos, 1, 1);
	}

	/**
	 * The {@link Label} is placed at the first row of each column.
	 *
	 * @param label The {@link Label} text.
	 * @param pos The index of the used column.
	 * @param width The width of the label in columns.
	 */
	private void addLabel(final String label, final int pos, final int width) {
		final Label jLabel = new Label(label);
		addToMapResultPanel(jLabel, 0, pos, width, 1);
	}

	/**
	 * This method collects all setting for adding a new {@link Component} to the
	 * {@link MapResultPanel}.
	 *
	 * @param comp The {@link Component} you want to add.
	 * @param x Column, starts at 0
	 * @param y Row, starts at 0
	 * @param width Width in rows
	 * @param height Height in columns
	 */
	private void addToMapResultPanel(final Component comp, final int x,
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
	 * A single {@link Button} that is linked at the {@link Hashtable} buttonTable
	 * can be enabled.
	 *
	 * @param key The key to access the button.
	 */
	public void enableButton(final String key) {
		final Button button = buttonTable.get(key);
		button.setEnabled(true);
	}
}