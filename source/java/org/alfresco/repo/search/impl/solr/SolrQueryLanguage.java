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
package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryLanguage;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * @author Andy
 *
 */
public class SolrQueryLanguage extends AbstractLuceneQueryLanguage
{
    private SolrQueryHTTPClient solrQueryHTTPClient;

    
    
    public void setSolrQueryHTTPClient(SolrQueryHTTPClient solrQueryHTTPClient)
    {
        this.solrQueryHTTPClient = solrQueryHTTPClient;
    }



    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI#executeQuery(org.alfresco.service.cmr.search.SearchParameters, org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl)
     */
    @Override
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
       return solrQueryHTTPClient.executeQuery(searchParameters, getName());
    }

}
