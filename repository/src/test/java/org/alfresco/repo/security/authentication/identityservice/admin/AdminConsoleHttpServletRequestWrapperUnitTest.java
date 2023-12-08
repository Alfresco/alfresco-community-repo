/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice.admin;

import static java.util.Collections.enumeration;
import static java.util.Collections.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.alfresco.error.AlfrescoRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class AdminConsoleHttpServletRequestWrapperUnitTest
{

    private static final String DEFAULT_HEADER = "default_header";
    private static final String DEFAULT_HEADER_VALUE = "default_value";
    private static final String ADDITIONAL_HEADER = "additional_header";
    private static final String ADDITIONAL_HEADER_VALUE = "additional_value";
    private static final Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>()
    {{
        put(DEFAULT_HEADER, DEFAULT_HEADER_VALUE);
    }};
    private static final Map<String, String> ADDITIONAL_HEADERS = new HashMap<String, String>()
    {{
        put(ADDITIONAL_HEADER, ADDITIONAL_HEADER_VALUE);
    }};

    @Mock
    private HttpServletRequest request;
    private AdminConsoleHttpServletRequestWrapper requestWrapper;

    @Before
    public void setUp()
    {
        initMocks(this);
        requestWrapper = new AdminConsoleHttpServletRequestWrapper(ADDITIONAL_HEADERS, request);
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void wrapperShouldNotBeInstancedWithoutAdditionalHeaders()
    {
        new AdminConsoleHttpServletRequestWrapper(null, request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrapperShouldNotBeInstancedWithoutRequestsToWrap()
    {
        new AdminConsoleHttpServletRequestWrapper(new HashMap<>(), null);
    }

    @Test
    public void wrapperShouldReturnAdditionalHeaderNamesOnTopOfDefaultOnes()
    {
        when(request.getHeaderNames()).thenReturn(enumeration(DEFAULT_HEADERS.keySet()));

        Enumeration<String> headerNames = requestWrapper.getHeaderNames();
        assertNotNull("headerNames should not be null", headerNames);
        assertTrue("headerNames should not be empty", headerNames.hasMoreElements());

        List<String> headers = list(headerNames);
        assertEquals("There should be 2 headers", 2, headers.size());
        assertTrue("The default header should be included", headers.contains(DEFAULT_HEADER));
        assertTrue("The additional header should be included", headers.contains(ADDITIONAL_HEADER));

        verify(request).getHeaderNames();
    }

    @Test
    public void wrapperShouldReturnDefaultHeaderNamesIfNoAdditionalHeaders()
    {
        when(request.getHeaderNames()).thenReturn(enumeration(DEFAULT_HEADERS.keySet()));

        requestWrapper = new AdminConsoleHttpServletRequestWrapper(new HashMap<>(), request);
        Enumeration<String> headerNames = requestWrapper.getHeaderNames();
        assertNotNull("headerNames should not be null", headerNames);
        assertTrue("headerNames should not be empty", headerNames.hasMoreElements());
        assertEquals("The returned header should be the default header", DEFAULT_HEADER, headerNames.nextElement());
        assertFalse("There should be no additional headers", headerNames.hasMoreElements());

        verify(request).getHeaderNames();
    }

    @Test
    public void wrapperShouldReturnAdditionalHeaderNamesIfNoDefaultHeaders()
    {
        when(request.getHeaderNames()).thenReturn(null);

        Enumeration<String> headerNames = requestWrapper.getHeaderNames();
        assertNotNull("headerNames should not be null", headerNames);
        assertTrue("headerNames should not be empty", headerNames.hasMoreElements());
        assertEquals("The returned header should be the additional header", ADDITIONAL_HEADER,
            headerNames.nextElement());
        assertFalse("There should be no more headers", headerNames.hasMoreElements());

        verify(request).getHeaderNames();
    }

    @Test
    public void wrapperShouldReturnDefaultHeaderValues()
    {
        when(request.getHeader(DEFAULT_HEADER)).thenReturn(DEFAULT_HEADER_VALUE);

        String header = requestWrapper.getHeader(DEFAULT_HEADER);
        assertEquals("The header should be the default one", DEFAULT_HEADER_VALUE, header);

        verify(request).getHeader(DEFAULT_HEADER);
    }

    @Test
    public void wrapperShouldReturnAdditionalHeaderValues()
    {
        String header = requestWrapper.getHeader(ADDITIONAL_HEADER);
        assertEquals("The header should be the additional one", ADDITIONAL_HEADER_VALUE, header);
    }

    @Test
    public void wrapperShouldPreferAdditionalHeaderValuesToDefaultOnes()
    {
        when(request.getHeader(DEFAULT_HEADER)).thenReturn(DEFAULT_HEADER_VALUE);

        String overrideHeaderValue = "override";
        Map<String, String> overrideHeaders = new HashMap<>();
        overrideHeaders.put(DEFAULT_HEADER, overrideHeaderValue);

        requestWrapper = new AdminConsoleHttpServletRequestWrapper(overrideHeaders, request);
        String header = requestWrapper.getHeader(DEFAULT_HEADER);
        assertEquals("The header should have the overridden value", overrideHeaderValue, header);

        verify(request).getHeader(DEFAULT_HEADER);
    }

    @Test
    public void wrapperShouldReturnDefaultHeaderEnumeration()
    {
        when(request.getHeader(DEFAULT_HEADER)).thenReturn(DEFAULT_HEADER_VALUE);

        Enumeration<String> headers = requestWrapper.getHeaders(DEFAULT_HEADER);
        assertNotNull("The headers enumeration should not be null", headers);
        assertTrue("The headers enumeration should not be empty", headers.hasMoreElements());
        assertEquals("The header should be the default one", DEFAULT_HEADER_VALUE, headers.nextElement());
        assertFalse("There should be no more headers", headers.hasMoreElements());

        verify(request).getHeader(DEFAULT_HEADER);
    }

    @Test
    public void wrapperShouldReturnAdditionalHeaderEnumeration()
    {
        Enumeration<String> headers = requestWrapper.getHeaders(ADDITIONAL_HEADER);
        assertNotNull("The headers enumeration should not be null", headers);
        assertTrue("The headers enumeration should not be empty", headers.hasMoreElements());
        assertEquals("The header should be the additional one", ADDITIONAL_HEADER_VALUE, headers.nextElement());
        assertFalse("There should be no more headers", headers.hasMoreElements());
    }

    @Test
    public void wrapperShouldPreferAdditionalHeaderEnumerationValuesToDefaultOnes()
    {
        when(request.getHeader(DEFAULT_HEADER)).thenReturn(DEFAULT_HEADER_VALUE);

        String overrideHeaderValue = "override";
        Map<String, String> overrideHeaders = new HashMap<>();
        overrideHeaders.put(DEFAULT_HEADER, overrideHeaderValue);

        requestWrapper = new AdminConsoleHttpServletRequestWrapper(overrideHeaders, request);
        Enumeration<String> headers = requestWrapper.getHeaders(DEFAULT_HEADER);
        assertNotNull("The headers enumeration should not be null", headers);
        assertTrue("The headers enumeration should not be empty", headers.hasMoreElements());
        assertEquals("The header should be the overridden one", overrideHeaderValue, headers.nextElement());
        assertFalse("There should be no more headers", headers.hasMoreElements());

        verify(request).getHeader(DEFAULT_HEADER);
    }
}
