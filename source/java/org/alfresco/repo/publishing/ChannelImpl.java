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

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 *
 */
public class ChannelImpl implements Channel
{
    private final NodeRef nodeRef;
    private final ChannelType channelType;
    private final String name;
    private final ChannelServiceImpl channelService;

    /**
     * @param channelType
     * @param name
     * @param channelService
     */
    public ChannelImpl(ChannelType channelType, NodeRef nodeRef, String name, ChannelServiceImpl channelService)
    {
        this.nodeRef = nodeRef;
        this.channelType = channelType;
        this.name = name;
        this.channelService = channelService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.channels.Channel#getChannelType()
     */
    @Override
    public ChannelType getChannelType()
    {
        return channelType;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.channels.Channel#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.publishing.channels.Channel#getNodeRef()
     */
    @Override
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

}
