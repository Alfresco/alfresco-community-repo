/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.publishing;

import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.model.ContentModel.TYPE_FOLDER;
import static org.alfresco.repo.publishing.PublishingModel.ASSOC_PUBLISHING_QUEUE;
import static org.alfresco.repo.publishing.PublishingModel.NAMESPACE;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_PUBLISHING_QUEUE;

import java.util.List;

import org.alfresco.repo.node.NodeUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.beans.BeansException;

/**
 * Returns a properly configured Environment. The factory is multi-tenancy enabled, returning the correct Environment object for the current domain.
 * 
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingRootObject
{
    public static final String NAME = "publishingRootObject";
    protected static final QName CHANNELS_QNAME = QName.createQName(NAMESPACE, "channels");
    
    private NodeService nodeService;
    private PublishingEventHelper publishingEventHelper;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private PermissionService permissionService;
    
    private StoreRef publishingStore;
    private String publishingRootPath;
    
    
    /**
     * @return the approprieate {@link Environment} for the current domain.
     * @throws BeansException
     */
    public Environment getEnvironment() throws BeansException
    {
        return createEnvironment();
    }

    public NodeRef getChannelContainer()
    {
        return getEnvironment().getChannelsContainer();
    }
    
    public PublishingQueueImpl getPublishingQueue()
    {
        return getEnvironment().getPublishingQueue();
    }
    
    private Environment createEnvironment()
    {
        return AuthenticationUtil.runAs(new RunAsWork<Environment>()
        {
            public Environment doWork() throws Exception
            {
                NodeRef environmentNode = getEnvironmentNode();
                PublishingQueueImpl queue = createPublishingQueue(environmentNode);
                NodeRef channelsContainer = getChannelsContainer(environmentNode);
                return new Environment(environmentNode, queue, channelsContainer);
            }

        }, AuthenticationUtil.getSystemUserName());
    }

    private NodeRef getChannelsContainer(NodeRef environmentNode)
    {
        List<ChildAssociationRef> childAssocs = 
            nodeService.getChildAssocs(environmentNode,ASSOC_CONTAINS, CHANNELS_QNAME);
        NodeRef channels = NodeUtils.getSingleChildAssocNode(childAssocs, true);
        if (channels == null)
        {
            // No channels container.
            channels = nodeService.createNode(environmentNode,
                    ASSOC_CONTAINS,
                    CHANNELS_QNAME,
                    TYPE_FOLDER).getChildRef();
        }
        return channels;
    }

    private PublishingQueueImpl createPublishingQueue(NodeRef environmentNode)
    {
        NodeRef queueNode = getPublishingQueueNode(environmentNode);
        return new PublishingQueueImpl(queueNode, publishingEventHelper);
    }

    private NodeRef getPublishingQueueNode(NodeRef environmentNode)
    {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(environmentNode, ASSOC_PUBLISHING_QUEUE, RegexQNamePattern.MATCH_ALL);
        NodeRef queueNode = NodeUtils.getSingleChildAssocNode(childAssocs, true);
        if (queueNode == null)
        {
            // No publishing queue
            queueNode = nodeService.createNode(environmentNode,
                    ASSOC_PUBLISHING_QUEUE,
                    QName.createQName(NAMESPACE, "publishingQueue"),
                    TYPE_PUBLISHING_QUEUE).getChildRef();
            permissionService.setPermission(queueNode, PermissionService.ALL_AUTHORITIES, PermissionService.ADD_CHILDREN, true);
        }
        return queueNode;
    }

    private NodeRef getEnvironmentNode()
    {
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                return findEnvrionmentNode();
            }
        }, true);
    }

    private NodeRef findEnvrionmentNode()
    {
        NodeRef rootNode = nodeService.getRootNode(publishingStore);
        List<NodeRef> refs = searchService.selectNodes(rootNode, publishingRootPath, null, namespaceService, false);
        if (refs.size() != 1)
        {
            String msg = "Invalid publishing root path: " + publishingRootPath + " - found: " + refs.size();
            throw new IllegalStateException(msg);
        }
        return refs.get(0);
    }

    /**
     * @param publishingStore the publishingStore to set
     */
    public void setPublishingStore(String publishingStore)
    {
        this.publishingStore = new StoreRef(publishingStore);
    }
    
    /**
     * @param publishingRootPath the publishingRootPath to set
     */
    public void setPublishingRootPath(String publishingRootPath)
    {
        this.publishingRootPath = publishingRootPath;
    }
    
    /**
     * @param retryingTransactionHelper the retryingTransactionHelper to set
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param publishingEventHelper the publishingEventHelper to set
     */
    public void setPublishingEventHelper(PublishingEventHelper publishingEventHelper)
    {
        this.publishingEventHelper = publishingEventHelper;
    }
    
    /**
     * @param searchService the searchService to set
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
}