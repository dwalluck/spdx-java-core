/**
 * Copyright (c) 2023 Source Auditor Inc.
 * <p>
 * SPDX-License-Identifier: Apache-2.0
 * <p>
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * <p>
 *       http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.spdx.core;

import org.spdx.storage.PropertyDescriptor;

/**
 * Records details when two model objects are compared and are determined to
 * not be equivalent
 * <p>
 * Primarily used for debugging.
 * 
 * @author Gary O'Neall
 */
public class NotEquivalentReason {

	/**
	 * Enum representing the reasons why two model objects are not equivalent.
	 */
	public enum NotEquivalent {
		/**
		 * Indicates that the objects are of different classes.
		 */
		DIFFERENT_CLASS,

		/**
		 * Indicates that a property is missing in one of the objects.
		 */
		MISSING_PROPERTY,

		/**
		 * Indicates that a property value is not equivalent between the objects.
		 */
		PROPERTY_NOT_EQUIVALENT,

		/**
		 * Indicates that a property to compare is missing.
		 */
		COMPARE_PROPERTY_MISSING
	}

		NotEquivalent reason;
		PropertyDescriptor property = null;

		/**
		 * Constructs a NotEquivalentReason with the specified reason.
		 * 
		 * @param reason the reason why the objects are not equivalent
		 */
		public NotEquivalentReason(NotEquivalent reason) {
			this.reason = reason;
		}

		/**
		 * Constructs a NotEquivalentReason with the specified reason and property.
		 * 
		 * @param reason the reason why the objects are not equivalent
		 * @param property the property descriptor associated with the reason
		 */
		public NotEquivalentReason(NotEquivalent reason, PropertyDescriptor property) {
			this(reason);
			this.property = property;
		}

		/**
		 * @return the reason why the objects are not equivalent
		 */
		public NotEquivalent getReason() {
			return reason;
		}

		/**
		 * @param reason the reason to set
		 */
		public void setReason(NotEquivalent reason) {
			this.reason = reason;
		}

		/**
		 * @return the property
		 */
		public PropertyDescriptor getProperty() {
			return property;
		}

		/**
		 * @param property the property to set
		 */
		public void setProperty(PropertyDescriptor property) {
			this.property = property;
		}

}
