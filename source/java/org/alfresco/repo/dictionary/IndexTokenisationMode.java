/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
