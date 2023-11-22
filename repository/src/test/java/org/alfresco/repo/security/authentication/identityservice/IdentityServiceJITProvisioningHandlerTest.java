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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.DefaultChildApplicationContextManager;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.BaseSpringTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

public class IdentityServiceJITProvisioningHandlerTest extends BaseSpringTest
{
    private PersonService personService;
    private NodeService nodeService;
    private IdentityServiceFacade identityServiceFacade;
    private IdentityServiceJITProvisioningHandler identityServiceJITProvisioningHandler;
    private ChildApplicationContextFactory childApplicationContextFactory;
    private final String idsUsername = "johndoe123";
    @Before
    public void setup()
    {
        personService = (PersonService) applicationContext.getBean("personService");
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        DefaultChildApplicationContextManager childApplicationContextManager = (DefaultChildApplicationContextManager) applicationContext
                    .getBean("Authentication");
        childApplicationContextFactory = childApplicationContextManager.getChildApplicationContextFactory("identity-service1");

        identityServiceFacade = (IdentityServiceFacade) childApplicationContextFactory.getApplicationContext().getBean("identityServiceFacade");
        identityServiceJITProvisioningHandler = (IdentityServiceJITProvisioningHandler) childApplicationContextFactory.getApplicationContext().getBean("jitProvisioningHandler");
        IdentityServiceConfig identityServiceConfig = (IdentityServiceConfig) childApplicationContextFactory.getApplicationContext().getBean("identityServiceConfig");
        identityServiceConfig.setAllowAnyHostname(true);
        identityServiceConfig.setClientKeystore(null);
        identityServiceConfig.setDisableTrustManager(true);
    }

    @Test
    public void shouldCreateUser()
    {
        assertFalse(personService.personExists(idsUsername));

        IdentityServiceFacade.AccessTokenAuthorization accessTokenAuthorization =
                    identityServiceFacade.authorize(IdentityServiceFacade.AuthorizationGrant.password(idsUsername, "password"));

        Optional<OIDCUserInfo> userInfoOptional = identityServiceJITProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(accessTokenAuthorization.getAccessToken().getTokenValue());

        NodeRef person = personService.getPerson(idsUsername);

        assertTrue(userInfoOptional.isPresent());
        assertEquals(idsUsername, userInfoOptional.get().username());
        assertEquals("John", userInfoOptional.get().firstName());
        assertEquals("Doe", userInfoOptional.get().lastName());
        assertEquals("johndoe@test.com", userInfoOptional.get().email());
        assertEquals(idsUsername, nodeService.getProperty(person, ContentModel.PROP_USERNAME));
        assertEquals("John", nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME));
        assertEquals("Doe", nodeService.getProperty(person, ContentModel.PROP_LASTNAME));
        assertEquals("johndoe@test.com", nodeService.getProperty(person, ContentModel.PROP_EMAIL));
    }

}
