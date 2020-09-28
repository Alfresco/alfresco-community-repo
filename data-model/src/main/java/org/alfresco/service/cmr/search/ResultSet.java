/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.search;

import org.alfresco.api.AlfrescoPublicApi;


/**
 * This is the common interface for both row (Alfresco node) and column (CMIS style property or function) based results.
 * The meta-data for the results sets contains the detailed info on what columns are available. For row based result
 * sets there is no selector - all the nodes returned do not have to have a specific type or aspect. For example, an FTS
 * search on properties of type d:content has no type constraint implied or otherwise. Searches against properties have
 * an implied type, but as there can be more than one property -> more than one type or aspect implied (eg via OR in FTS
 * or lucene) they are ignored An iterable result set from a searcher query.<b> Implementations must implement the
 * indexes for row lookup as zero-based.<b>
 * 
 * @author andyh
 */
@AlfrescoPublicApi
public interface ResultSet extends ResultSetSPI<ResultSetRow, ResultSetMetaData> // Specific iterator over ResultSetRows
{


}
