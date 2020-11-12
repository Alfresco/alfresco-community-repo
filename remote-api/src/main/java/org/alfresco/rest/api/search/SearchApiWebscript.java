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
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.search.impl.querymodel.impl.db.DBStats;
import org.alfresco.repo.search.impl.querymodel.impl.db.SingleTaskRestartableWatch;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.search.context.SearchRequestContext;
import org.alfresco.rest.api.search.impl.ResultMapper;
import org.alfresco.rest.api.search.impl.SearchMapper;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;
import org.alfresco.rest.framework.tools.RequestReader;
import org.alfresco.rest.framework.tools.ResponseWriter;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;
import org.springframework.util.StopWatch.TaskInfo;

/**
 * An implementation of the {{baseUrl}}/{{networkId}}/public/search/versions/1/search endpoint
 *
 * @author Gethin James
 */
public class SearchApiWebscript extends AbstractWebScript implements RecognizedParamsExtractor, RequestReader, ResponseWriter,
                                                                InitializingBean
{
    protected static final Log logger = LogFactory.getLog(SearchApiWebscript.class);
            
    private ServiceRegistry serviceRegistry;
    private SearchService searchService;
    private SearchMapper searchMapper;
    private ResultMapper resultMapper;
    protected ApiAssistant assistant;
    protected ResourceWebScriptHelper helper;
    private boolean statsEnabled;

    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "serviceRegistry", serviceRegistry);
        this.searchService = serviceRegistry.getSearchService();
        ParameterCheck.mandatory("assistant", this.assistant);
        ParameterCheck.mandatory("searchMapper", this.searchMapper);
        ParameterCheck.mandatory("resultMapper", this.resultMapper);
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException
    {
        StopWatch apiStopWatch = new StopWatch();
        try {
            //Turn JSON into a Java object respresentation
            SearchQuery searchQuery = extractJsonContent(webScriptRequest, assistant.getJsonHelper(), SearchQuery.class);

            //Parse the parameters
            Params params = getParams(webScriptRequest, searchQuery.getFields(), searchQuery.getInclude(), searchQuery.getPaging());

            //Make a copy of the request
            SearchRequestContext searchRequestContext = SearchRequestContext.from(searchQuery);

            //Turn the SearchQuery json into the Java SearchParameters object
            SearchParameters searchParams = searchMapper.toSearchParameters(params, searchQuery, searchRequestContext);

            //Call searchService
            apiStopWatch.start("nodes");
            ResultSet results = searchService.query(searchParams);
            apiStopWatch.stop();

            //Turn solr results into JSON
            apiStopWatch.start("props");
            CollectionWithPagingInfo<Node> resultJson = resultMapper.toCollectionWithPagingInfo(params, searchRequestContext, searchQuery, results);
            
            //Post-process the request and pass in params, eg. params.getFilter()
            Object toRender = helper.processAdditionsToTheResponse(null, null, null, params, resultJson);
            apiStopWatch.stop();
            
            // store execution stats in a special header if enabled
            if (statsEnabled)
            {
                // store execution time in a special header
                StringBuilder sb = new StringBuilder();
    
                sb.append("api={");
                sb.append("tot=").append(apiStopWatch.getTotalTimeMillis()).append("ms,");
                addStopWatchStats(sb, apiStopWatch);
                sb.append("}; ");
    
                sb.append("db={");
                addStopWatchStats(sb, DBStats.queryStopWatch());
                sb.append("}; ");
    
                sb.append("query={");
                addStopWatchStats(sb, DBStats.handlerStopWatch());
                sb.append(",");
                addStopWatchStats(sb, DBStats.aclReadStopWatch());
                sb.append(",");
                addStopWatchStats(sb, DBStats.aclOwnerStopWatch());
                sb.append("}");
    
                webScriptResponse.addHeader("X-Response-Stats", sb.toString());
            }

            //Write response
            setResponse(webScriptResponse, DEFAULT_SUCCESS);
            renderJsonResponse(webScriptResponse, toRender, assistant.getJsonHelper());

        } catch (Exception exception) {
            renderException(exception,webScriptResponse,assistant);
        }
    }

    private void addStopWatchStats(StringBuilder sb, StopWatch watch)
    {
        boolean first = true;
        
        for (TaskInfo task : watch.getTaskInfo()) {
            if (first)
                first = false;
            else
                sb.append(",");
            
            sb.append(task.getTaskName())
            .append("=")
            .append(task.getTimeMillis())
            .append("ms");

            int pc = Math.round(100 * task.getTimeNanos() / watch.getTotalTimeNanos());
            sb.append("(")
            .append(pc).append("%")
            .append(")");
        }
    }

    private void addStopWatchStats(StringBuilder sb, SingleTaskRestartableWatch watch)
    {
        long decimillis = (watch.getTotalTimeMicros()+5)/100;
        double millis = decimillis/10.0;

        sb.append(watch.getName())
        .append("=")
        .append(millis)
        .append("ms");
    }

    /**
     * Gets the Params object, parameters come from the SearchQuery json not the request
     * @param webScriptRequest
     * @param searchQuery
     * @return Params
     */
    protected Params getParams(WebScriptRequest webScriptRequest, List<String> fields, List<String> include, Paging paging)
    {
        if (paging == null)
        {
            paging = Paging.DEFAULT;
        }
        BeanPropertiesFilter filter = null;
        if (fields != null && !fields.isEmpty())
        {
            List<String> selectList = new ArrayList<>(fields.size());
            selectList.addAll(fields);

            if (include != null &&  !include.isEmpty())
            {
                selectList.addAll(include);
            }

          filter = getFilter("", selectList);
        }

        Params.RecognizedParams recognizedParams = new Params.RecognizedParams(null, paging, filter, null, include, null, null, null, false);
        return Params.valueOf(null, recognizedParams, null, webScriptRequest);
    }

    public void setSearchMapper(SearchMapper searchMapper)
    {
        this.searchMapper = searchMapper;
    }

    public void setResultMapper(ResultMapper resultMapper)
    {
        this.resultMapper = resultMapper;
    }

    public void setAssistant(ApiAssistant assistant) {
        this.assistant = assistant;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setHelper(ResourceWebScriptHelper helper)
    {
        this.helper = helper;
    }
    
    // receiving as a string because of known issue: https://jira.spring.io/browse/SPR-9989
    public void setStatsEnabled(String enabled) {
        this.statsEnabled = Boolean.valueOf(enabled);
        logger.info("API stats header: " + (this.statsEnabled ? "enabled" : "disabled"));
    }
    
}
