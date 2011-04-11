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
package org.alfresco.repo.web.scripts.invitation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.repo.web.scripts.invite.InviteServiceTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONException;
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
    private final static QName avatarQName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "test");

    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private NodeService nodeService;
    private TransactionService transactionService;

    private String userOne = "InvitationTestOne" + GUID.generate();
    private String userTwo = "InvitationTestTwo" + GUID.generate();
    private String userThree = "InvitationTestThree" + GUID.generate();

    private static final String URL_SITES = "/api/sites";
    private static final String URL_INVITATIONS = "/api/invitations";

    private List<String> createdSites = new ArrayList<String>(5);
    private List<Tracker> createdInvitations = new ArrayList<Tracker>(10);

    private final Map<String, Map<String, String>> userProperties = new HashMap<String, Map<String,String>>(3);

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

        this.authenticationService = (MutableAuthenticationService) getServer().getApplicationContext().getBean(
                    "AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean(
                    "authenticationComponent");
        this.personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");
        this.nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        this.transactionService = (TransactionService) getServer().getApplicationContext().getBean("TransactionService");

        // turn off email sending to prevent errors during unit testing 
        // (or sending out email by accident from tests)
        InviteServiceTest.configureMailExecutorForTestMode(this.getServer());
        
        this.authenticationComponent.setSystemUserAsCurrentUser();

        // Create users
        createUser(userOne, "Joe", "Bloggs");
        createUser(userTwo, "Jane", "Doe");
        createUser(userThree, "Nick", "Smith");

        // Do tests as user one
        this.authenticationComponent.setCurrentUser(userOne);
    }

    private void createUser(String userName, String firstName, String lastName)
    {
        this.authenticationService.createAuthentication(userName, "PWD".toCharArray());
        PropertyMap ppOne = new PropertyMap(5);
        ppOne.put(ContentModel.PROP_USERNAME, userName);
        ppOne.put(ContentModel.PROP_FIRSTNAME, firstName);
        ppOne.put(ContentModel.PROP_LASTNAME, lastName);
        String email = firstName+"@email.com";
        ppOne.put(ContentModel.PROP_EMAIL, email);
        ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
        NodeRef person = personService.createPerson(ppOne);
        String avatarUrl = makeAvatar(person);

        // Create expected user properties
        HashMap<String, String> properties = new HashMap<String, String>(4);
        properties.put("firstName", firstName);
        properties.put("lastName", lastName);
        properties.put("email", email);
        properties.put("avatar", avatarUrl);
        userProperties.put(userName, properties);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

        // Tidy-up any site's created during the execution of the test
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
        this.createdInvitations.clear();

        RetryingTransactionCallback<Void> deleteCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                personService.deletePerson(userOne);
                personService.deletePerson(userTwo);
                personService.deletePerson(userThree);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteCallback);
    }

    private JSONObject createSite(String sitePreset, String shortName, String title, String description,
                SiteVisibility visibility, int expectedStatus) throws Exception
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
     * Using URL: /api/invitations
     * 
     * @throws Exception
     */
    public void testInvitationsGet() throws Exception
    {
        // Create two sites.
        String shortNameSiteA = GUID.generate();
        createSite("myPreset", shortNameSiteA, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);

        String shortNameSiteB = GUID.generate();
        createSite("myPreset", shortNameSiteB, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);

        // Create a moderated invitation on SiteA, USER_TWO
        String inviteeComments = "Please sir, let $* me in";
        String userName = userTwo;
        String roleName = SiteModel.SITE_CONSUMER;
        String moderatedIdAUSER_TWO = createModeratedInvitation(shortNameSiteA, inviteeComments, userName, roleName);

        // Create a moderated invitation on SiteB, USER_TWO
        String moderatedIdBUSER_TWO = createModeratedInvitation(shortNameSiteB, inviteeComments, userName, roleName);

        String inviteeCommentsB = "Please sir, let $* me in";
        String userNameB = userThree;
        String roleNameB = SiteModel.SITE_CONSUMER;

        // Create a moderated invitation on SiteB, USER_THREE
        String moderatedIdBUSER_THREE = createModeratedInvitation(shortNameSiteB, inviteeCommentsB, userNameB,
                    roleNameB);

        String inviteeFirstName = "Buffy";
        String inviteeLastName = "Summers";
        String inviteeEmail = "buffy@sunnydale";
        String inviteeUserName = userName;
        String serverPath = "http://localhost:8081/share/";
        String acceptURL = "page/accept-invite";
        String rejectURL = "page/reject-invite";

        // Create a nominated invitation on SiteA, UsER_TWO
        String nominatedId = createNominatedInvitation(shortNameSiteA, inviteeFirstName, inviteeLastName, inviteeEmail,
                    inviteeUserName, roleName, serverPath, acceptURL, rejectURL);

        // search by user - find USER_TWO's three invitations
        {
            Response response = sendRequest(new GetRequest(URL_INVITATIONS + "?inviteeUserName=" + userTwo), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            // System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");

            JSONObject moderatedAInv = getInvitation(moderatedIdAUSER_TWO, data);
            assertNotNull("Moderated invitation to Site A not present!", moderatedAInv);
            JSONObject moderatedBInv = getInvitation(moderatedIdBUSER_TWO, data);
            assertNotNull("Moderated invitation to Site B not present!", moderatedBInv);
            JSONObject nominatedInv = getInvitation(nominatedId, data);
            assertNotNull("Nominated invitation to Site A not present!", nominatedInv);

            checkJSONInvitations(data);
        }

        
        // search by type - should find three moderated invitations
        {
            Response response = sendRequest(new GetRequest(URL_INVITATIONS + "?invitationType=MODERATED"), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            // System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            for (int i = 0; i < data.length(); i++)
            {
                JSONObject obj = data.getJSONObject(i);
                assertEquals("Wrong invitation type", "MODERATED", obj.getString("invitationType"));
            }
            JSONObject moderatedATwoInv = getInvitation(moderatedIdAUSER_TWO, data);
            assertNotNull("first is null", moderatedATwoInv);
            JSONObject moderatedBTwoInv = getInvitation(moderatedIdBUSER_TWO, data);
            assertNotNull("second is null", moderatedBTwoInv);
            JSONObject moderatedBThreeInv = getInvitation(moderatedIdBUSER_THREE, data);
            assertNotNull("third is null", moderatedBThreeInv);
        }

        // Search by type and site
        {
            Response response = sendRequest(new GetRequest(URL_INVITATIONS + "?invitationType=MODERATED&resourceName="
                        + shortNameSiteA + "&resourceType=WEB_SITE"), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals("One moderated invitations not found", 1, data.length());
        }

        // negative test - unknown resourceType
        {
            Response response = sendRequest(new GetRequest(URL_INVITATIONS + "?invitationType=MODERATED&resourceName="
                        + shortNameSiteA + "&resourceType=madeUpStuff"), 500);
            assertEquals(500, response.getStatus());
            JSONObject top = new JSONObject(response.getContentAsString());
            assertNotNull(top.getString("message"));
        }
    }

    /**
     * Detailed Test of List Invitation Web Script. 
     * Using URL: /api/sites/{shortname}/invitations
     * 
     * @throws Exception
     */
    public void testSiteInvitationsGet() throws Exception
    {
        // Create two sites.
        String shortNameSiteA = GUID.generate();
        createSite("myPreset", shortNameSiteA, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);

        String shortNameSiteB = GUID.generate();
        createSite("myPreset", shortNameSiteB, "myTitle", "myDescription", SiteVisibility.PUBLIC, 200);

        // Create a moderated invitation on SiteA, USER_TWO
        String inviteeComments = "Please sir, let $* me in";
        String userName = userTwo;
        String roleName = SiteModel.SITE_CONSUMER;
        String moderatedIdAUSER_TWO = createModeratedInvitation(shortNameSiteA, inviteeComments, userName, roleName);

        // Create a moderated invitation on SiteB, USER_TWO
        String moderatedIdBUSER_TWO = createModeratedInvitation(shortNameSiteB, inviteeComments, userName, roleName);

        String inviteeCommentsB = "Please sir, let $* me in";
        String userNameB = userThree;
        String roleNameB = SiteModel.SITE_CONSUMER;

        // Create a moderated invitation on SiteB, USER_THREE
        String moderatedIdBUSER_THREE = createModeratedInvitation(shortNameSiteB, inviteeCommentsB, userNameB,
                    roleNameB);

        String inviteeFirstName = "Buffy";
        String inviteeLastName = "Summers";
        String inviteeEmail = "buffy@sunnydale";
        String inviteeUserName = userName;
        String serverPath = "http://localhost:8081/share/";
        String acceptURL = "page/accept-invite";
        String rejectURL = "page/reject-invite";

        // Create a nominated invitation on SiteA, UsER_TWO
        String nominatedId = createNominatedInvitation(shortNameSiteA, inviteeFirstName, inviteeLastName, inviteeEmail,
                    inviteeUserName, roleName, serverPath, acceptURL, rejectURL);

        // search for all invitations to site A: One Moderated Usr2, One Nominated User2
        {
            String allSiteAUrl = URL_SITES +"/" + shortNameSiteA + "/invitations";
            Response response = sendRequest(new GetRequest(allSiteAUrl), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            // System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals("Wrong number of invitations!", 2, data.length());
            
            JSONObject moderatedAInv = getInvitation(moderatedIdAUSER_TWO, data);
            assertNotNull("Moderated invitation to Site A not present!", moderatedAInv);
            JSONObject nominatedInv = getInvitation(nominatedId, data);
            assertNotNull("Nominated invitation to Site A not present!", nominatedInv);
            checkJSONInvitations(data);
        }

        // search for all invitations to site B: One Moderated User2, One Moderated User3.
        {
            String allSiteBUrl = URL_SITES +"/" + shortNameSiteB + "/invitations";
            Response response = sendRequest(new GetRequest(allSiteBUrl), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            // System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals("Wrong number of invitations!", 2, data.length());
            
            JSONObject moderatedB2Inv = getInvitation(moderatedIdBUSER_TWO, data);
            assertNotNull("Moderated invitation User 2 to Site B not present!", moderatedB2Inv);
            JSONObject moderatedB3Inv = getInvitation(moderatedIdBUSER_THREE, data);
            assertNotNull("Moderated invitation User 3 to Site B not present!", moderatedB3Inv);
            checkJSONInvitations(data);
        }
        
        // search SiteA by type Moderated: One Moderated User2
        {
            String siteAModeratedUrl = URL_SITES + "/" +shortNameSiteA + "/invitations?invitationType=MODERATED";
            Response response = sendRequest(new GetRequest(siteAModeratedUrl), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            // System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals(1, data.length());
            JSONObject invitation = data.getJSONObject(0);
            assertEquals("Wrong invitation type", "MODERATED", invitation.getString("invitationType"));
            JSONObject moderatedATwoInv = getInvitation(moderatedIdAUSER_TWO, data);
            assertNotNull("first is null", moderatedATwoInv);
        }
        
        // search SiteA by type Nominated: One Nominated User2
        {
            String siteANominatedUrl = URL_SITES + "/" +shortNameSiteA + "/invitations?invitationType=NOMINATED";
            Response response = sendRequest(new GetRequest(siteANominatedUrl), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            // System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals(1, data.length());
            JSONObject invitation = data.getJSONObject(0);
            assertEquals("Wrong invitation type", "NOMINATED", invitation.getString("invitationType"));
            JSONObject nominatedATwoInv = getInvitation(nominatedId, data);
            assertNotNull("first is null", nominatedATwoInv);
        }
        
        // search SiteB by userId 2: One Moderated User2
        {
            String siteBUser2Url = URL_SITES + "/" +shortNameSiteB + "/invitations?inviteeUserName=" + userTwo;
            Response response = sendRequest(new GetRequest(siteBUser2Url), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            // System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals(1, data.length());
            JSONObject invitation = data.getJSONObject(0);
            assertEquals("Wrong invitation user", userTwo, invitation.getString("inviteeUserName"));
            JSONObject moderatedBTwoInv = getInvitation(moderatedIdBUSER_TWO, data);
            assertNotNull("first is null", moderatedBTwoInv);
        }
        
        // search SiteB by userId 2: One Moderated User2
        {
            String siteBUser2Url = URL_SITES + "/" +shortNameSiteB + "/invitations?inviteeUserName=" + userThree;
            Response response = sendRequest(new GetRequest(siteBUser2Url), 200);
            JSONObject top = new JSONObject(response.getContentAsString());
            // System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");
            assertEquals(1, data.length());
            JSONObject invitation = data.getJSONObject(0);
            assertEquals("Wrong invitation user", userThree, invitation.getString("inviteeUserName"));
            JSONObject moderatedBThreeInv = getInvitation(moderatedIdBUSER_THREE, data);
            assertNotNull("first is null", moderatedBThreeInv);
        }

        // negative test - unknown resourceType
        {
            Response response = sendRequest(new GetRequest(URL_INVITATIONS + "?invitationType=MODERATED&resourceName="
                        + shortNameSiteA + "&resourceType=madeUpStuff"), 500);
            assertEquals(500, response.getStatus());
            JSONObject top = new JSONObject(response.getContentAsString());
            assertNotNull(top.getString("message"));
        }
    }

    private void checkJSONInvitations(JSONArray data) throws JSONException
    {
        for (int i = 0; i < data.length(); i++)
        {
            JSONObject invitation = data.getJSONObject(i);
            String userId = invitation.getString("inviteeUserName");
         
            // Check invitee info for Nominated invitation.
            Map<String, String> expectedProps = userProperties.get(userId);
            JSONObject invitee = invitation.getJSONObject("invitee");
            assertNotNull(invitee);
            assertEquals("User name is wrong for user: " + i, userId, invitee.getString("userName"));
            assertEquals("Avatar URI is wrong for user: " + i, expectedProps.get("avatar"), invitee.getString("avatar"));
            assertEquals("First name is wrong!", expectedProps.get("firstName"), invitee.getString("firstName"));
            assertEquals("Last name is wrong!", expectedProps.get("lastName"), invitee.getString("lastName"));
        }
    }

    private String makeAvatar(final NodeRef person)
    {
        nodeService.addAspect(person, ContentModel.ASPECT_PREFERENCES, null);
        ChildAssociationRef assoc = nodeService.createNode(person, ContentModel.ASSOC_PREFERENCE_IMAGE, avatarQName,
                    ContentModel.TYPE_CONTENT);
        NodeRef avatar = assoc.getChildRef();
        nodeService.createAssociation(person, avatar, ContentModel.ASSOC_AVATAR);
        return "api/node/" + avatar + "/content/thumbnails/avatar";
    }

    private String createNominatedInvitation(String siteName, String inviteeFirstName, String inviteeLastName,
                String inviteeEmail, String inviteeUserName, String inviteeRoleName, String serverPath,
                String acceptURL, String rejectURL) throws Exception
    {
        /*
         * Create a new nominated invitation
         */
        JSONObject newInvitation = new JSONObject();

        newInvitation.put("invitationType", "NOMINATED");
        newInvitation.put("inviteeRoleName", inviteeRoleName);
        if (inviteeUserName != null)
        {
            // nominate an existing user
            newInvitation.put("inviteeUserName", inviteeUserName);
        } else
        {
            // nominate someone else
            newInvitation.put("inviteeFirstName", inviteeFirstName);
            newInvitation.put("inviteeLastName", inviteeLastName);
            newInvitation.put("inviteeEmail", inviteeEmail);
        }
        newInvitation.put("serverPath", serverPath);
        newInvitation.put("acceptURL", acceptURL);
        newInvitation.put("rejectURL", rejectURL);

        Response response = sendRequest(new PostRequest(URL_SITES + "/" + siteName + "/invitations", newInvitation
                    .toString(), "application/json"), 201);
        JSONObject top = new JSONObject(response.getContentAsString());
        JSONObject data = top.getJSONObject("data");
        String inviteId = data.getString("inviteId");

        createdInvitations.add(new Tracker(inviteId, siteName));

        return inviteId;
    }

    private String createModeratedInvitation(String siteName, String inviteeComments, String inviteeUserName,
                String inviteeRoleName) throws Exception
    {
        /*
         * Create a new moderated invitation
         */
        JSONObject newInvitation = new JSONObject();

        newInvitation.put("invitationType", "MODERATED");
        newInvitation.put("inviteeRoleName", inviteeRoleName);
        newInvitation.put("inviteeComments", inviteeComments);
        newInvitation.put("inviteeUserName", inviteeUserName);
        Response response = sendRequest(new PostRequest(URL_SITES + "/" + siteName + "/invitations", newInvitation
                    .toString(), "application/json"), 201);
        JSONObject top = new JSONObject(response.getContentAsString());
        JSONObject data = top.getJSONObject("data");
        String inviteId = data.getString("inviteId");

        createdInvitations.add(new Tracker(inviteId, siteName));

        return inviteId;
    }

    private JSONObject getInvitation(String inviteId, JSONArray data) throws Exception
    {
        for (int i = 0; i < data.length(); i++)
        {
            JSONObject obj = data.getJSONObject(i);
            if (inviteId.equals(obj.getString("inviteId")))
            {
                return obj;
            }
        }
        return null;
    }
}
