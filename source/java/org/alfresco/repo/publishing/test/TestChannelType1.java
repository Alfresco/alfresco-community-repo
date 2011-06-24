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

package org.alfresco.repo.publishing.test;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.NodeFilter;
import org.alfresco.service.cmr.transfer.NodeFinder;
import org.alfresco.service.namespace.QName;

public class TestChannelType1 extends AbstractChannelType
{

    @Override
    public boolean canPublish()
    {
        return true;
    }

    @Override
    public boolean canPublishStatusUpdates()
    {
        return false;
    }

    @Override
    public boolean canUnpublish()
    {
        return false;
    }

    @Override
    public QName getChannelNodeType()
    {
        return PublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    @Override
    public String getId()
    {
        return "TestChannelType1";
    }

    @Override
    public int getMaximumStatusLength()
    {
        return 0;
    }

    @Override
    public NodeFilter getNodeFilter()
    {
        return null;
    }

    @Override
    public NodeFinder getNodeFinder()
    {
        return null;
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        return node.getId();
    }

    @Override
    public Set<QName> getSupportedContentTypes()
    {
        return null;
    }

    @Override
    public Set<String> getSupportedMimetypes()
    {
        return null;
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties)
    {
        //Deliberately blank
    }

    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
        //Deliberately blank
    }

    @Override
    public void updateStatus(String status, Map<QName, Serializable> properties)
    {
        //Deliberately blank
    }

}
