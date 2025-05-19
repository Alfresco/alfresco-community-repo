/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade;

public class AccessTokenToDecodedTokenUserMapperUnitTest
{

    @Mock
    private IdentityServiceFacade.DecodedAccessToken decodedAccessToken;

    private AccessTokenToDecodedTokenUserMapper tokenToDecodedTokenUserMapper;

    public static final String USERNAME_CLAIM = "nickname";
    public static final String EMAIL_CLAIM = "email";
    public static final String FIRST_NAME_CLAIM = "given_name";
    public static final String LAST_NAME_CLAIM = "family_name";

    @Before
    public void setup()
    {
        initMocks(this);
        UserInfoAttrMapping userInfoAttrMapping = new UserInfoAttrMapping(USERNAME_CLAIM, FIRST_NAME_CLAIM, LAST_NAME_CLAIM, EMAIL_CLAIM);
        tokenToDecodedTokenUserMapper = new AccessTokenToDecodedTokenUserMapper(userInfoAttrMapping);
    }

    @Test
    public void shouldMapToDecodedTokenUserWithAllFieldsPopulated()
    {
        when(decodedAccessToken.getClaim(USERNAME_CLAIM)).thenReturn("johny123");
        when(decodedAccessToken.getClaim(FIRST_NAME_CLAIM)).thenReturn("John");
        when(decodedAccessToken.getClaim(LAST_NAME_CLAIM)).thenReturn("Doe");
        when(decodedAccessToken.getClaim(EMAIL_CLAIM)).thenReturn("johny123@email.com");

        Optional<DecodedTokenUser> result = tokenToDecodedTokenUserMapper.toDecodedTokenUser(decodedAccessToken);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertEquals("John", result.get().firstName());
        assertEquals("Doe", result.get().lastName());
        assertEquals("johny123@email.com", result.get().email());
    }

    @Test
    public void shouldMapToDecodedTokenUserWithSomeFieldsEmpty()
    {
        when(decodedAccessToken.getClaim(USERNAME_CLAIM)).thenReturn("johny123");
        when(decodedAccessToken.getClaim(FIRST_NAME_CLAIM)).thenReturn("");
        when(decodedAccessToken.getClaim(LAST_NAME_CLAIM)).thenReturn("Doe");
        when(decodedAccessToken.getClaim(EMAIL_CLAIM)).thenReturn("");

        Optional<DecodedTokenUser> result = tokenToDecodedTokenUserMapper.toDecodedTokenUser(decodedAccessToken);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertEquals("", result.get().firstName());
        assertEquals("Doe", result.get().lastName());
        assertEquals("", result.get().email());
    }

    @Test
    public void shouldReturnEmptyOptionalForNullUsername()
    {
        when(decodedAccessToken.getClaim(USERNAME_CLAIM)).thenReturn(null);
        when(decodedAccessToken.getClaim(FIRST_NAME_CLAIM)).thenReturn("John");
        when(decodedAccessToken.getClaim(LAST_NAME_CLAIM)).thenReturn("Doe");
        when(decodedAccessToken.getClaim(EMAIL_CLAIM)).thenReturn("johny123@email.com");

        Optional<DecodedTokenUser> result = tokenToDecodedTokenUserMapper.toDecodedTokenUser(decodedAccessToken);

        assertFalse(result.isPresent());
    }
}
