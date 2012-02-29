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

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Provides a way of lazily creating HazelcastInstances for a given configuration.
 * The HazelcastInstance will not be created until {@link #newInstance()} is called.
 * <p>
 * An intermediary class such as this is required in order to avoid starting
 * Hazelcast instances when clustering is not configured/required. Otherwise
 * simply by defining a HazelcastInstance bean clustering would spring into life.
 * 
 * @author Matt Ward
 */
public class HazelcastInstanceFactory
{
    public Config config;

    public HazelcastInstance newInstance()
    {
        return Hazelcast.newHazelcastInstance(config);
    }

    /**
     * @param config the config to set
     */
    public void setConfig(Config config)
    {
        this.config = config;
    }
}
