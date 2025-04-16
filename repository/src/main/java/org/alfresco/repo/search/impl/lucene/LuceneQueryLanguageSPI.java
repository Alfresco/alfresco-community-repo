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
package org.alfresco.repo.search.impl.lucene;

import java.util.List;
import java.util.regex.Pattern;

import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * @author andyh
 *
 */
public interface LuceneQueryLanguageSPI
{
    Pattern AFTS_QUERY = Pattern.compile("\\{\\s?!afts\\s?(.*?)\\}(.*)");

    /**
     * The unique name for the query language
     * 
     * @return - the unique name
     */
    public String getName();

    /**
     * Execute the query
     * 
     * @param searchParameters
     *            SearchParameters
     * @return - the query results
     */
    public ResultSet executeQuery(SearchParameters searchParameters);

    /**
     * Register
     */
    public void setFactories(List<IndexerAndSearcher> factories);
}
