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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.alfresco.repo.webdav.WebDAVServlet.WebDAVInitParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests for the allowing insecure POST method flag.
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

    @Before
    public void setUp()
    {
        davServlet = new WebDAVServlet();
        ReflectionTestUtils.setField(davServlet, "initParams", webDAVInitParameters);
        doReturn(true).when(webDAVInitParameters).getEnabled();
    }


    @Test
    public void shouldReturn405StatusForPostMethodWhenNotAllowed() throws ServletException, IOException
    {
        // given
        when(webDAVInitParameters.isPostMethodAllowed()).thenReturn(false);
        when(request.getMethod()).thenReturn("POST");

        // when
        davServlet.service(request, response);

        // then
        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void shouldNotReturn405StatusForPostMethodWhenAllowed() throws ServletException, IOException
    {
        // given
        when(webDAVInitParameters.isPostMethodAllowed()).thenReturn(true);
        when(request.getMethod()).thenReturn("POST");

        // when
        davServlet.service(request, response);

        // then
        verify(response, never()).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void shouldNotReturn405StatusForPutMethod() throws ServletException, IOException
    {
        // given
        when(request.getMethod()).thenReturn("PUT");

        // when
        davServlet.service(request, response);

        // then
        verify(response, never()).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void shouldNotReturn405StatusForGetMethod() throws ServletException, IOException
    {
        // given
        when(request.getMethod()).thenReturn("GET");

        // when
        davServlet.service(request, response);

        // then
        verify(response, never()).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void shouldNotReturn405StatusForDeleteMethod() throws ServletException, IOException
    {
        // given
        when(request.getMethod()).thenReturn("DELETE");

        // when
        davServlet.service(request, response);

        // then
        verify(response, never()).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
