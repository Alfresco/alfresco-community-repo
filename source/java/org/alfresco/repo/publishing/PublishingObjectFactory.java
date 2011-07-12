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

import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionService;

/**
 * @author Brian
 *
 */
public class PublishingObjectFactory implements EnvironmentFactory, PublishingQueueFactory
{
    public static final String NAME = "publishingObjectFactory";
    
    private EnvironmentHelper environmentHelper;
    private TransferManifestNodeFactory transferManifestNodeFactory;
    private PublishingEventHelper publishingEventHelper;
    private VersionService versionService;
    
    /**
     * @param environmentHelper the environmentHelper to set
     */
    public void setEnvironmentHelper(EnvironmentHelper environmentHelper)
    {
        this.environmentHelper = environmentHelper;
    }

    /**
     * @param transferManifestNodeFactory the transferManifestNodeFactory to set
     */
    public void setTransferManifestNodeFactory(TransferManifestNodeFactory transferManifestNodeFactory)
    {
        this.transferManifestNodeFactory = transferManifestNodeFactory;
    }

    /**
     * @param publishingEventHelper the publishingEventHelper to set
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
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.publishing.EnvironmentFactory#createEnvironmentObject(java.lang.String, java.lang.String)
     */
    public EnvironmentImpl createEnvironmentObject(String siteId)
    {
        NodeRef node = environmentHelper.getEnvironment(siteId);
        return createEnvironmentFromNode(node);
    }

    /**
     * @param node
     * @return
     */
    private EnvironmentImpl createEnvironmentFromNode(NodeRef node)
    {
        EnvironmentImpl environment = new EnvironmentImpl();
        environment.setNodeRef(node);
        environment.setPublishingQueueFactory(this);
        environment.setEnvironmentHelper(environmentHelper);
        return environment;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.publishing.PublishingQueueFactory#createPublishingQueueObject(java.lang.String, java.lang.String)
     */
    public PublishingQueue createPublishingQueueObject(String siteId)
    {
        NodeRef environmentNode = environmentHelper.getEnvironment(siteId);
        return createPublishingQueueObject(environmentNode);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.publishing.PublishingQueueFactory#createPublishingQueueObject(org.alfresco.service.cmr.repository.NodeRef)
     */
    public PublishingQueue createPublishingQueueObject(NodeRef environmentNodeRef)
    {
        NodeRef queueNode = environmentHelper.getPublishingQueue(environmentNodeRef);
        PublishingQueueImpl publishingQueue = new PublishingQueueImpl();
        publishingQueue.setNodeRef(queueNode);
        publishingQueue.setTransferManifestNodeFactory(transferManifestNodeFactory);
        publishingQueue.setPublishingEventHelper(publishingEventHelper);
        publishingQueue.setVersionService(versionService);
        return publishingQueue;
    }

}
