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

import org.alfresco.service.cmr.search.ResultSetSPI;



/**
 * A CMIS result set
 * 
 * @author andyh
 */
public interface CMISResultSet extends ResultSetSPI<CMISResultSetRow, CMISResultSetMetaData>
{
    /**
     * Get the result set meta-data.
     * @return the metadata
     */
    public CMISResultSetMetaData getMetaData();
    
    
    /**
     * Get the number of rows in this result set.
     * 
     * This will be less than or equal to the maximum number of rows requested or
     * the full length if no restriction on length is specified.
     * 
     * If a skip count is given, the length represents the number of results
     * after the skip count and does not include the items skipped.
     * 
     * @return the length
     */
    public int getLength();
    
  
}
