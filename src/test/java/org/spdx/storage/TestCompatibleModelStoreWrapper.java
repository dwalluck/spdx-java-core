/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.storage;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.core.MockModelInfo;
import org.spdx.core.ModelRegistry;
import org.spdx.core.ModelRegistryException;
import org.spdx.core.SpdxInvalidIdException;
import org.spdx.core.SpdxInvalidTypeException;
import org.spdx.core.TypedValue;

/**
 * @author Gary O'Neall
 *
 */
public class TestCompatibleModelStoreWrapper {
	
	static final String DOC_DOC_URI = "https://this/is/a/namespace";
	static final String DOC_NAMESPACE = DOC_DOC_URI + "#";
	static final String LICENSE_DOC_URI = "https://spdx.org/licenses/";
	static final String LICENSE_DOC_URI2 = "http://spdx.org/licenses/";
	static final String ANON_ID = "__anon__";
	static final String ID = "SPDXID_15";
	static final String OBJECT_URI = DOC_NAMESPACE + ID;
	
	IModelStore modelStore;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		modelStore = new MockModelStore();
		ModelRegistry.getModelRegistry().registerModel(new MockModelInfo());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.storage.CompatibleModelStoreWrapper#documentUriIdToUri(java.lang.String, java.lang.String, org.spdx.storage.IModelStore)}.
	 */
	@Test
	public void testDocumentUriIdToUriStringStringIModelStore() {
		assertEquals(OBJECT_URI, CompatibleModelStoreWrapper.documentUriIdToUri(DOC_DOC_URI, ID, modelStore));
		assertEquals(ANON_ID, CompatibleModelStoreWrapper.documentUriIdToUri(DOC_DOC_URI, ANON_ID, modelStore));
	}

	/**
	 * Test method for {@link org.spdx.storage.CompatibleModelStoreWrapper#documentUriToNamespace(java.lang.String, boolean)}.
	 */
	@Test
	public void testDocumentUriToNamespace() {

		assertEquals(DOC_DOC_URI + "#", CompatibleModelStoreWrapper.documentUriToNamespace(DOC_DOC_URI, false));
		assertEquals("", CompatibleModelStoreWrapper.documentUriToNamespace(ANON_ID, true));
		assertEquals(LICENSE_DOC_URI, CompatibleModelStoreWrapper.documentUriToNamespace(LICENSE_DOC_URI, false));
		assertEquals(LICENSE_DOC_URI2, CompatibleModelStoreWrapper.documentUriToNamespace(LICENSE_DOC_URI2, false));
	}

	/**
	 * Test method for {@link org.spdx.storage.CompatibleModelStoreWrapper#documentUriIdToUri(java.lang.String, java.lang.String, boolean)}.
	 */
	@Test
	public void testDocumentUriIdToUriStringStringBoolean() {
		assertEquals(OBJECT_URI, CompatibleModelStoreWrapper.documentUriIdToUri(DOC_DOC_URI, ID, false));
		assertEquals(ANON_ID, CompatibleModelStoreWrapper.documentUriIdToUri(DOC_DOC_URI, ANON_ID, true));
	}

	/**
	 * Test method for {@link org.spdx.storage.CompatibleModelStoreWrapper#typedValueFromDocUri(java.lang.String, java.lang.String, boolean, java.lang.String)}.
	 * @throws ModelRegistryException 
	 * @throws SpdxInvalidTypeException 
	 * @throws SpdxInvalidIdException 
	 */
	@Test
	public void testTypedValueFromDocUri() throws SpdxInvalidIdException, SpdxInvalidTypeException, ModelRegistryException {
		String type = "MockType";
		TypedValue result = CompatibleModelStoreWrapper.typedValueFromDocUri(DOC_DOC_URI, ID, false, type);
		assertEquals(type, result.getType());
		assertEquals(OBJECT_URI, result.getObjectUri());
		assertEquals(CompatibleModelStoreWrapper.LATEST_SPDX_2X_VERSION, result.getSpecVersion());
	}

	/**
	 * Test method for {@link org.spdx.storage.CompatibleModelStoreWrapper#objectUriToId(org.spdx.storage.IModelStore, java.lang.String, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testObjectUriToIdIModelStoreStringString() throws InvalidSPDXAnalysisException {
		assertEquals(ID, CompatibleModelStoreWrapper.objectUriToId(modelStore, OBJECT_URI, DOC_DOC_URI));
		assertEquals(ANON_ID, CompatibleModelStoreWrapper.objectUriToId(modelStore, ANON_ID, DOC_DOC_URI));
	}

	/**
	 * Test method for {@link org.spdx.storage.CompatibleModelStoreWrapper#objectUriToId(boolean, java.lang.String, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testObjectUriToIdBooleanStringString() throws InvalidSPDXAnalysisException {
		assertEquals(ID, CompatibleModelStoreWrapper.objectUriToId(false, OBJECT_URI, DOC_DOC_URI));
		assertEquals(ANON_ID, CompatibleModelStoreWrapper.objectUriToId(true, ANON_ID, DOC_DOC_URI));
	}

}
