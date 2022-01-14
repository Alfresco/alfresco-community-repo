/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

package org.alfresco.repo.action;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.action.executer.CounterIncrementActionExecuter;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterConstraint;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.alfresco.util.test.junitrules.TemporarySites.TestSiteAndMemberInfo;
import org.alfresco.util.test.junitrules.WellKnownNodes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Jamal Kaabi-Mofrad
 * @since Odin
 */
public class ActionServiceImpl2Test
{
    // Rule to initialise the default Alfresco spring configuration
    @ClassRule
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();

    /**
     * This JUnit rule will allow us to create Share sites and users and have
     * them automatically cleaned up for us.
     */
    @Rule
    public TemporarySites temporarySites = new TemporarySites(APP_CONTEXT_INIT);

    @Rule
    public WellKnownNodes wellKnownNodes = new WellKnownNodes(APP_CONTEXT_INIT);
    
    @Rule
    public TemporaryNodes tempNodes = new TemporaryNodes(APP_CONTEXT_INIT);

    // Various services
    private static NodeService nodeService;

    private static ActionService actionService;

    private static ContentService contentService;

    private static RetryingTransactionHelper transactionHelper;

    private TestSiteAndMemberInfo testSiteAndMemberInfo;

    private NodeRef testNode;



    @BeforeClass
    public static void initStaticData() throws Exception
    {
        nodeService = (NodeService) APP_CONTEXT_INIT.getApplicationContext().getBean("nodeService");
        actionService = (ActionService) APP_CONTEXT_INIT.getApplicationContext().getBean("actionService");
        contentService = (ContentService) APP_CONTEXT_INIT.getApplicationContext().getBean("contentService");
        transactionHelper = (RetryingTransactionHelper) APP_CONTEXT_INIT.getApplicationContext().getBean(
                "retryingTransactionHelper");
    }

    @Before
    public void initTestSiteAndUsersAndSomeContent()
    {
        final String siteShortName = ActionServiceImpl2Test.class.getSimpleName() + "TestSite"
                + System.currentTimeMillis();

        // This will create a public Share site whose creator is the admin user.
        // It will create 4 users (one for each of the Share roles, and add them
        // to the site.
        testSiteAndMemberInfo = temporarySites.createTestSiteWithUserPerRole(siteShortName, "sitePreset",
                SiteVisibility.PUBLIC, AuthenticationUtil.getAdminUserName());

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        testNode = transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // get the Document Library NodeRef
                final NodeRef docLibNodeRef = testSiteAndMemberInfo.doclib;

                // Create a test node. It doesn't need content.
                return nodeService.createNode(docLibNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
                        ContentModel.TYPE_CONTENT).getChildRef();
            }
        });
    }

    // MNT-15365
    @Test
    public void testIncrementCounterOnDeletedNode() throws Exception
    {
        final NodeRef deletedNode = transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // get the Document Library NodeRef
                final NodeRef docLibNodeRef = testSiteAndMemberInfo.doclib;

                NodeRef result = nodeService.createNode(docLibNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
                        ContentModel.TYPE_CONTENT).getChildRef();
                nodeService.deleteNode(result);
                return result;
            }
        });

        // before the fix that would thrown an error
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Action incrementAction = actionService.createAction(CounterIncrementActionExecuter.NAME);

                actionService.executeAction(incrementAction, deletedNode);
                return null;
            }
        });
    }

    @Test
    public void testIncrementCounter() throws Exception
    {
        // Set authentication to SiteManager.
        AuthenticationUtil.setFullyAuthenticatedUser(testSiteAndMemberInfo.siteManager);
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // add the cm:countable aspect and set the value to 1
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_COUNTER, 1);
                nodeService.addAspect(testNode, ContentModel.ASPECT_COUNTABLE, props);

                return null;
            }
        });
        // check that the default counter value is set to 1
        int beforeIncrement = (Integer) nodeService.getProperty(testNode, ContentModel.PROP_COUNTER);
        assertEquals("Counter value incorrect", 1, beforeIncrement);

        // Set authentication to SiteConsumer.
        AuthenticationUtil.setFullyAuthenticatedUser(testSiteAndMemberInfo.siteConsumer);
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Action incrementAction = actionService.createAction(CounterIncrementActionExecuter.NAME);

                actionService.executeAction(incrementAction, testNode);
                return null;
            }
        });

        int afterIncrement = (Integer) nodeService.getProperty(testNode, ContentModel.PROP_COUNTER);
        // CounterIncrementActionExecuter is a sample action, therefore, the
        // permission is no checked.
        assertEquals(2, afterIncrement);
    }

    @Test//(expected = AccessDeniedException.class)
    public void testTransform() throws Exception
    {
        final File file = loadAndAddQuickFileAsManager(testNode, "quick.txt", MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertNotNull("Failed to load required test file.", file);

        // Set authentication to SiteConsumer.
        AuthenticationUtil.setFullyAuthenticatedUser(testSiteAndMemberInfo.siteManager);
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Action action = actionService.createAction(TransformActionExecuter.NAME);
                Map<String, Serializable> map = new HashMap<String, Serializable>();
                map.put(TransformActionExecuter.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_HTML);
                map.put(TransformActionExecuter.PARAM_DESTINATION_FOLDER, nodeService.getPrimaryParent(testNode)
                        .getParentRef());
                action.setParameterValues(map);

                actionService.executeAction(action, testNode);

                return null;
            }
        });
    }

    @Test
    public void testParameterConstraints() throws Exception
    {
        List<ParameterConstraint> constraints = actionService.getParameterConstraints();
        assertNotNull(constraints);
        if (constraints.size() > 0)
        {
            ParameterConstraint parameterConstraint = constraints.get(0);
            ParameterConstraint pConstraintAgain = actionService.getParameterConstraint(parameterConstraint.getName());
            Assert.assertEquals(parameterConstraint, pConstraintAgain);
        }
    }

    @Test
    public void testExecuteScript() throws Exception
    {
        final NodeRef scriptToBeExecuted = addTempScript("changeFileNameTest.js",
                "document.properties.name = \"Changed_\" + document.properties.name;\ndocument.save();");
        assertNotNull("Failed to add the test script.", scriptToBeExecuted);

        // add a test file to the Site in order to change its name
        final File file = loadAndAddQuickFileAsManager(testNode, "quick.pdf", MimetypeMap.MIMETYPE_PDF);
        assertNotNull("Failed to load required test file.", file);

        // Set authentication to SiteConsumer
        AuthenticationUtil.setFullyAuthenticatedUser(testSiteAndMemberInfo.siteConsumer);
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Create the action
                Action action = actionService.createAction(ScriptActionExecuter.NAME);
                action.setParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF, scriptToBeExecuted);

                try
                {
                    // Execute the action
                    actionService.executeAction(action, testNode);
                }
                catch (Throwable th)
                {
                    // do nothing
                }
                assertTrue("The consumer shouldn't be able to change the name of the file.",
                        ("quick.pdf".equals(nodeService.getProperty(testNode, ContentModel.PROP_NAME))));

                return null;
            }
        });

        // Set authentication to SiteManager
        AuthenticationUtil.setFullyAuthenticatedUser(testSiteAndMemberInfo.siteManager);
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Create the action
                Action action = actionService.createAction(ScriptActionExecuter.NAME);
                action.setParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF, scriptToBeExecuted);

                // Execute the action
                actionService.executeAction(action, testNode);

                assertEquals("Changed_quick.pdf", nodeService.getProperty(testNode, ContentModel.PROP_NAME));

                return null;
            }
        });
        
        //Execute script not in Data Dictionary > Scripts
        AuthenticationUtil.setFullyAuthenticatedUser(testSiteAndMemberInfo.siteManager);
        NodeRef companyHomeRef = wellKnownNodes.getCompanyHome();
        NodeRef sharedFolderRef = nodeService.getChildByName(companyHomeRef, ContentModel.ASSOC_CONTAINS,
                "Shared");
        final NodeRef invalidScriptRef = addTempScript("changeFileNameTest.js",
                "document.properties.name = \"Invalid_Change.pdf\";\ndocument.save();",sharedFolderRef);
        assertNotNull("Failed to add the test script.", scriptToBeExecuted);
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Create the action
                Action action = actionService.createAction(ScriptActionExecuter.NAME);
                action.setParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF, invalidScriptRef);

                try
                {
                    // Execute the action
                    actionService.executeAction(action, testNode);
                }
                catch (Throwable th)
                {
                    // do nothing
                }
                assertFalse("Scripts outside of Data Dictionary Scripts folder should not be executed",
                        ("Invalid_Change.pdf".equals(nodeService.getProperty(testNode, ContentModel.PROP_NAME))));

                return null;
            }
        });
    }
    
    @Test
    public void testActionResult() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                try
                {
                    // Create the script node reference
                    NodeRef script = addTempScript("test-action-result-script.js", "\"VALUE\";");

                    // Create the action
                    Action action = actionService.createAction(ScriptActionExecuter.NAME);
                    action.setParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF, script);

                    // Execute the action
                    actionService.executeAction(action, testNode);

                    // Get the result
                    String result = (String) action.getParameterValue(ActionExecuter.PARAM_RESULT);
                    assertNotNull(result);
                    assertEquals("VALUE", result);
                }
                finally
                {
                    AuthenticationUtil.clearCurrentSecurityContext();
                }

                return null;
            }
        });
    }

    @Test
    public void testExtractMetedata() throws Exception
    {
        // add a test file to the Site in order to change its name
        final File file = loadAndAddQuickFileAsManager(testNode, "quick.pdf", MimetypeMap.MIMETYPE_PDF);
        assertNotNull("Failed to load required test file.", file);

        // Set authentication to SiteConsumer
        AuthenticationUtil.setFullyAuthenticatedUser(testSiteAndMemberInfo.siteConsumer);
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Create the action
                Action action = actionService.createAction(ContentMetadataExtracter.EXECUTOR_NAME);
                try
                {
                    actionService.executeAction(action, testNode);
                }
                catch (Throwable th)
                {
                    // do nothing
                }
                assertTrue("The consumer shouldn't be able to perform Extract Metadata.",
                        (nodeService.getProperty(testNode, ContentModel.PROP_DESCRIPTION) == null));

                return null;
            }
        });

        // Set authentication to SiteCollaborator
        AuthenticationUtil.setFullyAuthenticatedUser(testSiteAndMemberInfo.siteCollaborator);
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Create the action
                Action action = actionService.createAction(ContentMetadataExtracter.EXECUTOR_NAME);
                // Execute the action
                actionService.executeAction(action, testNode);
                return null;
            }
        });

        Thread.sleep(3000); // Need to wait for the async extract

        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertEquals("Pangram, fox, dog, Gym class featuring a brown fox and lazy dog",
                        nodeService.getProperty(testNode, ContentModel.PROP_DESCRIPTION));
                return null;
            }
        });
    }

    private NodeRef addTempScript(final String scriptFileName, final String javaScript, final NodeRef parentRef)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        return transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {

                // Create the script node reference
                NodeRef script = nodeService.createNode(parentRef, ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, scriptFileName),
                        ContentModel.TYPE_CONTENT).getChildRef();

                nodeService.setProperty(script, ContentModel.PROP_NAME, scriptFileName);

                ContentWriter contentWriter = contentService.getWriter(script, ContentModel.PROP_CONTENT, true);
                contentWriter.setMimetype(MimetypeMap.MIMETYPE_JAVASCRIPT);
                contentWriter.setEncoding("UTF-8");
                contentWriter.putContent(javaScript);

                tempNodes.addNodeRef(script);              
                return script;
            }
        });
    }

    private NodeRef addTempScript(final String scriptFileName, final String javaScript)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        return transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {

                // get the company_home
                NodeRef companyHomeRef = wellKnownNodes.getCompanyHome();
                // get the Data Dictionary
                NodeRef dataDictionaryRef = nodeService.getChildByName(companyHomeRef, ContentModel.ASSOC_CONTAINS,
                        "Data Dictionary");
                // get the Scripts
                NodeRef scriptsRef = nodeService.getChildByName(dataDictionaryRef, ContentModel.ASSOC_CONTAINS,
                        "Scripts");

                return addTempScript(scriptFileName, javaScript, scriptsRef);
            }
        });
    }

    private File loadAndAddQuickFileAsManager(final NodeRef nodeRef, final String quickFileName, final String mimeType)
            throws IOException
    {
        final File file = AbstractContentTransformerTest.loadNamedQuickTestFile(quickFileName);

        if (file == null) { return null; }

        // Set authentication to SiteManager and add a file
        AuthenticationUtil.setFullyAuthenticatedUser(testSiteAndMemberInfo.siteManager);
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, quickFileName);

                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(mimeType);
                writer.setEncoding("UTF-8");
                writer.putContent(file);

                return null;
            }
        });

        return file;
    }
}
