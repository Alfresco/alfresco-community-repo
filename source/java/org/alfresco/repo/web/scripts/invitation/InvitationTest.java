/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.invitation;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit test of Invitation REST API.
 * 
 * @See SiteServiceTest.java which tests invitations to Web sites.
 * 
 * @author Mark Rogers
 */
public class InvitationTest extends BaseWebScriptTest
{    
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private SiteService siteService;
    private NodeService nodeService;
    
    private static final String USER_ONE = "InvitationTestOne";
    private static final String USER_TWO = "InvitationTestTwo";
    private static final String USER_THREE = "InvitationTestThree";
    
    private static final String URL_SITES = "/api/sites";
    private static final String URL_INVITATIONS = "/api/invitations";

    
    private List<String> createdSites = new ArrayList<String>(5);
    private List<Tracker> createdInvitations = new ArrayList<Tracker>(10);
    
    private class Tracker
    {
    	public Tracker(String inviteId, String siteName)
    	{
    		this.inviteId = inviteId;
    		this.siteName = siteName;
    	}
    	public String inviteId;
    	public String siteName;
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        
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
        
        for (Tracker invite : this.createdInvitations)
        {
            sendRequest(new DeleteRequest(URL_SITES + "/" + invite.siteName + "/invitations/" + invite.inviteId), 0);
        }
        
        // Clear the list
        this.createdSites.clear();
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
    
    /**
     * Detailed Test of List Invitation Web Script.
     * @throws Exception
     */
    public void testListInvitation() throws Exception
    {
    	// Create two sites.
        String shortNameSiteA  = GUID.generate();
        createSite("myPreset", shortNameSiteA, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        String shortNameSiteB  = GUID.generate();
        createSite("myPreset", shortNameSiteB, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);
        
        // Create a moderated invitation on SiteA, USER_TWO
        String inviteeComments = "Please sir, let $* me in";
        String userName = USER_TWO;
        String roleName = SiteModel.SITE_CONSUMER;
        String moderatedIdAUSER_TWO = createModeratedInvitation(shortNameSiteA, inviteeComments, userName, roleName);

        // Create a moderated invitation on SiteB, USER_TWO
        String moderatedIdBUSER_TWO = createModeratedInvitation(shortNameSiteB, inviteeComments, userName, roleName);
        
        String inviteeCommentsB = "Please sir, let $* me in";
        String userNameB = USER_THREE;
        String roleNameB = SiteModel.SITE_CONSUMER;
        String moderatedIdBUSER_THREE = createModeratedInvitation(shortNameSiteA, inviteeCommentsB, userNameB, roleNameB);
        
        String inviteeFirstName = "Buffy";
        String inviteeLastName = "Summers";
        String inviteeEmail = "buffy@sunnydale";
        String inviteeUserName = userName;
        String serverPath = "http://localhost:8081/share/";
        String acceptURL = "page/accept-invite";
        String rejectURL = "page/reject-invite";
        
    	// Create a nominated invitation
        String nominatedId = createNominatedInvitation(shortNameSiteA, inviteeFirstName, inviteeLastName, inviteeEmail, inviteeUserName, roleName, serverPath, acceptURL, rejectURL);        
        
        /**
         * search by user - find USER_TWO's three invitations 
         */
        {
        	Response response = sendRequest(new GetRequest(URL_INVITATIONS + "?inviteeUserName=" + USER_TWO), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            //System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            
            JSONObject first = getInvitation(moderatedIdAUSER_TWO, data);
            assertNotNull("first is null", first);
            JSONObject second = getInvitation(moderatedIdBUSER_TWO, data);
            assertNotNull("second is null", second);
            JSONObject third = getInvitation(nominatedId, data);
            assertNotNull("third is null", third);
            
          	for(int i = 0; i < data.length(); i++)
        	{
        		JSONObject obj = data.getJSONObject(i);
                assertEquals("userid is wrong", obj.getString("inviteeUserName"), USER_TWO);
        	}            
        }
        
        /**
         * search by type - should find three moderated invitations
         */

        {
        	Response response = sendRequest(new GetRequest(URL_INVITATIONS + "?invitationType=MODERATED"), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            //System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
          	for(int i = 0; i < data.length(); i++)
        	{
        		JSONObject obj = data.getJSONObject(i);
                assertEquals("invitation type ", obj.getString("invitationType"), "MODERATED");
        	}
            JSONObject first = getInvitation(moderatedIdAUSER_TWO, data);
            assertNotNull("first is null", first);
            JSONObject second = getInvitation(moderatedIdBUSER_TWO, data);
            assertNotNull("second is null", second);
            JSONObject third = getInvitation(moderatedIdBUSER_THREE, data);
            assertNotNull("third is null", third);
        }
        
        /**
         * Search by type and site
         */
        
        {
        	Response response = sendRequest(new GetRequest(URL_INVITATIONS + "?invitationType=MODERATED&resourceName="+shortNameSiteA+ "&resourceType=WEB_SITE"), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals("two moderated invitations not found", data.length(), 2);
        }
       
        // negative test - unknown resourceType
        {
        	sendRequest(new GetRequest(URL_INVITATIONS + "?invitationType=MODERATED&resourceName="+shortNameSiteA+ "&resourceType=crap"), 500);
        }

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
        
        createdInvitations.add(new Tracker(inviteId, siteName));
        
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
        
        createdInvitations.add(new Tracker(inviteId, siteName));
        
        return inviteId;
    }
    
    private JSONObject getInvitation(String inviteId, JSONArray data) throws Exception
    {
    	for(int i = 0; i < data.length(); i++)
    	{
    		JSONObject obj = data.getJSONObject(i);
    		if(inviteId.equals(obj.getString("inviteId")))
    		{
    			return obj;
    		}
    	}
    	return null;
    }
}
