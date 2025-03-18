/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.search;

import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.QName;

public interface QueryRegisterComponent
{
    /**
     * Get a query defintion by Qname
     * 
     * @param qName
     *            QName
     * @return CannedQueryDef
     */
    public CannedQueryDef getQueryDefinition(QName qName);

    /**
     * Get the name of the collection containing a query
     * 
     * @param qName
     *            QName
     * @return String
     */
    public String getCollectionNameforQueryDefinition(QName qName);

    /**
     * Get a parameter definition
     * 
     * @param qName
     *            QName
     * @return QueryParameterDefinition
     */
    public QueryParameterDefinition getParameterDefinition(QName qName);

    /**
     * Get the name of the collection containing a parameter definition
     * 
     * @param qName
     *            QName
     * @return String
     */
    public String getCollectionNameforParameterDefinition(QName qName);

    /**
     * Get a query collection by name
     * 
     * @param name
     *            String
     * @return QueryCollection
     */
    public QueryCollection getQueryCollection(String name);

    /**
     * Load a query collection
     *
     * @param location
     *            String
     */
    public void loadQueryCollection(String location);
}
