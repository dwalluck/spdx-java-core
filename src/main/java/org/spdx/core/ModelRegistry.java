/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;

import org.spdx.core.SpdxCoreConstants.SpdxMajorVersion;
import org.spdx.storage.IModelStore;

/**
 * @author Gary O'Neall
 * 
 * Singleton class which contains a registry of SPDX model versions
 * 
 * Each model version implements a model interface <code>ISpdxModelInfo</code> which 
 * supports inflating an SPDX type specific to that version
 * s
 */
public class ModelRegistry {

	private static final ModelRegistry _instance = new ModelRegistry();
	private static final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private Map<String, ISpdxModelInfo> registeredModels = new HashMap<>();
	
	private Map<SpdxMajorVersion, String> majorVersionLatest = new HashMap<>();
	
	/**
	 * Private constructor - singleton class
	 */
	private ModelRegistry() {
		// Nothing really todo here
	}
	
	public static ModelRegistry getModelRegistry() {
		return _instance;
	}
	
	public void registerModel(ISpdxModelInfo modelInfo) {
		lock.writeLock().lock();
		try {
			for (String specVersion:modelInfo.getSpecVersions()) {
				registeredModels.put(specVersion, modelInfo);
				for (SpdxMajorVersion version:SpdxMajorVersion.values()) {
					if (specVersion.startsWith(version.prefix())) {
						if (!majorVersionLatest.containsKey(version) &&
								specVersion.compareToIgnoreCase(majorVersionLatest.get(version)) < 0) {
							majorVersionLatest.put(version, specVersion);
						}
					}
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * @param specVersion
	 * @return
	 */
	public boolean containsSpecVersion(String specVersion) {
		lock.readLock().lock();
		try {
			return registeredModels.containsKey(specVersion);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @param uri URI for the Enum individual
	 * @param specVersion Version of the spec the enum belongs to
	 * @return the Enum represented by the individualURI if it exists within the spec model
	 * @throws ModelRegistryException if the spec version does not exist
	 */
	public @Nullable Enum<?> uriToEnum(String uri, String specVersion) throws ModelRegistryException {
		Objects.requireNonNull(specVersion, "Spec version must not be null");
		Objects.requireNonNull(uri, "URI must not be null");
		lock.readLock().lock();
		try {
			if (!containsSpecVersion(specVersion)) {
				throw new ModelRegistryException(specVersion + " does not exits");
			}
			return registeredModels.get(specVersion).getUriToEnumMap().get(uri);
		} finally {
			lock.readLock().unlock();
		}
	}
	

	/**
	 * Use the default latest "dot" version within the major version
	 * @param uri URI for the Enum individual
	 * @param specMajorVersion major version of the spec	 * 
	 * @return the Enum represented by the individualURI if it exists within the spec model
	 */
	public Enum<?> uriToEnum(String uri, SpdxMajorVersion specMajorVersion) throws ModelRegistryException {
		Objects.requireNonNull(specMajorVersion, "Spec version must not be null");
		Objects.requireNonNull(uri, "URI must not be null");
		lock.readLock().lock();
		try {
			if (!majorVersionLatest.containsKey(specMajorVersion)) {
				throw new ModelRegistryException("No implementation found for SPDX spec versions "+specMajorVersion.prefix()+"...");
			}
			return registeredModels.get(majorVersionLatest.get(specMajorVersion)).getUriToEnumMap().get(uri);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @param store store to use for the inflated object
	 * @param uri URI of the external element
	 * @param copyManager if non-null, implicitly copy any referenced properties from other model stores
	 * @param documentUri URI for the SPDX document to store the external element reference - used for compatibility with SPDX 2.X model stores
	 * @param externalMap Map of URI's of elements referenced but not present in the store
	 * @param specVersion version of the SPDX spec the object complies with
	 * @return external element
	 * @throws InvalidSPDXAnalysisException 
	 */
	public Object getExternalElement(IModelStore store, String uri,
			@Nullable IModelCopyManager copyManager,
			String specVersion) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(specVersion, "Spec version must not be null");
		Objects.requireNonNull(uri, "URI must not be null");
		Objects.requireNonNull(store, "Store must nut be null");
		lock.readLock().lock();
		try {
			if (!containsSpecVersion(specVersion)) {
				throw new ModelRegistryException(specVersion + " does not exits");
			}
			return registeredModels.get(specVersion).createExternalElement(store, uri, copyManager, 
					specVersion);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @param individualUri URI for the individual
	 * @param specVersion version of the SPDX spec the object complies with
	 * @return Individual represented by the URI
	 * @throws ModelRegistryException 
	 */
	public Object uriToIndividual(String individualUri, String specVersion) throws ModelRegistryException {
		Objects.requireNonNull(specVersion, "Spec version must not be null");
		Objects.requireNonNull(individualUri, "individualURI must not be null");
		lock.readLock().lock();
		try {
			if (!containsSpecVersion(specVersion)) {
				throw new ModelRegistryException(specVersion + " does not exits");
			}
			return registeredModels.get(specVersion).getUriToIndividualMap().get(individualUri);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * @param individualUri URI for the individual
	 * @param specMajorVersion major version of the SPDX spec the object complies with
	 * @return Individual represented by the URI from the latest dot release supporting the major version
	 * @throws ModelRegistryException 
	 */
	public Object uriToIndividual(String individualUri, SpdxMajorVersion specMajorVersion) throws ModelRegistryException {
		Objects.requireNonNull(specMajorVersion, "Spec version must not be null");
		Objects.requireNonNull(individualUri, "individualURI must not be null");
		lock.readLock().lock();
		try {
			if (!majorVersionLatest.containsKey(specMajorVersion)) {
				throw new ModelRegistryException("No implementation found for SPDX spec versions "+specMajorVersion.prefix()+"...");
			}
			return registeredModels.get(majorVersionLatest.get(specMajorVersion)).getUriToIndividualMap().get(individualUri);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @param modelStore store to use for the inflated object
	 * @param objectUri URI of the external element
	 * @param documentUri URI for the SPDX document to store the external element reference - used for compatibility with SPDX 2.X model stores
	 * @param type Type of the object to create
	 * @param copyManager if non-null, implicitly copy any referenced properties from other model stores
	 * @param externalMap map of URI's to ExternalMaps for any external elements
	 * @param specVersion version of the SPDX spec the object complies with
	 * @param create if true, create the model object ONLY if it does not already exist
	 * @return model object of type type
	 * @throws InvalidSPDXAnalysisException 
	 */
	public CoreModelObject createModelObject(IModelStore modelStore, String objectUri, 
			String type, IModelCopyManager copyManager,
			String specVersion, boolean create) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(specVersion, "Spec version must not be null");
		Objects.requireNonNull(objectUri, "URI must not be null");
		Objects.requireNonNull(modelStore, "Store must nut be null");
		Objects.requireNonNull(type, "Type must not be null");
		lock.readLock().lock();
		try {
			if (!containsSpecVersion(specVersion)) {
				throw new ModelRegistryException(specVersion + " does not exits");
			}
			return registeredModels.get(specVersion).createModelObject(modelStore, objectUri, 
					type, copyManager, specVersion, create);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @param type String representation of the SPDX type
	 * @param specVersion version of the SPDX spec
	 * @return the class representing the SPDX type, null if it does not exist in the model
	 * @throws ModelRegistryException if the spec version isn't found
	 */
	public @Nullable Class<?> typeToClass(String type, String specVersion) throws ModelRegistryException {
		Objects.requireNonNull(type, "Type must not be null");
		Objects.requireNonNull(specVersion, "Version must not be null");
		lock.readLock().lock();
		try {
			if (!registeredModels.containsKey(specVersion)) {
				throw new ModelRegistryException("No implementation found for SPDX spec version "+specVersion);
			}
			return registeredModels.get(specVersion).getTypeToClassMap().get(type);
		} finally {
			lock.readLock().unlock();
		}
	}
}
