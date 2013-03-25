/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import junit.framework.Assert;

import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.springframework.context.ApplicationContext;

public class MailActionExecuterTest {
	
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    // Rules to create 2 test users.
    public static AlfrescoPerson AUSTRALIAN_USER = new AlfrescoPerson(APP_CONTEXT_INIT, "AustralianUser@test.com");
    public static AlfrescoPerson BRITISH_USER = new AlfrescoPerson(APP_CONTEXT_INIT, "EnglishUser@test.com");
    public static AlfrescoPerson FRENCH_USER = new AlfrescoPerson(APP_CONTEXT_INIT, "FrenchUser@test.com");
    public static AlfrescoPerson UNKNOWN_USER1 = new AlfrescoPerson(APP_CONTEXT_INIT, "UnknownUser1@test.com");
    public static AlfrescoPerson UNKNOWN_USER2 = new AlfrescoPerson(APP_CONTEXT_INIT, "UnknowUser2@test.com");    

    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain ruleChain = RuleChain.outerRule(APP_CONTEXT_INIT)
    														.around(AUSTRALIAN_USER)
    														.around(BRITISH_USER)
                                                            .around(FRENCH_USER)
                                                            .around(UNKNOWN_USER1)
                                                            .around(UNKNOWN_USER2);

	private static ActionService ACTION_SERVICE;
	private static MailActionExecuter ACTION_EXECUTER;
	private static PreferenceService PREFERENCE_SERVICE;

	private static boolean WAS_IN_TEST_MODE;
    
    @BeforeClass
    public static void setup()
    {
    	ApplicationContext appCtx = APP_CONTEXT_INIT.getApplicationContext();
    	ACTION_SERVICE = appCtx.getBean("ActionService", ActionService.class);
    	ACTION_EXECUTER = appCtx.getBean("OutboundSMTP", ApplicationContextFactory.class).getApplicationContext().getBean("mail", MailActionExecuter.class);
    	PREFERENCE_SERVICE = appCtx.getBean("PreferenceService", PreferenceService.class); 
    	
    	WAS_IN_TEST_MODE = ACTION_EXECUTER.isTestMode();
    	ACTION_EXECUTER.setTestMode(true);
    	
    	AuthenticationUtil.setRunAsUserSystem();
    	
    	Map<String, Serializable> preferences = new HashMap<String, Serializable>();
    	
    	preferences.put("locale", "fr");
    	PREFERENCE_SERVICE.setPreferences(FRENCH_USER.getUsername(), preferences);
    	
    	preferences.clear();
    	preferences.put("locale", "en_GB");
    	PREFERENCE_SERVICE.setPreferences(BRITISH_USER.getUsername(), preferences);
    	
    	preferences.clear();
    	preferences.put("locale", "en_AU");
    	PREFERENCE_SERVICE.setPreferences(AUSTRALIAN_USER.getUsername(), preferences);

    }
    
    @AfterClass
    public static void tearDown()
    {
    	ACTION_EXECUTER.setTestMode(WAS_IN_TEST_MODE);
    }
    
    @Test public void testUnknownRecipientUnknownSender() throws IOException, MessagingException
    {
    	Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
    	mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
    	mailAction.setParameterValue(MailActionExecuter.PARAM_TO, "some.bodyelse@example.com");

    	mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
    	mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");
    	
    	mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable)getModel());
    	
    	ACTION_SERVICE.executeAction(mailAction, null);
    	
    	MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
    	Assert.assertNotNull(message);
    	Assert.assertEquals("Hello Jan 1, 1970", (String)message.getContent());
    }
    
    private Serializable getModel() 
    {
    	Map<String, Object> model = new HashMap<String, Object>();
    	
    	model.put("epoch", new Date(0));
		return (Serializable)model;
	}

	@Test public void testFrenchRecipient() throws IOException, MessagingException
    {
    	Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
    	mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
    	mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, (Serializable)Arrays.asList(FRENCH_USER.getUsername()));

    	mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "");
    	mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");
    	mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());
    	
    	ACTION_SERVICE.executeAction(mailAction, null);
    	
    	MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
    	Assert.assertNotNull(message);
    	Assert.assertEquals("Bonjour 1 janv. 1970", (String)message.getContent());
    }

	@Test public void testUnknowRecipientAustralianSender() throws IOException, MessagingException
    {
    	Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
    	mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, AUSTRALIAN_USER.getUsername());
    	mailAction.setParameterValue(MailActionExecuter.PARAM_TO, "some.body@eaxmple.com");

    	mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
    	mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");
    	mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, getModel());
    	
    	ACTION_SERVICE.executeAction(mailAction, null);
    	
    	MimeMessage message = ACTION_EXECUTER.retrieveLastTestMessage();
    	Assert.assertNotNull(message);
    	Assert.assertEquals("G'Day 01/01/1970", (String)message.getContent());
    }

}
