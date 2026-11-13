/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.rest.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.repo.web.scripts.BufferedRequest;
import org.alfresco.repo.webdav.auth.AuthenticationDriver;
import org.alfresco.repo.webdav.auth.BaseAuthenticationFilter;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.PublicApiTenantWebScriptServletRequest;
import org.alfresco.rest.api.model.LoginTicketResponse;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.web.app.servlet.KerberosAuthenticationFilter;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationsImplUnitTest
{
    private static final String USER = "user1";
    private static final String OTHER_USER = "user2";
    private static final String SESSION_TICKET = "TICKET_session";
    private static final String PARAM_TICKET_VALUE = "TICKET_param";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String GLOBAL_AUTHENTICATION_FILTER_BEAN = "globalAuthenticationFilter";
    private static final String KERBEROS_INSTANCE = "kerberos1";
    private static final String OTHER_INSTANCE = "other1";

    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private TicketComponent ticketComponent;
    @Mock
    private ChildApplicationContextManager authenticationContextManager;
    @Mock
    private Parameters parameters;
    @Mock
    private BufferedRequest bufferedRequest;
    @Mock
    private PublicApiTenantWebScriptServletRequest webScriptServletRequest;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpSession session;
    @Mock
    private SessionUser sessionUser;
    @Mock
    private ApplicationContext kerberosContext;
    @Mock
    private ApplicationContext otherContext;

    private KerberosAuthenticationFilter kerberosFilter;
    private AuthenticationsImpl authentications;

    @BeforeClass
    public static void initAuthenticationUtil() throws Exception
    {
        new AuthenticationUtil().afterPropertiesSet();
    }

    @Before
    public void setUp()
    {
        authentications = new AuthenticationsImpl();
        authentications.setAuthenticationService(authenticationService);
        authentications.setTicketComponent(ticketComponent);
        authentications.setAuthenticationContextManager(authenticationContextManager);

        AuthenticationUtil.setFullyAuthenticatedUser(USER);

        // Request without ticket, coming from an externally authenticated HTTP session
        lenient().when(parameters.getRequest()).thenReturn(bufferedRequest);
        lenient().when(bufferedRequest.getNext()).thenReturn(webScriptServletRequest);
        lenient().when(webScriptServletRequest.getHttpServletRequest()).thenReturn(httpServletRequest);
        lenient().when(httpServletRequest.getSession(false)).thenReturn(session);
        lenient().when(session.getAttribute(BaseAuthenticationFilter.LOGIN_EXTERNAL_AUTH)).thenReturn(Boolean.TRUE);
        lenient().when(session.getAttribute(AuthenticationDriver.AUTHENTICATION_USER)).thenReturn(sessionUser);
        lenient().when(sessionUser.getUserName()).thenReturn(USER);
        lenient().when(sessionUser.getTicket()).thenReturn(SESSION_TICKET);

        // Kerberos SSO is the active global authentication filter
        kerberosFilter = new KerberosAuthenticationFilter();
        lenient().when(authenticationContextManager.getInstanceIds()).thenReturn(List.of(KERBEROS_INSTANCE));
        lenient().when(authenticationContextManager.getApplicationContext(KERBEROS_INSTANCE)).thenReturn(kerberosContext);
        lenient().when(kerberosContext.getBean(GLOBAL_AUTHENTICATION_FILTER_BEAN)).thenReturn(kerberosFilter);

        // The session ticket is valid and belongs to the user
        lenient().when(ticketComponent.validateTicket(SESSION_TICKET)).thenReturn(USER);
    }

    @After
    public void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Test
    public void testSessionTicketReturnedWhenNoTicketProvided()
    {
        assertSessionTicketReturned();
    }

    @Test
    public void testSessionTicketReturnedWhenAuthorizationHeaderIsNegotiate()
    {
        when(bufferedRequest.getHeader(AUTHORIZATION_HEADER)).thenReturn("Negotiate YIIGtokenvalue");

        assertSessionTicketReturned();
    }

    @Test
    public void testAlfTicketParameterValidatedInsteadOfSessionTicket()
    {
        when(parameters.getParameter("alf_ticket")).thenReturn(PARAM_TICKET_VALUE);
        when(ticketComponent.validateTicket(PARAM_TICKET_VALUE)).thenReturn(USER);

        final LoginTicketResponse response = authentications.validateTicket(People.DEFAULT_USER, parameters, null);

        assertEquals(PARAM_TICKET_VALUE, response.getId());
        verify(ticketComponent, never()).validateTicket(SESSION_TICKET);
        verifyNoInteractions(authenticationContextManager);
    }

    @Test
    public void testSessionTicketNotReturnedWhenTicketParameterProvided()
    {
        when(parameters.getParameter("ticket")).thenReturn(PARAM_TICKET_VALUE);

        assertSsoNotAppliedBeforeKerberosCheck();
    }

    @Test
    public void testBasicAuthTicketValidatedInsteadOfSessionTicket()
    {
        final String basicTicket = "TICKET_basic";
        final String credentials = Base64.getEncoder().encodeToString((":" + basicTicket).getBytes(StandardCharsets.UTF_8));
        when(bufferedRequest.getHeader(AUTHORIZATION_HEADER)).thenReturn("Basic " + credentials);
        when(ticketComponent.validateTicket(basicTicket)).thenReturn(USER);

        final LoginTicketResponse response = authentications.validateTicket(People.DEFAULT_USER, parameters, null);

        assertEquals(basicTicket, response.getId());
        verify(ticketComponent, never()).validateTicket(SESSION_TICKET);
        verifyNoInteractions(authenticationContextManager);
    }

    @Test
    public void testSessionTicketNotReturnedWhenAuthorizationHeaderIsBearer()
    {
        when(bufferedRequest.getHeader(AUTHORIZATION_HEADER)).thenReturn("Bearer token");

        assertSsoNotAppliedBeforeKerberosCheck();
    }

    @Test
    public void testSessionTicketNotReturnedWhenUserNotAuthenticated()
    {
        AuthenticationUtil.clearCurrentSecurityContext();

        assertSsoNotAppliedBeforeKerberosCheck();
    }

    @Test
    public void testSessionTicketNotReturnedWhenRunningAsSystemUser()
    {
        AuthenticationUtil.setRunAsUserSystem();

        assertSsoNotAppliedBeforeKerberosCheck();
    }

    @Test
    public void testSessionTicketNotReturnedWhenNotAServletRequest()
    {
        when(bufferedRequest.getNext()).thenReturn(mock(WebScriptRequest.class));

        assertSsoNotAppliedBeforeKerberosCheck();
    }

    @Test
    public void testSessionTicketNotReturnedWhenNoHttpSession()
    {
        when(httpServletRequest.getSession(false)).thenReturn(null);

        assertSsoNotAppliedBeforeKerberosCheck();
    }

    @Test
    public void testSessionTicketNotReturnedWhenSessionNotExternallyAuthenticated()
    {
        when(session.getAttribute(BaseAuthenticationFilter.LOGIN_EXTERNAL_AUTH)).thenReturn(null);

        assertSsoNotAppliedBeforeKerberosCheck();
    }

    @Test
    public void testSessionTicketNotReturnedWhenNoSessionUser()
    {
        when(session.getAttribute(AuthenticationDriver.AUTHENTICATION_USER)).thenReturn(USER);

        assertSsoNotAppliedBeforeKerberosCheck();
    }

    @Test
    public void testSessionTicketNotReturnedWhenSessionUserIsAnotherUser()
    {
        when(sessionUser.getUserName()).thenReturn(OTHER_USER);

        assertSsoNotAppliedBeforeKerberosCheck();
    }

    @Test
    public void testSessionTicketNotReturnedWhenSessionTicketIsEmpty()
    {
        when(sessionUser.getTicket()).thenReturn("");

        assertSsoNotAppliedBeforeKerberosCheck();
    }

    @Test
    public void testSessionTicketNotReturnedWhenNoAuthenticationContextManager()
    {
        authentications.setAuthenticationContextManager(null);

        assertSsoNotApplied();
    }

    @Test
    public void testSessionTicketNotReturnedWhenKerberosFilterIsInactive()
    {
        kerberosFilter.setActive(false);

        assertSsoNotApplied();
    }

    @Test
    public void testSessionTicketNotReturnedWhenAnotherFilterIsActiveBeforeKerberos()
    {
        final ActivateableBean otherFilter = mock(ActivateableBean.class);
        when(otherFilter.isActive()).thenReturn(true);
        when(authenticationContextManager.getInstanceIds()).thenReturn(List.of(OTHER_INSTANCE, KERBEROS_INSTANCE));
        when(authenticationContextManager.getApplicationContext(OTHER_INSTANCE)).thenReturn(otherContext);
        when(otherContext.getBean(GLOBAL_AUTHENTICATION_FILTER_BEAN)).thenReturn(otherFilter);

        assertSsoNotApplied();
    }

    @Test
    public void testSessionTicketReturnedWhenSubsystemWithoutGlobalFilterPrecedesKerberos()
    {
        when(authenticationContextManager.getInstanceIds()).thenReturn(List.of(OTHER_INSTANCE, KERBEROS_INSTANCE));
        when(authenticationContextManager.getApplicationContext(OTHER_INSTANCE)).thenReturn(otherContext);
        when(otherContext.getBean(GLOBAL_AUTHENTICATION_FILTER_BEAN)).thenThrow(new NoSuchBeanDefinitionException(GLOBAL_AUTHENTICATION_FILTER_BEAN));

        assertSessionTicketReturned();
    }

    @Test
    public void testSessionTicketReturnedWhenSubsystemThatFailedToStartPrecedesKerberos()
    {
        when(authenticationContextManager.getInstanceIds()).thenReturn(List.of(OTHER_INSTANCE, KERBEROS_INSTANCE));
        when(authenticationContextManager.getApplicationContext(OTHER_INSTANCE)).thenThrow(new RuntimeException("Subsystem failed to start"));

        assertSessionTicketReturned();
    }

    @Test
    public void testSessionTicketNotReturnedWhenSessionTicketIsInvalid()
    {
        when(ticketComponent.validateTicket(SESSION_TICKET)).thenThrow(new AuthenticationException("Invalid ticket"));

        assertThrows(InvalidArgumentException.class, this::validateTicket);
        verify(ticketComponent).validateTicket(SESSION_TICKET);
    }

    @Test
    public void testSessionTicketNotReturnedWhenSessionTicketBelongsToAnotherUser()
    {
        when(ticketComponent.validateTicket(SESSION_TICKET)).thenReturn(OTHER_USER);

        assertThrows(InvalidArgumentException.class, this::validateTicket);
        verify(ticketComponent).validateTicket(SESSION_TICKET);
    }

    private LoginTicketResponse validateTicket()
    {
        return authentications.validateTicket(People.DEFAULT_USER, parameters, null);
    }

    private void assertSessionTicketReturned()
    {
        final LoginTicketResponse response = validateTicket();

        assertEquals(SESSION_TICKET, response.getId());
        verify(ticketComponent).validateTicket(SESSION_TICKET);
    }

    private void assertSsoNotApplied()
    {
        assertThrows(InvalidArgumentException.class, this::validateTicket);
        verify(ticketComponent, never()).validateTicket(SESSION_TICKET);
    }

    private void assertSsoNotAppliedBeforeKerberosCheck()
    {
        assertSsoNotApplied();
        verifyNoInteractions(authenticationContextManager);
    }
}
