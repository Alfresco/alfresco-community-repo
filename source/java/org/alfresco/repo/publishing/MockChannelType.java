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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * For test purposes only.
 * @author Nick Smith
 * @since 4.0
 *
 */
public class MockChannelType extends AbstractChannelType
{
    public final static String ID = "MockChannelType";
    
    /**
    * {@inheritDoc}
    */
    public String getId()
    {
        return ID;
    }

    /**
    * {@inheritDoc}
    */
    public Map<String, String> getCapabilities()
    {
        return null;
    }

    /**
    * {@inheritDoc}
    */
    public QName getChannelNodeType()
    {
        return PublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    /**
    * {@inheritDoc}
    */
    public QName getContentRootNodeType()
    {
        return ContentModel.TYPE_FOLDER;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties)
    {
        // NOOP
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
        //NOOP
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void updateStatus(Channel channel, String status, Map<QName, Serializable> properties)
    {
        //NOOP
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public boolean canPublish()
    {
        return false;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public boolean canUnpublish()
    {
        return false;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Set<String> getSupportedMimetypes()
    {
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Set<QName> getSupportedContentTypes()
    {
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public boolean canPublishStatusUpdates()
    {
        return false;
    }

    /**
    * {@inheritDoc}
    */
    public String getNodeUrl(NodeRef node)
    {
        return null;
    }

    @Override
    public String getAuthorisationUrl(Channel channel, String callbackUrl)
    {
        return "";
    }

}
