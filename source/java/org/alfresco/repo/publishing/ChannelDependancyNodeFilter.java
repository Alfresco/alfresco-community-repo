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

import static org.alfresco.repo.publishing.PublishingModel.PROP_CHANNEL_TYPE;

import org.alfresco.repo.transfer.AbstractNodeFilter;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.NodeFilter;
import org.alfresco.util.ParameterCheck;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class ChannelDependancyNodeFilter extends AbstractNodeFilter
{
    private final ChannelService channelService;
    private NodeService nodeService;
    
    /**
     * @param channelService
     */
    public ChannelDependancyNodeFilter(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void init()
    {
        super.init();
        this.nodeService = serviceRegistry.getNodeService();
        ParameterCheck.mandatory("channelService", channelService);
    }

    /**
    * {@inheritDoc}
    */
    public boolean accept(NodeRef thisNode)
    {
        String typeId = (String) nodeService.getProperty(thisNode, PROP_CHANNEL_TYPE);
        if (typeId !=null)
        {
            ChannelType type = channelService.getChannelType(typeId);
            if (type != null)
            {
                NodeFilter filter = type.getNodeFilter();
                if (filter !=null)
                {
                    return filter.accept(thisNode);
                }
            }
        }
        return true;
    }
}
