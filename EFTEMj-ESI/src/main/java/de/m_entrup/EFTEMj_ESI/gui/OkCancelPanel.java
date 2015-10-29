
package de.m_entrup.EFTEMj_ESI.gui;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Panel;

import de.m_entrup.EFTEMj_ESI.plugin.PluginConstants;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;

/**
 * This class extends from {@link Panel}. It uses the {@link FlowLayout} to
 * arrange a OK- and a Cancel-button.
 */
@SuppressWarnings("serial")
public class OkCancelPanel extends Panel {

	/**
	 * An instance of {@link OkCancelPanel} is created, that uses a
	 * {@link OkCancelListener} to react on pressing the OK- or the Cancel-button.
	 *
	 * @param actionListener Your implementation of the abstract class
	 *          {@link OkCancelListener}.
	 */
	public OkCancelPanel(final OkCancelListener actionListener) {
		this.setLayout(new FlowLayout(FlowLayout.CENTER,
			PluginConstants.LAYOUT__GAP, PluginConstants.LAYOUT__GAP));
		final Button okButton = new Button(PluginMessages.getString("Button.OK"));
		okButton.addActionListener(actionListener);
		this.add(okButton);
		final Button cancelButton = new Button(PluginMessages.getString(
			"Button.Cancel"));
		cancelButton.addActionListener(actionListener);
		this.add(cancelButton);
	}
}
