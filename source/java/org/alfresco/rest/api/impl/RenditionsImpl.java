/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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

package org.alfresco.rest.api.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailHelper;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Renditions;
import org.alfresco.rest.api.model.ContentInfo;
import org.alfresco.rest.api.model.Rendition;
import org.alfresco.rest.api.model.Rendition.RenditionStatus;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class RenditionsImpl implements Renditions
{
    private static final String PARAM_status = "status";
    private static final Set<String> RENDITION_STATUS_COLLECTION_EQUALS_QUERY_PROPERTIES = Collections.singleton(PARAM_status);

    private Nodes nodes;
    private NodeService nodeService;
    private ThumbnailService thumbnailService;
    private RenditionService renditionService;
    private MimetypeService mimetypeService;
    private ActionService actionService;
    private NamespaceService namespaceService;
    private ServiceRegistry serviceRegistry;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "nodes", nodes);
        PropertyCheck.mandatory(this, "thumbnailService", thumbnailService);
        PropertyCheck.mandatory(this, "serviceRegistry", serviceRegistry);

        this.nodeService = serviceRegistry.getNodeService();
        this.actionService = serviceRegistry.getActionService();
        this.renditionService = serviceRegistry.getRenditionService();
        this.mimetypeService = serviceRegistry.getMimetypeService();
        this.namespaceService = serviceRegistry.getNamespaceService();
    }

    @Override
    public CollectionWithPagingInfo<Rendition> getRenditions(String nodeId, Parameters parameters)
    {
        final NodeRef nodeRef = validateSourceNode(nodeId);

        ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        String contentMimeType = contentData.getMimetype();

        Query query = parameters.getQuery();
        boolean includeCreated = true;
        boolean includeNotCreated = true;
        if (query != null)
        {
            // Filtering via "where" clause
            MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(RENDITION_STATUS_COLLECTION_EQUALS_QUERY_PROPERTIES, null);
            QueryHelper.walk(query, propertyWalker);

            String withStatus = propertyWalker.getProperty(PARAM_status, WhereClauseParser.EQUALS);
            if (withStatus != null)
            {
                try
                {
                    includeCreated = RenditionStatus.CREATED.equals(RenditionStatus.valueOf(withStatus));
                }
                catch (IllegalArgumentException ex)
                {
                    throw new InvalidArgumentException("Invalid status value: " + withStatus);
                }
                includeNotCreated = !includeCreated;
            }
        }

        Map<String, Rendition> apiRenditions = new TreeMap<>();
        if (includeNotCreated)
        {
            // List all available thumbnail definitions
            List<ThumbnailDefinition> thumbnailDefinitions = thumbnailService.getThumbnailRegistry().getThumbnailDefinitions(contentMimeType, -1);
            for (ThumbnailDefinition thumbnailDefinition : thumbnailDefinitions)
            {
                apiRenditions.put(thumbnailDefinition.getName(), toApiRendition(thumbnailDefinition));
            }
        }

        List<ChildAssociationRef> nodeRefRenditions = renditionService.getRenditions(nodeRef);
        if (!nodeRefRenditions.isEmpty())
        {
            for (ChildAssociationRef childAssociationRef : nodeRefRenditions)
            {
                NodeRef renditionNodeRef = childAssociationRef.getChildRef();
                Rendition apiRendition = toApiRendition(renditionNodeRef);
                if (includeCreated)
                {
                    // Replace/append any thumbnail definitions with created rendition info
                    apiRenditions.put(apiRendition.getId(), apiRendition);
                }
                else
                {
                    // Remove any thumbnail definitions that has been created from the list,
                    // as the filter requires only the Not_Created renditions
                    apiRenditions.remove(apiRendition.getId());
                }
            }
        }

        // Wrap paging info, as the core service doesn't support paging
        Paging paging = parameters.getPaging();
        PagingResults<Rendition> results = Util.wrapPagingResults(paging, apiRenditions.values());

        return CollectionWithPagingInfo.asPaged(paging, results.getPage(), results.hasMoreItems(), results.getTotalResultCount().getFirst());
    }

    @Override
    public Rendition getRendition(String nodeId, String renditionId, Parameters parameters)
    {
        final NodeRef nodeRef = validateSourceNode(nodeId);
        NodeRef renditionNodeRef = getRenditionByName(nodeRef, renditionId, parameters);

        // if there is no rendition, then try to find the available/registered rendition (yet to be created).
        if (renditionNodeRef == null)
        {
            ThumbnailDefinition thumbnailDefinition = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(renditionId);
            if (thumbnailDefinition == null)
            {
                throw new EntityNotFoundException(renditionId);
            }
            return toApiRendition(thumbnailDefinition);
        }

        return toApiRendition(renditionNodeRef);
    }

    @Override
    public void createRendition(String nodeId, Rendition rendition, Parameters parameters)
    {
        NodeRef sourceNodeRef = nodes.validateNode(nodeId);

        // If thumbnail generation has been configured off, then don't bother with any of this.
        if (thumbnailService.getThumbnailsEnabled())
        {
            // Use the thumbnail registry to get the details of the thumbnail
            ThumbnailRegistry registry = thumbnailService.getThumbnailRegistry();
            ThumbnailDefinition details = registry.getThumbnailDefinition(rendition.getId());
            if (details == null)
            {
                // Throw exception
                throw new InvalidArgumentException("The thumbnail name '" + rendition.getId() + "' is not registered");
            }

            // Check if anything is currently registered to generate thumbnails for the specified mimeType
            ContentData contentData = (ContentData) nodeService.getProperty(sourceNodeRef, ContentModel.PROP_CONTENT);
            if (!ContentData.hasContent(contentData))
            {
                throw new InvalidArgumentException("Unable to create thumbnail '" + details.getName() + "' as there is no content");
            }
            if (!registry.isThumbnailDefinitionAvailable(contentData.getContentUrl(), contentData.getMimetype(), contentData.getSize(), sourceNodeRef, details))
            {
                throw new InvalidArgumentException("Unable to create thumbnail '" + details.getName() + "' for " +
                            contentData.getMimetype() + " as no transformer is currently available.");
            }

            Action action = ThumbnailHelper.createCreateThumbnailAction(details, serviceRegistry);
            // Queue async creation of thumbnail
            actionService.executeAction(action, sourceNodeRef, true, true);
        }
    }

    protected NodeRef getRenditionByName(NodeRef nodeRef, String renditionId, Parameters parameters)
    {
        if (StringUtils.isEmpty(renditionId))
        {
            throw new InvalidArgumentException("renditionId can't be null or empty.");
        }
        // Thumbnails have a cm: prefix.
        QName renditionQName = QName.resolveToQName(namespaceService, renditionId);

        ChildAssociationRef nodeRefRendition = renditionService.getRenditionByName(nodeRef, renditionQName);
        if (nodeRefRendition == null)
        {
            return null;
        }
        return nodeRefRendition.getChildRef();
    }

    protected Rendition toApiRendition(NodeRef renditionNodeRef)
    {
        Rendition apiRendition = new Rendition();

        String renditionName = (String) nodeService.getProperty(renditionNodeRef, ContentModel.PROP_NAME);
        apiRendition.setId(renditionName);

        ContentData contentData = (ContentData) nodeService.getProperty(renditionNodeRef, ContentModel.PROP_CONTENT);
        ContentInfo contentInfo = new ContentInfo(contentData.getMimetype(), getMimeTypeDisplayName(contentData.getMimetype()), contentData.getSize(), contentData.getEncoding());
        apiRendition.setContent(contentInfo);
        apiRendition.setStatus(RenditionStatus.CREATED);

        return apiRendition;
    }

    protected Rendition toApiRendition(ThumbnailDefinition thumbnailDefinition)
    {
        ContentInfo contentInfo = new ContentInfo(thumbnailDefinition.getMimetype(),
                    getMimeTypeDisplayName(thumbnailDefinition.getMimetype()), null, null);
        Rendition apiRendition = new Rendition();
        apiRendition.setId(thumbnailDefinition.getName());
        apiRendition.setContent(contentInfo);
        apiRendition.setStatus(RenditionStatus.NOT_CREATED);

        return apiRendition;
    }

    protected NodeRef validateSourceNode(String nodeId)
    {
        final NodeRef nodeRef = nodes.validateNode(nodeId);
        if (!nodes.isSubClass(nodeRef, ContentModel.PROP_CONTENT, false))
        {
            throw new InvalidArgumentException("Node id '" + nodeId + "' does not represent a file.");
        }
        return nodeRef;
    }

    private String getMimeTypeDisplayName(String mimeType)
    {
        return mimetypeService.getDisplaysByMimetype().get(mimeType);
    }
}
