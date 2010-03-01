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
package org.alfresco.service.cmr.search;


/**
 * Meta Data associated with a result set.
 * 
 * @author Andy Hind
 */
public interface ResultSetMetaData
{
    
    /**
     * Return how, <b>in fact</b>, the result set was limited.
     * This may not be how it was requested.
     * 
     * If a limit of 100 were requested and there were 100 or less actual results
     * this will report LimitBy.UNLIMITED.
     * 
     * @return
     */
    public LimitBy getLimitedBy();
    
    /**
     * Return how permission evaluations are being made.
     * 
     * @return
     */
    public PermissionEvaluationMode getPermissionEvaluationMode();
    
    /**
     * Get the parameters that were specified to define this search.
     * 
     * @return
     */
    public SearchParameters getSearchParameters();
    
    /**
     * Get the result set type
     * @return
     */
    public ResultSetType getResultSetType();
    
    /**
     * The selector meta-data.
     * @return - the selector meta-data.
     */
    public ResultSetSelector[] getSelectors();
    
    
    /**
     * The column meta-data.
     * @return - the column meta-data.
     */
    public ResultSetColumn[] getColumns();
    
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
    public ResultSetSelector getSelector(String name);
    
    /**
     * Get the column meta-data by column name.
     * @param name
     * @return - the column meta-data.
     */
    public ResultSetColumn getColumn(String name);
    
    
    
}
