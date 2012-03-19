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

import org.springframework.util.StringUtils;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

/**
 * Hazelcast-based implementation of the {@link MessengerFactory} interface.
 * The factory must be configured with a {@link HazelcastInstance} - which
 * is the underlying factory for {@link ITopic} creation.
 * 
 * @author Matt Ward
 */
public class HazelcastMessengerFactory implements MessengerFactory
{
    private HazelcastInstance hazelcast;

    @Override
    public <T extends Serializable> Messenger<T> createMessenger(String appRegion)
    {
        return createMessenger(appRegion);
    }

    @Override
    public <T extends Serializable> Messenger<T> createMessenger(String appRegion, boolean acceptLocalMessages)
    {
        ITopic<T> topic = hazelcast.getTopic(appRegion);
        String address = hazelcast.getCluster().getLocalMember().getInetSocketAddress().toString();
        return new HazelcastMessenger<T>(topic, address);
    }
    
    /**
     * @param hazelcast the hazelcast to set
     */
    public void setHazelcast(HazelcastInstance hazelcast)
    {
        this.hazelcast = hazelcast;
    }

    @Override
    public boolean isClusterActive()
    {
        Config config = hazelcast.getConfig();
        if (config == null || config.getGroupConfig() == null)
        {
            return false;
        }
        GroupConfig groupConfig = config.getGroupConfig();
        return StringUtils.hasText(groupConfig.getName());
    }
}
