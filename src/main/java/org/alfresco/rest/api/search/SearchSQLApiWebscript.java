/*-
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.search;
import java.io.IOException;
import java.util.Locale;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.search.impl.solr.SolrSQLJSONResultSet;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSet;
import org.alfresco.rest.api.search.impl.ResultMapper;
import org.alfresco.rest.api.search.impl.SearchMapper;
import org.alfresco.rest.api.search.model.SearchSQLQuery;
import org.alfresco.rest.api.search.model.TupleList;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;
import org.alfresco.rest.framework.tools.RequestReader;
import org.alfresco.rest.framework.tools.ResponseWriter;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


/**
 * Search SQL API end point.
 * An implementation of the {{baseUrl}}/{{networkId}}/public/search/versions/1/sql
 * @author Michael Suzuki
 *
 */
public class SearchSQLApiWebscript extends AbstractWebScript implements RecognizedParamsExtractor,
                                                                        RequestReader,
                                                                        ResponseWriter,
                                                                        InitializingBean
{
    private ServiceRegistry serviceRegistry;
    private SearchService searchService;
    private SearchMapper searchMapper;
    private ResultMapper resultMapper;
    protected ApiAssistant assistant;
    protected ResourceWebScriptHelper helper;
    
    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse res) throws IOException
    {
        try
        {
            //Turn JSON into a Java object representation
            SearchSQLQuery searchQuery = extractJsonContent(webScriptRequest, assistant.getJsonHelper(), SearchSQLQuery.class);
            SearchParameters sparams = buildSearchParameters(searchQuery);

            ResultSet results = searchService.query(sparams);
            FilteringResultSet frs = (FilteringResultSet) results;
            SolrSQLJSONResultSet ssjr = (SolrSQLJSONResultSet) frs.getUnFilteredResultSet();
            //When solr format is requested pass the solr output directly.
            if(searchQuery.getFormat().equalsIgnoreCase("solr"))
            {
                res.getWriter().write(ssjr.getSolrResponse());
            }
            else
            {
                CollectionWithPagingInfo<TupleList> nodes = resultMapper.toCollectionWithPagingInfo(ssjr.getDocs(), searchQuery);
                renderJsonResponse(res, nodes, assistant.getJsonHelper());
            }
            setResponse(res, DEFAULT_SUCCESS);
        }
        catch (Exception exception) 
        {
            if (exception instanceof LuceneQueryParserException)
            {
                renderException(exception,res,assistant);
            }
            else
            {
                renderException(new WebScriptException(400, exception.getMessage()), res, assistant);
            }
        }
    }
    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "serviceRegistry", serviceRegistry);
        this.searchService = serviceRegistry.getSearchService();
        ParameterCheck.mandatory("assistant", this.assistant);
        ParameterCheck.mandatory("searchMapper", this.searchMapper);
        ParameterCheck.mandatory("resultMapper", this.resultMapper);
    }
    
    public SearchParameters buildSearchParameters(SearchSQLQuery searchQuery)
    {
        SearchParameters sparams = new SearchParameters();
        sparams.setLanguage(SearchService.LANGUAGE_SOLR_SQL);
        sparams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        if(StringUtils.isEmpty(searchQuery.getStmt()))
        {
            throw new AlfrescoRuntimeException("Required stmt parameter is missing.");
        }
        if(searchQuery.getFormat().equalsIgnoreCase("solr"))
        {
            sparams.addExtraParameter("format", "solr");
        }
        if(!StringUtils.isEmpty(searchQuery.getTimezone()))
        {
            sparams.setTimezone(searchQuery.getTimezone());
        }
        sparams.setQuery(searchQuery.getStmt());
        searchQuery.getLocales().forEach(action->{
            Locale locale = new Locale(action);
            sparams.addLocale(locale);
        });
        searchQuery.getFilterQueries().forEach(sparams::addFilterQuery);
        
        sparams.setIncludeMetadata(searchQuery.isIncludeMetadata());
        return sparams;
    }
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setSearchMapper(SearchMapper searchMapper)
    {
        this.searchMapper = searchMapper;
    }

    public void setResultMapper(ResultMapper resultMapper)
    {
        this.resultMapper = resultMapper;
    }

    public void setAssistant(ApiAssistant assistant)
    {
        this.assistant = assistant;
    }

    public void setHelper(ResourceWebScriptHelper helper)
    {
        this.helper = helper;
    }

}
