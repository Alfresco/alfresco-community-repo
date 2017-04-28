/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.events.types.*;
import org.alfresco.events.types.authority.AuthorityAddedToGroupEvent;
import org.alfresco.events.types.authority.AuthorityRemovedFromGroupEvent;
import org.alfresco.events.types.authority.GroupDeletedEvent;
import org.alfresco.events.types.permission.InheritPermissionsDisabledEvent;
import org.alfresco.events.types.permission.InheritPermissionsEnabledEvent;
import org.alfresco.events.types.permission.LocalPermissionGrantedEvent;
import org.alfresco.events.types.permission.LocalPermissionRevokedEvent;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.Client;
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

            // Work out the old and new paths. Note that the FileFolderService sets the node name to a temporary name during the move,
            // so we can't rely on the name. Use the association name instead.
            String oldName = oldChildAssocRef.getQName().getLocalName();
            String tmpNewName = newChildAssocRef.getQName().getLocalName();
            String newName = null;
            if(oldName != null && tmpNewName != null && !oldName.equals(tmpNewName))
            {
                newName = tmpNewName;
            }
            String oldParentNodeName = (String)nodeService.getProperty(oldParentNodeRef, ContentModel.PROP_NAME);
            String newParentNodeName = (String)nodeService.getProperty(newParentNodeRef, ContentModel.PROP_NAME);
            List<Path> newParentPaths = nodeService.getPaths(newParentNodeRef, false);
            List<String> newPaths = getPaths(newParentPaths, Arrays.asList(newParentNodeName, tmpNewName));

            // renames are handled by an onUpdateProperties callback, we just deal with real moves here.
            if(!oldParentNodeRef.equals(newParentNodeRef))
            {
                List<List<String>> toParentNodeIds = getNodeIds(newParentPaths);
                List<Path> oldParentPaths = nodeService.getPaths(oldParentNodeRef, false);
                List<String> previousPaths = getPaths(oldParentPaths, Arrays.asList(oldParentNodeName, oldName));
                List<List<String>> previousParentNodeIds = getNodeIds(oldParentPaths);

                Set<String> aspects = nodeInfo.getAspectsAsStrings();
                Map<String, Serializable> properties = nodeInfo.getProperties();

                Client alfrescoClient = getAlfrescoClient(nodeInfo.getClient());

                Event event = new NodeMovedEvent(nextSequenceNumber(), oldName, newName, txnId, timestamp, networkId, siteId, objectId, nodeType,
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
}
