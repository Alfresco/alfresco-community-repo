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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.service.cmr.publishing.Environment;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 *
 */
public class PublishingObjectFactory implements EnvironmentFactory, PublishingQueueFactory
{
    private EnvironmentHelper environmentHelper;
    private TransferManifestNodeFactory transferManifestNodeFactory;
    private PublishingEventHelper publishingEventHelper;
    
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

    /* (non-Javadoc)
     * @see org.alfresco.repo.publishing.EnvironmentFactory#createEnvironmentObject(java.lang.String, java.lang.String)
     */
    @Override
    public Environment createEnvironmentObject(String siteId, String environmentName)
    {
        NodeRef node = environmentHelper.getEnvironment(siteId, environmentName);
        return createEnvironmentFromNode(node);
    }

    /**
     * @param node
     * @return
     */
    private Environment createEnvironmentFromNode(NodeRef node)
    {
        EnvironmentImpl environment = new EnvironmentImpl();
        environment.setNodeRef(node);
        environment.setPublishingQueueFactory(this);
        environment.setEnvironmentHelper(environmentHelper);
        return environment;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.publishing.EnvironmentFactory#createEnvironmentObjects(java.lang.String)
     */
    @Override
    public List<Environment> createEnvironmentObjects(String siteId)
    {
        Map<String,NodeRef> environmentMap = environmentHelper.getEnvironments(siteId);
        List<Environment> environments = new ArrayList<Environment>(environmentMap.size());
        for (NodeRef node : environmentMap.values())
        {
            environments.add(createEnvironmentFromNode(node));
        }
        return environments;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.publishing.PublishingQueueFactory#createPublishingQueueObject(java.lang.String, java.lang.String)
     */
    @Override
    public PublishingQueue createPublishingQueueObject(String siteId, String environmentName)
    {
        NodeRef environmentNode = environmentHelper.getEnvironment(siteId, environmentName);
        return createPublishingQueueObject(environmentNode);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.publishing.PublishingQueueFactory#createPublishingQueueObject(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public PublishingQueue createPublishingQueueObject(NodeRef environmentNodeRef)
    {
        NodeRef queueNode = environmentHelper.getPublishingQueue(environmentNodeRef);
        PublishingQueueImpl publishingQueue = new PublishingQueueImpl();
        publishingQueue.setNodeRef(queueNode);
        publishingQueue.setTransferManifestNodeFactory(transferManifestNodeFactory);
        publishingQueue.setPublishingEventHelper(publishingEventHelper);
        return publishingQueue;
    }

}
