/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.invitation;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.processor.TemplateServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.workflow.WorkflowAdminServiceImpl;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;
import org.alfresco.service.cmr.invitation.InvitationExceptionUserError;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.email.ExtendedMailActionExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;


/**
 * Unit tests of Invitation Service
 */
@Transactional
public abstract class AbstractInvitationServiceImplTest extends BaseAlfrescoSpringTest
{
    private static final String TEST_REJECT_URL = "testRejectUrl";

    private static final String TEST_ACCEPT_URL = "testAcceptUrl";

    private static final String TEST_SERVER_PATH = "testServerPath";

    private static final Log logger = LogFactory.getLog(AbstractInvitationServiceImplTest.class);
    
    private SiteService siteService;
    private PersonService personService;
    protected InvitationService invitationService;
    private MailActionExecuter mailService;
    private boolean startSendEmails;
    protected InvitationServiceImpl invitationServiceImpl;
    protected WorkflowAdminServiceImpl workflowAdminService;
    private TemplateService templateService;
 
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

    @Before
    public void before() throws Exception
    {
        super.before();
        this.invitationService = (InvitationService) this.applicationContext.getBean("InvitationService");
        this.siteService = (SiteService) this.applicationContext.getBean("SiteService");
        this.personService = (PersonService) this.applicationContext.getBean("PersonService");
        this.authenticationComponent = (AuthenticationComponent) this.applicationContext
                    .getBean("authenticationComponent");
        this.invitationServiceImpl = (InvitationServiceImpl) applicationContext.getBean("invitationService");
        this.workflowAdminService = (WorkflowAdminServiceImpl)applicationContext.getBean(WorkflowAdminServiceImpl.NAME);

        this.templateService = (TemplateServiceImpl)applicationContext.getBean("templateService");
        
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

    @After
    public void after() throws Exception
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
        super.after();
    }

    /*
     * end of setup now for some real tests
     */

    @Test
    public void testConfiguration()
    {
        assertNotNull("Invitation service is null", invitationService);
    }
   
    /**
     * MNT-9101 An internal user account (disabled) should not be deleted if an
     * associated nominated invitation is cancelled.
     * 
     * @throws Exception
     */
    @Test
    public void testInternalUserNotDeletedAfterInviteCancelled() throws Exception
    {
        // Disable our existing User
        boolean enabled = authenticationService.getAuthenticationEnabled(USER_ONE);
        assertTrue("User One authentication disabled", enabled);
        authenticationService.setAuthenticationEnabled(USER_ONE, false);
        enabled = authenticationService.getAuthenticationEnabled(USER_ONE);
        assertTrue("User One authentication enabled", !enabled);

        String inviteeUserName = USER_ONE;
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        this.authenticationComponent.setCurrentUser(USER_MANAGER);

        // Invite our existing user
        try
        {
            invitationService.inviteNominated(inviteeUserName, resourceType, resourceName, inviteeRole, acceptUrl, rejectUrl);
            fail("An exception of type " + InvitationExceptionUserError.class.getName() + " should be thrown");
        }
        catch (Exception ex)
        {
            assertTrue("Incorrect exception was thrown", ex instanceof InvitationExceptionUserError);
        }
       

        // Our User and associated Authentication still exists
        assertNotNull("User Exists", personService.getPersonOrNull(USER_ONE));
        assertTrue("Authentication Exists", authenticationService.authenticationExists(USER_ONE));
    }

    /**
     * Ensure that an External user account is deleted when an invite is
     * cancelled
     * 
     * @throws Exception
     */
    @Test
    public void testExternalUserDeletedAfterInviteCancelled() throws Exception
    {
        String inviteeFirstName = PERSON_FIRSTNAME;
        String inviteeLastName = PERSON_LASTNAME;
        String inviteeEmail = "123@alfrescotesting.com";
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        this.authenticationComponent.setCurrentUser(USER_MANAGER);

        NominatedInvitation nominatedInvitation = invitationService.inviteNominated(
                inviteeFirstName, inviteeLastName, inviteeEmail, resourceType, resourceName,
                inviteeRole, serverPath, acceptUrl, rejectUrl);

        String inviteeUsername = nominatedInvitation.getInviteeUserName();

        invitationService.cancel(nominatedInvitation.getInviteId());

        // Our User and Authentication has been removed
        assertNull("Person deleted", personService.getPersonOrNull(inviteeUsername));
        assertFalse("Authentication deleted",
                authenticationService.authenticationExists(inviteeUsername));
    }

    /**
     * Test nominated user - new user
     * 
     * @throws Exception
     */
    @Test
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
        AuthenticationUtil.setFullyAuthenticatedUser(inviteeUserName);
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
            Field faf;
            if (mailService instanceof ExtendedMailActionExecutor)
            {
                faf = mailService.getClass().getSuperclass().getDeclaredField("fromDefaultAddress");
            }
            else
            {
                faf = mailService.getClass().getDeclaredField("fromDefaultAddress");
            }
            faf.setAccessible(true);
            String defaultFromAddress = (String) ReflectionUtils.getField(faf, mailService);
           
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
    @Test
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
    @Test
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
        AuthenticationUtil.setFullyAuthenticatedUser(nominatedInvitationA.getInviteeUserName());
        NominatedInvitation acceptedInvitationA = (NominatedInvitation) invitationService.accept(inviteAId,
                    nominatedInvitationA.getTicket());
        assertEquals("invite id wrong", inviteAId, acceptedInvitationA.getInviteId());
        assertEquals("first name wrong", inviteeAFirstName, acceptedInvitationA.getInviteeFirstName());
        assertEquals("last name wrong", inviteeALastName, acceptedInvitationA.getInviteeLastName());
        assertEquals("user name wrong", inviteeAUserName, acceptedInvitationA.getInviteeUserName());

        AuthenticationUtil.setFullyAuthenticatedUser(nominatedInvitationB.getInviteeUserName());
        NominatedInvitation acceptedInvitationB = (NominatedInvitation) invitationService.accept(inviteBId,
                    nominatedInvitationB.getTicket());
        assertEquals("invite id wrong", inviteBId, acceptedInvitationB.getInviteId());
        assertEquals("first name wrong", inviteeBFirstName, acceptedInvitationB.getInviteeFirstName());
        assertEquals("last name wrong", inviteeBLastName, acceptedInvitationB.getInviteeLastName());
        assertEquals("user name wrong", inviteeBUserName, acceptedInvitationB.getInviteeUserName());

        /**
         * Now verify access control list
         */
        AuthenticationUtil.setFullyAuthenticatedUser(USER_MANAGER);
        String roleNameA = siteService.getMembersRole(resourceName, inviteeAUserName);
        assertEquals("role name wrong", roleNameA, inviteeRole);
        String roleNameB = siteService.getMembersRole(resourceName, inviteeBUserName);
        assertEquals("role name wrong", roleNameB, inviteeRole);
        siteService.removeMembership(resourceName, inviteeAUserName);
        siteService.removeMembership(resourceName, inviteeBUserName);
    }

    @Test
    public void testMNT11775() throws Exception
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
        ModeratedInvitation invitation = invitationService.inviteModerated(comments, inviteeUserName, resourceType, resourceName, inviteeRole);

        String invitationId = invitation.getInviteId();

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
            fail("rejected invitation not working");
        }
        catch (Exception e)
        {
            // An exception should have been thrown
            e.printStackTrace();
            System.out.println(e.toString());
        }

        /**
         * process email message template
         */
        try
        {
            // Build our model
            Map<String, Serializable> model = new HashMap<String, Serializable>(8, 1.0f);
            model.put("resourceName", resourceName);
            model.put("resourceType", resourceType);
            model.put("inviteeRole", inviteeRole);
            model.put("reviewComments", "Go away!");
            model.put("inviteeUserName", inviteeUserName);

            // Process the template
            String emailMsg = templateService.processTemplate("freemarker", "/alfresco/bootstrap/invite/moderated-reject-email.ftl", model);

            assertNotNull("Email message is null", emailMsg);
            assertTrue("Email message doesn't contain review comment", emailMsg.contains("Go away!"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.toString());
            fail("Process email message template exception");
        }
    }
    
    /**
     * MNT-15614 Site with name "IT" cannot be managed properly
     * 
     * @throws Exception
     */
    @Test
    public void test_MNT15614() throws Exception
    {
        String[] siteNames = {"it", "site", "GROUP"};
        String inviteeUserName = USER_ONE;
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;

        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";
        
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        for (String siteName : siteNames)
        {
            SiteInfo siteInfoRed = siteService.getSite(siteName);
            if (siteInfoRed == null)
            {
                siteService.createSite("InviteSitePreset", siteName, "InviteSiteTitle",
                        "InviteSiteDescription", SiteVisibility.MODERATED);
            }
            assertEquals( SiteModel.SITE_MANAGER, siteService.getMembersRole(siteName, AuthenticationUtil.getAdminUserName()));

            // Invite user
            NominatedInvitation nominatedInvitation = invitationService.inviteNominated(
                    inviteeUserName, resourceType, siteName, inviteeRole, acceptUrl, rejectUrl);
            assertNotNull("nominated invitation is null", nominatedInvitation);
        }
    }
    
    /**
     * Test nominated user - new user with whitespace in name. Related to
     * ETHREEOH-3030.
     */
    @Test
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
        expectedUserName = expectedUserName.replaceAll("\\s+", "_");
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
        AuthenticationUtil.setFullyAuthenticatedUser(invitation.getInviteeUserName());
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
     * If requireAcceptance=true skip to C1
     * 
     * A1. Create a Nominated Invitation (for existing user, USER_ONE)
     * A2. Read it
     * A3. Search for it.
     * A4. Cancel it
     * A5. Search for it again (and fail to find it)
     * 
     * B1. Create a Nominated Invitation
     * B2. Read it
     * B3. Search for it
     * B4. Reject it
     * 
     * C1. Create a Nominated Invitation
     * C2. Read it
     * C3. Accept it
     * C4. Verify ACL
     * 
     * @param requireAcceptance true if a workflow requiring acceptance is being used
     * @throws Exception
     */
    protected void testNominatedInvitationExistingUser(boolean requireAcceptance) throws Exception
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

        if (requireAcceptance)
        {
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
        }

        /**
         * Now invite and accept
         */
        NominatedInvitation thirdInvite = invitationService.inviteNominated(inviteeUserName, resourceType,
                    resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
        if (requireAcceptance)
        {
            NominatedInvitation acceptedInvitation = (NominatedInvitation) invitationService.accept(thirdInvite
                        .getInviteId(), thirdInvite.getTicket());
            assertEquals("invite id wrong", thirdInvite.getInviteId(), acceptedInvitation.getInviteId());
            assertEquals("first name wrong", inviteeFirstName, acceptedInvitation.getInviteeFirstName());
            assertEquals("last name wrong", inviteeLastName, acceptedInvitation.getInviteeLastName());
            assertEquals("user name wrong", inviteeUserName, acceptedInvitation.getInviteeUserName());
        }

        List<Invitation> it4 = invitationService.listPendingInvitationsForResource(resourceType, resourceName);
        assertTrue("invitations is not empty", it4.isEmpty());

        /**
         * Now verify access control list
         */
        String roleName = siteService.getMembersRole(resourceName, inviteeUserName);
        assertEquals("role name wrong", inviteeRole, roleName);
        siteService.removeMembership(resourceName, inviteeUserName);
    }
    
    @Test
    public void testNominatedInvitationExistingUser() throws Exception
    {
        this.invitationServiceImpl.setNominatedInvitationWorkflowId(
                WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI_INVITE);
        testNominatedInvitationExistingUser(true);
    }

    /**
     * Create a moderated invitation Get it Search for it Cancel it Create a
     * moderated invitation Reject the invitation Create a moderated invitation
     * Approve the invitation
     */
    @Test
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
     * Create a moderated invitation for workspace client Get it Search for it Cancel it
     */
    @Test
    public void testWorkspaceModeratedInvitation()
    {
        String inviteeUserName = USER_TWO;
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String comments = "please sir, let me in!";

        this.authenticationComponent.setCurrentUser(USER_TWO);
        ModeratedInvitation invitation = invitationService.inviteModerated(comments, inviteeUserName, resourceType,
                    resourceName, inviteeRole, "workspace");

        assertNotNull("moderated invitation is null", invitation);
        String inviteId = invitation.getInviteId();
        assertEquals("user name wrong", inviteeUserName, invitation.getInviteeUserName());
        assertEquals("role  name wrong", inviteeRole, invitation.getRoleName());
        assertEquals("comments", comments, invitation.getInviteeComments());
        assertEquals("resource type name wrong", resourceType, invitation.getResourceType());
        assertEquals("resource name wrong", resourceName, invitation.getResourceName());
        assertEquals("client name wrong", "workspace", invitation.getClientName());

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
        ModeratedInvitation invite3 = invitationService.inviteModerated(comments, inviteeUserName, resourceType,
                resourceName, inviteeRole);
        assertEquals("client name wrong", null, invite3.getClientName());

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
    @Test
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
            fail("excetion not thrown");

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
            fail("duplicate approve excetion not thrown");
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
    @Test
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
            fail("excetion not thrown");

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
            fail("rejected invitation not working");
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
    @Test
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
         * Search for invitations for BLUE - no pending invite since the user is added directly without waiting for the user to accept it
         */
        List<Invitation> resOne = invitationService.listPendingInvitationsForResource(ResourceType.WEB_SITE,
                    SITE_SHORT_NAME_BLUE);
        assertEquals("blue invites not 0", 0, resOne.size());

        /**
         * Search for invitations for RED - no pending nominated invites since the user is added directly without waiting for the user to accept it
         */
        List<Invitation> resTwo = invitationService.listPendingInvitationsForResource(ResourceType.WEB_SITE,
                    SITE_SHORT_NAME_RED);
        assertEquals("red invites not 2", 2, resTwo.size());

        /**
         * Search for invitations for USER_ONE
         */
        List<Invitation> resThree = invitationService.listPendingInvitationsForInvitee(USER_ONE);
        assertEquals("user one does not have 1 invitations", 1, resThree.size());

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
        assertEquals("user one should not have any nominated invites", 0, resFive.size());

        // now limit the search to 1 returned value
        List<Invitation> limitRes = invitationService.searchInvitation(crit1, 1);
        assertEquals("user one should not have any nominated invites", 0, limitRes.size());

        /**
         * Search with an empty criteria - should find all open invitations
         */
        InvitationSearchCriteria crit2 = new InvitationSearchCriteriaImpl();
        List<Invitation> searchInvitation = invitationService.searchInvitation(crit2);
        assertTrue("2 moderated invitations should be found", searchInvitation.size() == 2);

        // now search everything but limit the results to 3
        searchInvitation = invitationService.searchInvitation(crit2, 2);
        assertTrue("search everything returned 0 or more than 2 elements", searchInvitation.size() > 0 && searchInvitation.size() <=2);

        InvitationSearchCriteriaImpl crit3 = new InvitationSearchCriteriaImpl();
        crit3.setInviter(USER_MANAGER);
        crit3.setInvitationType(InvitationSearchCriteria.InvitationType.NOMINATED);

        List<Invitation> res3 = invitationService.searchInvitation(crit3);
        assertEquals("user one should not have any nominated invites", 0, res3.size());

        //now limit the search to 1 result
        res3 = invitationService.searchInvitation(crit3, 1);
        assertEquals("user one should not have any nominated invites", 0, res3.size());
    }

    /**
     * test that the search limiter works
     */
    @Test
    public void testSearchInvitationWithLimit() throws Exception
    {
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        authenticationComponent.setCurrentUser(USER_MANAGER);

        // Create 10 invites
        for (int i = 0; i < 10; i++)
        {
            invitationService
                    .inviteNominated(USER_ONE, resourceType, resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
        }

        // Invite USER_TWO
        NominatedInvitation inviteForUserTwo = invitationService.inviteNominated(USER_TWO, resourceType, resourceName,
                inviteeRole, serverPath, acceptUrl, rejectUrl);

        InvitationSearchCriteriaImpl query = new InvitationSearchCriteriaImpl();
        query.setInvitee(USER_TWO);

        // search all of them
        List<Invitation> results = invitationService.searchInvitation(query, 0);
        assertEquals(1, results.size());
        assertEquals(inviteForUserTwo.getInviteId(), results.get(0).getInviteId());

        query = new InvitationSearchCriteriaImpl();
        query.setInvitee(USER_ONE);

        final int MAX_SEARCH = 3;
        // only search for the first MAX_SEARCH
        results = invitationService.searchInvitation(query, MAX_SEARCH);
        assertEquals(MAX_SEARCH, results.size());
    }
    
    /**
     * MNT-17341 : External users with Manager role cannot invite other external users to the site because site invitation accept fails
     */
    @Test
    public void testExternalUserManagerInvitingAnotherExternalUser() throws Exception{
        String inviteeFirstName = PERSON_FIRSTNAME;
        String inviteeLastName = PERSON_LASTNAME;
        String inviteeEmail = "123@alfrescotesting.com";
        
        String inviteeFirstName2 = "user2name";
        String inviteeLastName2 = "user2lastname";
        String inviteeEmail2 = "1234@alfrescotesting.com";

        this.authenticationComponent.setCurrentUser(USER_MANAGER);

        // internal user invites an external user as a site manager
        NominatedInvitation nominatedInvitation = invitationService.inviteNominated(inviteeFirstName, inviteeLastName, inviteeEmail,
                Invitation.ResourceType.WEB_SITE, SITE_SHORT_NAME_INVITE, SiteModel.SITE_MANAGER, TEST_SERVER_PATH, TEST_ACCEPT_URL, TEST_REJECT_URL);

        AuthenticationUtil.setFullyAuthenticatedUser(nominatedInvitation.getInviteeUserName());

        invitationService.accept(nominatedInvitation.getInviteId(), nominatedInvitation.getTicket());

        // external user1 now a site manager, invites another external user as a site collaborator
        NominatedInvitation nominatedInvitation2 = invitationService.inviteNominated(inviteeFirstName2, inviteeLastName2, inviteeEmail2,
                Invitation.ResourceType.WEB_SITE, SITE_SHORT_NAME_INVITE, SiteModel.SITE_COLLABORATOR, TEST_SERVER_PATH, TEST_ACCEPT_URL, TEST_REJECT_URL);

        assertNotNull("nominated invitation is null", nominatedInvitation2);
        assertEquals("first name wrong", inviteeFirstName2, nominatedInvitation2.getInviteeFirstName());
        assertEquals("last name wrong", inviteeLastName2, nominatedInvitation2.getInviteeLastName());
        assertEquals("email name wrong", inviteeEmail2, nominatedInvitation2.getInviteeEmail());
        
        AuthenticationUtil.setFullyAuthenticatedUser(nominatedInvitation2.getInviteeUserName());
        
        NodeRef person = personService.getPersonOrNull(nominatedInvitation2.getInviteeUserName());
        assertTrue("user has not been created", person != null);
        assertTrue("user should have the ASPECT_ANULLABLE aspect since the invitation hasn't been accepted yet", nodeService.hasAspect(person, ContentModel.ASPECT_ANULLABLE));      
        
        // authenticated as external user 2 accept the invitation
        Invitation acceptedNominatedInvitation2 = invitationService.accept(nominatedInvitation2.getInviteId(), nominatedInvitation2.getTicket());
        
        assertNotNull("accepted nominated invitation is null", acceptedNominatedInvitation2);
        assertEquals("role is wrong", SiteModel.SITE_COLLABORATOR, acceptedNominatedInvitation2.getRoleName());
        assertEquals("user name wrong", inviteeFirstName2 + "_" + inviteeLastName2, acceptedNominatedInvitation2.getInviteeUserName());
        
        person = personService.getPersonOrNull(acceptedNominatedInvitation2.getInviteeUserName());
        assertTrue("user has not been created", person != null);
        assertTrue("user should not have the ASPECT_ANULLABLE aspect anymore", !nodeService.hasAspect(person, ContentModel.ASPECT_ANULLABLE));
        
        Invitation invitation = invitationService.getInvitation(acceptedNominatedInvitation2.getInviteId());
        assertEquals("invited user name is wrong", invitation.getInviteeUserName(), acceptedNominatedInvitation2.getInviteeUserName());
        assertEquals("invite id is wrong", invitation.getInviteId(), acceptedNominatedInvitation2.getInviteId());
        assertEquals("invite resource name is wrong", invitation.getResourceName(), acceptedNominatedInvitation2.getResourceName());
        
    }

    @Ignore
    @Test
    @Commit
    public void disabled_test100Invites() throws Exception
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
    }

    @Test
    public void testGetInvitation()
    {
        try
        {
            /**
             * Get an invitation that does not exist.
             */
            invitationService.getInvitation("activiti$99999999");
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
