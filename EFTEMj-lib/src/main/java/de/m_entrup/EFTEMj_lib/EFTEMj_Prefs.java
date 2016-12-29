/**
 * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
 *
 * Copyright (c) 2015, Michael Entrup b. Epping <mail@m-entrup.de>
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

import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import ij.IJ;
import ij.ImageJ;
import ij.Prefs;

/**
 * A class that handles writing and reading of EFTEMj related properties to
 * IJ_Prefs.txt.
 *
 * @author #auMichael Entrup b. Epping
 */
public class EFTEMj_Prefs {

	/**
	 * <code>EFTEMj.</code> Prefix for all EFTEMj related properties in
	 * IJ_Prefs.txt.
	 */
	public static final String PREFS_PREFIX = "EFTEMj.";

	public static void main(final String[] args) {
		/*
		 * start ImageJ
		 */
		final ImageJ ij = new ImageJ();
		System.out.println("Testing with " + ij.getInfo());

		final Properties props = Prefs.getControlPanelProperties();
		final Set<Object> set = props.keySet();
		final String searchWord = "dispersion";
		final ArrayList<String> found = new ArrayList<>();
		for (final Object i : set) {
			final String str = (String) i;
			if (str.contains("EFTEMj")) {
				if (str.contains(searchWord)) {
					found.add(str);
				}
			}
		}
		IJ.showMessage("EFTEMj keys in IJ_Prefs.txt",
				"directory: " + Prefs.getPrefsDir() + "\nSize: " + set.size() + "\nEFTEMj keys: " + found.size());
		for (final String i : found) {
			IJ.log(i + " = " + props.getProperty(i));
		}
	}

	public static ArrayList<String> getAllKeys() {
		final ArrayList<String> keys = new ArrayList<>();
		final Properties props = Prefs.getControlPanelProperties();
		final Set<Object> set = props.keySet();
		for (final Object obj : set) {
			String key = (String) obj;
			key = key.substring(1, key.length());
			keys.add(key);
		}
		return keys;
	}

	public static ArrayList<String> getAllKeys(final String searchWord) {
		final ArrayList<String> keys = getAllKeys();
		final ArrayList<String> found = new ArrayList<>();
		for (final String key : keys) {
			if (key.contains(searchWord)) {
				found.add(key);
			}
		}
		return found;
	}

}
