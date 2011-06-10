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
import static org.alfresco.repo.publishing.PublishingModel.ASSOC_LAST_PUBLISHING_EVENT;
import static org.alfresco.repo.publishing.PublishingModel.NAMESPACE;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.ParameterCheck;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishingEventProcessor
{
    private PublishingEventHelper eventHelper;
    private ChannelHelper channelHelper;
    private ChannelService channelService;
    private NodeService nodeService;
    private BehaviourFilter behaviourFilter;
    
     public void processEventNode(NodeRef eventNode)
     {
        ParameterCheck.mandatory("actionedUponNodeRef", eventNode);
        try
        {
            behaviourFilter.disableAllBehaviours();
            PublishingEvent event = eventHelper.getPublishingEvent(eventNode);
            NodeRef environment = eventHelper.getEnvironmentNodeForPublishingEvent(eventNode);
            String channelName = event.getChannelName();
            NodeRef channelNode = channelHelper.getChannelNodeForEnvironment(environment, channelName);
            if (channelNode == null)
            {
                fail(event, "No channel found");
            }
            else
            {
                publishEvent(channelNode, event);
            }
        }
        finally
        {
            behaviourFilter.enableAllBehaviours();
        }
     }

     public void publishEvent(NodeRef channelNode, PublishingEvent event)
     {
         Channel channel = channelHelper.buildChannelObject(channelNode, channelService);
         NodeRef eventNode = eventHelper.getPublishingEventNode(event.getId());
         for (PublishingPackageEntry entry : event.getPackage().getEntries())
         {
             if (entry.isPublish())
             {
                 publishEntry(channel, entry, eventNode);
             }
             else
             {
                 unpublishEntry(channel, entry);
             }
         }
     }
     
     public void unpublishEntry(Channel channel, PublishingPackageEntry entry)
     {
         // TODO Auto-generated method stub
         
     }


     public void fail(PublishingEvent event, String msg)
     {
         // TODO Auto-generated method stub
     }

     public NodeRef publishEntry(Channel channel, PublishingPackageEntry entry, NodeRef eventNode)
     {
         NodeRef mappedNode = channelHelper.mapSourceToEnvironment(entry.getNodeRef(), channel.getNodeRef());
         if(mappedNode == null)
         {
             mappedNode = publishNewNode(channel.getNodeRef(),  entry.getSnapshot());
         }
         else
         {
             updatePublishedNode(mappedNode, entry);
         }
         QName qName = QName.createQName(NAMESPACE, eventNode.getId());
         nodeService.addChild(mappedNode, eventNode, ASSOC_LAST_PUBLISHING_EVENT, qName);
         return mappedNode; 
     }

     /**
      * Creates a new node under the root of the specified channel. The type,
      * aspects and properties of the node are determined by the supplied
      * snapshot.
      * 
      * @param channel
      * @param snapshot
      * @return the newly published node.
      */
     private NodeRef publishNewNode(NodeRef channel, NodeSnapshot snapshot)
     {
         ParameterCheck.mandatory("channel", channel);
         ParameterCheck.mandatory("snapshot", snapshot);
         
         NodeRef root = channelHelper.getChannelRootNode(channel);
         NodeRef publishedNode = createPublishedNode(root, snapshot);
         addAspects(publishedNode, snapshot.getAspects());
         NodeRef source = snapshot.getNodeRef();
         channelHelper.createMapping(source, publishedNode);
         return publishedNode;
     }

     private void updatePublishedNode(NodeRef publishedNode, PublishingPackageEntry entry)
     {
        NodeSnapshot snapshot = entry.getSnapshot();
        Set<QName> newAspects = snapshot.getAspects();
        removeUnwantedAspects(publishedNode, newAspects);

        Map<QName, Serializable> snapshotProps = snapshot.getProperties();
        removeUnwantedProperties(publishedNode, snapshotProps);
        
        // Add new properties
        Map<QName, Serializable> newProps= new HashMap<QName, Serializable>(snapshotProps);
        newProps.remove(ContentModel.PROP_NODE_UUID);
        nodeService.setProperties(publishedNode, snapshotProps);
        
        // Add new aspects
        addAspects(publishedNode, newAspects);
     }

    /**
     * @param publishedNode
     * @param snapshotProps
     */
    private void removeUnwantedProperties(NodeRef publishedNode, Map<QName, Serializable> snapshotProps)
    {
        Map<QName, Serializable> publishProps = nodeService.getProperties(publishedNode);
        Set<QName> propsToRemove = new HashSet<QName>(publishProps.keySet());
        propsToRemove.removeAll(snapshotProps.keySet());
        for (QName propertyToRemove : propsToRemove)
        {
            nodeService.removeProperty(publishedNode, propertyToRemove);
        }
    }

    /**
     * @param publishedNode
     * @param newAspects
     */
    private void removeUnwantedAspects(NodeRef publishedNode, Set<QName> newAspects)
    {
        Set<QName> aspectsToRemove = nodeService.getAspects(publishedNode);
        aspectsToRemove.removeAll(newAspects);
        for (QName aspectToRemove : aspectsToRemove)
        {
            nodeService.removeAspect(publishedNode, aspectToRemove);
        }
    }


     private void addAspects(NodeRef publishedNode, Collection<QName> aspects)
     {
         Set<QName> currentAspects = nodeService.getAspects(publishedNode);
          for (QName aspect : aspects)
          {
              if(currentAspects.contains(aspect)==false)
              {
                  nodeService.addAspect(publishedNode, aspect, null);
              }
          }
     }

     private NodeRef createPublishedNode(NodeRef root, NodeSnapshot snapshot)
     {
         QName type = snapshot.getType();
          Map<QName, Serializable> actualProps = getPropertiesToPublish(snapshot);
          String name = (String) actualProps.get(ContentModel.PROP_NAME);
          if(name == null)
          {
              name = GUID.generate();
          }
          QName assocName = QName.createQName(NAMESPACE, name);
          ChildAssociationRef publishedAssoc = nodeService.createNode(root, ASSOC_CONTAINS, assocName, type, actualProps);
          NodeRef publishedNode = publishedAssoc.getChildRef();
         return publishedNode;
     }

     private Map<QName, Serializable> getPropertiesToPublish(NodeSnapshot snapshot)
     {
         Map<QName, Serializable> properties = snapshot.getProperties();
          // Remove the Node Ref Id
          Map<QName, Serializable> actualProps = new HashMap<QName, Serializable>(properties);
          actualProps.remove(ContentModel.PROP_NODE_UUID);
         return actualProps;
     }

     /**
      * @param channelHelper the channelHelper to set
      */
     public void setChannelHelper(ChannelHelper channelHelper)
     {
         this.channelHelper = channelHelper;
     }
     
     /**
      * @param channelService the channelService to set
      */
     public void setChannelService(ChannelService channelService)
     {
         this.channelService = channelService;
     }
     
     /**
      * @param eventHelper the Publishing Event Helper to set
      */
     public void setPublishingEventHelper(PublishingEventHelper eventHelper)
     {
         this.eventHelper = eventHelper;
     }

     /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param behaviourFilter the behaviourFilter to set
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
}
