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
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final Log logger = LogFactory.getLog(NodeSizeActionExecuter.class);

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
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @see ActionExecuter#execute(Action, NodeRef)
     */
    @Override
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        Serializable serializable = ruleAction.getParameterValue(PAGE_SIZE);
        int maxItems = Integer.parseInt(serializable.toString());
        int skipCount = 0;
        int totalItems = maxItems;
        NodeRef nodeRef = actionedUponNodeRef;
        long totalSize = 0;
        long resultSize;
        ResultSet results = null;

        StringBuilder aftsQuery = new StringBuilder();
        aftsQuery.append("ANCESTOR:\"").append(nodeRef).append("\" AND TYPE:content");
        String query = aftsQuery.toString();

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.setQuery(query);

        try{
            // executing Alfresco FTS query.
            results = searchService.query(searchParameters);

            while(true) {

                if (results.getNodeRefs().size() < maxItems)
                {
                    totalItems = results.getNodeRefs().size();
                }

                resultSize = results.getNodeRefs().subList(skipCount,totalItems).parallelStream()
                        .map(id -> ((ContentData) nodeService.getProperty(id, ContentModel.PROP_CONTENT)).getSize())
                        .reduce(0L, Long::sum);

                totalSize+=resultSize;

                if (results.getNodeRefs().size() <= totalItems || results.getNodeRefs().size() <= maxItems)
                {
                    break;
                }

                if (results.getNodeRefs().size() > maxItems)
                {
                    skipCount += maxItems;
                    int remainingItems = results.getNodeRefs().size()-totalItems;

                    if(remainingItems > maxItems)
                    {
                        totalItems += maxItems;
                    }
                    else
                    {
                        totalItems += remainingItems;
                    }
                }
            }
        }
        catch (RuntimeException ex)
        {
            logger.error("Exception occured in NodeSizeActionExecutor:results "+ex.getMessage());
        }

        final LocalDateTime eventTimestamp = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
        String formattedTimestamp = eventTimestamp.format(formatter);
        Map<String,Object> response = new HashMap<>();
        response.put("id", nodeRef.getId());
        response.put("size", totalSize);
        response.put("calculatedAtTime", formattedTimestamp);
        response.put("numberOfFiles", results!=null?results.getNodeRefs().size():0);
        nodeService.setProperty(nodeRef, FolderSizeModel.PROP_OUTPUT, (Serializable) response);
        nodeService.setProperty(nodeRef, FolderSizeModel.PROP_STATUS,"COMPLETED");
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    }
}
