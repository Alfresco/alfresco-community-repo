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

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.filesys.ExtendedServerConfigurationAccessor;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.repo.security.authentication.AuthenticationServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.external.DefaultRemoteUserMapper;
import org.alfresco.repo.security.authentication.jaas.JAASAuthenticationComponent;
import org.alfresco.repo.web.auth.NoopAuthenticationListener;
import org.alfresco.repo.webdav.auth.KerberosAuthenticationFilter;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;


@RunWith(MockitoJUnitRunner.class)
public class KerberosRemoteUserMapperTest
{
    private static ApplicationContext ctx;

    private KerberosAuthenticationFilter webDavAuthFilter;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext(new String[] { "classpath:alfresco/application-context.xml" });
    }

    @After
    public void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Before
    public void setUp() throws Exception
    {
        // authentication.chain=external1:external,kerberos1:kerberos
        // create webDavAuthenticationFilter from kerberos authentication chain
        webDavAuthFilter = new KerberosAuthenticationFilter();
        webDavAuthFilter.setActive(true);
        webDavAuthFilter.setTicketLogons(true);
        webDavAuthFilter.setServerConfiguration((ExtendedServerConfigurationAccessor) ctx.getBean("fileServerConfiguration"));
        webDavAuthFilter.setAuthenticationListener(new NoopAuthenticationListener());

        // kerberos authenticationComponent
        JAASAuthenticationComponent krbAuthComponent = new JAASAuthenticationComponent();
        krbAuthComponent.setRealm("ALFRESCO.DOMAIN.LOCAL");
        krbAuthComponent.setJaasConfigEntryName("AlfrescoHTTP");
        krbAuthComponent.setNodeService((NodeService) ctx.getBean("nodeService"));
        krbAuthComponent.setPersonService((PersonService) ctx.getBean("personService"));
        krbAuthComponent.setTransactionService((TransactionService) ctx.getBean("transactionService"));
        krbAuthComponent.setDefaultAdministratorUserNameList("Administrator");

        krbAuthComponent.setAuthenticationContext((AuthenticationContext) ctx.getBean("authenticationContext"));
        krbAuthComponent.setUserRegistrySynchronizer((UserRegistrySynchronizer) ctx.getBean("userRegistrySynchronizer"));

        webDavAuthFilter.setAuthenticationComponent(krbAuthComponent);

        // kerberos AuthenticationService
        AuthenticationServiceImpl krbLocalAuthService = new AuthenticationServiceImpl();
        krbLocalAuthService.setTicketComponent((TicketComponent) ctx.getBean("ticketComponent"));
        krbLocalAuthService.setAuthenticationComponent(krbAuthComponent);
        krbLocalAuthService.setSysAdminParams((SysAdminParams) ctx.getBean("sysAdminParams"));

        webDavAuthFilter.setAuthenticationService(krbLocalAuthService);

        webDavAuthFilter.setPersonService((PersonService) ctx.getBean("personService"));
        webDavAuthFilter.setNodeService((NodeService) ctx.getBean("NodeService"));
        webDavAuthFilter.setTransactionService((TransactionService) ctx.getBean("TransactionService"));
        webDavAuthFilter.setRealm("ALFRESCO.DOMAIN.LOCAL");
        webDavAuthFilter.setPassword("12345678");
        webDavAuthFilter.setJaasConfigEntryName("AlfrescoHTTP");
        webDavAuthFilter.setStripKerberosUsernameSuffix(true);

        //create remoteUserMapper from external authentication chain
        DefaultRemoteUserMapper remoteUserMapper = new DefaultRemoteUserMapper();
        remoteUserMapper.setActive(true);
        remoteUserMapper.setProxyUserName("alfresco-system");
        remoteUserMapper.setProxyHeader("X-Alfresco-Remote-User");
        remoteUserMapper.setUserIdPattern(null);
        remoteUserMapper.setPersonService((PersonService) ctx.getBean("PersonService"));

        webDavAuthFilter.setRemoteUserMapper(remoteUserMapper);

    }

    @Test
    public void testMNT_9979()
    {
        // create mock request
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("X-Alfresco-Remote-User")).thenReturn("alfresco-system");
        when(mockRequest.getRemoteUser()).thenReturn("alfresco-system");

        // create mock session
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession()).thenReturn(mockSession);

        // create mock response
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        // create mock context
        ServletContext mockContext = mock(ServletContext.class);

        boolean result = false;
        String message;
        try
        {
            result = webDavAuthFilter.authenticateRequest(mockContext, mockRequest, mockResponse);
            message = "External, kerberos authentication";
        }
        catch (IOException e)
        {
            message = e.getMessage();
        }
        catch (ServletException e)
        {
            message = e.getMessage();
        }
        
        assertTrue(message, result);
    }

}
