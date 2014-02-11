/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.util.test.junitrules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

/**
 * Test class for {@link TemporaryNodes}.
 * 
 * @author Neil Mc Erlean
 * @since 4.1
 */
@Category(OwnJVMTestsCategory.class)
public class TemporaryNodesTest
{
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    // Rules to create test users.
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
    
    // A rule to allow individual test methods all to be run as "admin".
    @Rule public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getAdminUserName());
    
    // Various services
    private static CheckOutCheckInService      COCI_SERVICE;
    private static ContentService              CONTENT_SERVICE;
    private static NodeService                 NODE_SERVICE;
    private static SiteService                 SITE_SERVICE;
    private static RetryingTransactionHelper   TRANSACTION_HELPER;
    private static VersionService              VERSION_SERVICE;
    
    private static NodeRef COMPANY_HOME;
    
    // These NodeRefs are used by the test methods.
    private NodeRef testNode1, testNode2;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        COCI_SERVICE       = APP_CONTEXT_INIT.getApplicationContext().getBean("checkOutCheckInService", CheckOutCheckInService.class);
        CONTENT_SERVICE    = APP_CONTEXT_INIT.getApplicationContext().getBean("ContentService", ContentService.class);
        NODE_SERVICE       = APP_CONTEXT_INIT.getApplicationContext().getBean("NodeService", NodeService.class);
        SITE_SERVICE       = APP_CONTEXT_INIT.getApplicationContext().getBean("SiteService", SiteService.class);
        TRANSACTION_HELPER = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        VERSION_SERVICE    = APP_CONTEXT_INIT.getApplicationContext().getBean("VersionService", VersionService.class);
        
        Repository repositoryHelper = APP_CONTEXT_INIT.getApplicationContext().getBean("repositoryHelper", Repository.class);
        COMPANY_HOME = repositoryHelper.getCompanyHome();
    }
    
    @Before public void createTestContent()
    {
        // Create some test content
        testNode1 = testNodes.createNode(COMPANY_HOME,                  "doc 1",     ContentModel.TYPE_CONTENT, TEST_USER1.getUsername());
        testNode2 = testNodes.createNodeWithTextContent(COMPANY_HOME,   "doc 2",     ContentModel.TYPE_CONTENT, TEST_USER2.getUsername(), "Hello world");
    }
    
    @Test public void ensureTestNodesWereCreatedOk() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertTrue("Test node does not exist", NODE_SERVICE.exists(testNode1));
                assertTrue("Test node does not exist", NODE_SERVICE.exists(testNode2));
                
                Map<QName, Serializable> node1Props = NODE_SERVICE.getProperties(testNode1);
                Map<QName, Serializable> node2Props = NODE_SERVICE.getProperties(testNode2);
                
                // name
                assertEquals("cm:name was wrong", "doc 1", node1Props.get(ContentModel.PROP_NAME));
                assertEquals("cm:name was wrong", "doc 2", node2Props.get(ContentModel.PROP_NAME));
                
                // creator
                assertEquals("cm:creator was wrong", TEST_USER1.getUsername(), node1Props.get(ContentModel.PROP_CREATOR));
                assertEquals("cm:creator was wrong", TEST_USER2.getUsername(), node2Props.get(ContentModel.PROP_CREATOR));
                
                // content
                ContentReader reader = CONTENT_SERVICE.getReader(testNode1, ContentModel.PROP_CONTENT);
                assertNull("Content was unexpectedly present", reader);
                
                reader = CONTENT_SERVICE.getReader(testNode2, ContentModel.PROP_CONTENT);
                assertEquals("Content was wrong", "Hello world", reader.getContentString("Hello world".length()));
                
                return null;
            }
        });
    }
    
    @Test public void ensureCheckedOutNodesAreCleanedUp() throws Throwable
    {
        // Note that because we need to test that the Rule's 'after' behaviour has worked correctly, we cannot
        // use the Rule that has been declared in the normal way - otherwise nothing would be cleaned up until
        // after our test method.
        // Therefore we have to manually poke the Rule to get it to cleanup during test execution.
        // NOTE! This is *not* how a JUnit Rule would normally be used.
        TemporaryNodes myTemporaryNodes = new TemporaryNodes(APP_CONTEXT_INIT);
        
        // Currently this is a no-op, but just in case that changes.
        myTemporaryNodes.before();
        
        
        // Create some test nodes.
        final List<NodeRef> nodesThatShouldBeDeletedByRule = new ArrayList<NodeRef>();
        
        nodesThatShouldBeDeletedByRule.add(myTemporaryNodes.createNode(COMPANY_HOME, "normal node", ContentModel.TYPE_CONTENT, TEST_USER1.getUsername()));
        final NodeRef checkedoutNode = myTemporaryNodes.createNode(COMPANY_HOME, "checkedout node", ContentModel.TYPE_CONTENT, TEST_USER1.getUsername());
        nodesThatShouldBeDeletedByRule.add(checkedoutNode);
        
        // and check one of them out.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                NodeRef workingCopy = COCI_SERVICE.checkout(checkedoutNode);
                
                // Ensure that the working copy is cleaned up too.
                nodesThatShouldBeDeletedByRule.add(workingCopy);
                return null;
            }
        });
        
        // Now trigger the Rule's cleanup behaviour.
        myTemporaryNodes.after();
        
        // and ensure that the nodes are all gone.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                for (NodeRef node : nodesThatShouldBeDeletedByRule)
                {
                    if (NODE_SERVICE.exists(node))
                    {
                        fail("Node '" + NODE_SERVICE.getProperty(node, ContentModel.PROP_NAME) + "' still exists.");
                    }
                }
                return null;
            }
        });
    }
    
    /**
     * This test ensures that any temporary nodes which are versioned, have all their various versions cleaned up too.
     * @since 4.1.7
     */
    @Test public void ensureVersionedNodesAreFullyCleanedUp() throws Throwable
    {
        // Note that because we need to test that the Rule's 'after' behaviour has worked correctly, we cannot
        // use the Rule that has been declared in the normal way - otherwise nothing would be cleaned up until
        // after our test method.
        // Therefore we have to manually poke the Rule to get it to cleanup during test execution.
        // NOTE! This is *not* how a JUnit Rule would normally be used.
        final TemporaryNodes myTemporaryNodes = new TemporaryNodes(APP_CONTEXT_INIT);
        
        // Currently this is a no-op, but just in case that changes.
        myTemporaryNodes.before();
        
        
        final List<NodeRef> nodesThatShouldBeDeletedByRule = new ArrayList<NodeRef>();
        
        // Create a versioned, temporary node
        final NodeRef versionedTempNode = myTemporaryNodes.createQuickFile(MimetypeMap.MIMETYPE_TEXT_PLAIN,
                                                                           COMPANY_HOME,
                                                                           "versionableNode",
                                                                           TEST_USER1.getUsername(),
                                                                           true);
        
        nodesThatShouldBeDeletedByRule.add(versionedTempNode);
        
        // Create various versions
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertTrue("The test node was not versioned as it should be.", VERSION_SERVICE.isVersioned(versionedTempNode));
                
                Version v1 = VERSION_SERVICE.createVersion(versionedTempNode, null);
                Version v2 = VERSION_SERVICE.createVersion(versionedTempNode, null);
                Version v3 = VERSION_SERVICE.createVersion(versionedTempNode, null);
                
                // Ensure that these version nodes are all cleaned up too.
                for (Version v : new Version[] {v1, v2, v3})
                {
                    nodesThatShouldBeDeletedByRule.add(v.getFrozenStateNodeRef());
                }
                
                return null;
            }
        });
        
        // Now trigger the Rule's cleanup behaviour.
        myTemporaryNodes.after();
        
        // and ensure that the nodes are all gone.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                for (NodeRef node : nodesThatShouldBeDeletedByRule)
                {
                    if (NODE_SERVICE.exists(node))
                    {
                        fail("Node '" + NODE_SERVICE.getProperty(node, ContentModel.PROP_NAME) + "' still exists.");
                    }
                }
                return null;
            }
        });
    }
    
    /** Site nodes are a special case as they can only be deleted through the SiteService. */
    @Test public void ensureSiteNodesAreCleanedUp() throws Throwable
    {
        // Note that because we need to test that the Rule's 'after' behaviour has worked correctly, we cannot
        // use the Rule that has been declared in the normal way - otherwise nothing would be cleaned up until
        // after our test method.
        // Therefore we have to manually poke the Rule to get it to cleanup during test execution.
        // NOTE! This is *not* how a JUnit Rule would normally be used.
        TemporaryNodes myTemporaryNodes = new TemporaryNodes(APP_CONTEXT_INIT);
        
        // Currently this is a no-op, but just in case that changes.
        myTemporaryNodes.before();
        
        
        // and ensure that the site node is gone.
        SiteInfo createdSite = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<SiteInfo>()
        {
            public SiteInfo execute() throws Throwable
            {
                return SITE_SERVICE.createSite("sitePreset", "siteShortName", "site title", "site description", SiteVisibility.PUBLIC);
            }
        });
        final NodeRef siteNodeRef = createdSite.getNodeRef();
        myTemporaryNodes.addNodeRef(siteNodeRef);
        
        // Now trigger the Rule's cleanup behaviour.
        myTemporaryNodes.after();
        
        // and ensure that the site node is gone.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                if (NODE_SERVICE.exists(siteNodeRef))
                {
                    fail("Node '" + NODE_SERVICE.getProperty(siteNodeRef, ContentModel.PROP_NAME) + "' still exists.");
                }
                return null;
            }
        });
    }
    
    @Test public void testCreateFolderAndQuickFiles() throws Throwable
    {
        // Note that because we need to test that the Rule's 'after' behaviour has worked correctly, we cannot
        // use the Rule that has been declared in the normal way - otherwise nothing would be cleaned up until
        // after our test method.
        // Therefore we have to manually poke the Rule to get it to cleanup during test execution.
        // NOTE! This is *not* how a JUnit Rule would normally be used.
        TemporaryNodes myTemporaryNodes = new TemporaryNodes(APP_CONTEXT_INIT);
        
        // Currently this is a no-op, but just in case that changes.
        myTemporaryNodes.before();
        
        
        
        // Create the temporary nodes relevant for this test.
        //
        // Create a target folder
        final NodeRef folder = myTemporaryNodes.createFolder(COMPANY_HOME, "testFolder", AuthenticationUtil.getAdminUserName());
        
        // create a normal 'quick' node under it.
        final NodeRef quickTxt = myTemporaryNodes.createQuickFile(MimetypeMap.MIMETYPE_TEXT_PLAIN, folder, "quickFile", AuthenticationUtil.getAdminUserName());
        
        // create a named 'quick' node under it.
        final NodeRef namedQuickTxt = myTemporaryNodes.createQuickFileByName("quickCorrupt.jpg", folder, AuthenticationUtil.getAdminUserName());
        
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Check the nodes were created ok
                assertTrue(NODE_SERVICE.exists(folder));
                assertTrue(NODE_SERVICE.exists(quickTxt));
                assertTrue(NODE_SERVICE.exists(namedQuickTxt));
                
                assertEquals(ContentModel.TYPE_FOLDER, NODE_SERVICE.getType(folder));
                assertEquals(ContentModel.TYPE_CONTENT, NODE_SERVICE.getType(quickTxt));
                assertEquals(ContentModel.TYPE_CONTENT, NODE_SERVICE.getType(namedQuickTxt));
                
                assertEquals(AuthenticationUtil.getAdminUserName(), NODE_SERVICE.getProperty(folder, ContentModel.PROP_CREATOR));
                assertEquals(AuthenticationUtil.getAdminUserName(), NODE_SERVICE.getProperty(quickTxt, ContentModel.PROP_CREATOR));
                assertEquals(AuthenticationUtil.getAdminUserName(), NODE_SERVICE.getProperty(namedQuickTxt, ContentModel.PROP_CREATOR));
                
                ContentReader reader = CONTENT_SERVICE.getReader(quickTxt, ContentModel.PROP_CONTENT);
                assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, reader.getMimetype());
                
                assertEquals(235, reader.getSize()); // 235 chars in the quick.txt file
                final String content = reader.getContentString();
                assertTrue(content.contains("quick brown fox"));
                
                ContentReader reader2 = CONTENT_SERVICE.getReader(namedQuickTxt, ContentModel.PROP_CONTENT);
                assertEquals(MimetypeMap.MIMETYPE_IMAGE_JPEG, reader2.getMimetype());
                // No checks on the actual content.
                
                return null;
            }
        });
        
        // Now trigger the Rule's cleanup behaviour.
        myTemporaryNodes.after();
        
        // and ensure that the temporary nodes are gone.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertFalse(NODE_SERVICE.exists(folder));
                assertFalse(NODE_SERVICE.exists(quickTxt));
                assertFalse(NODE_SERVICE.exists(namedQuickTxt));
                return null;
            }
        });
    }
}
