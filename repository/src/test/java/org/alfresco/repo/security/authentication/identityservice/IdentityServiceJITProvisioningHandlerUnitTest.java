/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;

import com.nimbusds.openid.connect.sdk.claims.PersonClaims;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class IdentityServiceJITProvisioningHandlerUnitTest
{

    @Mock
    private IdentityServiceFacade identityServiceFacade;

    @Mock
    private PersonService personService;

    @Mock
    private IdentityServiceFacade.DecodedAccessToken decodedAccessToken;

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserInfo userInfo;

    private IdentityServiceJITProvisioningHandler identityServiceJITProvisioningHandler;

    private final String JWT_TOKEN = "myToken";

    @Before
    public void setup()
    {
        initMocks(this);

        when(transactionService.isReadOnly()).thenReturn(false);
        when(identityServiceFacade.decodeToken(JWT_TOKEN)).thenReturn(decodedAccessToken);
        when(personService.createMissingPeople()).thenReturn(true);
        identityServiceJITProvisioningHandler = new IdentityServiceJITProvisioningHandler(identityServiceFacade,
            personService, transactionService);
    }

    @Test
    public void shouldExtractUserInfoForExistingUser()
    {
        when(personService.personExists("johny123")).thenReturn(true);
        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn("johny123");

        Optional<OIDCUserInfo> result = identityServiceJITProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertFalse(result.get().allFieldsNotEmpty());
        verify(identityServiceFacade, times(0)).getUserInfo(JWT_TOKEN);
    }

    @Test
    public void shouldExtractUserInfoFromAccessTokenAndCreateUser()
    {
        when(personService.personExists("johny123")).thenReturn(false);

        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn("johny123");
        when(decodedAccessToken.getClaim(PersonClaims.GIVEN_NAME_CLAIM_NAME)).thenReturn("John");
        when(decodedAccessToken.getClaim(PersonClaims.FAMILY_NAME_CLAIM_NAME)).thenReturn("Doe");
        when(decodedAccessToken.getClaim(PersonClaims.EMAIL_CLAIM_NAME)).thenReturn("johny123@email.com");

        Optional<OIDCUserInfo> result = identityServiceJITProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertEquals("John", result.get().firstName());
        assertEquals("Doe", result.get().lastName());
        assertEquals("johny123@email.com", result.get().email());
        assertTrue(result.get().allFieldsNotEmpty());
        verify(personService).createPerson(any());
        verify(identityServiceFacade, times(0)).getUserInfo(JWT_TOKEN);
    }

    @Test
    public void shouldExtractUserInfoFromUserInfoEndpointAndCreateUser()
    {
        when(userInfo.getPreferredUsername()).thenReturn("johny123");
        when(userInfo.getGivenName()).thenReturn("John");
        when(userInfo.getFamilyName()).thenReturn("Doe");
        when(userInfo.getEmailAddress()).thenReturn("johny123@email.com");

        when(personService.personExists("johny123")).thenReturn(false);

        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn("johny123");
        when(identityServiceFacade.getUserInfo(JWT_TOKEN)).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = identityServiceJITProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertEquals("John", result.get().firstName());
        assertEquals("Doe", result.get().lastName());
        assertEquals("johny123@email.com", result.get().email());
        assertTrue(result.get().allFieldsNotEmpty());
        verify(personService).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN);
    }

    @Test
    public void shouldReturnEmptyOptionalIfUsernameNotExtracted()
    {

        when(identityServiceFacade.getUserInfo(JWT_TOKEN)).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = identityServiceJITProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertFalse(result.isPresent());
        verify(personService, times(0)).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN);
    }

    @Test
    public void shouldCallUserInfoEndpointToGetUsername()
    {
        when(personService.personExists("johny123")).thenReturn(true);

        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn("");

        when(userInfo.getPreferredUsername()).thenReturn("johny123");
        when(identityServiceFacade.getUserInfo(JWT_TOKEN)).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = identityServiceJITProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertEquals("", result.get().firstName());
        assertEquals("", result.get().lastName());
        assertEquals("", result.get().email());
        assertFalse(result.get().allFieldsNotEmpty());
        verify(personService, times(0)).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN);
    }

}
