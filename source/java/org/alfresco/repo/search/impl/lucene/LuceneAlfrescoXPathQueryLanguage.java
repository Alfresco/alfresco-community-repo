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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.repo.search.impl.parsers.FTSParser;
import org.alfresco.repo.search.impl.parsers.FTSQueryParser;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Score;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneOrdering;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.saxpath.SAXPathException;

import com.werken.saxpath.XPathReader;

/**
 * Alfresco FTS Query language support
 * 
 * @author andyh
 */
public class LuceneAlfrescoXPathQueryLanguage extends AbstractLuceneQueryLanguage
{
    public LuceneAlfrescoXPathQueryLanguage()
    {
        this.setName(SearchService.LANGUAGE_XPATH);
    }

    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        try
        {
            XPathReader reader = new XPathReader();
            LuceneXPathHandler handler = new LuceneXPathHandler();
            handler.setNamespacePrefixResolver(admLuceneSearcher.getNamespacePrefixResolver());
            handler.setDictionaryService(admLuceneSearcher.getDictionaryService());
            // TODO: Handler should have the query parameters to use in
            // building its lucene query
            // At the moment xpath style parameters in the PATH
            // expression are not supported.
            reader.setXPathHandler(handler);
            reader.parse(searchParameters.getQuery());
            Query query = handler.getQuery();
            Searcher searcher = admLuceneSearcher.getClosingIndexSearcher();
            if (searcher == null)
            {
                // no index return an empty result set
                return new EmptyResultSet();
            }
            Hits hits = searcher.search(query);
            ResultSet rs = new LuceneResultSet(hits, searcher, admLuceneSearcher.getNodeService(), admLuceneSearcher.getTenantService(), searchParameters, admLuceneSearcher.getLuceneConfig());
            rs = new PagingLuceneResultSet(rs, searchParameters, admLuceneSearcher.getNodeService());
            return rs;
        }
        catch (SAXPathException e)
        {
            throw new SearcherException("Failed to parse query: " + searchParameters.getQuery(), e);
        }
        catch (IOException e)
        {
            throw new SearcherException("IO exception during search", e);
        }
    }
}
