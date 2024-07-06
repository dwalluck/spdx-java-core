/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.core;

/**
 * @author Gary O'Neall
 * 
 * Exception where the default store is used before it has been initialized
 *
 */
public class DefaultStoreNotInitialized extends InvalidSPDXAnalysisException {

	private static final long serialVersionUID = 1L;

	public DefaultStoreNotInitialized() {
	}
	
	public DefaultStoreNotInitialized(String arg0) {
		super(arg0);
	}

	public DefaultStoreNotInitialized(Throwable arg0) {
		super(arg0);
	}

	public DefaultStoreNotInitialized(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	
	public DefaultStoreNotInitialized(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
