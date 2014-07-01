/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.search.StatsResultSet;
import org.alfresco.service.cmr.search.StatsService;

/**
 * Uses solr to retrieve stats about your content.
 *
 * @author Gethin James
 * @since 5.0
 */
public class SolrStatsService implements StatsService
{
    private IndexerAndSearcher searcher;
    
    public void setSearcher(IndexerAndSearcher searcher)
    {
        this.searcher = searcher;
    }

    @Override
    public StatsResultSet query(StatsParameters searchParameters)
    {
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        
        LuceneQueryLanguageSPI language = searcher.getQueryLanguages().get(searchParameters.getLanguage().toLowerCase());
        if (language != null && SearchService.LANGUAGE_SOLR_FTS_ALFRESCO.equals(language.getName()))
        {
            SolrQueryLanguage solr = (SolrQueryLanguage) language;
            return solr.executeStatsQuery(searchParameters);
        }
        else
        {
            throw new SearcherException("Unknown stats query language: " + searchParameters.getLanguage());
        }
    }   
    
}
