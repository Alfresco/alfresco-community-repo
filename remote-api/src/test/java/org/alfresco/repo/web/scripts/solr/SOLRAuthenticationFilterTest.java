/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail. Otherwise, the software is
 * provided under the following open source license terms:
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.web.scripts.solr;

import org.alfresco.error.AlfrescoRuntimeException;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;

public class SOLRAuthenticationFilterTest
{
    @Test(expected = AlfrescoRuntimeException.class)
    public void testSharedSecretNotConfigured() throws Exception
    {
        SOLRAuthenticationFilter filter = new SOLRAuthenticationFilter();
        filter.setSecureComms(SOLRAuthenticationFilter.SecureCommsType.SECRET.name());
        filter.afterPropertiesSet();
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void testSharedHeaderNotConfigured() throws Exception
    {
        SOLRAuthenticationFilter filter = new SOLRAuthenticationFilter();
        filter.setSecureComms(SOLRAuthenticationFilter.SecureCommsType.SECRET.name());
        filter.setSharedSecret("shared-secret");
        filter.setSharedSecretHeader("");
        filter.afterPropertiesSet();
    }

    @Test
    public void testHTTPSFilterAndSharedSecretSet() throws Exception
    {
        String headerKey = "test-header";
        String sharedSecret = "shared-secret";
        SOLRAuthenticationFilter filter = new SOLRAuthenticationFilter();
        filter.setSecureComms(SOLRAuthenticationFilter.SecureCommsType.HTTPS.name());
        filter.setSharedSecret(sharedSecret);
        filter.setSharedSecretHeader(headerKey);
        filter.afterPropertiesSet();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.when(request.getHeader(headerKey)).thenReturn(sharedSecret);
        Mockito.when(request.isSecure()).thenReturn(true);

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(Mockito.mock(ServletContext.class), request, response, chain);
        Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void testHTTPSFilterAndInsecureRequest() throws Exception
    {
        SOLRAuthenticationFilter filter = new SOLRAuthenticationFilter();
        filter.setSecureComms(SOLRAuthenticationFilter.SecureCommsType.HTTPS.name());
        filter.afterPropertiesSet();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.when(request.isSecure()).thenReturn(false);

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(Mockito.mock(ServletContext.class), request, response, chain);
    }

    @Test
    public void testNoAuthentication() throws Exception
    {
        SOLRAuthenticationFilter filter = new SOLRAuthenticationFilter();
        filter.setSecureComms(SOLRAuthenticationFilter.SecureCommsType.NONE.name());
        filter.afterPropertiesSet();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(Mockito.mock(ServletContext.class), request, response, chain);
        Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
    }

    @Test
    public void testSharedSecretFilter() throws Exception
    {
        String headerKey = "test-header";
        String sharedSecret = "shared-secret";
        SOLRAuthenticationFilter filter = new SOLRAuthenticationFilter();
        filter.setSecureComms(SOLRAuthenticationFilter.SecureCommsType.SECRET.name());
        filter.setSharedSecret(sharedSecret);
        filter.setSharedSecretHeader(headerKey);
        filter.afterPropertiesSet();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.when(request.getHeader(headerKey)).thenReturn(sharedSecret);

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(Mockito.mock(ServletContext.class), request, response, chain);
        Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
    }

    @Test
    public void testSharedSecretDontMatch() throws Exception
    {
        String headerKey = "test-header";
        String sharedSecret = "shared-secret";
        SOLRAuthenticationFilter filter = new SOLRAuthenticationFilter();
        filter.setSecureComms(SOLRAuthenticationFilter.SecureCommsType.SECRET.name());
        filter.setSharedSecret(sharedSecret);
        filter.setSharedSecretHeader(headerKey);
        filter.afterPropertiesSet();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.when(request.getHeader(headerKey)).thenReturn("wrong-secret");

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(Mockito.mock(ServletContext.class), request, response, chain);
        Mockito.verify(chain, Mockito.times(0)).doFilter(request, response);
        Mockito.verify(response).sendError(Mockito.eq(HttpServletResponse.SC_FORBIDDEN), Mockito.anyString());
    }

    @Test
    public void testSharedHeaderNotPresent() throws Exception
    {
        String headerKey = "test-header";
        String sharedSecret = "shared-secret";
        SOLRAuthenticationFilter filter = new SOLRAuthenticationFilter();
        filter.setSecureComms(SOLRAuthenticationFilter.SecureCommsType.SECRET.name());
        filter.setSharedSecret(sharedSecret);
        filter.setSharedSecretHeader(headerKey);
        filter.afterPropertiesSet();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(Mockito.mock(ServletContext.class), request, response, chain);
        Mockito.verify(chain, Mockito.times(0)).doFilter(request, response);
        Mockito.verify(response).sendError(Mockito.eq(HttpServletResponse.SC_FORBIDDEN), Mockito.anyString());
    }
}
