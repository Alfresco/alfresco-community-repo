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
package org.alfresco.service.cmr.search;


/**
 * This is the common interface for both row (Alfresco node) and column (CMIS style property or function) based results.
 * The meta-data for the results sets contains the detailed info on what columns are available. For row based result
 * sets there is no selector - all the nodes returned do not have to have a specific type or aspect. For example, an FTS
 * search on properties of type d:content has no type constraint implied or otherwise. Searches against properties have
 * an implied type, but as there can be more than one property -> more than one type or aspect implied (eg via OR in FTS
 * or lucene) they are ignored An iterable result set from a searcher query.<b/> Implementations must implement the
 * indexes for row lookup as zero-based.<b/>
 * 
 * @author andyh
 */
public interface ResultSet extends ResultSetSPI<ResultSetRow, ResultSetMetaData> // Specific iterator over ResultSetRows
{

}
