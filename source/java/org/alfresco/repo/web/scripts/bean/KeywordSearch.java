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
package org.alfresco.repo.web.scripts.bean;

import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.web.scripts.RepositoryImageResolver;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.QueryParser;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * Alfresco Keyword (simple) Search Service
 * 
 * @author davidc
 */
public class KeywordSearch extends DeclarativeWebScript
{
    // Logger
    private static final Log logger = LogFactory.getLog(KeywordSearch.class);

    // search parameters 
    // TODO: allow configuration of search store
    protected static final StoreRef SEARCH_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    protected static final int DEFAULT_ITEMS_PER_PAGE = 10;
    protected static final String QUERY_FORMAT = "query_";

    // dependencies
    protected ServiceRegistry serviceRegistry;
    protected RepositoryImageResolver imageResolver;
    protected SearchService searchService;
    protected NodeService nodeService;

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setRepositoryImageResolver(RepositoryImageResolver imageResolver)
    {
        this.imageResolver = imageResolver;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        //
        // process arguments
        //
        
        String searchTerms = req.getParameter("q");
        ParameterCheck.mandatoryString("q", searchTerms);
        String startPageArg = req.getParameter("p");
        int startPage = 1;
        try
        {
            startPage = new Integer(startPageArg);
        }
        catch(NumberFormatException e)
        {
            // NOTE: use default startPage
        }
        String itemsPerPageArg = req.getParameter("c");
        int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
        try
        {
            itemsPerPage = new Integer(itemsPerPageArg);
        }
        catch(NumberFormatException e)
        {
            // NOTE: use default itemsPerPage
        }
        Locale locale = I18NUtil.getLocale();
        String language = req.getParameter("l");
        if (language != null && language.length() > 0)
        {
            // NOTE: Simple conversion from XML Language Id to Java Locale Id
            locale = new Locale(language.replace("-", "_"));
        }
        
        //
        // execute the search
        //
        
        SearchResult results = search(searchTerms, startPage, itemsPerPage, locale, req);
        
        //
        // create model
        //
        
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        model.put("search", results);
        return model;
    }
    
    /**
     * Execute the search
     */
    private SearchResult search(String searchTerms, int startPage, int itemsPerPage, Locale locale, WebScriptRequest req)
    {
        SearchResult searchResult = null;
        ResultSet results = null;
        
        try
        {
            // construct search statement
            String[] terms = searchTerms.split(" "); 
            searchTerms = searchTerms.replaceAll("\"", "&quot;"); 

            // Escape special characters in the terms, so that they can't confuse the parser 
            for (int i=0; i<terms.length; i++) 
            { 
                terms[i] = QueryParser.escape(terms[i]); 
            } 

            Map<String, Object> statementModel = new HashMap<String, Object>(7, 1.0f);
            statementModel.put("args", createArgs(req));
            statementModel.put("terms", terms);
            Writer queryWriter = new StringWriter(1024);
            renderFormatTemplate(QUERY_FORMAT, statementModel, queryWriter);
            String query = queryWriter.toString();
            
            // execute query
            if (logger.isDebugEnabled())
            {
                logger.debug("Search parameters: searchTerms=" + searchTerms + ", startPage=" + startPage + ", itemsPerPage=" + itemsPerPage + ", search locale=" + locale.toString());
                logger.debug("Issuing lucene search: " + query);
            }

            SearchParameters parameters = new SearchParameters();
            parameters.addStore(SEARCH_STORE);
            parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
            parameters.setQuery(query);
            if (locale != null)
            {
                parameters.addLocale(locale);
            }
            results = searchService.query(parameters);
            int totalResults = results.length();
            
            if (logger.isDebugEnabled())
                logger.debug("Results: " + totalResults + " rows (limited: " + results.getResultSetMetaData().getLimitedBy() + ")");
            
            // are we out-of-range
            int totalPages = (totalResults / itemsPerPage);
            totalPages += (totalResults % itemsPerPage != 0) ? 1 : 0;
            if (totalPages != 0 && (startPage < 1 || startPage > totalPages))
            {
                throw new WebScriptException("Start page " + startPage + " is outside boundary of " + totalPages + " pages");
            }

            // construct search result
            searchResult = new SearchResult();
            searchResult.setSearchTerms(searchTerms);
            searchResult.setLocale(locale);
            searchResult.setItemsPerPage(itemsPerPage);
            searchResult.setStartPage(startPage);
            searchResult.setTotalResults(totalResults);
            if (totalResults == 0)
            {
                searchResult.setTotalPages(0);
                searchResult.setStartIndex(0);
                searchResult.setTotalPageItems(0);
            }
            else
            {
                searchResult.setTotalPages(totalPages);
                searchResult.setStartIndex(((startPage -1) * itemsPerPage) + 1);
                searchResult.setTotalPageItems(Math.min(itemsPerPage, totalResults - searchResult.getStartIndex() + 1));
            }
            SearchTemplateNode[] nodes = new SearchTemplateNode[searchResult.getTotalPageItems()];
            for (int i = 0; i < searchResult.getTotalPageItems(); i++)
            {
                NodeRef node = results.getNodeRef(i + searchResult.getStartIndex() - 1);
                // Make the search resilient to invalid nodes
                if (!nodeService.exists(node))
                {
                    continue;
                }
                float score = results.getScore(i + searchResult.getStartIndex() - 1);
                nodes[i] = new SearchTemplateNode(node, score);
            }
            searchResult.setResults(nodes);
            return searchResult;
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }        
    }

    /**
     * Search Result
     *  
     * @author davidc
     */
    public static class SearchResult
    {
        private String id;
        private String searchTerms;
        private Locale locale;
        private int itemsPerPage;
        private int totalPages;
        private int totalResults;
        private int totalPageItems;
        private int startPage;
        private int startIndex;
        private SearchTemplateNode[] results;
        
        
        public int getItemsPerPage()
        {
            return itemsPerPage;
        }
        
        /*package*/ void setItemsPerPage(int itemsPerPage)
        {
            this.itemsPerPage = itemsPerPage;
        }

        public TemplateNode[] getResults()
        {
            return results;
        }

        /*package*/ void setResults(SearchTemplateNode[] results)
        {
            this.results = results;
        }

        public int getStartIndex()
        {
            return startIndex;
        }

        /*package*/ void setStartIndex(int startIndex)
        {
            this.startIndex = startIndex;
        }

        public int getStartPage()
        {
            return startPage;
        }

        /*package*/ void setStartPage(int startPage)
        {
            this.startPage = startPage;
        }

        public int getTotalPageItems()
        {
            return totalPageItems;
        }

        /*package*/ void setTotalPageItems(int totalPageItems)
        {
            this.totalPageItems = totalPageItems;
        }

        public int getTotalPages()
        {
            return totalPages;
        }

        /*package*/ void setTotalPages(int totalPages)
        {
            this.totalPages = totalPages;
        }

        public int getTotalResults()
        {
            return totalResults;
        }

        /*package*/ void setTotalResults(int totalResults)
        {
            this.totalResults = totalResults;
        }

        public String getSearchTerms()
        {
            return searchTerms;
        }

        /*package*/ void setSearchTerms(String searchTerms)
        {
            this.searchTerms = searchTerms;
        }

        public Locale getLocale()
        {
            return locale;
        }
        
        /**
         * @return XML 1.0 Language Identification
         */
        public String getLocaleId()
        {
            return locale.toString().replace('_', '-');
        }

        /*package*/ void setLocale(Locale locale)
        {
            this.locale = locale;
        }

        public String getId()
        {
            if (id == null)
            {
                id = GUID.generate();
            }
            return id;
        }
    }
    
    /**
     * Search result row template node
     */
    public class SearchTemplateNode extends TemplateNode
    {
        protected final static String URL = "/api/node/content/{0}/{1}/{2}/{3}";
        
        private static final long serialVersionUID = -1791913270786140012L;
        private float score;

        /**
         * Construct
         */
        public SearchTemplateNode(NodeRef nodeRef, float score)
        {
            super(nodeRef, serviceRegistry, KeywordSearch.this.imageResolver.getImageResolver());
            this.score = score;
        }
        
        /**
         * Gets the result row score
         */
        public float getScore()
        {
            return score;
        }

        @Override
        public String getUrl()
        {
            return MessageFormat.format(URL, new Object[] {
                getNodeRef().getStoreRef().getProtocol(),
                getNodeRef().getStoreRef().getIdentifier(),
                getNodeRef().getId(),
                URLEncoder.encode(getName()) } );
        }
    }
}