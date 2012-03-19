/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.repo.cluster;

import java.io.Serializable;

import org.alfresco.repo.jgroups.AlfrescoJGroupsChannelFactory;
import org.alfresco.util.ParameterCheck;
import org.jgroups.Channel;

/**
 * JGroups implementation of the {@link MessengerFactory} interface.
 * 
 * @author Matt Ward
 */
public class JGroupsMessengerFactory implements MessengerFactory
{
    @Override
    public <T extends Serializable> Messenger<T> createMessenger(String appRegion)
    {
        return createMessenger(appRegion, false);
    }

    @Override
    public <T extends Serializable> Messenger<T> createMessenger(String appRegion, boolean acceptLocalMessages)
    {
        ParameterCheck.mandatory("appRegion", appRegion);
        Channel channel = AlfrescoJGroupsChannelFactory.getChannel(appRegion, acceptLocalMessages);
        return new JGroupsMessenger<T>(channel);
    }

    @Override
    public boolean isClusterActive()
    {
        return AlfrescoJGroupsChannelFactory.isClusterActive();
    }    
}
