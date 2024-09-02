/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.core;

/**
 * Runtime Exception wrapper for SPDX exceptions (cause field)
 * 
 * @author Gary O'Neall
 *
 */
public class RuntimeSpdxException extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message exception message
	 */
	public RuntimeSpdxException(String message) {
		super(message);
	}

	/**
	 * @param cause SPDX analysis cause
	 */
	public RuntimeSpdxException(InvalidSPDXAnalysisException cause) {
		super(cause);
	}

	/**
	 * @param message exception message
	 * @param cause SPDX analysis cause
	 */
	public RuntimeSpdxException(String message, InvalidSPDXAnalysisException cause) {
		super(message, cause);
	}

	/**
	 * @param message exception message
	 * @param cause SPDX analysis cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public RuntimeSpdxException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
