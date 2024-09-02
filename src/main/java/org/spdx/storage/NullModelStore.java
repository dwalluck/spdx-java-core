/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.core.TypedValue;

/**
 * Null model store to be used with constants and individuals
 * 
 * @author Gary O'Neall
 *
 */
public class NullModelStore implements IModelStore {

	@Override
	public void close() throws Exception {
		// Nothing to close
	}

	@Override
	public boolean exists(String objectUri) {
		return false;
	}

	@Override
	public void create(TypedValue typedValue)
			throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public List<PropertyDescriptor> getPropertyValueDescriptors(
			String objectUri) throws InvalidSPDXAnalysisException {
		return new ArrayList<>();
	}

	@Override
	public void setValue(String objectUri,
			PropertyDescriptor propertyDescriptor, Object value)
			throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public Optional<Object> getValue(String objectUri,
			PropertyDescriptor propertyDescriptor)
			throws InvalidSPDXAnalysisException {
		return Optional.empty();
	}

	@Override
	public String getNextId(IdType idType) throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public void removeProperty(String objectUri,
			PropertyDescriptor propertyDescriptor)
			throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public Stream<TypedValue> getAllItems(String nameSpace, String typeFilter)
			throws InvalidSPDXAnalysisException {
		return Stream.empty();
	}

	@Override
	public IModelStoreLock enterCriticalSection(boolean readLockRequested)
			throws InvalidSPDXAnalysisException {
		return new IModelStoreLock() {

			@Override
			public void unlock() {
				// no need to do anything
			}
			
		};
	}

	@Override
	public void leaveCriticalSection(IModelStoreLock lock) {
		lock.unlock();
	}

	@Override
	public boolean removeValueFromCollection(String objectUri,
			PropertyDescriptor propertyDescriptor, Object value)
			throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public int collectionSize(String objectUri,
			PropertyDescriptor propertyDescriptor)
			throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public boolean collectionContains(String objectUri,
			PropertyDescriptor propertyDescriptor, Object value)
			throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public void clearValueCollection(String objectUri,
			PropertyDescriptor propertyDescriptor)
			throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public boolean addValueToCollection(String objectUri,
			PropertyDescriptor propertyDescriptor, Object value)
			throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public Iterator<Object> listValues(String objectUri,
			PropertyDescriptor propertyDescriptor)
			throws InvalidSPDXAnalysisException {
		return new ArrayList<Object>().iterator();
	}

	@Override
	public boolean isCollectionMembersAssignableTo(String objectUri,
			PropertyDescriptor propertyDescriptor, Class<?> clazz)
			throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public boolean isPropertyValueAssignableTo(String objectUri,
			PropertyDescriptor propertyDescriptor, Class<?> clazz,
			String specVersion) throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public boolean isCollectionProperty(String objectUri,
			PropertyDescriptor propertyDescriptor)
			throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public IdType getIdType(String objectUri) {
		return IdType.Unkown;
	}

	@Override
	public Optional<String> getCaseSensisitiveId(String nameSpace,
			String caseInsensisitiveId) {
		return Optional.empty();
	}

	@Override
	public Optional<TypedValue> getTypedValue(String objectUri)
			throws InvalidSPDXAnalysisException {
		return Optional.empty();
	}

	@Override
	public void delete(String objectUri) throws InvalidSPDXAnalysisException {
		throw new InvalidSPDXAnalysisException("Null model store - can only be used with constants and individuals");
	}

	@Override
	public boolean isAnon(String objectUri) {
		return false;
	}

}
