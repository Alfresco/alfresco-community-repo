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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.person;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test to test person Web Script API
 * 
 * @author Glen Johnson
 */
public class PersonServiceTest extends BaseWebScriptTest
{    
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    private static final String USER_ONE = "User.One";
    private static final String USER_TWO = "User.Two";
    private static final String USER_THREE = "User.Three";
    
    private static final String URL_PEOPLE = "/api/people";
    
    private List<String> createdPeople = new ArrayList<String>(5);
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (AuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        
        // Create users
        createUser(USER_ONE);
        createUser(USER_TWO);
        createUser(USER_THREE);
        
        // Do tests as user one
        this.authenticationComponent.setCurrentUser(USER_ONE);
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
        }        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        String adminUser = this.authenticationComponent.getSystemUserName();
        this.authenticationComponent.setCurrentUser(adminUser);
        
        /*
         * TODO: glen.johnson at alfresco dot com -
         * When DELETE /people/{userid} becomes a requirement and is subsequently implemented,
         * include this section to tidy-up people's resources created during the execution of the test
         *
         */ 
        for (String userName : this.createdPeople)
        {
            // deleteRequest(URL_PEOPLE + "/" + userName, 0);
            personService.deletePerson(userName);
        }
        
        // Clear the list
        this.createdPeople.clear();
    }
    
    public void testCreatePerson() throws Exception
    {
        String userName  = RandomStringUtils.randomNumeric(6);
                
        // Create a new site
        JSONObject result = createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                                "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg", 200);        
        assertEquals(userName, result.get("userName"));
        assertEquals("myTitle", result.get("title"));
        assertEquals("myFirstName", result.get("firstName"));
        assertEquals("myLastName", result.get("lastName"));
        assertEquals("myOrganisation", result.get("organisation"));
        assertEquals("myJobTitle", result.get("jobTitle"));
        assertEquals("firstName.lastName@email.com", result.get("email"));
        
        // Check for duplicate names
        createPerson(userName, "myTitle", "myFirstName", "mylastName", "myOrganisation",
                "myJobTitle", "myEmail", "myBio", "images/avatar.jpg", 500);
    }
    
    private JSONObject createPerson(String userName, String title, String firstName, String lastName, 
                        String organisation, String jobTitle, String email, String bio, String avatarUrl, int expectedStatus)
        throws Exception
    {
        // switch to admin user to create a person
        String currentUser = this.authenticationComponent.getCurrentUserName();
        String adminUser = this.authenticationComponent.getSystemUserName();
        this.authenticationComponent.setCurrentUser(adminUser);
        
        JSONObject person = new JSONObject();
        person.put("userName", userName);
        person.put("title", title);
        person.put("firstName", firstName);
        person.put("lastName", lastName);
        person.put("organisation", organisation);
        person.put("jobTitle", jobTitle);
        person.put("email", email);
        
        MockHttpServletResponse response = postRequest(URL_PEOPLE, expectedStatus, person.toString(), "application/json"); 
        this.createdPeople.add(userName);
        
        // switch back to non-admin user
        this.authenticationComponent.setCurrentUser(currentUser);
        
        return new JSONObject(response.getContentAsString());
    }
    
    public void testGetPeople() throws Exception
    {
        // Test basic GET people with no filters ==
        
        MockHttpServletResponse response = getRequest(URL_PEOPLE, 200);        
                
        System.out.println(response.getContentAsString());
    }
    
    public void testGetPerson() throws Exception
    {
        // Get a person that doesn't exist
        MockHttpServletResponse response = getRequest(URL_PEOPLE + "/" + "nonExistantUser", 404);
        
        // Create a person and get him/her
        String userName  = RandomStringUtils.randomNumeric(6);
        JSONObject result = createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                                "myJobTitle", "myEmailAddress", "myBio", "images/avatar.jpg", 200);
        response = getRequest(URL_PEOPLE + "/" + userName, 200);
       
    }
}
