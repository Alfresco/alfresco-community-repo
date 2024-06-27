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

import org.alfresco.model.ContentModel;
import org.alfresco.model.FolderSizeModel;
import org.alfresco.repo.action.ParameterizedItemAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
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
    /**
     * Action constants
     */
    public static final String NAME = "folder-size";
    public static final String PAGE_SIZE = "page-size";

    /**
     * The node service
     */
    private NodeService nodeService;
    private SearchService searchService;

    /**
     * The logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(NodeSizeActionExecuter.class);

    /**
     * Set the node service
     *
     * @param nodeService  the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

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
     * @see ActionExecuter#execute(Action, NodeRef)
     */
    @Override
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        Serializable serializable = ruleAction.getParameterValue(PAGE_SIZE);
        int maxItems;
        Map<String,Object> response = new HashMap<>();

        try
        {
            maxItems = Integer.parseInt(serializable.toString());
        }
        catch (NumberFormatException e)
        {
            LOG.error("Exception occurred while parsing String to INT: {}", e.getMessage());
            nodeService.setProperty(actionedUponNodeRef, FolderSizeModel.PROP_ERROR,e.getMessage());
            throw e;
        }

        NodeRef nodeRef = actionedUponNodeRef;
        long totalSize = 0;
        ResultSet results;
        boolean isCalculationCompleted = false;

        StringBuilder aftsQuery = new StringBuilder();
        aftsQuery.append("ANCESTOR:\"").append(nodeRef).append("\" AND TYPE:content");
        String query = aftsQuery.toString();

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.setQuery(query);

        try
        {
            // executing Alfresco FTS query.
            results = searchService.query(searchParameters);
            int skipCount = 0;
            int totalItems;
            totalItems = Math.min(results.getNodeRefs().size(), maxItems);

            while (!isCalculationCompleted)
            {
                List<NodeRef> nodeRefs = results.getNodeRefs().subList(skipCount, totalItems);
                // Using AtomicLong to accumulate the total size.
                AtomicLong resultSize = new AtomicLong(0);
                nodeRefs.parallelStream().forEach(id -> {
                    try
                    {
                        ContentData contentData = (ContentData) nodeService.getProperty(id, ContentModel.PROP_CONTENT);
                        if (contentData != null)
                        {
                            resultSize.addAndGet(contentData.getSize());
                        }
                    }
                    catch (Exception e)
                    {
                        resultSize.addAndGet(0);
                    }
                });

                totalSize+=resultSize.longValue();

                if (results.getNodeRefs().size() <= totalItems || results.getNodeRefs().size() <= maxItems)
                {
                    isCalculationCompleted = true;
                }
                else
                {
                    skipCount += maxItems;
                    int remainingItems = results.getNodeRefs().size() - totalItems;
                    totalItems += Math.min(remainingItems, maxItems);
                }
            }
        }
        catch (RuntimeException ex)
        {
            LOG.error("Exception occurred in NodeSizeActionExecutor:results {}", ex.getMessage());
            nodeService.setProperty(nodeRef, FolderSizeModel.PROP_ERROR,ex.getMessage());
            throw ex;
        }

        LOG.info(" Calculating size of Folder Node - NodeSizeActionExecutor:executeImpl ");
        final LocalDateTime eventTimestamp = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        String formattedTimestamp = eventTimestamp.format(formatter);
        response.put("id", nodeRef.getId());
        response.put("size", totalSize);
        response.put("calculatedAtTime", formattedTimestamp);
        response.put("numberOfFiles", results != null ? results.getNodeRefs().size() : 0);

        if(isCalculationCompleted)
        {
            nodeService.setProperty(nodeRef, FolderSizeModel.PROP_OUTPUT, (Serializable) response);
            nodeService.setProperty(nodeRef, FolderSizeModel.PROP_ERROR,null);
        }
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
