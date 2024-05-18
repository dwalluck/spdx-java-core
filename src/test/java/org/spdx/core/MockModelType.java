/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.spdx.storage.IModelStore;
import org.spdx.storage.PropertyDescriptor;

/**
 * @author Gary
 * 
 * Mock model type for testing
 *
 */
public class MockModelType extends CoreModelObject {
	
	static final String TYPE = "MockType";

	/**
	 * @param modelStore
	 * @param objectUri
	 * @param copyManager
	 * @param create
	 * @param specVersion
	 * @throws InvalidSPDXAnalysisException
	 */
	public MockModelType(IModelStore modelStore, String objectUri,
			IModelCopyManager copyManager, boolean create, String specVersion)
			throws InvalidSPDXAnalysisException {
		super(modelStore, objectUri, copyManager, create, specVersion);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<String> _verify(Set<String> verifiedElementIds,
			String specVersion, List<IndividualUriValue> profiles) {
		return new ArrayList<>();
	}

	@Override
	public List<String> verify(Set<String> verifiedIds, String specVersion) {
		return new ArrayList<>();
	}

	@Override
	public boolean isRelatedElement(PropertyDescriptor propertyDescriptor) {
		return false;
	}

}
