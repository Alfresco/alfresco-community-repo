/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.security.authentication.identityservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.ConnectException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationException;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;

/**
 * Verifies the slim {@link IdentityServiceAuthenticationComponent} contract: it delegates credential validation to its injected {@link UserTokenProvider} and translates failures into {@link AuthenticationException}. The decorator/caching behaviour is covered by dedicated unit tests on {@link CachingUserTokenProvider} and {@link DirectUserTokenProvider}; this test deliberately knows nothing about caching.
 */
public class IdentityServiceAuthenticationComponentTest extends BaseSpringTest
{
    private static final String TEST_USER = "username";
    private static final String TEST_PASS = "password";
    private static final String FRESH_TOKEN = "fresh.jwt.value";

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

    private UserTokenProvider userTokenProvider;

    @Before
    public void setUp()
    {
        authComponent.setAuthenticationContext(authenticationContext);
        authComponent.setTransactionService(transactionService);
        authComponent.setUserRegistrySynchronizer(userRegistrySynchronizer);
        authComponent.setNodeService(nodeService);
        authComponent.setPersonService(personService);

        userTokenProvider = mock(UserTokenProvider.class);
        authComponent.setUserTokenProvider(userTokenProvider);
    }

    @After
    public void tearDown()
    {
        authenticationContext.clearCurrentSecurityContext();
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticationFail()
    {
        doThrow(new AuthorizationException("Failed"))
                .when(userTokenProvider).getUserToken(any(), any());

        authComponent.authenticateImpl(TEST_USER, TEST_PASS.toCharArray());
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticationFail_connectionException()
    {
        doThrow(new AuthorizationException("Couldn't connect to server",
                new ConnectException("ConnectionRefused")))
                        .when(userTokenProvider).getUserToken(any(), any());

        try
        {
            authComponent.authenticateImpl(TEST_USER, TEST_PASS.toCharArray());
        }
        catch (RuntimeException ex)
        {
            Throwable cause = ExceptionStackUtil.getCause(ex, ConnectException.class);
            assertNotNull(cause);
            throw ex;
        }
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticationFail_otherException()
    {
        doThrow(new RuntimeException("Some other errors!"))
                .when(userTokenProvider).getUserToken(any(), any());

        authComponent.authenticateImpl(TEST_USER, TEST_PASS.toCharArray());
    }

    @Test
    public void testAuthenticationPass()
    {
        when(userTokenProvider.getUserToken(any(), any()))
                .thenReturn(new UserToken(TEST_USER, FRESH_TOKEN));

        authComponent.authenticateImpl(TEST_USER, TEST_PASS.toCharArray());

        assertEquals("User has not been set as expected.", TEST_USER, authenticationContext.getCurrentUserName());
    }

    @Test(expected = AuthenticationException.class)
    public void testFallthroughWhenUserTokenProviderIsNull()
    {
        authComponent.setUserTokenProvider(null);
        authComponent.authenticateImpl(TEST_USER, TEST_PASS.toCharArray());
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
