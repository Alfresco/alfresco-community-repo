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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.search;

import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.QName;

public interface QueryRegisterComponent
{
    /**
     * Get a query defintion by Qname
     * 
     * @param qName
     * @return
     */
    public CannedQueryDef getQueryDefinition(QName qName);
    
    /**
     * Get the name of the collection containing a query
     * 
     * @param qName
     * @return
     */
    public String getCollectionNameforQueryDefinition(QName qName);
    
    /**
     * Get a parameter definition
     * 
     * @param qName
     * @return
     */
    public QueryParameterDefinition getParameterDefinition(QName qName);
    
    /**
     * Get the name of the collection containing a parameter definition
     * 
     * @param qName
     * @return
     */
    public String getCollectionNameforParameterDefinition(QName qName);
    
    
    /**
     * Get a query collection by name
     * 
     * @param name
     * @return
     */
    public QueryCollection getQueryCollection(String name);
    
    
    /**
     * Load a query collection
     * 
     * @param location
     */
    public void loadQueryCollection(String location);
}
