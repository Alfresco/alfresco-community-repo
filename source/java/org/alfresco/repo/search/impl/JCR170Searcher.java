/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.search.impl;

import org.alfresco.repo.search.AbstractSearcherComponent;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;

/**
 * Simple searcher against another store using the JSR 170 API.
 * <p>
 * This class is not fully implemented and hence still abstract.
 */
public abstract class JCR170Searcher extends AbstractSearcherComponent
{
    public ResultSet query(StoreRef store, String language, String query, Path[] queryOptions,
            QueryParameter[] queryParameters)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public ResultSet query(StoreRef store, String language, String query, Path[] attributePaths, QueryParameterDefinition[] queryParameterDefinitions)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public ResultSet query(StoreRef store, QName queryId, QueryParameter[] queryParameters)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public ResultSet query(SearchParameters searchParameters)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }
}
