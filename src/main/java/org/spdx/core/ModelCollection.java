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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.spdx.storage.IModelStore;
import org.spdx.storage.PropertyDescriptor;

/**
 * Collection of elements stored in a ModelStore
 * 
 * @author Gary O'Neall
 *
 */
public class ModelCollection<T extends Object> implements Collection<Object> {

	private IModelStore modelStore;
	private String objectUri;
	private PropertyDescriptor propertyDescriptor;
	private IModelCopyManager copyManager;
	private String specVersion;
	private String idPrefix;
	
	/**
	 * Map of URI's of elements referenced but not present in the store
	 */
	protected Map<String, IExternalElementInfo> externalMap;
	private Class<?> type;
	//TODO: See if this boolean is needed before deleting the comments
//	private boolean licensePrimitiveAssignable;  // If true, NONE and NOASSERTION should be converted to NoneLicense and NoAssertionLicense
	
	/**
	 * @author Gary O'Neall
	 * 
	 * Iterator over the model collection
	 *
	 */
	class ModelCollectionIterator implements Iterator<Object> {
		
		private Iterator<Object> storageIterator;

		public ModelCollectionIterator(Iterator<Object> storageIterator) {
			this.storageIterator = storageIterator;
		}

		@Override
		public boolean hasNext() {
			return storageIterator.hasNext();
		}

		@Override
		public Object next() {
			return checkConvertTypedValue(storageIterator.next());
		}
		
	}
	
	/**
	 * @param modelStore Storage for the model collection
	 * @param objectUri Object URI or anonymous ID
	 * @param propertyDescriptor descriptor for the property use for the model collections
	 * @param copyManager if non-null, use this to copy properties when referenced outside this model store
	 * @param type The class of the elements to be stored in the collection if none, null if not known
	 * @param specVersion version of the SPDX spec the object complies with
	 * @param idPrefix prefix to be used when generating new SPDX IDs
	 * @throws InvalidSPDXAnalysisException on parsing or store errors
	 */
	public ModelCollection(IModelStore modelStore, String objectUri, PropertyDescriptor propertyDescriptor,
			@Nullable IModelCopyManager copyManager,
			@Nullable Class<?> type, String specVersion, String idPrefix) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(modelStore, "Model store can not be null");
		this.modelStore = modelStore;
		Objects.requireNonNull(objectUri, "Object URI or anonymous ID can not be null");
		this.objectUri = objectUri;
		Objects.requireNonNull(propertyDescriptor, "Property descriptor can not be null");
		this.propertyDescriptor = propertyDescriptor;
		this.copyManager = copyManager;
		Objects.requireNonNull(specVersion, "specVersion can not be null");
		this.specVersion = specVersion;
		this.idPrefix = idPrefix;
		if (!modelStore.exists(objectUri)) {
			throw new SpdxIdNotFoundException(objectUri+" does not exist.");
		}
		if (Objects.nonNull(type)) {
			this.type = type;
			// TODO: make sure the following commented logic is not needed
//			licensePrimitiveAssignable = type.isAssignableFrom(SpdxNoneLicense.class) || type.isAssignableFrom(SpdxNoAssertionLicense.class);
			if (!modelStore.isCollectionMembersAssignableTo(objectUri, propertyDescriptor, type)) {
				throw new SpdxInvalidTypeException("Incompatible type for property "+propertyDescriptor+": "+type.toString());
			}
//		} else {
//			licensePrimitiveAssignable = false;
		}
	}

	@Override
	public int size() {
		try {
			return this.modelStore.collectionSize(objectUri, this.propertyDescriptor);
		} catch (InvalidSPDXAnalysisException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isEmpty() {
		try {
			return this.modelStore.collectionSize(objectUri, this.propertyDescriptor) == 0;
		} catch (InvalidSPDXAnalysisException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean contains(Object o) {
		try {
			Object storedObject = null;
			try {
				storedObject = ModelObjectHelper.modelObjectToStoredObject(o, modelStore, copyManager, idPrefix);
			} catch (SpdxObjectNotInStoreException e1) {
				return false;	// The exception is due to the model object not being in the store
			}
			return this.modelStore.collectionContains(
					objectUri, this.propertyDescriptor, storedObject);
		} catch (InvalidSPDXAnalysisException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Converts a typedValue to a model object checking for type compatibility
	 * @param value value to convert
	 * @return model object
	 */
	private Object checkConvertTypedValue(Object value) {
		try {
			Object retval = ModelObjectHelper.storedObjectToModelObject(value, modelStore, copyManager, 
					this.specVersion, this.type, this.idPrefix);
			if (Objects.nonNull(this.type) && !this.type.isAssignableFrom(retval.getClass())) {
				if (retval instanceof IndividualUriValue) {
					throw new SpdxInvalidTypeException("No enumeration, external or individual of the proper type was found for URI "+((IndividualUriValue)retval).getIndividualURI()+
							" for type "+type.toString());
				} else {
					throw new SpdxInvalidTypeException("A collection element of type "+retval.getClass().toString()+
							" was found in a collection of type "+type.toString());
				}
			}
			return retval;
		} catch (InvalidSPDXAnalysisException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Converts any typed or individual value objects to a ModelObject
	 */
	private UnaryOperator<Object> checkConvertTypedValue = value -> {
		return checkConvertTypedValue(value);
	};
	
	/**
	 * @return a list of objects for the model collection
	 */
	public List<Object> toImmutableList() {
		return Collections.unmodifiableList(StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED), false).map(checkConvertTypedValue)
				.collect(Collectors.toList()));
	}

	@Override
	public Iterator<Object> iterator() {
		try {
			return new ModelCollectionIterator(
					modelStore.listValues(objectUri, propertyDescriptor));
		} catch (InvalidSPDXAnalysisException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object[] toArray() {
		return toImmutableList().toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return toImmutableList().toArray(a);
	}

	@Override
	public boolean add(Object element) {
		try {
			return modelStore.addValueToCollection(
					objectUri, propertyDescriptor, 
					ModelObjectHelper.modelObjectToStoredObject(element, modelStore, copyManager, idPrefix));
		} catch (InvalidSPDXAnalysisException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean remove(Object element) {
		try {
			return modelStore.removeValueFromCollection(objectUri, propertyDescriptor,
					ModelObjectHelper.modelObjectToStoredObject(element, modelStore, null, null));
		} catch (InvalidSPDXAnalysisException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return toImmutableList().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		boolean retval = false;
		Iterator<? extends Object> iter = c.iterator();
		while (iter.hasNext()) {
			if (add(iter.next())) {
				retval = true;
			}
		}
		return retval;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean retval = false;
		Iterator<? extends Object> iter = c.iterator();
		while (iter.hasNext()) {
			if (remove(iter.next())) {
				retval = true;
			}
		}
		return retval;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		List<Object> values = toImmutableList();
		boolean retval = false;
		for (Object value:values) {
			if (!c.contains(value) && remove(value)) {
				retval = true;
			}
		}
		return retval;
	}

	@Override
	public void clear() {
		try {
			modelStore.clearValueCollection(objectUri, propertyDescriptor);
		} catch (InvalidSPDXAnalysisException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the modelStore
	 */
	public IModelStore getModelStore() {
		return modelStore;
	}

	/**
	 * @return the objectUri
	 */
	public String getObjectUri() {
		return objectUri;
	}

	/**
	 * @return the propertyDescriptor
	 */
	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}
}
