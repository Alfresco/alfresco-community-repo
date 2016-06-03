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
