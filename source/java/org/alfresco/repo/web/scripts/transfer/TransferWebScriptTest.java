/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.transfer;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;

/**
 * Test the transfer web script API
 * 
 * @author brian
 */
public class TransferWebScriptTest extends BaseWebScriptTest
{    
    private static final String USERNAME = "noddy.transfer";

    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private List<String> createdPeople = new ArrayList<String>(5);
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();       
 
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // Create users
        createUser(USERNAME);
    }    

    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "password".toCharArray());
            
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "myFirstName");
            personProps.put(ContentModel.PROP_LASTNAME, "myLastName");
            personProps.put(ContentModel.PROP_EMAIL, "myFirstName.myLastName@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "myJobTitle");
            personProps.put(ContentModel.PROP_JOBTITLE, "myOrganisation");
            
            this.personService.createPerson(personProps);
            
            this.createdPeople.add(userName);
        }        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        String adminUser = this.authenticationComponent.getSystemUserName();
        this.authenticationComponent.setCurrentUser(adminUser);
        
        for (String userName : this.createdPeople)
        {
            personService.deletePerson(userName);
        }
        // Clear the list
        this.createdPeople.clear();
    }
    
    
    public void testVerify() throws Exception
    {
        String url = "/api/transfer/test";
        PostRequest req = new PostRequest(url, new JSONObject().toString(), "application/json");
        
        //First, we'll try the request as a simple, non-admin user (expect a 401)
        AuthenticationUtil.setFullyAuthenticatedUser(USERNAME);
        sendRequest(req, 401);
        
        //Then we'll have a go as the system user (expect a 200)
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);
        sendRequest(req, 200);
        
        //Then we'll disable the transfer receiver and try again as the system user (expect a 404)
        TransferWebScript webscript = (TransferWebScript)getServer().getApplicationContext().getBean("webscript.org.alfresco.repository.transfer.transfer.post");
        webscript.setEnabled(false);
        sendRequest(req, 404);
    }
    
}
