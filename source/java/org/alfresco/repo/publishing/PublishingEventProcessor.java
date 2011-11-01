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

import java.util.Set;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.Status;
import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.urlshortening.UrlShortener;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingEventProcessor
{
    private static final Log log = LogFactory.getLog(PublishingEventProcessor.class);
    
    private PublishingEventHelper eventHelper;
    private ChannelService channelService;
    private NodeService nodeService;
    private BehaviourFilter behaviourFilter;
    private UrlShortener urlShortener;
    private TransactionService transactionService;
    
    public void processEventNode(NodeRef eventNode)
    {
        ParameterCheck.mandatory("eventNode", eventNode);
        try
        {
            updateEventStatus(eventNode, Status.IN_PROGRESS);
            final PublishingEvent event = eventHelper.getPublishingEvent(eventNode);
            String channelName = event.getChannelId();
            final ChannelImpl channel = (ChannelImpl) channelService.getChannelById(channelName);
            if (channel == null)
            {
                fail(eventNode, "No channel found");
            }
            else
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    @Override
                    public Void execute() throws Throwable
                    {
                        try
                        {
                            behaviourFilter.disableBehaviour();
                            channel.publishEvent(event);
                            sendStatusUpdate(channel, event.getStatusUpdate());
                        }
                        finally
                        {
                            behaviourFilter.enableBehaviour();
                        }
                        return null;
                    }
                }, false, true);
                updateEventStatus(eventNode, Status.COMPLETED);
            }
        }
        catch (Exception e)
        {
            log.error("Caught exception while processing publishing event " + eventNode, e);
            fail(eventNode, e.getMessage());
        }
     }

    public void sendStatusUpdate(Channel publishChannel, StatusUpdate update)
    {
        if (update == null)
        {
            return;
        }
        String message = update.getMessage();
        String nodeUrl = getNodeUrl(publishChannel, update);
        Set<String> channels = update.getChannelIds();
        for (String channelId : channels)
        {
            Channel channel = channelService.getChannelById(channelId);
            if (channel != null)
            {
                channel.sendStatusUpdate(message, nodeUrl);
            }
        }
    }

    /**
     * @param publishChannel
     * @param update
     * @return
     */
    private String getNodeUrl(Channel publishChannel, StatusUpdate update)
    {
        NodeRef node = update.getNodeToLinkTo();
        String nodeUrl = null;
        if (node!= null)
        {
            nodeUrl = publishChannel.getUrl(node);
            if (nodeUrl != null)
            {
                nodeUrl = " " + urlShortener.shortenUrl(nodeUrl);
            }
        }
        return nodeUrl;
    }

     public void fail(NodeRef eventNode, String msg)
     {
         log.error("Failed to process publishing event " + eventNode + ": " + msg);
         updateEventStatus(eventNode, Status.FAILED);
     }

     private void updateEventStatus(final NodeRef eventNode, final Status status)
     {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.setProperty(eventNode, PublishingModel.PROP_PUBLISHING_EVENT_STATUS, status.name());
                return null;
            }
        }, false, true);
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

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
}