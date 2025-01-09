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

package org.alfresco.repo.security.authentication.identityservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;

import com.nimbusds.openid.connect.sdk.claims.PersonClaims;

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
    private IdentityServiceConfig identityServiceConfig;

    @Mock
    private OIDCUserInfo userInfo;

    private IdentityServiceJITProvisioningHandler jitProvisioningHandler;

    private static final String JWT_TOKEN = "myToken";

    @Before
    public void setup()
    {
        initMocks(this);

        when(transactionService.isReadOnly()).thenReturn(false);
        when(identityServiceFacade.decodeToken(JWT_TOKEN)).thenReturn(decodedAccessToken);
        when(personService.createMissingPeople()).thenReturn(true);
        jitProvisioningHandler = new IdentityServiceJITProvisioningHandler(identityServiceFacade,
            personService, transactionService, identityServiceConfig);
    }

    @Test
    public void shouldExtractUserInfoForExistingUser()
    {
        when(personService.personExists("johny123")).thenReturn(true);
        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn("johny123");

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertFalse(result.get().allFieldsNotEmpty());
        verify(identityServiceFacade, never()).getUserInfo(JWT_TOKEN, PersonClaims.PREFERRED_USERNAME_CLAIM_NAME);
    }

    @Test
    public void shouldExtractUserInfoForExistingUserWithProviderPrincipalAttribute()
    {
        when(identityServiceConfig.getPrincipalAttribute()).thenReturn("nickname");
        when(personService.personExists("johny123")).thenReturn(true);
        when(decodedAccessToken.getClaim("nickname")).thenReturn("johny123");

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertFalse(result.get().allFieldsNotEmpty());
        verify(identityServiceFacade, never()).getUserInfo(JWT_TOKEN, "nickname");
    }

    @Test
    public void shouldExtractUserInfoFromAccessTokenAndCreateUser()
    {
        when(personService.personExists("johny123")).thenReturn(false);

        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn("johny123");
        when(decodedAccessToken.getClaim(PersonClaims.GIVEN_NAME_CLAIM_NAME)).thenReturn("John");
        when(decodedAccessToken.getClaim(PersonClaims.FAMILY_NAME_CLAIM_NAME)).thenReturn("Doe");
        when(decodedAccessToken.getClaim(PersonClaims.EMAIL_CLAIM_NAME)).thenReturn("johny123@email.com");

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertEquals("John", result.get().firstName());
        assertEquals("Doe", result.get().lastName());
        assertEquals("johny123@email.com", result.get().email());
        assertTrue(result.get().allFieldsNotEmpty());
        verify(personService).createPerson(any());
        verify(identityServiceFacade, never()).getUserInfo(JWT_TOKEN, PersonClaims.PREFERRED_USERNAME_CLAIM_NAME);
    }

    @Test
    public void shouldExtractUserInfoFromUserInfoEndpointAndCreateUser()
    {
        when(userInfo.username()).thenReturn("johny123");
        when(userInfo.firstName()).thenReturn("John");
        when(userInfo.lastName()).thenReturn("Doe");
        when(userInfo.email()).thenReturn("johny123@email.com");

        when(personService.personExists("johny123")).thenReturn(false);

        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn("johny123");
        when(identityServiceFacade.getUserInfo(JWT_TOKEN, PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertEquals("John", result.get().firstName());
        assertEquals("Doe", result.get().lastName());
        assertEquals("johny123@email.com", result.get().email());
        assertTrue(result.get().allFieldsNotEmpty());
        verify(personService).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN, PersonClaims.PREFERRED_USERNAME_CLAIM_NAME);
    }

    @Test
    public void shouldReturnEmptyOptionalIfUsernameNotExtracted()
    {

        when(identityServiceFacade.getUserInfo(JWT_TOKEN, PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertFalse(result.isPresent());
        verify(personService, never()).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN, PersonClaims.PREFERRED_USERNAME_CLAIM_NAME);
    }

    @Test
    public void shouldCallUserInfoEndpointToGetUsername()
    {
        when(personService.personExists("johny123")).thenReturn(true);

        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn("");

        when(userInfo.username()).thenReturn("johny123");
        when(identityServiceFacade.getUserInfo(JWT_TOKEN, PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertEquals("", result.get().firstName());
        assertEquals("", result.get().lastName());
        assertEquals("", result.get().email());
        assertFalse(result.get().allFieldsNotEmpty());
        verify(personService, never()).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN, PersonClaims.PREFERRED_USERNAME_CLAIM_NAME);
    }

    @Test
    public void shouldCallUserInfoEndpointToGetUsernameWithProvidedPrincipalAttribute()
    {
        when(identityServiceConfig.getPrincipalAttribute()).thenReturn("nickname");
        when(personService.personExists("johny123")).thenReturn(true);

        when(decodedAccessToken.getClaim("nickname")).thenReturn("");

        when(userInfo.username()).thenReturn("johny123");
        when(identityServiceFacade.getUserInfo(JWT_TOKEN, "nickname")).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
            JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals("johny123", result.get().username());
        assertEquals("", result.get().firstName());
        assertEquals("", result.get().lastName());
        assertEquals("", result.get().email());
        assertFalse(result.get().allFieldsNotEmpty());
        verify(personService, never()).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN, "nickname");
    }

    @Test
    public void shouldNotCallUserInfoEndpointIfTokenIsNullOrEmpty()
    {
        jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(null);
        jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded("");

        verify(personService, never()).createPerson(any());
        verify(identityServiceFacade, never()).decodeToken(null);
        verify(identityServiceFacade, never()).decodeToken("");
        verify(identityServiceFacade, never()).getUserInfo(null, PersonClaims.PREFERRED_USERNAME_CLAIM_NAME);
        verify(identityServiceFacade, never()).getUserInfo("", PersonClaims.PREFERRED_USERNAME_CLAIM_NAME);
    }

}