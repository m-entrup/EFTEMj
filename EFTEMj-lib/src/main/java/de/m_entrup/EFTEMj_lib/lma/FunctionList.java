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

package de.m_entrup.EFTEMj_lib.lma;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

/**
 * @author Michael Entrup b. Epping
 */
public class FunctionList {

	private final HashMap<String, EELS_BackgroundFunction> functions;

	public FunctionList() {
		functions = new HashMap<>();
		final Reflections reflections = new Reflections("de.m_entrup.EFTEMj_lib.lma", new SubTypesScanner(false));
		final Set<Class<? extends EELS_BackgroundFunction>> allClasses = reflections
				.getSubTypesOf(EELS_BackgroundFunction.class);
		final Iterator<Class<? extends EELS_BackgroundFunction>> iter = allClasses.iterator();
		while (iter.hasNext()) {
			final Class<? extends EELS_BackgroundFunction> function = iter.next();
			try {
				final EELS_BackgroundFunction functionInstance = function.newInstance();
				functions.put(functionInstance.getFunctionName(), functionInstance);
			} catch (final InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public HashMap<String, EELS_BackgroundFunction> getFunctions() {
		return functions;
	}

	public String[] getKeys() {
		final Set<String> set = functions.keySet();
		final String[] keys = new String[functions.size()];
		final Iterator<String> iter = set.iterator();
		int index = 0;
		while (iter.hasNext()) {
			keys[index] = iter.next();
			index++;
		}
		return keys;
	}

}
