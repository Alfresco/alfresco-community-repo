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
package org.alfresco.repo.search;

import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSetColumn;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetSelector;
import org.alfresco.service.cmr.search.ResultSetType;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Simple implementation of result set meta data.
 * 
 * @author Andy Hind
 */
public class SimpleResultSetMetaData implements ResultSetMetaData
{
    private LimitBy limitedBy; 
    
    private PermissionEvaluationMode permissoinEvaluationMode;
    
    private SearchParameters searchParameters;
    
    
    /**
     * Default properties.
     * 
     * @param limitedBy
     * @param permissoinEvaluationMode
     * @param searchParameters
     */
    public SimpleResultSetMetaData(LimitBy limitedBy, PermissionEvaluationMode permissoinEvaluationMode, SearchParameters searchParameters)
    {
        super();
        this.limitedBy = limitedBy;
        this.permissoinEvaluationMode = permissoinEvaluationMode;
        this.searchParameters = searchParameters;
    }

    public LimitBy getLimitedBy()
    {
        return limitedBy;
    }

    public PermissionEvaluationMode getPermissionEvaluationMode()
    {
        return permissoinEvaluationMode;
    }

    public SearchParameters getSearchParameters()
    {
        return searchParameters;
    }

    public ResultSetColumn getColumn(String name)
    {
      throw new UnsupportedOperationException();
    }

    public String[] getColumnNames()
    {
        throw new UnsupportedOperationException();
    }

    public ResultSetColumn[] getColumns()
    {
        throw new UnsupportedOperationException();
    }

    public ResultSetType getResultSetType()
    {
        throw new UnsupportedOperationException();
    }

    public ResultSetSelector getSelector(String name)
    {
        throw new UnsupportedOperationException();
    }

    public String[] getSelectorNames()
    {
        throw new UnsupportedOperationException();
    }

    public ResultSetSelector[] getSelectors()
    {
        throw new UnsupportedOperationException();
    }

}
