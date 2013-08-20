/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.filesys.config;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link ServerConfigurationBean} class.
 * 
 * @author Matt Ward
 */
public class ServerConfigurationBeanTest
{
    private ServerConfigurationBean serverConf;
    
    @Before
    public void setUp() throws Exception
    {
        serverConf = new ServerConfigurationBean();
    }

    /**
     * ALF-19669: NullPointerException when stopping fileServers subsystem
     */
    @Test
    public void testDestroyWhenThreadPoolIsNull() throws Exception
    {
        // Ensure threadPool is null
        Field threadPoolField = serverConf.getClass().getDeclaredField("threadPool");
        threadPoolField.setAccessible(true);
        threadPoolField.set(serverConf, null);
        assertNull("Test precondition failure - threadPool is not null", threadPoolField.get(serverConf));
        
        try
        {
            serverConf.destroy();
        }
        catch (NullPointerException error)
        {
            fail("Unable to cleanly destroy " + serverConf.getClass().getSimpleName() + " instance.");
        }
    }

}
