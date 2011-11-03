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

package org.alfresco.repo.invitation;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.workflow.WorkflowAdminServiceImpl;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;

/**
 * Unit tests of Invitation Service
 */
public abstract class AbstractInvitationServiceImplTest extends BaseAlfrescoSpringTest
{
    private static final Log logger = LogFactory.getLog(AbstractInvitationServiceImplTest.class);
    
    private SiteService siteService;
    protected AuthenticationComponent authenticationComponent;
    private PersonService personService;
    protected InvitationService invitationService;
    private MailActionExecuter mailService;
    private boolean startSendEmails;
    protected InvitationServiceImpl invitationServiceImpl;
    protected WorkflowAdminServiceImpl workflowAdminService;
 
    protected final static String SITE_SHORT_NAME_INVITE = "InvitationTest";
    protected final static String SITE_SHORT_NAME_RED = "InvitationTestRed";
    protected final static String SITE_SHORT_NAME_BLUE = "InvitationTestBlue";
    public final static String PERSON_FIRSTNAME = "InvitationFirstName123";
    public final static String PERSON_FIRSTNAME_SPACES = "Invitation First\tName\n1\r2\r\n3";
    public final static String PERSON_LASTNAME = "InvitationLastName123";
    public final static String PERSON_LASTNAME_SPACES = "Invitation Last\tName\n1\r2\r\n3";
    public final static String PERSON_JOBTITLE = "JobTitle123";
    public final static String PERSON_ORG = "Organisation123";

    public final static String USER_MANAGER = "InvitationServiceManagerOne";
    public final static String USER_ONE = "InvitationServiceAlice";
    public final static String USER_TWO = "InvitationServiceBob";
    public final static String USER_EVE = "InvitationServiceEve";
    public final static String USER_NOEMAIL = "InvitationServiceNoEmail";
    public final static String USER_ONE_FIRSTNAME = "One";
    public final static String USER_ONE_LASTNAME = "Test";
    public final static String USER_ONE_EMAIL = USER_ONE + "@alfrescotesting.com";
    public final static String USER_TWO_EMAIL = USER_TWO + "@alfrescotesting.com";
    
    private Collection<String> enabledEngines;
    private Collection<String> visibleEngines;
    /**
     * Called during the transaction setup
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        this.invitationService = (InvitationService) this.applicationContext.getBean("InvitationService");
        this.siteService = (SiteService) this.applicationContext.getBean("SiteService");
        this.personService = (PersonService) this.applicationContext.getBean("PersonService");
        this.authenticationComponent = (AuthenticationComponent) this.applicationContext
                    .getBean("authenticationComponent");
        this.invitationServiceImpl = (InvitationServiceImpl) applicationContext.getBean("invitationService");
        this.workflowAdminService = (WorkflowAdminServiceImpl)applicationContext.getBean(WorkflowAdminServiceImpl.NAME); 
        
        this.startSendEmails = invitationServiceImpl.isSendEmails();
        
        this.enabledEngines = workflowAdminService.getEnabledEngines();
        this.visibleEngines = workflowAdminService.getVisibleEngines();

        invitationServiceImpl.setSendEmails(true);
        
        // TODO MER 20/11/2009 Bodge - turn off email sending to prevent errors
        // during unit testing
        // (or sending out email by accident from tests)
        mailService = (MailActionExecuter) ((ApplicationContextFactory) this.applicationContext
                .getBean("OutboundSMTP")).getApplicationContext().getBean("mail");
        mailService.setTestMode(true);
        
        
        createPerson(USER_MANAGER, USER_MANAGER + "@alfrescotesting.com", PERSON_FIRSTNAME, PERSON_LASTNAME);
        createPerson(USER_ONE, USER_ONE_EMAIL, USER_ONE_FIRSTNAME, USER_ONE_LASTNAME);
        createPerson(USER_TWO, USER_TWO + "@alfrescotesting.com", PERSON_FIRSTNAME, PERSON_LASTNAME);
        createPerson(USER_EVE, USER_EVE + "@alfrescotesting.com", PERSON_FIRSTNAME, PERSON_LASTNAME);
        createPerson(USER_NOEMAIL, null, USER_NOEMAIL, USER_NOEMAIL);

        this.authenticationComponent.setCurrentUser(USER_MANAGER);

        SiteInfo siteInfo = siteService.getSite(SITE_SHORT_NAME_INVITE);
        if (siteInfo == null)
        {
            siteInfo = siteService.createSite("InviteSitePreset", SITE_SHORT_NAME_INVITE, "InviteSiteTitle",
                        "InviteSiteDescription", SiteVisibility.MODERATED);
            
            siteService.setMembership(SITE_SHORT_NAME_INVITE, USER_NOEMAIL, SiteModel.SITE_MANAGER);
        }

        SiteInfo siteInfoRed = siteService.getSite(SITE_SHORT_NAME_RED);
        if (siteInfoRed == null)
        {
            siteService.createSite("InviteSiteRed", SITE_SHORT_NAME_RED, "InviteSiteTitle", "InviteSiteDescription",
                        SiteVisibility.MODERATED);
        }
        SiteInfo siteInfoBlue = siteService.getSite(SITE_SHORT_NAME_BLUE);
        if (siteInfoBlue == null)
        {
            siteService.createSite("InviteSiteBlue", SITE_SHORT_NAME_BLUE, "InviteSiteTitle", "InviteSiteDescription",
                        SiteVisibility.MODERATED);
        }

    }

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        // Make sure both workflow engines are enabled.and visible
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        workflowAdminService.setEnabledEngines(enabledEngines);
        workflowAdminService.setVisibleEngines(visibleEngines);
        
//        invitationServiceImpl.setSendEmails(startSendEmails);
//        siteService.deleteSite(SITE_SHORT_NAME_INVITE);
//        siteService.deleteSite(SITE_SHORT_NAME_RED);
//        siteService.deleteSite(SITE_SHORT_NAME_BLUE);
//        deletePersonByUserName(USER_ONE);
//        deletePersonByUserName(USER_TWO);
//        deletePersonByUserName(USER_EVE);
//        deletePersonByUserName(USER_MANAGER);
        super.onTearDownInTransaction();
    }

    /*
     * end of setup now for some real tests
     */

    /**
     * 
     */
    public void testConfiguration()
    {
        assertNotNull("Invitation service is null", invitationService);
    }

    /**
     * Test nominated user - new user
     * 
     * @throws Exception
     */
    public void testNominatedInvitationNewUser() throws Exception
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -1);
        Date startDate = calendar.getTime();

        String inviteeFirstName = PERSON_FIRSTNAME;
        String inviteeLastName = PERSON_LASTNAME;
        String inviteeEmail = "123@alfrescotesting.com";
        String inviteeUserName = null;
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        this.authenticationComponent.setCurrentUser(USER_MANAGER);

        NominatedInvitation nominatedInvitation = invitationService.inviteNominated(inviteeFirstName, inviteeLastName,
                    inviteeEmail, resourceType, resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);

        assertNotNull("nominated invitation is null", nominatedInvitation);
        String inviteId = nominatedInvitation.getInviteId();
        assertEquals("first name wrong", inviteeFirstName, nominatedInvitation.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, nominatedInvitation.getInviteeLastName());
        assertEquals("email name wrong", inviteeEmail, nominatedInvitation.getInviteeEmail());

        // Generated User Name should be returned
        inviteeUserName = nominatedInvitation.getInviteeUserName();
        assertNotNull("generated user name is null", inviteeUserName);
        // sentInviteDate should be set to today
        {
            Date sentDate = nominatedInvitation.getSentInviteDate();
            assertTrue("sentDate wrong - too early. Start Date: " +startDate +"\nSent Date: "+sentDate, sentDate.after(startDate));
            assertTrue("sentDate wrong - too lateStart Date: " +startDate +"\nSent Date: "+sentDate, sentDate.before(new Date(new Date().getTime() + 1)));
        }

        assertEquals("resource type name wrong", resourceType, nominatedInvitation.getResourceType());
        assertEquals("resource name wrong", resourceName, nominatedInvitation.getResourceName());
        assertEquals("role  name wrong", inviteeRole, nominatedInvitation.getRoleName());
        assertEquals("server path wrong", serverPath, nominatedInvitation.getServerPath());
        assertEquals("accept URL wrong", acceptUrl, nominatedInvitation.getAcceptUrl());
        assertEquals("reject URL wrong", rejectUrl, nominatedInvitation.getRejectUrl());

        /**
         * Now we have an invitation get it and check the details have been
         * returned correctly.
         */
        {
            NominatedInvitation invitation = (NominatedInvitation) invitationService.getInvitation(inviteId);

            assertNotNull("invitation is null", invitation);
            assertEquals("invite id wrong", inviteId, invitation.getInviteId());
            assertEquals("first name wrong", inviteeFirstName, invitation.getInviteeFirstName());
            assertEquals("last name wrong", inviteeLastName, invitation.getInviteeLastName());
            assertEquals("user name wrong", inviteeUserName, invitation.getInviteeUserName());
            assertEquals("resource type name wrong", resourceType, invitation.getResourceType());
            assertEquals("resource name wrong", resourceName, invitation.getResourceName());
            assertEquals("role  name wrong", inviteeRole, invitation.getRoleName());
            assertEquals("server path wrong", serverPath, invitation.getServerPath());
            assertEquals("accept URL wrong", acceptUrl, invitation.getAcceptUrl());
            assertEquals("reject URL wrong", rejectUrl, invitation.getRejectUrl());

            Date sentDate = invitation.getSentInviteDate();
            // sentInviteDate should be set to today
            assertTrue("sentDate wrong too early", sentDate.after(startDate));
            assertTrue("sentDate wrong - too late", sentDate.before(new Date(new Date().getTime() + 1)));
        }
        
        /**
         * Check the email itself, and check it
         *  is as we would expect it to be
         */
        {
           MimeMessage msg = mailService.retrieveLastTestMessage();
           
           assertEquals(1, msg.getAllRecipients().length);
           assertEquals(inviteeEmail, msg.getAllRecipients()[0].toString());
           
           assertEquals(1, msg.getFrom().length);
           assertEquals(USER_MANAGER + "@alfrescotesting.com", msg.getFrom()[0].toString());
           
           // Hasn't been sent, so no sent or received date
           assertNull("Not been sent yet", msg.getSentDate());
           assertNull("Not been sent yet", msg.getReceivedDate());
           
           // TODO - check some more details of the email
           assertTrue((msg.getSubject().indexOf("You have been invited to join the") != -1));
        }

        /**
         * Search for the new invitation
         */
        List<Invitation> invitations = invitationService.listPendingInvitationsForResource(resourceType, resourceName);
        assertTrue("invitations is empty", !invitations.isEmpty());

        NominatedInvitation firstInvite = (NominatedInvitation) invitations.get(0);
        assertEquals("invite id wrong", inviteId, firstInvite.getInviteId());
        assertEquals("first name wrong", inviteeFirstName, firstInvite.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, firstInvite.getInviteeLastName());
        assertEquals("user name wrong", inviteeUserName, firstInvite.getInviteeUserName());

        /**
         * Now accept the invitation
         */
        NominatedInvitation acceptedInvitation = (NominatedInvitation) invitationService.accept(firstInvite
                    .getInviteId(), firstInvite.getTicket());
        assertEquals("invite id wrong", firstInvite.getInviteId(), acceptedInvitation.getInviteId());
        assertEquals("first name wrong", inviteeFirstName, acceptedInvitation.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, acceptedInvitation.getInviteeLastName());
        assertEquals("user name wrong", inviteeUserName, acceptedInvitation.getInviteeUserName());

        List<Invitation> it4 = invitationService.listPendingInvitationsForResource(resourceType, resourceName);
        assertTrue("invitations is not empty", it4.isEmpty());

        /**
         * Now get the invitation that we accepted
         */
        NominatedInvitation acceptedInvitation2 = (NominatedInvitation) invitationService.getInvitation(firstInvite
                    .getInviteId());
        assertNotNull("get after accept does not return", acceptedInvitation2);

        /**
         * Now verify access control list
         */
        String roleName = siteService.getMembersRole(resourceName, inviteeUserName);
        assertEquals("role name wrong", roleName, inviteeRole);
        siteService.removeMembership(resourceName, inviteeUserName);
        
        
        /**
         * Check that system generated invitations can work as well
         */
        {
           Field faf = mailService.getClass().getDeclaredField("fromAddress");
           faf.setAccessible(true);
           String defaultFromAddress = (String)ReflectionUtils.getField(faf, mailService);
           
           AuthenticationUtil.setFullyAuthenticatedUser(USER_NOEMAIL);

           // Check invitiation
           NominatedInvitation nominatedInvitation2 = invitationService.inviteNominated(inviteeFirstName, inviteeLastName,
                 USER_TWO_EMAIL, resourceType, resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);

           assertNotNull("nominated invitation is null", nominatedInvitation2);
           inviteId = nominatedInvitation.getInviteId();
           assertEquals("first name wrong", inviteeFirstName, nominatedInvitation2.getInviteeFirstName());
           assertEquals("last name wrong", inviteeLastName, nominatedInvitation2.getInviteeLastName());
           assertEquals("email name wrong", USER_TWO_EMAIL, nominatedInvitation2.getInviteeEmail());
     
           // Check the email
           MimeMessage msg = mailService.retrieveLastTestMessage();
           
           assertEquals(1, msg.getAllRecipients().length);
           assertEquals(USER_TWO_EMAIL, msg.getAllRecipients()[0].toString());
           
           assertEquals(1, msg.getFrom().length);
           assertEquals(defaultFromAddress, msg.getFrom()[0].toString());           
        }
    }

    // TODO MER START
    /**
     * Test nominated user - new user who rejects invitation
     * 
     * @throws Exception
     */
    public void testNominatedInvitationNewUserReject() throws Exception
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -1);
        Date startDate = calendar.getTime();

        String inviteeFirstName = PERSON_FIRSTNAME;
        String inviteeLastName = PERSON_LASTNAME;
        String inviteeEmail = "123@alfrescotesting.com";
        String inviteeUserName = null;
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        this.authenticationComponent.setCurrentUser(USER_MANAGER);

        NominatedInvitation nominatedInvitation = invitationService.inviteNominated(inviteeFirstName, inviteeLastName,
                    inviteeEmail, resourceType, resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);

        assertNotNull("nominated invitation is null", nominatedInvitation);
        assertEquals("first name wrong", inviteeFirstName, nominatedInvitation.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, nominatedInvitation.getInviteeLastName());
        assertEquals("email name wrong", inviteeEmail, nominatedInvitation.getInviteeEmail());

        // Generated User Name should be returned
        inviteeUserName = nominatedInvitation.getInviteeUserName();
        assertNotNull("generated user name is null", inviteeUserName);
        // sentInviteDate should be set to today
        {
            Date sentDate = nominatedInvitation.getSentInviteDate();
            assertTrue("sentDate wrong - too earlyStart Date: " +startDate +"\nSent Date: "+sentDate, sentDate.after(startDate));
            assertTrue("sentDate wrong - too lateStart Date: " +startDate +"\nSent Date: "+sentDate, sentDate.before(new Date(new Date().getTime() + 1)));
        }

        /**
         * Now reject the invitation
         */
        NominatedInvitation rejectedInvitation = (NominatedInvitation) invitationService.reject(nominatedInvitation
                    .getInviteId(), "dont want it");
        assertEquals("invite id wrong", nominatedInvitation.getInviteId(), rejectedInvitation.getInviteId());
        assertEquals("first name wrong", inviteeFirstName, rejectedInvitation.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, rejectedInvitation.getInviteeLastName());
        assertEquals("user name wrong", inviteeUserName, rejectedInvitation.getInviteeUserName());

        List<Invitation> it4 = invitationService.listPendingInvitationsForResource(resourceType, resourceName);
        assertTrue("invitations is not empty", it4.isEmpty());

        /**
         * Now verify access control list inviteeUserName should not exist
         */
        String roleName = siteService.getMembersRole(resourceName, inviteeUserName);
        if (roleName != null)
        {
            fail("role has been set for a rejected user");
        }

        /**
         * Now verify that the generated user has been removed
         */
        if (personService.personExists(inviteeUserName))
        {
            fail("generated user has not been cleaned up");
        }
    }

    // TODO MER END

    /**
     * Test nominated user - new user Creates two separate users with two the
     * same email address.
     * 
     * @throws Exception
     */
    public void testNominatedInvitationNewUserSameEmails() throws Exception
    {
        String inviteeAFirstName = "John";
        String inviteeALastName = "Smith";

        String inviteeBFirstName = "Jane";
        String inviteeBLastName = "Smith";

        String inviteeEmail = "123@alfrescotesting.com";
        String inviteeAUserName = null;
        String inviteeBUserName = null;

        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        this.authenticationComponent.setCurrentUser(USER_MANAGER);

        NominatedInvitation nominatedInvitationA = invitationService.inviteNominated(inviteeAFirstName,
                    inviteeALastName, inviteeEmail, resourceType, resourceName, inviteeRole, serverPath, acceptUrl,
                    rejectUrl);

        assertNotNull("nominated invitation is null", nominatedInvitationA);
        String inviteAId = nominatedInvitationA.getInviteId();
        assertEquals("first name wrong", inviteeAFirstName, nominatedInvitationA.getInviteeFirstName());
        assertEquals("last name wrong", inviteeALastName, nominatedInvitationA.getInviteeLastName());
        assertEquals("email name wrong", inviteeEmail, nominatedInvitationA.getInviteeEmail());

        // Generated User Name should be returned
        inviteeAUserName = nominatedInvitationA.getInviteeUserName();
        assertNotNull("generated user name is null", inviteeAUserName);

        NominatedInvitation nominatedInvitationB = invitationService.inviteNominated(inviteeBFirstName,
                    inviteeBLastName, inviteeEmail, resourceType, resourceName, inviteeRole, serverPath, acceptUrl,
                    rejectUrl);

        assertNotNull("nominated invitation is null", nominatedInvitationB);
        String inviteBId = nominatedInvitationB.getInviteId();
        assertEquals("first name wrong", inviteeBFirstName, nominatedInvitationB.getInviteeFirstName());
        assertEquals("last name wrong", inviteeBLastName, nominatedInvitationB.getInviteeLastName());
        assertEquals("email name wrong", inviteeEmail, nominatedInvitationB.getInviteeEmail());

        // Generated User Name should be returned
        inviteeBUserName = nominatedInvitationB.getInviteeUserName();
        assertNotNull("generated user name is null", inviteeBUserName);
        assertFalse("generated user names are the same", inviteeAUserName.equals(inviteeBUserName));

        /**
         * Now accept the invitation
         */
        NominatedInvitation acceptedInvitationA = (NominatedInvitation) invitationService.accept(inviteAId,
                    nominatedInvitationA.getTicket());
        assertEquals("invite id wrong", inviteAId, acceptedInvitationA.getInviteId());
        assertEquals("first name wrong", inviteeAFirstName, acceptedInvitationA.getInviteeFirstName());
        assertEquals("last name wrong", inviteeALastName, acceptedInvitationA.getInviteeLastName());
        assertEquals("user name wrong", inviteeAUserName, acceptedInvitationA.getInviteeUserName());

        NominatedInvitation acceptedInvitationB = (NominatedInvitation) invitationService.accept(inviteBId,
                    nominatedInvitationB.getTicket());
        assertEquals("invite id wrong", inviteBId, acceptedInvitationB.getInviteId());
        assertEquals("first name wrong", inviteeBFirstName, acceptedInvitationB.getInviteeFirstName());
        assertEquals("last name wrong", inviteeBLastName, acceptedInvitationB.getInviteeLastName());
        assertEquals("user name wrong", inviteeBUserName, acceptedInvitationB.getInviteeUserName());

        /**
         * Now verify access control list
         */
        String roleNameA = siteService.getMembersRole(resourceName, inviteeAUserName);
        assertEquals("role name wrong", roleNameA, inviteeRole);
        String roleNameB = siteService.getMembersRole(resourceName, inviteeBUserName);
        assertEquals("role name wrong", roleNameB, inviteeRole);
        siteService.removeMembership(resourceName, inviteeAUserName);
        siteService.removeMembership(resourceName, inviteeBUserName);
    }

    /**
     * Test nominated user - new user with whitespace in name. Related to
     * ETHREEOH-3030.
     */
    public void testNominatedInvitationNewUserWhitespace() throws Exception
    {
        String inviteeFirstName = PERSON_FIRSTNAME_SPACES;
        String inviteeLastName = PERSON_LASTNAME_SPACES;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeEmail = "123@alfrescotesting.com";
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";
        String expectedUserName = (inviteeFirstName + "_" + inviteeLastName).toLowerCase();

        authenticationComponent.setCurrentUser(USER_MANAGER);

        NominatedInvitation nominatedInvitation = invitationService.inviteNominated(inviteeFirstName, inviteeLastName,
                    inviteeEmail, resourceType, resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);

        assertNotNull("nominated invitation is null", nominatedInvitation);
        assertEquals("Wrong username!", expectedUserName, nominatedInvitation.getInviteeUserName());

        String inviteId = nominatedInvitation.getInviteId();

        // Now we have an invitation get it and check the details have been
        // returned correctly.
        NominatedInvitation invitation = (NominatedInvitation) invitationService.getInvitation(inviteId);
        assertNotNull("invitation is null", invitation);
        assertEquals("first name wrong", inviteeFirstName, invitation.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, invitation.getInviteeLastName());
        assertEquals("user name wrong", expectedUserName, invitation.getInviteeUserName());

        // Now accept the invitation
        NominatedInvitation acceptedInvitation = (NominatedInvitation) invitationService.accept(invitation
                    .getInviteId(), invitation.getTicket());

        assertEquals("first name wrong", inviteeFirstName, acceptedInvitation.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, acceptedInvitation.getInviteeLastName());
        assertEquals("user name wrong", expectedUserName, acceptedInvitation.getInviteeUserName());

        // Now verify access control list
        String roleName = siteService.getMembersRole(resourceName, expectedUserName);
        assertEquals("role name wrong", roleName, inviteeRole);
        siteService.removeMembership(resourceName, expectedUserName);
    }

    /**
     * Create a Nominated Invitation (for existing user, USER_ONE) read it.
     * search for it cancel it search for it again (and fail to find it) Create
     * a Nominated Invitation read it. search for it reject it Create a
     * Nominated Invitation read it. accept it
     */
    public void testNominatedInvitationExistingUser() throws Exception
    {
        String inviteeUserName = USER_ONE;
        String inviteeEmail = USER_ONE_EMAIL;
        String inviteeFirstName = USER_ONE_FIRSTNAME;
        String inviteeLastName = USER_ONE_LASTNAME;

        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        authenticationComponent.setCurrentUser(USER_MANAGER);

        NominatedInvitation nominatedInvitation = invitationService.inviteNominated(inviteeUserName, resourceType,
                    resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);

        assertNotNull("nominated invitation is null", nominatedInvitation);
        String inviteId = nominatedInvitation.getInviteId();
        assertEquals("user name wrong", inviteeUserName, nominatedInvitation.getInviteeUserName());
        assertEquals("resource type name wrong", resourceType, nominatedInvitation.getResourceType());
        assertEquals("resource name wrong", resourceName, nominatedInvitation.getResourceName());
        assertEquals("role  name wrong", inviteeRole, nominatedInvitation.getRoleName());
        assertEquals("server path wrong", serverPath, nominatedInvitation.getServerPath());
        assertEquals("accept URL wrong", acceptUrl, nominatedInvitation.getAcceptUrl());
        assertEquals("reject URL wrong", rejectUrl, nominatedInvitation.getRejectUrl());

        // These values should be read from the person record
        assertEquals("first name wrong", inviteeFirstName, nominatedInvitation.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, nominatedInvitation.getInviteeLastName());
        assertEquals("email name wrong", inviteeEmail, nominatedInvitation.getInviteeEmail());

        /**
         * Now we have an invitation get it and check the details have been
         * returned correctly.
         */
        NominatedInvitation invitation = (NominatedInvitation) invitationService.getInvitation(inviteId);

        assertNotNull("invitation is null", invitation);
        assertEquals("invite id wrong", inviteId, invitation.getInviteId());
        assertEquals("user name wrong", inviteeUserName, nominatedInvitation.getInviteeUserName());
        assertEquals("resource type name wrong", resourceType, invitation.getResourceType());
        assertEquals("resource name wrong", resourceName, invitation.getResourceName());
        assertEquals("role  name wrong", inviteeRole, invitation.getRoleName());
        assertEquals("server path wrong", serverPath, invitation.getServerPath());
        assertEquals("accept URL wrong", acceptUrl, invitation.getAcceptUrl());
        assertEquals("reject URL wrong", rejectUrl, invitation.getRejectUrl());

        // These values should have been read from the DB
        assertEquals("first name wrong", inviteeFirstName, invitation.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, invitation.getInviteeLastName());
        assertEquals("email name wrong", inviteeEmail, invitation.getInviteeEmail());

        /**
         * Search for the new invitation
         */
        List<Invitation> invitations = invitationService.listPendingInvitationsForResource(resourceType, resourceName);
        assertTrue("invitations is empty", !invitations.isEmpty());

        NominatedInvitation firstInvite = (NominatedInvitation) invitations.get(0);
        assertEquals("invite id wrong", inviteId, firstInvite.getInviteId());
        assertEquals("first name wrong", inviteeFirstName, firstInvite.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, firstInvite.getInviteeLastName());
        assertEquals("user name wrong", inviteeUserName, firstInvite.getInviteeUserName());

        /**
         * Now cancel the invitation
         */
        NominatedInvitation canceledInvitation = (NominatedInvitation) invitationService.cancel(inviteId);
        assertEquals("invite id wrong", inviteId, canceledInvitation.getInviteId());
        assertEquals("first name wrong", inviteeFirstName, canceledInvitation.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, canceledInvitation.getInviteeLastName());
        assertEquals("user name wrong", inviteeUserName, canceledInvitation.getInviteeUserName());

        /**
         * Do the query again - should no longer find anything
         */
        List<Invitation> it2 = invitationService.listPendingInvitationsForResource(resourceType, resourceName);
        assertTrue("invitations is not empty", it2.isEmpty());

        /**
         * Now invite and reject
         */
        NominatedInvitation secondInvite = invitationService.inviteNominated(inviteeUserName, resourceType,
                    resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);

        NominatedInvitation rejectedInvitation = (NominatedInvitation) invitationService.cancel(secondInvite
                    .getInviteId());
        assertEquals("invite id wrong", secondInvite.getInviteId(), rejectedInvitation.getInviteId());
        assertEquals("user name wrong", inviteeUserName, rejectedInvitation.getInviteeUserName());

        List<Invitation> it3 = invitationService.listPendingInvitationsForResource(resourceType, resourceName);
        assertTrue("invitations is not empty", it3.isEmpty());

        /**
         * Now invite and accept
         */
        NominatedInvitation thirdInvite = invitationService.inviteNominated(inviteeUserName, resourceType,
                    resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);

        NominatedInvitation acceptedInvitation = (NominatedInvitation) invitationService.accept(thirdInvite
                    .getInviteId(), thirdInvite.getTicket());
        assertEquals("invite id wrong", thirdInvite.getInviteId(), acceptedInvitation.getInviteId());
        assertEquals("first name wrong", inviteeFirstName, acceptedInvitation.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName, acceptedInvitation.getInviteeLastName());
        assertEquals("user name wrong", inviteeUserName, acceptedInvitation.getInviteeUserName());

        List<Invitation> it4 = invitationService.listPendingInvitationsForResource(resourceType, resourceName);
        assertTrue("invitations is not empty", it4.isEmpty());

        /**
         * Now verify access control list
         */
        String roleName = siteService.getMembersRole(resourceName, inviteeUserName);
        assertEquals("role name wrong", inviteeRole, roleName);
        siteService.removeMembership(resourceName, inviteeUserName);
    }

    /**
     * Create a moderated invitation Get it Search for it Cancel it Create a
     * moderated invitation Reject the invitation Create a moderated invitation
     * Approve the invitation
     */
    public void testModeratedInvitation()
    {
        String inviteeUserName = USER_TWO;
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String comments = "please sir, let me in!";

        this.authenticationComponent.setCurrentUser(USER_TWO);
        ModeratedInvitation invitation = invitationService.inviteModerated(comments, inviteeUserName, resourceType,
                    resourceName, inviteeRole);

        assertNotNull("moderated invitation is null", invitation);
        String inviteId = invitation.getInviteId();
        assertEquals("user name wrong", inviteeUserName, invitation.getInviteeUserName());
        assertEquals("role  name wrong", inviteeRole, invitation.getRoleName());
        assertEquals("comments", comments, invitation.getInviteeComments());
        assertEquals("resource type name wrong", resourceType, invitation.getResourceType());
        assertEquals("resource name wrong", resourceName, invitation.getResourceName());

        /**
         * Now we have an invitation get it and check the details have been
         * returned correctly.
         */
        ModeratedInvitation mi2 = (ModeratedInvitation) invitationService.getInvitation(inviteId);
        assertEquals("invite id", inviteId, mi2.getInviteId());
        assertEquals("user name wrong", inviteeUserName, mi2.getInviteeUserName());
        assertEquals("role  name wrong", inviteeRole, mi2.getRoleName());
        assertEquals("comments", comments, mi2.getInviteeComments());
        assertEquals("resource type name wrong", resourceType, mi2.getResourceType());
        assertEquals("resource name wrong", resourceName, mi2.getResourceName());

        /**
         * Search for the new invitation
         */
        List<Invitation> invitations = invitationService.listPendingInvitationsForResource(resourceType, resourceName);
        assertTrue("invitations is empty", !invitations.isEmpty());

        ModeratedInvitation firstInvite = (ModeratedInvitation) invitations.get(0);
        assertEquals("invite id wrong", inviteId, firstInvite.getInviteId());

        /**
         * Cancel the invitation
         */
        ModeratedInvitation canceledInvitation = (ModeratedInvitation) invitationService.cancel(inviteId);
        assertEquals("invite id wrong", inviteId, canceledInvitation.getInviteId());
        assertEquals("comments wrong", comments, canceledInvitation.getInviteeComments());

        /**
         * Should now be no invitation
         */
        List<Invitation> inv2 = invitationService.listPendingInvitationsForResource(resourceType, resourceName);
        assertTrue("After cancel invitations is not empty", inv2.isEmpty());

        /**
         * New invitation
         */
        this.authenticationComponent.setCurrentUser(USER_TWO);
        ModeratedInvitation invite2 = invitationService.inviteModerated(comments, inviteeUserName, resourceType,
                    resourceName, inviteeRole);

        String secondInvite = invite2.getInviteId();

        this.authenticationComponent.setCurrentUser(USER_MANAGER);
        invitationService.reject(secondInvite, "This is a test reject");

        /**
         * New invitation
         */
        this.authenticationComponent.setCurrentUser(USER_TWO);
        ModeratedInvitation invite3 = invitationService.inviteModerated(comments, inviteeUserName, resourceType,
                    resourceName, inviteeRole);

        String thirdInvite = invite3.getInviteId();

        this.authenticationComponent.setCurrentUser(USER_MANAGER);
        invitationService.approve(thirdInvite, "Welcome in");

        /**
         * Now verify access control list
         */
        String roleName = siteService.getMembersRole(resourceName, inviteeUserName);
        assertEquals("role name wrong", inviteeRole, roleName);
        siteService.removeMembership(resourceName, inviteeUserName);

    }

    /**
     * Test the approval of a moderated invitation
     */
    public void testModeratedApprove()
    {
        String inviteeUserName = USER_TWO;
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String comments = "please sir, let me in!";

        /**
         * New invitation from User TWO
         */
        this.authenticationComponent.setCurrentUser(USER_TWO);
        ModeratedInvitation invitation = invitationService.inviteModerated(comments, inviteeUserName, resourceType,
                    resourceName, inviteeRole);

        String invitationId = invitation.getInviteId();

        /**
         * Negative test Attempt to approve without the necessary role
         */
        try
        {
            invitationService.approve(invitationId, "No Way Hosea!");
            assertTrue("excetion not thrown", false);

        }
        catch (Exception e)
        {
            // An exception should have been thrown
            e.printStackTrace();
            System.out.println(e.toString());
        }

        /**
         * Approve the invitation
         */
        this.authenticationComponent.setCurrentUser(USER_MANAGER);
        invitationService.approve(invitationId, "Come on in");

        /**
         * Now verify access control list contains user two
         */
        String roleName = siteService.getMembersRole(resourceName, inviteeUserName);
        assertEquals("role name wrong", inviteeRole, roleName);

        /**
         * Negative test attempt to approve an invitation that has aready been
         * approved
         */
        try
        {
            invitationService.approve(invitationId, "Have I not already done this?");
            assertTrue("duplicate approve excetion not thrown", false);
        }
        catch (Exception e)
        {
            // An exception should have been thrown
            e.printStackTrace();
            System.out.println(e.toString());
        }
        /**
         * Negative test User is already a member of the site
         */
        siteService.removeMembership(resourceName, inviteeUserName);
    }

    /**
     * Tests of Moderated Reject
     */
    public void testModeratedReject()
    {
        String inviteeUserName = USER_TWO;
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String comments = "please sir, let me in!";

        /**
         * New invitation from User TWO
         */
        this.authenticationComponent.setCurrentUser(USER_TWO);
        ModeratedInvitation invitation = invitationService.inviteModerated(comments, inviteeUserName, resourceType,
                    resourceName, inviteeRole);

        String invitationId = invitation.getInviteId();

        /**
         * Negative test Attempt to reject without the necessary role
         */
        try
        {
            invitationService.reject(invitationId, "No Way Hosea!");
            assertTrue("excetion not thrown", false);

        }
        catch (Exception e)
        {
            // An exception should have been thrown
            e.printStackTrace();
            System.out.println(e.toString());
        }

        /**
         * Reject the invitation
         */
        this.authenticationComponent.setCurrentUser(USER_MANAGER);
        invitationService.reject(invitationId, "Go away!");

        /**
         * Negative test attempt to approve an invitation that has been rejected
         */
        try
        {
            invitationService.approve(invitationId, "Have I not rejected this?");
            assertTrue("rejected invitation not working", false);
        }
        catch (Exception e)
        {
            // An exception should have been thrown
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }

    /**
     * Test search invitation
     */
    public void testSearchInvitation()
    {
        /**
         * Make up a tree of invitations and then search Resource, User,
         * Workflow 1) RED, One, Moderated 2) RED, One, Nominated 3) BLUE, One,
         * Nominated 4) RED, Two, Moderated
         */
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String comments = "please sir, let me in!";
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        this.authenticationComponent.setCurrentUser(USER_MANAGER);
        invitationService.inviteModerated(comments, USER_ONE, resourceType, SITE_SHORT_NAME_RED, inviteeRole);

        invitationService.inviteNominated(USER_ONE, resourceType, SITE_SHORT_NAME_RED, inviteeRole, serverPath,
                    acceptUrl, rejectUrl);

        NominatedInvitation invitationThree = invitationService.inviteNominated(USER_ONE, resourceType,
                    SITE_SHORT_NAME_BLUE, inviteeRole, serverPath, acceptUrl, rejectUrl);
        String threeId = invitationThree.getInviteId();

        invitationService.inviteModerated(comments, USER_TWO, resourceType, SITE_SHORT_NAME_RED, inviteeRole);

        /**
         * Search for invitations for BLUE
         */
        List<Invitation> resOne = invitationService.listPendingInvitationsForResource(ResourceType.WEB_SITE,
                    SITE_SHORT_NAME_BLUE);
        assertEquals("blue invites not 1", 1, resOne.size());
        assertEquals("blue id wrong", threeId, resOne.get(0).getInviteId());

        /**
         * Search for invitations for RED
         */
        List<Invitation> resTwo = invitationService.listPendingInvitationsForResource(ResourceType.WEB_SITE,
                    SITE_SHORT_NAME_RED);
        assertEquals("red invites not 3", 3, resTwo.size());

        /**
         * Search for invitations for USER_ONE
         */
        List<Invitation> resThree = invitationService.listPendingInvitationsForInvitee(USER_ONE);
        assertEquals("user one does not have 3 invitations", 3, resThree.size());

        /**
         * Search for invitations for USER_TWO
         */
        List<Invitation> resFour = invitationService.listPendingInvitationsForInvitee(USER_TWO);
        assertEquals("user two does not have 1 invitations", 1, resFour.size());

        /**
         * Search for user1's nominated invitations
         */
        InvitationSearchCriteriaImpl crit1 = new InvitationSearchCriteriaImpl();
        crit1.setInvitee(USER_ONE);
        crit1.setInvitationType(InvitationSearchCriteria.InvitationType.NOMINATED);

        List<Invitation> resFive = invitationService.searchInvitation(crit1);
        assertEquals("user one does not have 2 nominated", 2, resFive.size());

        /**
         * Search with an empty criteria - should find all open invitations
         */
        InvitationSearchCriteria crit2 = new InvitationSearchCriteriaImpl();
        invitationService.searchInvitation(crit2);
        assertTrue("search everything returned 0 elements", resFive.size() > 0);

        InvitationSearchCriteriaImpl crit3 = new InvitationSearchCriteriaImpl();
        crit3.setInviter(USER_MANAGER);
        crit3.setInvitationType(InvitationSearchCriteria.InvitationType.NOMINATED);

        List<Invitation> res3 = invitationService.searchInvitation(crit3);
        assertEquals("user one does not have 2 nominated", 2, res3.size());

    }

    public void test100Invites() throws Exception
    {
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        authenticationComponent.setCurrentUser(USER_MANAGER);

        // Create 1000 invites
        for (int i = 0; i < 1000; i++)
        {
            invitationService.inviteNominated(USER_ONE, resourceType, resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
        }
        
        // Invite USER_TWO
        NominatedInvitation invite = invitationService.inviteNominated(USER_TWO, resourceType, resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
        
        InvitationSearchCriteriaImpl query = new InvitationSearchCriteriaImpl();
        query.setInvitee(USER_TWO);
        
        long start = System.currentTimeMillis();
        List<Invitation> results = invitationService.searchInvitation(query);
        long end= System.currentTimeMillis();
        System.out.println("Invitation Search took " + (end - start) + "ms.");
        
        assertEquals(1, results.size());
        assertEquals(invite.getInviteId(), results.get(0).getInviteId());
        this.setComplete();
        this.endTransaction();

    }

    public void testGetInvitation()
    {
        try
        {
            /**
             * Get an invitation that does not exist.
             */
            invitationService.getInvitation("jbpm$99999999");
            fail("should have thrown an exception");
        }
        catch (Exception e)
        {
            // should have gone here
        }
    }

    private void createPerson(String userName, String emailAddress, String firstName, String lastName)
    {
        // if user with given user name doesn't already exist then create user
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            // create user
            this.authenticationService.createAuthentication(userName, "password".toCharArray());
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
}
