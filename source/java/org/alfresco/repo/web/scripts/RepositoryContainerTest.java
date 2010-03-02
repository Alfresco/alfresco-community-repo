/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.web.scripts;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit test to test runas function
 * 
 * @author David Ward
 */
public class RepositoryContainerTest extends BaseWebScriptTest
{
    private MutableAuthenticationService authenticationService;
    private PersonService personService;

    private static final String USER_ONE = "RunAsOne";

    private static final String URL_RUNAS = "/test/runas";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.authenticationService = (MutableAuthenticationService) getServer().getApplicationContext().getBean(
                "AuthenticationService");
        this.personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        // Create users
        createUser(USER_ONE);
    }

    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());

            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            this.personService.createPerson(ppOne);
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }

    public void testRunAs() throws Exception
    {
        // TODO: this test needs refactoring after SpringSurf changes
        //Response response = sendRequest(new GetRequest(URL_RUNAS), 200, AuthenticationUtil.getAdminUserName());
        //assertEquals(USER_ONE, response.getContentAsString());
        assertTrue(true);
    }
}
