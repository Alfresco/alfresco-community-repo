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
import java.util.Set;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

/**
 * Hazelcast-based implementation of the {@link MessengerFactory} interface.
 * The factory must be configured with a {@link HazelcastInstance} - which
 * is the underlying factory for {@link ITopic} creation.
 * 
 * @author Matt Ward
 */
public class HazelcastMessengerFactory implements MessengerFactory
{
    private HazelcastInstanceFactory hazelcastInstanceFactory;
    
    @Override
    public <T extends Serializable> Messenger<T> createMessenger(String appRegion)
    {
        return createMessenger(appRegion, false);
    }

    @Override
    public <T extends Serializable> Messenger<T> createMessenger(String appRegion, boolean acceptLocalMessages)
    {
        if (!isClusterActive())
        {
            return new NullMessenger<T>();
        }
        // Clustering is enabled, create a messenger.
        HazelcastInstance hazelcast = hazelcastInstanceFactory.getInstance();
        ITopic<T> topic = hazelcast.getTopic(appRegion);
        String address = hazelcast.getCluster().getLocalMember().getInetSocketAddress().toString();
        return new HazelcastMessenger<T>(topic, address);
    }
    
    /**
     * Provide the messenger factory with a means to obtain a HazelcastInstance.
     * 
     * @param hazelcastInstanceFactory
     */
    public void setHazelcastInstanceFactory(HazelcastInstanceFactory hazelcastInstanceFactory)
    {
        this.hazelcastInstanceFactory = hazelcastInstanceFactory;
    }

    @Override
    public boolean isClusterActive()
    {
        return hazelcastInstanceFactory.isClusteringEnabled();
    }

    @Override
    public void addMembershipListener(final ClusterMembershipListener listener)
    {
        if (isClusterActive())
        {
            HazelcastInstance hazelcast = hazelcastInstanceFactory.getInstance();
            hazelcast.getCluster().addMembershipListener(new MembershipListener()
            {
                @Override
                public void memberRemoved(MembershipEvent e)
                {
                    listener.memberLeft(member(e), cluster(e));
                }
                
                @Override
                public void memberAdded(MembershipEvent e)
                {
                    listener.memberJoined(member(e), cluster(e));
                }
                
                private String member(MembershipEvent e)
                {
                    return e.getMember().getInetSocketAddress().toString();
                }
                
                private String[] cluster(MembershipEvent e)
                {
                    Set<Member> members = e.getCluster().getMembers();
                    String[] cluster = new String[members.size()];
                    int i = 0;
                    for (Member m : members)
                    {
                        cluster[i++] = m.getInetSocketAddress().toString();
                    }
                    return cluster;
                }
            });
        }
    }
}
