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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule.RunAsUser;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.alfresco.util.test.junitrules.TemporarySites.TestSiteAndMemberInfo;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;


/**
 * @author Neil Mc Erlean
 * @since 4.2
 */
public class ScriptNodeTest
{
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
    
    // A rule to allow individual test methods all to be run as "UserOne".
    @Rule public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(TEST_USER1);

    // Various services
    private static NodeService                 NODE_SERVICE;
    private static RetryingTransactionHelper   TRANSACTION_HELPER;
    private static PermissionServiceSPI        PERMISSION_SERVICE;
    private static Search                      SEARCH_SCRIPT;
    
    private static TestSiteAndMemberInfo USER_ONES_TEST_SITE;
    private static NodeRef               USER_ONES_TEST_FILE;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        NODE_SERVICE              = (NodeService)                 APP_CONTEXT_INIT.getApplicationContext().getBean("NodeService");
        TRANSACTION_HELPER        = (RetryingTransactionHelper)   APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper");        
        PERMISSION_SERVICE = (PermissionServiceSPI) APP_CONTEXT_INIT.getApplicationContext().getBean("permissionService");
        SEARCH_SCRIPT = (Search) APP_CONTEXT_INIT.getApplicationContext().getBean("searchScript");
        
        USER_ONES_TEST_SITE = STATIC_TEST_SITES.createTestSiteWithUserPerRole(GUID.generate(), "sitePreset", SiteVisibility.PRIVATE, USER_ONE_NAME);
        USER_ONES_TEST_FILE = STATIC_TEST_NODES.createQuickFile(MimetypeMap.MIMETYPE_TEXT_PLAIN, USER_ONES_TEST_SITE.doclib, "test.txt", USER_ONE_NAME);       
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
}
