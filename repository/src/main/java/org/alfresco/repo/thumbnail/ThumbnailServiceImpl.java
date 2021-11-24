/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.content.transform.swf.SWFTransformationOptions;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.rendition.executer.ImageRenderingEngine;
import org.alfresco.repo.rendition.executer.ReformatRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.thumbnail.FailedThumbnailInfo;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.transaction.TransactionListener;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * @author Roy Wetherall
 * @author Neil McErlean
 *
 * @deprecated The thumbnails code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class ThumbnailServiceImpl implements ThumbnailService,
                                             NodeServicePolicies.BeforeCreateNodePolicy,
                                             NodeServicePolicies.OnCreateNodePolicy,
                                             NodeServicePolicies.OnDeleteNodePolicy
{
    /** Logger */
    private static Log logger = LogFactory.getLog(ThumbnailServiceImpl.class);
    
    /** Error messages */
    private static final String ERR_NO_PARENT = "Thumbnail has no parent so update cannot take place.";
    
    /** Mimetype wildcard postfix */
    private static final String SUBTYPES_POSTFIX = "/*";

    /** Transaction resources name */
    private static final String THUMBNAIL_PARENT_NODES = "ThumbnailServiceImpl.ParentNodes";
    private static final String THUMBNAIL_TO_DELETE_NODES = "ThumbnailServiceImpl.ToDeleteNodes";

    /** Node service */
    private NodeService nodeService;
    
    /** Thumbnail registry */
    private ThumbnailRegistry thumbnailRegistry;
    
    /** Flag to enable/disable the generation of all thumbnails. */
    private boolean thumbnailsEnabled;
    
    /** Rendition service */
    private RenditionService renditionService;
    
    /** Behaviour filter */
    private BehaviourFilter behaviourFilter;
    
    /** Rule service */
    private RuleService ruleService;
    
    /**
     * The policy component.
     * @since 3.5.0
     */
    private PolicyComponent policyComponent;

    private TransactionService transactionService;

    private TransactionListener transactionListener;
    private TransactionListener thumbnailsToDeleteTransactionListener;
    
    /**
     * Set the behaviour filter.
     * 
     * @param behaviourFilter BehaviourFilter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * Set the rendition service.
     * 
     * @param renditionService RenditionService
     */
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }

    /**
     * Set the node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set thumbnail registry
     * 
     * @param thumbnailRegistry     thumbnail registry
     */
    public void setThumbnailRegistry(ThumbnailRegistry thumbnailRegistry)
    {
        this.thumbnailRegistry = thumbnailRegistry;
    }
    
    @Override public void setThumbnailsEnabled(boolean thumbnailsEnabled) { this.thumbnailsEnabled = thumbnailsEnabled; }
    
    @Override public boolean getThumbnailsEnabled() { return this.thumbnailsEnabled; }
    
    /**
     * Set the policy component to listen for various events
     * @since 3.5.0
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param ruleService   rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * Registers to listen for events of interest.
     * @since 3.5.0
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                ContentModel.TYPE_THUMBNAIL,
                new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.EVERY_EVENT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeCreateNodePolicy.QNAME,
                ContentModel.TYPE_FAILED_THUMBNAIL,
                new JavaBehaviour(this, "beforeCreateNode", Behaviour.NotificationFrequency.EVERY_EVENT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                ContentModel.TYPE_THUMBNAIL,
                new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT));

        // Register copy class behaviour
        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                ContentModel.ASPECT_THUMBNAIL_MODIFICATION,
                new JavaBehaviour(this, "getCopyCallback"));

        transactionListener = new ThumbnailTransactionListenerAdapter();
        thumbnailsToDeleteTransactionListener = new ThumbnailCleanupTransactionListenerAdapter();
    }
    
    public void beforeCreateNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName)
    {
        // When a thumbnail has failed, we must delete any existing (successful) thumbnails of that thumbnailDefinition.
        if (ContentModel.TYPE_FAILED_THUMBNAIL.equals(nodeTypeQName))
        {
            // In fact there should only be zero or one such thumbnails
            Set<QName> childNodeTypes = new HashSet<QName>();
            childNodeTypes.add(ContentModel.TYPE_THUMBNAIL);
            List<ChildAssociationRef> existingThumbnails = nodeService.getChildAssocs(parentRef, childNodeTypes);
            
            for (ChildAssociationRef chAssRef : existingThumbnails)
            {
                if (chAssRef.getQName().equals(assocQName))
                {
                    if (logger.isDebugEnabled())
                    {
                        StringBuilder msg = new StringBuilder();
                        msg.append("Deleting thumbnail node ").append(chAssRef.getChildRef());
                        logger.debug(msg.toString());
                    }
                    nodeService.deleteNode(chAssRef.getChildRef());
                }
            }
        }
        
        // We can't respond to the creation of a cm:thumbnail node at this point as they are created with
        // temporary assoc qnames and so cannot be matched to the relevant thumbnail definition.
        // Instead we must do it "onCreateNode()"
    }
    
    public void onCreateNode(ChildAssociationRef childAssoc)
    {
        NodeRef thumbnailNodeRef = childAssoc.getChildRef();
        NodeRef sourceNodeRef = childAssoc.getParentRef();

        Map<QName, Serializable> props = nodeService.getProperties(thumbnailNodeRef);
        if (logger.isDebugEnabled())
        {
        	logger.debug("Thumbnail " + thumbnailNodeRef + " props " + props);
        }

        String thumbnailName = (String) nodeService.getProperty(thumbnailNodeRef, ContentModel.PROP_NAME);

        if (logger.isDebugEnabled())
        {
            logger.debug("Thumbnail created " + childAssoc + " for sourceNodeRef " + sourceNodeRef + ", thumbnail " + thumbnailName
                          + ", thumbnailNodeRef " + thumbnailNodeRef);
        }

        // MNT-15135: Cache the associations between parent nodes and updated thumbnails,
        // in order to mark the update after the transactions that creates the thumbnail commits.
        if (nodeService.exists(thumbnailNodeRef))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Caching " + childAssoc + " in transaction resources, thumbnail " + thumbnailName);
            }
            TransactionalResourceHelper.getSet(THUMBNAIL_PARENT_NODES).add(childAssoc);
            TransactionSupportUtil.bindListener(this.transactionListener, 0);
            
            // For new version/content the child association contains a temporary thumbnail node, that gets removed later.
            // We need to add the original thumbnail node to the TransactionalResourceHelper instead.
            QName thumbnailQname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbnailName);
            if (!childAssoc.getQName().equals(thumbnailQname)) 
            {
                List<ChildAssociationRef> allAssocList = nodeService.getChildAssocs(childAssoc.getParentRef(), RegexQNamePattern.MATCH_ALL, thumbnailQname);
                for (ChildAssociationRef aChildAssoc : allAssocList)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Caching original thumbnail " + aChildAssoc + " in transaction resources, thumbnail " + thumbnailName);
                    }
                    TransactionalResourceHelper.getSet(THUMBNAIL_PARENT_NODES).add(aChildAssoc);
                }
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Thumbnail " + thumbnailNodeRef + " does not exist, thumbnail " + thumbnailName);
            }
        }
        // When a thumbnail succeeds, we must delete any existing thumbnail failure nodes
        // In fact there should only be zero or one such failedThumbnails
        Map<String, FailedThumbnailInfo> failures = getFailedThumbnails(sourceNodeRef);

        if (logger.isDebugEnabled())
        {
        	logger.debug("Thumbnail failures " + failures + " for sourceNodeRef " + sourceNodeRef + ", thumbnail " + thumbnailName);
        }

        FailedThumbnailInfo existingFailedThumbnail = failures.get(thumbnailName);

        if (logger.isDebugEnabled())
        {
        	logger.debug("Existing failed thumbnail " + existingFailedThumbnail + ", thumbnail " + thumbnailName);
        }

        if (existingFailedThumbnail != null)
        {
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Deleting failedThumbnail node ").append(existingFailedThumbnail.getFailedThumbnailNode());
                logger.debug(msg.toString());
            }
            nodeService.deleteNode(existingFailedThumbnail.getFailedThumbnailNode());
        }
    }

    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Removing from cached resources: " + childAssocRef);
        }
        TransactionalResourceHelper.getSet(THUMBNAIL_PARENT_NODES).remove(childAssocRef);
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#getThumbnailRegistry()
     */
    public ThumbnailRegistry getThumbnailRegistry()
    {
       return this.thumbnailRegistry;
    }    
    
    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#createThumbnail(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions, java.lang.String)
     */
    public NodeRef createThumbnail(NodeRef node, QName contentProperty, String mimetype, TransformationOptions transformationOptions, String thumbnailName)
    {
        return createThumbnail(node, contentProperty, mimetype, transformationOptions, thumbnailName, null);
    }
    
    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#createThumbnail(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions, java.lang.String, org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails)
     */
    public NodeRef createThumbnail(final NodeRef node, final QName contentProperty, final String mimetype,
            final TransformationOptions transformationOptions, final String thumbnailName, final ThumbnailParentAssociationDetails assocDetails)
    {
        // Parameter check
        ParameterCheck.mandatory("node", node);
        ParameterCheck.mandatory("contentProperty", contentProperty);
        ParameterCheck.mandatoryString("mimetype", mimetype);
        ParameterCheck.mandatory("transformationOptions", transformationOptions);

        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Creating thumbnail (node=" + node.toString() + "; contentProperty="
                        + contentProperty.toString() + "; mimetype=" + mimetype);
        }
        
        if (thumbnailName != null)
        {
            NodeRef existingThumbnail = getThumbnailByName(node, contentProperty, thumbnailName);
            if (existingThumbnail != null)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Creating thumbnail: There is already a thumbnail with the name '" + thumbnailName + "' (node=" + node.toString() + "; contentProperty=" + contentProperty.toString() + "; mimetype=" + mimetype);
                }
                
                // Return the thumbnail that has already been created
                return existingThumbnail;
            }
        }
        
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setForceWritable(true);
        boolean requiresNew = false;
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE)
        {
            //We can be in a read-only transaction, so force a new transaction 
            requiresNew = true;
        }
        return txnHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {

            @Override
            public NodeRef execute() throws Throwable
            {
                return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
                {
                    public NodeRef doWork() throws Exception
                    {
                       return createThumbnailNode( node, 
                                                   contentProperty,
                                                    mimetype, 
                                                    transformationOptions, 
                                                    thumbnailName, 
                                                    assocDetails);
                    }
                }, AuthenticationUtil.getSystemUserName());
            }
    
        }, false, requiresNew);
        
    }
    
    private QName getThumbnailQName(String localThumbnailName)
    {
        if (localThumbnailName == null || localThumbnailName.length() == 0)
        {
            localThumbnailName = GUID.generate();
        }
        
        // We're prepending the cm namespace here.
        QName thumbnailQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, localThumbnailName);
        return thumbnailQName;
    }

    private String getRenderingEngineNameFor(TransformationOptions options)
    {
        if (options instanceof ImageTransformationOptions)
        {
            return ImageRenderingEngine.NAME;
        }
        else if (options instanceof SWFTransformationOptions)
        {
            return ReformatRenderingEngine.NAME;
        }
        else if (options instanceof RuntimeExecutableContentTransformerOptions)
        {
            return ReformatRenderingEngine.NAME;
        }
        else
        {
            // TODO What can we do here? Can we treat this as an error?
            // Isn't this a 'standard' TransformationOptions?
            return "";
        }
    }

    /**
     * This method returns the NodeRef for the thumbnail, using the ChildAssociationRef
     * that links the sourceNode to its associated Thumbnail node.
     * 
     * @param thumbnailRef the ChildAssociationRef containing the child NodeRef.
     * @return the NodeRef of the thumbnail itself.
     */
    public NodeRef getThumbnailNode(ChildAssociationRef thumbnailRef)
    {
        return thumbnailRef.getChildRef();
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#updateThumbnail(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public void updateThumbnail(final NodeRef thumbnail, final TransformationOptions transformationOptions)
    {
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Updating thumbnail (thumbnail=" + thumbnail.toString() + ")");
        }
        
        // First check that we are dealing with a rendition object
        if (renditionService.isRendition(thumbnail))
        {
            // Get the node that is the source of the thumbnail
            ChildAssociationRef parentAssoc = renditionService.getSourceNode(thumbnail);
            
            if (parentAssoc == null)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Updating thumbnail: The thumbnails parent cannot be found (thumbnail=" + thumbnail.toString() + ")");
                }
                throw new ThumbnailException(ERR_NO_PARENT);
            }
            
            final QName renditionAssociationName = parentAssoc.getQName();
            NodeRef sourceNode = parentAssoc.getParentRef();

            // Get the content property
            QName contentProperty = (QName)nodeService.getProperty(thumbnail, ContentModel.PROP_CONTENT_PROPERTY_NAME);

            // Set the basic detail of the transformation options
            transformationOptions.setSourceNodeRef(sourceNode);
            transformationOptions.setSourceContentProperty(contentProperty);
            transformationOptions.setTargetContentProperty(ContentModel.PROP_CONTENT);

            // Do the thumbnail transformation. Rendition Definitions are persisted underneath the Data Dictionary for which Group ALL
            // has Consumer access by default. However, we cannot assume that that access level applies for all deployments. See ALF-7334.
            RenditionDefinition rendDefn = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<RenditionDefinition>()
                {
                    @Override
                    public RenditionDefinition doWork() throws Exception
                    {
                        return renditionService.loadRenditionDefinition(renditionAssociationName);
                    }
                }, AuthenticationUtil.getSystemUserName());
            
            if (rendDefn == null)
            {
                String renderingEngineName = getRenderingEngineNameFor(transformationOptions);

                rendDefn = renditionService.createRenditionDefinition(parentAssoc.getQName(), renderingEngineName);
            }
            Map<String, Serializable> params = thumbnailRegistry.getThumbnailRenditionConvertor().convert(transformationOptions, null);
            for (String key : params.keySet())
            {
                rendDefn.setParameterValue(key, params.get(key));
            }
            
            renditionService.render(sourceNode, rendDefn);
        }
        else
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Updating thumbnail: cannot update a thumbnail node that isn't the correct thumbnail type (thumbnail=" + thumbnail.toString() + ")");
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#getThumbnailByName(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String)
     */
    public NodeRef getThumbnailByName(NodeRef node, QName contentProperty, String thumbnailName)
    {
        //
        // NOTE:
        //
        // Since there is not an easy alternative and for clarity the node service is being used to retrieve the thumbnails.
        // If retrieval performance becomes an issue then this code can be replaced
        //
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Getting thumbnail by name (nodeRef=" + node.toString() + "; contentProperty=" + contentProperty.toString() + "; thumbnailName=" + thumbnailName + ")");
        }
        
        // Thumbnails have a cm: prefix.
        QName namespacedThumbnailName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, thumbnailName);
        
        ChildAssociationRef existingRendition = renditionService.getRenditionByName(node, namespacedThumbnailName);
        NodeRef thumbnail = null;
        
        // Check the child to see if it matches the content property we are concerned about.
        // We can assume there will only ever be one per content property since createThumbnail enforces this.
        if (existingRendition != null)
        {
            NodeRef child = existingRendition.getChildRef();
            Serializable contentPropertyName = this.nodeService.getProperty(child, ContentModel.PROP_CONTENT_PROPERTY_NAME);
            if (contentProperty.equals(contentPropertyName) == true)
            {
                if (validateThumbnail(child))
                {
                    thumbnail = child;
                }
            }
        }
        
        return thumbnail;
    }
    
    /**
     * @see org.alfresco.service.cmr.thumbnail.ThumbnailService#getThumbnails(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public List<NodeRef> getThumbnails(NodeRef node, QName contentProperty, String mimetype, TransformationOptions options)
    {
        List<NodeRef> thumbnails = new ArrayList<NodeRef>(5);
        
        //
        // NOTE:
        //
        // Since there is not an easy alternative and for clarity the node service is being used to retrieve the thumbnails.
        // If retrieval performance becomes an issue then this code can be replaced
        //
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Getting thumbnails (nodeRef=" + node.toString() + "; contentProperty=" + contentProperty.toString() + "; mimetype=" + mimetype + ")");
        }
        
        List<ChildAssociationRef> renditions = this.renditionService.getRenditions(node);
        
        for (ChildAssociationRef assoc : renditions)
        {
            // Check the child to see if it matches the content property we are concerned about.
            // We can assume there will only ever be one per content property since createThumbnail enforces this.
            NodeRef child = assoc.getChildRef();
            if (contentProperty.equals(this.nodeService.getProperty(child, ContentModel.PROP_CONTENT_PROPERTY_NAME)) == true &&
                matchMimetypeOptions(child, mimetype, options) == true)
            {
                if (validateThumbnail(child))
                {
                    thumbnails.add(child);
                }
            }
        }
        
        //TODO Ensure this doesn't return non-thumbnail renditions.
        return thumbnails;
    }
    
    private boolean validateThumbnail(NodeRef thumbnailNode)
    {
        boolean valid = true;
        ContentData content = (ContentData) this.nodeService.getProperty(thumbnailNode, ContentModel.PROP_CONTENT);
        // (MNT-17162) A thumbnail with an empty content is cached for post-transaction removal, to prevent the delete in read-only transactions. 
        if (content == null || content.getSize() == 0)
        {
            TransactionalResourceHelper.getSet(THUMBNAIL_TO_DELETE_NODES).add(thumbnailNode);
            TransactionSupportUtil.bindListener(this.thumbnailsToDeleteTransactionListener, 0);
            valid = false;
        }
        return valid;
    }

    @Override
    public Map<String, FailedThumbnailInfo> getFailedThumbnails(NodeRef sourceNode)
    {
        Map<String, FailedThumbnailInfo> result = Collections.emptyMap();
        
        if (nodeService.hasAspect(sourceNode, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE))
        {
            List<ChildAssociationRef> failedThumbnailChildren = nodeService.getChildAssocs(sourceNode,
                                                 ContentModel.ASSOC_FAILED_THUMBNAIL, RegexQNamePattern.MATCH_ALL);
            result = new HashMap<String, FailedThumbnailInfo>();
            for (ChildAssociationRef chAssRef : failedThumbnailChildren)
            {
                final QName failedThumbnailName = chAssRef.getQName();
                NodeRef failedThumbnailNode = chAssRef.getChildRef();
                Map<QName, Serializable> props = nodeService.getProperties(failedThumbnailNode);
                Date failureDateTime = (Date)props.get(ContentModel.PROP_FAILED_THUMBNAIL_TIME);
                int failureCount = (Integer)props.get(ContentModel.PROP_FAILURE_COUNT);
                
                final FailedThumbnailInfo failedThumbnailInfo = new FailedThumbnailInfo(failedThumbnailName.getLocalName(),
                                                                                failureDateTime, failureCount,
                                                                                failedThumbnailNode);
                result.put(failedThumbnailName.getLocalName(), failedThumbnailInfo);
            }
        }
        else
        {
        	logger.debug(sourceNode + " does not have " + ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE + " aspect");
        }

        return result;
    }
    
    /**
     * Determine whether the thumbnail meta-data matches the given mimetype and options
     * 
     * If mimetype and transformation options are null then match is guarenteed
     * 
     * @param  thumbnail     thumbnail node reference
     * @param  mimetype      mimetype
     * @param  options       transformation options
     * @return boolean       true if the mimetype and options match the thumbnail metadata, false otherwise
     */
    private boolean matchMimetypeOptions(NodeRef thumbnail, String mimetype, TransformationOptions options)
    {
        boolean result = true;
        
        if (mimetype != null)
        {
            // Check the mimetype
            String thumbnailMimetype = ((ContentData) this.nodeService.getProperty(thumbnail, ContentModel.PROP_CONTENT)).getMimetype();

            if (mimetype.endsWith(SUBTYPES_POSTFIX))
            {
                String baseMimetype = mimetype.substring(0, mimetype.length() - SUBTYPES_POSTFIX.length());
                if (thumbnailMimetype == null || thumbnailMimetype.startsWith(baseMimetype) == false)
                {
                    result = false;
                }
            }
            else
            {
                if (mimetype.equals(thumbnailMimetype) == false)
                {
                    result = false;
                }
            }
        }
        
        if (result != false && options != null)
        {
            // TODO .. check for matching options here ...
        }
        
        return result;
    }

    /**
     * Creates a {@link RenditionDefinition} with no parameters set.
     * @param thumbnailQName QName
     * @param transformationOptions TransformationOptions
     * @return RenditionDefinition
     */
    private RenditionDefinition createRawRenditionDefinition(QName thumbnailQName,
                final TransformationOptions transformationOptions)
    {
        // Create the renditionDefinition
        String renderingEngineName = getRenderingEngineNameFor(transformationOptions);
        RenditionDefinition definition = renditionService.createRenditionDefinition(thumbnailQName, renderingEngineName);
        // Track thumbnail rendition actions so cancellation can be requested
        definition.setTrackStatus(true);
        return definition;
    }

    /**
     * Creates a fully parameterized {@link RenditionDefinition}.
     * @param contentProperty QName
     * @param mimetype String
     * @param transformationOptions TransformationOptions
     * @param thumbnailQName QName
     * @param assocDetails ThumbnailParentAssociationDetails
     * @return RenditionDefinition
     */
    private RenditionDefinition createRenditionDefinition(final QName contentProperty, final String mimetype,
                final TransformationOptions transformationOptions, final QName thumbnailQName,
                final ThumbnailParentAssociationDetails assocDetails)
    {
        RenditionDefinition definition = createRawRenditionDefinition(thumbnailQName, transformationOptions);

        // Convert the TransformationOptions and ThumbnailParentAssocDetails to
        // rendition-style parameters
        Map<String, Serializable> params = thumbnailRegistry.getThumbnailRenditionConvertor().convert(transformationOptions, assocDetails);
        // Add the other parameters given in this method signature.
        params.put(AbstractRenderingEngine.PARAM_SOURCE_CONTENT_PROPERTY, contentProperty);
        params.put(AbstractRenderingEngine.PARAM_MIME_TYPE, mimetype);

        // Set the parameters on the rendition definition.
        definition.addParameterValues(params);
        return definition;
    }

    private NodeRef createThumbnailNode(final NodeRef node, final QName contentProperty,
                final String mimetype, final TransformationOptions transformationOptions, final String thumbnailName,
                final ThumbnailParentAssociationDetails assocDetails)
    {
        // Get the name of the thumbnail and add to properties map
        QName thumbnailQName = getThumbnailQName(thumbnailName);
        RenditionDefinition definition = createRenditionDefinition(contentProperty, mimetype,
                    transformationOptions, thumbnailQName, assocDetails);
        try
        {
            ChildAssociationRef thumbnailAssoc = renditionService.render(node, definition);
            NodeRef thumbnail = getThumbnailNode(thumbnailAssoc);
            setThumbnailNameProperty(thumbnail, thumbnailName);
            return thumbnail;
        } catch (RenditionServiceException rsx)
        {
            throw new ThumbnailException(rsx.getMessage(), rsx);
        }
    }

    /**
     * Sets the thumbnail name if the rendition is of type cm:thumbnail.
     * @param thumbnail NodeRef
     * @param thumbnailName String
     */
    private void setThumbnailNameProperty(NodeRef thumbnail, String thumbnailName)
    {
        if (thumbnailName != null && thumbnailName.length() > 0)
        {
            if (ContentModel.TYPE_THUMBNAIL.equals(nodeService.getType(thumbnail)))
            {
                nodeService.setProperty(thumbnail, ContentModel.PROP_THUMBNAIL_NAME, thumbnailName);
            }
        }
    }
    
    /**
     * <p>Updates the parent of the supplied {@link NodeRef} to ensure that it has the "cm:thumbnailModification" aspect
     * and sets the last modification data for it.</p>
     * @param nodeRef A {@link NodeRef} representing a thumbnail to provide last modification data for.
     */
    @SuppressWarnings("unchecked")
    private void addThumbnailModificationData(final NodeRef nodeRef, final String thumbnailName)
    {
        if (nodeService.exists(nodeRef))
        {
            if (thumbnailName != null && !nodeRef.toString().endsWith(thumbnailName))
            {
                Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
                if (modified != null)
                {
                    // Get the last modified value as a timestamp...
                    Long timestamp = modified.getTime();

                    // Create the value we want to set...
                    final String lastModifiedValue = thumbnailName + ":" + timestamp;

                    // Get the parent node (there should be only one) and apply the aspect and
                    // set the property to indicate which thumbnail the checksum refers to...
                    for (ChildAssociationRef parent: nodeService.getParentAssocs(nodeRef))
                    {
                        List<String> thumbnailMods = null;

                        NodeRef parentNode = parent.getParentRef();

                        // we don't want to audit any changes to the parent here.
                        behaviourFilter.disableBehaviour(parentNode, ContentModel.ASPECT_AUDITABLE);
                        behaviourFilter.disableBehaviour(parentNode, ContentModel.ASPECT_VERSIONABLE);
                        try
                        {
                            if (nodeService.hasAspect(parentNode, ContentModel.ASPECT_THUMBNAIL_MODIFICATION))
                            {
                                // The node already has the aspect, check to see if the current thumbnail modification exists...
                                thumbnailMods = (List<String>) nodeService.getProperty(parentNode, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA);

                                // If we have previously set last modified thumbnail data then it will exist as part of the multi-value
                                // property. The value will consist of the "cm:thumbnailName" value delimited with a ":" and then the
                                // timestamp. We need to find the appropriate entry in the multivalue property and then update it
                                String target = null;
                                for (String currThumbnailMod: thumbnailMods)
                                {
                                    if (currThumbnailMod.startsWith(thumbnailName))
                                    {
                                        target = currThumbnailMod;
                                    }
                                }

                                // Remove the previous value
                                if (target != null)
                                {
                                    thumbnailMods.remove(target);
                                }

                                // Add the timestamp...
                                thumbnailMods.add(lastModifiedValue);

                                // Set the property...
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("Setting thumbnail last modified date to " + lastModifiedValue +" on parent node: " + parentNode);
                                }
                                ruleService.disableRuleType(RuleType.UPDATE);
                                try
                                {
                                    nodeService.setProperty(parentNode, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA, (Serializable) thumbnailMods);
                                }
                                finally
                                {
                                    ruleService.enableRuleType(RuleType.UPDATE);
                                }
                            }
                            else
                            {
                                // If the aspect has not previously been added then we'll need to set it now...
                                thumbnailMods = new ArrayList<String>();
                                thumbnailMods.add(lastModifiedValue);

                                // Add the aspect with the new property...
                                Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                                properties.put(ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA, (Serializable) thumbnailMods);

                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("Adding " + ContentModel.ASPECT_THUMBNAIL_MODIFICATION + " aspect to parent node: " + parentNode);
                                }
                                ruleService.disableRuleType(RuleType.UPDATE);
                                try
                                {
                                    nodeService.addAspect(parentNode, ContentModel.ASPECT_THUMBNAIL_MODIFICATION, properties);
                                }
                                finally
                                {
                                    ruleService.enableRuleType(RuleType.UPDATE);
                                }
                            }
                        }
                        finally
                        {
                            behaviourFilter.enableBehaviour(parentNode, ContentModel.ASPECT_AUDITABLE);
                            behaviourFilter.enableBehaviour(parentNode, ContentModel.ASPECT_VERSIONABLE);
                        }
                    }
                }
            }
        }
    }

    private class ThumbnailTransactionListenerAdapter extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Starting aftercommit listener execution.");
            }
            final Set<ChildAssociationRef> childAssocs =  TransactionalResourceHelper.getSet(THUMBNAIL_PARENT_NODES);

            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    // MNT-15135: Do the property update in a new transaction, in case the parent node was already changed (has a different version) in another transaction.
                    // This way the failures will not propagate up the retry stack.
                    RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                    txnHelper.setForceWritable(true);
                    txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                    {
                        @Override
                        public Void execute() throws Throwable
                        {
                            for (ChildAssociationRef childAssoc : childAssocs)
                            {
                                NodeRef thumbnailNodeRef = childAssoc.getChildRef();
                                NodeRef sourceNodeRef = childAssoc.getParentRef();

                                // check if thumbnail node exists
                                if (thumbnailNodeRef == null || !nodeService.exists(thumbnailNodeRef))
                                {
                                    logger.debug("Thumbnail node " + thumbnailNodeRef + " does not exist. It will be skipped");
                                    continue;
                                }
                                // check if source node exists
                                if (sourceNodeRef == null || !nodeService.exists(sourceNodeRef))
                                {
                                    logger.debug("Parent node " + sourceNodeRef + " does not exist. It will be skipped");
                                    continue;
                                }

                                String thumbnailName = (String) nodeService.getProperty(thumbnailNodeRef, ContentModel.PROP_NAME);
                                // Update the parent node with the thumbnail update...
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("Found cached parent node " + sourceNodeRef + " in transactional resources");
                                    logger.debug("Adding thumbnail modification data.");
                                }
                                addThumbnailModificationData(thumbnailNodeRef, thumbnailName);
                            }
                            return null;
                        }
                    }, false, true);
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }
    
    private class ThumbnailCleanupTransactionListenerAdapter extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Starting aftercommit listener execution.");
            }
            final Set<NodeRef> thumbnailToDelete =  TransactionalResourceHelper.getSet(THUMBNAIL_TO_DELETE_NODES);

            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                   RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                    txnHelper.setForceWritable(true);
                    txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                    {
                        @Override
                        public Void execute() throws Throwable
                        {
                            for (NodeRef node : thumbnailToDelete)
                            {
                                // Update lastThumbnailModification on parent node
                                // so that the thumbnail will be recreated when browsing share
                                String thumbnailName = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
                                addThumbnailModificationData(node, thumbnailName);

                                nodeService.deleteNode(node);
                            }
                            return null;
                        }
                    }, false, true);
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    /**
     * See init - eg. registers "do nothing" copy behaviour for "cm:thumbnailModification" aspect
     * 
     * @return          Returns {@link DoNothingCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }
}
