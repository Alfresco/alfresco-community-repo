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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.alfresco.service.cmr.security.PersonService;

public class TokenUserToOIDCUserMapperUnitTest
{

    @Mock
    private PersonService personService;

    @InjectMocks
    private TokenUserToOIDCUserMapper tokenUserToOIDCUserMapper;

    @Before
    public void setup()
    {
        initMocks(this);
    }

    @Test
    public void shouldMapToOIDCUserWithAllFieldsPopulated()
    {
        DecodedTokenUser decodedTokenUser = new DecodedTokenUser("JOHNY123", "John", "Doe", "johny123@email.com");
        when(personService.getUserIdentifier("JOHNY123")).thenReturn("johny123");

        OIDCUserInfo oidcUserInfo = tokenUserToOIDCUserMapper.toOIDCUser(decodedTokenUser);

        assertEquals("johny123", oidcUserInfo.username());
        assertEquals("John", oidcUserInfo.firstName());
        assertEquals("Doe", oidcUserInfo.lastName());
        assertEquals("johny123@email.com", oidcUserInfo.email());
    }

    @Test
    public void shouldMapToOIDCUserWithSomeFieldsEmpty()
    {
        DecodedTokenUser decodedTokenUser = new DecodedTokenUser("johny123", "", "Doe", "");
        when(personService.getUserIdentifier("johny123")).thenReturn("johny123");

        OIDCUserInfo oidcUserInfo = tokenUserToOIDCUserMapper.toOIDCUser(decodedTokenUser);

        assertEquals("johny123", oidcUserInfo.username());
        assertEquals("", oidcUserInfo.firstName());
        assertEquals("Doe", oidcUserInfo.lastName());
        assertEquals("", oidcUserInfo.email());
    }

    @Test
    public void shouldReturnNullForNullUsername()
    {
        DecodedTokenUser decodedTokenUser = new DecodedTokenUser(null, "John", "Doe", "johny123@email.com");

        OIDCUserInfo oidcUserInfo = tokenUserToOIDCUserMapper.toOIDCUser(decodedTokenUser);

        assertNull(oidcUserInfo.username());
        assertEquals("John", oidcUserInfo.firstName());
        assertEquals("Doe", oidcUserInfo.lastName());
        assertEquals("johny123@email.com", oidcUserInfo.email());
    }
}
