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


/**
 * Support to execute CMIS queries
 * 
 * @author andyh
 *
 */
public interface CMISQueryService
{
    /**
     * Execute a CMIS query as defined by options 
     * 
     * @param options
     * @return a result set
     */
    public CMISResultSet query(CMISQueryOptions options);
    
    /**
     * Execute a CMIS query with all the default options;
     * 
     * @param query
     * @return
     */
    public CMISResultSet query(String query);
    

    /**
     * Get the query support level
     */
    public CMISQueryEnum getQuerySupport();
    
    /**
     * Get the join support level in queries.
     */
    public CMISJoinEnum getJoinSupport();
    
    /**
     * Can you query Private Working Copies of a document.
     * 
     * @return
     */
    public boolean getPwcSearchable();
    
    /**
     * Can you query non-latest versions of a document. 
     * The current latest version is always searchable according to the type definition.
     * 
     * @return
     */
    public boolean getAllVersionsSearchable();
}
