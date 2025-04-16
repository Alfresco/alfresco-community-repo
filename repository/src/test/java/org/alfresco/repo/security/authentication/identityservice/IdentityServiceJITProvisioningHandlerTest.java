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

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Optional;

import com.nimbusds.openid.connect.sdk.claims.PersonClaims;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.DefaultChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.identityservice.user.OIDCUserInfo;
import org.alfresco.repo.security.authentication.identityservice.user.UserInfoAttrMapping;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;

@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class IdentityServiceJITProvisioningHandlerTest extends BaseSpringTest
{
    private static final String IDS_USERNAME = "johndoe123";

    private PersonService personService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private IdentityServiceFacade identityServiceFacade;
    private IdentityServiceJITProvisioningHandler jitProvisioningHandler;

    private final boolean isAuth0Enabled = Optional.ofNullable(System.getProperty("auth0.enabled"))
            .map(Boolean::valueOf)
            .orElse(false);

    private final String userPassword = Optional.ofNullable(System.getProperty("admin.password"))
            .filter(password -> isAuth0Enabled)
            .orElse("password");

    @Before
    public void setup()
    {
        personService = (PersonService) applicationContext.getBean("personService");
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        transactionService = (TransactionService) applicationContext.getBean("transactionService");
        DefaultChildApplicationContextManager childApplicationContextManager = (DefaultChildApplicationContextManager) applicationContext
                .getBean("Authentication");
        ChildApplicationContextFactory childApplicationContextFactory = childApplicationContextManager.getChildApplicationContextFactory(
                "identity-service1");

        identityServiceFacade = (IdentityServiceFacade) childApplicationContextFactory.getApplicationContext()
                .getBean("identityServiceFacade");
        jitProvisioningHandler = (IdentityServiceJITProvisioningHandler) childApplicationContextFactory.getApplicationContext()
                .getBean("jitProvisioningHandler");
        IdentityServiceConfig identityServiceConfig = (IdentityServiceConfig) childApplicationContextFactory.getApplicationContext()
                .getBean("identityServiceConfig");
        identityServiceConfig.setAllowAnyHostname(true);
        identityServiceConfig.setClientKeystore(null);
        identityServiceConfig.setDisableTrustManager(true);
    }

    @Test
    public void shouldCreateNonExistingUserInRepo()
    {
        assertFalse(personService.personExists(IDS_USERNAME));

        IdentityServiceFacade.AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade.authorize(
                IdentityServiceFacade.AuthorizationGrant.password(IDS_USERNAME, userPassword));

        Optional<OIDCUserInfo> userInfoOptional = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
                accessTokenAuthorization.getAccessToken().getTokenValue());

        NodeRef person = personService.getPerson(IDS_USERNAME);

        assertTrue(userInfoOptional.isPresent());
        assertEquals(IDS_USERNAME, userInfoOptional.get().username());
        assertEquals("johndoe123@alfresco.com", userInfoOptional.get().email());
        assertEquals(IDS_USERNAME, nodeService.getProperty(person, ContentModel.PROP_USERNAME));
        assertEquals("johndoe123@alfresco.com", nodeService.getProperty(person, ContentModel.PROP_EMAIL));

        if (!isAuth0Enabled)
        {
            assertEquals("John", userInfoOptional.get().firstName());
            assertEquals("Doe", userInfoOptional.get().lastName());
            assertEquals("John", nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME));
            assertEquals("Doe", nodeService.getProperty(person, ContentModel.PROP_LASTNAME));
        }
    }

    @Test
    public void shouldCallUserInfoEndpointAndCreateUser() throws IllegalAccessException, NoSuchFieldException
    {
        assertFalse(personService.personExists(IDS_USERNAME));

        String principalAttribute = isAuth0Enabled ? PersonClaims.NICKNAME_CLAIM_NAME : PersonClaims.PREFERRED_USERNAME_CLAIM_NAME;
        IdentityServiceFacade.AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade.authorize(
                IdentityServiceFacade.AuthorizationGrant.password(IDS_USERNAME, userPassword));
        UserInfoAttrMapping userInfoAttrMapping = new UserInfoAttrMapping(principalAttribute, "given_name", "family_name", "email");

        String accessToken = accessTokenAuthorization.getAccessToken().getTokenValue();
        ClientRegistration clientRegistration = mock(ClientRegistration.class, RETURNS_DEEP_STUBS);
        when(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()).thenReturn(principalAttribute);
        IdentityServiceFacade idsServiceFacadeMock = mock(IdentityServiceFacade.class);
        when(idsServiceFacadeMock.decodeToken(accessToken)).thenReturn(null);
        when(idsServiceFacadeMock.getUserInfo(accessToken, userInfoAttrMapping)).thenReturn(identityServiceFacade.getUserInfo(accessToken, userInfoAttrMapping));
        when(idsServiceFacadeMock.getClientRegistration()).thenReturn(clientRegistration);

        // Replace the original facade with a mocked one to prevent user information from being extracted from the access token.
        Field declaredField = jitProvisioningHandler.getClass()
                .getDeclaredField("identityServiceFacade");
        declaredField.setAccessible(true);
        declaredField.set(jitProvisioningHandler, idsServiceFacadeMock);

        Optional<OIDCUserInfo> userInfoOptional = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(
                accessToken);

        declaredField.set(jitProvisioningHandler, identityServiceFacade);

        NodeRef person = personService.getPerson(IDS_USERNAME);

        assertTrue(userInfoOptional.isPresent());
        assertEquals(IDS_USERNAME, userInfoOptional.get().username());
        assertEquals(IDS_USERNAME, nodeService.getProperty(person, ContentModel.PROP_USERNAME));
        assertEquals("johndoe123@alfresco.com", userInfoOptional.get().email());
        assertEquals("johndoe123@alfresco.com", nodeService.getProperty(person, ContentModel.PROP_EMAIL));
        verify(idsServiceFacadeMock).decodeToken(accessToken);
        verify(idsServiceFacadeMock, atLeast(1)).getUserInfo(accessToken, userInfoAttrMapping);
        if (!isAuth0Enabled)
        {
            assertEquals("John", userInfoOptional.get().firstName());
            assertEquals("Doe", userInfoOptional.get().lastName());
            assertEquals("John", nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME));
            assertEquals("Doe", nodeService.getProperty(person, ContentModel.PROP_LASTNAME));
        }
    }

    @After
    public void tearDown()
    {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                transactionService.getRetryingTransactionHelper()
                        .doInTransaction((RetryingTransactionCallback<Void>) () -> {
                            personService.deletePerson(IDS_USERNAME);
                            return null;
                        });
                return null;
            }
        });
    }
}
