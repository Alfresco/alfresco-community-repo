/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.events;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.sync.events.types.Event;
import org.alfresco.sync.events.types.NodeAddedEvent;
import org.alfresco.sync.events.types.NodeCheckOutCancelledEvent;
import org.alfresco.sync.events.types.NodeCheckedInEvent;
import org.alfresco.sync.events.types.NodeCheckedOutEvent;
import org.alfresco.sync.events.types.NodeCommentedEvent;
import org.alfresco.sync.events.types.NodeContentGetEvent;
import org.alfresco.sync.events.types.NodeContentPutEvent;
import org.alfresco.sync.events.types.NodeFavouritedEvent;
import org.alfresco.sync.events.types.NodeLikedEvent;
import org.alfresco.sync.events.types.NodeLockedEvent;
import org.alfresco.sync.events.types.NodeMovedEvent;
import org.alfresco.sync.events.types.NodeRemovedEvent;
import org.alfresco.sync.events.types.NodeRenamedEvent;
import org.alfresco.sync.events.types.NodeTaggedEvent;
import org.alfresco.sync.events.types.NodeUnFavouritedEvent;
import org.alfresco.sync.events.types.NodeUnLikedEvent;
import org.alfresco.sync.events.types.NodeUnTaggedEvent;
import org.alfresco.sync.events.types.NodeUnlockedEvent;
import org.alfresco.sync.events.types.NodeUpdatedEvent;
import org.alfresco.sync.events.types.Property;
import org.alfresco.sync.events.types.authority.AuthorityAddedToGroupEvent;
import org.alfresco.sync.events.types.authority.AuthorityRemovedFromGroupEvent;
import org.alfresco.sync.events.types.authority.GroupDeletedEvent;
import org.alfresco.sync.events.types.permission.InheritPermissionsDisabledEvent;
import org.alfresco.sync.events.types.permission.InheritPermissionsEnabledEvent;
import org.alfresco.sync.events.types.permission.LocalPermissionGrantedEvent;
import org.alfresco.sync.events.types.permission.LocalPermissionRevokedEvent;
import org.alfresco.sync.events.types.recordsmanagement.FileClassifiedEvent;
import org.alfresco.sync.events.types.recordsmanagement.FileUnclassifiedEvent;
import org.alfresco.sync.events.types.recordsmanagement.RecordCreatedEvent;
import org.alfresco.sync.events.types.recordsmanagement.RecordRejectedEvent;
import org.alfresco.model.ContentModel;
import org.alfresco.sync.repo.Client;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Event service implementation. Generates events and sends them to an event queue.
 * 
 * TODO: transaction rollback handling, deletion of nodes (currently tied to beforeDeleteNode).
 * 
 * @author steveglover
 */
public class EventsServiceImpl extends AbstractEventsService implements EventsService
{
    private static Log logger = LogFactory.getLog(EventsServiceImpl.class);

    private static final String RM_MODEL_PROP_NAME_RECORD_ORIGINATING_LOCATION = "PROP_RECORD_ORIGINATING_LOCATION";
    private static final String RECORDS_MANAGEMENT_MODEL = "org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel";

    public static QName PROP_RMA_RECORD_ORIGINATING_LOCATION = getRmPropOriginatingLocation();

    private NodeRenamedEvent nodeRenamedEvent(NodeInfo nodeInfo, String oldName, String newName)
    {
        String username = AuthenticationUtil.getFullyAuthenticatedUser();
        String networkId = TenantUtil.getCurrentDomain();

        String objectId = nodeInfo.getNodeId();
        String siteId = nodeInfo.getSiteId();
        String txnId = AlfrescoTransactionSupport.getTransactionId();
        long timestamp = System.currentTimeMillis();
        Long modificationTime = nodeInfo.getModificationTimestamp();
        QName nodeTypeQName = nodeInfo.getType();
        String nodeType = nodeTypeQName.toPrefixString(namespaceService);
        List<List<String>> parentNodeIds = nodeInfo.getParentNodeIds();

        List<String> newPaths = nodeInfo.getPaths();
        List<String> paths = null;

        // For site display name (title) rename events we don't want the path to be changed to the display name (title); leave it to name (id)
        if (nodeTypeQName.equals(SiteModel.TYPE_SITE))
        {
            paths = newPaths;
        }
        else
        {
            nodeInfo.updateName(oldName);
            paths = nodeInfo.getPaths();
        }

        Set<String> aspects = nodeInfo.getAspectsAsStrings();
        Map<String, Serializable> properties = nodeInfo.getProperties();

        Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

        NodeRenamedEvent event = new NodeRenamedEvent(nextSequenceNumber(), oldName, newName, txnId, timestamp, networkId, siteId, objectId, nodeType,
                paths, parentNodeIds, username, modificationTime, newPaths, alfrescoClient,aspects, properties);
        return event;
    }

    @Override
    public void nodeMoved(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        NodeRef nodeRef = newChildAssocRef.getChildRef();
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeMovedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);

            NodeRef oldParentNodeRef = oldChildAssocRef.getParentRef();
            NodeRef newParentNodeRef = newChildAssocRef.getParentRef();
            NodeRef oldNodeRef = oldChildAssocRef.getChildRef();
            NodeRef newNodeRef = newChildAssocRef.getChildRef();

            String oldParentNodeName = (String)nodeService.getProperty(oldParentNodeRef, ContentModel.PROP_NAME);
            String newParentNodeName = (String)nodeService.getProperty(newParentNodeRef, ContentModel.PROP_NAME);
            String oldNodeName = (String)nodeService.getProperty(oldNodeRef, ContentModel.PROP_NAME);
            String newNodeName = (String)nodeService.getProperty(newNodeRef, ContentModel.PROP_NAME);
            List<Path> newParentPaths = nodeService.getPaths(newParentNodeRef, false);
            List<String> newPaths = getPaths(newParentPaths, Arrays.asList(newParentNodeName, newNodeName));

            // renames are handled by an onUpdateProperties callback, we just deal with real moves here.
            if(!oldParentNodeRef.equals(newParentNodeRef))
            {
                List<List<String>> toParentNodeIds = getNodeIds(newParentPaths);
                List<Path> oldParentPaths = nodeService.getPaths(oldParentNodeRef, false);
                List<String> previousPaths = getPaths(oldParentPaths, Arrays.asList(oldParentNodeName, oldNodeName));
                List<List<String>> previousParentNodeIds = getNodeIds(oldParentPaths);

                Set<String> aspects = nodeInfo.getAspectsAsStrings();
                Map<String, Serializable> properties = nodeInfo.getProperties();
               
                Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

                Event event = new NodeMovedEvent(nextSequenceNumber(), oldNodeName, newNodeName, txnId, timestamp, networkId, siteId, objectId, nodeType, 
                        previousPaths, previousParentNodeIds, username, modificationTime, newPaths, toParentNodeIds, alfrescoClient,
                        aspects, properties);
                sendEvent(event);
            }
        }
    }

    @Override
    public void nodeRenamed(NodeRef nodeRef, String oldName, String newName)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeRenamedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            NodeRenamedEvent nodeRenamedEvent = nodeRenamedEvent(nodeInfo, oldName, newName);
            sendEvent(nodeRenamedEvent);
        }
    }

    @Override
    public void nodeTagged(final NodeRef nodeRef, final String tag)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeTaggedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Event event = new NodeTaggedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType, nodePaths,
                    pathNodeIds, username, modificationTime, tag, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void nodeTagRemoved(final NodeRef nodeRef, final String tag)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeUnTaggedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Event event = new NodeUnTaggedEvent(nextSequenceNumber(), tag, name, txnId, timestamp, networkId, siteId, objectId, nodeType, nodePaths,
                    pathNodeIds, username, modificationTime, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void nodeLiked(final NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeLikedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Event event = new NodeLikedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType, nodePaths,
                    pathNodeIds, username, modificationTime, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void nodeUnLiked(final NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeUnLikedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Event event = new NodeUnLikedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType, nodePaths,
                    pathNodeIds, username, modificationTime, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void nodeFavourited(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeFavouritedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Event event = new NodeFavouritedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType, nodePaths,
                    pathNodeIds, username, modificationTime, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void nodeUnFavourited(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeUnFavouritedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths  = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            NodeUnFavouritedEvent event = new NodeUnFavouritedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType, nodePaths,
                    pathNodeIds, username, modificationTime, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void nodeCreated(final NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeAddedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = new NodeAddedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType, nodePaths, pathNodeIds,
                    username, modificationTime, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }
    
    @Override
    public void secondaryAssociationCreated(final ChildAssociationRef secAssociation)
    {
        NodeInfo nodeInfo = getNodeInfo(secAssociation.getChildRef(), NodeAddedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            
            NodeRef secParentNodeRef = secAssociation.getParentRef();
            String secParentNodeName = (String)nodeService.getProperty(secAssociation.getParentRef(), ContentModel.PROP_NAME);
            List<Path> secParentPath = nodeService.getPaths(secParentNodeRef, true);
            List<String> nodePaths = getPaths(secParentPath, Arrays.asList(secParentNodeName, name));
            List<List<String>> pathNodeIds = this.getNodeIds(secParentPath);
            
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = new NodeAddedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType, nodePaths, pathNodeIds,
                    username, modificationTime, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }
    
    @Override
    public void secondaryAssociationDeleted(final ChildAssociationRef secAssociation)
    {
        NodeInfo nodeInfo = getNodeInfo(secAssociation.getChildRef(), NodeRemovedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            
            NodeRef secParentNodeRef = secAssociation.getParentRef();
            String secParentNodeName = (String)nodeService.getProperty(secAssociation.getParentRef(), ContentModel.PROP_NAME);
            List<Path> secParentPath = nodeService.getPaths(secParentNodeRef, true);
            List<String> nodePaths = getPaths(secParentPath, Arrays.asList(secParentNodeName, name));
            List<List<String>> pathNodeIds = this.getNodeIds(secParentPath);
            
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = new NodeRemovedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType,
                    nodePaths, pathNodeIds, username, modificationTime, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void nodeDeleted(final NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeRemovedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = new NodeRemovedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType,
                    nodePaths, pathNodeIds, username, modificationTime, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void nodeCommented(final NodeRef nodeRef, final String comment)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeCommentedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = new NodeCommentedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType, nodePaths,
                    pathNodeIds, username, modificationTime, comment, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void nodeUpdated(final NodeRef nodeRef, final Map<String, Property> propertiesAdded,
            final Set<String> propertiesRemoved, final Map<String, Property> propertiesChanged,
            final Set<String> aspectsAdded, final Set<String> aspectsRemoved)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeUpdatedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = new NodeUpdatedEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType, nodePaths,
                    pathNodeIds, username, modificationTime, propertiesAdded, propertiesRemoved, propertiesChanged,
                    aspectsAdded, aspectsRemoved, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void contentGet(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeContentGetEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = new NodeContentGetEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId,
                    objectId, nodeType, nodePaths, pathNodeIds, username, modificationTime, alfrescoClient,
                    aspects, properties);
            sendEvent(event);
        }
    }

    @Override
    public void contentWrite(NodeRef nodeRef, QName propertyQName, ContentData value)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeContentPutEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            long size;
            String mimeType;
            String encoding;
            if (value != null)
            {
                size = value.getSize();
                mimeType = value.getMimetype();
                encoding = value.getEncoding();
            }
            else
            {
                size = 0;
                mimeType = "";
                encoding = "";
            }

            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = new NodeContentPutEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId,
                    nodeType, nodePaths, pathNodeIds, username, modificationTime, size, mimeType, encoding, alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    public void nodeCheckedOut(NodeRef workingCopyNodeRef)
    {
        NodeInfo workingCopyNodeInfo = getNodeInfo(workingCopyNodeRef, NodeCheckedOutEvent.EVENT_TYPE);
        if(workingCopyNodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = workingCopyNodeInfo.getName();
            String objectId = workingCopyNodeInfo.getNodeId();
            String siteId = workingCopyNodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = workingCopyNodeInfo.getPaths();
            List<List<String>> pathNodeIds = workingCopyNodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = workingCopyNodeInfo.getModificationTimestamp();
            String nodeType = workingCopyNodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(workingCopyNodeInfo.getClient());
            String workingCopyNodeId = workingCopyNodeInfo.getNodeId();

            Set<String> aspects = workingCopyNodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = workingCopyNodeInfo.getProperties();

            Event event = new NodeCheckedOutEvent(nextSequenceNumber(), workingCopyNodeId, name, txnId, timestamp,
                    networkId, siteId, objectId, nodeType, nodePaths, pathNodeIds, username, modificationTime,
                    alfrescoClient, aspects, properties);
            sendEvent(event);
        }
    }

    public void nodeCheckOutCancelled(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeCheckOutCancelledEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = new NodeCheckOutCancelledEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId,
                    objectId, nodeType, nodePaths, pathNodeIds, username, modificationTime, alfrescoClient,
                    aspects, properties);
            sendEvent(event);
        }
    }

    public void nodeCheckedIn(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeCheckedInEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();

            String name = nodeInfo.getName();
            String objectId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());
            
            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = new NodeCheckedInEvent(nextSequenceNumber(), name, txnId, timestamp, networkId, siteId, objectId, nodeType,
                    nodePaths, pathNodeIds, username, modificationTime, alfrescoClient, aspects,
                    properties);
            sendEvent(event);
        }
    }

    @Override
    public void authorityRemovedFromGroup(String parentGroup, String childAuthority)
    {        
        if (includeEventType(AuthorityRemovedFromGroupEvent.EVENT_TYPE))
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            long timestamp = System.currentTimeMillis();
            Client client = getAlfrescoClient(ClientUtil.from(FileFilterMode.getClient()));
            
            Event event = AuthorityRemovedFromGroupEvent.builder().parentGroup(parentGroup).authorityName(childAuthority)
                    .seqNumber(nextSequenceNumber()).txnId(txnId).networkId(networkId).timestamp(timestamp).username(username).client(client).build();
            
            sendEvent(event);
        }
    }

    @Override
    public void authorityAddedToGroup(String parentGroup, String childAuthority)
    {
        if (includeEventType(AuthorityAddedToGroupEvent.EVENT_TYPE))
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            long timestamp = System.currentTimeMillis();
            Client client = getAlfrescoClient(ClientUtil.from(FileFilterMode.getClient()));
            
            Event event = AuthorityAddedToGroupEvent.builder().parentGroup(parentGroup).authorityName(childAuthority).seqNumber(nextSequenceNumber())
                    .txnId(txnId).networkId(networkId).timestamp(timestamp).username(username).client(client).build();
                    
            sendEvent(event);
        }
    }
    
    @Override
    public void groupDeleted(String groupName, boolean cascade)
    {
        if (includeEventType(GroupDeletedEvent.EVENT_TYPE))
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            long timestamp = System.currentTimeMillis();
            Client client = getAlfrescoClient(ClientUtil.from(FileFilterMode.getClient()));
            
            Event event = GroupDeletedEvent.builder().authorityName(groupName).cascade(cascade).seqNumber(nextSequenceNumber()).txnId(txnId)
                    .networkId(networkId).timestamp(timestamp).username(username).client(client).build();
            
            sendEvent(event);
        }        
    }

    @Override
    public void inheritPermissionsEnabled(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, InheritPermissionsEnabledEvent.EVENT_TYPE);
        if (nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();
            String name = nodeInfo.getName();
            String nodeId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = InheritPermissionsEnabledEvent.builder().seqNumber(nextSequenceNumber()).name(name).txnId(txnId).timestamp(timestamp)
                    .networkId(networkId).siteId(siteId).nodeId(nodeId).nodeType(nodeType).paths(nodePaths).parentNodeIds(pathNodeIds)
                    .username(username).nodeModificationTime(modificationTime).client(alfrescoClient).aspects(aspects).nodeProperties(properties)
                    .build();         
                   
            sendEvent(event);
        }
    }

    @Override
    public void inheritPermissionsDisabled(NodeRef nodeRef, boolean async)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, InheritPermissionsDisabledEvent.EVENT_TYPE);
        if (nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();
            String name = nodeInfo.getName();
            String nodeId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = InheritPermissionsDisabledEvent.builder().async(async).seqNumber(nextSequenceNumber()).name(name).txnId(txnId)
                    .timestamp(timestamp).networkId(networkId).siteId(siteId).nodeId(nodeId).nodeType(nodeType).paths(nodePaths)
                    .parentNodeIds(pathNodeIds).username(username).nodeModificationTime(modificationTime).client(alfrescoClient).aspects(aspects)
                    .nodeProperties(properties).build();      
            sendEvent(event);
        }
    }

    @Override
    public void revokeLocalPermissions(NodeRef nodeRef, String authority, String permission)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, LocalPermissionRevokedEvent.EVENT_TYPE);
        if (nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();
            String name = nodeInfo.getName();
            String nodeId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            Event event = LocalPermissionRevokedEvent.builder().authority(authority).permission(permission).seqNumber(nextSequenceNumber()).name(name)
                    .txnId(txnId).timestamp(timestamp).networkId(networkId).siteId(siteId).nodeId(nodeId).nodeType(nodeType).paths(nodePaths).parentNodeIds(pathNodeIds)
                    .username(username).nodeModificationTime(modificationTime).client(alfrescoClient).aspects(aspects).nodeProperties(properties).build();                  
            sendEvent(event);
        }
        
    }

    @Override
    public void grantLocalPermission(NodeRef nodeRef, String authority, String permission)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, LocalPermissionGrantedEvent.EVENT_TYPE);
        if (nodeInfo.checkNodeInfo())
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            String networkId = TenantUtil.getCurrentDomain();
            String name = nodeInfo.getName();
            String nodeId = nodeInfo.getNodeId();
            String siteId = nodeInfo.getSiteId();
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            List<String> nodePaths = nodeInfo.getPaths();
            List<List<String>> pathNodeIds = nodeInfo.getParentNodeIds();
            long timestamp = System.currentTimeMillis();
            Long modificationTime = nodeInfo.getModificationTimestamp();
            String nodeType = nodeInfo.getType().toPrefixString(namespaceService);
            Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

            Set<String> aspects = nodeInfo.getAspectsAsStrings();
            Map<String, Serializable> properties = nodeInfo.getProperties();

            LocalPermissionGrantedEvent event = LocalPermissionGrantedEvent.builder().authority(authority).permission(permission).seqNumber(nextSequenceNumber())
                    .name(name).txnId(txnId).timestamp(timestamp).networkId(networkId).siteId(siteId).nodeId(nodeId).nodeType(nodeType)
                    .paths(nodePaths).parentNodeIds(pathNodeIds).username(username).nodeModificationTime(modificationTime).client(alfrescoClient)
                    .aspects(aspects).nodeProperties(properties).build();
            sendEvent(event);
        }
        
    }

    @Override
    public void fileUnclassified(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, FileUnclassifiedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            Event event = FileUnclassifiedEvent.builder()
                              .seqNumber(nextSequenceNumber())
                              .name(nodeInfo.getName())
                              .txnId(AlfrescoTransactionSupport.getTransactionId())
                              .timestamp(System.currentTimeMillis())
                              .networkId(TenantUtil.getCurrentDomain())
                              .siteId(nodeInfo.getSiteId())
                              .nodeId(nodeInfo.getNodeId())
                              .nodeType(nodeInfo.getType().toPrefixString(namespaceService))
                              .paths(nodeInfo.getPaths())
                              .parentNodeIds(nodeInfo.getParentNodeIds())
                              .username(AuthenticationUtil.getFullyAuthenticatedUser())
                              .nodeModificationTime(nodeInfo.getModificationTimestamp())
                              .client(getAlfrescoClient(nodeInfo.getClient()))
                              .aspects(nodeInfo.getAspectsAsStrings())
                              .nodeProperties(nodeInfo.getProperties())
                              .build();
            sendEvent(event);
        }
    }

    @Override
    public void fileClassified(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, FileClassifiedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            Event event = FileClassifiedEvent.builder()
                              .seqNumber(nextSequenceNumber())
                              .name(nodeInfo.getName())
                              .txnId(AlfrescoTransactionSupport.getTransactionId())
                              .timestamp(System.currentTimeMillis())
                              .networkId(TenantUtil.getCurrentDomain())
                              .siteId(nodeInfo.getSiteId())
                              .nodeId(nodeInfo.getNodeId())
                              .nodeType(nodeInfo.getType().toPrefixString(namespaceService))
                              .paths(nodeInfo.getPaths())
                              .parentNodeIds(nodeInfo.getParentNodeIds())
                              .username(AuthenticationUtil.getFullyAuthenticatedUser())
                              .nodeModificationTime(nodeInfo.getModificationTimestamp())
                              .client(getAlfrescoClient(nodeInfo.getClient()))
                              .aspects(nodeInfo.getAspectsAsStrings())
                              .nodeProperties(nodeInfo.getProperties())
                              .build();
            sendEvent(event);
        }
    }

    @Override
    public void recordRejected(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, RecordRejectedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            //The event should contain the path that points to the original location of the file.
            //Since the record creation, the record might've been hidden on the collaboration site, thus removing the secondary parent-child association, 
            //we'll use a RM specific property that stores the original location of the file.
            if (PROP_RMA_RECORD_ORIGINATING_LOCATION == null)
            {
                logger.error(format("Could not generate %s event for node %s because %s property is not found.", RecordRejectedEvent.EVENT_TYPE, nodeRef, RM_MODEL_PROP_NAME_RECORD_ORIGINATING_LOCATION));
                return;
            }
            NodeRef recordOriginatingLocation = (NodeRef) nodeService.getProperty(nodeRef, PROP_RMA_RECORD_ORIGINATING_LOCATION);
            String recordOriginatingParentName = (String) nodeService.getProperty(recordOriginatingLocation, ContentModel.PROP_NAME);
            Path originatingParentPath = nodeService.getPath(recordOriginatingLocation);
            
            Event event = RecordRejectedEvent.builder()
                              .seqNumber(nextSequenceNumber())
                              .name(nodeInfo.getName())
                              .txnId(AlfrescoTransactionSupport.getTransactionId())
                              .timestamp(System.currentTimeMillis())
                              .networkId(TenantUtil.getCurrentDomain())
                              .siteId(nodeInfo.getSiteId())
                              .nodeId(nodeInfo.getNodeId())
                              .nodeType(nodeInfo.getType().toPrefixString(namespaceService))
                              .paths(getPaths(singletonList(originatingParentPath), asList(recordOriginatingParentName, nodeInfo.getName())))
                              .parentNodeIds(this.getNodeIds(singletonList(originatingParentPath)))
                              .username(AuthenticationUtil.getFullyAuthenticatedUser())
                              .nodeModificationTime(nodeInfo.getModificationTimestamp())
                              .client(getAlfrescoClient(nodeInfo.getClient()))
                              .aspects(nodeInfo.getAspectsAsStrings())
                              .nodeProperties(nodeInfo.getProperties())
                              .build();
            sendEvent(event);
        }
    }

    /**
     * <pre>
     * In order to avoid hard-coding of RM property names, the name of the property that designates the original location of a record,
     * is retrieved from the {@value #RECORDS_MANAGEMENT_MODEL} interface(marked as @AlfrescoPublicApi) using constant {@value #RM_MODEL_PROP_NAME_RECORD_ORIGINATING_LOCATION}
     * </pre>
     * 
     * @return the QName of the RM property pointing to the original location of a node that has been declared as a record
     */
    private static QName getRmPropOriginatingLocation()
    {
        QName originatingLocation = null;
        try
        {
            Class<?> recordsManagementModel = ClassUtils.getClass(RECORDS_MANAGEMENT_MODEL);
            originatingLocation = (QName) FieldUtils.readStaticField(recordsManagementModel, RM_MODEL_PROP_NAME_RECORD_ORIGINATING_LOCATION);
        }
        catch (ClassNotFoundException | IllegalAccessException e)
        {
            logger.info(format("Could not retrieve property %s from class %s. Maybe RM isn't installed, the property %s will be null.", 
                                RM_MODEL_PROP_NAME_RECORD_ORIGINATING_LOCATION, RECORDS_MANAGEMENT_MODEL, "PROP_RMA_RECORD_ORIGINATING_LOCATION"));
        }
        return originatingLocation;
    }

    @Override
    public void recordCreated(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, RecordCreatedEvent.EVENT_TYPE);
        if(nodeInfo.checkNodeInfo())
        {
            //The event should contain the path that points to the original location of the file.
            //When a node is declared as a record, a secondary association is created for the original location, hence use just that.
            List<Path> allPaths = nodeService.getPaths(nodeRef, false);
            Path primaryPath = nodeService.getPath(nodeRef);
            
            if (allPaths.size() >= 2)
            {
                allPaths.remove(primaryPath);
            }

            List<Path> recordPath = Collections.singletonList(allPaths.get(0));
                        
            Event event = RecordCreatedEvent.builder()
                              .seqNumber(nextSequenceNumber())
                              .name(nodeInfo.getName())
                              .txnId(AlfrescoTransactionSupport.getTransactionId())
                              .timestamp(System.currentTimeMillis())
                              .networkId(TenantUtil.getCurrentDomain())
                              .siteId(nodeInfo.getSiteId())
                              .nodeId(nodeInfo.getNodeId())
                              .nodeType(nodeInfo.getType().toPrefixString(namespaceService))
                              .paths(getPaths(recordPath, Arrays.asList(nodeInfo.getName())))
                              .parentNodeIds(this.getNodeIdsFromParent(recordPath))
                              .username(AuthenticationUtil.getFullyAuthenticatedUser())
                              .nodeModificationTime(nodeInfo.getModificationTimestamp())
                              .client(getAlfrescoClient(nodeInfo.getClient()))
                              .aspects(nodeInfo.getAspectsAsStrings())
                              .nodeProperties(nodeInfo.getProperties())
                              .build();
            sendEvent(event);
        }
    }

    @Override
    public void nodeLocked(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeLockedEvent.EVENT_TYPE);

        if (nodeInfo.checkNodeInfo())
        {
            NodeLockedEvent event = new NodeLockedEvent(nextSequenceNumber(), nodeInfo.getName(),
                    AlfrescoTransactionSupport.getTransactionId(), System.currentTimeMillis(),
                    TenantUtil.getCurrentDomain(), nodeInfo.getSiteId(), nodeInfo.getNodeId(),
                    nodeInfo.getType().toPrefixString(namespaceService), nodeInfo.getPaths(),
                    nodeInfo.getParentNodeIds(), AuthenticationUtil.getFullyAuthenticatedUser(),
                    nodeInfo.getModificationTimestamp(), getAlfrescoClient(nodeInfo.getClient()),
                    nodeInfo.getAspectsAsStrings(), nodeInfo.getProperties());

            sendEvent(event);
        }
    }

    @Override
    public void nodeUnlocked(NodeRef nodeRef)
    {
        NodeInfo nodeInfo = getNodeInfo(nodeRef, NodeUnlockedEvent.EVENT_TYPE);

        if (nodeInfo.checkNodeInfo())
        {
            NodeUnlockedEvent event = new NodeUnlockedEvent(nextSequenceNumber(), nodeInfo.getName(),
                    AlfrescoTransactionSupport.getTransactionId(), System.currentTimeMillis(),
                    TenantUtil.getCurrentDomain(), nodeInfo.getSiteId(), nodeInfo.getNodeId(),
                    nodeInfo.getType().toPrefixString(namespaceService), nodeInfo.getPaths(),
                    nodeInfo.getParentNodeIds(), AuthenticationUtil.getFullyAuthenticatedUser(),
                    nodeInfo.getModificationTimestamp(), getAlfrescoClient(nodeInfo.getClient()),
                    nodeInfo.getAspectsAsStrings(), nodeInfo.getProperties());

            sendEvent(event);
        }
    }
}
