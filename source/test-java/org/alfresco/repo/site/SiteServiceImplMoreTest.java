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

package org.alfresco.repo.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.repo.node.archive.RestoreNodeReport.RestoreStatus;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.alfresco.util.test.junitrules.TemporarySites.TestSiteAndMemberInfo;
import org.alfresco.util.test.junitrules.TemporarySitesTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestName;
import org.springframework.extensions.webscripts.GUID;

/**
 * This class contains some tests for the {@link SiteServiceImpl} - in addition to those already
 * included in {@link SiteServiceImplTest}. This uses JUnit 4 annotations and JUnit Rules.
 * 
 * TODO Refactor the two classes together into one common approach.
 * 
 * @author Neil Mc Erlean
 * @since 4.0.3
 */
public class SiteServiceImplMoreTest
{
    protected static Log log = LogFactory.getLog(SiteServiceImplMoreTest.class);
    
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = ApplicationContextInit.createStandardContextWithOverrides("classpath:sites/test-"
                                                                                       + TemporarySitesTest.class.getSimpleName() + "-context.xml");
    
    // A rule to manage test nodes reused across all the test methods
    public static TemporarySites STATIC_TEST_SITES = new TemporarySites(APP_CONTEXT_INIT);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain staticRuleChain = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                            .around(STATIC_TEST_SITES);
    
    public RunAsFullyAuthenticatedRule runAllTestsAsAdmin = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getAdminUserName());
    public TemporarySites perMethodTestSites = new TemporarySites(APP_CONTEXT_INIT);
    public AlfrescoPerson testUser = new AlfrescoPerson(APP_CONTEXT_INIT);
    
    // Need to ensure the rules are in the correct order so that we're authenticated as admin before trying to create the person.
    @Rule public RuleChain ruleChain = RuleChain.outerRule(runAllTestsAsAdmin)
                                                .around(perMethodTestSites)
                                                .around(testUser);
    
    @Rule public TestName testName = new TestName();
    
    // Various services
    private static AuthorityService            AUTHORITY_SERVICE;
    private static NamespaceService            NAMESPACE_SERVICE;
    private static NodeService                 NODE_SERVICE;
    private static NodeArchiveService          NODE_ARCHIVE_SERVICE;
    private static SiteService                 SITE_SERVICE;
    private static RetryingTransactionHelper   TRANSACTION_HELPER;
    
    private static String TEST_SITE_NAME, TEST_SUB_SITE_NAME;
    private static TestSiteAndMemberInfo TEST_SITE_WITH_MEMBERS;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        AUTHORITY_SERVICE         = APP_CONTEXT_INIT.getApplicationContext().getBean("AuthorityService", AuthorityService.class);
        NAMESPACE_SERVICE         = APP_CONTEXT_INIT.getApplicationContext().getBean("namespaceService", NamespaceService.class);
        NODE_SERVICE              = APP_CONTEXT_INIT.getApplicationContext().getBean("NodeService", NodeService.class);
        NODE_ARCHIVE_SERVICE      = APP_CONTEXT_INIT.getApplicationContext().getBean("nodeArchiveService", NodeArchiveService.class);
        SITE_SERVICE              = APP_CONTEXT_INIT.getApplicationContext().getBean("siteService", SiteService.class);
        TRANSACTION_HELPER        = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        
        // We'll create this test content as admin.
        final String admin = AuthenticationUtil.getAdminUserName();
        
        TEST_SITE_NAME = GUID.generate();
        TEST_SUB_SITE_NAME = GUID.generate();
        
        final QName subSiteType = QName.createQName("testsite", "testSubsite", NAMESPACE_SERVICE);
        
        STATIC_TEST_SITES.createSite("sitePreset", TEST_SITE_NAME, "siteTitle", "siteDescription", SiteVisibility.PUBLIC, admin);
        STATIC_TEST_SITES.createSite("sitePreset", TEST_SUB_SITE_NAME, "siteTitle", "siteDescription", SiteVisibility.PUBLIC, subSiteType, admin);
        
        TEST_SITE_WITH_MEMBERS = STATIC_TEST_SITES.createTestSiteWithUserPerRole(SiteServiceImplMoreTest.class.getSimpleName(), "sitePreset", SiteVisibility.PUBLIC, admin);
    }
    
    /**
     * This method ensures that {@link SiteService#listSites(String)} includes content subtypes of {@link SiteModel#TYPE_SITE st:site}.
     */
    @Test public void listSitesIncludingSubTypesOfSite() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                PagingResults<SiteInfo> sites = SITE_SERVICE.listSites(null, null, new PagingRequest(0, 1024));
                
                Map<String, SiteInfo> sitesByName = new HashMap<String, SiteInfo>();
                for (SiteInfo site : sites.getPage())
                {
                    sitesByName.put(site.getShortName(), site);
                }
                
                assertNotNull("st:site missing.", sitesByName.get(TEST_SITE_NAME));
                assertNotNull("subtype of st:site missing.", sitesByName.get(TEST_SUB_SITE_NAME));
                
                return null;
            }
        });
    }
    
    /**
     * This method ensures that sites can be deleted by any SiteManager, not just the Site Creator (ALF-15257).
     */
    @Test public void anySiteManagerShouldBeAbleToDeleteASite() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // We already have a site with 4 users created by the TestSiteAndMemberInfo rule above.
                // We'll use the testUser from the rule above - oh how I wish JUnit supported @Test-level rules, so I didn't have to create that user for all tests.
                SITE_SERVICE.setMembership(TEST_SITE_WITH_MEMBERS.siteInfo.getShortName(), testUser.getUsername(), SiteModel.SITE_MANAGER);
                
                // Now switch to run as that user and try to delete the site.
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    @Override public Void doWork() throws Exception
                    {
                        SITE_SERVICE.deleteSite(TEST_SITE_WITH_MEMBERS.siteInfo.getShortName());
                        return null;
                    }
                }, testUser.getUsername());
                
                return null;
            }
        });
    }
    
    /**
     * This test ensures that when sites are deleted (moved to the trashcan) and then restored, that the 4 role-based groups are
     * restored correctly and that any users who were members of those groups are made members once more.
     * 
     * @see SiteServiceImpl#deleteSite(String)
     * @see SiteServiceImpl#onRestoreNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Test public void deleteSiteAndRestoreEnsuringSiteGroupsAreRecovered() throws Exception
    {
        // Implementation note: as of Alfresco 4.2, authorities cannot be archived and are always hard-deleted.
        // Therefore, on site restore, the SiteService *recreates* the deleted groups associated with the restored site.
        // Having recreated them, it then needs to add any old members (users, TODO what about groups?) into those newly created site groups.
        // It does this by writing the site members and their roles onto a property on the st:site node in the archive & reading them on restore.
        
        // Choose a site name that will link back to this test case...
        final String siteShortName = testName.getMethodName();
        log.debug("Creating test site called: " + siteShortName);
        
        // ...and create the site
        final TestSiteAndMemberInfo testSiteAndMemberInfo = perMethodTestSites.createTestSiteWithUserPerRole(siteShortName, "sitePreset", SiteVisibility.PUBLIC, AuthenticationUtil.getAdminUserName());
        
        // Now get the various site-related data that we want to examine after deletion & restoration
        final Map<String, String> userNameToRoleMap =
                TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Map<String, String>>()
        {
            public Map<String, String> execute() throws Throwable
            {
                final Map<String, String> userNameToRoleMap = new HashMap<String, String>();
                
                // Which users are members of which groups?
                for (String role : SITE_SERVICE.getSiteRoles())
                {
                    // putAll here shouldn't overwrite any keys in the above map as each authority should only have one role in a site.
                    userNameToRoleMap.putAll(SITE_SERVICE.listMembers(siteShortName, null, role, 0, true));
                }
                
                // Some sanity checking before we delete the site
                final String siteContributorGroup = SITE_SERVICE.getSiteRoleGroup(siteShortName, "SiteContributor");
                assertTrue("Site contributor user was not in site contributors group", AUTHORITY_SERVICE.getContainedAuthorities(AuthorityType.USER, siteContributorGroup, true).contains(testSiteAndMemberInfo.siteContributor));
                assertEquals("Site contributor user did not have expected Contributor role",
                             SiteModel.SITE_CONTRIBUTOR,
                             userNameToRoleMap.get(testSiteAndMemberInfo.siteContributor));
                
                log.debug("About to delete site.");
                SITE_SERVICE.deleteSite(siteShortName);
                log.debug("Site deleted.");
                
                return userNameToRoleMap;
            }
        });
        
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertThatArchivedNodeExists(testSiteAndMemberInfo.siteInfo.getNodeRef(), "Site node not found in archive.");
                
                // At this point we might assert that the groups associated with the site were gone, but that's an implementation detail really.
                
                log.debug("About to restore site node from archive");
                
                final NodeRef archivedSiteNode = NODE_ARCHIVE_SERVICE.getArchivedNode(testSiteAndMemberInfo.siteInfo.getNodeRef());
                RestoreNodeReport report = NODE_ARCHIVE_SERVICE.restoreArchivedNode(archivedSiteNode);
                // ...which should work
                assertEquals("Failed to restore site from archive", RestoreStatus.SUCCESS, report.getStatus());
                
                log.debug("Successfully restored site from arhive.");
                
                return null;
            }
        });
        
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // The site itself should have been restored, of course...
                assertTrue("The site noderef was not restored as expected", NODE_SERVICE.exists(testSiteAndMemberInfo.siteInfo.getNodeRef()));
                
                // But the group authority nodes should be restored (recreated) as well.
                for (String role : SITE_SERVICE.getSiteRoles())
                {
                    final String siteGroup = SITE_SERVICE.getSiteRoleGroup(siteShortName, role);
                    assertTrue("Site group for role " + role + " did not exist after site restoration",
                               AUTHORITY_SERVICE.authorityExists(siteGroup));
                }
                
                log.debug(SITE_SERVICE.listMembers(siteShortName, null, null, 0, true).size() + " members...");
                for (Map.Entry<String, String> entry : SITE_SERVICE.listMembers(siteShortName, null, null, 0, true).entrySet()) { log.debug(entry); }
                
                // And finally, the original members of the site should have been given the same membership that they had before.
                for (Map.Entry<String, String> entry : userNameToRoleMap.entrySet())
                {
                    assertEquals("Unexpected role for site user: " + entry.getKey(),
                                 entry.getValue(),
                                 SITE_SERVICE.getMembersRole(siteShortName, entry.getKey()));
                }
                
                return null;
            }
        });
    }
    
    private void assertThatArchivedNodeExists(NodeRef originalNodeRef, String failureMsg)
    {
        final NodeRef archivedNodeRef = NODE_ARCHIVE_SERVICE.getArchivedNode(originalNodeRef);
        assertTrue(failureMsg, NODE_SERVICE.exists(archivedNodeRef));
    }
}
