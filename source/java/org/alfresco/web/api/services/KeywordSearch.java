/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.api.services;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.GUID;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.api.APIException;
import org.alfresco.web.api.APIRequest;
import org.alfresco.web.api.APIResponse;
import org.alfresco.web.api.APIRequest.HttpMethod;
import org.alfresco.web.api.APIRequest.RequiredAuthentication;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Alfresco Keyword (simple) Search Service
 * 
 * @author davidc
 */
public class KeywordSearch extends APIServiceTemplateImpl
{
    // Logger
    private static final Log logger = LogFactory.getLog(KeywordSearch.class);

    // search parameters 
    // TODO: allow configuration of search store
    protected static final StoreRef SEARCH_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    protected static final int DEFAULT_ITEMS_PER_PAGE = 10;
    protected static final String QUERY_TEMPLATE_TYPE = "query";

    // dependencies
    protected SearchService searchService;

    // icon resolver
    protected TemplateImageResolver iconResolver = new TemplateImageResolver()
    {
        public String resolveImagePathForName(String filename, boolean small)
        {
            return Utils.getFileTypeImage(getAPIContext(), filename, small);
        }
    };

    /**
     * @param searchService
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getRequiredAuthentication()
     */
    public RequiredAuthentication getRequiredAuthentication()
    {
        return APIRequest.RequiredAuthentication.User;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getHttpMethod()
     */
    public HttpMethod getHttpMethod()
    {
        return APIRequest.HttpMethod.GET;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getDefaultFormat()
     */
    public String getDefaultFormat()
    {
        return APIResponse.HTML_FORMAT;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.api.APIService#getDescription()
     */
    public String getDescription()
    {
        return "Issue an Alfresco Web Client keyword search";
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.services.APIServiceTemplateImpl#createModel(org.alfresco.web.api.APIRequest, org.alfresco.web.api.APIResponse, java.util.Map)
     */
    @Override
    protected Map<String, Object> createModel(APIRequest req, APIResponse res, Map<String, Object> model)
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
        
        SearchResult results = search(searchTerms, startPage, itemsPerPage, locale);
        
        //
        // append to model
        //
        
        model.put("search", results);
        return model;
    }
    
    /**
     * Execute the search
     * 
     * @param searchTerms The search terms
     * @param startPage The page number to start at
     * @param itemsPerPage The number of items to show on a page
     * @param locale The current locale
     * @return The search result
     */
    private SearchResult search(String searchTerms, int startPage, int itemsPerPage, Locale locale)
    {
        SearchResult searchResult = null;
        ResultSet results = null;
        
        try
        {
            // construct search statement
            String[] terms = searchTerms.split(" "); 
            Map<String, Object> statementModel = new HashMap<String, Object>(7, 1.0f);
            statementModel.put("terms", terms);
            Writer queryWriter = new StringWriter(1024);
            renderTemplate(QUERY_TEMPLATE_TYPE, null, statementModel, queryWriter);
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
                throw new APIException("Start page " + startPage + " is outside boundary of " + totalPages + " pages");
            }

            // construct search result
            searchResult = new SearchResult();
            searchResult.setSearchTerms(searchTerms);
            searchResult.setLocale(locale);
            searchResult.setItemsPerPage(itemsPerPage);
            searchResult.setStartPage(startPage);
            searchResult.setTotalPages(totalPages);
            searchResult.setTotalResults(totalResults);
            searchResult.setStartIndex(((startPage -1) * itemsPerPage) + 1);
            searchResult.setTotalPageItems(Math.min(itemsPerPage, totalResults - searchResult.getStartIndex() + 1));
            SearchTemplateNode[] nodes = new SearchTemplateNode[searchResult.getTotalPageItems()];
            for (int i = 0; i < searchResult.getTotalPageItems(); i++)
            {
                NodeRef node = results.getNodeRef(i + searchResult.getStartIndex() - 1);
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
        private static final long serialVersionUID = -1791913270786140012L;
        private float score;

        /**
         * Construct
         * 
         * @param nodeRef
         * @param score
         */
        public SearchTemplateNode(NodeRef nodeRef, float score)
        {
            super(nodeRef, getServiceRegistry(), iconResolver);
            this.score = score;
        }
        
        /**
         * Gets the result row score
         * 
         * @return  score
         */
        public float getScore()
        {
            return score;
        }
    }
        
        
    /**
     * Simple test that can be executed outside of web context
     */
    public static void main(String[] args)
        throws Exception
    {
        KeywordSearch service = (KeywordSearch)APIServiceImpl.getMethod("web.api.KeywordSearch");
        service.test(APIResponse.HTML_FORMAT);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.api.services.APIServiceImpl#createTestModel()
     */
    @Override
    protected Map<String, Object> createTestModel()
    {
        Map<String, Object> model = super.createTestModel();
        SearchResult result = search("alfresco tutorial", 1, 5, I18NUtil.getLocale());
        model.put("search", result);
        return model;
    }
    
}