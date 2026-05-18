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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AccessToken;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AccessTokenAuthorization;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;
import org.alfresco.repo.security.authentication.identityservice.user.OIDCUserInfo;

/**
 * Pure unit tests for {@link DirectUserTokenProvider}: every call performs a fresh authorize() against the IdP and runs JIT provisioning before returning. No caching, no token decoding.
 */
public class DirectUserTokenProviderTest
{
    private static final String TEST_USER = "alice";
    private static final char[] TEST_PASS = "p@ssw0rd".toCharArray();
    private static final String TOKEN_VALUE = "fresh.jwt.value";

    private IdentityServiceFacade facade;
    private IdentityServiceJITProvisioningHandler jit;
    private DirectUserTokenProvider direct;

    @Before
    public void setUp()
    {
        facade = mock(IdentityServiceFacade.class);
        jit = mock(IdentityServiceJITProvisioningHandler.class);
        direct = new DirectUserTokenProvider(facade, jit);
    }

    @Test
    public void successfulAuthorizationReturnsNormalizedUsername()
    {
        AccessTokenAuthorization authz = mock(AccessTokenAuthorization.class);
        AccessToken token = mock(AccessToken.class);
        when(authz.getAccessToken()).thenReturn(token);
        when(token.getTokenValue()).thenReturn(TOKEN_VALUE);
        when(facade.authorize(any(AuthorizationGrant.class))).thenReturn(authz);
        when(jit.extractUserInfoAndCreateUserIfNeeded(TOKEN_VALUE))
                .thenReturn(Optional.of(new OIDCUserInfo(TEST_USER, "", "", "")));

        UserToken result = direct.getUserToken(TEST_USER, TEST_PASS.clone());

        assertEquals(TEST_USER, result.getNormalizedUsername());
        assertEquals(TOKEN_VALUE, result.getTokenString());
        verify(facade, times(1)).authorize(any(AuthorizationGrant.class));
        verify(jit, times(1)).extractUserInfoAndCreateUserIfNeeded(TOKEN_VALUE);
    }

    @Test
    public void facadeAuthorizationFailurePropagates()
    {
        doThrow(new AuthorizationException("invalid grant"))
                .when(facade).authorize(any(AuthorizationGrant.class));

        assertThrows(AuthorizationException.class,
                () -> direct.getUserToken(TEST_USER, TEST_PASS.clone()));
    }

    @Test
    public void emptyJitResultRaisesAuthenticationException()
    {
        AccessTokenAuthorization authz = mock(AccessTokenAuthorization.class);
        AccessToken token = mock(AccessToken.class);
        when(authz.getAccessToken()).thenReturn(token);
        when(token.getTokenValue()).thenReturn(TOKEN_VALUE);
        when(facade.authorize(any(AuthorizationGrant.class))).thenReturn(authz);
        when(jit.extractUserInfoAndCreateUserIfNeeded(TOKEN_VALUE))
                .thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> direct.getUserToken(TEST_USER, TEST_PASS.clone()));
    }

    @Test
    public void constructorRejectsNullFacade()
    {
        assertThrows(NullPointerException.class, () -> new DirectUserTokenProvider(null, jit));
    }

    @Test
    public void constructorRejectsNullJitHandler()
    {
        assertThrows(NullPointerException.class, () -> new DirectUserTokenProvider(facade, null));
    }
}
