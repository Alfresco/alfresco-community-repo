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
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.alfresco.web.scripts.Status;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit Test to test Invite Web Script API
 * 
 * @author Glen Johnson at Alfresco dot com
 */
public class InviteServiceTest extends BaseWebScriptTest
{
    private AuthorityService authorityService;
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private SiteService siteService;
    private TransactionService transactionService;

    private static final String USER_ADMIN = "admin";
    private static final String USER_INVITER = "InviterUser";
    private static final String INVITEE_FIRSTNAME = "InviteeFirstName";
    private static final String INVITEE_LASTNAME = "InviteeLastName";
    private static final String INVITEE_EMAIL = "inviteeFN.inviteeLN@email123.com";
    private static final String SITE_SHORT_NAME_INVITE = "InviteSiteShortName";
    private static final String GROUP_EMAIL_CONTRIBUTORS = "EMAIL_CONTRIBUTORS";

    private static final String URL_INVITE_SERVICE = "/api/invite";
    private static final String URL_INVITERSP_SERVICE = "/api/inviteresponse";
    private static final String URL_INVITES_SERVICE = "/api/invites";

    private static final String INVITE_ACTION_START = "start";
    private static final String INVITE_ACTION_CANCEL = "cancel";

    private static final String INVITE_RSP_ACCEPT = "accept";
    private static final String INVITE_RSP_REJECT = "reject";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // get references to services
        this.authorityService = (AuthorityService) getServer()
                .getApplicationContext().getBean("authorityService");
        this.authenticationService = (AuthenticationService) getServer()
                .getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent) getServer()
                .getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService) getServer()
                .getApplicationContext().getBean("PersonService");
        this.siteService = (SiteService) getServer().getApplicationContext()
                .getBean("siteService");
        this.transactionService = (TransactionService) getServer().getApplicationContext()
                .getBean("transactionService");
        
        // set current user as admin for various setup operations needing admin rights 
        this.authenticationComponent.setCurrentUser(USER_ADMIN);
        
        // Create inviter
        createPerson(USER_INVITER);
        
        // Create site for Inviter to invite Invitee to
        // - only create the site if it doesn't already exist
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_INVITE);
        if (siteInfo == null)
        {
            this.siteService.createSite("InviteSitePreset",
                    SITE_SHORT_NAME_INVITE, "InviteSiteTitle",
                    "InviteSiteDescription", true);
        }

        // Do tests as inviter user
        this.authenticationComponent.setCurrentUser(USER_INVITER);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        // admin authority required for various cleanup operations needing admin rights
        this.authenticationComponent.setCurrentUser(USER_ADMIN);
        
        // delete the inviter
        deletePerson(USER_INVITER);

        // delete invite site
        siteService.deleteSite(SITE_SHORT_NAME_INVITE);        
    }

    private void addUserToGroup(String groupName, String userName)
    {
        // get the full name for the group
        String fullGroupName = this.authorityService.getName(AuthorityType.GROUP, groupName);
        
        // create group if it does not exist
        if (this.authorityService.authorityExists(fullGroupName) == false)
        {
            this.authorityService.createAuthority(AuthorityType.GROUP, null, fullGroupName);
        }

        // add the user to the group
        this.authorityService.addAuthority(fullGroupName, userName);
    }
    
    private void removeUserFromGroup(String groupName, String userName)
    {
        // get the full name for the group
        String fullGroupName = this.authorityService.getName(AuthorityType.GROUP, groupName);
        
        // remove user from the group
        this.authorityService.removeAuthority(fullGroupName, userName);
        
        // delete the group
        this.authorityService.deleteAuthority(fullGroupName);
    }
    
    private void createPerson(String userName)
    {
        // if user with given user name doesn't already exist then create user
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            // create user
            this.authenticationService.createAuthentication(userName,
                    "password".toCharArray());
        }

        // if person node with given user name doesn't already exist then create
        // person
        if (this.personService.personExists(userName) == false)
        {
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "FirstName123");
            personProps.put(ContentModel.PROP_LASTNAME, "LastName123");
            personProps.put(ContentModel.PROP_EMAIL,
                    "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");

            // create person node for user
            this.personService.createPerson(personProps);
        }
    }
    
    private void deletePerson(String userName)
    {
        // delete person node associated with user
        this.personService.deletePerson(userName);
        
        // delete user
        this.authenticationService.deleteAuthentication(userName);
    }

    private JSONObject startInvite(String inviteeFirstName, String inviteeLastName, String inviteeEmail,
            String siteShortName, int expectedStatus) throws Exception
    {
        // Inviter sends invitation to Invitee to join a Site
        String startInviteUrl = URL_INVITE_SERVICE + "/" + INVITE_ACTION_START
                + "?inviteeFirstName=" + inviteeFirstName + "&inviteeLastName=" + inviteeLastName
                + "&inviteeEmail=" + inviteeEmail + "&siteShortName=" + siteShortName;
        MockHttpServletResponse response = getRequest(startInviteUrl, expectedStatus);

        JSONObject result = new JSONObject(response.getContentAsString());

        return result;
    }

    private JSONArray getInvitesByInviteId(String inviteId, int expectedStatus)
            throws Exception
    {
        // construct get invites URL
        String getInvitesUrl = URL_INVITES_SERVICE + "?inviteId=" + inviteId;

        // invoke get invites web script
        MockHttpServletResponse response = getRequest(getInvitesUrl,
                expectedStatus);

        JSONArray result = new JSONArray(response.getContentAsString());

        return result;
    }

    private JSONArray getInvitesByInviterUserName(String inviterUserName, int expectedStatus)
    throws Exception
    {
        // construct get invites URL
        String getInvitesUrl = URL_INVITES_SERVICE + "?inviterUserName=" + inviterUserName;
        
        // invoke get invites web script
        MockHttpServletResponse response = getRequest(getInvitesUrl,
                expectedStatus);
        
        JSONArray result = new JSONArray(response.getContentAsString());
        
        return result;
    }

    private JSONArray getInvitesByInviteeUserName(String inviteeUserName, int expectedStatus)
    throws Exception
    {
        // construct get invites URL
        String getInvitesUrl = URL_INVITES_SERVICE + "?inviteeUserName=" + inviteeUserName;
        
        // invoke get invites web script
        MockHttpServletResponse response = getRequest(getInvitesUrl,
                expectedStatus);
        
        JSONArray result = new JSONArray(response.getContentAsString());
        
        return result;
    }

    private JSONArray getInvitesBySiteShortName(String siteShortName, int expectedStatus)
    throws Exception
    {
        // construct get invites URL
        String getInvitesUrl = URL_INVITES_SERVICE + "?siteShortName=" + siteShortName;
        
        // invoke get invites web script
        MockHttpServletResponse response = getRequest(getInvitesUrl,
                expectedStatus);
        
        JSONArray result = new JSONArray(response.getContentAsString());
        
        return result;
    }

    public void testStartInvite() throws Exception
    {
        JSONObject result = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_EMAIL,
                SITE_SHORT_NAME_INVITE, Status.STATUS_OK);

        assertEquals(INVITE_ACTION_START, result.get("action"));
        assertEquals(INVITEE_FIRSTNAME, result.get("inviteeFirstName"));
        assertEquals(INVITEE_LASTNAME, result.get("inviteeLastName"));
        assertEquals(INVITEE_EMAIL, result.get("inviteeEmail"));
        assertEquals(SITE_SHORT_NAME_INVITE, result.get("siteShortName"));
    }

    public void testCancelInvite() throws Exception
    {
        // inviter starts invite workflow
        JSONObject result = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_EMAIL,
                SITE_SHORT_NAME_INVITE, Status.STATUS_OK);

        // get hold of invite ID of started invite
        String inviteId = result.getString("inviteId");

        // Inviter cancels pending invitation
        String cancelInviteUrl = URL_INVITE_SERVICE + "/"
                + INVITE_ACTION_CANCEL + "?inviteId=" + inviteId;
        MockHttpServletResponse response = getRequest(cancelInviteUrl,
                Status.STATUS_OK);
    }

    public void testAcceptInvite() throws Exception
    {
        // inviter starts invite (sends out invitation)
        JSONObject result = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_EMAIL,
                SITE_SHORT_NAME_INVITE, Status.STATUS_OK);

        // get hold of invite ID of started invite
        String inviteId = result.getString("inviteId");

        // get hold of invitee user name that was generated as part of starting
        // the invite
        String inviteeUserName = result.getString("inviteeUserName");

        // Invitee accepts invitation to a Site from Inviter
        String acceptInviteUrl = URL_INVITERSP_SERVICE + "/"
                + INVITE_RSP_ACCEPT + "?inviteId=" + inviteId
                + "&inviteeUserName=" + inviteeUserName + "&siteShortName="
                + SITE_SHORT_NAME_INVITE;
        MockHttpServletResponse response = getRequest(acceptInviteUrl,
                Status.STATUS_OK);
    }

    public void testRejectInvite() throws Exception
    {
        // inviter starts invite (sends out invitation)
        JSONObject result = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_EMAIL,
                SITE_SHORT_NAME_INVITE, Status.STATUS_OK);

        // get hold of invite ID of started invite
        String inviteId = result.getString("inviteId");

        // get hold of invitee user name that was generated as part of starting
        // the invite
        String inviteeUserName = result.getString("inviteeUserName");

        // Invitee rejects invitation to a Site from Inviter
        String rejectInviteUrl = URL_INVITERSP_SERVICE + "/"
                + INVITE_RSP_REJECT + "?inviteId=" + inviteId
                + "&inviteeUserName=" + inviteeUserName + "&siteShortName="
                + SITE_SHORT_NAME_INVITE;
        MockHttpServletResponse response = getRequest(rejectInviteUrl,
                Status.STATUS_OK);
    }

    public void testGetInvitesByInviteId() throws Exception
    {
        // inviter starts invite workflow
        JSONObject startInviteResult = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_EMAIL,
                SITE_SHORT_NAME_INVITE, Status.STATUS_OK);

        // get hold of workflow ID of started invite workflow instance
        
        String inviteId = startInviteResult.getString("inviteId");

        assertEquals(true, ((inviteId != null) && (inviteId.length() != 0)));
        
        // get pending invite matching inviteId from invite started above
        JSONArray getInvitesResult = getInvitesByInviteId(inviteId, Status.STATUS_OK);
        
        assertEquals(getInvitesResult.length(), 1);
        
        JSONObject inviteJSONObj = getInvitesResult.getJSONObject(0);

        assertEquals(inviteId, inviteJSONObj.get("inviteId"));
    }
    
    public void testGetInvitesByInviterUserName() throws Exception
    {
        // inviter starts invite workflow
        JSONObject startInviteResult = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_EMAIL,
                SITE_SHORT_NAME_INVITE, Status.STATUS_OK);
        
        // get pending invites matching inviter user name used in invite started above
        JSONArray getInvitesResult = getInvitesByInviterUserName(USER_INVITER, Status.STATUS_OK);
        
        assertEquals(true, getInvitesResult.length() > 0);
        
        JSONObject inviteJSONObj = getInvitesResult.getJSONObject(0);

        assertEquals(USER_INVITER, inviteJSONObj.get("inviterUserName"));
    }
    
    public void testGetInvitesByInviteeUserName() throws Exception
    {
        // inviter starts invite workflow
        JSONObject startInviteResult = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_EMAIL,
                SITE_SHORT_NAME_INVITE, Status.STATUS_OK);
        
        // get hold of invitee user name property of started invite workflow instance
        String inviteeUserName = startInviteResult.getString("inviteeUserName");

        assertEquals(true, ((inviteeUserName != null) && (inviteeUserName.length() != 0)));
        
        // get pending invites matching invitee user name from invite started above
        JSONArray getInvitesResult = getInvitesByInviteeUserName(inviteeUserName, Status.STATUS_OK);
        
        assertEquals(true, getInvitesResult.length() > 0);
        
        JSONObject inviteJSONObj = getInvitesResult.getJSONObject(0);

        assertEquals(inviteeUserName, inviteJSONObj.get("inviteeUserName"));
    }
    
    public void testGetInvitesBySiteShortName() throws Exception
    {
        // inviter starts invite workflow
        JSONObject startInviteResult = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_EMAIL,
                SITE_SHORT_NAME_INVITE, Status.STATUS_OK);
        
        // get hold of site short name property of started invite workflow instance
        String siteShortName = startInviteResult.getString("siteShortName");

        assertEquals(true, ((siteShortName != null) && (siteShortName.length() != 0)));
        
        // get pending invites matching site short name from invite started above
        JSONArray getInvitesResult = getInvitesBySiteShortName(siteShortName, Status.STATUS_OK);
        
        assertEquals(true, getInvitesResult.length() > 0);
        
        JSONObject inviteJSONObj = getInvitesResult.getJSONObject(0);

        assertEquals(siteShortName, inviteJSONObj.get("siteShortName"));
    }
}