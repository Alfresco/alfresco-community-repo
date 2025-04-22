/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.action.executer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;

/**
 * Provides tests for the MailActionExecuter class. Most of this logic was in MailActionExecuterTest. Cloud code now includes extra tests and different rule config. Unfortunately this is messy due to the extensive use of static variables and Junit rules annotations. This class contains most of the test code, child classes actually set up the ClassRules and test fixtures, of particular importance is the static setupRuleChain() method in the child classes. The name is just a convention because it can't actually be enforced. The setupRuleChain() actually creates the users, as well as ordering the rules. You will see the AlfrescoPerson variables below are initialized as null, the assumption is that the child classes will create these users before they are needed (in the setupRuleChain() method), again this can't be enforced :(.
 *
 */
public abstract class AbstractMailActionExecuterTest
{

    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();

    // Rules to create test users, these are actually created in the setupRuleChain() method of child classes.
    public static AlfrescoPerson BRITISH_USER = null;
    public static AlfrescoPerson FRENCH_USER = null;
    public static AlfrescoPerson AUSTRALIAN_USER = null;
    public static AlfrescoPerson EXTERNAL_USER = null;

    private static final String ALFRESCO_EE_USER = "plainUser";

    protected static TransactionService TRANSACTION_SERVICE;
    protected static ActionService ACTION_SERVICE;
    protected static MailActionExecuter ACTION_EXECUTER;
    protected static PreferenceService PREFERENCE_SERVICE;
    protected static PersonService PERSON_SERVICE;
    protected static AuthorityService AUTHORITY_SERVICE;
    protected static NodeService NODE_SERVICE;
    protected static PermissionService PERMISSION_SERVICE;

    protected static boolean WAS_IN_TEST_MODE;

    public static void setupTests(ApplicationContext appCtx)
    {
        TRANSACTION_SERVICE = appCtx.getBean("TransactionService", TransactionService.class);
        ACTION_SERVICE = appCtx.getBean("ActionService", ActionService.class);
        ACTION_EXECUTER = appCtx.getBean("OutboundSMTP", ApplicationContextFactory.class).getApplicationContext().getBean("mail", MailActionExecuter.class);
        PREFERENCE_SERVICE = appCtx.getBean("PreferenceService", PreferenceService.class);
        PERSON_SERVICE = appCtx.getBean("PersonService", PersonService.class);
        NODE_SERVICE = appCtx.getBean("NodeService", NodeService.class);
        AUTHORITY_SERVICE = appCtx.getBean("AuthorityService", AuthorityService.class);
        PERMISSION_SERVICE = appCtx.getBean("PermissionService", PermissionService.class);

        WAS_IN_TEST_MODE = ACTION_EXECUTER.isTestMode();
        ACTION_EXECUTER.setTestMode(true);

        AuthenticationUtil.setRunAsUserSystem();

        Map<QName, Serializable> properties = new HashMap<>(1);
        properties.put(ContentModel.PROP_USERNAME, ALFRESCO_EE_USER);
        properties.put(ContentModel.PROP_EMAIL, "testemail@testdomain.com");
        PERSON_SERVICE.createPerson(properties, null);

        // All these test users are in the same tenant - either they're enterprise where there's only one,
        // or they're cloud, where they have the same email domain
        final String tenantId = getUsersHomeTenant(FRENCH_USER.getUsername());
        TenantUtil.runAsSystemTenant(() -> {
            final Map<String, Serializable> preferences = new HashMap<>();

            preferences.put("locale", "fr");
            PREFERENCE_SERVICE.setPreferences(FRENCH_USER.getUsername(), preferences);

            preferences.clear();
            preferences.put("locale", "en_GB");
            PREFERENCE_SERVICE.setPreferences(BRITISH_USER.getUsername(), preferences);

            preferences.clear();
            preferences.put("locale", "en_AU");
            PREFERENCE_SERVICE.setPreferences(AUSTRALIAN_USER.getUsername(), preferences);

            return null;
        }, tenantId);
    }

    private static String getUsersHomeTenant(String userName)
    {
        boolean thisIsCloud = false;
        try
        {
            thisIsCloud = (Class.forName("org.alfresco.module.org_alfresco_module_cloud.registration.RegistrationService") != null);
        }
        catch (ClassNotFoundException ignoreIfThrown)
        {
            // Intentionally empty
        }

        String result = TenantService.DEFAULT_DOMAIN;

        // Even if we get email address-style usernames in an enterprise system, those are not to be given home tenants.
        if (thisIsCloud)
        {
            String[] elems = userName.split("@");
            result = elems[1];
        }

        return result;

    }

    public static void tearDownTests()
    {
        ACTION_EXECUTER.setTestMode(WAS_IN_TEST_MODE);
        PERSON_SERVICE.deletePerson(ALFRESCO_EE_USER);
    }

    @Test
    public void testUnknownRecipientUnknownSender() throws IOException, MessagingException
    {
        // PARAM_TO variant
        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO, "some.bodyelse@example.com");

        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");

        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());

        ACTION_SERVICE.executeAction(mailAction, null);

        MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Hello Jan 1, 1970", message.getContent());
    }

    @Test
    public void testUnknownRecipientUnknownSender_ToMany() throws IOException, MessagingException
    {
        // PARAM_TO_MANY variant - this code path currently has separate validation FIXME fix this.
        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, "some.bodyelse@example.com");

        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");

        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());

        ACTION_SERVICE.executeAction(mailAction, null);

        MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Hello Jan 1, 1970", message.getContent());
    }

    private Serializable getModel()
    {
        Map<String, Object> model = new HashMap<>();

        model.put("epoch", new Date(0));
        return (Serializable) model;
    }

    @Test
    public void testFrenchRecipient() throws IOException, MessagingException
    {
        String from = "some.body@example.com";
        Serializable recipients = (Serializable) Arrays.asList(FRENCH_USER.getUsername());
        String subject = "";
        String template = "alfresco/templates/mail/test.txt.ftl";

        MimeMessage message = sendMessage(from, recipients, subject, template);

        Assert.assertNotNull(message);
        Assert.assertEquals("Bonjour 1 janv. 1970", message.getContent());
    }

    @Test
    public void testHTMLDetection() throws IOException, MessagingException
    {
        String from = "some.body@example.com";
        Serializable recipients = (Serializable) Arrays.asList(FRENCH_USER.getUsername());
        String subject = "";

        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, recipients);

        // First with plain text
        String text = "This is plain text\nOnly\nBut it mentions HTML and <html>";

        MimeMessage message = sendMessage(from, subject, null, text, mailAction);

        Assert.assertNotNull(message);
        Assert.assertEquals(text, message.getContent());
        Assert.assertEquals("text/plain", // Ignore charset
                message.getDataHandler().getContentType().substring(0, 10));

        // HTML opening tag
        text = "<html><body>HTML emails are great</body></html>";
        message = sendMessage(from, subject, null, text, mailAction);

        Assert.assertNotNull(message);
        Assert.assertEquals(text, message.getContent());
        Assert.assertEquals("text/html", // Ignore charset
                message.getDataHandler().getContentType().substring(0, 9));

        // HTML Doctype
        text = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<body>More complex HTML</body></html>";
        message = sendMessage(from, subject, null, text, mailAction);

        Assert.assertNotNull(message);
        Assert.assertEquals(text, message.getContent());
        Assert.assertEquals("text/html", // Ignore charset
                message.getDataHandler().getContentType().substring(0, 9));
    }

    protected MimeMessage sendMessage(String from, Serializable recipients, String subject, String template)
    {
        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, recipients);

        return sendMessage(from, subject, template, mailAction);
    }

    protected MimeMessage sendMessage(String from, String subject, String template, final Action mailAction)
    {
        return sendMessage(from, subject, template, null, mailAction);
    }

    protected MimeMessage sendMessage(String from, String subject, String template, String bodyText, final Action mailAction)
    {
        if (from != null)
        {
            mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, from);
        }
        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, subject);
        if (template != null)
        {
            mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, template);
            mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());
        }
        else
        {
            mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, bodyText);
        }

        RetryingTransactionHelper txHelper = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);

        return txHelper.doInTransaction(() -> {
            ACTION_SERVICE.executeAction(mailAction, null);
            return ACTION_EXECUTER.retrieveLastTestMessage();
        }, true);
    }

    protected MimeMessage sendMessage(String from, String to, String subject, String template)
    {
        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO, to);
        return sendMessage(from, subject, template, mailAction);
    }

    /**
     * Test for ALF-19231
     */
    @Test
    public void testSendMailActionForUserNameAsRecipient() throws IOException, MessagingException
    {
        String from = BRITISH_USER.getUsername();
        Serializable recipients = (Serializable) Arrays.asList(ALFRESCO_EE_USER);
        String subject = "Testing";
        String template = "alfresco/templates/mail/test.txt.ftl";

        MimeMessage message = sendMessage(from, recipients, subject, template);

        Assert.assertNotNull(message);
        Assert.assertEquals("Hello 1 Jan 1970", message.getContent());
    }

    @Test
    public void testUnknownRecipientAustralianSender() throws IOException, MessagingException
    {
        String from = AUSTRALIAN_USER.getUsername();
        String to = "some.body@example.com";
        String subject = "Testing";
        String template = "alfresco/templates/mail/test.txt.ftl";

        MimeMessage message = sendMessage(from, to, subject, template);

        Assert.assertNotNull(message);
        Assert.assertEquals("G'Day 1 Jan 1970", message.getContent());
    }

    @Test
    public void testSendingTestMessageWithNoCurrentUser()
    {
        try
        {
            // run with no current user
            AuthenticationUtil.clearCurrentSecurityContext();

            final Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
            mailAction.setParameterValue(MailActionExecuter.PARAM_TO, "some.body@eaxmple.com");
            mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, "This is a test message.");

            TRANSACTION_SERVICE.getRetryingTransactionHelper().doInTransaction(
                    (RetryingTransactionCallback<Void>) () -> {
                        ACTION_EXECUTER.executeImpl(mailAction, null);
                        return null;
                    });
        }
        finally
        {
            // restore system user as current user
            AuthenticationUtil.setRunAsUserSystem();
        }
    }

    @Test
    public void testPrepareEmailForDisabledUsers() throws MessagingException
    {
        String groupName = null;
        final String USER1 = "test_user1";
        final String USER2 = "test_user2";
        try
        {
            createUser(USER1, null);
            NodeRef userNode = createUser(USER2, null);
            groupName = AUTHORITY_SERVICE.createAuthority(AuthorityType.GROUP, "testgroup1");
            AUTHORITY_SERVICE.addAuthority(groupName, USER1);
            AUTHORITY_SERVICE.addAuthority(groupName, USER2);
            NODE_SERVICE.addAspect(userNode, ContentModel.ASPECT_PERSON_DISABLED, null);
            final Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
            mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, groupName);

            mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, "Testing");

            RetryingTransactionHelper txHelper = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);

            MimeMessage mm = txHelper.doInTransaction(
                    () -> ACTION_EXECUTER.prepareEmail(mailAction, null, null, null).getMimeMessage(), true);

            Address[] addresses = mm.getRecipients(Message.RecipientType.TO);
            Assert.assertEquals(1, addresses.length);
            Assert.assertEquals(USER1 + "@email.com", addresses[0].toString());
        }
        finally
        {
            if (groupName != null)
            {
                AUTHORITY_SERVICE.deleteAuthority(groupName, true);
            }
            PERSON_SERVICE.deletePerson(USER1);
            PERSON_SERVICE.deletePerson(USER2);
        }
    }

    @Test
    public void testPrepareEmailSubjectParams()
    {
        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO, "some.bodyelse@example.com");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");
        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Test Subject Params");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());
        Pair<String, Locale> recipient = new Pair<>("test", Locale.ENGLISH);

        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT_PARAMS, new Object[]{"Test", "Subject", "Params", "Object", "Array"});
        Assert.assertNotNull("We should support Object[] value for PARAM_SUBJECT_PARAMS", ACTION_EXECUTER.prepareEmail(mailAction, null, recipient, null));

        ArrayList<Object> params = new ArrayList<>();
        params.add("Test");
        params.add("Subject");
        params.add("Params");
        params.add("ArrayList");

        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT_PARAMS, params);
        Assert.assertNotNull("We should support List<Object> value for PARAM_SUBJECT_PARAMS", ACTION_EXECUTER.prepareEmail(mailAction, null, recipient, null));

        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT_PARAMS, "Test Subject Params Single String");
        Assert.assertNotNull("We should support String value for PARAM_SUBJECT_PARAMS", ACTION_EXECUTER.prepareEmail(mailAction, null, recipient, null));

        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT_PARAMS, null);
        Assert.assertNotNull("We should support null value for PARAM_SUBJECT_PARAMS", ACTION_EXECUTER.prepareEmail(mailAction, null, recipient, null));
    }

    /**
     * Creates a test user with the specified username and optionally custom email.
     * 
     * @param userName
     *            String
     * @param email
     *            Optional, if not specified assigned to <code>userName + "@email.com"</code>
     * @return NodeRef
     */
    private NodeRef createUser(String userName, String email)
    {
        PropertyMap personProps = new PropertyMap();
        personProps.put(ContentModel.PROP_USERNAME, userName);
        personProps.put(ContentModel.PROP_FIRSTNAME, userName);
        personProps.put(ContentModel.PROP_LASTNAME, userName);
        personProps.put(ContentModel.PROP_EMAIL, Objects.requireNonNullElseGet(email, () -> userName + "@email.com"));

        return PERSON_SERVICE.createPerson(personProps);
    }

    /**
     * Test for MNT-10874
     */
    @Test
    public void testUserWithNonExistingTenant()
    {
        final String USER_WITH_NON_EXISTING_TENANT = "test_user_non_tenant@non_existing_tenant.com";

        try
        {
            createUser(USER_WITH_NON_EXISTING_TENANT, USER_WITH_NON_EXISTING_TENANT);

            final Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
            mailAction.setParameterValue(MailActionExecuter.PARAM_TO, USER_WITH_NON_EXISTING_TENANT);
            mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, "This is a test message.");

            // run as non admin and non system
            AuthenticationUtil.setFullyAuthenticatedUser(BRITISH_USER.getUsername());
            TRANSACTION_SERVICE.getRetryingTransactionHelper().doInTransaction(
                    (RetryingTransactionCallback<Void>) () -> {
                        ACTION_EXECUTER.executeImpl(mailAction, null);
                        return null;
                    });
        }
        finally
        {
            // restore system user as current user
            AuthenticationUtil.setRunAsUserSystem();
            // tidy up
            PERSON_SERVICE.deletePerson(USER_WITH_NON_EXISTING_TENANT);
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    /**
     * Test for MNT-11488
     * 
     * @throws MessagingException
     */
    @Test
    public void testSendingToMultipleUsers() throws MessagingException
    {
        final String USER_1 = "recipient1";
        final String USER_2 = "recipient2";
        final String[] recipientsArray = {USER_1 + "@email.com", USER_2 + "@email.com"};
        final List<String> recipientsResult = new ArrayList<>(Arrays.asList(recipientsArray));

        try
        {
            createUser(USER_1, null);
            createUser(USER_2, null);
            ArrayList<String> recipients = new ArrayList<>(2);
            recipients.add(USER_1);
            recipients.add(USER_2);

            Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
            mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "sender@example.com");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, recipients);
            mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());

            ACTION_EXECUTER.resetTestSentCount();

            ACTION_SERVICE.executeAction(mailAction, null);

            MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
            Assert.assertNotNull(message);
            Assert.assertEquals("One email should be sent", 1, ACTION_EXECUTER.getTestSentCount());
            Assert.assertEquals("All recipients should receive single message", 2, message.getAllRecipients().length);

            Assert.assertTrue("Both users should receive message", recipientsResult.contains(((InternetAddress) message.getAllRecipients()[0]).getAddress()));
            Assert.assertTrue("Both users should receive message", recipientsResult.contains(((InternetAddress) message.getAllRecipients()[1]).getAddress()));
        }
        finally
        {
            // tidy up
            PERSON_SERVICE.deletePerson(USER_1);
            PERSON_SERVICE.deletePerson(USER_2);
        }
    }

    /**
     * Test for CC / BCC
     * 
     * @throws MessagingException
     */
    @Test
    public void testSendingToCarbonCopy() throws MessagingException
    {
        // PARAM_TO variant
        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO, "some.bodyelse@example.com");
        mailAction.setParameterValue(MailActionExecuter.PARAM_CC, "some.carbon@example.com");
        mailAction.setParameterValue(MailActionExecuter.PARAM_BCC, "some.blindcarbon@example.com");

        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing CARBON COPY");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");

        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());

        ACTION_SERVICE.executeAction(mailAction, null);

        MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
        Assert.assertNotNull(message);
        Address[] all = message.getAllRecipients();
        Address[] ccs = message.getRecipients(RecipientType.CC);
        Address[] bccs = message.getRecipients(RecipientType.BCC);
        Assert.assertEquals(3, all.length);
        Assert.assertEquals(1, ccs.length);
        Assert.assertEquals(1, bccs.length);
        Assert.assertTrue(ccs[0].toString().contains("some.carbon"));
        Assert.assertTrue(bccs[0].toString().contains("some.blindcarbon"));
    }

    /**
     * Test for MNT-11079
     */
    @Test
    public void testSendingToUserWithMailAlikeName() throws IOException, MessagingException
    {
        final String USER_1 = "user1@namelookslikeemail";
        final String USER_1_EMAIL = "user1@trueemail.com";

        try
        {
            createUser(USER_1, USER_1_EMAIL);

            Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
            mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, USER_1);

            mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");

            mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());

            ACTION_SERVICE.executeAction(mailAction, null);

            MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
            Assert.assertNotNull(message);
            Assert.assertEquals("Hello Jan 1, 1970", message.getContent());
            Assert.assertEquals(1, message.getAllRecipients().length);
            jakarta.mail.internet.InternetAddress address = (InternetAddress) message.getAllRecipients()[0];
            Assert.assertEquals(USER_1_EMAIL, address.getAddress());
        }
        finally
        {
            // tidy up
            PERSON_SERVICE.deletePerson(USER_1);
        }
    }

    /**
     * Test for MNT-12464
     */
    @Test
    public void testMultipleIdenticalEmailsToUser()
    {
        final String USER_1 = "user12464_1";
        final String USER_2 = "user12464_2";
        final String USER_3 = "user12464_3";
        final String USER_6 = "user12464_6";

        final String USER_4_USERNAME = "user12464_4@mnt12464mail.com";
        final String USER_5_USERNAME = "user12464_5@mnt12464mail.com";

        String[] users = new String[]{USER_1, USER_2, USER_3, USER_4_USERNAME, USER_5_USERNAME};

        final String GROUP_1_SHORT_NAME = "mnt12464group1";
        final String GROUP_1 = "GROUP_" + GROUP_1_SHORT_NAME;
        final String GROUP_2_SHORT_NAME = "mnt12464group2";
        final String GROUP_2 = "GROUP_" + GROUP_2_SHORT_NAME;

        try
        {
            createUser(USER_1, null);
            createUser(USER_2, null);
            createUser(USER_3, null);
            AUTHORITY_SERVICE.createAuthority(AuthorityType.GROUP, GROUP_1_SHORT_NAME);
            AUTHORITY_SERVICE.createAuthority(AuthorityType.GROUP, GROUP_2_SHORT_NAME);
            AUTHORITY_SERVICE.addAuthority(GROUP_1, USER_1);
            AUTHORITY_SERVICE.addAuthority(GROUP_1, USER_2);
            AUTHORITY_SERVICE.addAuthority(GROUP_2, USER_1);
            AUTHORITY_SERVICE.addAuthority(GROUP_2, USER_2);
            AUTHORITY_SERVICE.addAuthority(GROUP_2, USER_3);

            // these persons should be without emails
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, USER_4_USERNAME);
            personProps.put(ContentModel.PROP_FIRSTNAME, USER_4_USERNAME);
            personProps.put(ContentModel.PROP_LASTNAME, USER_4_USERNAME);
            PERSON_SERVICE.createPerson(personProps);
            AUTHORITY_SERVICE.addAuthority(GROUP_1, USER_4_USERNAME);
            AUTHORITY_SERVICE.addAuthority(GROUP_2, USER_4_USERNAME);

            personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, USER_5_USERNAME);
            personProps.put(ContentModel.PROP_FIRSTNAME, USER_5_USERNAME);
            personProps.put(ContentModel.PROP_LASTNAME, USER_5_USERNAME);
            PERSON_SERVICE.createPerson(personProps);

            Action mailAction1 = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
            mailAction1.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
            ArrayList<String> toMany1 = new ArrayList<>();
            toMany1.add(USER_1);
            toMany1.add(GROUP_1);
            toMany1.add(USER_2);
            toMany1.add(GROUP_2);
            toMany1.add(USER_3);
            toMany1.add(USER_4_USERNAME);
            toMany1.add(USER_5_USERNAME);
            mailAction1.setParameterValue(MailActionExecuter.PARAM_TO_MANY, toMany1);
            mailAction1.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing MNT-12464");
            mailAction1.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");
            mailAction1.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());

            ACTION_EXECUTER.resetTestSentCount();
            ACTION_SERVICE.executeAction(mailAction1, null);

            Assert.assertEquals("Must be received one letter on each recipient", users.length, ACTION_EXECUTER.getTestSentCount());

            // testing for GROUP_EVERYONE

            ACTION_EXECUTER.resetTestSentCount();
            everyoneSending();
            int before = ACTION_EXECUTER.getTestSentCount();
            // create one additional user
            NodeRef user6 = createUser(USER_6, null);
            PERMISSION_SERVICE.setInheritParentPermissions(user6, false);
            PERMISSION_SERVICE.deletePermissions(user6);

            // USER_6 not exist for USER_1, but he will be added to recipients
            int after = AuthenticationUtil.runAs(() -> {
                ACTION_EXECUTER.resetTestSentCount();
                everyoneSending();
                return ACTION_EXECUTER.getTestSentCount();
            }, USER_1);

            Assert.assertEquals("One additional user was created, quantity of recipients GROUP_EVERYONE must be +1 user", 1, after - before);
        }
        finally
        {
            PERSON_SERVICE.deletePerson(USER_1);
            PERSON_SERVICE.deletePerson(USER_2);
            PERSON_SERVICE.deletePerson(USER_3);
            PERSON_SERVICE.deletePerson(USER_4_USERNAME);
            PERSON_SERVICE.deletePerson(USER_5_USERNAME);
            PERSON_SERVICE.deletePerson(USER_6);
            AUTHORITY_SERVICE.deleteAuthority(GROUP_1);
            AUTHORITY_SERVICE.deleteAuthority(GROUP_2);
        }
    }

    private void everyoneSending()
    {
        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
        ArrayList<String> toMany = new ArrayList<>();
        toMany.add(PERMISSION_SERVICE.getAllAuthorities());
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, toMany);
        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing MNT-12464");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());

        ACTION_EXECUTER.resetTestSentCount();

        ACTION_SERVICE.executeAction(mailAction, null);
    }

    /**
     * ACE-2564
     */
    @Test
    public void testSendEmailByExternalUser() throws IOException, MessagingException
    {
        final Serializable recipients = (Serializable) Arrays.asList(BRITISH_USER.getUsername());
        final String subject = "";
        final String template = "alfresco/templates/mail/test.txt.ftl";
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(EXTERNAL_USER.getUsername());

        try
        {
            // these people should be without emails
            // testing for GROUP_EVERYONE
            final String tenantId = getUsersHomeTenant(BRITISH_USER.getUsername());

            // USER_6 not exist for USER_1, but he will be added to recipients
            MimeMessage message = TenantUtil.runAsTenant(() -> sendMessage(null, recipients, subject, template), tenantId);

            Assert.assertNotNull(message);
            Assert.assertEquals("Hello 1 Jan 1970", message.getContent());
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * Test for MNT-17970
     * 
     * @throws IOException
     * @throws MessagingException
     */
    @Test
    public void testGetToUsersWhenSendingToGroup() throws IOException, MessagingException
    {
        String groupName = null;
        final String USER1 = "test_user1";
        final String USER2 = "test_user2";
        try
        {
            // Create users and add them to a group
            createUser(USER1, null);
            createUser(USER2, null);
            groupName = AUTHORITY_SERVICE.createAuthority(AuthorityType.GROUP, "testgroup1");
            AUTHORITY_SERVICE.addAuthority(groupName, USER1);
            AUTHORITY_SERVICE.addAuthority(groupName, USER2);

            // Create mail
            final Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
            mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, groupName);
            mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, "Testing");
            mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/testSentTo.txt.ftl");

            RetryingTransactionHelper txHelper = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);

            // Send mail
            MimeMessage message = txHelper.doInTransaction(() -> {
                ACTION_EXECUTER.executeImpl(mailAction, null);
                return ACTION_EXECUTER.retrieveLastTestMessage();
            }, true);

            // Check that both users are displayed in message body
            String recipients = USER1 + "@email.com" + "," + USER2 + "@email.com";
            Assert.assertNotNull(message);
            Assert.assertEquals("This email was sent to " + recipients, message.getContent());
        }
        finally
        {
            if (groupName != null)
            {
                AUTHORITY_SERVICE.deleteAuthority(groupName, true);
            }
            PERSON_SERVICE.deletePerson(USER1);
            PERSON_SERVICE.deletePerson(USER2);
        }
    }

    /**
     * ALF-21948
     */
    @Test
    public void testSendingToArrayOfCarbonCopyAndBlindCarbonCopyUsers() throws MessagingException
    {
        Map<String, Serializable> params = new HashMap<>();
        String[] ccArray = {"cc_user1@example.com", "cc_user2@example.com"};
        String[] bccArray = {"bcc_user3@example.com", "bcc_user4@example.com", "bcc_user5@example.com"};
        params.put(MailActionExecuter.PARAM_FROM, "sender@email.com");
        params.put(MailActionExecuter.PARAM_TO, "test@email.com");
        params.put(MailActionExecuter.PARAM_CC, ccArray);
        params.put(MailActionExecuter.PARAM_BCC, bccArray);

        params.put(MailActionExecuter.PARAM_TEXT, "Mail body here");
        params.put(MailActionExecuter.PARAM_SUBJECT, "Subject text");

        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME, params);
        ACTION_EXECUTER.resetTestSentCount();

        ACTION_SERVICE.executeAction(mailAction, null);
        MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
        Assert.assertNotNull(message);

        Address[] all = message.getAllRecipients();
        Address[] ccs = message.getRecipients(RecipientType.CC);
        Address[] bccs = message.getRecipients(RecipientType.BCC);
        Assert.assertEquals(6, all.length);
        Assert.assertEquals(2, ccs.length);
        Assert.assertEquals(3, bccs.length);
        Assert.assertTrue(ccs[0].toString().contains("cc_user1") && ccs[1].toString().contains("cc_user2"));
        Assert.assertTrue(bccs[0].toString().contains("bcc_user3") && bccs[1].toString().contains("bcc_user4")
                && bccs[2].toString().contains("bcc_user5"));
    }

    /**
     * ALF-21948
     */
    @Test
    public void testSendingToListOfCarbonCopyAndBlindCarbonCopyUsers() throws MessagingException
    {
        List<String> ccList = new ArrayList<>();
        ccList.add("cc_user1@example.com");
        ccList.add("cc_user2@example.com");

        List<String> bccList = new ArrayList<>();
        bccList.add("bcc_user3@example.com");
        bccList.add("bcc_user4@example.com");
        bccList.add("bcc_user5@example.com");

        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO, "some.bodyelse@example.com");
        mailAction.setParameterValue(MailActionExecuter.PARAM_CC, (Serializable) ccList);
        mailAction.setParameterValue(MailActionExecuter.PARAM_BCC, (Serializable) bccList);

        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing (BLIND) CARBON COPY");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, "mail body here");

        ACTION_EXECUTER.resetTestSentCount();
        ACTION_SERVICE.executeAction(mailAction, null);
        MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
        Assert.assertNotNull(message);

        Address[] all = message.getAllRecipients();
        Address[] ccs = message.getRecipients(RecipientType.CC);
        Address[] bccs = message.getRecipients(RecipientType.BCC);
        Assert.assertEquals(6, all.length);
        Assert.assertEquals(2, ccs.length);
        Assert.assertEquals(3, bccs.length);
        Assert.assertTrue(ccs[0].toString().contains("cc_user1") && ccs[1].toString().contains("cc_user2"));
        Assert.assertTrue(bccs[0].toString().contains("bcc_user3") && bccs[1].toString().contains("bcc_user4")
                && bccs[2].toString().contains("bcc_user5"));
    }

}
