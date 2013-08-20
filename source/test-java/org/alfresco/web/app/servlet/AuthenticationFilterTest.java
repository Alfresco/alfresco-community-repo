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
package org.alfresco.web.app.servlet;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.web.config.ClientConfigElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.config.ConfigImpl;
import org.springframework.extensions.config.ConfigService;

/**
 * Test for the AuthenticationFilter class.
 * 
 * @author alex.mukha
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationFilterTest
{
    private static String loginPage = "loginpage";

    private String tenantDomain = "tenantDomain-" + System.currentTimeMillis();

    private @Mock ServletContext context;
    private @Mock HttpServletResponse res;
    private @Mock FilterChain chain;
    private @Mock ApplicationEvent event;
    
    /**
     * Test for the fix for ALF-18611
     * @throws Exception
     */
    @Test
    public void testALF18611() throws Exception
    {
        ClientConfigElement clientConfigElementMock = mock(ClientConfigElement.class);
        when(clientConfigElementMock.getLoginPage()).thenReturn(loginPage);
        ConfigImpl configImplMock = mock(ConfigImpl.class);
        when(configImplMock.getConfigElement(ClientConfigElement.CONFIG_ELEMENT_ID)).thenReturn(clientConfigElementMock);
        ConfigService configServiceMock = mock(ConfigService.class);
        when(configServiceMock.getGlobalConfig()).thenReturn(configImplMock);

        TenantContextHolder.setTenantDomain(tenantDomain);
        assertTrue("Tenant domain should be equal", TenantContextHolder.getTenantDomain().equals(tenantDomain.toLowerCase()));
        AuthenticationFilter authenticationFilter = new AuthenticationFilter();
        authenticationFilter.setConfigService(configServiceMock);
        authenticationFilter.onBootstrap(event);

        HttpServletRequest reqMock = mock(HttpServletRequest.class);
        when(reqMock.getRequestURI()).thenReturn(loginPage);
        authenticationFilter.doFilter(context, reqMock, res, chain);

        assertTrue("Tenant domain should be empty", TenantContextHolder.getTenantDomain() == null);
    }
}
