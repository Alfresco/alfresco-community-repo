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
package org.alfresco.repo.web.scripts.site;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test to test site Web Script API
 * 
 * @author Roy Wetherall
 */
public class SiteServiceTest extends BaseWebScriptTest
{    
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    private static final String USER_ONE = "SiteTestOne";
    private static final String USER_TWO = "SiteTestTwo";
    private static final String USER_THREE = "SiteTestThree";
    
    private static final String URL_SITES = "/api/sites";
    private static final String URL_MEMBERSHIPS = "/memberships";
    
    private List<String> createdSites = new ArrayList<String>(5);
    
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
        super.tearDown();
        this.authenticationComponent.setCurrentUser("admin");
        
        // Tidy-up any site's create during the execution of the test
        for (String shortName : this.createdSites)
        {
            deleteRequest(URL_SITES + "/" + shortName, 0);
        }
        
        // Clear the list
        this.createdSites.clear();
    }
    
    public void testCreateSite() throws Exception
    {
        String shortName  = GUID.generate();
        
        // Create a new site
        JSONObject result = createSite("myPreset", shortName, "myTitle", "myDescription", true, 200);        
        assertEquals("myPreset", result.get("sitePreset"));
        assertEquals(shortName, result.get("shortName"));
        assertEquals("myTitle", result.get("title"));
        assertEquals("myDescription", result.get("description"));
        assertTrue(result.getBoolean("isPublic"));
        
        // Check for duplicate names
        createSite("myPreset", shortName, "myTitle", "myDescription", true, 500); 
    }
    
    private JSONObject createSite(String sitePreset, String shortName, String title, String description, boolean isPublic, int expectedStatus)
        throws Exception
    {
        JSONObject site = new JSONObject();
        site.put("sitePreset", sitePreset);
        site.put("shortName", shortName);
        site.put("title", title);
        site.put("description", description);
        site.put("isPublic", isPublic);                
        MockHttpServletResponse response = postRequest(URL_SITES, expectedStatus, site.toString(), "application/json"); 
        this.createdSites.add(shortName);
        return new JSONObject(response.getContentAsString());
    }
    
    public void testGetSites() throws Exception
    {
        // == Test basic GET with no filters ==
        
        MockHttpServletResponse response = getRequest(URL_SITES, 200);        
        JSONArray result = new JSONArray(response.getContentAsString());
        
        // TODO formalise this test once i can be sure that i know what's already in the site store 
        //      ie: .. i need to clean up after myself in this test 
        
        System.out.println(response.getContentAsString());
    }
    
    public void testGetSite() throws Exception
    {
        // Get a site that doesn't exist
        MockHttpServletResponse response = getRequest(URL_SITES + "/" + "somerandomshortname", 404);
        
        // Create a site and get it
        String shortName  = GUID.generate();
        JSONObject result = createSite("myPreset", shortName, "myTitle", "myDescription", true, 200);
        response = getRequest(URL_SITES + "/" + shortName, 200);
       
    }
    
    public void testUpdateSite() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        JSONObject result = createSite("myPreset", shortName, "myTitle", "myDescription", true, 200);
        
        // Update the site
        result.put("title", "abs123abc");
        result.put("description", "123abc123");
        result.put("isPublic", false);
        MockHttpServletResponse response = putRequest(URL_SITES + "/" + shortName, 200, result.toString(), "application/json");
        result = new JSONObject(response.getContentAsString());
        assertEquals("abs123abc", result.get("title"));
        assertEquals("123abc123", result.get("description"));
        assertFalse(result.getBoolean("isPublic"));
        
        // Try and get the site and double check it's changed
        response = getRequest(URL_SITES + "/" + shortName, 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals("abs123abc", result.get("title"));
        assertEquals("123abc123", result.get("description"));
        assertFalse(result.getBoolean("isPublic"));
    }
    
    public void testDeleteSite() throws Exception
    {
        // Delete non-existant site
        MockHttpServletResponse response = deleteRequest(URL_SITES + "/" + "somerandomshortname", 404);
        
        // Create a site
        String shortName  = GUID.generate();
        JSONObject result = createSite("myPreset", shortName, "myTitle", "myDescription", true, 200);
        
        // Get the site
        response = getRequest(URL_SITES + "/" + shortName, 200);
        
        // Delete the site
        response = deleteRequest(URL_SITES + "/" + shortName, 200);
        
        // Get the site
        response = getRequest(URL_SITES + "/" + shortName, 404);
    }
    
    public void testGetMemeberships() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", true, 200);
        
        // Check the memberships
        MockHttpServletResponse response = getRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, 200);
        JSONArray result = new JSONArray(response.getContentAsString());        
        assertNotNull(result);
        assertEquals(1, result.length());
        JSONObject membership = result.getJSONObject(0);
        assertEquals(SiteModel.SITE_MANAGER, membership.get("role"));
        assertEquals(USER_ONE, membership.getJSONObject("person").get("userName"));        
    }
    
    public void testPostMemberships() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", true, 200);
        
        // Build the JSON membership object
        JSONObject membership = new JSONObject();
        membership.put("role", SiteModel.SITE_CONSUMER);
        JSONObject person = new JSONObject();
        person.put("userName", USER_TWO);
        membership.put("person", person);
        
        // Post the memebership
        MockHttpServletResponse response = postRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, 200, membership.toString(), "application/json");
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // Check the result
        assertEquals(SiteModel.SITE_CONSUMER, membership.get("role"));
        assertEquals(USER_TWO, membership.getJSONObject("person").get("userName")); 
        
        // Get the membership list
        response = getRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, 200);   
        JSONArray result2 = new JSONArray(response.getContentAsString());
        assertNotNull(result2);
        assertEquals(2, result2.length());
    }
    
    public void testGetMembership() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", true, 200);
        
        // Test error conditions
        getRequest(URL_SITES + "/badsite" + URL_MEMBERSHIPS + "/" + USER_ONE, 404);
        getRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/baduser", 404);
        getRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO, 404);
        
        MockHttpServletResponse response = getRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_ONE, 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // Check the result
        assertEquals(SiteModel.SITE_MANAGER, result.get("role"));
        assertEquals(USER_ONE, result.getJSONObject("person").get("userName")); 
    }
    
    public void testPutMembership() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", true, 200);
        
        // Test error conditions
        // TODO
        
        // Build the JSON membership object
        JSONObject membership = new JSONObject();
        membership.put("role", SiteModel.SITE_CONSUMER);
        JSONObject person = new JSONObject();
        person.put("userName", USER_TWO);
        membership.put("person", person);
        
        // Post the memebership
        MockHttpServletResponse response = postRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, 200, membership.toString(), "application/json");
        JSONObject newMember = new JSONObject(response.getContentAsString());
        
        // Update the role
        newMember.put("role", SiteModel.SITE_COLLABORATOR);
        response = putRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO, 200, newMember.toString(), "application/json");
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // Check the result
        assertEquals(SiteModel.SITE_COLLABORATOR, result.get("role"));
        assertEquals(USER_TWO, result.getJSONObject("person").get("userName"));
        
        // Double check and get the membership for user two
        response = getRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO, 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(SiteModel.SITE_COLLABORATOR, result.get("role"));
        assertEquals(USER_TWO, result.getJSONObject("person").get("userName"));
    }
    
    public void testDeleteMembership() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", true, 200);
     
        // Build the JSON membership object
        JSONObject membership = new JSONObject();
        membership.put("role", SiteModel.SITE_CONSUMER);
        JSONObject person = new JSONObject();
        person.put("userName", USER_TWO);
        membership.put("person", person);
        
        // Post the membership
        postRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, 200, membership.toString(), "application/json");
        
        // Delete the membership
        deleteRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO, 200);
        
        // Check that the membership has been deleted
        getRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO, 404);
        
    }
}
