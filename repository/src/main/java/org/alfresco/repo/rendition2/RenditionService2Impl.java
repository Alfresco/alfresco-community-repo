/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AsynchronousExtractor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rendition.RenditionPreventionRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.util.PostTxnCallbackScheduler;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.alfresco.model.ContentModel.PROP_CONTENT;
import static org.alfresco.model.RenditionModel.PROP_RENDITION_CONTENT_HASH_CODE;
import static org.alfresco.service.namespace.QName.createQName;

/**
 * The Async Rendition service. Replaces the original deprecated RenditionService.
 *
 * @author adavis
 */
public class RenditionService2Impl implements RenditionService2, InitializingBean, ContentServicePolicies.OnContentUpdatePolicy
{
    public static final String TRANSFORMING_ERROR_MESSAGE = "Some error occurred during document transforming. Error message: ";

    public static final QName DEFAULT_RENDITION_CONTENT_PROP = ContentModel.PROP_CONTENT;
    public static final String DEFAULT_MIMETYPE = MimetypeMap.MIMETYPE_TEXT_PLAIN;
    public static final String DEFAULT_ENCODING = "UTF-8";

    public static final int SOURCE_HAS_NO_CONTENT = -1;
    public static final int RENDITION2_DOES_NOT_EXIST = -2;

    private static Log logger = LogFactory.getLog(RenditionService2Impl.class);

    // As Async transforms and renditions are so similar, this class provides a way to provide the code that is different.
    private abstract static class RenderOrTransformCallBack
    {
        abstract String getName();

        abstract RenditionDefinition2 getRenditionDefinition();

        void handleUnsupported(UnsupportedOperationException e)
        {
        }

        void throwIllegalStateExceptionIfAlreadyDone(int sourceContentHashCode)
        {
        }
    }

    private TransactionService transactionService;
    private NodeService nodeService;
    private ContentService contentService;
    private RenditionPreventionRegistry renditionPreventionRegistry;
    private RenditionDefinitionRegistry2 renditionDefinitionRegistry2;
    private TransformClient transformClient;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;
    private RuleService ruleService;
    private PostTxnCallbackScheduler renditionRequestSheduler;
    private TransformReplyProvider transformReplyProvider;
    private AsynchronousExtractor asynchronousExtractor;
    private boolean enabled;
    private boolean thumbnailsEnabled;

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setRenditionPreventionRegistry(RenditionPreventionRegistry renditionPreventionRegistry)
    {
        this.renditionPreventionRegistry = renditionPreventionRegistry;
    }

    public void setRenditionDefinitionRegistry2(RenditionDefinitionRegistry2 renditionDefinitionRegistry2)
    {
        this.renditionDefinitionRegistry2 = renditionDefinitionRegistry2;
    }

    @Override
    public RenditionDefinitionRegistry2 getRenditionDefinitionRegistry2()
    {
        return renditionDefinitionRegistry2;
    }

    public void setTransformClient(TransformClient transformClient)
    {
        this.transformClient = transformClient;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    public void setRenditionRequestSheduler(PostTxnCallbackScheduler renditionRequestSheduler)
    {
        this.renditionRequestSheduler = renditionRequestSheduler;
    }

    public void setTransformReplyProvider(TransformReplyProvider transformReplyProvider)
    {
        this.transformReplyProvider = transformReplyProvider;
    }

    public void setAsynchronousExtractor(AsynchronousExtractor asynchronousExtractor)
    {
        this.asynchronousExtractor = asynchronousExtractor;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setThumbnailsEnabled(boolean thumbnailsEnabled)
    {
        this.thumbnailsEnabled = thumbnailsEnabled;
    }

    public boolean isThumbnailsEnabled()
    {
        return thumbnailsEnabled;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "renditionPreventionRegistry", renditionPreventionRegistry);
        PropertyCheck.mandatory(this, "renditionDefinitionRegistry2", renditionDefinitionRegistry2);
        PropertyCheck.mandatory(this, "transformClient", transformClient);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "behaviourFilter", behaviourFilter);
        PropertyCheck.mandatory(this, "ruleService", ruleService);
        PropertyCheck.mandatory(this, "asynchronousExtractor", asynchronousExtractor);
    }

    @Override
    public void transform(NodeRef sourceNodeRef, TransformDefinition transformDefinition)
    {
        requestAsyncTransformOrRendition(sourceNodeRef, new RenderOrTransformCallBack()
        {
            @Override
            public String getName()
            {
                String transformName = transformDefinition.getTransformName();
                return "Transform" + (transformName == null ? "" : " " + transformName);
            }

            @Override
            public RenditionDefinition2 getRenditionDefinition()
            {
                return transformDefinition;
            }
        });
    }

    @Override
    public void render(NodeRef sourceNodeRef, String renditionName)
    {
        requestAsyncTransformOrRendition(sourceNodeRef, new RenderOrTransformCallBack()
        {
            @Override
            public String getName()
            {
                return "Rendition " + renditionName;
            }

            @Override
            public RenditionDefinition2 getRenditionDefinition()
            {
                checkSourceNodeForPreventionClass(sourceNodeRef);

                RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(renditionName);
                if (renditionDefinition == null)
                {
                    throw new IllegalArgumentException(getName() + " has not been registered.");
                }
                return renditionDefinition;
            }

            @Override
            public void handleUnsupported(UnsupportedOperationException e)
            {
                // On the initial request for a rendition  throw the exception.
                NodeRef renditionNode = getRenditionNode(sourceNodeRef, renditionName);
                if (renditionNode == null)
                {
                    throw e;
                }
            }

            @Override
            public void throwIllegalStateExceptionIfAlreadyDone(int sourceContentHashCode)
            {
                // Avoid doing extra transforms that have already been done.
                NodeRef renditionNode = getRenditionNode(sourceNodeRef, renditionName);
                int renditionContentHashCode = getRenditionContentHashCode(renditionNode);
                if (logger.isDebugEnabled())
                {
                    logger.debug(getName() + ": Source " + sourceContentHashCode + " rendition " + renditionContentHashCode+ " hashCodes");
                }
                if (renditionContentHashCode == sourceContentHashCode)
                {
                    throw new IllegalStateException(getName() + " has already been created.");
                }
            }
        });
    }

    private void requestAsyncTransformOrRendition(NodeRef sourceNodeRef, RenderOrTransformCallBack renderOrTransform)
    {
        try
        {
            if (!isEnabled())
            {
                throw new RenditionService2Exception("Async transforms and renditions are disabled " +
                        "(system.thumbnail.generate=false or renditionService2.enabled=false).");
            }

            if (!nodeService.exists(sourceNodeRef))
            {
                throw new IllegalArgumentException(renderOrTransform.getName()+ ": The supplied sourceNodeRef "+sourceNodeRef+" does not exist.");
            }

            RenditionDefinition2 renditionDefinition = renderOrTransform.getRenditionDefinition();

            if (logger.isDebugEnabled())
            {
                logger.debug(renderOrTransform.getName()+ ": transform " +sourceNodeRef);
            }

            AtomicBoolean supported = new AtomicBoolean(true);
            ContentData contentData = (ContentData) nodeService.getProperty(sourceNodeRef, ContentModel.PROP_CONTENT);
            if (contentData != null && contentData.getContentUrl() != null)
            {
                String contentUrl = contentData.getContentUrl();
                String sourceMimetype = contentData.getMimetype();
                long size = contentData.getSize();
                try
                {
                    transformClient.checkSupported(sourceNodeRef, renditionDefinition, sourceMimetype, size, contentUrl);
                }
                catch (UnsupportedOperationException e)
                {
                    renderOrTransform.handleUnsupported(e);
                    supported.set(false);
                }
            }

            String user = AuthenticationUtil.getRunAsUser();
            RetryingTransactionHelper.RetryingTransactionCallback callback = () ->
            {
                int sourceContentHashCode = getSourceContentHashCode(sourceNodeRef);
                if (!supported.get())
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(renderOrTransform.getName() +" is not supported. " +
                                "The content might be too big or the source mimetype cannot be converted.");
                    }
                    failure(sourceNodeRef, renditionDefinition, sourceContentHashCode);
                }
                else
                {
                    renderOrTransform.throwIllegalStateExceptionIfAlreadyDone(sourceContentHashCode);

                    if (sourceContentHashCode != SOURCE_HAS_NO_CONTENT)
                    {
                        transformClient.transform(sourceNodeRef, renditionDefinition, user, sourceContentHashCode);
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug(renderOrTransform.getName() + ": Source had no content.");
                        }
                        failure(sourceNodeRef, renditionDefinition, sourceContentHashCode);
                    }
                }
                return null;
            };
            String renditionName = renditionDefinition.getRenditionName();
            renditionRequestSheduler.scheduleRendition(callback, sourceNodeRef + renditionName);
        }
        catch (Exception e)
        {
            logger.debug(e.getMessage());
            throw e;
        }
    }

    public void failure(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, int transformContentHashCode)
    {
        // The original transaction may have already have failed
        AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    consume(sourceNodeRef, null, renditionDefinition, transformContentHashCode);
                    return null;
                }, false, true));
    }

    public void consume(NodeRef sourceNodeRef, InputStream transformInputStream, RenditionDefinition2 renditionDefinition,
                        int transformContentHashCode)
    {
        int sourceContentHashCode = getSourceContentHashCode(sourceNodeRef);
        if (logger.isDebugEnabled())
        {
            logger.debug("Consume: Source " + sourceContentHashCode + " and transform's source " + transformContentHashCode+" hashcodes");
        }

        if (renditionDefinition instanceof TransformDefinition)
        {
            TransformDefinition transformDefinition = (TransformDefinition)renditionDefinition;
            String targetMimetype = transformDefinition.getTargetMimetype();
            if (AsynchronousExtractor.isMetadataExtractMimetype(targetMimetype))
            {
                consumeExtractedMetadata(sourceNodeRef, sourceContentHashCode, transformInputStream, transformDefinition, transformContentHashCode);
            }
            else if (AsynchronousExtractor.isMetadataEmbedMimetype(targetMimetype))
            {
                consumeEmbeddedMetadata(sourceNodeRef, sourceContentHashCode, transformInputStream, transformDefinition, transformContentHashCode);
            }
            else
            {
                consumeTransformReply(sourceNodeRef, transformInputStream, transformDefinition, transformContentHashCode);
            }
        }
        else
        {
            consumeRendition(sourceNodeRef, sourceContentHashCode, transformInputStream, renditionDefinition, transformContentHashCode);
        }
    }

    private void consumeExtractedMetadata(NodeRef nodeRef, int sourceContentHashCode, InputStream transformInputStream,
                                          TransformDefinition transformDefinition, int transformContentHashCode)
    {
        if (transformInputStream == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignore transform for metadata extraction on " + nodeRef + " as it failed");
            }
        }
        else if (transformContentHashCode != sourceContentHashCode)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignore transform for metadata extraction on " + nodeRef + " as it is no longer needed");
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Set the metadata extraction on " + nodeRef);
            }
            asynchronousExtractor.setMetadata(nodeRef, transformInputStream);
        }
    }

    private void consumeEmbeddedMetadata(NodeRef nodeRef, int sourceContentHashCode, InputStream transformInputStream,
                                         TransformDefinition transformDefinition, int transformContentHashCode)
    {
        if (transformInputStream == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignore transform for metadata embed on " + nodeRef + " as it failed");
            }
        }
        else if (transformContentHashCode != sourceContentHashCode)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignore transform for metadata embed on " + nodeRef + " as it is no longer needed");
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Set the content with embedded metadata on " + nodeRef);
            }

            asynchronousExtractor.setEmbeddedMetadata(nodeRef, transformInputStream);
        }
    }

    private void consumeTransformReply(NodeRef sourceNodeRef, InputStream transformInputStream,
                                       TransformDefinition transformDefinition, int transformContentHashCode)
    {
        if (logger.isDebugEnabled())
        {
            String transformName = transformDefinition.getTransformName();
            String replyQueue = transformDefinition.getReplyQueue();
            String clientData = transformDefinition.getClientData();
            boolean success = transformInputStream != null;
            logger.info("Reply to " + replyQueue + " that the transform " + transformName +
                    " with the client data " + clientData + " " + (success ? "was successful" : "failed."));
        }
        transformReplyProvider.produceTransformEvent(sourceNodeRef, transformInputStream,
                transformDefinition, transformContentHashCode);
    }

    /**
     *  Takes a transformation (InputStream) and attaches it as a rendition to the source node.
     *  Does nothing if there is already a newer rendition.
     *  If the transformInputStream is null, this is taken to be a transform failure.
     */
    private void consumeRendition(NodeRef sourceNodeRef, int sourceContentHashCode, InputStream transformInputStream,
                                  RenditionDefinition2 renditionDefinition, int transformContentHashCode)
    {
        String renditionName = renditionDefinition.getRenditionName();
        if (transformContentHashCode != sourceContentHashCode)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignore transform for rendition " + renditionName + " on " + sourceNodeRef + " as it is no longer needed");
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Set the content of rendition " + renditionName + " on " + sourceNodeRef +
                        (transformInputStream == null ? " to null as the transform failed" : " to the transform result"));
            }

            AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () ->
                    transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                    {
                        // Ensure that the creation of a rendition does not cause updates to the modified, modifier properties on the source node
                        NodeRef renditionNode = getRenditionNode(sourceNodeRef, renditionName);
                        boolean createRenditionNode = renditionNode == null;
                        boolean sourceHasAspectRenditioned = nodeService.hasAspect(sourceNodeRef, RenditionModel.ASPECT_RENDITIONED);
                        boolean sourceChanges = !sourceHasAspectRenditioned || createRenditionNode || transformInputStream == null;
                        try
                        {
                            if (sourceChanges)
                            {
                                ruleService.disableRuleType(RuleType.UPDATE);
                                behaviourFilter.disableBehaviour(sourceNodeRef, ContentModel.ASPECT_AUDITABLE);
                                behaviourFilter.disableBehaviour(sourceNodeRef, ContentModel.ASPECT_VERSIONABLE);
                            }

                            // If they do not exist create the rendition association and the rendition node.
                            if (createRenditionNode)
                            {
                                renditionNode = createRenditionNode(sourceNodeRef, renditionDefinition);
                            }
                            else if (!nodeService.hasAspect(renditionNode, RenditionModel.ASPECT_RENDITION2))
                            {
                                nodeService.addAspect(renditionNode, RenditionModel.ASPECT_RENDITION2, null);
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("Added rendition2 aspect to rendition " + renditionName + " on " + sourceNodeRef);
                                }
                            }
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Set ThumbnailLastModified for " + renditionName);
                            }
                            setThumbnailLastModified(sourceNodeRef, renditionName);

                            if (transformInputStream != null)
                            {
                                try
                                {
                                    // Set or replace rendition content
                                    ContentWriter contentWriter = contentService.getWriter(renditionNode, DEFAULT_RENDITION_CONTENT_PROP, true);
                                    String targetMimetype = renditionDefinition.getTargetMimetype();
                                    contentWriter.setMimetype(targetMimetype);
                                    contentWriter.setEncoding(DEFAULT_ENCODING);
                                    ContentWriter renditionWriter = contentWriter;
                                    renditionWriter.putContent(transformInputStream);

                                    ContentReader contentReader = renditionWriter.getReader();
                                    long sizeOfRendition = contentReader.getSize();
                                    if (sizeOfRendition > 0L)
                                    {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("Set rendition hashcode for " + renditionName);
                                        }
                                        nodeService.setProperty(renditionNode, RenditionModel.PROP_RENDITION_CONTENT_HASH_CODE, transformContentHashCode);
                                    }
                                    else
                                    {
                                        logger.error("Transform was zero bytes for " + renditionName + " on " + sourceNodeRef);
                                        clearRenditionContentData(renditionNode);
                                    }
                                }
                                catch (Exception e)
                                {
                                    logger.error("Failed to copy transform InputStream into rendition " + renditionName + " on " + sourceNodeRef);
                                    throw e;
                                }
                            }
                            else
                            {
                                clearRenditionContentData(renditionNode);
                            }

                            if (!sourceHasAspectRenditioned)
                            {
                                nodeService.addAspect(sourceNodeRef, RenditionModel.ASPECT_RENDITIONED, null);
                            }
                        }
                        catch (Exception e)
                        {
                            throw new RenditionService2Exception(TRANSFORMING_ERROR_MESSAGE + e.getMessage(), e);
                        }
                        finally
                        {
                            if (sourceChanges)
                            {
                                behaviourFilter.enableBehaviour(sourceNodeRef, ContentModel.ASPECT_AUDITABLE);
                                behaviourFilter.enableBehaviour(sourceNodeRef, ContentModel.ASPECT_VERSIONABLE);
                                ruleService.enableRuleType(RuleType.UPDATE);
                            }
                        }
                        return null;
                    }, false, true));
        }
    }

    // Based on original AbstractRenderingEngine.createRenditionNodeAssoc
    private NodeRef createRenditionNode(NodeRef sourceNode, RenditionDefinition2 renditionDefinition)
    {
        String renditionName = renditionDefinition.getRenditionName();

        Map<QName, Serializable> nodeProps = new HashMap<QName, Serializable>();
        nodeProps.put(ContentModel.PROP_NAME, renditionName);
        nodeProps.put(ContentModel.PROP_THUMBNAIL_NAME, renditionName);
        nodeProps.put(ContentModel.PROP_CONTENT_PROPERTY_NAME, ContentModel.PROP_CONTENT);
        nodeProps.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);

        QName assocName = createQName(NamespaceService.CONTENT_MODEL_1_0_URI, renditionName);
        QName assocType = RenditionModel.ASSOC_RENDITION;
        QName nodeType = ContentModel.TYPE_THUMBNAIL;

        ChildAssociationRef childAssoc = nodeService.createNode(sourceNode, assocType, assocName, nodeType, nodeProps);
        NodeRef renditionNode = childAssoc.getChildRef();

        nodeService.addAspect(renditionNode, RenditionModel.ASPECT_RENDITION2, null);
        nodeService.addAspect(renditionNode, RenditionModel.ASPECT_HIDDEN_RENDITION, null);

        if (logger.isDebugEnabled())
        {
            logger.debug("Created " + renditionName + " rendition node " + childAssoc.getChildRef() + " as a child of " + sourceNode);
        }

        return renditionNode;
    }

    // Based on code from org.alfresco.repo.thumbnail.ThumbnailServiceImpl.addThumbnailModificationData
    private void setThumbnailLastModified(NodeRef sourceNodeRef, String renditionName)
    {
        String prefix = renditionName + ':';
        final String lastModifiedValue = prefix + System.currentTimeMillis();

        if (logger.isTraceEnabled())
        {
            logger.trace("Setting thumbnail last modified date to " + lastModifiedValue +" on source node: " + sourceNodeRef);
        }

        if (nodeService.hasAspect(sourceNodeRef, ContentModel.ASPECT_THUMBNAIL_MODIFICATION))
        {
            List<String> thumbnailMods = (List<String>) nodeService.getProperty(sourceNodeRef, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA);
            String target = null;
            for (String currThumbnailMod: thumbnailMods)
            {
                if (currThumbnailMod.startsWith(prefix))
                {
                    target = currThumbnailMod;
                }
            }
            if (target != null)
            {
                thumbnailMods.remove(target);
            }
            thumbnailMods.add(lastModifiedValue);
            nodeService.setProperty(sourceNodeRef, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA, (Serializable) thumbnailMods);
        }
        else
        {
            List<String> thumbnailMods = Collections.singletonList(lastModifiedValue);
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA, (Serializable) thumbnailMods);
            nodeService.addAspect(sourceNodeRef, ContentModel.ASPECT_THUMBNAIL_MODIFICATION, properties);
        }
    }

    /**
     * Returns the hash code of the source node's content url. As transformations may be returned in a different
     * sequences to which they were requested, this is used work out if a rendition should be replaced.
     */
    private int getSourceContentHashCode(NodeRef sourceNodeRef)
    {
        int hashCode = SOURCE_HAS_NO_CONTENT;
        ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(sourceNodeRef, PROP_CONTENT));
        if (contentData != null)
        {
            // Originally we used the contentData URL, but that is not enough if the mimetype changes.
            String contentString = contentData.getContentUrl()+contentData.getMimetype();
            if (contentString != null)
            {
                hashCode = contentString.hashCode();
            }
        }
        return hashCode;
    }

    /**
     * Returns the hash code of source node's content url on the rendition node (node may be null) if it does not exist.
     * Used work out if a rendition should be replaced. {@code -2} is returned if the rendition does not exist or was
     * not created by RenditionService2. {@code -1} is returned if there was no source content or the rendition failed.
     */
    private int getRenditionContentHashCode(NodeRef renditionNode)
    {
        if ( renditionNode == null || !nodeService.hasAspect(renditionNode, RenditionModel.ASPECT_RENDITION2))
        {
            return RENDITION2_DOES_NOT_EXIST;
        }

        Serializable hashCode = nodeService.getProperty(renditionNode, PROP_RENDITION_CONTENT_HASH_CODE);
        return hashCode == null
                ? SOURCE_HAS_NO_CONTENT
                : (int)hashCode;
    }

    private NodeRef getRenditionNode(NodeRef sourceNodeRef, String renditionName)
    {
        QName renditionQName = createQName(NamespaceService.CONTENT_MODEL_1_0_URI, renditionName);
        List<ChildAssociationRef> renditionAssocs = nodeService.getChildAssocs(sourceNodeRef, RenditionModel.ASSOC_RENDITION, renditionQName);
        return renditionAssocs.isEmpty() ? null : renditionAssocs.get(0).getChildRef();
    }

    // Only called by the old service to work out if a rendition was created by the new service.
    public boolean isCreatedByRenditionService2(NodeRef sourceNodeRef, String renditionName)
    {
        boolean result = false;
        NodeRef renditionNode = getRenditionNode(sourceNodeRef, renditionName);
        if (renditionNode != null)
        {
            result = nodeService.hasAspect(renditionNode, RenditionModel.ASPECT_RENDITION2);
        }
        return result;
    }

    // Only called by the old service, so that it can take over. Normally RenditionService2 just updates
    // rendition nodes to new versions, or to mark them as failed. The older service creates new nodes.
    public void deleteRendition(NodeRef sourceNodeRef, String renditionName)
    {
        NodeRef renditionNode = getRenditionNode(sourceNodeRef, renditionName);
        if (renditionNode != null)
        {
            if (nodeService.hasAspect(renditionNode, RenditionModel.ASPECT_RENDITION2))
            {
                nodeService.deleteNode(renditionNode);
            }
        }
    }

    /**
     * Clears source nodeRef rendition content and content hash code using supplied rendition name
     *
     * @param sourceNodeRef
     * @param renditionName
     */
    public void clearRenditionContentData(NodeRef sourceNodeRef, String renditionName)
    {
        clearRenditionContentData(getRenditionNode(sourceNodeRef, renditionName));
    }

    /**
     * Clears supplied rendition node content (if exists) and content hash code
     *
     * @param renditionNode
     */
    private void clearRenditionContentData(NodeRef renditionNode)
    {
        if (renditionNode != null)
        {
            Serializable content = nodeService.getProperty(renditionNode, PROP_CONTENT);
            if (content != null)
            {
                nodeService.removeProperty(renditionNode, PROP_CONTENT);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Cleared rendition content");
                }
            }
            nodeService.removeProperty(renditionNode, PROP_RENDITION_CONTENT_HASH_CODE);
            if (logger.isDebugEnabled())
            {
                logger.debug("Cleared rendition hashcode");
            }
        }
    }

    /**
     * This method checks whether the specified source node is of a content class which has been registered for
     * rendition prevention.
     *
     * @param sourceNode the node to check.
     * @throws RenditionService2PreventedException if the source node is configured for rendition prevention.
     */
    // This code is based on the old RenditionServiceImpl.checkSourceNodeForPreventionClass(...)
    private void checkSourceNodeForPreventionClass(NodeRef sourceNode)
    {
        if (sourceNode != null && nodeService.exists(sourceNode))
        {
            // A node's content class is its type and all its aspects.
            Set<QName> nodeContentClasses = nodeService.getAspects(sourceNode);
            nodeContentClasses.add(nodeService.getType(sourceNode));

            for (QName contentClass : nodeContentClasses)
            {
                if (renditionPreventionRegistry.isContentClassRegistered(contentClass))
                {
                    String msg = "Node " + sourceNode + " cannot be renditioned as it is of class " + contentClass;
                    logger.debug(msg);
                    throw new RenditionService2PreventedException(msg);
                }
            }
        }
    }

    private List<ChildAssociationRef> getRenditionChildAssociations(NodeRef sourceNodeRef)
    {
        // Copy of code from the original RenditionService.
        List<ChildAssociationRef> result = Collections.emptyList();

        // Check that the node has the renditioned aspect applied
        if (nodeService.hasAspect(sourceNodeRef, RenditionModel.ASPECT_RENDITIONED))
        {
            // Get all the renditions that match the given rendition name
            result = nodeService.getChildAssocs(sourceNodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
        }
        return result;
    }

    @Override
    // Only returns valid renditions. These may be from RenditionService2 or original RenditionService.
    public List<ChildAssociationRef> getRenditions(NodeRef sourceNodeRef)
    {
        List<ChildAssociationRef> result = new ArrayList<>();
        List<ChildAssociationRef> childAsocs = getRenditionChildAssociations(sourceNodeRef);

        for (ChildAssociationRef childAssoc : childAsocs)
        {
            NodeRef renditionNode =  childAssoc.getChildRef();
            if (isRenditionAvailable(sourceNodeRef, renditionNode))
            {
                result.add(childAssoc);
            }
        }
        return result;
    }

    /**
     * Indicates if the rendition is available. Failed renditions (there was an error) don't have a contentUrl
     * and out of date renditions or those still being created don't have a matching contentHashCode.
     */
    public boolean isRenditionAvailable(NodeRef sourceNodeRef, NodeRef renditionNode)
    {
        boolean available = true;
        if (nodeService.hasAspect(renditionNode, RenditionModel.ASPECT_RENDITION2))
        {
            Serializable contentUrl = nodeService.getProperty(renditionNode, ContentModel.PROP_CONTENT);
            if (contentUrl == null)
            {
                available = false;
            }
            else
            {
                int sourceContentHashCode = getSourceContentHashCode(sourceNodeRef);
                int renditionContentHashCode = getRenditionContentHashCode(renditionNode);
                if (logger.isDebugEnabled())
                {
                    logger.debug("isRenditionAvailable source " + sourceContentHashCode + " and rendition " + renditionContentHashCode+" hashcodes");
                }
                if (sourceContentHashCode != renditionContentHashCode)
                {
                    available = false;
                }
            }
        }
        return available;
    }

    @Override
    // Only returns a valid renditions. This may be from RenditionService2 or original RenditionService.
    public ChildAssociationRef getRenditionByName(NodeRef sourceNodeRef, String renditionName)
    {
        // Based on code from the original RenditionService. renditionName is a String rather than a QName.
        List<ChildAssociationRef> renditions = Collections.emptyList();

        // Thumbnails have a cm: prefix.
        QName renditionQName = createQName(NamespaceService.CONTENT_MODEL_1_0_URI, renditionName);

        // Check that the sourceNodeRef has the renditioned aspect applied
        if (nodeService.hasAspect(sourceNodeRef, RenditionModel.ASPECT_RENDITIONED))
        {
            // Get all the renditions that match the given rendition name -
            // there should only be 1 (or 0)
            renditions = this.nodeService.getChildAssocs(sourceNodeRef, RenditionModel.ASSOC_RENDITION, renditionQName);
        }
        if (renditions.isEmpty())
        {
            return null;
        }
        else
        {
            if (renditions.size() > 1 && logger.isDebugEnabled())
            {
                logger.debug("Unexpectedly found " + renditions.size() + " renditions of name " + renditionQName + " on node " + sourceNodeRef);
            }
            ChildAssociationRef childAssoc = renditions.get(0);
            NodeRef renditionNode = childAssoc.getChildRef();
            return !isRenditionAvailable(sourceNodeRef, renditionNode) ? null: childAssoc;
        }
    }

    @Override
    public void clearRenditionContentDataInTransaction(NodeRef renditionNode)
    {
        AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    clearRenditionContentData(renditionNode);
                    return null;
                }, false, true));
    }

    @Override
    public boolean isEnabled()
    {
        return enabled && thumbnailsEnabled;
    }

    @Override
    public void onContentUpdate(NodeRef sourceNodeRef, boolean newContent)
    {
        if (isEnabled())
        {
            if (nodeService.exists(sourceNodeRef))
            {
                logger.debug("onContentUpdate on " + sourceNodeRef);
                List<ChildAssociationRef> childAssocs = getRenditionChildAssociations(sourceNodeRef);
                for (ChildAssociationRef childAssoc : childAssocs)
                {
                    NodeRef renditionNodeRef = childAssoc.getChildRef();
                    // TODO: This check will not be needed once the original RenditionService is removed.
                    if (nodeService.hasAspect(renditionNodeRef, RenditionModel.ASPECT_RENDITION2))
                    {
                        QName childAssocQName = childAssoc.getQName();
                        String renditionName = childAssocQName.getLocalName();
                        RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(renditionName);
                        if (renditionDefinition != null)
                        {
                            clearRenditionContentData(sourceNodeRef, renditionName);
                            render(sourceNodeRef, renditionName);
                        }
                        else
                        {
                            logger.debug("onContentUpdate rendition " + renditionName + " only exists in the original rendition service.");
                        }
                    }
                }
            }
            else
            {
                logger.debug("onContentUpdate rendition " + sourceNodeRef + " does not exist.");
            }
        }
    }

}
