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
package org.alfresco.cmis;

import org.alfresco.service.cmr.search.ResultSetMetaData;


/**
 * The meta data associated with a result set
 * 
 * @author andyh
 *
 */
public interface CMISResultSetMetaData extends ResultSetMetaData
{
    /**
     * The selector meta-data.
     * @return - the selector meta-data.
     */
    public CMISResultSetSelector[] getSelectors();
    
    /**
     * The column meta-data.
     * @return - the column meta-data.
     */
    public CMISResultSetColumn[] getColumns();
    
    /**
     * Get the query options used to create this result set
     * @return the query options
     */
    public CMISQueryOptions getQueryOptions();
    
    /**
     * Get the names of the selectors.
     * @return - the selector names.
     */
    public String[] getSelectorNames();
    
    /**
     * Get the column names.
     * @return - the names of the columns.
     */
    public String[] getColumnNames();
    
    /**
     * Get the selector meta-data by name.
     * @param name
     * @return - the selector meta-data.
     */
    public CMISResultSetSelector getSelector(String name);
    
    /**
     * Get the column meta-data by column name.
     * @param name
     * @return - the column meta-data.
     */
    public CMISResultSetColumn getColumn(String name);
}
