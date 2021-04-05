/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

/**
 * Additional metadata ops available for {@link org.alfresco.service.cmr.search.ResultSet} coming from a search engine.
 *
 * @author Gethin James
 * @since 5.0
 * @see SearchEngineResultSet
 */
public interface SearchEngineResultMetadata
{
    /**
     * Returns the query execution time, or put in other words, the amount of
     * time the search engine spent for processing the request.
     *
     * @return the query execution time
     */
    Long getQueryTime();

    /**
     * Total number of items matching a the current query execution.
     *
     * @return the number of items in the search index that matched a query execution.
     */
    long getNumberFound();
}
