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
package org.alfresco.repo.web.scripts.site;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit test to test site Web Script API of the Site Object.
 * 
 * @author Roy Wetherall
 */
public class SiteServiceTest extends BaseWebScriptTest
{    
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private SiteService siteService;
    private NodeService nodeService;
    private AuthorityService authorityService;
    
    private static final String USER_ONE = "SiteTestOne";
    private static final String USER_TWO = "SiteTestTwo";
    private static final String USER_THREE = "SiteTestThree";
    
    private static final String URL_SITES = "/api/sites";
    private static final String URL_SITES_QUERY = URL_SITES + "/query";
    private static final String URL_MEMBERSHIPS = "/memberships";
    
    private List<String> createdSites = new ArrayList<String>(5);
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.authorityService = (AuthorityService)getServer().getApplicationContext().getBean("AuthorityService");
        
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
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Tidy-up any site's create during the execution of the test
        for (String shortName : this.createdSites)
        {
            sendRequest(new DeleteRequest(URL_SITES + "/" + shortName), 0);
        }
        
        // Clear the list
        this.createdSites.clear();
    }
    
    public void testCreateSite() throws Exception
    {
        String shortName  = GUID.generate();
        
        // Create a new site
        JSONObject result = createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);        
        assertEquals("myPreset", result.get("sitePreset"));
        assertEquals(shortName, result.get("shortName"));
        assertEquals("myTitle", result.get("title"));
        assertEquals("myDescription", result.get("description"));
        assertNotNull(result.get("node"));
        assertNotNull(result.get("tagScope"));
        assertEquals(SiteVisibility.PUBLIC.toString(), result.get("visibility"));
        assertTrue(result.getBoolean("isPublic"));
        
        // Check for duplicate names
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 500); 
    }
    
    private JSONObject createSite(String sitePreset, String shortName, String title, String description, SiteVisibility visibility, int expectedStatus)
        throws Exception
    {
        JSONObject site = new JSONObject();
        site.put("sitePreset", sitePreset);
        site.put("shortName", shortName);
        site.put("title", title);
        site.put("description", description);
        site.put("visibility", visibility.toString());                
        Response response = sendRequest(new PostRequest(URL_SITES, site.toString(), "application/json"), expectedStatus); 
        this.createdSites.add(shortName);
        return new JSONObject(response.getContentAsString());
    }
    
    public void testGetSites() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_SITES), 200);        
        JSONArray result = new JSONArray(response.getContentAsString());        
        assertNotNull(result);
        assertEquals("Sites exist prior to running test", 0, result.length());
        
        createSite("myPreset", GUID.generate(), "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        createSite("myPreset", GUID.generate(), "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        createSite("myPreset", GUID.generate(), "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        createSite("myPreset", GUID.generate(), "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        createSite("myPreset", GUID.generate(), "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        response = sendRequest(new GetRequest(URL_SITES), 200);        
        result = new JSONArray(response.getContentAsString());        
        assertNotNull(result);
        assertEquals(5, result.length());
        
        response = sendRequest(new GetRequest(URL_SITES + "?size=3"), 200);        
        result = new JSONArray(response.getContentAsString());        
        assertNotNull(result);
        assertEquals(3, result.length());        

        response = sendRequest(new GetRequest(URL_SITES + "?size=13"), 200);        
        result = new JSONArray(response.getContentAsString());        
        assertNotNull(result);
        assertEquals(5, result.length());
    }
    
    /**
     * https://issues.alfresco.com/jira/browse/JAWS-456
     */
    public void testQuerySites() throws Exception
    {
        // Generate the short names of the sites
        String[] shortNames = new String[]{GUID.generate(), GUID.generate(), GUID.generate(), GUID.generate(), GUID.generate()};
        
        // Create the sites
        createSite("myPreset", shortNames[0], "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        createSite("myPreset", shortNames[1], "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        createSite("myPreset", shortNames[2], "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        createSite("myPreset", shortNames[3], "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        createSite("myPreset", shortNames[4], "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        // build query json
        JSONObject shortNameQuery = new JSONObject();
        shortNameQuery.put("match", "exact");
        JSONArray valuesArray = new JSONArray();
        valuesArray.put(0, shortNames[0]);
        valuesArray.put(1, shortNames[2]);
        valuesArray.put(2, shortNames[4]);
        valuesArray.put(3, "bobbins");
        shortNameQuery.put("values", valuesArray);
        JSONObject query = new JSONObject();
        query.put("shortName", shortNameQuery);
        
        // execute site query
        Response response = sendRequest(new PostRequest(URL_SITES_QUERY, query.toString(), "application/json"), 200);
        JSONArray result = new JSONArray(response.getContentAsString());
        
        // check we have the results we expect
        assertEquals(3, result.length());
        Set<String> resultSet = new HashSet<String>();
        for (int i=0; i<result.length(); i++)
        {
           resultSet.add((String)result.getJSONObject(i).get("shortName"));
        }
        assertTrue(resultSet.contains(shortNames[0]));
        assertFalse(resultSet.contains(shortNames[1]));
        assertTrue(resultSet.contains(shortNames[2]));
        assertFalse(resultSet.contains(shortNames[3]));
        assertTrue(resultSet.contains(shortNames[4]));
        assertFalse(resultSet.contains("bobbins"));
        
        // sample one of the returned sites and check it's what we expect
        JSONObject site = result.getJSONObject(0);
        assertNotNull(site);
        assertEquals("myPreset", site.get("sitePreset"));
        assertEquals("myTitle", site.get("title"));
        assertEquals("myDescription", site.get("description"));
        assertNotNull(site.get("node"));
        assertNotNull(site.get("tagScope"));
        assertTrue(site.getBoolean("isPublic"));
    }
    
    public void testGetSite() throws Exception
    {
        // Get a site that doesn't exist
        sendRequest(new GetRequest(URL_SITES + "/" + "somerandomshortname"), 404);
        
        // Create a site and get it
        String shortName  = GUID.generate();
        JSONObject result = createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName), 200);
       
    }
    
    public void testUpdateSite() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        JSONObject result = createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        // Update the site
        result.put("title", "abs123abc");
        result.put("description", "123abc123");
        result.put("visibility", SiteVisibility.PRIVATE.toString());
        Response response = sendRequest(new PutRequest(URL_SITES + "/" + shortName, result.toString(), "application/json"), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals("abs123abc", result.get("title"));
        assertEquals("123abc123", result.get("description"));
        assertFalse(result.getBoolean("isPublic"));
        assertEquals(SiteVisibility.PRIVATE.toString(), result.get("visibility"));
        
        // Try and get the site and double check it's changed
        response = sendRequest(new GetRequest(URL_SITES + "/" + shortName), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals("abs123abc", result.get("title"));
        assertEquals("123abc123", result.get("description"));
        assertFalse(result.getBoolean("isPublic"));
        assertEquals(SiteVisibility.PRIVATE.toString(), result.get("visibility"));
    }
    
    public void testDeleteSite() throws Exception
    {
        // Delete non-existent site
        Response response = sendRequest(new DeleteRequest(URL_SITES + "/" + "somerandomshortname"), 404);
        
        // Create a site
        String shortName  = GUID.generate();
        JSONObject result = createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        // Get the site
        response = sendRequest(new GetRequest(URL_SITES + "/" + shortName), 200);
        
        // Delete the site
        response = sendRequest(new DeleteRequest(URL_SITES + "/" + shortName), 200);
        
        // Get the site
        response = sendRequest(new GetRequest(URL_SITES + "/" + shortName), 404);
    }
    
    public void testGetMemberships() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        // Check the memberships
        Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS), 200);
        JSONArray result = new JSONArray(response.getContentAsString());        
        assertNotNull(result);
        assertEquals(1, result.length());
        JSONObject membership = result.getJSONObject(0);
        assertEquals(SiteModel.SITE_MANAGER, membership.get("role"));
        assertEquals(USER_ONE, membership.getJSONObject("authority").get("userName"));        
    }
    
    public void testPostMemberships() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        // Build the JSON membership object
        JSONObject membership = new JSONObject();
        membership.put("role", SiteModel.SITE_CONSUMER);
        JSONObject person = new JSONObject();
        person.put("userName", USER_TWO);
        membership.put("person", person);
        
        // Post the membership
        Response response = sendRequest(new PostRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, membership.toString(), "application/json"), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // Check the result
        assertEquals(SiteModel.SITE_CONSUMER, membership.get("role"));
        assertEquals(USER_TWO, membership.getJSONObject("person").get("userName")); 
        
        // Get the membership list
        response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS), 200);   
        JSONArray result2 = new JSONArray(response.getContentAsString());
        assertNotNull(result2);
        assertEquals(2, result2.length());
    }
    
    public void testGetMembership() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        // Test error conditions
        sendRequest(new GetRequest(URL_SITES + "/badsite" + URL_MEMBERSHIPS + "/" + USER_ONE), 404);
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/baduser"), 404);
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO), 404);
        
        Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_ONE), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // Check the result
        assertEquals(SiteModel.SITE_MANAGER, result.get("role"));
        assertEquals(USER_ONE, result.getJSONObject("authority").get("userName")); 
    }
    
    public void testPutMembership() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
                
        // Build the JSON membership object
        JSONObject membership = new JSONObject();
        membership.put("role", SiteModel.SITE_CONSUMER);
        JSONObject person = new JSONObject();
        person.put("userName", USER_TWO);
        membership.put("person", person);
        
        // Post the membership
        Response response = sendRequest(new PostRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, membership.toString(), "application/json"), 200);
        JSONObject newMember = new JSONObject(response.getContentAsString());
        
        // Update the role by returning the data.
        newMember.put("role", SiteModel.SITE_COLLABORATOR);
        response = sendRequest(new PutRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO, newMember.toString(), "application/json"), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        // Check the result
        assertEquals(SiteModel.SITE_COLLABORATOR, result.get("role"));
        assertEquals(USER_TWO, result.getJSONObject("authority").get("userName"));
        
        // Double check and get the membership for user two
        response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO), 200);
        result = new JSONObject(response.getContentAsString());
        assertEquals(SiteModel.SITE_COLLABORATOR, result.get("role"));
        assertEquals(USER_TWO, result.getJSONObject("authority").get("userName"));
        
    }
    
    public void testGroupMembership() throws Exception
    {
    	String testGroup = "SiteServiceTestGroupA";
    	String testGroupName = "GROUP_" + testGroup;
    	
        if(!authorityService.authorityExists(testGroup))
        {
            this.authenticationComponent.setSystemUserAsCurrentUser();
        	 
        	testGroupName = authorityService.createAuthority(AuthorityType.GROUP, testGroup, testGroup, authorityService.getDefaultZones());
        }     	
         
        this.authenticationComponent.setCurrentUser(USER_ONE);

    	// CRUD a membership group for a web site
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
                
        // Build the JSON membership object
        JSONObject membership = new JSONObject();
        membership.put("role", SiteModel.SITE_CONSUMER);
        JSONObject group = new JSONObject();
        group.put("fullName", testGroupName);
        membership.put("group", group);
        
        // Create a new group membership
        {
        	Response response = sendRequest(new PostRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, membership.toString(), "application/json"), 200);
        	JSONObject newMember = new JSONObject(response.getContentAsString());
        
        	// Validate the return value
        	assertEquals("role not correct", SiteModel.SITE_CONSUMER, newMember.getString("role"));
        	JSONObject newGroup = newMember.getJSONObject("authority");
        	assertNotNull("newGroup");
        	assertEquals("full name not correct", testGroupName, newGroup.getString("fullName"));
         	assertEquals("authorityType not correct", "GROUP", newGroup.getString("authorityType"));
        	

        	// Now send the returned value back with a new role (COLLABORATOR)
        	newMember.put("role", SiteModel.SITE_COLLABORATOR);
        	response = sendRequest(new PutRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO, newMember.toString(), "application/json"), 200);
        	JSONObject updateResult = new JSONObject(response.getContentAsString());
        	assertEquals("role not correct", SiteModel.SITE_COLLABORATOR, updateResult.getString("role"));
        	
        }
        
        // Now List membership to show the group from above.
        {
        	Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS), 200);   
        	JSONArray listResult = new JSONArray(response.getContentAsString());
        	
        	/**
        	 * The result should have at least 2 elements, 1 for the user who created and 1 for the group added above
        	 */
        	assertTrue("result too small", listResult.length() >= 2);
        	for(int i = 0; i < listResult.length(); i++)
        	{
        		JSONObject obj = listResult.getJSONObject(i);
        		JSONObject authority = obj.getJSONObject("authority");
        		if(authority.getString("authorityType").equals("GROUP"))
        		{
        			assertEquals("full name not correct", testGroupName, authority.getString("fullName"));
        			
        		}
        		if(authority.getString("authorityType").equals("USER"))
        		{
        			assertEquals("full name not correct", USER_ONE, authority.getString("fullName"));
        		}
        	}
        }
        
        // Now get the group membership from above
        // Now List membership to show the group from above.
        {
        	Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + '/' + testGroupName), 200);   
        	JSONObject getResult = new JSONObject(response.getContentAsString());
        	System.out.println(response.getContentAsString());
        	JSONObject grp = getResult.getJSONObject("authority");
        	assertEquals("full name not correct", testGroupName, grp.getString("fullName"));
        }
    }
    
    public void testDeleteMembership() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
     
        // Build the JSON membership object
        JSONObject membership = new JSONObject();
        membership.put("role", SiteModel.SITE_CONSUMER);
        JSONObject person = new JSONObject();
        person.put("userName", USER_TWO);
        membership.put("person", person);
        
        // Post the membership
        sendRequest(new PostRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, membership.toString(), "application/json"), 200);
        
        // Delete the membership
        sendRequest(new DeleteRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO), 200);
        
        // Check that the membership has been deleted
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS + "/" + USER_TWO), 404);
        
    }
    
    public void testGetPersonSites() throws Exception
    {
        // Create a site
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        String shortName2  = GUID.generate();
        createSite("myPreset", shortName2, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        Response response = sendRequest(new GetRequest("/api/people/" + USER_TWO + "/sites"), 200);
        JSONArray result = new JSONArray(response.getContentAsString());
        
        assertNotNull(result);
        assertEquals(0, result.length());
        
        // Add some memberships
        JSONObject membership = new JSONObject();
        membership.put("role", SiteModel.SITE_CONSUMER);
        JSONObject person = new JSONObject();
        person.put("userName", USER_TWO);
        membership.put("person", person);
        sendRequest(new PostRequest(URL_SITES + "/" + shortName + URL_MEMBERSHIPS, membership.toString(), "application/json"), 200);
        membership = new JSONObject();
        membership.put("role", SiteModel.SITE_CONSUMER);
        person = new JSONObject();
        person.put("userName", USER_TWO);
        membership.put("person", person);
        sendRequest(new PostRequest(URL_SITES + "/" + shortName2 + URL_MEMBERSHIPS, membership.toString(), "application/json"), 200);        
        
        response = sendRequest(new GetRequest("/api/people/" + USER_TWO + "/sites"), 200);
        result = new JSONArray(response.getContentAsString());
        
        assertNotNull(result);
        assertEquals(2, result.length());
        
        response = sendRequest(new GetRequest("/api/people/" + USER_ONE + "/sites"), 200);
        result = new JSONArray(response.getContentAsString());
        
        assertNotNull(result);
        assertEquals(2, result.length());
        
        response = sendRequest(new GetRequest("/api/people/" + USER_THREE + "/sites"), 200);
        result = new JSONArray(response.getContentAsString());
        
        assertNotNull(result);
        assertEquals(0, result.length());
        
        response = sendRequest(new GetRequest("/api/people/" + USER_ONE + "/sites?size=1"), 200);
        result = new JSONArray(response.getContentAsString());
        
        assertNotNull(result);
        assertEquals(1, result.length());
        
        response = sendRequest(new GetRequest("/api/people/" + USER_ONE + "/sites?size=5"), 200);
        result = new JSONArray(response.getContentAsString());
        
        assertNotNull(result);
        assertEquals(2, result.length());
    }   
    
    public void testSiteCustomProperties()
        throws Exception
    {
        // Create a site with a custom property
        SiteInfo siteInfo = this.siteService.createSite("testPreset", "mySiteWithCustomProperty2", "testTitle", "testDescription", SiteVisibility.PUBLIC);
        NodeRef siteNodeRef = siteInfo.getNodeRef();
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(QName.createQName(SiteModel.SITE_CUSTOM_PROPERTY_URL, "additionalInformation"), "information");
        this.nodeService.addAspect(siteNodeRef, QName.createQName(SiteModel.SITE_MODEL_URL, "customSiteProperties"), properties);
        this.createdSites.add("mySiteWithCustomProperty2");
        
        // Get the detail so of the site
        Response response = sendRequest(new GetRequest(URL_SITES + "/mySiteWithCustomProperty2"), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        assertNotNull(result);
        JSONObject customProperties = result.getJSONObject("customProperties");
        assertNotNull(customProperties);
        JSONObject addInfo = customProperties.getJSONObject("{http://www.alfresco.org/model/sitecustomproperty/1.0}additionalInformation");
        assertNotNull(addInfo);
        assertEquals("{http://www.alfresco.org/model/sitecustomproperty/1.0}additionalInformation", addInfo.get("name"));
        assertEquals("information", addInfo.get("value"));
        assertEquals("{http://www.alfresco.org/model/dictionary/1.0}text", addInfo.get("type"));
        assertEquals("Additional Site Information", addInfo.get("title"));
        
    }
    
    /**
     * End to end sanity check of web site invitation.
     * 
     * Nominated and Moderated invitations.
     * 
     * @throws Exception
     */
    public void testInvitationSanityCheck() throws Exception
    {
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        String inviteComments = "Please sir, let me in";
        String userName = USER_TWO;
        String roleName = SiteModel.SITE_CONSUMER;
        
        String inviteeFirstName = "Buffy";
        String inviteeLastName = "Summers";
        String inviteeEmail = "buffy@sunnydale";
        String inviteeUserName = userName;
        String serverPath = "http://localhost:8081/share/";
        String acceptURL = "page/accept-invite";
        String rejectURL = "page/reject-invite";
        
    	// Create a nominated invitation
        String nominatedId = createNominatedInvitation(shortName, inviteeFirstName, inviteeLastName, inviteeEmail, inviteeUserName, roleName, serverPath, acceptURL, rejectURL);
        
    	// Get the nominated invitation
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations/" + nominatedId), 200);	
        
        //Create a new moderated invitation
        String moderatedId = createModeratedInvitation(shortName, inviteComments, userName, roleName);
    	
    	// Get the moderated invitation
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations/" + moderatedId), 200);
    	
    	// search for the moderated invitation 
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations?inviteeUserName=" + userName), 200);
          
        // Search for all invitations on this site
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations"), 200);
    	
    	// cancel the nominated invitation
        sendRequest(new DeleteRequest(URL_SITES + "/" + shortName + "/invitations/" + nominatedId), 200);
    	
    	// cancel the moderated invitation
        sendRequest(new DeleteRequest(URL_SITES + "/" + shortName + "/invitations/" + moderatedId), 200);	
    }
    
    /**
     * Detailed Test of Get Invitation Web Script
     * @throws Exception
     */
    public void testGetInvitation() throws Exception
    {
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        /*
         * Create a new moderated invitation
         */
        String inviteeComments = "Please sir, let $* me in";
        String userName = USER_TWO;
        String roleName = SiteModel.SITE_CONSUMER;
        String inviteId = createModeratedInvitation(shortName, inviteeComments, userName, roleName);
              
        /*
         * Negative test - site does not exist
         */
       	sendRequest(new GetRequest(URL_SITES + "/rubbish/invitations/" + inviteId), 404);
        
        /*
         * Negative test - site does exist but invitation doesn't
         */
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations/jbpm$8787487"), 404);
        
        /*
         * Negative test - site does exist but invitation engine is wrong
         */
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations/trash$123"), 404);
        
        /*
         * Negative test - site does exist but invitation doesn't  no $ in inviteId
         */
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations/trash123"), 404);
        
        /*
         * Positive test - get the invitation and validate that it is correct
         */
        {
        	Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations/" + inviteId), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            //System.out.println(response.getContentAsString());
            JSONObject data = top.getJSONObject("data");
            assertNotNull("data is null", data);
            assertEquals("inviteId is not set", data.getString("inviteId"), inviteId);
            assertEquals("invitationType", "MODERATED", data.getString("invitationType"));
            assertEquals("inviteeUserName is not set", userName, data.getString("inviteeUserName"));
            assertEquals("resourceName is not correct", shortName, data.getString("resourceName"));
            assertEquals("resourceType is not correct", "WEB_SITE", data.getString("resourceType"));
            // Moderated specific properties
            assertEquals("inviteeComments", inviteeComments, data.getString("inviteeComments"));
            assertEquals("roleName is not set", roleName, data.getString("roleName"));

        }
        
        /*
         * Cancel the invitation
         */
        sendRequest(new DeleteRequest(URL_SITES + "/" + shortName + "/invitations/" + inviteId), 200);
        
        /*
         * Verify that the invitation is no longer open
         */
        sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations/" + inviteId), 404);
        
        /**
         * Create a nominated invitation
         */
        String inviteeFirstName = "Buffy";
        String inviteeLastName = "Summers";
        String inviteeEmail = "FirstName123.LastName123@email.com";
        String inviteeUserName = null;
        String serverPath = "http://localhost:8081/share/";
        String acceptURL = "page/accept-invite";
        String rejectURL = "page/reject-invite";
        inviteId = createNominatedInvitation(shortName, inviteeFirstName, inviteeLastName, inviteeEmail, inviteeUserName, roleName, serverPath, acceptURL, rejectURL);
        
        /*
         * Positive test - get the invitation and validate that it is correct
         * inviteId and inviteeUserName will be generated.
         */
        {
        	Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations/" + inviteId), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            //System.out.println(response.getContentAsString());
            JSONObject data = top.getJSONObject("data");
            assertNotNull("data is null", data);
            assertEquals("inviteId is not set", data.getString("inviteId"), inviteId);
            assertEquals("invitationType", "NOMINATED", data.getString("invitationType"));
            assertEquals("resourceName is not correct", shortName, data.getString("resourceName"));
            assertEquals("resourceType is not correct", "WEB_SITE", data.getString("resourceType"));
            
            // Nominated specific attributes
            assertEquals("roleName is not set", roleName, data.getString("roleName"));
            // Generated user name
            assertNotNull("inviteeUserName is not set", data.getString("inviteeUserName"));
            
        }
        
        /*
         * Cancel the nominated invitation
         */
        sendRequest(new DeleteRequest(URL_SITES + "/" + shortName + "/invitations/" + inviteId), 200);
                       
    }
    
    /**
     * Detailed Test of List Invitation Web Script.
     * @throws Exception
     */
    public void testListInvitation() throws Exception
    {
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        String inviteeComments = "Please sir, let $* me in";
        String userName = USER_TWO;
        String roleName = SiteModel.SITE_CONSUMER;
        String moderatedIdA = createModeratedInvitation(shortName, inviteeComments, userName, roleName);
        
        String inviteeCommentsB = "Please sir, let $* me in";
        String userNameB = USER_THREE;
        String roleNameB = SiteModel.SITE_CONSUMER;
        String moderatedIdB = createModeratedInvitation(shortName, inviteeCommentsB, userNameB, roleNameB);
        
        String inviteeFirstName = "Buffy";
        String inviteeLastName = "Summers";
        String inviteeEmail = "buffy@sunnydale";
        String inviteeUserName = userName;
        String serverPath = "http://localhost:8081/share/";
        String acceptURL = "page/accept-invite";
        String rejectURL = "page/reject-invite";
        
    	// Create a nominated invitation
        String nominatedId = createNominatedInvitation(shortName, inviteeFirstName, inviteeLastName, inviteeEmail, inviteeUserName, roleName, serverPath, acceptURL, rejectURL);        
        
        /**
         * search by user - negative test wombat does not have an invitation 
         */
        {
        	Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations?inviteeUserName=wombat"), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals("user wombat", data.length(), 0);
            
        }
        /**
         * search by user - find USER_TWO's two invitations 
         */
        {
        	Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations?inviteeUserName=" + USER_TWO), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            //System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals("user two invitation not found", data.length(), 2);
            JSONObject first = data.getJSONObject(0);
            assertEquals("first userid is wrong", first.getString("inviteeUserName"), USER_TWO);
            JSONObject second = data.getJSONObject(0);
            assertEquals("second userid is wrong", second.getString("inviteeUserName"), USER_TWO);
            
        }
        
        /**
         * search by type - should find two moderated invitations
         */

        {
        	Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations?invitationType=MODERATED"), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            //System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals("two moderated invitations not found", data.length(), 2);
        }
        
        {
        	Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations?invitationType=NOMINATED"), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            //System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals("one nominated invitation not found", data.length(), 1);
        }
        
        // negative test - unknown invitationType
        {
        	Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations?invitationType=Crap"), 500);
            JSONObject top = new JSONObject(response.getContentAsString());
        }
        
        /**
         * search by user and type
         */
        {
        	Response response = sendRequest(new GetRequest(URL_SITES + "/" + shortName + "/invitations?inviteeUserName=" + USER_TWO + "&invitationType=MODERATED"), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            //System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals("user two invitation not found", data.length(), 1);
            JSONObject first = data.getJSONObject(0);
            assertEquals("first userid is wrong", first.getString("inviteeUserName"), USER_TWO);
            assertEquals("type is wrong", first.getString("invitationType"), "MODERATED"); 
        }      
    }
    
    /**
     * Detailed test of Create Invitation Web Script
     * 
     * Create Nominated Invitation
     * 
     * Create Moderated Invitation
     * 
     * @throws Exception
     */
    public void testCreateInvitation() throws Exception
    {
        String shortName  = GUID.generate();
        createSite("myPreset", shortName, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        

        String inviteComments = "Please sir, let me in";
        String userName = USER_TWO;
        String roleName = SiteModel.SITE_CONSUMER;
        String inviteId = null;

        /*
         * Negative test - wrong invitation type
         */
        {
            JSONObject newInvitation = new JSONObject();
            newInvitation.put("invitationType", "Grundge");
        	newInvitation.put("inviteeRoleName", roleName);
        	newInvitation.put("inviteeComments", inviteComments);
        	newInvitation.put("inviteeUserName", userName);
        	sendRequest(new PostRequest(URL_SITES + "/" + shortName + "/invitations",  newInvitation.toString(), "application/json"), Status.STATUS_BAD_REQUEST);   
        }
        
        /*
         * Negative test - missing Invitation type
         */
        {
            JSONObject newInvitation = new JSONObject();
        	newInvitation.put("inviteeRoleName", roleName);
        	newInvitation.put("inviteeComments", inviteComments);
        	newInvitation.put("inviteeUserName", userName);
        	sendRequest(new PostRequest(URL_SITES + "/" + shortName + "/invitations",  newInvitation.toString(), "application/json"), Status.STATUS_BAD_REQUEST);   
        }
        
        /*
         * Negative test - blank RoleName
         */
        {
            JSONObject newInvitation = new JSONObject();
            newInvitation.put("invitationType", "MODERATED");
        	newInvitation.put("inviteeRoleName", "");
        	newInvitation.put("inviteeComments", inviteComments);
        	newInvitation.put("inviteeUserName", userName);
        	sendRequest(new PostRequest(URL_SITES + "/" + shortName + "/invitations",  newInvitation.toString(), "application/json"), Status.STATUS_BAD_REQUEST);   
        }
        
        /*
         * Create a new moderated invitation
         */
        JSONObject newInvitation = new JSONObject();
        {
            newInvitation.put("invitationType", "MODERATED");
        	newInvitation.put("inviteeRoleName", roleName);
        	newInvitation.put("inviteeComments", inviteComments);
        	newInvitation.put("inviteeUserName", userName);
        	Response response = sendRequest(new PostRequest(URL_SITES + "/" + shortName + "/invitations",  newInvitation.toString(), "application/json"), Status.STATUS_CREATED);   
        	JSONObject top = new JSONObject(response.getContentAsString());
        	JSONObject data = top.getJSONObject("data");
        	inviteId = data.getString("inviteId");
            assertEquals("invitationType", "MODERATED", data.getString("invitationType"));
            assertEquals("inviteeUserName is not set", userName, data.getString("inviteeUserName"));
            assertEquals("resourceName is not correct", shortName, data.getString("resourceName"));
            assertEquals("resourceType is not correct", "WEB_SITE", data.getString("resourceType"));
        	
        }
        assertNotNull("inviteId is null", inviteId);
        assertTrue("inviteId is too small", inviteId.length() > 0);
        
    }    
    
    private String createNominatedInvitation(String siteName, String inviteeFirstName, String inviteeLastName, String inviteeEmail, String inviteeUserName, String inviteeRoleName, String serverPath, String acceptURL, String rejectURL) throws Exception 
    {
        /*
         * Create a new nominated invitation
         */
        JSONObject newInvitation = new JSONObject();
        
      	newInvitation.put("invitationType", "NOMINATED");
    	newInvitation.put("inviteeRoleName", inviteeRoleName);
        if(inviteeUserName != null)
        {
        	// nominate an existing user
            newInvitation.put("inviteeUserName", inviteeUserName);
        }
        else
        {
        	// nominate someone else
        	newInvitation.put("inviteeFirstName", inviteeFirstName);
        	newInvitation.put("inviteeLastName", inviteeLastName);
        	newInvitation.put("inviteeEmail", inviteeEmail);
        }
        newInvitation.put("serverPath", serverPath);
        newInvitation.put("acceptURL", acceptURL);
        newInvitation.put("rejectURL", rejectURL);    
        
        Response response = sendRequest(new PostRequest(URL_SITES + "/" + siteName + "/invitations",  newInvitation.toString(), "application/json"), 201);   
        JSONObject top = new JSONObject(response.getContentAsString());
        JSONObject data = top.getJSONObject("data");
        String inviteId = data.getString("inviteId");
        
        return inviteId;
    }
    
    private String createModeratedInvitation(String siteName, String inviteeComments, String inviteeUserName, String inviteeRoleName) throws Exception
    {
        /*
         * Create a new moderated invitation
         */
        JSONObject newInvitation = new JSONObject();
        
        newInvitation.put("invitationType", "MODERATED");
        newInvitation.put("inviteeRoleName", inviteeRoleName);
        newInvitation.put("inviteeComments", inviteeComments);
        newInvitation.put("inviteeUserName", inviteeUserName);
        Response response = sendRequest(new PostRequest(URL_SITES + "/" + siteName + "/invitations",  newInvitation.toString(), "application/json"), 201);   
        JSONObject top = new JSONObject(response.getContentAsString());
        JSONObject data = top.getJSONObject("data");
        String inviteId = data.getString("inviteId");
        
        return inviteId;
    }
}
