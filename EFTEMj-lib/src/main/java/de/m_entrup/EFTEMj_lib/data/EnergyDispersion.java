package de.m_entrup.EFTEMj_lib.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

import de.m_entrup.EFTEMj_lib.EFTEMj_Configuration;
import de.m_entrup.EFTEMj_lib.EFTEMj_ConfigurationManager;
import ij.IJ;

public class EnergyDispersion {

	private static String configKeyPrefix = "lib." + EnergyDispersion.class.getSimpleName() + ".";
	public static String specMagKey = configKeyPrefix + "SpecMag";
	private EFTEMj_Configuration config;
	/**
	 * This {@link Hashtable} is used to manage the SpecMag-dispersion pairs.
	 */
	public Hashtable<String, Double> dispersionStorage;

	public EnergyDispersion() {
		try {
			config = EFTEMj_ConfigurationManager.getConfiguration();
			loadDispersionStorage();
		} catch (final ConfigurationException e) {
			IJ.error("Failed to load config.", e.toString());
			return;
		}
	}

	public void save() {
		saveDispersionStorage();
	}

	private void loadDispersionStorage() {
		dispersionStorage = new Hashtable<>();
		final List<Object> specMags = config.getList(specMagKey);
		for (final Object specMagStr : specMags.toArray()) {
			final String[] specMagPair = ((String) specMagStr).split(":");
			final String valueKey = specMagPair[0];
			final double value = new Double(specMagPair[1]);
			dispersionStorage.put(valueKey, value);

		}
	}

	private void saveDispersionStorage() {
		final ArrayList<String> specMagList = new ArrayList<>();
		for (final String key : dispersionStorage.keySet()) {
			System.out.println(key + ":" + dispersionStorage.get(key));
			specMagList.add(key + ":" + dispersionStorage.get(key));
		}
		Collections.sort(specMagList);
		config.setProperty(specMagKey, specMagList);
		config.save();
	}

	public static void main(final String[] args) {
		System.out.println(specMagKey);
		final EnergyDispersion disp = new EnergyDispersion();
		disp.loadDispersionStorage();
		System.out.println(disp.dispersionStorage);
		System.out.println(disp.dispersionStorage.get("150"));
		disp.dispersionStorage.put("315", 0.75);
		System.out.println(disp.dispersionStorage.get("315"));
		disp.saveDispersionStorage();
	}

}
