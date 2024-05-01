/**
 * Copyright (c) 2023 Source Auditor Inc.
 *
 * SPDX-License-Identifier: Apache-2.0
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.spdx.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.core.NotEquivalentReason.NotEquivalent;
import org.spdx.storage.IModelStore;
import org.spdx.storage.PropertyDescriptor;
import org.spdx.storage.IModelStore.IModelStoreLock;
import org.spdx.storage.IModelStore.IdType;
import org.spdx.storage.IModelStore.ModelUpdate;

/**
 * @author Gary O'Neall
 * 
 * Superclass for all SPDX model objects
 * 
 * Provides the primary interface to the storage class that access and stores the data for 
 * the model objects.
 * 
 * This class includes several helper methods to manage the storage and retrieval of properties.
 * 
 * Each model object is in itself stateless.  All state is maintained in the Model Store.  
 * 
 * The concrete classes are expected to implements getters for the model class properties which translate
 * into calls to the getTYPEPropertyValue where TYPE is the type of value to be returned and the property descriptor
 * is passed as a parameter.
 * 
 * There are 2 methods of setting values:
 *   - call the setPropertyValue, clearValueCollection or addValueToCollection methods - this will call the modelStore and store the
 *     value immediately
 *   - Gather a list of updates by calling the updatePropertyValue, updateClearValueList, or updateAddPropertyValue
 *     methods.  These methods return a ModelUpdate which can be applied later by calling the <code>apply()</code> method.
 *     A convenience method <code>Write.applyUpdatesInOneTransaction</code> will perform all updates within
 *     a single transaction. This method may result in higher performance updates for some Model Store implementations.
 *     Note that none of the updates will be applied until the storage manager update method is invoked.
 * 
 * Property values are restricted to the following types:
 *   - String - Java Strings
 *   - Booolean - Java Boolean or primitive boolean types
 *   - CoreModelObject - A concrete subclass of this type
 *   - {@literal Collection<T>} - A Collection of type T where T is one of the supported non-collection types
 *     
 * This class also handles the conversion of a CoreModelObject to and from a TypeValue for storage in the ModelStore.
 *
 */
public abstract class CoreModelObject {
	
	static final Logger logger = LoggerFactory.getLogger(CoreModelObject.class);
	protected IModelStore modelStore;
	protected String objectUri;
	protected String specVersion;
	
	/**
	 * If non null, a reference made to a model object stored in a different modelStore and/or
	 * document will be copied to this modelStore and documentUri
	 */
	protected IModelCopyManager copyManager = null;
	/**
	 * if true, checks input values for setters to verify valid SPDX inputs
	 */
	protected boolean strict = true;
	
	NotEquivalentReason lastNotEquivalentReason = null;
		
	/**
	 * Create a new Model Object using an Anonymous ID with the default store and default document URI
	 * @param specVersion - version of the SPDX spec the object complies with
	 * @throws InvalidSPDXAnalysisException 
	 */
	public CoreModelObject(String specVersion) throws InvalidSPDXAnalysisException {
		this(DefaultModelStore.getDefaultModelStore().getNextId(IdType.Anonymous, DefaultModelStore.getDefaultDocumentUri()), specVersion);
	}
	
	/**
	 * Open or create a model object with the default store
	 * @param objectUri Anonymous ID or URI for the model object
	 * @param specVersion - version of the SPDX spec the object complies with
	 * @throws InvalidSPDXAnalysisException 
	 */
	public CoreModelObject(String objectUri, String specVersion) throws InvalidSPDXAnalysisException {
		this(DefaultModelStore.getDefaultModelStore(), objectUri, 
				DefaultModelStore.getDefaultCopyManager(), true, specVersion);
	}
	
	/**
	 * Creates a new model object
	 * @param modelStore Storage for the model objects - Must support model V3 classes
	 * @param objectUri Anonymous ID or URI for the model object
	 * @param copyManager - if supplied, model objects will be implicitly copied into this model store and document URI when referenced by setting methods
	 * @param create - if true, the object will be created in the store if it is not already present
	 * @param specVersion - version of the SPDX spec the object complies with
	 * @throws InvalidSPDXAnalysisException invalid parameters or duplicate objectUri
	 */
	public CoreModelObject(IModelStore modelStore, String objectUri, @Nullable IModelCopyManager copyManager, 
			boolean create, String specVersion) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(modelStore, "Model Store can not be null");
		Objects.requireNonNull(objectUri, "Object URI can not be null");
		Objects.requireNonNull(specVersion, "Spec version can not be null");
		if (!ModelRegistry.getModelRegistry().containsSpecVersion(specVersion)) {
			throw new InvalidSPDXAnalysisException("Unknown spec version "+specVersion);
		}
		this.modelStore = modelStore;
		this.copyManager = copyManager;
		this.objectUri = objectUri;
		this.specVersion = specVersion;

		Optional<TypedValue> existing = modelStore.getTypedValue(objectUri);
		if (existing.isPresent()) {
			if (create && !existing.get().getType().equals(getType())) {
				logger.error("Can not create "+objectUri+".  It is already in use with type "+existing.get().getType()+" which is incompatible with type "+getType());
				throw new SpdxIdInUseException("Can not create "+objectUri+".  It is already in use with type "+existing.get().getType()+" which is incompatible with type "+getType());
			}
		} else {
			if (create) {
				IModelStoreLock lock = enterCriticalSection(false);
				// re-check since previous check was done outside of the lock
				try {
					if (!modelStore.exists(objectUri)) {
						modelStore.create(new TypedValue(objectUri, getType(), specVersion));
					}
				} finally {
					lock.unlock();
				}
			} else {
				logger.error(objectUri+" does not exist");
				throw new SpdxIdNotFoundException(objectUri+" does not exist");
			}
		}
	}

	/**
	 * @param builder base builder to create the CoreModelObject from
	 * @param specVersion - version of the SPDX spec the object complies with
	 * @throws InvalidSPDXAnalysisException 
	 */
	public CoreModelObject(CoreModelObjectBuilder builder, String specVersion) throws InvalidSPDXAnalysisException {
		this(builder.modelStore, builder.objectUri, builder.copyManager, true, specVersion);
		this.strict = builder.strict;
	}

	// Abstract methods that must be implemented in the subclasses
	/**
	 * @return The class name for this object.  Class names are defined in the constants file
	 */
	public abstract String getType();
		
	/**
	 * Enter a critical section. leaveCriticialSection must be called.
	 * @param readLockRequested true implies a read lock, false implies write lock.
	 * @throws InvalidSPDXAnalysisException 
	 */
	public IModelStoreLock enterCriticalSection(boolean readLockRequested) throws InvalidSPDXAnalysisException {
		return modelStore.enterCriticalSection(readLockRequested);
	}
	
	/**
	 * Leave a critical section. Releases the lock form the matching enterCriticalSection
	 */
	public void leaveCriticalSection(IModelStoreLock lock) {
		modelStore.leaveCriticalSection(lock);
	}
	
	/**
	 * Implementation of the specific verifications for this model object
	 * @param specVersion Version of the SPDX spec to verify against
	 * @param verifiedElementIds list of all Element Id's which have already been verified - prevents infinite recursion
	 * @return Any verification errors or warnings associated with this object
	 */
	protected abstract List<String> _verify(Set<String> verifiedElementIds, String specVersion);
	
	/**
	 * @param specVersion Version of the SPDX spec to verify against
	 * @param verifiedIds verifiedIds list of all Id's which have already been verifieds - prevents infinite recursion
	 * @return Any verification errors or warnings associated with this object
	 */
	public abstract List<String> verify(Set<String> verifiedIds, String specVersion);
	
	/**
	 * @param specVersion Version of the SPDX spec to verify against
	 * @return Any verification errors or warnings associated with this object
	 */
	public List<String> verify(String specVersion) {
		return verify(new HashSet<>(), specVersion);
	}
	
	/**
	 * @return Any verification errors or warnings associated with this object
	 */
	public List<String> verify() {
		return verify(this.specVersion);
	}
	
	/**
	 * Verifies all elements in a collection
	 * @param specVersion version of the SPDX specification to verify against
	 * @param collection collection to be verifies
	 * @param verifiedIds verifiedIds list of all Id's which have already been verifieds - prevents infinite recursion
	 * @param warningPrefix String to prefix any warning messages
	 */
	public List<String> verifyCollection(Collection<? extends CoreModelObject> collection, String warningPrefix, Set<String> verifiedIds, String specVersion) {
		List<String> retval = new ArrayList<>();
		for (CoreModelObject mo:collection) {
			for (String warning:mo.verify(verifiedIds, specVersion)) {
				if (Objects.nonNull(warningPrefix)) {
					retval.add(warningPrefix + warning);
				} else {
					retval.add(warning);
				}
			}
		}
		return retval;
	}
	
	/**
	 * @return the Object URI or anonymous ID
	 */
	public String getObjectUri() {
		return this.objectUri;
	}
	
	/**
	 * @return the model store for this object
	 */
	public IModelStore getModelStore() {
		return this.modelStore;
	}
	
	/**
	 * @return if strict input checking is enabled
	 */
	public boolean isStrict() {
		return strict;
	}
	
	/**
	 * @param strict if true, inputs will be validated against the SPDX spec
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	//The following methods are to manage the properties associated with the model object
	/**
	 * @return all names of property descriptors currently associated with this object
	 * @throws InvalidSPDXAnalysisException 
	 */
	public List<PropertyDescriptor> getPropertyValueDescriptors() throws InvalidSPDXAnalysisException {
		return modelStore.getPropertyValueDescriptors(this.objectUri);
	}
	
	/**
	 * Get an object value for a property
	 * @param propertyDescriptor Descriptor for the property
	 * @return value associated with a property
	 */
	public Optional<Object> getObjectPropertyValue(PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException {
		Optional<Object> retval = ModelObjectHelper.getObjectPropertyValue(modelStore, objectUri, 
				propertyDescriptor, copyManager, specVersion);
		if (retval.isPresent() && retval.get() instanceof CoreModelObject && !strict) {
			((CoreModelObject)retval.get()).setStrict(strict);
		}
		return retval;
	}
	
	/**
	 * Set a property value for a property descriptor, creating the property if necessary
	 * @param propertyDescriptor Descriptor for the property associated with this object
	 * @param value Value to associate with the property
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setPropertyValue(PropertyDescriptor propertyDescriptor, @Nullable Object value) throws InvalidSPDXAnalysisException {
		if (this instanceof IndividualUriValue) {
			throw new InvalidSPDXAnalysisException("Can not set a property for the literal value "+((IndividualUriValue)this).getIndividualURI());
		}
		ModelObjectHelper.setPropertyValue(this.modelStore, objectUri, propertyDescriptor, value, copyManager);
	}
	
	/**
	 * Create an update when, when applied by the ModelStore, sets a property value for a property descriptor, creating the property if necessary
	 * @param propertyDescriptor Descriptor for the property associated with this object
	 * @param value Value to associate with the property
	 * @return an update which can be applied by invoking the apply method
	 */
	public ModelUpdate updatePropertyValue(PropertyDescriptor propertyDescriptor, Object value) {
		return () ->{
			ModelObjectHelper.setPropertyValue(this.modelStore, objectUri, propertyDescriptor, value, copyManager);
		};
	}
	
	/**
	 * @param propertyDescriptor Descriptor for a property
	 * @return the Optional String value associated with a property, null if no value is present
	 * @throws SpdxInvalidTypeException
	 */
	public Optional<String> getStringPropertyValue(PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException {
		Optional<Object> result = getObjectPropertyValue(propertyDescriptor);
		if (result.isPresent()) {
			if (result.get() instanceof String) {
				return Optional.of((String)result.get());
			} else {
				logger.error("Property "+propertyDescriptor+" is not of type String");
				throw new SpdxInvalidTypeException("Property "+propertyDescriptor+" is not of type String");
			}
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * @param propertyDescriptor Descriptor for a property
	 * @return the Optional Integer value associated with a property, null if no value is present
	 * @throws InvalidSPDXAnalysisException
	 */
	public Optional<Integer> getIntegerPropertyValue(PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException {
		Optional<Object> result = getObjectPropertyValue(propertyDescriptor);
		Optional<Integer> retval;
		if (result.isPresent()) {
			if (!(result.get() instanceof Integer)) {
				throw new SpdxInvalidTypeException("Property "+propertyDescriptor+" is not of type Integer");
			}
			retval = Optional.of((Integer)result.get());
		} else {
			retval = Optional.empty();
		}
		return retval;
	}
	
	/**
	 * @param propertyDescriptor descriptor for the property
	 * @return an enumeration value for the property
	 * @throws InvalidSPDXAnalysisException
	 */
	@SuppressWarnings("unchecked")
	public Optional<Enum<?>> getEnumPropertyValue(PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException {
		Optional<Object> result = getObjectPropertyValue(propertyDescriptor);
		if (!result.isPresent()) {
			return Optional.empty();
		}
		if (result.get() instanceof Enum) {
			return (Optional<Enum<?>>)(Optional<?>)result;
		}
		if (!(result.get() instanceof IndividualUriValue)) {
			throw new SpdxInvalidTypeException("Property "+propertyDescriptor+" is not of type Individual Value or enum");
		}
		Enum<?> retval = ModelRegistry.getModelRegistry().uriToEnum(((IndividualUriValue)result.get()).getIndividualURI(), this.specVersion);
		if (Objects.isNull(retval)) {
			logger.error("Unknown individual value for enum: "+((IndividualUriValue)result.get()).getIndividualURI());
			throw new InvalidSPDXAnalysisException("Unknown individual value for enum: "+((IndividualUriValue)result.get()).getIndividualURI());
		} else {
			return Optional.of(retval);
		}
	}
	
	/**
	 * @param propertyDescriptor Descriptor for the property
	 * @return the Optional Boolean value for a property
	 * @throws SpdxInvalidTypeException
	 */
	public Optional<Boolean> getBooleanPropertyValue(PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException {
		Optional<Object> result = getObjectPropertyValue(propertyDescriptor);
		if (result.isPresent()) {
			if (result.get() instanceof Boolean) {
				return Optional.of((Boolean)result.get());
			} else if (result.get() instanceof String) {
				// try to convert
				String sResult = ((String)result.get()).toLowerCase();
				if ("true".equals(sResult)) {
					return Optional.of(Boolean.valueOf(true));
				} else if ("false".equals(sResult)) {
					return Optional.of(Boolean.valueOf(false));
				} else {
					throw new SpdxInvalidTypeException("Property "+propertyDescriptor+" is not of type Boolean");
				}
			} else {
				throw new SpdxInvalidTypeException("Property "+propertyDescriptor+" is not of type Boolean");
			}
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * Removes a property and its value from the model store if it exists
	 * @param propertyDescriptor Descriptor for the property associated with this object to be removed
	 * @throws InvalidSPDXAnalysisException
	 */
	public void removeProperty(PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException {
		ModelObjectHelper.removeProperty(modelStore, objectUri, propertyDescriptor);
	}
	
	/**
	 * Create an update when, when applied by the ModelStore, removes a property and its value from the model store if it exists
	 * @param propertyDescriptor Descriptor for the property associated with this object to be removed
	 * @return  an update which can be applied by invoking the apply method
	 */
	public ModelUpdate updateRemoveProperty(PropertyDescriptor propertyDescriptor) {
		return () -> {
			ModelObjectHelper.removeProperty(modelStore, objectUri, propertyDescriptor);
		};
	}
	
	/**
	 * Clears a collection of values associated with a property
	 * @param propertyDescriptor Descriptor for the property
	 */
	public void clearValueCollection(PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException {
		ModelObjectHelper.clearValueCollection(modelStore, objectUri, propertyDescriptor);
	}
	
	/**
	 * Create an update when, when applied by the ModelStore, clears a collection of values associated with a property
	 * @param propertyDescriptor Descriptor for the property
	 * @return an update which can be applied by invoking the apply method
	 */
	public ModelUpdate updateClearValueCollection(PropertyDescriptor propertyDescriptor) {
		return () ->{
			ModelObjectHelper.clearValueCollection(modelStore, objectUri, propertyDescriptor);
		};
	}
	
	/**
	 * Add a value to a collection of values associated with a property.  If a value is a CoreModelObject and does not
	 * belong to the document, it will be copied into the object store
	 * @param propertyDescriptor  Descriptor for the property
	 * @param value to add
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void addPropertyValueToCollection(PropertyDescriptor propertyDescriptor, Object value) throws InvalidSPDXAnalysisException {
		ModelObjectHelper.addValueToCollection(modelStore, objectUri, propertyDescriptor, value, copyManager);
	}
	
	/**
	 * Create an update when, when applied, adds a value to a collection of values associated with a property.  If a value is a CoreModelObject and does not
	 * belong to the document, it will be copied into the object store
	 * @param propertyDescriptor  Descriptor for the property
	 * @param value to add
	 * @return an update which can be applied by invoking the apply method
	 */
	public ModelUpdate updateAddPropertyValueToCollection(PropertyDescriptor propertyDescriptor, Object value) {
		return () ->{
			ModelObjectHelper.addValueToCollection(modelStore, objectUri, propertyDescriptor, value, copyManager);
		};
	}
	
	/**
	 * Remove a property value from a collection
	 * @param propertyDescriptor Descriptor for the property
	 * @param value Value to be removed
	 * @throws InvalidSPDXAnalysisException
	 */
	public void removePropertyValueFromCollection(PropertyDescriptor propertyDescriptor, Object value) throws InvalidSPDXAnalysisException {
		ModelObjectHelper.removePropertyValueFromCollection(modelStore, objectUri, propertyDescriptor, value);
	}
	
	/**
	 * Create an update when, when applied, removes a property value from a collection
	 * @param propertyDescriptor descriptor for the property
	 * @param value Value to be removed
	 * @return an update which can be applied by invoking the apply method
	 */
	public ModelUpdate updateRemovePropertyValueFromCollection(PropertyDescriptor propertyDescriptor, Object value) {
		return () -> {
			ModelObjectHelper.removePropertyValueFromCollection(modelStore, objectUri, propertyDescriptor, value);
		};
	}
	
	/**
	 * @param propertyDescriptor Descriptor for the property
	 * @return Set of values associated with a property
	 */
	public ModelSet<?> getObjectPropertyValueSet(PropertyDescriptor propertyDescriptor, Class<?> type) throws InvalidSPDXAnalysisException {
		return new ModelSet<Object>(this.modelStore, this.objectUri, propertyDescriptor, 
				this.copyManager, type, specVersion);
	}
	
	/**
	 * @param propertyDescriptor Descriptor for the property
	 * @return Collection of values associated with a property
	 */
	public ModelCollection<?> getObjectPropertyValueCollection(PropertyDescriptor propertyDescriptor, Class<?> type) throws InvalidSPDXAnalysisException {
		return new ModelCollection<Object>(this.modelStore, this.objectUri, propertyDescriptor, 
				this.copyManager, type, specVersion);
	}
	
	/**
	 * @param propertyDescriptor Descriptor for property
	 * @return Collection of Strings associated with the property
	 * @throws SpdxInvalidTypeException
	 */
	@SuppressWarnings("unchecked")
	public Collection<String> getStringCollection(PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException {
		if (!isCollectionMembersAssignableTo(propertyDescriptor, String.class)) {
			throw new SpdxInvalidTypeException("Property "+propertyDescriptor+" does not contain a collection of Strings");
		}
		return (Collection<String>)(Collection<?>)getObjectPropertyValueSet(propertyDescriptor, String.class);
	}
	
	public boolean isCollectionMembersAssignableTo(PropertyDescriptor propertyDescriptor, Class<?> clazz) throws InvalidSPDXAnalysisException {
		return modelStore.isCollectionMembersAssignableTo(objectUri, propertyDescriptor, 
				ModelObjectHelper.modelClassToStoredClass(clazz));
	}
	
	/**
	 * @param compare
	 * @return true if all the properties have the same or equivalent values
	 */
	public boolean equivalent(CoreModelObject compare) throws InvalidSPDXAnalysisException {
		return equivalent(compare, false);
	}
	
	/**
	 * @param compare
	 * @param ignoreRelatedElements if true, do not compare properties relatedSpdxElement - used to prevent infinite recursion
	 * @return true if all the properties have the same or equivalent values
	 */
	public boolean equivalent(CoreModelObject compare, boolean ignoreRelatedElements) throws InvalidSPDXAnalysisException {
		if (!this.getClass().equals(compare.getClass())) {
			lastNotEquivalentReason = new NotEquivalentReason(NotEquivalent.DIFFERENT_CLASS);
			return false;
		}
		List<PropertyDescriptor> propertyValueDescriptors = getPropertyValueDescriptors();
		List<PropertyDescriptor> comparePropertyValueDescriptors = new ArrayList<PropertyDescriptor>(compare.getPropertyValueDescriptors());	// create a copy since we're going to modify it
		for (PropertyDescriptor propertyDescriptor:propertyValueDescriptors) {
			if (ignoreRelatedElements && isRelatedElement(propertyDescriptor)) {
				continue;
			}
			if (comparePropertyValueDescriptors.contains(propertyDescriptor)) {
				if (!propertyValuesEquivalent(propertyDescriptor, this.getObjectPropertyValue(propertyDescriptor), 
				        compare.getObjectPropertyValue(propertyDescriptor), ignoreRelatedElements)) {
					lastNotEquivalentReason = new NotEquivalentReason(
							NotEquivalent.PROPERTY_NOT_EQUIVALENT, propertyDescriptor);
				    return false;
				}
				comparePropertyValueDescriptors.remove(propertyDescriptor);
			} else {
				// No property value
			    Optional<Object> propertyValueOptional = this.getObjectPropertyValue(propertyDescriptor);
				if (propertyValueOptional.isPresent()) {
					Object propertyValue = propertyValueOptional.get();
					if (isEquivalentToNull(propertyValue, propertyDescriptor)) {
						continue;
					}
					lastNotEquivalentReason = new NotEquivalentReason(
							NotEquivalent.COMPARE_PROPERTY_MISSING, propertyDescriptor);
					return false;
				}
			}
		}
		for (PropertyDescriptor propertyDescriptor:comparePropertyValueDescriptors) { // check any remaining property values
			if (ignoreRelatedElements && isRelatedElement(propertyDescriptor)) {
				continue;
			}
			Optional<Object> comparePropertyValueOptional = compare.getObjectPropertyValue(propertyDescriptor);
			if (!comparePropertyValueOptional.isPresent()) {
				continue;
			}
			Object comparePropertyValue = comparePropertyValueOptional.get();
			if (isEquivalentToNull(comparePropertyValue, propertyDescriptor)) {
				continue;
			}
			lastNotEquivalentReason = new NotEquivalentReason(
					NotEquivalent.MISSING_PROPERTY, propertyDescriptor);
			return false;
		}
		return true;
	}
	
	// Some values are treated like null in comparisons - in particular empty model collections and 
	// "no assertion" values and a filesAnalyzed filed with a value of true
	private boolean isEquivalentToNull(Object propertyValue, PropertyDescriptor propertyDescriptor) {
		if (propertyValue instanceof ModelCollection) {
			return isEmptyModelCollection(propertyValue);
		} else if (isNoAssertion(propertyValue)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @param propertyDescriptor property descriptor for the object in question
	 * @return true if the object is "to" part of a relationship
	 */
	public abstract boolean isRelatedElement(PropertyDescriptor propertyDescriptor);
	
	/**
	 * @param value value to test against an empty model collection
	 * @return true if the value is a model collection and it is empty
	 */
	private boolean isEmptyModelCollection(Object value) {
		return (value instanceof ModelCollection)
				&& (((ModelCollection<?>) value).size() == 0);
	}
	
	private boolean isNoAssertion(Object propertyValue) {
		return false;
		//TODO: Implement
	}
	
	/**
	 * @param propertyDescriptor Descriptor for the property
	 * @param valueA value to compare
	 * @param valueB value to compare
	 * @param ignoreRelatedElements if true, do not compare properties relatedSpdxElement - used to prevent infinite recursion
	 * @return true if the property values are equivalent
	 * @throws InvalidSPDXAnalysisException
	 */
	private boolean propertyValuesEquivalent(PropertyDescriptor propertyDescriptor, Optional<Object> valueA,
            Optional<Object> valueB, boolean ignoreRelatedElements) throws InvalidSPDXAnalysisException {
	    if (!valueA.isPresent()) {
            if (valueB.isPresent()) {
                return isEmptyModelCollection(valueB.get());
            }
        } else if (!valueB.isPresent()) {
            return isEmptyModelCollection(valueA.get());
        } else if (valueA.get() instanceof ModelCollection && valueB.get() instanceof ModelCollection) {
            List<?> myList = ((ModelCollection<?>)valueA.get()).toImmutableList();
            List<?> compareList = ((ModelCollection<?>)valueB.get()).toImmutableList();
            if (!areEquivalent(myList, compareList, ignoreRelatedElements)) {
                return false;
            }
        } else if (valueA.get() instanceof List && valueB.get() instanceof List) {
            if (!areEquivalent((List<?>)valueA.get(), (List<?>)valueB.get(), ignoreRelatedElements)) {
                return false;
            }
        } else if (valueA.get() instanceof IndividualUriValue && valueB.get() instanceof IndividualUriValue) {
            if (!Objects.equals(((IndividualUriValue)valueA.get()).getIndividualURI(), ((IndividualUriValue)valueB.get()).getIndividualURI())) {
                return false;
            }
            // Note: we must check the IndividualValue before the CoreModelObject types since the IndividualValue takes precedence
        } else if (valueA.get() instanceof CoreModelObject && valueB.get() instanceof CoreModelObject) {
            if (!((CoreModelObject)valueA.get()).equivalent(((CoreModelObject)valueB.get()), 
                    isRelatedElement(propertyDescriptor) ? true : ignoreRelatedElements)) {
                return false;
            }
            
        } else if (!optionalObjectsEquivalent(valueA, valueB)) { // Present, not a list, and not a TypedValue
            return false;
        }
	    return true;
    }
	
    /**
	 * Compares 2 simple optional objects considering NONE and NOASSERTION values which are equivalent to their strings
	 * @param valueA
	 * @param valueB
	 * @return
	 */
	private boolean optionalObjectsEquivalent(Optional<Object> valueA, Optional<Object> valueB) {
		if (Objects.equals(valueA, valueB)) {
			return true;
		}
		if (!valueA.isPresent()) {
			return false;
		}
		if (!valueB.isPresent()) {
			return false;
		}
		if (valueA.get() instanceof IndividualUriValue) {
			if (!(valueB.get() instanceof IndividualUriValue)) {
				return false;
			}
			
			return ((IndividualUriValue)(valueA.get())).getIndividualURI().equals(((IndividualUriValue)(valueB.get())).getIndividualURI());
		}
		if (valueA.get() instanceof String && valueB.get() instanceof String) {
			return normalizeString((String)valueA.get()).equals(normalizeString((String)valueB.get()));
		}
		return false;
	}

	/**
	 * Normalize a string for dos and linux linefeeds
	 * @param s
	 * @return DOS style only linefeeds
	 */
	private Object normalizeString(String s) {
		return s.replaceAll("\r\n", "\n").trim();
	}

	/**
	 * Checks if for each item on either list, there is an item in the other list that is equivalent.
	 * @param ignoreRelatedElements Whether related elements should be ignored in the comparison
	 */
	private boolean areEquivalent(List<?> firstList, List<?> secondList,
										 boolean ignoreRelatedElements) throws InvalidSPDXAnalysisException {
		if (firstList.size() != secondList.size()) {
			return false;
		}
		for (Object item : firstList) {
			if (!containsEqualOrEquivalentItem(secondList, item, ignoreRelatedElements)) {
				return false;
			}
		}
		for (Object item : secondList) {
			if (!containsEqualOrEquivalentItem(firstList, item, ignoreRelatedElements)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Searches a list for an equal or equivalent item
	 * @param list list to search
	 * @param itemToFind  the item we're looking for
	 * @param ignoreRelatedElements if true, don't follow the to parts of relationships
	 * @return true if the list contains an equal or equivalent item
	 * @throws InvalidSPDXAnalysisException
	 */
	private boolean containsEqualOrEquivalentItem(List<?> list, Object itemToFind,
			  boolean ignoreRelatedElements) throws InvalidSPDXAnalysisException {
		if (list.contains(itemToFind)) {
			return true;
		} else if (itemToFind instanceof IndividualUriValue && list.contains(new SimpleUriValue((IndividualUriValue) itemToFind))) {
			// Two IndividualUriValues are considered equal if their URI coincides
			return true;
		}
		
		if (!(itemToFind instanceof CoreModelObject)) {
			return false;
		}
		
		CoreModelObject objectToFind = (CoreModelObject) itemToFind;
		for (Object objectToCompare : list) {
			if (!(objectToCompare instanceof CoreModelObject)) {
				continue;
			}
			if (objectToFind.equivalent((CoreModelObject) objectToCompare, ignoreRelatedElements)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (modelStore.getIdType(objectUri) == IdType.Anonymous) {
			return 11 ^ modelStore.hashCode() ^ objectUri.hashCode();
		} else {
			return this.objectUri.hashCode();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof CoreModelObject)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		CoreModelObject comp = (CoreModelObject)o;
		if (getModelStore().getIdType(objectUri).equals(IdType.Anonymous)) {
			return Objects.equals(modelStore, comp.getModelStore()) && Objects.equals(objectUri, comp.getObjectUri());
		} else {
			return Objects.equals(objectUri, comp.getObjectUri());
		}
	}

	
	/**
	 * Clone a new object using a different model store
	 * @param modelStore
	 * @return
	 */
	public CoreModelObject clone(IModelStore modelStore) {
		if (Objects.isNull(this.copyManager)) {
			throw new IllegalStateException("A copy manager must be provided to clone");
		}
		if (this.modelStore.equals(modelStore)) {
			throw new IllegalStateException("Can not clone to the same model store");
		}
		Objects.requireNonNull(modelStore, "Model store for clone must not be null");
		if (modelStore.exists(objectUri)) {
			throw new IllegalStateException("Can not clone - "+objectUri+" already exists.");
		}
		try {
			CoreModelObject retval = ModelRegistry.getModelRegistry().createModelObject(
					modelStore, objectUri, this.getType(), 
					this.copyManager, this.specVersion, true);
			retval.copyFrom(this);
			return retval;
		} catch (InvalidSPDXAnalysisException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copy all the properties from the source object
	 * @param source
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void copyFrom(CoreModelObject source) throws InvalidSPDXAnalysisException {
		if (Objects.isNull(copyManager)) {
			throw new InvalidSPDXAnalysisException("Copying is not enabled for "+objectUri);
		}
		copyManager.copy(this.modelStore, objectUri, 
				source.getModelStore(), source.getObjectUri(), this.getType(),
				specVersion, null);
	}
	
	/**
	 * Set the copy manager
	 * @param copyManager copy manager to set
	 */
	public void setCopyManager(IModelCopyManager copyManager) {
		this.copyManager = copyManager;
	}
	
	/**
	 * @return the copy manager - value may be null if copies are not allowd
	 */
	public IModelCopyManager getCopyManager() {
		return this.copyManager;
	}
	
	/**
	 * @return a typed value representation of this object suitable for storage in the model store
	 * @throws InvalidSPDXAnalysisException
	 */
	public TypedValue toTypedValue() throws InvalidSPDXAnalysisException {
		return new TypedValue(objectUri, getType(), specVersion);
	}
	
	@Override
	public String toString() {
		return this.getType() + ":" + objectUri;
	}

	/**
	 * Base builder class for all model objects
	 *
	 */
	public static class CoreModelObjectBuilder  {
		
		public IModelStore modelStore;
		public String objectUri;
		public IModelCopyManager copyManager;
		public boolean strict = true;

		public CoreModelObjectBuilder(IModelStore modelStore, String objectUri, @Nullable IModelCopyManager copyManager) {
			this.modelStore = modelStore;
			this.objectUri = objectUri;
			this.copyManager = copyManager;
		}
		
		public void setStrict(boolean strict) {
			this.strict = strict;
		}
	}

	/**
	 * @return the version of the SPDX specification this object complies with
	 */
	public String getSpecVersion() {
		return this.specVersion;
	}
}
