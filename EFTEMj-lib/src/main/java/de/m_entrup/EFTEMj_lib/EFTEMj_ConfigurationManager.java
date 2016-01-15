
package de.m_entrup.EFTEMj_lib;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * Testing Apache Commons Configuration
 *
 * @author Michael Entrup b. Epping
 */
public class EFTEMj_ConfigurationManager {

	private static EFTEMj_Configuration config = null;
	public static String userConfigPath = System.getProperty("user.home") + "/" +
		"EFTEMj_config.xml";
	private static String defaultConfigName = "default.properties";

	public static void main(final String[] args) {
		try {
			final CompositeConfiguration config = getConfiguration();
			System.out.println(config.getString("colors.background"));
			final int test = config.getInt("EFTEMj.test");
			System.out.println(test);
			config.setProperty("EFTEMj.test", 3);
			config.setProperty("Konstanten.PI", Math.PI);
			saveConfiguration();
		}
		catch (final ConfigurationException e) {
			// TODO Auto-generated catch block
			System.out.println("Es ist ein Fehler aufgetreten:\n" + e.toString());
		}
	}

	public static EFTEMj_Configuration getConfiguration()
		throws ConfigurationException
	{
		if (config != null) return config;
		config = new EFTEMj_Configuration();
		config.addConfiguration(getUserConfiguration());
		config.addConfiguration(new PropertiesConfiguration(defaultConfigName));
		return config;
	}

	private static XMLConfiguration getUserConfiguration()
		throws ConfigurationException
	{
		final File file = new File(userConfigPath);
		if (!file.exists()) createXML(file);
		final XMLConfiguration userConfig = new XMLConfiguration();
		userConfig.load(new File(EFTEMj_ConfigurationManager.userConfigPath));
		return userConfig;
	}

	public static void saveConfiguration() throws ConfigurationException {
		if (config == null) return;
		final Configuration changes = config.getInMemoryConfiguration();
		final XMLConfiguration userConfig = getUserConfiguration();
		for (final Iterator<String> i = changes.getKeys(); i.hasNext();) {
			final String key = i.next();
			final Object value = changes.getProperty(key);
			userConfig.setProperty(key, value);
		}
		userConfig.save(userConfigPath);
	}

	private static void createXML(final File xml) throws ConfigurationException {
		final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder(
			xml);
		builder.save();
	}
}
