/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.action.executer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Provides tests for the MailActionExecuter class.  Most of this logic was in MailActionExecuterTest.
 * Cloud code now includes extra tests and different rule config.
 * Unfortunately this is messy due to the extensive use of static variables and Junit rules annotations.
 * This class contains most of the test code, child classes actually setup the ClassRules and test fixtures, of
 * particular importance is the static setupRuleChain() method in the child classes.  The name is just a convention because
 * it can't actually be enforced.  The setupRuleChain() actually creates the users, as well as ordering the rules.
 * You will see the AlfrescoPerson variables below are initialized as null, the assumption is that the child classes will
 * create these users before they are needed (in the setupRuleChain() method), again this can't be enforced :(.
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

    private static String ALFRESCO_EE_USER = "plainUser";

    protected static TransactionService TRANSACTION_SERVICE;
    protected static ActionService ACTION_SERVICE;
    protected static MailActionExecuter ACTION_EXECUTER;
    protected static PreferenceService PREFERENCE_SERVICE;
    protected static PersonService PERSON_SERVICE;
    protected static AuthorityService AUTHORITY_SERVICE;
    protected static NodeService NODE_SERVICE;

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

        WAS_IN_TEST_MODE = ACTION_EXECUTER.isTestMode();
        ACTION_EXECUTER.setTestMode(true);

        AuthenticationUtil.setRunAsUserSystem();

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_USERNAME, ALFRESCO_EE_USER);
        properties.put(ContentModel.PROP_EMAIL, "testemail@testdomain.com");
        PERSON_SERVICE.createPerson(properties, null);

        // All these test users are in the same tenant - either they're enterprise where there's only one,
        // or they're cloud, where they have the same email domain
        final String tenantId = getUsersHomeTenant(FRENCH_USER.getUsername());
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                final Map<String, Serializable> preferences = new HashMap<String, Serializable>();

                preferences.put("locale", "fr");
                PREFERENCE_SERVICE.setPreferences(FRENCH_USER.getUsername(), preferences);

                preferences.clear();
                preferences.put("locale", "en_GB");
                PREFERENCE_SERVICE.setPreferences(BRITISH_USER.getUsername(), preferences);

                preferences.clear();
                preferences.put("locale", "en_AU");
                PREFERENCE_SERVICE.setPreferences(AUSTRALIAN_USER.getUsername(), preferences);

                return null;
            }
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

        // Even if we get email address-style user names in an enterprise system, those are not to be given home tenants.
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

        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) getModel());

        ACTION_SERVICE.executeAction(mailAction, null);

        MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Hello Jan 1, 1970", (String) message.getContent());
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

        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) getModel());

        ACTION_SERVICE.executeAction(mailAction, null);

        MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Hello Jan 1, 1970", (String) message.getContent());
    }

    private Serializable getModel()
    {
        Map<String, Object> model = new HashMap<String, Object>();

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
        Assert.assertEquals("Bonjour 1 janv. 1970", (String) message.getContent());
    }

    protected MimeMessage sendMessage(String from, Serializable recipients, String subject, String template)
    {
        Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, recipients);

        return sendMessage(from, subject, template, mailAction);
    }

    protected MimeMessage sendMessage(String from, String subject, String template, final Action mailAction)
    {
        if (from != null)
        {
            mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, from);
        }
        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, subject);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, template);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());

        RetryingTransactionHelper txHelper = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);

        return txHelper.doInTransaction(new RetryingTransactionCallback<MimeMessage>()
        {
            @Override
            public MimeMessage execute() throws Throwable
            {
                ACTION_SERVICE.executeAction(mailAction, null);

                return ACTION_EXECUTER.retrieveLastTestMessage();
            }
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
        Assert.assertEquals("Hello 01-Jan-1970", (String) message.getContent());
    }

    @Test
    public void testUnknowRecipientAustralianSender() throws IOException, MessagingException
    {
        String from = AUSTRALIAN_USER.getUsername();
        String to = "some.body@example.com";
        String subject = "Testing";
        String template = "alfresco/templates/mail/test.txt.ftl";

        MimeMessage message = sendMessage(from, to, subject, template);

        Assert.assertNotNull(message);
        Assert.assertEquals("G'Day 01/01/1970", (String) message.getContent());
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

            TRANSACTION_SERVICE.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    ACTION_EXECUTER.executeImpl(mailAction, null);
                    return null;
                }
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
    	try
    	{
            final String USER1 = "test_user1";
            final String USER2 = "test_user2";
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

            MimeMessage mm = txHelper.doInTransaction(new RetryingTransactionCallback<MimeMessage>()
            {
                @Override
                public MimeMessage execute() throws Throwable
                {
                    return ACTION_EXECUTER.prepareEmail(mailAction, null, null, null).getMimeMessage();
                }
            }, true);

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
    	}
    }

    /**
     * Creates a test user with the specified username and optionally custom email.
     * 
     * @param userName
     * @param email Optional, if not specified assigned to <code>userName + "@email.com"</code>
     * @return
     */
    private NodeRef createUser(String userName, String email)
    {
        PropertyMap personProps = new PropertyMap();
        personProps.put(ContentModel.PROP_USERNAME, userName);
        personProps.put(ContentModel.PROP_FIRSTNAME, userName);
        personProps.put(ContentModel.PROP_LASTNAME, userName);
        if (email != null)
        {
            personProps.put(ContentModel.PROP_EMAIL, email);
        }
        else
        {
            personProps.put(ContentModel.PROP_EMAIL, userName + "@email.com");
        }

        return PERSON_SERVICE.createPerson(personProps);
    }

    /**
     * Test for MNT-10874
     * @throws Exception 
     */
    @Test
    public void testUserWithNonExistingTenant() throws Exception
    {
        final String USER_WITH_NON_EXISTING_TENANT = "test_user_non_tenant@non_existing_tenant.com";
        
        createUser(USER_WITH_NON_EXISTING_TENANT, USER_WITH_NON_EXISTING_TENANT);
        
        final Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_TO, USER_WITH_NON_EXISTING_TENANT);
        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
        mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, "This is a test message.");

        // run as non admin and non system
        AuthenticationUtil.setFullyAuthenticatedUser(BRITISH_USER.getUsername());
        TRANSACTION_SERVICE.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ACTION_EXECUTER.executeImpl(mailAction, null);
                return null;
            }
        });
        
        AuthenticationUtil.clearCurrentSecurityContext();
    }
}
