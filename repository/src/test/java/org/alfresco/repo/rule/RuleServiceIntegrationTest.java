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

package org.alfresco.repo.rule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.action.executer.RemoveFeaturesActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

/**
 * @author Harpritt Kalsi
 * @since 3.4
 */
@Category(OwnJVMTestsCategory.class)
public class RuleServiceIntegrationTest
{
    private static final String STANDARD_TEXT_CONTENT = "standardTextContent";
	
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    // Rules to create 2 test users.
    public static AlfrescoPerson TEST_USER1 = new AlfrescoPerson(APP_CONTEXT_INIT, "UserOne");
    public static AlfrescoPerson TEST_USER2 = new AlfrescoPerson(APP_CONTEXT_INIT, "UserTwo");
    
    // A rule to manage test nodes reused across all the test methods
    public static TemporaryNodes STATIC_TEST_NODES = new TemporaryNodes(APP_CONTEXT_INIT);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain ruleChain = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                            .around(TEST_USER1)
                                                            .around(TEST_USER2)
                                                            .around(STATIC_TEST_NODES);
    
    // A rule to manage test nodes use in each test method
    @Rule public TemporaryNodes testNodes = new TemporaryNodes(APP_CONTEXT_INIT);
    
    // A rule to allow individual test methods all to be run as "UserOne".
    // Some test methods need to switch user during execution which they are free to do.
    @Rule public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(TEST_USER1);
    
    // Various services
    private static ServiceRegistry 				SERVICE_REGISTRY;
    private static NodeService                  NODE_SERVICE;
    private static RetryingTransactionHelper    TRANSACTION_HELPER;
    private static ActionService				ACTION_SERVICE;
    private static RuleService					RULE_SERVICE;
    private static ContentService 				CONTENT_SERVICE;
    protected static MailActionExecuter 		MAIL_ACTION_EXECUTER;
    
    
    private static NodeRef COMPANY_HOME;
    
    // These NodeRefs are used by the test methods.
    private static NodeRef TEST_FOLDER;
    private NodeRef parentFolder;
    private NodeRef childFolder;
    private NodeRef childContent;
    
    protected static boolean WAS_IN_TEST_MODE;
    
    
    @BeforeClass public static void setupTest() throws Exception
    {
    	
    	SERVICE_REGISTRY          = (ServiceRegistry)APP_CONTEXT_INIT.getApplicationContext().getBean(ServiceRegistry.SERVICE_REGISTRY);
        NODE_SERVICE              = SERVICE_REGISTRY.getNodeService();
        TRANSACTION_HELPER        = SERVICE_REGISTRY.getTransactionService().getRetryingTransactionHelper();
        ACTION_SERVICE			  = SERVICE_REGISTRY.getActionService();
        RULE_SERVICE			  = SERVICE_REGISTRY.getRuleService();
        CONTENT_SERVICE 		  = SERVICE_REGISTRY.getContentService();
        MAIL_ACTION_EXECUTER 		  = APP_CONTEXT_INIT.getApplicationContext().getBean("OutboundSMTP", ApplicationContextFactory.class).getApplicationContext().getBean("mail", MailActionExecuter.class);
        
        WAS_IN_TEST_MODE = MAIL_ACTION_EXECUTER.isTestMode();
        MAIL_ACTION_EXECUTER.setTestMode(true);
        
        Repository repositoryHelper = (Repository) APP_CONTEXT_INIT.getApplicationContext().getBean("repositoryHelper");
        COMPANY_HOME = repositoryHelper.getCompanyHome();
        
        // Create some static test content
        TEST_FOLDER = STATIC_TEST_NODES.createNode(COMPANY_HOME, "testFolder", ContentModel.TYPE_FOLDER, AuthenticationUtil.getAdminUserName());
   
    }
    
    @AfterClass public static void tearDownTests(){
    	MAIL_ACTION_EXECUTER.setTestMode(WAS_IN_TEST_MODE);
    }
    
    @Before public void createTestContent()
    {
        parentFolder = testNodes.createNode(TEST_FOLDER,   "testFolderInFolder", ContentModel.TYPE_FOLDER, TEST_USER2.getUsername());
        childFolder  = testNodes.createNode(parentFolder,  "testDocInFolder", ContentModel.TYPE_FOLDER, TEST_USER2.getUsername());
        childContent = testNodes.createNode(childFolder,   "theTestContent", ContentModel.TYPE_CONTENT, TEST_USER2.getUsername()); 
    }
    
    

    /**
     * Test that inherited rules with inverted actions behave correctly. 
     * Specifically that the VERSIONABLE aspect is correctly removed after
     * being adding by an inherited parent rule.
     */
    @Test public void testInheritedInvertedRule() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // We'll do all this as user 'UserTwo'.
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                
                //Parent Folder Rule: 
                //Add Versionable to newly created nodes
                org.alfresco.service.cmr.rule.Rule parentRule = new org.alfresco.service.cmr.rule.Rule();
                parentRule.setRuleTypes(Collections.singletonList(RuleType.INBOUND));
                parentRule.setTitle("RuleServiceTest" + GUID.generate());
                parentRule.setDescription("Add Versionable");
                Action action = ACTION_SERVICE.createAction(AddFeaturesActionExecuter.NAME);
                action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
                parentRule.setAction(action);
                parentRule.applyToChildren(true);
                
                //Child Folder Rule: 
                //Remove Versionable to newly created nodes
                org.alfresco.service.cmr.rule.Rule childRule = new org.alfresco.service.cmr.rule.Rule();
                childRule.setRuleTypes(Collections.singletonList(RuleType.INBOUND));
                childRule.setTitle("RuleServiceTest" + GUID.generate());
                childRule.setDescription("RemoveVersonable");
                Action action2 = ACTION_SERVICE.createAction(RemoveFeaturesActionExecuter.NAME);
                action2.setParameterValue(RemoveFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
                childRule.setAction(action2);
                childRule.applyToChildren(false); //Don't need to apply to children
                
                //Save Rules to appropriate Folders
                RULE_SERVICE.saveRule(parentFolder, parentRule);
                RULE_SERVICE.saveRule(childFolder, childRule);
                return null;
            }
        });
        
        //Trigger the inbound rules by adding content
        addContentToNode(childContent);
        
        //Aspect is removed
        assertTrue("Versionable Aspect was not removed", ! NODE_SERVICE.hasAspect(childContent, ContentModel.ASPECT_VERSIONABLE));
        
    }
    
    
    /**
     * ALF-18488 Rules: Send Email action is not working
     * Tests deletion of node triggers outbound rule and fires the MailAction
     */
    @Test public void testEmailExecutorOnOutboundTriggerDelete() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // We'll do all this as user 'UserTwo'.
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                
                //Parent Folder Rule: Outbound
                org.alfresco.service.cmr.rule.Rule rule = new org.alfresco.service.cmr.rule.Rule();
                rule.setRuleTypes(Collections.singletonList(RuleType.OUTBOUND));
                rule.setTitle("RuleServiceTest" + GUID.generate());
                rule.setDescription("Send email on delete");
              
                //Mail Action
                Action mailAction = ACTION_SERVICE.createAction(MailActionExecuter.NAME);
                mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "some.body@example.com");
                mailAction.setParameterValue(MailActionExecuter.PARAM_TO, "some.bodyelse@example.com");
                mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Testing");
                mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, "alfresco/templates/mail/test.txt.ftl");
                mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) getModel());
                
                rule.setAction(mailAction);

                //Save Rules to appropriate Folders
                RULE_SERVICE.saveRule(childFolder, rule);
              
                return null;
            }
        });
        
        //Trigger the outbound rule by deleting the node
        NODE_SERVICE.deleteNode(childContent);
        
        //Fetch unsent message (Test Mode)
        MimeMessage message = MAIL_ACTION_EXECUTER.retrieveLastTestMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals("Hello Jan 1, 1970", (String) message.getContent());
       
    }
    
    
    /**
     * Adds content to a given node. 
     * <p>
     * Used to trigger rules of type of incomming.
     * 
     * @param nodeRef  the node reference
     */
    private void addContentToNode(NodeRef nodeRef)
    {
    	ContentWriter contentWriter = CONTENT_SERVICE.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.setEncoding("UTF-8");
    	assertNotNull(contentWriter);
    	contentWriter.putContent(STANDARD_TEXT_CONTENT + System.currentTimeMillis());
    }

    
    private Serializable getModel()
    {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put("epoch", new Date(0));
        return (Serializable) model;
    }

}
