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
import static org.alfresco.repo.publishing.PublishingModel.ASPECT_PUBLISHED;
import static org.alfresco.repo.publishing.PublishingModel.ASSOC_LAST_PUBLISHING_EVENT;
import static org.alfresco.repo.publishing.PublishingModel.NAMESPACE;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.urlshortening.UrlShortener;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
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
    private UrlShortener urlShortener;
    private DictionaryService dictionaryService;
    
     public void processEventNode(NodeRef eventNode)
     {
        ParameterCheck.mandatory("eventNode", eventNode);
        try
        {
            behaviourFilter.disableAllBehaviours();
            String inProgressStatus = PublishingEvent.Status.IN_PROGRESS.name();
            nodeService.setProperty(eventNode, PublishingModel.PROP_PUBLISHING_EVENT_STATUS, inProgressStatus);
            PublishingEvent event = eventHelper.getPublishingEvent(eventNode);
            String channelName = event.getChannelId();
            Channel channel = channelService.getChannelById(channelName);
            if (channel == null)
            {
                fail(eventNode, "No channel found");
            }
            else
            {
                publishEvent(channel, event);
                updateStatus(channel, event.getStatusUpdate());
                String completedStatus = PublishingEvent.Status.COMPLETED.name();
                nodeService.setProperty(eventNode, PublishingModel.PROP_PUBLISHING_EVENT_STATUS, completedStatus);
            }
        }
        catch(Exception e)
        {
            fail(eventNode, e.getMessage());
        }
        finally
        {
            behaviourFilter.enableAllBehaviours();
        }
     }

    public void updateStatus(Channel publishChannel, StatusUpdate update)
    {
        if(update == null)
        {
            return;
        }
        String message = update.getMessage();
        NodeRef node = update.getNodeToLinkTo();
        if(node!= null)
        {
            String nodeUrl = publishChannel.getUrl(node);
            if(nodeUrl != null)
            {
                message += urlShortener.shortenUrl(nodeUrl);
            }
        }
        Set<String> channels = update.getChannelIds();
        for (String channelId : channels)
        {
            Channel channel = channelService.getChannelById(channelId);
            if(channel != null && channel.getChannelType().canPublishStatusUpdates())
            {
                channel.updateStatus(message);
            }
        }
    }

    public void publishEvent(Channel channel, PublishingEvent event)
     {
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


     public void fail(NodeRef eventNode, String msg)
     {
         String completedStatus = PublishingEvent.Status.FAILED.name();
         nodeService.setProperty(eventNode, PublishingModel.PROP_PUBLISHING_EVENT_STATUS, completedStatus);
     }

     public NodeRef publishEntry(Channel channel, PublishingPackageEntry entry, NodeRef eventNode)
     {
         NodeRef publishedNode = channelHelper.mapSourceToEnvironment(entry.getNodeRef(), channel.getNodeRef());
         if(publishedNode == null)
         {
             publishedNode = publishNewNode(channel.getNodeRef(),  entry.getSnapshot());
         }
         else
         {
             updatePublishedNode(publishedNode, entry);
         }
         eventHelper.linkToLastEvent(publishedNode, eventNode);
         channel.publish(publishedNode);
         return publishedNode; 
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
         
         NodeRef publishedNode = createPublishedNode(channel, snapshot);
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
        
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(publishedNode, ASSOC_LAST_PUBLISHING_EVENT, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef assoc : assocs)
        {
            nodeService.removeChildAssociation(assoc);
        }
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

        //We want to retain the published asset id and URL in the updated node...
        snapshotProps.put(PublishingModel.PROP_ASSET_ID, nodeService.getProperty(publishedNode, 
                PublishingModel.PROP_ASSET_ID));
        snapshotProps.put(PublishingModel.PROP_ASSET_URL, nodeService.getProperty(publishedNode, 
                PublishingModel.PROP_ASSET_URL));
        
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
        aspectsToRemove.remove(ASPECT_PUBLISHED);
        aspectsToRemove.remove(PublishingModel.ASPECT_ASSET);
        for (QName publishedAssetAspect : dictionaryService.getSubAspects(PublishingModel.ASPECT_ASSET, true))
        {
            aspectsToRemove.remove(publishedAssetAspect);
        }

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
    
    /**
     * @param urlShortener the urlShortener to set
     */
    public void setUrlShortener(UrlShortener urlShortener)
    {
        this.urlShortener = urlShortener;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
}