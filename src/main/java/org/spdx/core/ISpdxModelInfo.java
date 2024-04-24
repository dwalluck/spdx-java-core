/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.core;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.spdx.storage.IModelStore;

/**
 * @author Gary O'Neall
 * 
 * Interface for SPDX model information
 *
 */
public interface ISpdxModelInfo {
	
	/**
	 * @return a map of URIs to Enums which represents individual vocabularies in the SPDX model
	 */
	public Map<String, Enum<?>> getUriToEnumMap();

	/**
	 * @return the spec versions this model supports
	 */
	public List<String> getSpecVersions();

	/**
	 * @param store store to use for the inflated object
	 * @param uri URI of the external element
	 * @param copyManager if non-null, implicitly copy any referenced properties from other model stores
	 * @param documentUri URI for the SPDX document to store the external element reference - used for compatibility with SPDX 2.X model stores
	 * @return model object of type type
	 */
	public Object createExternalElement(IModelStore store, String uri,
			@Nullable IModelCopyManager copyManager, @Nullable IExternalElementInfo externalElementInfo,
			@Nullable String documentUri) throws InvalidSPDXAnalysisException;

	/**
	 * @return a map of URIs to Individuals which represents individuals in the SPDX model
	 */
	public Map<String, ISpdxModelInfo> getUriToIndividualMap();

	/**
	 * @param modelStore store to use for the inflated object
	 * @param objectUri URI of the external element
	 * @param type Type of the object to create
	 * @param copyManager if non-null, implicitly copy any referenced properties from other model stores
	 * @param externalMap map of URI's to ExternalMaps for any external elements
	 * @param create if true, create the model object ONLY if it does not already exist
	 * @return fully inflated model object of type type
	 */
	public CoreModelObject createModelObject(IModelStore modelStore,
			String objectUri, String type, IModelCopyManager copyManager, 
			@Nullable Map<String, IExternalElementInfo> externalMap, boolean create);

}
