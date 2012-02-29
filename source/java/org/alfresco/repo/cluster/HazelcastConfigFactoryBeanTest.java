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

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.hazelcast.config.Config;

/**
 * Tests for the HazelcastConfigFactoryBean class.
 * 
 * @author Matt Ward
 */
public class HazelcastConfigFactoryBeanTest
{
    private HazelcastConfigFactoryBean configFactory;
    private Resource resource;
    private Properties properties;
    
    @Before
    public void setUp() throws Exception
    {
        configFactory = new HazelcastConfigFactoryBean();
        resource = new ClassPathResource("cluster-test/placeholder-test.xml");
        configFactory.setConfigFile(resource);
        
        properties = new Properties();
        properties.setProperty("alfresco.hazelcast.password", "let-me-in");
        properties.setProperty("alfresco.cluster.name", "cluster-name");
        configFactory.setProperties(properties);
        
        // Trigger the spring post-bean creation lifecycle method
        configFactory.afterPropertiesSet();
    }


    @Test
    public void testConfigHasNewPropertyValues() throws Exception
    {
        // Invoke the factory method.
        Config config = configFactory.getObject();
        
        assertEquals("let-me-in", config.getGroupConfig().getPassword());
        assertEquals("cluster-name", config.getGroupConfig().getName());
    }
}
