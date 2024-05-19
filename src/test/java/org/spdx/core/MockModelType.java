/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.core;

import java.util.ArrayList;
import java.util.Arrays;
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
	
	public static final String TYPE = "MockType";
	static final List<String> TEST_VERIFY = Arrays.asList(new String[] {"testVerify"});

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
	}
	
	public MockModelType(CoreModelObjectBuilder builder, String specVersion) throws InvalidSPDXAnalysisException {
		super(builder,specVersion);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<String> _verify(Set<String> verifiedElementIds,
			String specVersion, List<IndividualUriValue> profiles) {
		return TEST_VERIFY;
	}

	@Override
	public List<String> verify(Set<String> verifiedIds, String specVersion) {
		return _verify(verifiedIds, specVersion, new ArrayList<>());
	}

	@Override
	public boolean isRelatedElement(PropertyDescriptor propertyDescriptor) {
		return false;
	}

}
