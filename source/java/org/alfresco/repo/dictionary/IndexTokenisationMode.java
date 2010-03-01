/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.dictionary;

/**
 * How tokenisation is supported in the index.
 * 
 * 
 * @author andyh
 * 
 */
public enum IndexTokenisationMode {
	/**
	 * Tokenise the property. If the analyser supported ordering then the field
	 * supports ordering FTS is supported via analysis.
	 */
	TRUE,
	/**
	 * Do not tokenise the property. The field supports ordering and pattern
	 * matching.
	 */
	FALSE,
	/**
	 * There may be two indexes - one to support ordering and one to support
	 * search.
	 */
	BOTH;

	public static String serializer(IndexTokenisationMode indexTokenisationMode) {
		return indexTokenisationMode.toString();
	}

	public static IndexTokenisationMode deserializer(String value) {
		if (value == null) {
			return null;
		} else if (value.equalsIgnoreCase(TRUE.toString())) {
			return TRUE;
		} else if (value.equalsIgnoreCase(FALSE.toString())) {
			return FALSE;
		} else if (value.equalsIgnoreCase(BOTH.toString())) {
			return BOTH;
		} else {
			throw new IllegalArgumentException(
					"Invalid IndexTokenisationMode: " + value);
		}
	}
}
