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
package org.alfresco.web.app.servlet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.management.subsystems.AbstractChainedSubsystemTest;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.DefaultChildApplicationContextManager;
import org.alfresco.repo.webdav.auth.RemoteUserMapper;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;


/**
 * @author dward
 *
 */
public class DefaultRemoteUserMapperTest extends AbstractChainedSubsystemTest
{
    ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    DefaultChildApplicationContextManager childApplicationContextManager;
    ChildApplicationContextFactory childApplicationContextFactory;    

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        childApplicationContextManager = (DefaultChildApplicationContextManager) ctx.getBean("Authentication");
        childApplicationContextManager.stop();
        childApplicationContextManager.setProperty("chain", "external1:external");
        childApplicationContextFactory = getChildApplicationContextFactory(childApplicationContextManager, "external1");
    }
    
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        childApplicationContextManager.destroy();
        childApplicationContextManager = null;
        childApplicationContextFactory = null;
    }


    public void testUnproxiedHeader() throws Exception
    {
        // Clear the proxy user name
        childApplicationContextFactory.stop();
        childApplicationContextFactory.setProperty("external.authentication.proxyUserName", "");
        
        // Mock a request with a username in the header
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("X-Alfresco-Remote-User")).thenReturn("AdMiN");
        assertEquals("admin", ((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
                "remoteUserMapper")).getRemoteUser(mockRequest));
        
        // Mock an unauthenticated request
        when(mockRequest.getHeader("X-Alfresco-Remote-User")).thenReturn(null);
        assertNull(((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
        "remoteUserMapper")).getRemoteUser(mockRequest)); 

        // Mock a remote user request
        when(mockRequest.getRemoteUser()).thenReturn("ADMIN");
        assertEquals("admin", ((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
                "remoteUserMapper")).getRemoteUser(mockRequest));
    }

    
    public void testProxiedHeader() throws Exception
    {
        // Set the proxy user name
        childApplicationContextFactory.stop();
        childApplicationContextFactory.setProperty("external.authentication.proxyUserName", "bob");

        // Mock a request with both a user and a header
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRemoteUser()).thenReturn("bob");
        when(mockRequest.getHeader("X-Alfresco-Remote-User")).thenReturn("AdMiN");
        assertEquals("admin", ((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
                "remoteUserMapper")).getRemoteUser(mockRequest));

        // Now try header pattern matching
        childApplicationContextFactory.stop();
        childApplicationContextFactory.setProperty("external.authentication.userIdPattern", "abc-(.*)-999");
        when(mockRequest.getHeader("X-Alfresco-Remote-User")).thenReturn("abc-AdMiN-999");
        assertEquals("admin", ((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
                "remoteUserMapper")).getRemoteUser(mockRequest));

        // Try a request with an invalid match
        when(mockRequest.getHeader("X-Alfresco-Remote-User")).thenReturn("abc-AdMiN-998");
        assertNull(((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
                "remoteUserMapper")).getRemoteUser(mockRequest));

        // Try a request without the remote user
        when(mockRequest.getRemoteUser()).thenReturn(null);
        assertNull(((RemoteUserMapper) childApplicationContextFactory.getApplicationContext().getBean(
                "remoteUserMapper")).getRemoteUser(mockRequest));
    }        
    
}
