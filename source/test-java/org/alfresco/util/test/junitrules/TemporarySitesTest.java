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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.test.junitrules.TemporarySites.TestSiteAndMemberInfo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

/**
 * Test class for {@link TemporarySites}.
 * 
 * @author Neil McErlean
 * @since 4.0.3
 */
@Category(OwnJVMTestsCategory.class)
public class TemporarySitesTest
{
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT =
            ApplicationContextInit.createStandardContextWithOverrides("classpath:sites/test-"
                                                                                       + TemporarySitesTest.class.getSimpleName() + "-context.xml");
    
    // A rule to manage test sites reused across all the test methods
    public static TemporaryNodes STATIC_TEST_SITES = new TemporaryNodes(APP_CONTEXT_INIT);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain ruleChain = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                            .around(STATIC_TEST_SITES);
    
    // A rule to manage test sites use in each test method
    public TemporarySites testSites = new TemporarySites(APP_CONTEXT_INIT);
    
    // A rule to allow individual test methods all to be run as "admin".
    public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getAdminUserName());
    
    // A non-static rule chain to ensure execution order is correct.
    @Rule public RuleChain nonStaticRules = RuleChain.outerRule(runAsRule)
                                                        .around(testSites);
    
    // Various services
    private static NamespaceService            NAMESPACE_SERVICE;
    private static NodeService                 NODE_SERVICE;
    private static SiteService                 SITE_SERVICE;
    private static RetryingTransactionHelper   TRANSACTION_HELPER;
    
    // These SiteInfos are used by the test methods.
    private SiteInfo testSite1, testSite2;
    private TestSiteAndMemberInfo testSiteWithMembers;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        NAMESPACE_SERVICE  = APP_CONTEXT_INIT.getApplicationContext().getBean("namespaceService", NamespaceService.class);
        NODE_SERVICE       = APP_CONTEXT_INIT.getApplicationContext().getBean("nodeService", NodeService.class);
        SITE_SERVICE       = APP_CONTEXT_INIT.getApplicationContext().getBean("siteService", SiteService.class);
        TRANSACTION_HELPER = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
    }
    
    @Before public void createTestContent()
    {
        // Create some test content
        final String guid = GUID.generate();
        
        testSite1 = testSites.createSite("sitePreset", "testSite1_" + guid, "t", "d", SiteVisibility.PUBLIC, AuthenticationUtil.getAdminUserName());
        final QName subSiteType = QName.createQName("testsite", "testSubsite", NAMESPACE_SERVICE);
        testSite2 = testSites.createSite("sitePreset", "testSite2_" + guid, "T", "D", SiteVisibility.PUBLIC, subSiteType, AuthenticationUtil.getAdminUserName());
        
        testSiteWithMembers = testSites.createTestSiteWithUserPerRole(GUID.generate(), "sitePreset", SiteVisibility.PUBLIC, AuthenticationUtil.getAdminUserName());
    }
    
    @Test public void ensureTestSitesWereCreatedOk() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                final SiteInfo recoveredSite1 = SITE_SERVICE.getSite(testSite1.getShortName());
                final SiteInfo recoveredSite2 = SITE_SERVICE.getSite(testSite2.getShortName());
                
                assertNotNull("Test site does not exist", recoveredSite1);
                assertNotNull("Test site does not exist", recoveredSite2);
                
                assertEquals("cm:title was wrong", "t", recoveredSite1.getTitle());
                assertEquals("cm:description was wrong", "d", recoveredSite1.getDescription());
                assertEquals("preset was wrong", "sitePreset", recoveredSite1.getSitePreset());
                
                assertEquals("site visibility was wrong", SiteVisibility.PUBLIC, recoveredSite1.getVisibility());
                
                for (String siteShortName : new String[] { testSite1.getShortName(), testSite2.getShortName() })
                {
                    assertNotNull("site had no doclib container node", SITE_SERVICE.getContainer(siteShortName, SiteService.DOCUMENT_LIBRARY));
                }
                
                return null;
            }
        });
    }
    
    @Test public void ensureUsersWithShareRolesArePresentAndCorrect() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                final String shortName = testSiteWithMembers.siteInfo.getShortName();
                final SiteInfo recoveredSite = SITE_SERVICE.getSite(shortName);
                
                assertNotNull("Test site does not exist", recoveredSite);
                
                assertEquals(SiteModel.SITE_MANAGER,      SITE_SERVICE.getMembersRole(shortName, testSiteWithMembers.siteManager));
                assertEquals(SiteModel.SITE_COLLABORATOR, SITE_SERVICE.getMembersRole(shortName, testSiteWithMembers.siteCollaborator));
                assertEquals(SiteModel.SITE_CONTRIBUTOR,  SITE_SERVICE.getMembersRole(shortName, testSiteWithMembers.siteContributor));
                assertEquals(SiteModel.SITE_CONSUMER,     SITE_SERVICE.getMembersRole(shortName, testSiteWithMembers.siteConsumer));
                
                assertNotNull(testSiteWithMembers.doclib);
                assertTrue("Site doclib was not pre-created.", NODE_SERVICE.exists(testSiteWithMembers.doclib));
                assertEquals("Site doclib was in wrong place.", testSiteWithMembers.siteInfo.getNodeRef(),
                                                                NODE_SERVICE.getPrimaryParent(testSiteWithMembers.doclib).getParentRef());
                
                return null;
            }
        });
    }
}
