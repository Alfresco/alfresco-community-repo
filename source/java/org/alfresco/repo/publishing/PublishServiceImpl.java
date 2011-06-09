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

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.publishing.Environment;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 *
 */
public class PublishServiceImpl implements PublishingService
{
    private EnvironmentFactory environmentFactory;
    private PublishingEventHelper publishingEventHelper;
    
    /**
     * @param environmentFactory the environmentFactory to set
     */
    public void setEnvironmentFactory(EnvironmentFactory environmentFactory)
    {
        this.environmentFactory = environmentFactory;
    }

    /**
     * @param publishingEventHelper the publishingEventHelper to set
     */
    public void setPublishingEventHelper(PublishingEventHelper publishingEventHelper)
    {
        this.publishingEventHelper = publishingEventHelper;
    }
    
    /**
     * 
    * {@inheritDoc}
     */
    public Environment getEnvironment(String siteId, String environmentName)
    {
        return environmentFactory.createEnvironmentObject(siteId, environmentName);
    }

    /**
     * 
    * {@inheritDoc}
     */
    public List<Environment> getEnvironments(String siteId)
    {
        return environmentFactory.createEnvironmentObjects(siteId);
    }

    /**
     * 
    * {@inheritDoc}
     */
    public Set<NodeRef> getPublishingDependencies(NodeRef node)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
     public PublishingEvent getPublishingEvent(String id)
     {
         return publishingEventHelper.getPublishingEvent(id);
     }
}
