package org.spdx.core;


/**
 * Value which is a stored typed item
 */
public class TypedValue {
	
	String objectUri;
	String type;
	
	public TypedValue(String objectUri, String type) throws SpdxInvalidIdException, SpdxInvalidTypeException {
		if (objectUri == null) {
			throw new SpdxInvalidIdException("Null value Id");
		}
		if (type == null) {
			throw new SpdxInvalidTypeException("Null type");
		}
		// Note: We can no longer check that the type is valid since the classes are model dependent
		this.objectUri = objectUri;
		this.type = type;
	}

	/**
	 * @return the objectUri
	 */
	public String getObjectUri() {
		return objectUri;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof TypedValue)) {
			return false;
		}
		TypedValue tv = (TypedValue)o;
		return tv.getObjectUri().equals(this.objectUri) && tv.getType().equals(this.type);
	}
	
	@Override
	public int hashCode() {
		return 181 ^ this.objectUri.hashCode() ^ this.type.hashCode();
	}
}