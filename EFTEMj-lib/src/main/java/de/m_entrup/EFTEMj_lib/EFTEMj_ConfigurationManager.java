/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2016, Michael Entrup b. Epping
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package de.m_entrup.EFTEMj_lib;

import java.io.File;
import java.io.InputStream;
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
	public static String userConfigPath = System.getProperty("user.home") + "/" + "EFTEMj_config.xml";
	private static String defaultConfigName = "/EFTEMj-lib.properties";

	public static void main(final String[] args) {
		try {
			final CompositeConfiguration config = getConfiguration();
			System.out.println(config.getString("colors.background"));
			final int test = config.getInt("EFTEMj.test");
			System.out.println(test);
			config.setProperty("EFTEMj.test", 3);
			config.setProperty("Konstanten.PI", Math.PI);
			saveConfiguration();
		} catch (final ConfigurationException e) {
			// TODO Auto-generated catch block
			System.out.println("Es ist ein Fehler aufgetreten:\n" + e.toString());
		}
	}

	public static EFTEMj_Configuration getConfiguration() throws ConfigurationException {
		if (config != null)
			return config;
		config = new EFTEMj_Configuration();
		config.addConfiguration(getUserConfiguration());
		PropertiesConfiguration defaultConfig = new PropertiesConfiguration();
		InputStream is = config.getClass().getResourceAsStream(defaultConfigName);
		defaultConfig.load(is);
		/*
		 * Iterator<String> iter = defaultConfig.getKeys(); while
		 * (iter.hasNext()) { IJ.log(iter.next()); }
		 */
		config.addConfiguration(defaultConfig);
		return config;
	}

	private static XMLConfiguration getUserConfiguration() throws ConfigurationException {
		final File file = new File(userConfigPath);
		if (!file.exists())
			createXML(file);
		final XMLConfiguration userConfig = new XMLConfiguration();
		userConfig.load(new File(EFTEMj_ConfigurationManager.userConfigPath));
		return userConfig;
	}

	public static void saveConfiguration() throws ConfigurationException {
		if (config == null)
			return;
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
		final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder(xml);
		builder.save();
	}
}
