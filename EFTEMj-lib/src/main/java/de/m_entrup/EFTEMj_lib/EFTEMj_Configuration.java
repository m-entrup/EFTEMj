
package de.m_entrup.EFTEMj_lib;

import java.util.Collection;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import ij.IJ;

public class EFTEMj_Configuration extends CompositeConfiguration {

	public EFTEMj_Configuration() {
		// TODO Auto-generated constructor stub
	}

	public EFTEMj_Configuration(final Configuration inMemoryConfiguration) {
		super(inMemoryConfiguration);
		// TODO Auto-generated constructor stub
	}

	public EFTEMj_Configuration(
		final Collection<? extends Configuration> configurations)
	{
		super(configurations);
		// TODO Auto-generated constructor stub
	}

	public EFTEMj_Configuration(final Configuration inMemoryConfiguration,
		final Collection<? extends Configuration> configurations)
	{
		super(inMemoryConfiguration, configurations);
		// TODO Auto-generated constructor stub
	}

	public void save() {
		try {
			EFTEMj_ConfigurationManager.saveConfiguration();
		}
		catch (final ConfigurationException e) {
			IJ.error("Failed to save config.", e.toString());
		}
	}

}
