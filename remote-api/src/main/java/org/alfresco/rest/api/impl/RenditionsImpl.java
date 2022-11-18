/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software LimitedP
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

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.alfresco.heartbeat.RenditionsDataCollector;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.rendition2.RenditionDefinition2;
import org.alfresco.repo.rendition2.RenditionDefinitionRegistry2;
import org.alfresco.repo.rendition2.RenditionService2;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.thumbnail.script.ScriptThumbnailService;
import org.alfresco.repo.version.common.VersionUtil;
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
import org.alfresco.rest.framework.core.exceptions.StaleEntityException;
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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionDoesNotExistException;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

/**
 * @author Jamal Kaabi-Mofrad, janv
 */
public class RenditionsImpl implements Renditions, ResourceLoaderAware
{
    private static final Log logger = LogFactory.getLog(RenditionsImpl.class);

    private static final Set<String> RENDITION_STATUS_COLLECTION_EQUALS_QUERY_PROPERTIES = Collections.singleton(PARAM_STATUS);

    private Nodes nodes;
    private NodeService nodeService;
    private ScriptThumbnailService scriptThumbnailService;
    private MimetypeService mimetypeService;
    private ServiceRegistry serviceRegistry;
    private ResourceLoader resourceLoader;
    private TenantService tenantService;
    private RenditionService2 renditionService2;
    private RenditionsDataCollector renditionsDataCollector;
    private VersionService versionService;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
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

    public void setRenditionService2(RenditionService2 renditionService2)
    {
        this.renditionService2 = renditionService2;
    }

    public void setRenditionsDataCollector(RenditionsDataCollector renditionsDataCollector)
    {
        this.renditionsDataCollector = renditionsDataCollector;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "nodes", nodes);
        PropertyCheck.mandatory(this, "scriptThumbnailService", scriptThumbnailService);
        PropertyCheck.mandatory(this, "serviceRegistry", serviceRegistry);
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "renditionService2", renditionService2);
        PropertyCheck.mandatory(this, "renditionsDataCollector", renditionsDataCollector);

        this.nodeService = serviceRegistry.getNodeService();
        this.versionService = serviceRegistry.getVersionService();
        this.mimetypeService = serviceRegistry.getMimetypeService();
    }

    @Override
    public CollectionWithPagingInfo<Rendition> getRenditions(NodeRef nodeRef, Parameters parameters)
    {
        return getRenditions(nodeRef, null, parameters);
    }

    @Override
    public CollectionWithPagingInfo<Rendition> getRenditions(NodeRef nodeRef, String versionLabelId, Parameters parameters)
    {
        final NodeRef validatedNodeRef = validateNode(nodeRef.getStoreRef(), nodeRef.getId(), versionLabelId, parameters);
        ContentData contentData = getContentData(validatedNodeRef, true);
        String sourceMimetype = contentData.getMimetype();

        boolean includeCreated = true;
        boolean includeNotCreated = true;
        String status = getStatus(parameters);
        if (status != null)
        {
            includeCreated = RenditionStatus.CREATED.equals(RenditionStatus.valueOf(status));
            includeNotCreated = !includeCreated;
        }

        // List all available rendition definitions
        long size = contentData.getSize();
        RenditionDefinitionRegistry2 renditionDefinitionRegistry2 = renditionService2.getRenditionDefinitionRegistry2();
        Set<String> renditionNames = renditionDefinitionRegistry2.getRenditionNamesFrom(sourceMimetype, size);

        Map<String, Rendition> apiRenditions = new TreeMap<>();
        if (includeNotCreated)
        {
            for (String renditionName : renditionNames)
            {
                apiRenditions.put(renditionName, toApiRendition(renditionName));
            }
        }

        List<ChildAssociationRef> nodeRefRenditions = renditionService2.getRenditions(validatedNodeRef);
        if (!nodeRefRenditions.isEmpty())
        {
            for (ChildAssociationRef childAssociationRef : nodeRefRenditions)
            {
                NodeRef renditionNodeRef = childAssociationRef.getChildRef();
                Rendition apiRendition = toApiRendition(renditionNodeRef);
                String renditionName = apiRendition.getId();
                if (renditionNames.contains(renditionName))
                {
                    if (includeCreated)
                    {
                        // Replace/append any thumbnail definitions with created rendition info
                        apiRenditions.put(renditionName, apiRendition);
                    }
                    else
                    {
                        // Remove any thumbnail definitions that has been created from the list,
                        // as the filter requires only the Not_Created renditions
                        apiRenditions.remove(renditionName);
                    }
                }
                else
                {
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Skip unknown rendition [" + renditionName + ", " + renditionNodeRef + "]");
                    }
                }
            }
        }

        // Wrap paging info, as the core service doesn't support paging
        Paging paging = parameters.getPaging();
        PagingResults<Rendition> results = Util.wrapPagingResults(paging, apiRenditions.values());

        return CollectionWithPagingInfo.asPaged(paging, results.getPage(), results.hasMoreItems(), results.getTotalResultCount().getFirst());
    }

    @Override
    public Rendition getRendition(NodeRef nodeRef, String renditionId, Parameters parameters)
    {
        return getRendition(nodeRef, null, renditionId, parameters);
    }

    @Override
    public Rendition getRendition(NodeRef nodeRef, String versionLabelId, String renditionId, Parameters parameters)
    {
        final NodeRef validatedNodeRef = validateNode(nodeRef.getStoreRef(), nodeRef.getId(), versionLabelId, parameters);
        NodeRef renditionNodeRef = getRenditionByName(validatedNodeRef, renditionId, parameters);
        boolean includeNotCreated = true;
        String status = getStatus(parameters);
        if (status != null)
        {
            includeNotCreated = !RenditionStatus.CREATED.equals(RenditionStatus.valueOf(status));
        }

        // if there is no rendition, then try to find the available/registered rendition (yet to be created).
        if (renditionNodeRef == null && includeNotCreated)
        {
            ContentData contentData = getContentData(validatedNodeRef, true);
            String sourceMimetype = contentData.getMimetype();
            long size = contentData.getSize();
            RenditionDefinitionRegistry2 renditionDefinitionRegistry2 = renditionService2.getRenditionDefinitionRegistry2();
            RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(renditionId);
            if (renditionDefinition == null)
            {
                throw new NotFoundException(renditionId + " is not registered.");
            }
            else
            {
                Set<String> renditionNames = renditionDefinitionRegistry2.getRenditionNamesFrom(sourceMimetype, size);
                boolean found = false;
                for (String renditionName : renditionNames)
                {
                    // Check the registered renditionId is applicable for the node's mimeType
                    if (renditionId.equals(renditionName))
                    {
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    throw new NotFoundException(renditionId + " is not applicable for the node's mimeType " + sourceMimetype);
                }
            }
            return toApiRendition(renditionId);
        }

        if (renditionNodeRef == null)
        {
            throw new NotFoundException("The rendition with id: " + renditionId + " was not found.");
        }

        return toApiRendition(renditionNodeRef);
    }

    @Override
    public void createRendition(NodeRef nodeRef, Rendition rendition, Parameters parameters)
    {
        createRendition(nodeRef, rendition, true, parameters);
    }

    @Override
    public void createRendition(NodeRef nodeRef, Rendition rendition, boolean executeAsync, Parameters parameters)
    {
        createRendition(nodeRef, null, rendition, executeAsync, parameters);
    }

    @Override
    public void createRendition(NodeRef nodeRef, String versionLabelId, Rendition rendition, boolean executeAsync, Parameters parameters)
    {
        // If thumbnail generation has been configured off, then don't bother.
        if (!renditionService2.isEnabled())
        {
            throw new DisabledServiceException("Rendition generation has been disabled.");
        }

        final NodeRef sourceNodeRef = validateNode(nodeRef.getStoreRef(), nodeRef.getId(), versionLabelId, parameters);
        final NodeRef renditionNodeRef = getRenditionByName(sourceNodeRef, rendition.getId(), parameters);
        if (renditionNodeRef != null)
        {
            throw new ConstraintViolatedException(rendition.getId() + " rendition already exists."); // 409
        }

        try
        {
            renditionService2.render(sourceNodeRef, rendition.getId());
        }
        catch (IllegalArgumentException e)
        {
            throw new NotFoundException(rendition.getId() + " is not registered."); // 404
        }
        catch (UnsupportedOperationException e)
        {
            throw new IllegalArgumentException((e.getMessage())); // 400
        }
        catch (IllegalStateException e)
        {
            throw new StaleEntityException(e.getMessage()); // 409
        }
    }

    @Override
    public void createRenditions(NodeRef nodeRef, List<Rendition> renditions, Parameters parameters)
            throws NotFoundException, ConstraintViolatedException
    {
        createRenditions(nodeRef, null, renditions, parameters);
    }

    @Override
    public void createRenditions(NodeRef nodeRef, String versionLabelId, List<Rendition> renditions, Parameters parameters)
            throws NotFoundException, ConstraintViolatedException
    {
        if (renditions.isEmpty())
        {
            return;
        }

        if (!renditionService2.isEnabled())
        {
            throw new DisabledServiceException("Rendition generation has been disabled.");
        }

        final NodeRef sourceNodeRef = validateNode(nodeRef.getStoreRef(), nodeRef.getId(), versionLabelId, parameters);
        RenditionDefinitionRegistry2 renditionDefinitionRegistry2 = renditionService2.getRenditionDefinitionRegistry2();

        // So that POST /nodes/{nodeId}/renditions can specify rendition names as a comma separated list just like
        // POST /nodes/{nodId}/children can specify a comma separated list, the following code checks to see if the
        // supplied Rendition names are actually comma separated lists. The following example shows it is possible to
        // use both approaches.
        // [
        //  { "id": "doclib" },
        //  { "id": "avatar,avatar32" }
        // ]
        Set<String> renditionNames = new HashSet<>();
        for (Rendition rendition : renditions)
        {
            String name = getName(rendition);
            Set<String> requestedRenditions = NodesImpl.getRequestedRenditions(name);
            if (requestedRenditions == null)
            {
                renditionNames.add(null);
            }
            else
            {
                renditionNames.addAll(requestedRenditions);
            }
        }

        StringJoiner renditionNamesAlreadyExist = new StringJoiner(",");
        StringJoiner renditionNamesNotRegistered = new StringJoiner(",");
        List<String> renditionNamesToCreate = new ArrayList<>();
        for (String renditionName : renditionNames)
        {
            if (renditionName == null)
            {
                throw new IllegalArgumentException(("Null rendition name supplied")); // 400
            }

            RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(renditionName);
            if (renditionDefinition == null)
            {
                renditionNamesNotRegistered.add(renditionName);
            }

            final NodeRef renditionNodeRef = getRenditionByName(sourceNodeRef, renditionName, parameters);
            if (renditionNodeRef == null)
            {
                renditionNamesToCreate.add(renditionName);
            }
            else
            {
                renditionNamesAlreadyExist.add(renditionName);
            }
        }

        if (renditionNamesNotRegistered.length() != 0)
        {
            throw new NotFoundException("Renditions not registered: " + renditionNamesNotRegistered); // 404
        }

        if (renditionNamesToCreate.size() == 0)
        {
            throw new ConstraintViolatedException("All renditions requested already exist: " + renditionNamesAlreadyExist); // 409
        }

        for (String renditionName : renditionNamesToCreate)
        {
            try
            {
                renditionService2.render(sourceNodeRef, renditionName);
            }
            catch (UnsupportedOperationException e)
            {
                throw new IllegalArgumentException((e.getMessage())); // 400
            }
            catch (IllegalStateException e)
            {
                throw new StaleEntityException(e.getMessage()); // 409
            }
        }

    }

    @Override
    public void deleteRendition(NodeRef nodeRef, String renditionId, Parameters parameters)
    {
        deleteRendition(nodeRef, null, renditionId, parameters);
    }

    @Override
    public void deleteRendition(NodeRef nodeRef, String versionId, String renditionId, Parameters parameters)
    {
        if (!renditionService2.isEnabled())
        {
            throw new DisabledServiceException("Rendition generation has been disabled.");
        }

        final NodeRef validatedNodeRef = validateNode(nodeRef.getStoreRef(), nodeRef.getId(), versionId, parameters);
        NodeRef renditionNodeRef = getRenditionByName(validatedNodeRef, renditionId, parameters);

        if (renditionNodeRef == null)
        {
            throw new NotFoundException(renditionId + " is not registered.");
        }

        renditionService2.clearRenditionContentDataInTransaction(renditionNodeRef);
    }

    private String getName(Rendition rendition)
    {
        String renditionName = rendition.getId();
        if (renditionName != null)
        {
            renditionName = renditionName.trim();
            if (renditionName.isEmpty())
            {
                renditionName = null;
            }
        }
        return renditionName;
    }

    @Override
    public BinaryResource getContent(NodeRef nodeRef, String renditionId, Parameters parameters)
    {
        return getContent(nodeRef, null, renditionId, parameters);
    }

    @Override
    public BinaryResource getContent(NodeRef nodeRef, String versionLabelId, String renditionId, Parameters parameters)
    {
        final NodeRef validatedNodeRef = validateNode(nodeRef.getStoreRef(), nodeRef.getId(), versionLabelId, parameters);
        return getContentImpl(validatedNodeRef, renditionId, parameters);
    }

    @Override
    public BinaryResource getContentNoValidation(NodeRef nodeRef, String renditionId, Parameters parameters)
    {
        return getContentNoValidation(nodeRef, null, renditionId, parameters);
    }

    @Override
    public BinaryResource getContentNoValidation(NodeRef nodeRef, String versionLabelId, String renditionId, Parameters parameters)
    {
        nodeRef = findVersionIfApplicable(nodeRef, versionLabelId);
        return getContentImpl(nodeRef, renditionId, parameters);
    }

    /**
     * {@inheritDoc}
     */
    public DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, String versionId, String renditionId, boolean attachment, Long validFor)
    {
        final NodeRef validatedNodeRef = validateNode(nodeRef.getStoreRef(), nodeRef.getId(), versionId, null);
        NodeRef renditionNodeRef = getRenditionByName(validatedNodeRef, renditionId, null);

        if (renditionNodeRef == null)
        {
            throw new NotFoundException("The rendition with id: " + renditionId + " was not found.");
        }

        return nodes.requestContentDirectUrl(renditionNodeRef, attachment, validFor);
    }

    private BinaryResource getContentImpl(NodeRef nodeRef, String renditionId, Parameters parameters)
    {
        NodeRef renditionNodeRef = getRenditionByName(nodeRef, renditionId, parameters);

        // By default set attachment header (with rendition Id) unless attachment=false
        boolean attach = true;
        String attachment = parameters.getParameter(PARAM_ATTACHMENT);
        if (attachment != null)
        {
            attach = Boolean.valueOf(attachment);
        }
        final String attachFileName = (attach ? renditionId : null);

        if (renditionNodeRef == null)
        {
            boolean isPlaceholder = Boolean.valueOf(parameters.getParameter(PARAM_PLACEHOLDER));
            if (!isPlaceholder)
            {
                throw new NotFoundException("Thumbnail was not found for [" + renditionId + ']');
            }
            String sourceNodeMimeType = null;
            try
            {
                sourceNodeMimeType = (nodeRef != null ? getMimeType(nodeRef) : null);
            }
            catch (InvalidArgumentException e)
            {
                // No content for node, e.g. ASSOC_AVATAR rather than ASSOC_PREFERENCE_IMAGE
            }
            // resource based on the content's mimeType and rendition id
            String phPath = scriptThumbnailService.getMimeAwarePlaceHolderResourcePath(renditionId, sourceNodeMimeType);
            if (phPath == null)
            {
                // 404 since no thumbnail was found
                throw new NotFoundException("Thumbnail was not found and no placeholder resource available for [" + renditionId + ']');
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Retrieving content from resource path [" + phPath + ']');
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
                    if (logger.isErrorEnabled())
                    {
                        logger.error("Couldn't load the placeholder." + ex.getMessage());
                    }
                    throw new ApiException("Couldn't load the placeholder.");
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
        if (nodeRef != null)
        {
            if (StringUtils.isEmpty(renditionId))
            {
                throw new InvalidArgumentException("renditionId can't be null or empty.");
            }

            ChildAssociationRef nodeRefRendition = renditionService2.getRenditionByName(nodeRef, renditionId);
            if (nodeRefRendition != null)
            {
                ContentData contentData = getContentData(nodeRefRendition.getChildRef(), false);
                if (contentData != null)
                {
                    return tenantService.getName(nodeRef, nodeRefRendition.getChildRef());
                }
            }
        }

        return null;
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

    protected Rendition toApiRendition(String renditionName)
    {
        RenditionDefinitionRegistry2 renditionDefinitionRegistry2 = renditionService2.getRenditionDefinitionRegistry2();
        RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(renditionName);
        ContentInfo contentInfo = new ContentInfo(renditionDefinition.getTargetMimetype(),
                getMimeTypeDisplayName(renditionDefinition.getTargetMimetype()), null, null);

        Rendition apiRendition = new Rendition();
        apiRendition.setId(renditionName);
        apiRendition.setContent(contentInfo);
        apiRendition.setStatus(RenditionStatus.NOT_CREATED);

        return apiRendition;
    }

    private NodeRef validateNode(StoreRef storeRef, final String nodeId, String versionLabelId, Parameters parameters)
    {
        if (nodeId == null)
        {
            throw new InvalidArgumentException("Missing nodeId");
        }

        NodeRef nodeRef = nodes.validateNode(storeRef, nodeId);
        // check if the node represents a file
        isContentFile(nodeRef);

        nodeRef = findVersionIfApplicable(nodeRef, versionLabelId);

        return nodeRef;
    }

    private NodeRef findVersionIfApplicable(NodeRef nodeRef, String versionLabelId)
    {
        if (versionLabelId != null)
        {
            nodeRef = nodes.validateOrLookupNode(nodeRef.getId(), null);
            VersionHistory vh = versionService.getVersionHistory(nodeRef);
            if (vh != null)
            {
                try
                {
                    Version version = vh.getVersion(versionLabelId);
                    nodeRef = VersionUtil.convertNodeRef(version.getFrozenStateNodeRef());
                }
                catch (VersionDoesNotExistException vdne)
                {
                    throw new NotFoundException("Couldn't find version: [" + nodeRef.getId() + ", " + versionLabelId + "]");
                }
            }
        }

        return nodeRef;
    }

    private void isContentFile(NodeRef nodeRef)
    {
        if (!nodes.isSubClass(nodeRef, ContentModel.PROP_CONTENT, false))
        {
            throw new InvalidArgumentException("Node id '" + nodeRef.getId() + "' does not represent a file.");
        }
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

