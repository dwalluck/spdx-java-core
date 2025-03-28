/**
 * Copyright (c) 2019 Source Auditor Inc.
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

/**
 * Implementation classes of the <code>IndividualUriValue</code> stores
 * a single URI value
 * <p>
 * These classes must NOT implement any properties themselves.
 * Any such properties will be lost during storage and retrieval.
 * 
 * @author Gary O'Neall
 */
public interface IndividualUriValue {
	
	/**
	 * @return a unique identifier for this value.  Typically the namespace + the long name
	 */
    String getIndividualURI();
}
