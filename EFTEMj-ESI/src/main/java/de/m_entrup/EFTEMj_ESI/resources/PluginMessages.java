
package de.m_entrup.EFTEMj_ESI.resources;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PluginMessages {

	private static final String BUNDLE_NAME = "resources.Elemental-Map"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
		.getBundle(BUNDLE_NAME);

	public static String getString(final String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (final MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
