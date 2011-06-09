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

import java.util.Calendar;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * 
 */
public class PublishingQueueImpl implements PublishingQueue
{
    private final static String MSG_FAILED_TO_CREATE_PUBLISHING_EVENT = "publishing-create-event-failed";
    private NodeRef nodeRef;
    private TransferManifestNodeFactory transferManifestNodeFactory;
    private PublishingEventHelper publishingEventHelper;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.service.cmr.publishing.PublishingQueue#cancelEvent(java.
     * lang.String)
     */
    @Override
    public void cancelEvent(String eventId)
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.service.cmr.publishing.PublishingQueue#createPublishingPackage
     * ()
     */
    @Override
    public MutablePublishingPackage createPublishingPackage()
    {
        return new MutablePublishingPackageImpl(transferManifestNodeFactory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.service.cmr.publishing.PublishingQueue#scheduleNewEvent(
     * org.alfresco.service.cmr.publishing.PublishingPackage,
     * java.util.Calendar, java.lang.String)
     */
    @Override
    public String scheduleNewEvent(PublishingPackage publishingPackage, Calendar schedule, String comment)
    {
        try
        {
            NodeRef eventNode = publishingEventHelper.create(nodeRef, publishingPackage, schedule, comment);
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

}
