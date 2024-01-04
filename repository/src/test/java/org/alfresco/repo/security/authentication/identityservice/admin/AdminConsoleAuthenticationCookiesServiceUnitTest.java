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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.alfresco.repo.admin.SysAdminParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public class AdminConsoleAuthenticationCookiesServiceUnitTest
{
    private static final int DEFAULT_COOKIE_LIFETIME = 86400;
    private static final String COOKIE_NAME = "cookie";
    private static final String COOKIE_VALUE = "value";
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private SysAdminParams sysAdminParams;
    @Captor
    private ArgumentCaptor<Cookie> cookieCaptor;
    private AdminConsoleAuthenticationCookiesService cookiesService;

    @Before
    public void setUp()
    {
        initMocks(this);
        cookiesService = new AdminConsoleAuthenticationCookiesService(sysAdminParams, DEFAULT_COOKIE_LIFETIME);
    }

    @Test
    public void cookieShouldBeFoundInRequestThatContainsIt()
    {
        when(request.getCookies()).thenReturn(new Cookie[] { new Cookie(COOKIE_NAME, COOKIE_VALUE) });

        String cookie = cookiesService.getCookie(COOKIE_NAME, request);

        assertNotNull("The cookie should not be null", cookie);
        assertEquals("The cookie's value should match", COOKIE_VALUE, cookie);
        verify(request).getCookies();
    }

    @Test
    public void cookieShouldNotBeFoundInRequestThatDoesNotContainIt()
    {
        when(request.getCookies()).thenReturn(new Cookie[] { new Cookie(COOKIE_NAME, COOKIE_VALUE) });

        assertNull("The cookie should be null", cookiesService.getCookie("non-contained-cookie", request));

        verify(request).getCookies();
    }

    @Test
    public void cookieShouldNotBeFoundInRequestWithoutCookies()
    {
        when(request.getCookies()).thenReturn(null);

        assertNull("The cookie should be null", cookiesService.getCookie(COOKIE_NAME, request));

        verify(request).getCookies();
    }

    @Test
    public void cookieShouldBeAddedToTheResponseWithDefaultParams()
    {
        when(sysAdminParams.getAlfrescoProtocol()).thenReturn("http");

        cookiesService.addCookie(COOKIE_NAME, COOKIE_VALUE, response);

        verify(sysAdminParams).getAlfrescoProtocol();
        verify(response).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertNotNull("The cookie should not be null", cookie);
        assertEquals("Cookie's name should match", COOKIE_NAME, cookie.getName());
        assertEquals("Cookie's value should match", COOKIE_VALUE, cookie.getValue());
        assertEquals("Cookie's path should be the root", "/", cookie.getPath());
        assertEquals("Cookie's maxAge should match the default lifetime", DEFAULT_COOKIE_LIFETIME, cookie.getMaxAge());
        assertFalse("Cookie's secure flag should be false", cookie.getSecure());
    }

    @Test
    public void secureCookieShouldBeAddedToTheResponseWhenAlfrescoProtocolIsHttps()
    {
        when(sysAdminParams.getAlfrescoProtocol()).thenReturn("https");

        cookiesService.addCookie(COOKIE_NAME, COOKIE_VALUE, response);

        verify(sysAdminParams).getAlfrescoProtocol();
        verify(response).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertNotNull("The cookie should not be null", cookie);
        assertTrue("Cookie's secure flag should be true", cookie.getSecure());
    }

    @Test
    public void cookieWithCustomMaxAgeShouldBeAddedToTheResponse()
    {
        int customMaxAge = 60;
        cookiesService = new AdminConsoleAuthenticationCookiesService(sysAdminParams, customMaxAge);
        when(sysAdminParams.getAlfrescoProtocol()).thenReturn("https");

        cookiesService.addCookie(COOKIE_NAME, COOKIE_VALUE, response);

        verify(sysAdminParams).getAlfrescoProtocol();
        verify(response).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertNotNull("The cookie should not be null", cookie);
        assertEquals("Cookie's maxAge should match the custom lifetime", customMaxAge, cookie.getMaxAge());
    }

    @Test
    public void cookieShouldBeReset()
    {
        when(sysAdminParams.getAlfrescoProtocol()).thenReturn("http");

        cookiesService.resetCookie(COOKIE_NAME, response);

        verify(sysAdminParams).getAlfrescoProtocol();
        verify(response).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertNotNull("The cookie should not be null", cookie);
        assertEquals("Cookie's name should match", COOKIE_NAME, cookie.getName());
        assertEquals("Cookie's value should be reset", "", cookie.getValue());
        assertEquals("Cookie's path should be the root", "/", cookie.getPath());
        assertEquals("Cookie's maxAge should be 0", 0, cookie.getMaxAge());
        assertFalse("Cookie's secure flag should be false", cookie.getSecure());
    }

    @Test
    public void secureCookieShouldBeReset()
    {
        when(sysAdminParams.getAlfrescoProtocol()).thenReturn("https");

        cookiesService.resetCookie(COOKIE_NAME, response);

        verify(sysAdminParams).getAlfrescoProtocol();
        verify(response).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertNotNull("The cookie should not be null", cookie);
        assertTrue("Cookie's secure flag should be true", cookie.getSecure());
    }
}
