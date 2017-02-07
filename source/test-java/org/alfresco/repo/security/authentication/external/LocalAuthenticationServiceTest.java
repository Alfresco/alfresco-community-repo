/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.external;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.DefaultChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * The test designed for local {@link org.alfresco.service.cmr.security.AuthenticationService}
 * declared in external authentication context file
 *
 * @author alex.mukha
 */
public class LocalAuthenticationServiceTest
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private DefaultChildApplicationContextManager childApplicationContextManager;
    private PersonService personService;
    private AuthenticationService localAuthenticationService;

    @Before
    public void before()
    {
        childApplicationContextManager = (DefaultChildApplicationContextManager) ctx.getBean("Authentication");
        personService = (PersonService) ctx.getBean("PersonService");
        childApplicationContextManager.stop();
        childApplicationContextManager.setProperty("chain", "external1:external");
        ChildApplicationContextFactory childApplicationContextFactory = childApplicationContextManager.getChildApplicationContextFactory("external1");
        // Clear the proxy user name
        childApplicationContextFactory.stop();
        childApplicationContextFactory.setProperty("external.authentication.proxyUserName", "");
        localAuthenticationService = (AuthenticationService) childApplicationContextFactory.getApplicationContext()
                .getBean("localAuthenticationService");
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @After
    public void after()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        childApplicationContextManager.destroy();
        childApplicationContextManager = null;
    }

    private String createPerson()
    {
        Map<QName, Serializable> properties = new HashMap<>();
        String username = "user" + GUID.generate();
        properties.put(ContentModel.PROP_USERNAME, username);
        properties.put(ContentModel.PROP_FIRSTNAME, username);
        properties.put(ContentModel.PROP_LASTNAME, username);
        personService.createPerson(properties);
        return username;
    }

    @Test
    public void testIsEnabledFlag()
    {
        String username = createPerson();
        assertTrue("The isEnabed flag should be set to true for the enabled user",
                localAuthenticationService.getAuthenticationEnabled(username));
        // disable person
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_ENABLED, false);
        personService.setPersonProperties(username, properties);
        assertFalse("The isEnabed flag should be set to false for the disabled user",
                localAuthenticationService.getAuthenticationEnabled(username));
    }
}
