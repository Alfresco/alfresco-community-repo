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
package org.alfresco.repo.web.scripts.invite;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.site.SiteInfo;
import org.alfresco.repo.site.SiteService;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.alfresco.web.scripts.Status;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit Test to test Invite Web Script API
 * 
 * @author Glen Johnson at Alfresco dot com
 */
public class InviteServiceTest extends BaseWebScriptTest
{
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private SiteService siteService;
    
    private static final String USER_ADMIN = "admin";
    private static final String USER_INVITER = "InviteeUser";
    private static final String INVITEE_EMAIL = "inviter123@email.com";
    private static final String SITE_SHORT_NAME_INVITE = "InviteSiteShortName";
    
    private static final String URL_INVITE_SERVICE = "/api/invite";
    private static final String URL_INVITERSP_SERVICE = "/api/inviteresponse";
    
    private static final String INVITE_ACTION_START = "start";
    private static final String INVITE_ACTION_CANCEL = "cancel";
    
    private static final String INVITE_RSP_ACCEPT = "accept";
    private static final String INVITE_RSP_REJECT = "reject";
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (AuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("siteService");
        
        // Create inviter user
        createUser(USER_INVITER);
        
        // Create site for Inviter to invite Invitee to
        // - only create the site if it doesn't already exist
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_INVITE);
        if (siteInfo == null)
        {
            this.siteService.createSite("InviteSitePreset", SITE_SHORT_NAME_INVITE, "InviteSiteTitle",
                "InviteSiteDescription", true);
        }
        
        // Do tests as inviter user
        this.authenticationComponent.setCurrentUser(USER_INVITER);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // admin user required to delete user
        this.authenticationComponent.setCurrentUser(USER_ADMIN);
        
        // delete the inviter user
        personService.deletePerson(USER_INVITER);
        
        // delete invite site
        siteService.deleteSite(SITE_SHORT_NAME_INVITE);
    }
    
    private void createUser(String userName)
    {
        // if user with given user name doesn't already exist then create user
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            // create user
            this.authenticationService.createAuthentication(userName, "password".toCharArray());
            
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "FirstName123");
            personProps.put(ContentModel.PROP_LASTNAME, "LastName123");
            personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");
            
            // create person node for user
            this.personService.createPerson(personProps);
        }        
    }
    
    private JSONObject startInvite(String inviteeEmail, String siteShortName, int expectedStatus)
        throws Exception
    {
        // Inviter sends invitation to Invitee to join a Site
        String startInviteUrl = URL_INVITE_SERVICE + "/" + INVITE_ACTION_START + "?inviteeEmail=" + inviteeEmail
            + "&siteShortName=" + siteShortName;
        MockHttpServletResponse response = getRequest(startInviteUrl, expectedStatus);
        
        JSONObject result = new JSONObject(response.getContentAsString()); 
        
        return result;
    }
    
    public void testStartInvite() throws Exception
    {
        // admin user required to delete user
        this.authenticationComponent.setCurrentUser(USER_ADMIN);
        
        JSONObject result = startInvite(INVITEE_EMAIL, SITE_SHORT_NAME_INVITE, Status.STATUS_OK);
        
        assertEquals(INVITE_ACTION_START, result.get("action"));
        assertEquals(SITE_SHORT_NAME_INVITE, result.get("siteShortName"));
    }
    
    public void testCancelInvite() throws Exception
    {
        // admin user required to delete user
        this.authenticationComponent.setCurrentUser(USER_ADMIN);
        
        // inviter starts invite workflow
        JSONObject result = startInvite(INVITEE_EMAIL, SITE_SHORT_NAME_INVITE, Status.STATUS_OK);
        
        // get hold of workflow ID of started invite workflow instance
        String workflowId = result.getString("workflowId");
        
        // Inviter cancels pending invitation
        String cancelInviteUrl = URL_INVITE_SERVICE + "/" + INVITE_ACTION_CANCEL + "?workflowId=" + workflowId;
        MockHttpServletResponse response = getRequest(cancelInviteUrl, Status.STATUS_OK);
    }
    
    public void testAcceptInvite() throws Exception
    {
        // admin user required to delete user
        this.authenticationComponent.setCurrentUser(USER_ADMIN);
        
        // inviter starts invite workflow
        JSONObject result = startInvite(INVITEE_EMAIL, SITE_SHORT_NAME_INVITE, Status.STATUS_OK);
        
        // get hold of workflow ID of started invite workflow instance
        String workflowId = result.getString("workflowId");
        
        // get hold of invitee user name that was generated as part of starting the invite
        String inviteeUserName = result.getString("inviteeUserName");
        
        // Invitee accepts invitation to a Site from Inviter
        String acceptInviteUrl = URL_INVITERSP_SERVICE + "/" + INVITE_RSP_ACCEPT + "?workflowId=" + workflowId
            + "&inviteeUserName=" + inviteeUserName + "&siteShortName=" + SITE_SHORT_NAME_INVITE;
        MockHttpServletResponse response = getRequest(acceptInviteUrl, Status.STATUS_OK);
    }
    
    public void testRejectInvite() throws Exception
    {
        // admin user required to delete user
        this.authenticationComponent.setCurrentUser(USER_ADMIN);
        
        // inviter starts invite workflow
        JSONObject result = startInvite(INVITEE_EMAIL, SITE_SHORT_NAME_INVITE, Status.STATUS_OK);
        
        // get hold of workflow ID of started invite workflow instance
        String workflowId = result.getString("workflowId");
        
        // get hold of invitee user name that was generated as part of starting the invite
        String inviteeUserName = result.getString("inviteeUserName");
        
        // Invitee rejects invitation to a Site from Inviter
        String rejectInviteUrl = URL_INVITERSP_SERVICE + "/" + INVITE_RSP_REJECT + "?workflowId=" + workflowId
            + "&inviteeUserName=" + inviteeUserName + "&siteShortName=" + SITE_SHORT_NAME_INVITE;
        MockHttpServletResponse response = getRequest(rejectInviteUrl, Status.STATUS_OK);
    }
}