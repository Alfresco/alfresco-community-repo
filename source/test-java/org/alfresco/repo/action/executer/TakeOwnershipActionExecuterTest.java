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

import static org.junit.Assert.assertEquals;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.alfresco.util.test.junitrules.TemporarySites.TestSiteAndMemberInfo;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

/** Tests for {@link TakeOwnershipActionExecuter}. */
@Category(OwnJVMTestsCategory.class)
public class TakeOwnershipActionExecuterTest
{
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    public static final String USER_ONE_NAME = "UserOne";
    public static final String USER_TWO_NAME = "UserTwo";
    
    // Rules to create 2 test users.
    public static AlfrescoPerson TEST_USER1 = new AlfrescoPerson(APP_CONTEXT_INIT, USER_ONE_NAME);
    
    // Rules to manage test sites & nodes reused across all the test methods
    public static TemporarySites TEMP_SITES = new TemporarySites(APP_CONTEXT_INIT);
    public static TemporaryNodes TEMP_NODES = new TemporaryNodes(APP_CONTEXT_INIT);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain STATIC_RULE_CHAIN = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                                    .around(TEST_USER1)
                                                                    .around(TEMP_NODES)
                                                                    .around(TEMP_SITES);
    
    // A rule to allow individual test methods all to be run as "UserOne".
    // Some test methods need to switch user during execution which they are free to do.
    @Rule public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(TEST_USER1);

    private static OwnableService              OWNABLE_SERVICE;
    private static RetryingTransactionHelper   TRANSACTION_HELPER;
    
    private static TestSiteAndMemberInfo SITE_INFO;
    private static NodeRef TEST_DOC;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        OWNABLE_SERVICE    = APP_CONTEXT_INIT.getApplicationContext().getBean("OwnableService", OwnableService.class);
        TRANSACTION_HELPER = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        
        // One user creates a site.
        final String siteShortName = GUID.generate();
        SITE_INFO = TEMP_SITES.createTestSiteWithUserPerRole(siteShortName,
                                                             "sitePreset",
                                                             SiteVisibility.PUBLIC,
                                                             TEST_USER1.getUsername());
        // A site contributor creates a document in it.
        TEST_DOC  = TEMP_NODES.createNode(SITE_INFO.doclib, "userOnesDoc", ContentModel.TYPE_CONTENT, SITE_INFO.siteContributor);
    }
    
    @Test public void siteOwnerCanTakeOwnershipOfNodeViaAnAction() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Initially the site contributor is the owner of a document (node).
                assertEquals("Wrong owner at test start.", SITE_INFO.siteContributor, OWNABLE_SERVICE.getOwner(TEST_DOC));
                
                // Then the site owner (user 1) takes ownership of the document.
                // See e.g. https://docs.alfresco.com/4.1/concepts/cuh-user-roles-permissions.html
                //     for details on who has permission to take ownership.
                assertEquals(TEST_USER1.getUsername(), AuthenticationUtil.getRunAsUser());
                
                TakeOwnershipActionExecuter executor = APP_CONTEXT_INIT.getApplicationContext().
                                                                        getBean(TakeOwnershipActionExecuter.NAME,
                                                                                TakeOwnershipActionExecuter.class);
                final String actionId = GUID.generate();
                ActionImpl action = new ActionImpl(null, actionId, TakeOwnershipActionExecuter.NAME, null);
                
                executor.execute(action, TEST_DOC);
                
                // Now the node should be owned by User 1.
                assertEquals("Wrong owner at test end.", TEST_USER1.getUsername(), OWNABLE_SERVICE.getOwner(TEST_DOC));
                
                return null;
            }
        });
    }
}

