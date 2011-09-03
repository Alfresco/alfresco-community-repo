/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.invite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit Test to test Invite Web Script API
 * 
 * @author Glen Johnson at Alfresco dot com
 */
public class InviteServiceTest extends BaseWebScriptTest
{
    // member variables for service instances
    private AuthorityService authorityService;
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private SiteService siteService;
    private NodeService nodeService;
    private WorkflowService workflowService;
    private MutableAuthenticationDao mutableAuthenticationDao;
    private NamespaceService namespaceService;
    private TransactionService transactionService;

    // stores invitee email addresses, one entry for each "start invite" operation
    // invoked, so that resources created for each invitee for each test
    // can be removed in the tearDown() method
    private List<String> inviteeEmailAddrs;

    private static final String WF_DEFINITION_INVITE = WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME;

    private static final String USER_INVITER = "InviterUser";
    private static final String USER_INVITER_2 = "InviterUser2";
    private static final String INVITEE_FIRSTNAME = "InviteeFirstName";
    private static final String INVITEE_LASTNAME = "InviteeLastName";
    private static final String INVITER_EMAIL = "FirstName123.LastName123@email.com";
    private static final String INVITER_EMAIL_2 = "FirstNameabc.LastNameabc@email.com";
    private static final String INVITEE_EMAIL_DOMAIN = "alfrescotesting.com";
    private static final String INVITEE_EMAIL_PREFIX = "invitee";
    private static final String INVITEE_SITE_ROLE = SiteModel.SITE_COLLABORATOR;
    private static final String SITE_SHORT_NAME_INVITE_1 = "SiteOneInviteTest";
    private static final String SITE_SHORT_NAME_INVITE_2 = "SiteTwoInviteTest";
    private static final String SITE_SHORT_NAME_INVITE_3 = "SiteThreeInviteTest";

    private static final String URL_INVITE = "/api/invite";
    private static final String URL_INVITES = "/api/invites";

    private static final String INVITE_ACTION_START = "start";
    private static final String INVITE_ACTION_CANCEL = "cancel";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        /**
         * We don't want to be authenticated as 'system' but run as 'InviterUser', because then
         * 'system' will be the creator for the sites and 'inviterUser' will be a nobody.
         */
        AuthenticationUtil.clearCurrentSecurityContext();

        // get references to services
        this.authorityService = (AuthorityService) getServer().getApplicationContext().getBean("AuthorityService");
        this.authenticationService = (MutableAuthenticationService) getServer().getApplicationContext()
                .getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext()
                .getBean("AuthenticationComponent");
        this.personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");
        this.siteService = (SiteService) getServer().getApplicationContext().getBean("SiteService");
        this.nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        this.workflowService = (WorkflowService) getServer().getApplicationContext().getBean("WorkflowService");
        this.mutableAuthenticationDao = (MutableAuthenticationDao) getServer().getApplicationContext()
                .getBean("authenticationDao");
        this.namespaceService = (NamespaceService) getServer().getApplicationContext().getBean("NamespaceService");
        this.transactionService = (TransactionService) getServer().getApplicationContext()
                .getBean("TransactionService");
        
        configureMailExecutorForTestMode(this.getServer());
        
        // We're using a MailActionExecuter defined in outboundSMTP-test-context.xml which
        // sets the testMode property to true via spring injection. This will prevent emails
        // from being sent from within this test case.
        // This MailExecutorAction bean is named "test-mail" but is in all other respects equivalent to the
        // 'real' executer bean. It is automatically included during OutboundSMTP subsystem startup.

        // redeploy invite process definition in case it has been modified
        WorkflowDefinition inviteWfDefinition = this.workflowService.getDefinitionByName(
                "jbpm$" + WorkflowModelNominatedInvitation.WF_PROCESS_INVITE.toPrefixString(this.namespaceService));
        this.workflowService.undeployDefinition(inviteWfDefinition.id);
        ClassPathResource inviteWfResource = new ClassPathResource(
                "alfresco/workflow/invitation-nominated_processdefinition.xml");
        workflowService.deployDefinition(
                JBPMEngine.ENGINE_ID, inviteWfResource.getInputStream(), MimetypeMap.MIMETYPE_XML);
        
        // Create new invitee email address list
        this.inviteeEmailAddrs = new ArrayList<String>();

        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                //
                // various setup operations which need to be run as system user
                //
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        // Create inviter person
                        createPerson(PERSON_FIRSTNAME, PERSON_LASTNAME, USER_INVITER, INVITER_EMAIL);
                        
                        // Create inviter2 person
                        createPerson(PERSON_FIRSTNAME, PERSON_LASTNAME, USER_INVITER_2, INVITER_EMAIL_2);
                        
                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
                
                // The creation of sites is heavily dependent on the authenticated user.  We must ensure that,
                // when doing the runAs below, the user both 'runAs' and 'fullyAuthenticated'.  In order for
                // this to be the case, the security context MUST BE EMPTY now.  We could do the old
                // "defensive clear", but really there should not be any lurking authentications on this thread
                // after the context starts up.  If there are, that is a bug, and we fail explicitly here.
                String residuallyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
                assertNull(
                        "Residual authentication on context-initiating thread (this thread):" + residuallyAuthenticatedUser,
                        residuallyAuthenticatedUser);

                //
                // various setup operations which need to be run as inviter user
                //
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        // Create first site for Inviter to invite Invitee to
                        SiteInfo siteInfo = siteService.getSite(SITE_SHORT_NAME_INVITE_1);
                        if (siteInfo == null)
                        {
                            siteService.createSite("InviteSitePreset", SITE_SHORT_NAME_INVITE_1,
                                    "InviteSiteTitle", "InviteSiteDescription", SiteVisibility.PUBLIC);
                        }
                        
                        // Create second site for inviter to invite invitee to
                        siteInfo = siteService.getSite(SITE_SHORT_NAME_INVITE_2);
                        if (siteInfo == null)
                        {
                            siteService.createSite("InviteSitePreset", SITE_SHORT_NAME_INVITE_2,
                                    "InviteSiteTitle", "InviteSiteDescription", SiteVisibility.PUBLIC);
                        }

                        // Create third site for inviter to invite invitee to
                        siteInfo = InviteServiceTest.this.siteService.getSite(SITE_SHORT_NAME_INVITE_3);
                        if (siteInfo == null)
                        {
                            siteService.createSite(
                                "InviteSitePreset", SITE_SHORT_NAME_INVITE_3,
                                "InviteSiteTitle", "InviteSiteDescription", SiteVisibility.PUBLIC);
                        }
                        
                        // set inviter2's role on third site to collaborator
                        String inviterSiteRole = siteService.getMembersRole(SITE_SHORT_NAME_INVITE_3, USER_INVITER_2);
                        if ((inviterSiteRole == null) || (inviterSiteRole.equals(SiteModel.SITE_COLLABORATOR) == false))
                        {
                            siteService.setMembership(SITE_SHORT_NAME_INVITE_3, USER_INVITER_2, SiteModel.SITE_COLLABORATOR);
                        }

                        return null;
                    }
                }, USER_INVITER);

                // Do tests as inviter user
                InviteServiceTest.this.authenticationComponent.setCurrentUser(USER_INVITER);
                return null;
            }});
    }

    /**
     * This method turns off email-sending within the MailActionExecuter bean.
     */
    public static void configureMailExecutorForTestMode(TestWebScriptServer server)
    {
        // This test class depends on a MailActionExecuter bean which sends out emails
        // in a live system. We want to prevent these emails from being sent during
        // test execution.
        // To do that, we need to get at the outboundSMTP-context.xml and change its
        // "mail" MailActionExecuter bean to test mode. setTestMode(true) on that object
        // will turn off email sending.
        // But that bean is defined within a subsystem i.e. a child application context.
        
        // There are a number of ways we could do this, none of them perfect.
        //
        // 1. Expose the setTestMode(boolean) method in the subsystem API.
        //    We could have the "mail" bean implement a "TestModeable" interface and
        //    expose that through the proxy.
        //    But that would mean that the setTestMode method would be available in the
        //    live system too, which is not ideal.
        // 2. Replace the "mail" bean in outboundSMTP-context.xml with an alternative in a
        //    different subsystem context file as described in
        //    http://wiki.alfresco.com/wiki/Alfresco_Subsystems#Debugging_Alfresco_Subsystems
        //    But to do that, we'd have to reproduce all the spring config for that bean
        //    and add a testMode property. Again not ideal.
        // 3. Hack into the "mail" bean by programmatically going through the known applicationContext
        //    and bean structure. This is not ideal either, but it has no effect on product code
        //    and isolates all the hacking into this test class.
        //
        // Therefore we've decided to do [3].
        
        ChildApplicationContextFactory outboundSmptSubsystem
            = (ChildApplicationContextFactory)server.getApplicationContext().getBean("OutboundSMTP");
        ApplicationContext childAppCtxt = outboundSmptSubsystem.getApplicationContext();
        MailActionExecuter mailActionExecutor = (MailActionExecuter)childAppCtxt.getBean("mail");
        mailActionExecutor.setTestMode(true);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        //
        // run various teardown operations which need to be run as 'admin'
        //
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                RunAsWork<Object> runAsWork = new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        // delete invite sites
                        siteService.deleteSite(SITE_SHORT_NAME_INVITE_1);
                        siteService.deleteSite(SITE_SHORT_NAME_INVITE_2);
                        siteService.deleteSite(SITE_SHORT_NAME_INVITE_3);
                        
                        // delete the inviter
                        deletePersonByUserName(USER_INVITER);

                        // delete all invitee people
                        for (String inviteeEmail : InviteServiceTest.this.inviteeEmailAddrs)
                        {
                            //
                            // delete all people with given email address
                            //

                            Set<NodeRef> people = InviteServiceTest.this.personService.getPeopleFilteredByProperty(
                                    ContentModel.PROP_EMAIL, inviteeEmail);
                            for (NodeRef person : people)
                            {
                                String userName = DefaultTypeConverter.INSTANCE.convert(String.class,
                                        InviteServiceTest.this.nodeService.getProperty(person, ContentModel.PROP_USERNAME));

                                // delete person
                                deletePersonByUserName(userName);
                            }
                        }
                        
                        return null;
                    }
                };
                AuthenticationUtil.runAs(runAsWork, AuthenticationUtil.getSystemUserName());

                // cancel all active invite workflows
                WorkflowDefinition wfDef = InviteServiceTest.this.workflowService.getDefinitionByName(WF_DEFINITION_INVITE);
                List<WorkflowInstance> workflowList = InviteServiceTest.this.workflowService.getActiveWorkflows(wfDef.id);
                for (WorkflowInstance workflow : workflowList)
                {
                    InviteServiceTest.this.workflowService.cancelWorkflow(workflow.id);
                }
                return null;
            }});
    }

    private void addUserToGroup(String groupName, String userName)
    {
        // get the full name for the group
        String fullGroupName = this.authorityService.getName(
                AuthorityType.GROUP, groupName);

        // create group if it does not exist
        if (this.authorityService.authorityExists(fullGroupName) == false)
        {
            this.authorityService.createAuthority(AuthorityType.GROUP, fullGroupName);
        }

        // add the user to the group
        this.authorityService.addAuthority(fullGroupName, userName);
    }

    private void removeUserFromGroup(String groupName, String userName)
    {
        // get the full name for the group
        String fullGroupName = this.authorityService.getName(
                AuthorityType.GROUP, groupName);

        // remove user from the group
        this.authorityService.removeAuthority(fullGroupName, userName);

        // delete the group
        this.authorityService.deleteAuthority(fullGroupName);
    }

    public static String PERSON_FIRSTNAME = "FirstName123";
    public static String PERSON_LASTNAME = "LastName123";
    public static String PERSON_JOBTITLE = "JobTitle123";
    public static String PERSON_ORG = "Organisation123";
    
    
    private void createPerson(String firstName, String lastName, String userName, String emailAddress)
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
            personProps.put(ContentModel.PROP_FIRSTNAME, firstName);
            personProps.put(ContentModel.PROP_LASTNAME, lastName);
            personProps.put(ContentModel.PROP_EMAIL, emailAddress);
            personProps.put(ContentModel.PROP_JOBTITLE, PERSON_JOBTITLE);
            personProps.put(ContentModel.PROP_ORGANIZATION, PERSON_ORG);

            // create person node for user
            this.personService.createPerson(personProps);
        }
    }

    private void deletePersonByUserName(String userName)
    {
        // delete person node associated with given user name
        // if one exists
        if (this.personService.personExists(userName))
        {
            this.personService.deletePerson(userName);
        }
    }

    private JSONObject startInvite(String inviteeFirstName, String inviteeLastName, String inviteeEmail, String inviteeSiteRole,
            String siteShortName, int expectedStatus)
            throws Exception
    {
        this.inviteeEmailAddrs.add(inviteeEmail);

        // Inviter sends invitation to Invitee to join a Site
        String startInviteUrl = URL_INVITE + "/" + INVITE_ACTION_START
                + "?inviteeFirstName=" + inviteeFirstName + "&inviteeLastName="
                + inviteeLastName + "&inviteeEmail="
                + URLEncoder.encode(inviteeEmail) + "&siteShortName="
                + siteShortName + "&inviteeSiteRole=" + inviteeSiteRole
                + "&serverPath=" + "http://localhost:8081/share/"
                + "&acceptUrl=" + "page/accept-invite"
                + "&rejectUrl=" + "page/reject-invite";

        Response response = sendRequest(new GetRequest(startInviteUrl), expectedStatus);

        JSONObject result = new JSONObject(response.getContentAsString());

        return result;
    }

    private JSONObject startInvite(String inviteeFirstName,
            String inviteeLastName, String inviteeSiteRole, String siteShortName, int expectedStatus)
            throws Exception
    {
        String inviteeEmail = INVITEE_EMAIL_PREFIX + RandomStringUtils.randomAlphanumeric(6)
                + "@" + INVITEE_EMAIL_DOMAIN;
        
        return startInvite(inviteeFirstName, inviteeLastName, inviteeEmail, inviteeSiteRole, siteShortName,
                expectedStatus);
    }
    
    private JSONObject cancelInvite(String inviteId, int expectedStatus) throws Exception
    {
        String cancelInviteUrl = URL_INVITE + "/"
        + INVITE_ACTION_CANCEL + "?inviteId=" + inviteId;
        
        Response response = sendRequest(new GetRequest(cancelInviteUrl), expectedStatus);;
        JSONObject result = new JSONObject(response.getContentAsString());
        
        return result;
    }
    
    private JSONObject rejectInvite(String inviteId, String inviteTicket, int expectedStatus) throws Exception
    {
        // Invitee rejects invitation to a Site from Inviter
        String rejectInviteUrl = URL_INVITE + "/" + inviteId + "/" + inviteTicket + "/reject";
        Response response = sendRequest(new PutRequest(rejectInviteUrl, (byte[])null, null), expectedStatus);
        JSONObject result = new JSONObject(response.getContentAsString());
        return result;
    }

    private JSONObject getInvitesByInviteId(String inviteId, int expectedStatus)
            throws Exception
    {
        // construct get invites URL
        String getInvitesUrl = URL_INVITES + "?inviteId=" + inviteId;

        // invoke get invites web script
        Response response = sendRequest(new GetRequest(getInvitesUrl), expectedStatus);

        JSONObject result = new JSONObject(response.getContentAsString());

        return result;
    }

    private JSONObject getInvitesByInviterUserName(String inviterUserName,
            int expectedStatus) throws Exception
    {
        // construct get invites URL
        String getInvitesUrl = URL_INVITES + "?inviterUserName="
                + inviterUserName;

        // invoke get invites web script
        Response response = sendRequest(new GetRequest(getInvitesUrl), expectedStatus);

        JSONObject result = new JSONObject(response.getContentAsString());

        return result;
    }

    private JSONObject getInvitesByInviteeUserName(String inviteeUserName,
            int expectedStatus) throws Exception
    {
        // construct get invites URL
        String getInvitesUrl = URL_INVITES + "?inviteeUserName="
                + inviteeUserName;

        // invoke get invites web script
        Response response = sendRequest(new GetRequest(getInvitesUrl), expectedStatus);

        JSONObject result = new JSONObject(response.getContentAsString());

        return result;
    }

    private JSONObject getInvitesBySiteShortName(String siteShortName,
            int expectedStatus) throws Exception
    {
        // construct get invites URL
        String getInvitesUrl = URL_INVITES + "?siteShortName="
                + siteShortName;

        // invoke get invites web script
        Response response = sendRequest(new GetRequest(getInvitesUrl), expectedStatus);

        JSONObject result = new JSONObject(response.getContentAsString());

        return result;
    }

    public void testStartInvite() throws Exception
    {
        JSONObject result = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_SITE_ROLE,
                SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);

        assertEquals(INVITE_ACTION_START, result.get("action"));
        assertEquals(INVITEE_FIRSTNAME, result.get("inviteeFirstName"));
        assertEquals(INVITEE_LASTNAME, result.get("inviteeLastName"));
        assertEquals(this.inviteeEmailAddrs
                .get(this.inviteeEmailAddrs.size() - 1), result
                .get("inviteeEmail"));
        assertEquals(SITE_SHORT_NAME_INVITE_1, result.get("siteShortName"));
    }
    
    public void testStartInviteWhenInviteeIsAlreadyMemberOfSite()
        throws Exception
    {
        //
        // add invitee as member of site: SITE_SHORT_NAME_INVITE
        //
        
        String randomStr = RandomStringUtils.randomNumeric(6);
        final String inviteeUserName = "inviteeUserName" + randomStr;
        final String inviteeEmailAddr = INVITEE_EMAIL_PREFIX + randomStr
            + "@" + INVITEE_EMAIL_DOMAIN;
        
        // create person with invitee user name and invitee email address 
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                createPerson(INVITEE_FIRSTNAME, INVITEE_LASTNAME, inviteeUserName, inviteeEmailAddr);
                return null;
            }
    
        }, AuthenticationUtil.getSystemUserName());
        
        // add invitee person to site: SITE_SHORT_NAME_INVITE
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                
                InviteServiceTest.this.siteService.setMembership(
                        SITE_SHORT_NAME_INVITE_1, inviteeUserName,
                        INVITEE_SITE_ROLE);
                return null;
            }
            
        }, USER_INVITER);
        
        /**
         * Should conflict
         */
        startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, inviteeEmailAddr, INVITEE_SITE_ROLE, 
                SITE_SHORT_NAME_INVITE_1, Status.STATUS_CONFLICT);
        
        // Should go through
        startInvite(INVITEE_FIRSTNAME, "Belzebub", inviteeEmailAddr, INVITEE_SITE_ROLE, 
                SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);
        
        // Should go through
        startInvite("Lucifer", INVITEE_LASTNAME, inviteeEmailAddr, INVITEE_SITE_ROLE, 
                SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);
    }

//    public void testStartInviteWhenAlreadyInProgress()
//    throws Exception
//    {        
//        JSONObject result = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_SITE_ROLE,
//                SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);
//        
//        String inviteeEmail = (String) result.get("inviteeEmail");
//        
//        startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, inviteeEmail, INVITEE_SITE_ROLE,
//                SITE_SHORT_NAME_INVITE_1,  Status.STATUS_CONFLICT);
//    }
//
    public void testStartInviteForSameInviteeButTwoDifferentSites()
        throws Exception
    {        
        JSONObject result = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_SITE_ROLE,
                SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);
        
        String inviteeEmail = (String) result.get("inviteeEmail");
        
        startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, inviteeEmail, INVITEE_SITE_ROLE,
                SITE_SHORT_NAME_INVITE_2,  Status.STATUS_OK);
    }

    public void testCancelInvite() throws Exception
    {
        // inviter starts invite workflow
        JSONObject result = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_SITE_ROLE,
                SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);

        // get hold of invite ID of started invite
        String inviteId = result.getString("inviteId");

        // Inviter cancels pending invitation
        cancelInvite(inviteId, Status.STATUS_OK);
    }

    public void testAcceptInvite() throws Exception
    {
        // inviter starts invite (sends out invitation)
        JSONObject result = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_SITE_ROLE,
                SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);

        // get hold of invite ID and invite ticket of started invite
        String inviteId = result.getString("inviteId");
        String inviteTicket = result.getString("inviteTicket");

        // Invitee accepts invitation to a Site from Inviter
        String acceptInviteUrl = URL_INVITE + "/" + inviteId + "/" + inviteTicket + "/accept";
        sendRequest(new PutRequest(acceptInviteUrl, (byte[])null, null), Status.STATUS_OK);
        
        // Invitee attempts to accept the invitation again
        sendRequest(new PutRequest(acceptInviteUrl, (byte[])null, null), Status.STATUS_CONFLICT);
        
        // Invitee attempts to reject an invitation that has already been accepted.
        rejectInvite(inviteId, inviteTicket, Status.STATUS_CONFLICT);


        //
        // test that invitation represented by invite ID (of invitation started
        // above)
        // is no longer pending (as a result of the invitation having being
        // accepted)
        //

        // get pending invite matching inviteId from invite started above (run as inviter user)
        this.authenticationComponent.setCurrentUser(USER_INVITER);
        JSONObject getInvitesResult = getInvitesByInviteId(inviteId,
                Status.STATUS_OK);

        // there should no longer be any invites identified by invite ID pending
        //assertEquals(0, getInvitesResult.getJSONArray("invites").length());
    }

    public void testRejectInvite() throws Exception
    {
        // inviter starts invite (sends out invitation)
        JSONObject result = startInvite(INVITEE_FIRSTNAME, INVITEE_LASTNAME, INVITEE_SITE_ROLE,
                SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);

        // get hold of invite ID of started invite
        String inviteId = result.getString("inviteId");
        String inviteTicket = result.getString("inviteTicket");

        rejectInvite(inviteId, inviteTicket, Status.STATUS_OK);
        
        // Negative test 
        rejectInvite(inviteId, inviteTicket, Status.STATUS_CONFLICT);

        //
        // test that invite represented by invite ID (of invitation started
        // above)
        // is no longer pending (as a result of the invitation having being
        // rejected)
        //

        // get pending invite matching inviteId from invite started above (run as inviter user)
        this.authenticationComponent.setCurrentUser(USER_INVITER);
        JSONObject getInvitesResult = getInvitesByInviteId(inviteId, Status.STATUS_OK);

        // there should no longer be any invites identified by invite ID pending
        // assertEquals(0, getInvitesResult.getJSONArray("invites").length());
        
        
    }

    public void testGetInvitesByInviteId() throws Exception
    {
        // inviter starts invite workflow
        JSONObject startInviteResult = startInvite(INVITEE_FIRSTNAME,
                INVITEE_LASTNAME, INVITEE_SITE_ROLE, SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);

        // get hold of workflow ID of started invite workflow instance

        String inviteId = startInviteResult.getString("inviteId");

        assertEquals(true, ((inviteId != null) && (inviteId.length() != 0)));

        // get pending invite matching inviteId from invite started above
        JSONObject getInvitesResult = getInvitesByInviteId(inviteId,
                Status.STATUS_OK);
        
        assertEquals(getInvitesResult.getJSONArray("invites").length(), 1);

        JSONObject inviteJSONObj = getInvitesResult.getJSONArray("invites").getJSONObject(0);

        assertEquals(inviteId, inviteJSONObj.get("inviteId"));
    }

    public void testGetInvitesByInviterUserName() throws Exception
    {
        // inviter starts invite workflow
        startInvite(INVITEE_FIRSTNAME,
                INVITEE_LASTNAME, INVITEE_SITE_ROLE, SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);

        // get pending invites matching inviter user name used in invite started
        // above
        JSONObject getInvitesResult = getInvitesByInviterUserName(USER_INVITER,
                Status.STATUS_OK);

        assertEquals(true, getInvitesResult.length() > 0);

//        JSONObject inviteJSONObj = getInvitesResult.getJSONArray("invites").getJSONObject(0);

//        assertEquals(USER_INVITER, inviteJSONObj.getJSONObject("inviter").get("userName"));
    }

    public void testGetInvitesByInviteeUserName() throws Exception
    {
        // inviter starts invite workflow
        JSONObject startInviteResult = startInvite(INVITEE_FIRSTNAME,
                INVITEE_LASTNAME, INVITEE_SITE_ROLE, SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);

        // get hold of invitee user name property of started invite workflow
        // instance
        String inviteeUserName = startInviteResult.getString("inviteeUserName");

        assertEquals(true, ((inviteeUserName != null) && (inviteeUserName
                .length() != 0)));

        // get pending invites matching invitee user name from invite started
        // above
        JSONObject getInvitesResult = getInvitesByInviteeUserName(
                inviteeUserName, Status.STATUS_OK);

        assertEquals(true, getInvitesResult.length() > 0);

        JSONObject inviteJSONObj = getInvitesResult.getJSONArray("invites").getJSONObject(0);

        assertEquals(inviteeUserName, inviteJSONObj.getJSONObject("invitee").get("userName"));
    }

    public void testGetInvitesBySiteShortName() throws Exception
    {
        // inviter starts invite workflow
        JSONObject startInviteResult = startInvite(INVITEE_FIRSTNAME,
                INVITEE_LASTNAME, INVITEE_SITE_ROLE, SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);

        // get hold of site short name property of started invite workflow
        // instance
        String siteShortName = startInviteResult.getString("siteShortName");

        assertEquals(true,
                ((siteShortName != null) && (siteShortName.length() != 0)));

        // get pending invites matching site short name from invite started
        // above
        JSONObject getInvitesResult = getInvitesBySiteShortName(siteShortName,
                Status.STATUS_OK);

        assertEquals(true, getInvitesResult.getJSONArray("invites").length() > 0);

        JSONObject inviteJSONObj = getInvitesResult.getJSONArray("invites").getJSONObject(0);

        assertEquals(siteShortName, inviteJSONObj.getJSONObject("site").get("shortName"));
    }
    
    public void testStartInviteForbiddenWhenInviterNotSiteManager() throws Exception
    {
        // inviter2 starts invite workflow, but he/she is not the site manager of the given site
        AuthenticationUtil.setFullyAuthenticatedUser(USER_INVITER_2);
        startInvite(INVITEE_FIRSTNAME,
                INVITEE_LASTNAME, INVITEE_SITE_ROLE, SITE_SHORT_NAME_INVITE_3, Status.STATUS_FORBIDDEN);
    }
    
    public void testCancelInviteForbiddenWhenInviterNotSiteManager() throws Exception
    {
        // inviter (who is Site Manager of the given site) starts invite workflow
        JSONObject result = startInvite(INVITEE_FIRSTNAME,
                INVITEE_LASTNAME, INVITEE_SITE_ROLE, SITE_SHORT_NAME_INVITE_3, Status.STATUS_OK);
        
        // get hold of invite ID of started invite
        String inviteId = result.getString("inviteId");
        
        // when inviter 2 (who is not Site Manager of the given site) tries to cancel invite
        // http status FORBIDDEN must be returned
        AuthenticationUtil.setFullyAuthenticatedUser(USER_INVITER_2);
        cancelInvite(inviteId, Status.STATUS_FORBIDDEN);
    }
    
    public void testInviteeResourcesDeletedUponRejectWhenNoInvitePending() throws Exception
    {
        // inviter starts invite workflow
        JSONObject result = startInvite(INVITEE_FIRSTNAME,
                INVITEE_LASTNAME, INVITEE_SITE_ROLE, SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);
        
        // get hold of properties of started invite
        String inviteId = result.getString("inviteId");
        String inviteTicket = result.getString("inviteTicket");
        final String inviteeUserName = result.getString("inviteeUserName");
        
        rejectInvite(inviteId, inviteTicket, Status.STATUS_OK);
        
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(
                        new RetryingTransactionCallback<Void>()
                        {
                            public Void execute() throws Throwable
                            {
                                assertEquals(false, mutableAuthenticationDao.userExists(inviteeUserName));
                                assertEquals(false, personService.personExists(inviteeUserName));
                                return null;
                            }
                        });
                return null;
            }
    
        }, AuthenticationUtil.getSystemUserName());
    }
    
    public void testInviteeResourcesNotDeletedUponRejectWhenInvitesPending() throws Exception
    {
        // inviter invites invitee to site 1
        JSONObject result = startInvite(INVITEE_FIRSTNAME,
                INVITEE_LASTNAME, INVITEE_SITE_ROLE, SITE_SHORT_NAME_INVITE_1, Status.STATUS_OK);
        
        // get hold of properties of started invite
        String invite1Id = result.getString("inviteId");
        String invite1Ticket = result.getString("inviteTicket");
        String inviteeEmail = result.getString("inviteeEmail");
        final String inviteeUserName = result.getString("inviteeUserName");
        
        // inviter invites invitee to site 2
        startInvite(INVITEE_FIRSTNAME,
                INVITEE_LASTNAME, inviteeEmail, INVITEE_SITE_ROLE, SITE_SHORT_NAME_INVITE_2, Status.STATUS_OK);
        
        rejectInvite(invite1Id, invite1Ticket, Status.STATUS_OK);
        
        boolean inviteeUserExists = AuthenticationUtil.runAs(new RunAsWork<Boolean>()
        {
            public Boolean doWork() throws Exception
            {
                RetryingTransactionHelper tranHelper = transactionService.getRetryingTransactionHelper();
                Boolean result = tranHelper.doInTransaction(new RetryingTransactionCallback<Boolean>()
                {
                    public Boolean execute() throws Throwable
                    {
                        Boolean result = mutableAuthenticationDao.userExists(inviteeUserName);
                        return result;
                    }
                });                
                
                return result;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        // test that the invitee's user account still exists (has not been deleted
        assertEquals(true, inviteeUserExists);
        
        boolean inviteePersonExists = AuthenticationUtil.runAs(new RunAsWork<Boolean>()
        {
                public Boolean doWork() throws Exception
                {
                    Boolean result = personService.personExists(inviteeUserName);
    
                    return result;
                }
        }, AuthenticationUtil.getSystemUserName());
        
        assertEquals(true, inviteePersonExists);
    }
    
    /**
     * https://issues.alfresco.com/jira/browse/ETHREEOH-520
     */
    public void testETHREEOH_520()
        throws Exception
    {
        final String userName = "userInviteServiceTest" + GUID.generate();
        final String emailAddress = " ";
        
        // Create a person with a blank email address and 
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                createPerson(PERSON_FIRSTNAME, PERSON_LASTNAME, userName, " ");
                return null;
            }
    
        }, AuthenticationUtil.getSystemUserName());
        
        // Try and add an existing person to the site with no email address
        // Should return bad request since the email address has not been provided
        startInvite(PERSON_FIRSTNAME, PERSON_LASTNAME, emailAddress, INVITEE_SITE_ROLE, SITE_SHORT_NAME_INVITE_1, 400);
    }
}
