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

package de.m_entrup.EFTEMj_SR_EELS.shared;

import de.m_entrup.EFTEMj_lib.EFTEMj_Prefs;
import ij.Prefs;

/**
 * This enum generates all {@link Prefs} keys that are used by
 * <code>SR_EELS_</code> classes.<br>
 * If you select an item of this enum, use <code>getValue()</code> to get the
 * corresponding {@link Prefs} key as a {@link String}.
 *
 * @author Michael Entrup b. Epping
 */
public enum SR_EELS_PrefsKeys {
	specMagValues("specMagValues"), specMagIndex("specMagIndex"), binningIndex("binningIndex"), binningUser(
			"binningUser"), offsetIndex("offsetIndex"), offsetLoss("offsetLoss"), offsetAbsolute(
					"offsetAbsolute"), dispersionEloss("dispersionEloss."), dispersionSettings(
							"dispersionSettings."), none(""), databasePath(
									"databasePath"), fileTypesToImport("fileTypesToImport"), plotsAsTif("plotsAsTif");

	/**
	 * <code>EFTEMj.PREFS_PREFIX + "SR-EELS.".</code>
	 */
	protected static final String PREFS_PREFIX = EFTEMj_Prefs.PREFS_PREFIX + "SR-EELS.";

	private String value;

	SR_EELS_PrefsKeys(final String value) {
		this.value = value;
	}

	/**
	 * @return the full key that is used to access the property with
	 *         {@link Prefs} .
	 */
	public String getValue() {
		switch (this) {
		/*
		 * Begin section - dispersionSettings All parameters get an additional
		 * prefix.
		 */
		case specMagIndex:
		case binningIndex:
		case binningUser:
		case offsetIndex:
		case offsetLoss:
		case offsetAbsolute:
			return PREFS_PREFIX + dispersionSettings.getValue() + value;
		/*
		 * End section - dispersionSettings
		 */
		/*
		 * Begin section - default All parameters that are not listed get the
		 * default EFTEMj-SR-EELS prefix.
		 */
		default:
			return PREFS_PREFIX + value;
		/*
		 * End section - default
		 */
		}
	}
}
