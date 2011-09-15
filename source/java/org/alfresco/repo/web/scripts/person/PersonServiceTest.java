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
package org.alfresco.repo.web.scripts.person;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;

/**
 * Unit test to test person Web Script API
 * 
 * @author Glen Johnson
 */
public class PersonServiceTest extends BaseWebScriptTest
{    
    private MutableAuthenticationService authenticationService;
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
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
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
    
    private JSONObject updatePerson(String userName, String title, String firstName, String lastName, 
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
        person.put("jobtitle", jobTitle);
        person.put("email", email);
        
        Response response = sendRequest(new PutRequest(URL_PEOPLE + "/" + userName, person.toString(), "application/json"), expectedStatus); 
        
        // switch back to non-admin user
        this.authenticationComponent.setCurrentUser(currentUser);
        
        return new JSONObject(response.getContentAsString());
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
        person.put("jobtitle", jobTitle);
        person.put("email", email);
        
        Response response = sendRequest(new PostRequest(URL_PEOPLE, person.toString(), "application/json"), expectedStatus); 
        
        if ((userName != null) && (userName.length() != 0))
        {
            this.createdPeople.add(userName);
        }
        
        // switch back to non-admin user
        this.authenticationComponent.setCurrentUser(currentUser);
        
        return new JSONObject(response.getContentAsString());
    }
    
    private JSONObject deletePerson(String userName, int expectedStatus)
    throws Exception
    {
        // switch to admin user to delete a person
        String currentUser = this.authenticationComponent.getCurrentUserName();
        String adminUser = this.authenticationComponent.getSystemUserName();
        this.authenticationComponent.setCurrentUser(adminUser);
        
        Response response = sendRequest(new DeleteRequest(URL_PEOPLE + "/" + userName), expectedStatus); 
        this.createdPeople.remove(userName);
        
        // switch back to non-admin user
        this.authenticationComponent.setCurrentUser(currentUser);
        
        return new JSONObject(response.getContentAsString());
    }
    
    @SuppressWarnings("unused")
    public void testGetPeople() throws Exception
    {
        // Test basic GET people with no filters ==
        Response response = sendRequest(new GetRequest(URL_PEOPLE), 200);        
    }
    
    @SuppressWarnings("unused")
    public void testGetPerson() throws Exception
    {
        // Get a person that doesn't exist
        Response response = sendRequest(new GetRequest(URL_PEOPLE + "/" + "nonExistantUser"), 404);
        
        // Create a person and get him/her
        String userName  = RandomStringUtils.randomNumeric(6);
        JSONObject result = createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                                "myJobTitle", "myEmailAddress", "myBio", "images/avatar.jpg", 200);
        response = sendRequest(new GetRequest(URL_PEOPLE + "/" + userName), 200);
    }
    
    public void testUpdatePerson() throws Exception
    {
        // Create a new person
        String userName  = RandomStringUtils.randomNumeric(6);                
        createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                                "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg",
                                Status.STATUS_OK);
        
        // Update the person's details
        JSONObject result = updatePerson(userName, "updatedTitle", "updatedFirstName", "updatedLastName",
                "updatedOrganisation", "updatedJobTitle", "updatedFN.updatedLN@email.com", "updatedBio",
                "images/updatedAvatar.jpg", Status.STATUS_OK);

        assertEquals(userName, result.get("userName"));
        assertEquals("updatedFirstName", result.get("firstName"));
        assertEquals("updatedLastName", result.get("lastName"));
        assertEquals("updatedOrganisation", result.get("organization"));
        assertEquals("updatedJobTitle", result.get("jobtitle"));
        assertEquals("updatedFN.updatedLN@email.com", result.get("email"));
    }
    
    public void testDeletePerson() throws Exception
    {
        // Create a new person
        String userName  = RandomStringUtils.randomNumeric(6);                
        createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                                "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg",
                                Status.STATUS_OK);
        
        // Delete the person
        deletePerson(userName, Status.STATUS_OK);
        
        // Make sure that the person has been deleted and no longer exists
        deletePerson(userName, Status.STATUS_NOT_FOUND);
    }
    
    public void testCreatePerson() throws Exception
    {
        String userName  = RandomStringUtils.randomNumeric(6);
                
        // Create a new person
        JSONObject result = createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                                "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg",
                                Status.STATUS_OK);        
        assertEquals(userName, result.get("userName"));
        assertEquals("myFirstName", result.get("firstName"));
        assertEquals("myLastName", result.get("lastName"));
        assertEquals("myOrganisation", result.get("organization"));
        assertEquals("myJobTitle", result.get("jobtitle"));
        assertEquals("firstName.lastName@email.com", result.get("email"));
        
        // Check for duplicate names
        createPerson(userName, "myTitle", "myFirstName", "mylastName", "myOrganisation",
                "myJobTitle", "myEmail", "myBio", "images/avatar.jpg", 409);
    }
    
    public void testCreatePersonMissingUserName() throws Exception
    {
        // Create a new person with userName == null (user name missing)
        createPerson(null, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                        "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg",
                        Status.STATUS_BAD_REQUEST);        
        
        // Create a new person with userName == "" (user name is blank)
        createPerson("", "myTitle", "myFirstName", "myLastName", "myOrganisation",
                        "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg",
                        Status.STATUS_BAD_REQUEST);        
    }
    
    public void testCreatePersonMissingFirstName() throws Exception
    {
        String userName  = RandomStringUtils.randomNumeric(6);
                
        // Create a new person with firstName == null (first name missing)
        createPerson(userName, "myTitle", null, "myLastName", "myOrganisation",
                        "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg",
                        Status.STATUS_BAD_REQUEST);        
        
        // Create a new person with firstName == "" (first name is blank)
        createPerson(userName, "myTitle", "", "myLastName", "myOrganisation",
                        "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg",
                        Status.STATUS_BAD_REQUEST);        
    }  
}
