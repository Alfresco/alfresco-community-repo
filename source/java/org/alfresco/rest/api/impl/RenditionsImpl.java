/*
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

package org.alfresco.rest.api.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailHelper;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.thumbnail.script.ScriptThumbnailService;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Renditions;
import org.alfresco.rest.api.model.ContentInfo;
import org.alfresco.rest.api.model.Rendition;
import org.alfresco.rest.api.model.Rendition.RenditionStatus;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.CacheDirective;
import org.alfresco.rest.framework.resource.content.ContentInfoImpl;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.rest.framework.resource.content.NodeBinaryResource;
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
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class RenditionsImpl implements Renditions, ResourceLoaderAware
{
    private static final Log LOGGER = LogFactory.getLog(RenditionsImpl.class);

    private static final Set<String> RENDITION_STATUS_COLLECTION_EQUALS_QUERY_PROPERTIES = Collections.singleton(PARAM_STATUS);

    private Nodes nodes;
    private NodeService nodeService;
    private ThumbnailService thumbnailService;
    private ScriptThumbnailService scriptThumbnailService;
    private RenditionService renditionService;
    private MimetypeService mimetypeService;
    private ActionService actionService;
    private NamespaceService namespaceService;
    private ServiceRegistry serviceRegistry;
    private ResourceLoader resourceLoader;
    private TenantService tenantService;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }

    public void setScriptThumbnailService(ScriptThumbnailService scriptThumbnailService)
    {
        this.scriptThumbnailService = scriptThumbnailService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "nodes", nodes);
        PropertyCheck.mandatory(this, "thumbnailService", thumbnailService);
        PropertyCheck.mandatory(this, "scriptThumbnailService", scriptThumbnailService);
        PropertyCheck.mandatory(this, "serviceRegistry", serviceRegistry);
        PropertyCheck.mandatory(this, "tenantService", tenantService);

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
        String contentMimeType = getMimeType(nodeRef);

        Query query = parameters.getQuery();
        boolean includeCreated = true;
        boolean includeNotCreated = true;
        String status = getStatus(parameters);
        if (status != null)
        {
            includeCreated = RenditionStatus.CREATED.equals(RenditionStatus.valueOf(status));
            includeNotCreated = !includeCreated;
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
        boolean includeNotCreated = true;
        String status = getStatus(parameters);
        if (status != null)
        {
            includeNotCreated = !RenditionStatus.CREATED.equals(RenditionStatus.valueOf(status));
        }

        // if there is no rendition, then try to find the available/registered rendition (yet to be created).
        if (renditionNodeRef == null && includeNotCreated)
        {
            ThumbnailDefinition thumbnailDefinition = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(renditionId);
            if (thumbnailDefinition == null)
            {
                throw new NotFoundException(renditionId + " is not registered.");
            }
            else
            {
                String contentMimeType = getMimeType(nodeRef);
                // List all available thumbnail definitions for the source node
                List<ThumbnailDefinition> thumbnailDefinitions = thumbnailService.getThumbnailRegistry().getThumbnailDefinitions(contentMimeType, -1);
                boolean found = false;
                for (ThumbnailDefinition td : thumbnailDefinitions)
                {
                    // Check the registered renditionId is applicable for the node's mimeType
                    if (renditionId.equals(td.getName()))
                    {
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    throw new NotFoundException(renditionId + " is not applicable for the node's mimeType " + contentMimeType);
                }
            }
            return toApiRendition(thumbnailDefinition);
        }

        if (renditionNodeRef == null)
        {
            throw new NotFoundException("The rendition with id: " + renditionId + " was not found.");
        }

        return toApiRendition(renditionNodeRef);
    }

    @Override
    public void createRendition(String nodeId, Rendition rendition, Parameters parameters)
    {
        // If thumbnail generation has been configured off, then don't bother.
        if (!thumbnailService.getThumbnailsEnabled())
        {
            throw new DisabledServiceException("Thumbnail generation has been disabled.");
        }

        final NodeRef sourceNodeRef = validateSourceNode(nodeId);
        final NodeRef renditionNodeRef = getRenditionByName(sourceNodeRef, rendition.getId(), parameters);
        if (renditionNodeRef != null)
        {
            throw new ConstraintViolatedException(rendition.getId() + " rendition already exists.");
        }

        // Use the thumbnail registry to get the details of the thumbnail
        ThumbnailRegistry registry = thumbnailService.getThumbnailRegistry();
        ThumbnailDefinition thumbnailDefinition = registry.getThumbnailDefinition(rendition.getId());
        if (thumbnailDefinition == null)
        {
            throw new NotFoundException(rendition.getId() + " is not registered.");
        }

        ContentData contentData = getContentData(sourceNodeRef, true);
        // Check if anything is currently available to generate thumbnails for the specified mimeType
        if (!registry.isThumbnailDefinitionAvailable(contentData.getContentUrl(), contentData.getMimetype(), contentData.getSize(), sourceNodeRef,
                    thumbnailDefinition))
        {
            throw new InvalidArgumentException("Unable to create thumbnail '" + thumbnailDefinition.getName() + "' for " +
                        contentData.getMimetype() + " as no transformer is currently available.");
        }

        Action action = ThumbnailHelper.createCreateThumbnailAction(thumbnailDefinition, serviceRegistry);
        // Queue async creation of thumbnail
        actionService.executeAction(action, sourceNodeRef, true, true);
    }

    @Override
    public BinaryResource getContent(String nodeId, String renditionId, Parameters parameters)
    {
        final NodeRef sourceNodeRef = validateSourceNode(nodeId);
        return getContent(sourceNodeRef, renditionId, parameters);
    }

    @Override
    public BinaryResource getContent(NodeRef sourceNodeRef, String renditionId, Parameters parameters)
    {
        NodeRef renditionNodeRef = getRenditionByName(sourceNodeRef, renditionId, parameters);

        // By default set attachment header (with rendition Id) unless attachment=false
        boolean attach = true;
        String attachment = parameters.getParameter("attachment");
        if (attachment != null)
        {
            attach = Boolean.valueOf(attachment);
        }
        final String attachFileName = (attach ? renditionId : null);

        if (renditionNodeRef == null)
        {
            boolean isPlaceholder = Boolean.valueOf(parameters.getParameter("placeholder"));
            if (!isPlaceholder)
            {
                throw new NotFoundException("Thumbnail was not found for [" + renditionId + ']');
            }
            String sourceNodeMimeType = getMimeType(sourceNodeRef);
            // resource based on the content's mimeType and rendition id
            String phPath = scriptThumbnailService.getMimeAwarePlaceHolderResourcePath(renditionId, sourceNodeMimeType);
            if (phPath == null)
            {
                // 404 since no thumbnail was found
                throw new NotFoundException("Thumbnail was not found and no placeholder resource available for [" + renditionId + ']');
            }
            else
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Retrieving content from resource path [" + phPath + ']');
                }
                // get extension of resource
                String ext = "";
                int extIndex = phPath.lastIndexOf('.');
                if (extIndex != -1)
                {
                    ext = phPath.substring(extIndex);
                }

                try
                {
                    final String resourcePath = "classpath:" + phPath;
                    InputStream inputStream = resourceLoader.getResource(resourcePath).getInputStream();
                    // create temporary file
                    File file = TempFileProvider.createTempFile(inputStream, "RenditionsApi-", ext);
                    return new FileBinaryResource(file, attachFileName);
                }
                catch (Exception ex)
                {
                    if (LOGGER.isErrorEnabled())
                    {
                        LOGGER.error("Couldn't load the placeholder." + ex.getMessage());
                    }
                    new ApiException("Couldn't load the placeholder.");
                }
            }
        }

        Map<QName, Serializable> nodeProps = nodeService.getProperties(renditionNodeRef);
        ContentData contentData = (ContentData) nodeProps.get(ContentModel.PROP_CONTENT);
        Date modified = (Date) nodeProps.get(ContentModel.PROP_MODIFIED);

        org.alfresco.rest.framework.resource.content.ContentInfo contentInfo = null;
        if (contentData != null)
        {
            contentInfo = new ContentInfoImpl(contentData.getMimetype(), contentData.getEncoding(), contentData.getSize(), contentData.getLocale());
        }
        // add cache settings
        CacheDirective cacheDirective = new CacheDirective.Builder()
                    .setNeverCache(false)
                    .setMustRevalidate(false)
                    .setLastModified(modified)
                    .setETag(modified != null ? Long.toString(modified.getTime()) : null)
                    .setMaxAge(Long.valueOf(31536000))// one year (in seconds)
                    .build();

        return new NodeBinaryResource(renditionNodeRef, ContentModel.PROP_CONTENT, contentInfo, attachFileName, cacheDirective);
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

        return tenantService.getName(nodeRef, nodeRefRendition.getChildRef());
    }

    protected Rendition toApiRendition(NodeRef renditionNodeRef)
    {
        Rendition apiRendition = new Rendition();

        String renditionName = (String) nodeService.getProperty(renditionNodeRef, ContentModel.PROP_NAME);
        apiRendition.setId(renditionName);

        ContentData contentData = getContentData(renditionNodeRef, false);
        ContentInfo contentInfo = null;
        if (contentData != null)
        {
            contentInfo = new ContentInfo(contentData.getMimetype(),
                        getMimeTypeDisplayName(contentData.getMimetype()),
                        contentData.getSize(),
                        contentData.getEncoding());
        }
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

    private ContentData getContentData(NodeRef nodeRef, boolean validate)
    {
        ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        if (validate && !ContentData.hasContent(contentData))
        {
            throw new InvalidArgumentException("Node id '" + nodeRef.getId() + "' has no content.");
        }
        return contentData;
    }

    private String getMimeType(NodeRef nodeRef)
    {
        ContentData contentData = getContentData(nodeRef, true);
        return contentData.getMimetype();
    }

    private String getStatus(Parameters parameters)
    {
        Query query = parameters.getQuery();
        String status = null;
        if (query != null)
        {
            // Filtering via "where" clause
            MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(RENDITION_STATUS_COLLECTION_EQUALS_QUERY_PROPERTIES, null);
            QueryHelper.walk(query, propertyWalker);

            status = propertyWalker.getProperty(PARAM_STATUS, WhereClauseParser.EQUALS);
        }
        return status;
    }
}

