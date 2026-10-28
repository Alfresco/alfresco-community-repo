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
package org.alfresco.repo.webdav.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;

/**
 * Unit tests for the Basic credentials handling of {@link SSOFallbackBasicAuthenticationDriver#authenticateRequest(ServletContext, HttpServletRequest, HttpServletResponse)}: user name and password, ticket with a user name part (":TICKET_xxx", "ROLE_TICKET:TICKET_xxx"), bare ticket ("TICKET_xxx") and empty credentials.
 */
@RunWith(MockitoJUnitRunner.class)
public class SSOFallbackBasicAuthenticationDriverTest
{
    private static final String USER = "user1";
    // pragma: allowlist nextline secret dummy password for a mocked AuthenticationService
    private static final String PASSWORD = "secret";
    private static final String TICKET = "TICKET_abc";
    private static final NodeRef PERSON_NODE = new NodeRef("workspace://SpacesStore/person");
    private static final NodeRef HOME_FOLDER = new NodeRef("workspace://SpacesStore/home");

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private PersonService personService;

    @Mock
    private NodeService nodeService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private RetryingTransactionHelper retryingTransactionHelper;

    @Mock
    private ServletContext servletContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private SSOFallbackBasicAuthenticationDriver driver;

    @BeforeClass
    public static void initAuthenticationUtil() throws Exception
    {
        new AuthenticationUtil().afterPropertiesSet();
    }

    @Before
    public void setUp() throws Throwable
    {
        driver = new SSOFallbackBasicAuthenticationDriver();
        driver.setAuthenticationService(authenticationService);
        driver.setPersonService(personService);
        driver.setNodeService(nodeService);
        driver.setTransactionService(transactionService);

        // No user in the HTTP session yet: the Basic credentials are processed
        lenient().when(request.getSession()).thenReturn(session);

        // Building the session user after a successful authentication
        lenient().when(transactionService.getRetryingTransactionHelper()).thenReturn(retryingTransactionHelper);
        lenient().when(retryingTransactionHelper.doInTransaction(any(RetryingTransactionCallback.class), eq(true)))
                .thenAnswer(invocation -> invocation.<RetryingTransactionCallback<?>> getArgument(0).execute());
        lenient().when(authenticationService.getCurrentUserName()).thenReturn(USER);
        lenient().when(authenticationService.getCurrentTicket()).thenReturn(TICKET);
        lenient().when(personService.getPerson(USER)).thenReturn(PERSON_NODE);
        lenient().when(nodeService.getProperty(PERSON_NODE, ContentModel.PROP_USERNAME)).thenReturn(USER);
        lenient().when(nodeService.getProperty(PERSON_NODE, ContentModel.PROP_HOMEFOLDER)).thenReturn(HOME_FOLDER);
    }

    @After
    public void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Test
    public void testUserNameAndPasswordAuthenticated() throws Exception
    {
        givenBasicCredentials(USER + ":" + PASSWORD);

        assertTrue(authenticateRequest());

        verify(authenticationService).authenticate(USER, PASSWORD.toCharArray());
        verify(authenticationService, never()).validate(anyString());
        assertSessionUserStored();
    }

    @Test
    public void testTicketWithEmptyUserNameValidated() throws Exception
    {
        givenBasicCredentials(":" + TICKET);

        assertTrue(authenticateRequest());

        assertTicketValidatedInsteadOfAuthentication(TICKET);
        assertSessionUserStored();
    }

    @Test
    public void testTicketWithTicketUserIdValidated() throws Exception
    {
        givenBasicCredentials("ROLE_TICKET:" + TICKET);

        assertTrue(authenticateRequest());

        assertTicketValidatedInsteadOfAuthentication(TICKET);
        assertSessionUserStored();
    }

    @Test
    public void testBareTicketValidated() throws Exception
    {
        givenBasicCredentials(TICKET);

        assertTrue(authenticateRequest());

        assertTicketValidatedInsteadOfAuthentication(TICKET);
        assertSessionUserStored();
    }

    @Test
    public void testInvalidBareTicketNotAuthenticated() throws Exception
    {
        givenBasicCredentials(TICKET);
        doThrow(new AuthenticationException("Invalid ticket")).when(authenticationService).validate(TICKET);

        assertFalse(authenticateRequest());

        assertTicketValidatedInsteadOfAuthentication(TICKET);
        verify(session, never()).setAttribute(eq(AuthenticationDriver.AUTHENTICATION_USER), any());
    }

    @Test
    public void testEmptyCredentialsNotAuthenticated() throws Exception
    {
        // "Basic " followed by nothing decodes to empty credentials, which Authorization(String) would reject
        when(request.getHeader("Authorization")).thenReturn("Basic ");
        doThrow(new AuthenticationException("Invalid ticket")).when(authenticationService).validate("");

        assertFalse(authenticateRequest());

        assertTicketValidatedInsteadOfAuthentication("");
        verify(session, never()).setAttribute(eq(AuthenticationDriver.AUTHENTICATION_USER), any());
    }

    private void givenBasicCredentials(String credentials)
    {
        final String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        when(request.getHeader("Authorization")).thenReturn("Basic " + encoded);
    }

    private boolean authenticateRequest() throws Exception
    {
        return driver.authenticateRequest(servletContext, request, response);
    }

    private void assertTicketValidatedInsteadOfAuthentication(String ticket)
    {
        verify(authenticationService).validate(ticket);
        verify(authenticationService, never()).authenticate(anyString(), any(char[].class));
    }

    private void assertSessionUserStored()
    {
        final ArgumentCaptor<SessionUser> sessionUser = ArgumentCaptor.forClass(SessionUser.class);
        verify(session).setAttribute(eq(AuthenticationDriver.AUTHENTICATION_USER), sessionUser.capture());
        assertEquals(USER, sessionUser.getValue().getUserName());
        assertEquals(TICKET, sessionUser.getValue().getTicket());
    }
}
