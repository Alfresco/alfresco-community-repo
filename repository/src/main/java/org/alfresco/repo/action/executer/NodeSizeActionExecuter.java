/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.action.executer;

import org.alfresco.repo.action.ParameterizedItemAbstractBase;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *   NodeSizeActionExecuter
 *   Executing Alfresco FTS Query to find size of Folder Node
 */

public class NodeSizeActionExecuter extends ActionExecuterAbstractBase
{

    private static final Logger LOG = LoggerFactory.getLogger(NodeSizeActionExecuter.class);

    /**
     * Action constants
     */
    public static final String NAME = "folder-size";
    private static final String EXCEPTION = "Exception";
    public static final String DEFAULT_SIZE = "default-size";
    private static final String FIELD_FACET = "content.size";
    private static final String FACET_QUERY = "content.size:[0 TO "+Integer.MAX_VALUE+"] \"label\": \"large\",\"group\":\"Size\"";
    private SearchService searchService;
    private SimpleCache<Serializable,Object> simpleCache;

    /**
     * Set the search service
     *
     * @param searchService  the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Set the simpleCache service
     *
     * @param simpleCache  the cache service
     */
    public void setSimpleCache(SimpleCache<Serializable, Object> simpleCache)
    {
        this.simpleCache = simpleCache;
    }

    /**
     * @see ActionExecuter#execute(Action, NodeRef)
     */
    @Override
    public void executeImpl(Action nodeAction, NodeRef actionedUponNodeRef)
    {
        Serializable serializable = nodeAction.getParameterValue(DEFAULT_SIZE);
        int defaultItems;
        Map<String,Object> response = new HashMap<>();

        try
        {
            defaultItems = Integer.parseInt(serializable.toString());
        }
        catch (NumberFormatException numberFormatException)
        {
            LOG.error("Exception occurred while parsing String to INT: {}", numberFormatException.getMessage());
            response.put(EXCEPTION,numberFormatException.getMessage());
            simpleCache.put(actionedUponNodeRef.getId(),response);
            throw numberFormatException;
        }

        NodeRef nodeRef = actionedUponNodeRef;
        long totalSizeFromFacet = 0;
        int skipCount = 0;
        ResultSet results;
        int totalItems;
        boolean isCalculationCompleted = false;

        try
        {
            // executing Alfresco FTS facet query.
            results = facetQuery(nodeRef);
            totalItems = Math.min(results.getFieldFacet(FIELD_FACET).size(), defaultItems);
            // Using AtomicLong to accumulate the total size.
            AtomicLong resultSize = new AtomicLong(0);

            while (!isCalculationCompleted)
            {
                List<Pair<String, Integer>> pairSizes = results.getFieldFacet(FIELD_FACET).subList(skipCount, totalItems);
                pairSizes.parallelStream().forEach(id -> {
                    try
                    {
                        if(id.getSecond()>0)
                        {
                            resultSize.addAndGet(Long.valueOf(id.getFirst()) * id.getSecond());
                        }
                    }
                    catch (Exception e)
                    {
                        resultSize.addAndGet(0);
                    }
                });

                totalSizeFromFacet+=resultSize.longValue();
                resultSize.set(0);

                if (results.getFieldFacet(FIELD_FACET).size() <= totalItems || results.getFieldFacet(FIELD_FACET).size() <= defaultItems)
                {
                    isCalculationCompleted = true;
                }
                else
                {
                    skipCount += defaultItems;
                    int remainingItems = results.getFieldFacet(FIELD_FACET).size() - totalItems;
                    totalItems += Math.min(remainingItems, defaultItems);
                }
            }
        }
        catch (RuntimeException runtimeException)
        {
            LOG.error("Exception occurred in NodeSizeActionExecutor:results {}", runtimeException.getMessage());
            response.put(EXCEPTION,runtimeException.getMessage());
            simpleCache.put(nodeRef.getId(),response);
            throw runtimeException;
        }

        LOG.debug(" Calculating size of Folder Node - NodeSizeActionExecutor:executeImpl ");
        final LocalDateTime eventTimestamp = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        String formattedTimestamp = eventTimestamp.format(formatter);
        response.put("nodeId", nodeRef.getId());
        response.put("size", totalSizeFromFacet);
        response.put("calculatedAt", formattedTimestamp);
        response.put("numberOfFiles", results != null ? results.getNodeRefs().size() : 0);
        if(isCalculationCompleted)
        {
            simpleCache.put(nodeRef.getId(),response);
        }
    }

    protected ResultSet facetQuery(NodeRef nodeRef)
    {
        StringBuilder aftsQuery = new StringBuilder();
        aftsQuery.append("ANCESTOR:\"").append(nodeRef).append("\" AND TYPE:content");
        String query = aftsQuery.toString();

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.setQuery(query);

        searchParameters.addFacetQuery(FACET_QUERY);
        final SearchParameters.FieldFacet ff = new SearchParameters.FieldFacet(FIELD_FACET);
        searchParameters.addFieldFacet(ff);
        return searchService.query(searchParameters);
    }

    /**
     * @see ParameterizedItemAbstractBase#addParameterDefinitions(List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // Intentionally empty.
    }
}
