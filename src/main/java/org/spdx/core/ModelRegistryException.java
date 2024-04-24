/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.core;

/**
 * @author gary
 *
 */
public class ModelRegistryException extends InvalidSPDXAnalysisException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ModelRegistryException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public ModelRegistryException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public ModelRegistryException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ModelRegistryException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public ModelRegistryException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
