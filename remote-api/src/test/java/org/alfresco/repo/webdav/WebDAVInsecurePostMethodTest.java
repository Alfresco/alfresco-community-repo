/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.webdav;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Hashtable;

import org.alfresco.repo.webdav.WebDAVServlet.WebDAVInitParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests for the allowInsecurePOSTMethod flag.
 *
 * @see  WebDAVInitParameters
 * @author Aleksandra Onych
 */
@RunWith(MockitoJUnitRunner.class)
public class WebDAVInsecurePostMethodTest
{
    private WebDAVServlet davServlet;
    private @Mock WebDAVInitParameters webDAVInitParameters;
    private @Mock HttpServletRequest request;
    private @Mock HttpServletResponse response;
    private @Mock Hashtable<String,Class<? extends WebDAVMethod>> davMethods;

    @Before
    public void setUp()
    {
        davServlet = new WebDAVServlet();
        ReflectionTestUtils.setField(davServlet, "initParams", webDAVInitParameters);
        ReflectionTestUtils.setField(davServlet, "m_davMethods", davMethods);
        when(webDAVInitParameters.getEnabled()).thenReturn(true);
    }


    @Test
    public void shouldReturn405StatusWhenPostMethodIsNotAllowed() throws ServletException, IOException
    {
        prepareRequest(WebDAV.METHOD_POST);
        when(webDAVInitParameters.allowInsecurePOSTMethod()).thenReturn(false);

        davServlet.service(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void shouldNotReturn405StatusWhenPostMethodIsAllowed() throws ServletException, IOException
    {
        prepareRequest(WebDAV.METHOD_POST);
        when(webDAVInitParameters.allowInsecurePOSTMethod()).thenReturn(true);

        davServlet.service(request, response);

        verify(response, never()).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void shouldNotReturn405StatusForPutMethod() throws ServletException, IOException
    {
        prepareRequest(WebDAV.METHOD_PUT);

        davServlet.service(request, response);

        verify(response, never()).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void shouldNotReturn405StatusForGetMethod() throws ServletException, IOException
    {
        prepareRequest(WebDAV.METHOD_GET);

        davServlet.service(request, response);

        verify(response, never()).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void shouldNotReturn405StatusForDeleteMethod() throws ServletException, IOException
    {
        prepareRequest(WebDAV.METHOD_DELETE);

        davServlet.service(request, response);

        verify(response, never()).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void prepareRequest(String requestMethodName)
    {
        doReturn(PutMethod.class).when(davMethods).get(requestMethodName);
        when(request.getMethod()).thenReturn(requestMethodName);
    }
}
