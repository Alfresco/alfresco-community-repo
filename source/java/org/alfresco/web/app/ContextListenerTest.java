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
package org.alfresco.web.app;

import static org.junit.Assert.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the ContextListener class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextListenerTest
{
    private ContextListener contextListener;
    private @Mock ServletContextEvent event;
    
    
    @Before
    public void setUp() throws Exception
    {
        contextListener = new ContextListener();
        contextListener.setEnterpriseListenerClass("org.alfresco.web.app.ContextListenerTest$StubEnterpriseListener");
        StubEnterpriseListener.enterpriseDestroyed = false;
    }

    @Test
    public void testContextDestroyed()
    {
        contextListener.findEnterpriseListener();
        contextListener.contextDestroyed(event);
        
        assertTrue("Enterprise contextDestroyed() not executed.", StubEnterpriseListener.enterpriseDestroyed);
    }
    
    
    /**
     * ServletContextListener to simulate an enterprise-specific context listener. 
     */
    protected static class StubEnterpriseListener implements ServletContextListener
    {
        static boolean enterpriseDestroyed;
        
        @Override
        public void contextDestroyed(ServletContextEvent arg0)
        {
            enterpriseDestroyed = true;
        }

        @Override
        public void contextInitialized(ServletContextEvent arg0)
        {
            // Noop
        }
    }
}
