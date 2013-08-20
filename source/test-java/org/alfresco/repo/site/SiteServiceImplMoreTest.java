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

package org.alfresco.repo.site;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
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
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = ApplicationContextInit.createStandardContextWithOverrides("classpath:sites/test-"
                                                                                       + TemporarySitesTest.class.getSimpleName() + "-context.xml");
    
    // A rule to manage test nodes reused across all the test methods
    public static TemporarySites STATIC_TEST_SITES = new TemporarySites(APP_CONTEXT_INIT);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain staticRuleChain = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                            .around(STATIC_TEST_SITES);
    
    public RunAsFullyAuthenticatedRule runAllTestsAsAdmin = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getAdminUserName());
    public AlfrescoPerson testUser = new AlfrescoPerson(APP_CONTEXT_INIT);
    
    // Need to ensure the rules are in the correct order so that we're authenticated as admin before trying to create the person.
    @Rule public RuleChain ruleChain = RuleChain.outerRule(runAllTestsAsAdmin)
                                                .around(testUser);
    
    
    // Various services
    private static NamespaceService            NAMESPACE_SERVICE;
    private static SiteService                 SITE_SERVICE;
    private static RetryingTransactionHelper   TRANSACTION_HELPER;
    
    private static String TEST_SITE_NAME, TEST_SUB_SITE_NAME;
    private static TestSiteAndMemberInfo TEST_SITE_WITH_MEMBERS;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        NAMESPACE_SERVICE         = APP_CONTEXT_INIT.getApplicationContext().getBean("namespaceService", NamespaceService.class);
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
}
