/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.core;

/**
 * @author Gary O'Neall
 * 
 * Enum for testing
 *
 */
public enum MockEnum implements IndividualUriValue {
	
	ENUM1("https://mock/enum1"),
	ENUM2("https://mock/enum2");
	
	private String uri;
	
	private MockEnum(String enumUri) {
		this.uri = enumUri;
	}
	@Override
	public String getIndividualURI() {
		return this.uri;
	}

}
