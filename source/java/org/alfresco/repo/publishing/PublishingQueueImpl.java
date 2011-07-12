/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEventFilter;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionService;

/**
 * @author Brian
 * @author Nick Smith
 * 
 */
public class PublishingQueueImpl implements PublishingQueue
{
    private final static String MSG_FAILED_TO_CREATE_PUBLISHING_EVENT = "publishing-create-event-failed";
    private NodeRef nodeRef;
    private TransferManifestNodeFactory transferManifestNodeFactory;
    private VersionService versionService;
    private PublishingEventHelper publishingEventHelper;

    /**
     * {@inheritDoc}
    */
        public MutablePublishingPackage createPublishingPackage()
    {
        return new MutablePublishingPackageImpl(transferManifestNodeFactory, versionService);
    }

    /**
     * {@inheritDoc}
     */
     public StatusUpdate createStatusUpdate(String message, NodeRef nodeToLinkTo, String... channelNames)
     {
         return createStatusUpdate(message, nodeToLinkTo, Arrays.asList(channelNames));
     }

     /**
     * {@inheritDoc}
     */
     public StatusUpdate createStatusUpdate(String message, NodeRef nodeToLinkTo, Collection<String> channelNames)
     {
         return new StatusUpdateImpl(message, nodeToLinkTo, channelNames);
     }

     /**
     * {@inheritDoc}
    */
    public String scheduleNewEvent(PublishingPackage publishingPackage, String channelName, Calendar schedule, String comment, StatusUpdate statusUpdate)
    {
        try
        {
            NodeRef eventNode = publishingEventHelper.createNode(nodeRef, publishingPackage, channelName, schedule, comment, statusUpdate);
            publishingEventHelper.startPublishingWorkflow(eventNode, schedule);
            return eventNode.toString();
        }
        catch (Exception ex)
        {
            throw new AlfrescoRuntimeException(MSG_FAILED_TO_CREATE_PUBLISHING_EVENT, ex);
        }
    }

    /**
     * @param queueNode
     */
    public void setNodeRef(NodeRef queueNode)
    {
        this.nodeRef = queueNode;
    }

    /**
     * @param transferManifestNodeFactory
     *            the transferManifestNodeFactory to set
     */
    public void setTransferManifestNodeFactory(TransferManifestNodeFactory transferManifestNodeFactory)
    {
        this.transferManifestNodeFactory = transferManifestNodeFactory;
    }

    /**
     * @param publishingEventHelper
     *            the publishingEventHelper to set
     */
    public void setPublishingEventHelper(PublishingEventHelper publishingEventHelper)
    {
        this.publishingEventHelper = publishingEventHelper;
    }

    /**
     * @param versionService the versionService to set
     */
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    /**
     * {@inheritDoc}
     */
    public List<PublishingEvent> getPublishingEvents(PublishingEventFilter filter)
    {
        return publishingEventHelper.findPublishingEvents(nodeRef, filter);
    }

    /**
     * {@inheritDoc}
     */
     public PublishingEventFilter createPublishingEventFilter()
     {
         return new PublishingEventFilterImpl();
     }
}
