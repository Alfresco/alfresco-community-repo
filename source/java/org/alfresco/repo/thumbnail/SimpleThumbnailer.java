/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * A simplistic policy that generates all applicable thumbnails for content as it is added or updated. As this is done
 * synchronously, this is not recommended for production use.
 * 
 * @author dward
 */
public class SimpleThumbnailer extends TransactionListenerAdapter implements
        ContentServicePolicies.OnContentUpdatePolicy, InitializingBean
{

    private static final Log logger = LogFactory.getLog(SimpleThumbnailer.class);

    /** The key under which nodes to thumbnail at the end of the transaction are stored. */
    private static final String KEY_POST_TXN_NODES_TO_THUMBNAIL = "SimpleThumbnailer.KEY_POST_TXN_NODES_TO_THUMBNAIL";

    /** The component to register the behaviour with. */
    private PolicyComponent policyComponent;

    /** The node service. */
    private NodeService nodeService;

    /** The transaction service. */
    private TransactionService transactionService;

    /** The thumbnail service. */
    private ThumbnailService thumbnailService;

    /**
     * Sets the policy component.
     * 
     * @param policyComponent
     *            used for registrations
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Sets the node service.
     * 
     * @param nodeService
     *            the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the transaction service.
     * 
     * @param transactionService
     *            the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Sets the thumbnail service.
     * 
     * @param thumbnailService
     *            the thumbnail service
     */
    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }

    /**
     * Registers the policy behaviour methods.
     */
    public void afterPropertiesSet()
    {
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onContentUpdate"),
                this, new JavaBehaviour(this, "onContentUpdate"));
    }

    /**
     * When content changes, thumbnails are (re)generated.
     * 
     * @param nodeRef
     *            the node ref
     * @param newContent
     *            is the content new?
     */
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        if (!this.nodeService.getType(nodeRef).equals(ContentModel.TYPE_THUMBNAIL)
                && this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT) != null)
        {
            // Bind this service to the transaction and add the node to the set of nodes to thumbnail post txn
            AlfrescoTransactionSupport.bindListener(this);
            getPostTxnNodesToThumbnail().add(nodeRef);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
     */
    @Override
    public void afterCommit()
    {
        for (final NodeRef nodeRef : getPostTxnNodesToThumbnail())
        {
            if (!this.nodeService.exists(nodeRef))
            {
        	continue;
            }
            Serializable value = this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
            if (contentData != null)
            {
                List<ThumbnailDefinition> thumbnailDefinitions = this.thumbnailService.getThumbnailRegistry()
                    .getThumbnailDefinitions(contentData.getMimetype(), contentData.getSize());
                for (final ThumbnailDefinition thumbnailDefinition : thumbnailDefinitions)
                {
                    final NodeRef existingThumbnail = this.thumbnailService.getThumbnailByName(nodeRef,
                            ContentModel.PROP_CONTENT, thumbnailDefinition.getName());
                    try
                    {
                        // Generate each thumbnail in its own transaction, so that we can recover if one of them goes wrong
                        this.transactionService.getRetryingTransactionHelper().doInTransaction(
                                new RetryingTransactionCallback<Object>()
                                {

                                    public Object execute() throws Throwable
                                    {
                                        if (existingThumbnail == null)
                                        {
                                            if (SimpleThumbnailer.logger.isDebugEnabled())
                                            {
                                                SimpleThumbnailer.logger.debug("Creating thumbnail \""
                                                        + thumbnailDefinition.getName() + "\" for node " + nodeRef.getId());
                                            }
                                            SimpleThumbnailer.this.thumbnailService.createThumbnail(nodeRef,
                                                    ContentModel.PROP_CONTENT, thumbnailDefinition.getMimetype(),
                                                    thumbnailDefinition.getTransformationOptions(), thumbnailDefinition
                                                            .getName());
                                        }
                                        else
                                        {
                                            SimpleThumbnailer.logger.debug("Updating thumbnail \""
                                                    + thumbnailDefinition.getName() + "\" for node " + nodeRef.getId());
                                            SimpleThumbnailer.this.thumbnailService.updateThumbnail(existingThumbnail,
                                                    thumbnailDefinition.getTransformationOptions());
                                        }
                                        return null;
                                    }
                                }, false, true);
                    }
                    catch (Exception e)
                    {
                        SimpleThumbnailer.logger.warn("Failed to generate thumbnail \"" + thumbnailDefinition.getName()
                                + "\" for node " + nodeRef.getId(), e);
                    }
                }
            }
        }
    }

    /**
     * Gets the txn-bound set of nodes that need thumbnailing.
     * 
     * @return the set of nodes that need thumbnailing
     */
    private Set<NodeRef> getPostTxnNodesToThumbnail()
    {
        @SuppressWarnings("unchecked")
        Set<NodeRef> nodesToThumbnail = (Set<NodeRef>) AlfrescoTransactionSupport
                .getResource(SimpleThumbnailer.KEY_POST_TXN_NODES_TO_THUMBNAIL);
        if (nodesToThumbnail == null)
        {
            nodesToThumbnail = new LinkedHashSet<NodeRef>(11);
            AlfrescoTransactionSupport
                    .bindResource(SimpleThumbnailer.KEY_POST_TXN_NODES_TO_THUMBNAIL, nodesToThumbnail);
        }
        return nodesToThumbnail;
    }

}
