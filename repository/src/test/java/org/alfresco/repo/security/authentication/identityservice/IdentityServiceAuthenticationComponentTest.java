/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.ConnectException;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;

public class IdentityServiceAuthenticationComponentTest extends BaseSpringTest
{
    private final IdentityServiceAuthenticationComponent authComponent = new IdentityServiceAuthenticationComponent();

    @Autowired
    private AuthenticationContext authenticationContext;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRegistrySynchronizer userRegistrySynchronizer;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private PersonService personService;

    private AuthzClient mockAuthzClient;

    @Before
    public void setUp()
    {
        authComponent.setAuthenticationContext(authenticationContext);
        authComponent.setTransactionService(transactionService);
        authComponent.setUserRegistrySynchronizer(userRegistrySynchronizer);
        authComponent.setNodeService(nodeService);
        authComponent.setPersonService(personService);

        mockAuthzClient = mock(AuthzClient.class);
        authComponent.setAuthenticatorAuthzClient(mockAuthzClient);
    }

    @After
    public void tearDown()
    {
        authenticationContext.clearCurrentSecurityContext();
    }

    @Test (expected=AuthenticationException.class)
    public void testAuthenticationFail()
    {
        when(mockAuthzClient.obtainAccessToken("username", "password"))
                .thenThrow(new HttpResponseException("Unauthorized", 401, "Unauthorized", null));

        authComponent.authenticateImpl("username", "password".toCharArray());
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticationFail_connectionException()
    {
        when(mockAuthzClient.obtainAccessToken("username", "password")).thenThrow(
                    new RuntimeException("Couldn't connect to server", new ConnectException("ConnectionRefused")));

        try
        {
            authComponent.authenticateImpl("username", "password".toCharArray());
        }
        catch (RuntimeException ex)
        {
            Throwable cause = ExceptionStackUtil.getCause(ex, ConnectException.class);
            assertNotNull(cause);
            throw ex;
        }
    }

    @Test (expected=AuthenticationException.class)
    public void testAuthenticationFail_otherException()
    {
        when(mockAuthzClient.obtainAccessToken("username", "password"))
                    .thenThrow(new RuntimeException("Some other errors!"));

        authComponent.authenticateImpl("username", "password".toCharArray());
    }

    @Test
    public void testAuthenticationPass()
    {
        when(mockAuthzClient.obtainAccessToken("username", "password"))
                .thenReturn(new AccessTokenResponse());

        authComponent.authenticateImpl("username", "password".toCharArray());

        // Check that the authenticated user has been set
        assertEquals("User has not been set as expected.","username", authenticationContext.getCurrentUserName());
    }

    @Test (expected= AuthenticationException.class)
    public void testFallthroughWhenAuthzClientIsNull()
    {
        authComponent.setAuthenticatorAuthzClient(null);
        authComponent.authenticateImpl("username", "password".toCharArray());
    }

    @Test
    public void testSettingAllowGuestUser()
    {
        authComponent.setAllowGuestLogin(true);
        assertTrue(authComponent.guestUserAuthenticationAllowed());

        authComponent.setAllowGuestLogin(false);
        assertFalse(authComponent.guestUserAuthenticationAllowed());
    }
}
