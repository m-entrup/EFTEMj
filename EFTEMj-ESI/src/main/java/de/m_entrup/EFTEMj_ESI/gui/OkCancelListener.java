
package de.m_entrup.EFTEMj_ESI.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;

/**
 * The {@link OkCancelListener} is a small abstract class that implements the
 * method <code>actionPerformed(ActionEvent e)</code> and handles buttons with
 * the text <code>PluginMessages.getString("Button.OK")</code> and
 * <code>PluginMessages.getString("Button.Cancel")</code>.<br />
 * Additionally it implements {@link WindowListener} to handle the closing of
 * the window. If the window is closed the Cancel-operation is called. Use the
 * following methods:<br />
 * <code>this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 * 		this.addWindowListener(listener);</code><br />
 * <code>listener</code> has to extend {@link OkCancelListener}.
 */
public abstract class OkCancelListener implements ActionListener,
	WindowListener
{

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand() == PluginMessages.getString("Button.OK")) {
			okOperation();
		}
		if (e.getActionCommand() == PluginMessages.getString("Button.Cancel")) {
			cancelOperation();
		}
	}

	/**
	 * The method that is called if the Cancel-button is pressed.
	 */
	protected abstract void cancelOperation();

	/**
	 * The method that is called if the OK-button is pressed.
	 */
	protected abstract void okOperation();

	@Override
	public void windowActivated(final WindowEvent e) {

	}

	@Override
	public void windowClosed(final WindowEvent e) {

	}

	@Override
	public void windowClosing(final WindowEvent e) {
		cancelOperation();
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {

	}

	@Override
	public void windowDeiconified(final WindowEvent e) {

	}

	@Override
	public void windowIconified(final WindowEvent e) {

	}

	@Override
	public void windowOpened(final WindowEvent e) {

	}

}
