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
package org.alfresco.repo.search.impl.lucene;

import java.util.List;

import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.apache.lucene.search.Searcher;

/**
 * @author andyh
 *
 */
public interface LuceneQueryLanguageSPI
{
    /**
     * The unique name for the query language
     * @return - the unique name
     */
    public String getName();
    
    /**
     * Execute the query
     * @param searchParameters 
     * @param admLuceneSearcher 
     * @return - the query results
     */
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher);
    
    /**
     * Register
     */
    public void setFactories(List<AbstractLuceneIndexerAndSearcherFactory> factories);
}
