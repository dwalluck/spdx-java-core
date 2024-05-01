/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.core;

/**
 * @author Gary O'Neall
 * 
 * Common constants used in the SPDX core library
 *
 */
public class SpdxCoreConstants {

	public enum SpdxMajorVersion {
		VERSION_1("SPDX-1."),
		VERSION_2("SPDX-2."),
		VERSION_3("3.");
		
		final private String prefix;
		
		SpdxMajorVersion(String prefix) {
			this.prefix = prefix;
		}

		public static SpdxMajorVersion latestVersion() {
			return VERSION_3;
		}

		/**
		 * @return the string prefix used in all version strings
		 */
		String prefix() {
			return prefix;
		}
	}

		// SPDX Listed License constants
	public static final String LISTED_LICENSE_URL = "https://spdx.org/licenses/";
	// http rather than https since RDF depends on the exact string, 
	// we were not able to update the namespace variable to match the URL's.
	public static final String LISTED_LICENSE_NAMESPACE_PREFIX = "http://spdx.org/licenses/";

}
