/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts;

import java.io.IOException;
import java.util.HashMap;

import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.web.scripts.TestWebScriptServer;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Base unit test class for web scripts.
 * 
 * @author Roy Wetherall
 */
public abstract class BaseWebScriptTest extends TestCase
{
    /** Standard HTTP method names */
    protected static final String METHOD_POST = "post";
    protected static final String METHOD_GET = "get";
    protected static final String METHOD_PUT = "put";
    protected static final String METHOD_DELETE = "delete";
    
    /** Test web script server */
    private static TestWebScriptServer server = null;
    
    protected static TestWebScriptServer getServer()
    {
        if (BaseWebScriptTest.server == null)
        {
            BaseWebScriptTest.server = TestWebScriptRepoServer.getTestServer();
        }
        return BaseWebScriptTest.server;
    }

    /**
     * "GET" the url and check for the expected status code 
     * 
     * @param url
     * @param expectedStatus
     * @return
     * @throws IOException
     */
    protected MockHttpServletResponse getRequest(String url, int expectedStatus)
        throws IOException
    {
        return getRequest(url, expectedStatus, null);
    }

    /**
     * "DELETE" the url and check for the expected status code
     * 
     * @param url
     * @param expectedStatus
     * @return
     * @throws IOException
     */
    protected MockHttpServletResponse deleteRequest(String url, int expectedStatus)
        throws IOException
    {
        return sendRequest(METHOD_DELETE, url, expectedStatus, null, null);
    }
    
    /**
     * "GET" the url and check for the expected status code 
     * 
     * @param url
     * @param expectedStatus
     * @param asUser
     * @return
     * @throws IOException
     */
    protected MockHttpServletResponse getRequest(String url, int expectedStatus, String asUser)
        throws IOException
    {
        return sendRequest(METHOD_GET, url, expectedStatus, null, null, asUser);
    }

    /**
     * "POST" the url and check for the expected status code
     * 
     * @param url
     * @param expectedStatus
     * @param body
     * @param contentType
     * @return
     * @throws IOException
     */
    protected MockHttpServletResponse postRequest(String url, int expectedStatus, String body, String contentType)
        throws IOException
    {
        return postRequest(url, expectedStatus, body.getBytes(), contentType, null);
    }

    /**
     * "POST" the url and check for the expected status code
     * 
     * @param url
     * @param expectedStatus
     * @param asUser
     * @return
     * @throws IOException
     */
    protected MockHttpServletResponse postRequest(String url, int expectedStatus, String body, String contentType, String asUser)
        throws IOException
    {
        return postRequest(url, expectedStatus, body.getBytes(), contentType, asUser);
    }

    /**
     * "POST" the url and check for the expected status code
     * 
     * @param url
     * @param expectedStatus
     * @return
     * @throws IOException
     */
    protected MockHttpServletResponse postRequest(String url, int expectedStatus, byte[] body, String contentType)
        throws IOException
    {
        return postRequest(url, expectedStatus, body, contentType, null);
    }

    /**
     * "POST" the url and check for the expected status code
     * 
     * @param url
     * @param expectedStatus
     * @param asUser
     * @return
     * @throws IOException
     */
    protected MockHttpServletResponse postRequest(String url, int expectedStatus, byte[] body, String contentType, String asUser)
        throws IOException
    {
        return sendRequest(METHOD_POST, url, expectedStatus, body, contentType, asUser);
    }

    /**
     * Send request to Test Web Script Server
     * 
     * @param url
     * @param expectedStatus
     * @param body
     * @param contentType
     * @return
     * @throws IOException
     */
    protected MockHttpServletResponse putRequest(String url, int expectedStatus, String body, String contentType)
        throws IOException
    {
        return sendRequest(METHOD_PUT, url, expectedStatus, body, contentType);
    }
    
    /**
     * 
     * @param method
     * @param url
     * @param expectedStatus
     * @param body
     * @param contentType
     * @param asUser
     * @return
     * @throws IOException
     */
    private MockHttpServletResponse sendRequest(final String method, final String url, final int expectedStatus, final byte[] body, final String contentType, String asUser)
        throws IOException
    {
        // send request in context of specified user
        String runAsUser = (asUser == null) ? AuthenticationUtil.getSystemUserName() : asUser;
        MockHttpServletResponse response = AuthenticationUtil.runAs(new RunAsWork<MockHttpServletResponse>()
        {
            @SuppressWarnings("synthetic-access")
            public MockHttpServletResponse doWork() throws Exception
            {
                return BaseWebScriptTest.getServer().submitRequest(method, url, new HashMap<String, String>(), body, contentType);
            }
        }, runAsUser);
        
        if (expectedStatus > 0 && expectedStatus != response.getStatus())
        {
            //if (response.getStatus() == 500)
            //{
            //    System.out.println(response.getContentAsString());
            //}
                        
            fail("Status code " + response.getStatus() + " returned, but expected " + expectedStatus + " for " + url + " (" + method + ")");
        }
        return response;
    }

}
