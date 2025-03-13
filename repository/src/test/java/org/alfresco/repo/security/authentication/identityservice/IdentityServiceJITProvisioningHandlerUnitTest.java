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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;

public class IdentityServiceJITProvisioningHandlerUnitTest
{

    @Mock
    private IdentityServiceFacade identityServiceFacade;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ClientRegistration clientRegistration;

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

    @InjectMocks
    private IdentityServiceJITProvisioningHandler jitProvisioningHandler;

    private UserInfoAttrMapping expectedMapping;

    private static final String JWT_TOKEN = "myToken";
    private static final String USERNAME = "johny123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "johny123@email.com";

    @Before
    public void setup()
    {
        initMocks(this);

        when(transactionService.isReadOnly()).thenReturn(false);
        when(identityServiceFacade.decodeToken(JWT_TOKEN)).thenReturn(decodedAccessToken);
        when(personService.createMissingPeople()).thenReturn(true);
        when(identityServiceFacade.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()).thenReturn("nickname");
        when(identityServiceConfig.getEmailAttribute()).thenReturn("email");
        when(identityServiceConfig.getFirstNameAttribute()).thenReturn("given_name");
        when(identityServiceConfig.getLastNameAttribute()).thenReturn("family_name");
        expectedMapping = new UserInfoAttrMapping("nickname", "given_name", "family_name", "email");
    }

    @Test
    public void shouldExtractUserInfoForExistingUser()
    {
        when(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()).thenReturn(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME);
        when(personService.personExists(USERNAME)).thenReturn(true);
        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn(USERNAME);

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
                JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().username());
        assertFalse(result.get().allFieldsNotEmpty());
        verify(identityServiceFacade, never()).getUserInfo(JWT_TOKEN, expectedMapping);
    }

    @Test
    public void shouldExtractUserInfoForExistingUserWithProviderPrincipalAttribute()
    {
        when(identityServiceConfig.getPrincipalAttribute()).thenReturn("nickname");
        when(personService.personExists(USERNAME)).thenReturn(true);
        when(decodedAccessToken.getClaim("nickname")).thenReturn(USERNAME);

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
                JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().username());
        assertFalse(result.get().allFieldsNotEmpty());
        verify(identityServiceFacade, never()).getUserInfo(JWT_TOKEN, expectedMapping);
    }

    @Test
    public void shouldExtractUserInfoFromAccessTokenAndCreateUser()
    {
        when(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()).thenReturn(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME);

        when(personService.personExists(USERNAME)).thenReturn(false);

        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn(USERNAME);
        when(decodedAccessToken.getClaim(PersonClaims.GIVEN_NAME_CLAIM_NAME)).thenReturn(FIRST_NAME);
        when(decodedAccessToken.getClaim(PersonClaims.FAMILY_NAME_CLAIM_NAME)).thenReturn(LAST_NAME);
        when(decodedAccessToken.getClaim(PersonClaims.EMAIL_CLAIM_NAME)).thenReturn(EMAIL);

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
                JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().username());
        assertEquals(FIRST_NAME, result.get().firstName());
        assertEquals(LAST_NAME, result.get().lastName());
        assertEquals(EMAIL, result.get().email());
        assertTrue(result.get().allFieldsNotEmpty());
        verify(personService).createPerson(any());
        verify(identityServiceFacade, never()).getUserInfo(JWT_TOKEN, expectedMapping);
    }

    @Test
    public void shouldExtractUserInfoFromUserInfoEndpointAndCreateUser()
    {
        when(userInfo.username()).thenReturn(USERNAME);
        when(userInfo.firstName()).thenReturn(FIRST_NAME);
        when(userInfo.lastName()).thenReturn(LAST_NAME);
        when(userInfo.email()).thenReturn(EMAIL);

        when(personService.personExists(USERNAME)).thenReturn(false);

        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn(USERNAME);
        when(identityServiceFacade.getUserInfo(JWT_TOKEN, expectedMapping)).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
                JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().username());
        assertEquals(FIRST_NAME, result.get().firstName());
        assertEquals(LAST_NAME, result.get().lastName());
        assertEquals(EMAIL, result.get().email());
        assertTrue(result.get().allFieldsNotEmpty());
        verify(personService).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN, expectedMapping);;
    }

    @Test
    public void shouldReturnEmptyOptionalIfUsernameNotExtracted()
    {

        when(identityServiceFacade.getUserInfo(JWT_TOKEN, expectedMapping)).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
                JWT_TOKEN);

        assertFalse(result.isPresent());
        verify(personService, never()).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN, expectedMapping);;
    }

    @Test
    public void shouldCallUserInfoEndpointToGetUsername()
    {
        when(personService.personExists(USERNAME)).thenReturn(true);

        when(decodedAccessToken.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)).thenReturn("");

        when(userInfo.username()).thenReturn(USERNAME);
        when(identityServiceFacade.getUserInfo(JWT_TOKEN, expectedMapping)).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
                JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().username());
        assertEquals("", result.get().firstName());
        assertEquals("", result.get().lastName());
        assertEquals("", result.get().email());
        assertFalse(result.get().allFieldsNotEmpty());
        verify(personService, never()).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN, expectedMapping);;
    }

    @Test
    public void shouldCallUserInfoEndpointToGetUsernameWithProvidedPrincipalAttribute()
    {
        when(identityServiceConfig.getPrincipalAttribute()).thenReturn("nickname");
        when(personService.personExists(USERNAME)).thenReturn(true);

        when(decodedAccessToken.getClaim("nickname")).thenReturn("");

        when(userInfo.username()).thenReturn(USERNAME);
        when(identityServiceFacade.getUserInfo(JWT_TOKEN, expectedMapping)).thenReturn(Optional.of(userInfo));

        Optional<OIDCUserInfo> result = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
                JWT_TOKEN);

        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().username());
        assertEquals("", result.get().firstName());
        assertEquals("", result.get().lastName());
        assertEquals("", result.get().email());
        assertFalse(result.get().allFieldsNotEmpty());
        verify(personService, never()).createPerson(any());
        verify(identityServiceFacade).getUserInfo(JWT_TOKEN, expectedMapping);;
    }

    @Test
    public void shouldNotCallUserInfoEndpointIfTokenIsNullOrEmpty()
    {
        jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(null);
        jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded("");

        verify(personService, never()).createPerson(any());
        verify(identityServiceFacade, never()).decodeToken(null);
        verify(identityServiceFacade, never()).decodeToken("");
        verify(identityServiceFacade, never()).getUserInfo(null, expectedMapping);
        verify(identityServiceFacade, never()).getUserInfo(null, expectedMapping);
    }

}
