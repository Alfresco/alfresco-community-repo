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

import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

public interface QueryCollection
{
    /**
     * The name of the query collection
     * 
     * @return
     */
    public String getName();
    
    /**
     * Does this collection contain a query for the given QName?
     * @param qName
     * @return
     */
    public boolean containsQueryDefinition(QName qName);
    
    /**
     * Get a query definition by QName.
     * @param qName
     * @return
     */
    public CannedQueryDef getQueryDefinition(QName qName);
    
    /**
     * Does this collection contain a query for the given QName?
     * @param qName
     * @return
     */
    public boolean containsParameterDefinition(QName qName);
    
    /**
     * Get a query definition by QName.
     * @param qName
     * @return
     */
    public QueryParameterDefinition getParameterDefinition(QName qName);
    
    /**
     * Return the mechanism that this query definition uses to map namespace prefixes to URIs.
     * A query may use a predefined set of prefixes for known URIs.
     * I would be unwise to rely on the defaults.
     *  
     * @return
     */
    public NamespacePrefixResolver getNamespacePrefixResolver();
    
}
