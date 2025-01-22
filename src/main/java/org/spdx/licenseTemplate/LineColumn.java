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
package org.spdx.licenseTemplate;

/**
 * Holds information on lines and columns
 * @author Gary O'Neall
 *
 */
public class LineColumn {
	private int line;
	private int column;
	private int len;
	
	public LineColumn(int line, int column,int len) {
		this.line = line;
		this.column = column;
		this.len = len;
	}

	/**
	 * @return line number
	 */
	public int getLine() {
		return line;
	}

	/**
	 * @param line line number
	 */
	public void setLine(int line) {
		this.line = line;
	}

	/**
	 * @return column
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * @param column column
	 */
	@SuppressWarnings("unused")
    public void setColumn(int column) {
		this.column = column;
	}

	/**
	 * @return length of the line
	 */
	public int getLen() {
		return len;
	}

	@SuppressWarnings("unused")
    public void setLen(int len) {
		this.len = len;
	}		
}
