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

package org.alfresco.repo.jscript;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionableAspect;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.util.GUID;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule.RunAsUser;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.alfresco.util.test.junitrules.TemporarySites.TestSiteAndMemberInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestName;


/**
 * @author Neil Mc Erlean
 * @since 4.1.7, 4.2
 */
public class ScriptNodeTest
{
    private static Log log = LogFactory.getLog(ScriptNodeTest.class);

    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    // A rule to manage a test site with 4 users.
    public static TemporarySites STATIC_TEST_SITES = new TemporarySites(APP_CONTEXT_INIT);
    
    // A rule to manage test nodes reused across all the test methods
    public static TemporaryNodes STATIC_TEST_NODES = new TemporaryNodes(APP_CONTEXT_INIT);
    
    public static final String USER_ONE_NAME = "UserOne";
    public static final String USER_TWO_NAME = "UserTwo";
    // Rules to create 2 test users.
    public static AlfrescoPerson TEST_USER1 = new AlfrescoPerson(APP_CONTEXT_INIT, USER_ONE_NAME);
    public static AlfrescoPerson TEST_USER2 = new AlfrescoPerson(APP_CONTEXT_INIT, USER_TWO_NAME);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain STATIC_RULE_CHAIN = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                            .around(STATIC_TEST_SITES)
                                                            .around(STATIC_TEST_NODES)
                                                            .around(TEST_USER1)
                                                            .around(TEST_USER2);
    
    @Rule public final TestName testName = new TestName();
    
    // A JUnit Rule to manage test nodes use in each test method
    public TemporaryNodes testNodes = new TemporaryNodes(APP_CONTEXT_INIT);
    
    // A rule to allow individual test methods all to be run as "UserOne".
    public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(TEST_USER1);
    
    // Tie them together in a non-static rule chain.
    @Rule public RuleChain ruleChain = RuleChain.outerRule(runAsRule)
                                                .around(testNodes);
    
    // Various services
    private static ContentService              CONTENT_SERVICE;
    private static NodeService                 NODE_SERVICE;
    private static ServiceRegistry             SERVICE_REGISTRY;
    private static RetryingTransactionHelper   TRANSACTION_HELPER;
    private static PermissionServiceSPI        PERMISSION_SERVICE;
    private static Search                      SEARCH_SCRIPT;
    private static VersionableAspect           VERSIONABLE_ASPECT;
    private static VersionService              VERSION_SERVICE;
    
    private static TestSiteAndMemberInfo USER_ONES_TEST_SITE;
    private static NodeRef               USER_ONES_TEST_FILE;
    
    private List<String> excludedOnUpdateProps;
    private NodeRef testNode;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        CONTENT_SERVICE    = APP_CONTEXT_INIT.getApplicationContext().getBean("ContentService", ContentService.class);
        NODE_SERVICE       = APP_CONTEXT_INIT.getApplicationContext().getBean("NodeService", NodeService.class);
        SERVICE_REGISTRY   = APP_CONTEXT_INIT.getApplicationContext().getBean("ServiceRegistry", ServiceRegistry.class);
        TRANSACTION_HELPER = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        PERMISSION_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("permissionService", PermissionServiceSPI.class);
        SEARCH_SCRIPT      = APP_CONTEXT_INIT.getApplicationContext().getBean("searchScript", Search.class);
        VERSIONABLE_ASPECT = APP_CONTEXT_INIT.getApplicationContext().getBean("versionableAspect", VersionableAspect.class);
        VERSION_SERVICE    = APP_CONTEXT_INIT.getApplicationContext().getBean("VersionService", VersionService.class);
        
        USER_ONES_TEST_SITE = STATIC_TEST_SITES.createTestSiteWithUserPerRole(GUID.generate(), "sitePreset", SiteVisibility.PRIVATE, USER_ONE_NAME);
        USER_ONES_TEST_FILE = STATIC_TEST_NODES.createQuickFile(MimetypeMap.MIMETYPE_TEXT_PLAIN, USER_ONES_TEST_SITE.doclib, "test.txt", USER_ONE_NAME);
    }
    
    @Before public void createTestContent()
    {
        Repository repositoryHelper = (Repository) APP_CONTEXT_INIT.getApplicationContext().getBean("repositoryHelper");
        NodeRef companyHome = repositoryHelper.getCompanyHome();
        
        // Create some test content
        testNode = testNodes.createQuickFile(MimetypeMap.MIMETYPE_TEXT_PLAIN, companyHome, "userOnesDoc", TEST_USER1.getUsername(), true);
        
        excludedOnUpdateProps = VERSIONABLE_ASPECT.getExcludedOnUpdateProps();
    }
    
    @After public void versionableAspectTearDown()
    {
        VERSIONABLE_ASPECT.setExcludedOnUpdateProps(excludedOnUpdateProps);
        VERSIONABLE_ASPECT.afterDictionaryInit();
    }
    
    @Test(expected=AccessDeniedException.class)
    @RunAsUser(userName=USER_TWO_NAME)
    public void userTwoCannotAccessTestFile() throws Exception
    {
        touchFileToTriggerPermissionCheck(USER_ONES_TEST_FILE);
    }

    @Test public void userOneCanAccessTestFile() throws Exception
    {
        touchFileToTriggerPermissionCheck(USER_ONES_TEST_FILE);
    }
    
    private void touchFileToTriggerPermissionCheck(final NodeRef noderef)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // We don't actually care about the path of the NodeRef.
                // We just want to access some state of the NodeRef that will throw an AccessDenied if the current user
                // doesn't have the correct permissions.
                NODE_SERVICE.getPath(noderef);
                
                return null;
            }
        });
    }
    
    /** See ALF-15010 */
    @Test public void findNode_ALF15010() throws Exception
    {
        // Set the READ permission for the USER_TWO to false, so he cannot access the node
        // created by USER_ONE
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        PERMISSION_SERVICE.setPermission(USER_ONES_TEST_FILE, USER_TWO_NAME, PermissionService.READ, false);
        
        // Now that USER_TWO doesn't have the READ permission, we should get
        // null rather than AccessDeniedException.
        // Note: AccessDeniedException was thrown upon retrieving a property of the node
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO_NAME);
        ScriptNode scriptNode = SEARCH_SCRIPT.findNode(USER_ONES_TEST_FILE);
        assertNull(scriptNode);
        
        // USER_ONE is the node creator, so he can access the node
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE_NAME);
        scriptNode = SEARCH_SCRIPT.findNode(USER_ONES_TEST_FILE);
        assertNotNull(scriptNode);

        // Give USER_TWO READ permission
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        PERMISSION_SERVICE.setPermission(USER_ONES_TEST_FILE, USER_TWO_NAME, PermissionService.READ, true);

        // Now USER_TWO can access the node created by USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO_NAME);
        scriptNode = SEARCH_SCRIPT.findNode(USER_ONES_TEST_FILE);
        assertNotNull(scriptNode);
        
        // cleanup
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        PERMISSION_SERVICE.clearPermission(USER_ONES_TEST_FILE, USER_TWO_NAME);
    }
    
    /** See ALF-19783. */
    @Test public void versionNumberShouldIncrementOnNodeRevert()
    {
       log.debug(testName.getMethodName() + "()");
       
       // We've already got a test node set up. Let's see what its content is so we can ensure that the revert works.
       final String originalContent = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<String>()
       {
           public String execute() throws Throwable
           {
               return CONTENT_SERVICE.getReader(testNode, ContentModel.PROP_CONTENT).getContentString();
           }
       });
       log.debug("Test node's original content is: '" + originalContent + "'");
       
       
       // This is the content we'll be updating it with.
       final String updatedContent = "If a tree falls in a forest and there is no one there to hear it, will it make a sound?";
       
       // Let's do some basic sanity checking on this initial version.
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
       {
           public Void execute() throws Throwable
           {
               VersionHistory history = VERSION_SERVICE.getVersionHistory(testNode);
               log.debug("Node version history: " + history);
               
               Version version1_0 = history.getHeadVersion();
               
               assertEquals("Incorrect version label",     version1_0.getVersionLabel(),     history.getHeadVersion().getVersionLabel());
               assertEquals("Incorrect head version node", version1_0.getVersionedNodeRef(), history.getHeadVersion().getVersionedNodeRef());
               assertEquals("Incorrect history size",      1,                                history.getAllVersions().size());
               
               Version[] versions = history.getAllVersions().toArray(new Version[0]);
               assertEquals("Incorrect version label", "1.0", versions[0].getVersionLabel());
               assertEquals("Incorrect version label", "1.0", NODE_SERVICE.getProperty(testNode, ContentModel.PROP_VERSION_LABEL));
               
               return null;
           }
       });
       
       final Version version1_1 = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Version>()
       {
           public Version execute() throws Throwable
           {
               // Now let's change the content value...
               ContentWriter contentWriter = CONTENT_SERVICE.getWriter(testNode, ContentModel.PROP_CONTENT, true);
               assertNotNull(contentWriter);
               contentWriter.putContent(updatedContent);
               
               // ... and record this as a new version of the node
               return VERSION_SERVICE.createVersion(testNode, null);
           }
       });
       log.debug("Stored next version of node: " + version1_1.getVersionLabel());
       
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
       {
           public Void execute() throws Throwable
           {
               // Check we're now seeing both versions in the history
               VersionHistory history = VERSION_SERVICE.getVersionHistory(testNode);
               log.debug("Node version history: " + history);
               assertEquals(version1_1.getVersionLabel(),     history.getHeadVersion().getVersionLabel());
               assertEquals(version1_1.getVersionedNodeRef(), history.getHeadVersion().getVersionedNodeRef());
               assertEquals(2,                              history.getAllVersions().size());
               
               Version[] versions = history.getAllVersions().toArray(new Version[0]);
               assertEquals("1.1", versions[0].getVersionLabel());
               assertEquals("1.0", versions[1].getVersionLabel());
               assertEquals("1.1", NODE_SERVICE.getProperty(testNode, ContentModel.PROP_VERSION_LABEL));
               
               return null;
           }
       });
       
       // Now we'll revert the node to a specific, named previous version.
       // Note: we're doing this through a call to scriptNode.revert(...) as that is what Share does via revert.post.desc.xml
       // A straight call to VERSION_SERVICE.revert(testNode, version1_0); would not work here as ScriptNode.revert also adds a checkout/checkin call to the revert.
       // Rather than reproduce what ScriptNode does in this class, we'll just call ScriptNode.revert
       
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
       {
           public Void execute() throws Throwable
           {
               log.debug("Reverting versionable node to version 1.0 ...");
               
               ScriptNode sn = new ScriptNode(testNode, SERVICE_REGISTRY);
               sn.revert("", false, "1.0");
               
               return null;
           }
       });
       
       TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
       {
           public Void execute() throws Throwable
           {
               // Check that the version label is correct
               assertEquals("1.2", NODE_SERVICE.getProperty(testNode, ContentModel.PROP_VERSION_LABEL));
               
               // Check that the content is correct
               ContentReader contentReader = CONTENT_SERVICE.getReader(testNode, ContentModel.PROP_CONTENT);
               assertNotNull(contentReader);
               assertEquals(originalContent, contentReader.getContentString());
               
               // Check the history still has 3 versions
               // The head version is now 1.2
               VersionHistory history = VERSION_SERVICE.getVersionHistory(testNode);
               log.debug("Node version history: " + history);
               for (Version v : history.getAllVersions()) { log.debug(v.getVersionLabel()); }
               
               final Version version1_2 = history.getHeadVersion();
               
               assertEquals(version1_2.getVersionLabel(), history.getHeadVersion().getVersionLabel());
               assertEquals(version1_2.getVersionedNodeRef(), history.getHeadVersion().getVersionedNodeRef());
               assertEquals(3, history.getAllVersions().size());
               
               Version[] versions = history.getAllVersions().toArray(new Version[0]);
               assertEquals("1.2", versions[0].getVersionLabel());
               assertEquals("1.1", versions[1].getVersionLabel());
               assertEquals("1.0", versions[2].getVersionLabel());
               
               assertEquals("1.2", history.getHeadVersion().getVersionLabel());
               return null;
           }
       });
    }
}
