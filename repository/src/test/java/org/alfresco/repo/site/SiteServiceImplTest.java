/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.site;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.admin.SysAdminParamsImpl;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropString;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.person.UserNameMatcherImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteMemberInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.testing.category.LuceneTests;
import org.alfresco.util.testing.category.RedundantTests;

/**
 * Site service implementation unit test
 * 
 * @author Roy Wetherall
 */
@Category({BaseSpringTestsCategory.class, LuceneTests.class})
@Transactional
@ContextConfiguration({"classpath:alfresco/application-context.xml",
        "classpath:org/alfresco/repo/site/site-custom-context.xml"})
public class SiteServiceImplTest extends BaseAlfrescoSpringTest
{
    public static final StoreRef SITE_STORE = new StoreRef("workspace://SpacesStore");

    private static final String TEST_SITE_PRESET = "testSitePreset";
    private static final String TEST_SITE_PRESET_2 = "testSitePreset2";
    private static final String TEST_TITLE = "TitleTest This is my title";
    private static final String TEST_DESCRIPTION = "DescriptionTest This is my description";

    private static final String USER_ONE = "UserOne_SiteServiceImplTest";
    private static final String USER_TWO = "UserTwo_SiteServiceImplTest";
    private static final String USER_THREE = "UserThree_SiteServiceImplTest";
    private static final String USER_FOUR = "UserFour_SiteServiceImplTest";
    private static final String USER_SITE_ADMIN = "UserSiteAdmin_SiteServiceImplTest";
    private static final String GROUP_ONE = "GrpOne_SiteServiceImplTest";
    private static final String GROUP_TWO = "GrpTwo_SiteServiceImplTest";
    private static final String GROUP_THREE = "GrpThree_SiteServiceImplTest";
    private static final String GROUP_FOUR = "GrpFour_SiteServiceImplTest";
    private static final String GROUP_ONE_DISPLAY = "DisplayOfGrpOne-SiteServiceImplTest";
    private static final String GROUP_TWO_DISPLAY = "DisplayOfGrpTwo-SiteServiceImplTest";

    private static boolean IS_FIRST_SETUP = true;

    private CopyService copyService;
    private ScriptService scriptService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private AuthenticationComponent authenticationComponent;
    private TaggingService taggingService;
    private AuthorityService authorityService;
    private FileFolderService fileFolderService;
    private NodeArchiveService nodeArchiveService;
    private PermissionService permissionService;
    private SiteService siteService;
    private UserNameMatcherImpl userNameMatcherImpl;

    /**
     * There are some tests which need access to the unproxied SiteServiceImpl
     */
    private SiteServiceImpl siteServiceImpl;
    private SysAdminParams sysAdminParams;

    private String groupOne;
    private String groupTwo;
    private String groupThree;
    private String groupFour;

    /**
     * Called during the transaction setup
     */
    @SuppressWarnings("deprecation")
    @Before
    public void before() throws Exception
    {
        super.before();
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                // Get a reference to the node service
                nodeService = (NodeService) applicationContext.getBean("nodeService");
                contentService = (ContentService) applicationContext.getBean("contentService");
                authenticationService = (MutableAuthenticationService) applicationContext.getBean("authenticationService");
                actionService = (ActionService) applicationContext.getBean("actionService");
                transactionService = (TransactionService) applicationContext.getBean("transactionComponent");

                // Authenticate as the system user
                authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
                authenticationComponent.setSystemUserAsCurrentUser();

                // Create the store and get the root node
                storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
                rootNodeRef = nodeService.getRootNode(storeRef);

                // Get the required services
                copyService = (CopyService) applicationContext.getBean("CopyService");
                scriptService = (ScriptService) applicationContext.getBean("ScriptService");
                nodeService = (NodeService) applicationContext.getBean("NodeService");
                authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
                taggingService = (TaggingService) applicationContext.getBean("TaggingService");
                authorityService = (AuthorityService) applicationContext.getBean("AuthorityService");
                fileFolderService = (FileFolderService) applicationContext.getBean("FileFolderService");
                nodeArchiveService = (NodeArchiveService) applicationContext.getBean("nodeArchiveService");
                permissionService = (PermissionService) applicationContext.getBean("PermissionService");
                dictionaryService = (DictionaryService) applicationContext.getBean("DictionaryService");
                namespaceService = (NamespaceService) applicationContext.getBean("namespaceService");
                siteService = (SiteService) applicationContext.getBean("SiteService"); // Big 'S'
                siteServiceImpl = (SiteServiceImpl) applicationContext.getBean("siteService"); // Small 's'
                sysAdminParams = (SysAdminParams) applicationContext.getBean("sysAdminParams");
                userNameMatcherImpl = (UserNameMatcherImpl) applicationContext.getBean("userNameMatcher");

                if (IS_FIRST_SETUP)
                {
                    // Create the test users
                    createUser(USER_ONE, "UserOne");
                    createUser(USER_TWO, "UserTwo");
                    createUser(USER_THREE, "UsRthree");
                    createUser(USER_FOUR, "UsRFoUr");
                    createUser(USER_SITE_ADMIN, "UserAdmin");

                    // Create the test groups
                    groupOne = authorityService.createAuthority(AuthorityType.GROUP, GROUP_ONE, GROUP_ONE_DISPLAY, null);
                    authorityService.addAuthority(groupOne, USER_TWO);

                    groupTwo = authorityService.createAuthority(AuthorityType.GROUP, GROUP_TWO, GROUP_TWO_DISPLAY, null);
                    authorityService.addAuthority(groupTwo, USER_TWO);
                    authorityService.addAuthority(groupTwo, USER_THREE);

                    groupThree = authorityService.createAuthority(AuthorityType.GROUP, GROUP_THREE);
                    authorityService.addAuthority(groupThree, USER_TWO);
                    authorityService.addAuthority(groupThree, USER_THREE);

                    groupFour = authorityService.createAuthority(AuthorityType.GROUP, GROUP_FOUR);
                    authorityService.addAuthority(groupThree, groupFour);
                    authorityService.addAuthority(groupFour, USER_FOUR);

                    authorityService.addAuthority("GROUP_SITE_ADMINISTRATORS", USER_SITE_ADMIN);

                    IS_FIRST_SETUP = false;
                }
                else
                {
                    groupOne = authorityService.getName(AuthorityType.GROUP, GROUP_ONE);
                    groupTwo = authorityService.getName(AuthorityType.GROUP, GROUP_TWO);
                    groupThree = authorityService.getName(AuthorityType.GROUP, GROUP_THREE);
                    groupFour = authorityService.getName(AuthorityType.GROUP, GROUP_FOUR);
                }
                // Set the current authentication
                authenticationComponent.setCurrentUser(USER_ONE);

                return null;
            }
        };
        transactionService = (TransactionService) this.applicationContext.getBean("transactionComponent");
        transactionService.getRetryingTransactionHelper().doInTransaction(work, false, true);
    }

    @After
    public void after() throws Exception
    {
        super.after();

        // Reset the sysadmin params on the site service, in case of changes to it
        siteServiceImpl.setSysAdminParams(sysAdminParams);
    }

    /**
     * This test method ensures that public sites can be created and that their site info is correct. It also tests that a duplicate site cannot be created.
     */
    @Test
    public void testCreateSite() throws Exception
    {
        // Create a public site
        String mySiteTest = "mySiteTest" + UUID.randomUUID();
        SiteInfo siteInfo = this.siteService.createSite(TEST_SITE_PRESET, mySiteTest, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, mySiteTest, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        String name = "!Â£$%^&*()_+=-[]{}";
        // Calls deprecated method (still creates a public Site)
        siteInfo = this.siteService.createSite(TEST_SITE_PRESET, name, TEST_TITLE, TEST_DESCRIPTION, true);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, name, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        siteInfo = this.siteService.getSite(name);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, name, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        name = "Ã©Ã­Ã³ÃºÃ�Ã‰Ã�Ã“Ãš";
        siteInfo = this.siteService.createSite(TEST_SITE_PRESET, name, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, name, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        siteInfo = this.siteService.getSite(name);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, name, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        NodeRef siteNodeRef = siteInfo.getNodeRef();
        assertEquals(siteInfo.getShortName(), this.siteService.getSiteShortName(siteNodeRef));

        // Localize the title and description
        Locale locale = Locale.getDefault();
        try
        {
            I18NUtil.setLocale(Locale.FRENCH);
            nodeService.setProperty(siteNodeRef, ContentModel.PROP_TITLE, "Localized-title");
            nodeService.setProperty(siteNodeRef, ContentModel.PROP_DESCRIPTION, "Localized-description");

            siteInfo = this.siteService.getSite(name);
            checkSiteInfo(siteInfo, TEST_SITE_PRESET, name, "Localized-title", "Localized-description", SiteVisibility.PUBLIC);
        }
        finally
        {
            I18NUtil.setLocale(locale);
        }

        // Test for duplicate site error
        try
        {
            this.siteService.createSite(TEST_SITE_PRESET, mySiteTest, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
            fail("Shouldn't allow duplicate site short names.");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }

        try
        {
            // Create a site with an invalid site type
            this.siteService.createSite(TEST_SITE_PRESET, "InvalidSiteType", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC, ServiceRegistry.CMIS_SERVICE);
            fail("Shouldn't allow invalid site type.");
        }
        catch (SiteServiceException ssexception)
        {
            // Expected
        }
    }

    @Test
    public void testHasSite() throws Exception
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                authenticationComponent.setCurrentUser(USER_ONE);
                String publicsite1 = "publicsite1" + UUID.randomUUID();
                String privatesite1 = "privatesite1" + UUID.randomUUID();
                // Create a Public site
                createSite(publicsite1, "doclib", SiteVisibility.PUBLIC);
                // Create a Private site
                createSite(privatesite1, "doclib", SiteVisibility.PRIVATE);

                // ensure USER_TWO has correct visibility - can "get" public site but not a private one, can "has" exist check both
                authenticationComponent.setCurrentUser(USER_TWO);
                assertTrue(siteService.getSite(publicsite1) != null);
                assertTrue(siteService.getSite(privatesite1) == null); // should not be visible to get()
                assertTrue(siteService.hasSite(publicsite1));
                assertTrue(siteService.hasSite(privatesite1)); // should be visible to has() exist check

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.deleteSite(publicsite1);
                siteService.deleteSite(privatesite1);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    /**
     * Test for duplicate site exception where the duplicate is a private site.
     * 
     * @throws Exception
     */
    @Test
    public void testETHREEOH_2133() throws Exception
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                // Test for duplicate site error with a private site
                String siteShortName = "wibble" + UUID.randomUUID();
                siteService.createSite(TEST_SITE_PRESET, siteShortName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);

                authenticationComponent.setCurrentUser(USER_THREE);

                try
                {
                    siteService.createSite(TEST_SITE_PRESET, siteShortName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
                    fail("Shouldn't allow duplicate site short names.");
                }
                catch (AlfrescoRuntimeException exception)
                {
                    // Expected
                }

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.deleteSite(siteShortName);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    /**
     * This method tests https://issues.alfresco.com/jira/browse/ALF-3785 which allows 'public' sites to be only visible to members of a configured group, by default EVERYONE.
     * 
     * <br>
     * <br/>
     * author Neil McErlean
     * 
     * @since 3.4
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testConfigurableSitePublicGroup() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // We'll be configuring a JMX managed bean (in this test method only).
        ChildApplicationContextFactory sysAdminSubsystem = (ChildApplicationContextFactory) applicationContext.getBean("sysAdmin");
        final String sitePublicGroupPropName = "site.public.group";
        final String originalSitePublicGroup = "GROUP_EVERYONE";

        try
        {
            // Firstly we'll ensure that the site.public.group has the correct (pristine) value.
            String groupName = sysAdminSubsystem.getProperty(sitePublicGroupPropName);
            assertEquals(sitePublicGroupPropName + " was not the pristine value",
                    originalSitePublicGroup, groupName);

            // Create a 'normal', unconfigured site.
            SiteInfo unconfiguredSite = siteService.createSite(TEST_SITE_PRESET, "unconfigured",
                    TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
            assertTrue(containsConsumerPermission(originalSitePublicGroup, unconfiguredSite));

            // Now set the managed bean's visibility group to something other than GROUP_EVERYONE.
            // This is the group that will have visibility of subsequently created sites.
            //
            // We'll intentionally set it to a group that DOES NOT EXIST YET.
            String newGroupName = this.getClass().getSimpleName() + System.currentTimeMillis();
            String prefixedNewGroupName = PermissionService.GROUP_PREFIX + newGroupName;

            sysAdminSubsystem.stop();
            sysAdminSubsystem.setProperty(sitePublicGroupPropName, prefixedNewGroupName);
            sysAdminSubsystem.start();

            // Now create a site as before. It should fail as we're using a group that doesn't exist.
            boolean expectedExceptionThrown = false;
            try
            {
                siteService.createSite(TEST_SITE_PRESET, "thisShouldFail",
                        TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
            }
            catch (SiteServiceException expected)
            {
                expectedExceptionThrown = true;
            }
            if (!expectedExceptionThrown)
            {
                fail("Expected exception on createSite with non-existent group was not thrown.");
            }

            // Now we'll create the group used above.
            authorityService.createAuthority(AuthorityType.GROUP, newGroupName);

            // And create the site as before. This time it should succeed.
            SiteInfo configuredSite = siteService.createSite(TEST_SITE_PRESET, "configured",
                    TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

            // And check the permissions on the site.
            assertTrue("The configured site should not have " + originalSitePublicGroup + " as SiteContributor",
                    !containsConsumerPermission(originalSitePublicGroup, configuredSite));
            assertTrue("The configured site should have (newGroupName) as SiteContributor",
                    containsConsumerPermission(prefixedNewGroupName, configuredSite));
        }
        finally
        {
            // Reset the JMX bean to its out-of-the-box values.
            sysAdminSubsystem.stop();
            sysAdminSubsystem.setProperty(sitePublicGroupPropName, originalSitePublicGroup);
            sysAdminSubsystem.start();
        }
    }

    private boolean containsConsumerPermission(final String groupName,
            SiteInfo unconfiguredSite)
    {
        boolean result = false;
        Set<AccessPermission> perms = permissionService.getAllSetPermissions(unconfiguredSite.getNodeRef());
        for (AccessPermission p : perms)
        {
            if (p.getAuthority().equals(groupName) &&
                    p.getPermission().equals(SiteModel.SITE_CONSUMER))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * This method tests that admin and system users can set site membership for a site of which they are not SiteManagers.
     */
    @Test
    public void testETHREEOH_15() throws Exception
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                String mySiteTest = "mySiteTest" + UUID.randomUUID();
                SiteInfo siteInfo = siteService.createSite(TEST_SITE_PRESET, mySiteTest, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
                checkSiteInfo(siteInfo, TEST_SITE_PRESET, mySiteTest, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

                authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
                siteService.setMembership(siteInfo.getShortName(), USER_TWO, SiteModel.SITE_MANAGER);

                authenticationComponent.setCurrentUser(USER_TWO);
                siteService.setMembership(siteInfo.getShortName(), USER_THREE, SiteModel.SITE_CONTRIBUTOR);
                siteService.removeMembership(siteInfo.getShortName(), USER_THREE);

                authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
                siteService.removeMembership(siteInfo.getShortName(), USER_TWO);

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.setMembership(siteInfo.getShortName(), USER_THREE, SiteModel.SITE_CONTRIBUTOR);

                authenticationComponent.setCurrentUser(USER_THREE);
                try
                {
                    siteService.setMembership(siteInfo.getShortName(), USER_TWO, SiteModel.SITE_CONTRIBUTOR);
                    fail("Shouldn't be able to do this cos you don't have permissions");
                }
                catch (Exception exception)
                {}
                try
                {
                    siteService.removeMembership(siteInfo.getShortName(), USER_ONE);
                    fail("Shouldn't be able to do this cos you don't have permissions");
                }
                catch (Exception exception)
                {}
                siteService.removeMembership(siteInfo.getShortName(), USER_THREE);

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.deleteSite(mySiteTest);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    private void checkSiteInfo(SiteInfo siteInfo,
            String expectedSitePreset,
            String expectedShortName,
            String expectedTitle,
            String expectedDescription,
            SiteVisibility expectedVisibility)
    {
        assertNotNull(siteInfo);
        assertEquals(expectedSitePreset, siteInfo.getSitePreset());
        assertEquals(expectedShortName, siteInfo.getShortName());
        assertEquals(expectedTitle, siteInfo.getTitle());
        assertEquals(expectedDescription, siteInfo.getDescription());
        assertEquals(expectedVisibility, siteInfo.getVisibility());
        assertNotNull(siteInfo.getNodeRef());

        // Check that the site is a tag scope
        assertTrue(this.taggingService.isTagScope(siteInfo.getNodeRef()));
    }

    /**
     * Test listSite and findSites methods.
     * <p/>
     * Note that {@link SiteService#findSites(String, int)} offers eventually consistent results and therefore may exhibit changed behaviour if Lucene is switched off or is replaced by SOLR. {@link SiteService#listSites(List, List, org.alfresco.query.PagingRequest)} and the other listSites methods should offer consistent, accurate result sets.
     */
    @Category(RedundantTests.class)
    @Test
    public void testListSites() throws Exception
    {
        // We'll match against the first few letter of TEST_TITLE in various listSites() tests below.
        final String testTitlePrefix = TEST_TITLE.substring(0, 9);

        List<SiteInfo> sites = this.siteService.listSites(null, null);
        assertNotNull("sites list was null.", sites);
        final int preexistingSitesCount = sites.size();

        // Create some sites
        this.siteService.createSite(TEST_SITE_PRESET, "mySiteOne", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, "mySiteTwo", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
        this.siteService.createSite(TEST_SITE_PRESET_2, "mySiteThree", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET_2, "mySiteFour", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
        this.siteService.createSite(TEST_SITE_PRESET_2, "mySiteFive", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);

        // Get all the sites
        sites = this.siteService.listSites(null, null);
        assertNotNull(sites);
        assertEquals(preexistingSitesCount + 5, sites.size());
        List<SiteInfo> sitesFromFind = this.siteService.findSites(null, null, 100);
        assertEquals(preexistingSitesCount + 5, sitesFromFind.size());
        sitesFromFind = this.siteService.findSites(null, 100);
        assertEquals(preexistingSitesCount + 5, sitesFromFind.size());
        List<SiteInfo> siteFromFind = this.siteService.findSites(null, null, 1);
        assertEquals("SiteService.findSites did not limit results", (sites.isEmpty() ? 0 : 1), siteFromFind.size());

        // Get sites by matching name - as of 4.0 listSites only supports STARTS WITH matches
        sites = this.siteService.listSites("mySiteO", null);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites named 'mySiteO*'", 1, sites.size());
        // However 'findSites' allows CONTAINS matching.
        sitesFromFind = this.siteService.findSites("One", null, 100);
        assertEquals("Matched wrong number of sites named 'One'", 1, sitesFromFind.size());

        List<FilterProp> filterProps = new ArrayList<FilterProp>();
        filterProps.add(new FilterPropString(ContentModel.PROP_NAME, "mySiteO", FilterPropString.FilterTypeString.STARTSWITH_IGNORECASE));
        PagingResults<SiteInfo> pageSite = this.siteService.listSites(filterProps, null, new PagingRequest(100));
        assertNotNull(pageSite);
        assertNotNull(pageSite.getQueryExecutionId());
        assertFalse(pageSite.hasMoreItems());

        // Get sites by matching title
        sites = this.siteService.listSites(testTitlePrefix, null);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites starting with '" + testTitlePrefix + "'", 5, sites.size());
        sitesFromFind = this.siteService.findSites("title", null, 100);
        assertEquals("Matched wrong number of sites containing 'title'\n" + sitesFromFind, 5, sitesFromFind.size());

        // Get sites by matching description
        sites = this.siteService.listSites("description", null);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites named 'description'", 5, sites.size());
        sitesFromFind = this.siteService.findSites("description", null, 100);
        assertEquals("Matched wrong number of sites named 'description'", 5, sitesFromFind.size());

        // Get sites by matching sitePreset - see ALF-5620
        sites = this.siteService.findSites(null, TEST_SITE_PRESET, 100);
        assertNotNull(sites);
        sites = this.siteService.listSites(null, TEST_SITE_PRESET);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites with PRESET", 2, sites.size());

        sites = this.siteService.listSites(null, TEST_SITE_PRESET_2);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites with PRESET_2", 3, sites.size());

        // Do detailed check of the site info objects
        for (SiteInfo site : sites)
        {
            String shortName = site.getShortName();
            if (shortName.equals("mySiteOne") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET, "mySiteOne", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
            }
            else if (shortName.equals("mySiteTwo") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET, "mySiteTwo", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
            }
            else if (shortName.equals("mySiteThree") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET_2, "mySiteThree", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
            }
            else if (shortName.equals("mySiteFour") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET_2, "mySiteFour", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
            }
            else if (shortName.equals("mySiteFive") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET_2, "mySiteFive", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);
            }
            else
            {
                fail("The shortname " + shortName + " is not recognised");
            }
        }

        // Test the public method on the implementation.
        Set<String> sitesSet = new HashSet<>(2);
        sitesSet.add("mySiteOne");
        sitesSet.add("mySiteTwo");
        sites = siteServiceImpl.listSites(sitesSet);
        assertEquals(2, sites.size());

        /**
         * Test list sites for a user
         */
        sites = this.siteService.listSites(USER_TWO);
        assertNotNull(sites);
        assertEquals(0, sites.size());

        this.siteService.setMembership("mySiteOne", USER_TWO, SiteModel.SITE_CONSUMER);
        this.siteService.setMembership("mySiteTwo", USER_TWO, SiteModel.SITE_CONSUMER);

        sites = this.siteService.listSites(USER_TWO);
        assertNotNull(sites);
        assertEquals(2, sites.size());

        /**
         * User One is the creator of all the sites.
         */
        sites = this.siteService.listSites(USER_ONE);
        assertNotNull(sites);
        assertEquals(5, sites.size());

        /**
         * Test list sites with a name filter
         */
        sites = this.siteService.listSites("mySiteOne", null, 10);
        assertNotNull(sites);
        assertEquals(1, sites.size());
        sitesFromFind = this.siteService.findSites("One", null, 100);
        assertEquals(1, sitesFromFind.size());

        /**
         * Search for partial match on more titles - matches word "Site".
         */
        sitesFromFind = this.siteService.findSites("ite", null, 100);
        assertEquals(5, sitesFromFind.size());

        /**
         * Now Switch to User Two and do the same sort of searching.
         */
        // Set the current authentication
        this.authenticationComponent.setCurrentUser(USER_TWO);

        /**
         * As User Two Search for partial match on more titles - matches word "Site" - should not find private sites
         */
        sitesFromFind = this.siteService.findSites("ite", null, 100);
        assertEquals(4, sitesFromFind.size());
        for (SiteInfo site : sites)
        {
            String shortName = site.getShortName();
            if (shortName.equals("mySiteOne") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET, "mySiteOne", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
            }
            else if (shortName.equals("mySiteTwo") == true)
            {
                // User Two is a member of this private site
                checkSiteInfo(site, TEST_SITE_PRESET, "mySiteTwo", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
            }
            else if (shortName.equals("mySiteThree") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET_2, "mySiteThree", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
            }
            else if (shortName.equals("mySiteFour") == true)
            {
                // User two is not a member of this site
                fail("Can see private site mySiteFour");
            }
            else if (shortName.equals("mySiteFive") == true)
            {
                // User Two should be able to see this moderated site.
                checkSiteInfo(site, TEST_SITE_PRESET_2, "mySiteFive", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);
            }
            else
            {
                fail("The shortname " + shortName + " is not recognised");
            }
        }

        authenticationComponent.setCurrentUser(USER_THREE);
        /**
         * As User Three Search for partial match on more titles - matches word "Site" - should not find private and moderated sites
         */
        sitesFromFind = this.siteService.findSites("ite", null, 100);
        assertEquals(3, sitesFromFind.size());
        for (SiteInfo site : sites)
        {
            String shortName = site.getShortName();
            if (shortName.equals("mySiteOne") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET, "mySiteOne", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
            }
            else if (shortName.equals("mySiteTwo") == true)
            {
                fail("Can see private site mySiteTwo");
            }
            else if (shortName.equals("mySiteThree") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET_2, "mySiteThree", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
            }
            else if (shortName.equals("mySiteFour") == true)
            {
                fail("Can see private site mySiteFour");
            }
            else if (shortName.equals("mySiteFive") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET_2, "mySiteFive", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);
            }
            else
            {
                fail("The shortname " + shortName + " is not recognised");
            }
        }
    }

    @Test
    public void testMNT_13710() throws Exception
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                String siteName = "test" + System.currentTimeMillis();

                List<String> roleList = new ArrayList<String>();
                roleList.add("test_customrole");
                roleList.add("testCustomrole");

                try
                {
                    authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

                    SiteInfo siteInfo = siteService.createSite(siteName, siteName, siteName, siteName, SiteVisibility.PUBLIC);

                    for (String role : roleList)
                    {
                        siteService.setMembership(siteInfo.getShortName(), USER_ONE, role);

                        List<String> list = siteServiceImpl.getMembersRoles(siteName, USER_ONE);

                        assertTrue(list.contains(role));
                    }
                }
                finally
                {
                    if (siteService.getSite(siteName) != null)
                    {
                        siteService.deleteSite(siteName);
                    }
                }

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    /**
     * Test listSite case sensitivity
     */
    @Test
    public void testListSitesCaseSensitivity() throws Exception
    {
        // RUN AS USER_ONE
        // We'll match against the first few letter of TEST_TITLE in various listSites() tests below.
        final String testTitlePrefix = TEST_TITLE.substring(0, 9);

        // Create at least one site as user_one
        siteService.createSite("testCaseSensitive", "mySiteCaseSensitive", "Case Sensitive Title", "Test of case sensitivity", SiteVisibility.PUBLIC);

        boolean existingValue = userNameMatcherImpl.getUserNamesAreCaseSensitive();
        try
        {
            userNameMatcherImpl.setUserNamesAreCaseSensitive(true);
            assertTrue("Case Sensitive - non matching case", (siteService.listSites(USER_ONE.toLowerCase())).size() == 0); // odd one out
            assertTrue("Case Sensitive - matching case", (siteService.listSites(USER_ONE)).size() > 0);

            userNameMatcherImpl.setUserNamesAreCaseSensitive(false);
            assertTrue("Not Case Sensitive - non matching case", (siteService.listSites(USER_ONE.toLowerCase())).size() > 0);
            assertTrue("Not Case Sensitive - matching case", (siteService.listSites(USER_ONE)).size() > 0);

        }
        finally
        {
            userNameMatcherImpl.setUserNamesAreCaseSensitive(existingValue);
        }
    }

    /**
     * This test method ensures that searches with wildcards work as they should
     */
    @Category(RedundantTests.class)
    @Test
    public void testfindSitesWithWildcardTitles() throws Exception
    {
        // How many sites are there already in the repo?
        List<SiteInfo> preexistingSites = this.siteService.findSites(null, null, 0);
        final int preexistingSitesCount = preexistingSites.size();

        // Create some test sites
        //
        // Note that the shortName can't contain an asterisk but the title can.
        this.siteService.createSite(TEST_SITE_PRESET, "siteAlpha", "asterix", TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, "siteBeta", "asterix*obelix", TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        // Get sites by matching title
        List<SiteInfo> sites = this.siteService.findSites("asterix", null, 0);
        assertNotNull(sites);
        // As the name & description do not contain "asterix", this will become a search for sites whose titles match "asterix"
        assertEquals("Matched wrong number of sites with title equal to 'asterix'", 2, sites.size());

        // This means 'find all'
        sites = this.siteService.findSites("*", null, 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites using '*'", preexistingSitesCount + 2, sites.size());

        sites = this.siteService.findSites("as?erix", null, 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites using '?'", 2, sites.size());
    }

    /**
     * This test method ensures that searches with wildcards work as they should
     */
    @Category(RedundantTests.class)
    @Test
    public void testfindSitesForLiveSearchWithWildcardTitles() throws Exception
    {
        // How many sites are there already in the repo?
        List<SiteInfo> preexistingSites = this.siteService.findSites(null, 0);
        final int preexistingSitesCount = preexistingSites.size();

        // Create some test sites
        //
        // Note that the shortName can't contain an asterisk but the title can.
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveA", "getafix", TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveB", "getafix1vitalstatistix", TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveC", "Armorican Gaul France", TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        // ACE-1428
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveD", "n3w s1t3 creat3ed 88", TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveE", "n3w s1t3 creat3ed 99", TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        // More scenarios
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveF", "super exciting product", TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveG", "super exciting launch", TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveH", "amazing sales 54", TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveI", "wonderfulsupport32", TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveJ", "great89service", TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, "siteLiveK", "my top draw", TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        // Get sites by matching title
        List<SiteInfo> sites = this.siteService.findSites("getafix", 0);
        assertNotNull(sites);
        // As the name & description do not contain "asterix", this will become a search for sites whose titles match "asterix"
        assertEquals("Matched wrong number of sites with title equal to 'getafix'", 2, sites.size());

        // This means 'find all'
        sites = this.siteService.findSites("*", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites using '*'", preexistingSitesCount + 11, sites.size());

        sites = this.siteService.findSites("ge?afix", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites using '?'", 2, sites.size());

        sites = this.siteService.findSites("Armorican", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for tokenized search", 1, sites.size());

        sites = this.siteService.findSites("Gaul", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for tokenized search", 1, sites.size());

        sites = this.siteService.findSites("France", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for tokenized search", 1, sites.size());

        sites = this.siteService.findSites("Armorican Gaul", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for tokenized search", 1, sites.size());

        sites = this.siteService.findSites("Armori", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for tokenized search", 1, sites.size());

        sites = this.siteService.findSites("Fran", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for tokenized search", 1, sites.size());

        sites = this.siteService.findSites("n3w s1t3 88", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for tokenized search", 1, sites.size());

        sites = this.siteService.findSites("n3w s1t3 99", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for tokenized search", 1, sites.size());

        sites = this.siteService.findSites("n3w s1t3", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for tokenized search", 2, sites.size());

        sites = this.siteService.findSites("s1t3", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for tokenized search", 2, sites.size());

        sites = this.siteService.findSites("super", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for super", 2, sites.size());

        sites = this.siteService.findSites("exciting", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for exciting", 2, sites.size());

        sites = this.siteService.findSites("product", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for product", 1, sites.size());

        sites = this.siteService.findSites("super product", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for super product", 1, sites.size());

        sites = this.siteService.findSites("super launch", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for super launch", 1, sites.size());

        sites = this.siteService.findSites("exciting launch", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for super launch", 1, sites.size());

        sites = this.siteService.findSites("super exciting", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for super exciting", 2, sites.size());

        sites = this.siteService.findSites("amazing sales 54", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for amazing sales 54", 1, sites.size());

        sites = this.siteService.findSites("wonderfulsupport32", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for wonderfulsupport32", 1, sites.size());

        sites = this.siteService.findSites("great89service", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for great89service", 1, sites.size());

        sites = this.siteService.findSites("top draw", 0);
        assertNotNull(sites);
        assertEquals("Matched wrong number of sites for top draw", 1, sites.size());
    }

    @Test
    public void testGetSite()
    {
        // Get a site that isn't there
        SiteInfo siteInfo = this.siteService.getSite("testGetSite");
        assertNull(siteInfo);

        // Create a test site
        this.siteService.createSite(TEST_SITE_PRESET, "testGetSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        // Get the test site
        siteInfo = this.siteService.getSite("testGetSite");
        assertNotNull(siteInfo);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testGetSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        // Create a path to content within the site
        NodeRef container = siteService.createContainer(siteInfo.getShortName(), "folder.component", ContentModel.TYPE_FOLDER, null);
        NodeRef content = nodeService.createNode(container, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT).getChildRef();

        // Get the site from the lower-level child node.
        siteInfo = siteService.getSite(content);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testGetSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        NodeRef siteContainer = siteServiceImpl.getSiteContainer(siteInfo.getShortName(), "folder.component", false, siteService, transactionService, taggingService);
        assertEquals(container.getId(), siteContainer.getId());

        PagingResults<FileInfo> containers = siteService.listContainers(siteInfo.getShortName(), new PagingRequest(1000));
        assertNotNull(containers);

        try
        {
            siteServiceImpl.getSiteContainer("NON_SENSE", "folder.component", true, siteService, transactionService, taggingService);
            fail("Shouldn't get here");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
            assertTrue(exception.getMessage().contains("Unable to create the"));

        }
    }

    @Test
    public void testUpdateSite()
    {
        SiteInfo siteInfo = new SiteInfoImpl(TEST_SITE_PRESET, "testUpdateSite", "changedTitle", "changedDescription", SiteVisibility.PRIVATE, null);

        // update a site that isn't there
        try
        {
            this.siteService.updateSite(siteInfo);
            fail("Shouldn't be able to update a site that does not exist");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }

        // Create a test site
        this.siteService.createSite(TEST_SITE_PRESET, "testUpdateSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        // Update the details of the site
        this.siteService.updateSite(siteInfo);
        siteInfo = this.siteService.getSite("testUpdateSite");
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testUpdateSite", "changedTitle", "changedDescription", SiteVisibility.PRIVATE);

        // Update the permission again
        siteInfo.setVisibility(SiteVisibility.PUBLIC);
        this.siteService.updateSite(siteInfo);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testUpdateSite", "changedTitle", "changedDescription", SiteVisibility.PUBLIC);
    }

    @Test
    public void testDeleteSite_DoesNotExist()
    {
        // delete a site that isn't there
        try
        {
            this.siteService.deleteSite("testDeleteSite");
            fail("Shouldn't be able to delete a site that does not exist");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }
    }

    @Test
    public void testDeleteSite_ViaNodeService()
    {
        String siteShortName = "testUpdateSite";
        this.siteService.createSite(TEST_SITE_PRESET, siteShortName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        SiteInfo siteInfo = this.siteService.getSite(siteShortName);
        assertNotNull(siteInfo);

        // delete a site through the nodeService - not allowed
        try
        {
            nodeService.deleteNode(siteInfo.getNodeRef());
            fail("Shouldn't be able to delete a site via the nodeService");
        }
        catch (AlfrescoRuntimeException expected)
        {
            // Intentionally empty
        }
    }

    @Test
    public void testMoveSite_ViaNodeService()
    {
        String siteShortName1 = "testMoveSite" + GUID.generate();
        String siteShortName2 = "testMoveSite" + GUID.generate();
        this.siteService.createSite(TEST_SITE_PRESET, siteShortName1, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        this.siteService.createSite(TEST_SITE_PRESET, siteShortName2, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        SiteInfo siteInfo1 = this.siteService.getSite(siteShortName1);
        assertNotNull(siteInfo1);
        SiteInfo siteInfo2 = this.siteService.getSite(siteShortName2);
        assertNotNull(siteInfo2);

        // move a site through the nodeService - not allowed
        try
        {
            nodeService.moveNode(siteInfo1.getNodeRef(), siteInfo2.getNodeRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()));
            fail("Shouldn't be able to move a site via the nodeService");
        }
        catch (AlfrescoRuntimeException expected)
        {
            // Intentionally empty
        }
    }

    /**
     * This is an integration test for MNT-18014
     */
    @Test
    public void testMoveFolderStructureWithNonInheritedPermission()
    {
        // Login to share as the admin user
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // Create 2 sites test1, test2 as admin
        String test1SiteShortName = "test1" + GUID.generate();
        String test2SiteShortName = "test2" + GUID.generate();
        createSite(test1SiteShortName, SiteService.DOCUMENT_LIBRARY, SiteVisibility.PUBLIC);
        createSite(test2SiteShortName, SiteService.DOCUMENT_LIBRARY, SiteVisibility.PUBLIC);

        SiteInfo test1SiteInfo = this.siteService.getSite(test1SiteShortName);
        assertNotNull(test1SiteInfo);
        SiteInfo test2SiteInfo = this.siteService.getSite(test2SiteShortName);
        assertNotNull(test2SiteInfo);

        // add user1 (USER_ONE) and user2 (USER_TWO) as managers on test1 site (test1SiteInfo)
        siteService.setMembership(test1SiteShortName, USER_ONE, SiteModel.SITE_MANAGER);
        siteService.setMembership(test1SiteShortName, USER_TWO, SiteModel.SITE_MANAGER);

        // Give manager role to user1 to test2
        siteService.setMembership(test2SiteShortName, USER_ONE, SiteModel.SITE_MANAGER);

        // Log in as user2
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);

        // In document library of test1, create fol1 containing fol2 containing fol3
        NodeRef documentLibraryTest1Site = siteService.getContainer(test1SiteShortName, SiteService.DOCUMENT_LIBRARY);
        assertNotNull(documentLibraryTest1Site);
        NodeRef fol1 = this.fileFolderService.create(documentLibraryTest1Site, "fol1-" + GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
        NodeRef fol2 = this.fileFolderService.create(fol1, "fol2-" + GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
        NodeRef fol3 = this.fileFolderService.create(fol2, "fol3-" + GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();

        // Cut inheritance on fol2
        permissionService.setInheritParentPermissions(fol2, false);

        // this is what happens when called from Share : permissions.post:
        // var siteManagerAuthority = "GROUP_site_" + location.site + "_SiteManager";
        // // Insure Site Managers can still manage content.
        // node.setPermission("SiteManager", siteManagerAuthority);
        String test1SiteGroupPrefix = siteServiceImpl.getSiteGroup(test1SiteShortName, true);
        String test1SiteManagerAuthority = test1SiteGroupPrefix + "_" + SiteModel.SITE_MANAGER;
        permissionService.setPermission(fol2, test1SiteManagerAuthority, SiteModel.SITE_MANAGER, true);

        // Log in as user1, go to site test1
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);

        // Check that user1 can see fol1 fol2 fol3
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(documentLibraryTest1Site);
        assertEquals("Size should be 1", 1, childAssocs.size());
        assertTrue("Folder name should start with fol1", getFirstName(childAssocs).startsWith("fol1"));
        childAssocs = nodeService.getChildAssocs(childAssocs.get(0).getChildRef());
        assertEquals("Size should be 1", 1, childAssocs.size());
        assertTrue("Folder name should start with fol2", getFirstName(childAssocs).startsWith("fol2"));
        childAssocs = nodeService.getChildAssocs(childAssocs.get(0).getChildRef());
        assertEquals("Size should be 1", 1, childAssocs.size());
        assertTrue("Folder name should start with fol3", getFirstName(childAssocs).startsWith("fol3"));

        NodeRef documentLibraryTest2Site = siteService.getContainer(test2SiteShortName, SiteService.DOCUMENT_LIBRARY);
        assertNotNull(documentLibraryTest2Site);
        childAssocs = nodeService.getChildAssocs(documentLibraryTest2Site);
        assertTrue("Folder should be empty.", childAssocs.isEmpty());

        // Move fol1 to site test2
        ChildAssociationRef childAssociationRef = nodeService.moveNode(fol1, documentLibraryTest2Site, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()));

        // This is what Share does:
        // move the node
        // result.success = fileNode.move(parent, destNode);
        //
        // if (result.success)
        // {
        // // If this was an inter-site move, we'll need to clean up the permissions on the node
        // if ((fromSite) && (String(fromSite) !== String(fileNode.siteShortName)))
        // {
        // siteService.cleanSitePermissions(fileNode);
        // }
        // }
        siteService.cleanSitePermissions(fol1, test2SiteInfo);

        childAssocs = nodeService.getChildAssocs(documentLibraryTest1Site);
        assertTrue("test1Site document library should be empty.", childAssocs.isEmpty());

        assertFalse("After the move the folder should keep the inherit permission value(false).",
                permissionService.getInheritParentPermissions(fol2));

        // Go to the site test2's document library and click on fol1
        // user1 is able to see the contents of fol1
        childAssocs = nodeService.getChildAssocs(documentLibraryTest2Site);
        assertEquals("Size should be 1", 1, childAssocs.size());
        assertTrue("Folder name should start with fol1", getFirstName(childAssocs).startsWith("fol1"));
        childAssocs = nodeService.getChildAssocs(childAssocs.get(0).getChildRef());
        assertEquals("Size should be 1", 1, childAssocs.size());
        assertTrue("Folder name should start with fol2", getFirstName(childAssocs).startsWith("fol2"));
        childAssocs = nodeService.getChildAssocs(childAssocs.get(0).getChildRef());
        assertEquals("Size should be 1", 1, childAssocs.size());
        assertTrue("Folder name should start with fol3", getFirstName(childAssocs).startsWith("fol3"));
    }

    private String getFirstName(List<ChildAssociationRef> childAssocs)
    {
        return nodeService.getProperties(childAssocs.get(0).getChildRef()).get(ContentModel.PROP_NAME).toString();
    }

    @Test
    public void testDeleteSite()
    {
        @SuppressWarnings("deprecation")
        SiteService smallSiteService = (SiteService) this.applicationContext.getBean("siteService");
        // Create a test group
        final String testGroupName = "siteServiceImplTestGroup_" + GUID.generate();
        String testGroup = AuthenticationUtil.runAs(
                new AuthenticationUtil.RunAsWork<String>() {
                    public String doWork() throws Exception
                    {
                        return authorityService.createAuthority(AuthorityType.GROUP, testGroupName);
                    }
                }, AuthenticationUtil.getAdminUserName());

        // Create a test site
        String siteShortName = "testUpdateSite";
        this.siteService.createSite(TEST_SITE_PRESET, siteShortName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        SiteInfo siteInfo = this.siteService.getSite(siteShortName);
        assertNotNull(siteInfo);

        // Add the test group as a member of the site
        this.siteService.setMembership(siteShortName, testGroup, SiteModel.SITE_CONTRIBUTOR);

        // Delete the site
        this.siteService.deleteSite(siteShortName);
        assertNull(this.siteService.getSite(siteShortName));
        NodeRef archivedNodeRef = nodeArchiveService.getArchivedNode(siteInfo.getNodeRef());
        assertTrue("Deleted sites can be recovered from the Trash.", nodeService.exists(archivedNodeRef));

        // related site groups should remain after site delete but should be deleted on site purge from trashcan.
        // Such case is tested in SiteServiceImplMoreTest.deleteSiteAndRestoreEnsuringSiteGroupsAreRecovered
        assertTrue(authorityService.authorityExists(((SiteServiceImpl) smallSiteService).getSiteGroup(siteShortName, true)));
        assertTrue(authorityService.authorityExists(((SiteServiceImpl) smallSiteService).getSiteGroup(siteShortName)));
        Set<String> permissions = permissionService.getSettablePermissions(SiteModel.TYPE_SITE);
        for (String permission : permissions)
        {
            String siteRoleGroup = ((SiteServiceImpl) smallSiteService).getSiteRoleGroup(siteShortName, permission, true);
            assertTrue(authorityService.authorityExists(siteRoleGroup));
        }

        // Ensure that the added "normal" groups have not been deleted
        assertTrue(authorityService.authorityExists(testGroup));
    }

    @Test
    public void testIsPublic()
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                List<SiteInfo> sites = siteService.listSites(null, null);
                assertNotNull("initial sites list was null.", sites);
                final int preexistingSiteCount = sites.size();

                // Create a couple of sites as user one
                String isPublicTrue = "isPublicTrue" + UUID.randomUUID();
                String isPublicFalse = "isPublicFalse" + UUID.randomUUID();
                siteService.createSite(TEST_SITE_PRESET, isPublicTrue, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
                siteService.createSite(TEST_SITE_PRESET, isPublicFalse, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);

                // Get the sites as user one
                sites = siteService.listSites(null, null);
                assertNotNull(sites);
                assertEquals(preexistingSiteCount + 2, sites.size());

                // Now get the sites as user two
                authenticationComponent.setCurrentUser(USER_TWO);
                sites = siteService.listSites(null, null);
                assertNotNull(sites);
                assertEquals(preexistingSiteCount + 1, sites.size());
                SiteInfo userTwoSite = siteService.getSite(isPublicTrue);
                checkSiteInfo(userTwoSite, TEST_SITE_PRESET, isPublicTrue, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

                // Make user 2 a member of the site
                // TestWithUserUtils.authenticateUser(USER_ONE, "PWD", this.authenticationService, this.authenticationComponent);
                authenticationComponent.setCurrentUser(USER_ONE);
                siteService.setMembership(isPublicFalse, USER_TWO, SiteModel.SITE_CONSUMER);

                // Now get the sites as user two
                authenticationComponent.setCurrentUser(USER_TWO);
                sites = siteService.listSites(null, null);
                assertNotNull(sites);
                assertEquals(preexistingSiteCount + 2, sites.size());

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.deleteSite(isPublicTrue);
                siteService.deleteSite(isPublicFalse);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    @Test
    public void testMembership()
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                String siteShortName = "testMembership" + UUID.randomUUID();
                // Create a site as user one
                siteService.createSite(TEST_SITE_PRESET, siteShortName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);

                // Get the members of the site and check that user one is a manager
                Map<String, String> members = siteService.listMembers(siteShortName, null, null, 0);
                assertNotNull(members);
                assertEquals(1, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));

                // Add user two as a consumer and user three as a collaborator
                siteService.setMembership(siteShortName, USER_TWO, SiteModel.SITE_CONSUMER);
                siteService.setMembership(siteShortName, USER_THREE, SiteModel.SITE_COLLABORATOR);

                // Get the members of the site
                members = siteService.listMembers(siteShortName, null, null, 0);
                assertNotNull(members);
                assertEquals(3, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
                assertTrue(members.containsKey(USER_TWO));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(USER_TWO));
                assertTrue(members.containsKey(USER_THREE));
                assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_THREE));

                // Get only the site managers
                members = siteService.listMembers(siteShortName, null, SiteModel.SITE_MANAGER, 0);
                assertNotNull(members);
                assertEquals(1, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));

                // Get only user two
                members = siteService.listMembers(siteShortName, USER_TWO, null, 0);
                assertNotNull(members);
                assertEquals(1, members.size());
                assertTrue(members.containsKey(USER_TWO));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(USER_TWO));

                // Change the membership of user two
                siteService.setMembership(siteShortName, USER_TWO, SiteModel.SITE_COLLABORATOR);

                // Check the members of the site
                members = siteService.listMembers(siteShortName, null, null, 0);
                assertNotNull(members);
                assertEquals(3, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
                assertTrue(members.containsKey(USER_TWO));
                assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_TWO));
                assertTrue(members.containsKey(USER_THREE));
                assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_THREE));

                // Check other listMember calls
                siteService.listMembers(siteShortName, null, null, false, new SiteService.SiteMembersCallback() {
                    List<String> USERS = Arrays.asList(USER_ONE, USER_TWO, USER_THREE);
                    int userCount = 0;

                    @Override
                    public void siteMember(String authority, String permission)
                    {
                        if (USERS.contains(authority))
                        {
                            userCount++;
                        }
                    }

                    @Override
                    public boolean isDone()
                    {
                        return userCount == USERS.size();
                    }
                });

                // Remove user two's membership
                siteService.removeMembership(siteShortName, USER_TWO);

                // Check the members of the site
                members = siteService.listMembers(siteShortName, null, null, 0);
                assertNotNull(members);
                assertEquals(2, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
                assertTrue(members.containsKey(USER_THREE));
                assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_THREE));

                // Ensure that size limiting works correctly
                members = siteService.listMembers(siteShortName, null, null, 1);
                assertNotNull(members);
                assertEquals(1, members.size());

                members = siteService.listMembers(siteShortName, null, null, 2);
                assertNotNull(members);
                assertEquals(2, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
                assertTrue(members.containsKey(USER_THREE));
                assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_THREE));

                // Check that a non-manager and non-member cannot edit the memberships
                authenticationComponent.setCurrentUser(USER_TWO);
                try
                {
                    siteService.setMembership(siteShortName, USER_TWO, SiteModel.SITE_COLLABORATOR);
                    fail("A non member shouldnt be able to set memberships");
                }
                catch (AlfrescoRuntimeException e)
                {
                    // As expected
                }
                try
                {
                    siteService.removeMembership(siteShortName, USER_THREE);
                    fail("A non member shouldnt be able to remove a membership");
                }
                catch (AlfrescoRuntimeException e)
                {
                    // As expected
                }
                authenticationComponent.setCurrentUser(USER_THREE);
                try
                {
                    siteService.setMembership(siteShortName, USER_TWO, SiteModel.SITE_COLLABORATOR);
                    fail("A member who isn't a manager shouldnt be able to set memberships");
                }
                catch (AlfrescoRuntimeException e)
                {
                    // As expected
                }
                siteService.removeMembership(siteShortName, USER_THREE);

                authenticationComponent.setCurrentUser(USER_ONE);
                // Try and change the permissions of the only site manager
                siteService.setMembership(siteShortName, USER_TWO, SiteModel.SITE_MANAGER);
                siteService.setMembership(siteShortName, USER_TWO, SiteModel.SITE_COLLABORATOR);
                try
                {
                    siteService.setMembership(siteShortName, USER_ONE, SiteModel.SITE_COLLABORATOR);
                    fail("You can not change the role of the last site memnager");
                }
                catch (AlfrescoRuntimeException exception)
                {
                    // Expected
                    // exception.printStackTrace();
                }

                // Try and remove the only site manager and should get a failure
                siteService.setMembership(siteShortName, USER_TWO, SiteModel.SITE_MANAGER);
                siteService.removeMembership(siteShortName, USER_ONE);
                try
                {
                    siteService.removeMembership(siteShortName, USER_TWO);
                    fail("You can not remove the last site memnager from a site");
                }
                catch (AlfrescoRuntimeException exception)
                {
                    // Expected
                    // exception.printStackTrace();
                }

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.deleteSite(siteShortName);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    @Test
    public void testDefaults()
    {
        assertFalse(this.siteService.isSiteAdmin(null));
        assertTrue(this.siteService.hasCreateSitePermissions());
        Comparator<String> comparator = siteServiceImpl.getRoleComparator();
        assertNotNull(comparator);
    }

    @Test
    public void testListSiteMemberships()
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                String siteName1 = "testMembership1" + UUID.randomUUID();
                String siteName2 = "testMembership2" + UUID.randomUUID();
                String siteName3 = "testMembership3" + UUID.randomUUID();
                String publicSite = "publicSite" + UUID.randomUUID();
                String moderatedSite = "moderatedSite" + UUID.randomUUID();

                // Create a site as user one
                siteService.createSite(TEST_SITE_PRESET, siteName1, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);

                // Get the members of the site and check that user one is a manager
                List<SiteMembership> members = siteService.listSiteMemberships(USER_ONE, 0);
                assertNotNull(members);
                assertEquals(1, members.size());
                assertEquals(USER_ONE, members.get(0).getPersonId());
                assertEquals(SiteModel.SITE_MANAGER, members.get(0).getRole());

                authenticationComponent.setCurrentUser(USER_FOUR);
                siteService.createSite(TEST_SITE_PRESET, publicSite, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
                siteService.setMembership(publicSite, USER_ONE, SiteModel.SITE_CONSUMER);
                siteService.createSite(TEST_SITE_PRESET, moderatedSite, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);
                siteService.setMembership(moderatedSite, USER_ONE, SiteModel.SITE_CONTRIBUTOR);

                authenticationComponent.setCurrentUser(USER_ONE);
                PagingResults<SiteMembership> siteM = siteService.listSitesPaged(USER_ONE, null, new PagingRequest(1000));
                assertNotNull(siteM);
                assertFalse(siteM.hasMoreItems());

                // Create a site as user two and add user one
                authenticationComponent.setCurrentUser(USER_TWO);
                siteService.createSite(TEST_SITE_PRESET, siteName2, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
                siteService.setMembership(siteName2, USER_ONE, SiteModel.SITE_CONSUMER);

                // Create a site as user three and add user one
                authenticationComponent.setCurrentUser(USER_THREE);
                siteService.createSite(TEST_SITE_PRESET, siteName3, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
                siteService.setMembership(siteName3, USER_ONE, SiteModel.SITE_COLLABORATOR);

                authenticationComponent.setCurrentUser(USER_ONE);
                members = siteService.listSiteMemberships(USER_ONE, 0);
                assertNotNull(members);
                assertEquals(5, members.size());
                assertEquals(USER_ONE, members.get(0).getPersonId());
                assertEquals(SiteModel.SITE_MANAGER, members.get(0).getRole());
                assertEquals(siteName1, members.get(0).getSiteInfo().getShortName());
                assertEquals(USER_ONE, members.get(1).getPersonId());
                assertEquals(SiteModel.SITE_CONSUMER, members.get(1).getRole());
                assertEquals(publicSite, members.get(1).getSiteInfo().getShortName());
                assertEquals(USER_ONE, members.get(2).getPersonId());
                assertEquals(SiteModel.SITE_CONTRIBUTOR, members.get(2).getRole());
                assertEquals(moderatedSite, members.get(2).getSiteInfo().getShortName());
                assertEquals(USER_ONE, members.get(3).getPersonId());
                assertEquals(SiteModel.SITE_CONSUMER, members.get(3).getRole());
                assertEquals(siteName2, members.get(3).getSiteInfo().getShortName());
                assertEquals(USER_ONE, members.get(4).getPersonId());
                assertEquals(SiteModel.SITE_COLLABORATOR, members.get(4).getRole());
                assertEquals(siteName3, members.get(4).getSiteInfo().getShortName());

                authenticationComponent.setCurrentUser(USER_TWO);
                // (MNT-19035) USER_TWO list sites membership for USER_ONE - only sites that are public should be visible
                members = siteService.listSiteMemberships(USER_ONE, 0);
                assertNotNull(members);
                // Even though USER_ONE is part of 3 private sites, 1 moderated and 1 public site, only the non-private sites are visible to USER_TWO
                assertEquals(members.size(), 2);

                authenticationComponent.setCurrentUser(USER_SITE_ADMIN);
                members = siteService.listSiteMemberships(USER_ONE, 0);
                assertNotNull(members);
                // All sites are visible for a site administrator user
                assertEquals(members.size(), 5);

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.deleteSite(siteName1);
                siteService.deleteSite(siteName2);
                siteService.deleteSite(siteName3);
                siteService.deleteSite(publicSite);
                siteService.deleteSite(moderatedSite);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    @Test
    public void testJoinLeave()
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                String testMembershipPublic = "testMembershipPublic" + UUID.randomUUID();
                String testMembershipPrivate = "testMembershipPrivate" + UUID.randomUUID();
                // Create a site as user one
                siteService.createSite(TEST_SITE_PRESET, testMembershipPublic, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
                siteService.createSite(TEST_SITE_PRESET, testMembershipPrivate, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);

                // Become user two
                // TestWithUserUtils.authenticateUser(USER_TWO, "PWD", this.authenticationService, this.authenticationComponent);
                authenticationComponent.setCurrentUser(USER_TWO);

                // As user two try and add self as contributor
                try
                {
                    siteService.setMembership(testMembershipPublic, USER_TWO, SiteModel.SITE_COLLABORATOR);
                    fail("This should have failed because you don't have permissions");
                }
                catch (AlfrescoRuntimeException exception)
                {
                    // Ignore because as expected
                }

                // As user two try and add self as consumer to public site
                siteService.setMembership(testMembershipPublic, USER_TWO, SiteModel.SITE_CONSUMER);

                // As user two try and add self as consumer to private site
                try
                {
                    siteService.setMembership(testMembershipPrivate, USER_TWO, SiteModel.SITE_CONSUMER);
                    fail("This should have failed because you can't do this to a private site unless you are site manager");
                }
                catch (AlfrescoRuntimeException exception)
                {
                    // Ignore because as expected
                }

                // As user two try and add user three as a consumer to a public site
                try
                {
                    siteService.setMembership(testMembershipPublic, USER_THREE, SiteModel.SITE_CONSUMER);
                    fail("This should have failed because you can't add another user as a consumer of a public site");
                }
                catch (AlfrescoRuntimeException exception)
                {
                    // Ignore because as expected
                }

                // add some members use in remove tests
                authenticationComponent.setCurrentUser(USER_ONE);
                siteService.setMembership(testMembershipPublic, USER_THREE, SiteModel.SITE_COLLABORATOR);
                siteService.setMembership(testMembershipPrivate, USER_TWO, SiteModel.SITE_CONSUMER);
                authenticationComponent.setCurrentUser(USER_TWO);

                // Try and remove user threes membership from public site
                try
                {
                    siteService.removeMembership(testMembershipPublic, USER_THREE);
                    fail("Cannot remove membership");
                }
                catch (Exception exception)
                {
                    // Ignore because as expected
                }

                // Try and remove own membership
                siteService.removeMembership(testMembershipPublic, USER_TWO);

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.deleteSite(testMembershipPublic);
                siteService.deleteSite(testMembershipPrivate);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    @Test
    public void testContainer()
    {
        // Create a couple of sites as user one
        SiteInfo siteInfo = this.siteService.createSite(TEST_SITE_PRESET, "testContainer", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        boolean hasContainer = this.siteService.hasContainer(siteInfo.getShortName(), "folder.component");
        assertFalse(hasContainer);
        NodeRef container1 = this.siteService.getContainer(siteInfo.getShortName(), "folder.component");
        assertNull(container1);
        container1 = this.siteService.createContainer(siteInfo.getShortName(), "folder.component", null, null);
        assertTrue(this.taggingService.isTagScope(container1));
        NodeRef container2 = this.siteService.getContainer(siteInfo.getShortName(), "folder.component");
        assertNotNull(container2);
        assertTrue(this.taggingService.isTagScope(container2));
        assertTrue(container1.equals(container2));
        boolean hasContainer2 = this.siteService.hasContainer(siteInfo.getShortName(), "folder.component");
        assertTrue(hasContainer2);
        boolean hasContainer3 = this.siteService.hasContainer(siteInfo.getShortName(), "folder.component2");
        assertFalse(hasContainer3);

        NodeRef container3 = this.siteService.getContainer(siteInfo.getShortName(), "folder.component2");
        assertNull(container3);
        container3 = this.siteService.createContainer(siteInfo.getShortName(), "folder.component2", null, null);
        assertNotNull(container3);
        assertTrue(this.taggingService.isTagScope(container3));
        assertFalse(container1.equals(container3));

        boolean hasContainer4 = this.siteService.hasContainer(siteInfo.getShortName(), "folder.component2");
        assertTrue(hasContainer4);
        boolean hasContainer5 = this.siteService.hasContainer(siteInfo.getShortName(), "folder.component3");
        assertFalse(hasContainer5);
        NodeRef container5 = this.siteService.getContainer(siteInfo.getShortName(), "folder.component3");
        assertNull(container5);
        container5 = this.siteService.createContainer(siteInfo.getShortName(), "folder.component3", ContentModel.TYPE_FOLDER, null);
        assertNotNull(container5);

        NodeRef container6 = this.siteService.getContainer(siteInfo.getShortName(), "folder.component3");
        assertNotNull(container6);
        container6 = this.siteService.createContainer(siteInfo.getShortName(), "folder.component3", null, null);
        assertNotNull(container6);
        assertTrue(container5.equals(container6));
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(container6));
        NodeRef container7 = this.siteService.getContainer(siteInfo.getShortName(), "folder.component3");
        assertNotNull(container7);
        container7 = this.siteService.createContainer(siteInfo.getShortName(), "folder.component3", ForumModel.TYPE_FORUM, null);
        assertNotNull(container7);
        assertTrue(container5.equals(container7));
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(container7));
        NodeRef container8 = this.siteService.getContainer(siteInfo.getShortName(), "folder.component4");
        assertNull(container8);
        container8 = this.siteService.createContainer(siteInfo.getShortName(), "folder.component4", ForumModel.TYPE_FORUM, null);
        assertNotNull(container8);
        assertEquals(ForumModel.TYPE_FORUM, nodeService.getType(container8));

        try
        {
            boolean noItDoesnt = this.siteService.hasContainer("IDONT_EXISTS", "folder.component2");
            fail("Shouldn't get here");
        }
        catch (SiteDoesNotExistException exception)
        {
            // Expected
        }
    }

    @Test
    public void testSiteGetRoles()
    {
        List<String> roles = this.siteService.getSiteRoles();
        assertNotNull(roles);
        assertFalse(roles.isEmpty());

        // By default there are just the 4 roles, but in classpath:org/alfresco/repo/site/site-custom-context.xml there are 7
        assertEquals(7, roles.size());
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONSUMER));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONTRIBUTOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_COLLABORATOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_MANAGER));

        // For custom roles, see testCustomSiteType()
    }

    @Test
    public void testCustomSiteProperties()
    {
        QName additionalInformationQName = QName.createQName(SiteModel.SITE_CUSTOM_PROPERTY_URL, "additionalInformation");

        // Create a site
        String siteShortName = "mySiteTest" + UUID.randomUUID();
        SiteInfo siteInfo = this.siteService.createSite(TEST_SITE_PRESET, siteShortName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, siteShortName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        assertNull(siteInfo.getCustomProperty(additionalInformationQName));
        assertNotNull(siteInfo.getCustomProperties());
        assertTrue(siteInfo.getCustomProperties().isEmpty());

        // Add an aspect with a custom property
        NodeRef siteNodeRef = siteInfo.getNodeRef();
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(additionalInformationQName, "information");
        this.nodeService.addAspect(siteNodeRef, QName.createQName(SiteModel.SITE_MODEL_URL, "customSiteProperties"), properties);

        // Get the site again
        siteInfo = this.siteService.getSite(siteShortName);
        assertNotNull(siteInfo);
        assertEquals("information", siteInfo.getCustomProperty(additionalInformationQName));
        assertNotNull(siteInfo.getCustomProperties());
        assertFalse(siteInfo.getCustomProperties().isEmpty());
        assertEquals(1, siteInfo.getCustomProperties().size());
        assertEquals("information", siteInfo.getCustomProperties().get(additionalInformationQName));
    }

    /**
     * Creates a site with a custom type, and ensures that it behaves correctly.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testCustomSiteType()
    {
        final String CS_URI = "http://example.com/site";
        final String CS_PFX = "cs";

        // Setup our custom site type
        DictionaryDAO dictionaryDAO = (DictionaryDAO) this.applicationContext.getBean("dictionaryDAO");
        M2Model model = M2Model.createModel("cm:CustomSiteModel");
        model.createNamespace(CS_URI, CS_PFX);

        // Import the usual suspects too
        model.createImport(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                NamespaceService.CONTENT_MODEL_PREFIX);
        model.createImport(
                NamespaceService.DICTIONARY_MODEL_1_0_URI,
                NamespaceService.DICTIONARY_MODEL_PREFIX);
        model.createImport(
                SiteModel.SITE_MODEL_URL,
                SiteModel.SITE_MODEL_PREFIX);

        // Custom type
        M2Type customType = model.createType("cs:customSite");
        customType.setTitle("customSite");
        customType.setParentName(
                SiteModel.SITE_MODEL_PREFIX + ":" +
                        SiteModel.TYPE_SITE.getLocalName());

        M2Property customProp = customType.createProperty("cs:customSiteProp");
        customProp.setTitle("customSiteProp");
        customProp.setType("d:text");
        dictionaryDAO.putModel(model);

        // Get our custom type, to check it's in there properly
        final QName customTypeQ = QName.createQName("cs", "customSite", namespaceService);
        TypeDefinition td = dictionaryService.getType(customTypeQ);
        assertNotNull(td);

        // Create a site
        SiteInfo site = siteService.createSite(
                "custom", "custom", "Custom", "Custom",
                SiteVisibility.PUBLIC);

        // Check the roles on it
        List<String> roles = siteService.getSiteRoles();
        assertEquals(7, roles.size());
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONSUMER));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONTRIBUTOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_COLLABORATOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_MANAGER));

        roles = siteService.getSiteRoles(site.getShortName());
        assertEquals(7, roles.size());
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONSUMER));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONTRIBUTOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_COLLABORATOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_MANAGER));

        // Swap the type
        nodeService.setType(site.getNodeRef(), customTypeQ);

        // Check again
        roles = siteService.getSiteRoles();
        assertEquals(7, roles.size());
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONSUMER));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONTRIBUTOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_COLLABORATOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_MANAGER));

        roles = siteService.getSiteRoles(site.getShortName());
        assertEquals(7, roles.size());
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONSUMER));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONTRIBUTOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_COLLABORATOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_MANAGER));

        // Alter the permissions for the custom site
        PermissionService testPermissionService = spy(
                (PermissionService) this.applicationContext.getBean("permissionServiceImpl"));
        Set<String> customPerms = new HashSet<String>();
        customPerms.add(SiteServiceImpl.SITE_MANAGER);
        customPerms.add("CUSTOM");
        when(testPermissionService.getSettablePermissions(customTypeQ)).thenReturn(customPerms);

        // Check it changed for the custom site, but not normal
        SiteServiceImpl siteServiceImpl = (SiteServiceImpl) this.applicationContext.getBean("siteService");
        siteServiceImpl.setPermissionService(testPermissionService);
        roles = siteService.getSiteRoles();

        assertEquals(7, roles.size());
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONSUMER));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONTRIBUTOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_COLLABORATOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_MANAGER));

        roles = siteService.getSiteRoles(site.getShortName());
        assertEquals(2, roles.size());
        assertEquals(true, roles.contains("CUSTOM"));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_MANAGER));

        // Put the permissions back
        siteServiceImpl.setPermissionService(permissionService);
        roles = siteService.getSiteRoles();
        assertEquals(7, roles.size());
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONSUMER));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONTRIBUTOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_COLLABORATOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_MANAGER));

        roles = siteService.getSiteRoles(site.getShortName());
        assertEquals(7, roles.size());
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONSUMER));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_CONTRIBUTOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_COLLABORATOR));
        assertEquals(true, roles.contains(SiteServiceImpl.SITE_MANAGER));
    }

    @Test
    public void testGroupMembership()
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable
            {
                // USER_ONE - SiteAdmin
                // GROUP_ONE - USER_TWO
                // GROUP_TWO - USER_TWO, USER_THREE

                // Create a site as user one
                String testGroupMembership = "testGroupMembership" + UUID.randomUUID();
                siteService.createSite(TEST_SITE_PRESET, testGroupMembership, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);

                // Get the members of the site and check that user one is a manager
                Map<String, String> members = siteService.listMembers(testGroupMembership, null, null, 0);
                assertNotNull(members);
                assertEquals(1, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));

                /**
                 * Test of isMember - ONE is member, TWO and THREE are not
                 */
                assertTrue(siteService.isMember(testGroupMembership, USER_ONE));
                assertTrue(!siteService.isMember(testGroupMembership, USER_TWO));
                assertTrue(!siteService.isMember(testGroupMembership, USER_THREE));

                /**
                 * Add a group (GROUP_TWO) with role consumer
                 */
                siteService.setMembership(testGroupMembership, groupTwo, SiteModel.SITE_CONSUMER);
                // - is the group in the list of all members?
                members = siteService.listMembers(testGroupMembership, null, null, 0);

                assertNotNull(members);
                assertEquals(2, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
                assertTrue(members.containsKey(groupTwo));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(groupTwo));

                // - is the user in the expanded list?
                members = siteService.listMembers(testGroupMembership, null, null, 0, true);
                assertNotNull(members);
                assertEquals(3, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
                assertTrue(members.containsKey(USER_TWO));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(USER_TWO));
                assertTrue(members.containsKey(USER_THREE));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(USER_THREE));

                // - is the user a member?
                assertTrue(siteService.isMember(testGroupMembership, USER_ONE));
                assertTrue(siteService.isMember(testGroupMembership, USER_TWO));
                assertTrue(siteService.isMember(testGroupMembership, USER_THREE));

                // - is the group a member?
                assertTrue(siteService.isMember(testGroupMembership, groupTwo));

                // - can we get the roles for the various members directly
                assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(testGroupMembership, USER_ONE));
                assertEquals(SiteModel.SITE_CONSUMER, siteService.getMembersRole(testGroupMembership, USER_TWO));
                assertEquals(SiteModel.SITE_CONSUMER, siteService.getMembersRole(testGroupMembership, USER_THREE));
                assertEquals(SiteModel.SITE_CONSUMER, siteService.getMembersRole(testGroupMembership, groupTwo));

                // Uses Members role info
                assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRoleInfo(testGroupMembership, USER_ONE).getMemberRole());
                /**
                 * Check we can filter this list by name and role correctly
                 */

                // - filter by authority
                members = siteService.listMembers(testGroupMembership, null, SiteModel.SITE_MANAGER, 0, true);
                assertNotNull(members);
                assertEquals(1, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));

                members = siteService.listMembers(testGroupMembership, null, SiteModel.SITE_CONSUMER, 0, true);
                assertNotNull(members);
                assertEquals(2, members.size());
                assertTrue(members.containsKey(USER_TWO));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(USER_TWO));
                assertTrue(members.containsKey(USER_THREE));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(USER_THREE));

                // - filter by name - person name
                members = siteService.listMembers(testGroupMembership, "UserOne*", null, 0, true);
                assertNotNull(members);
                assertEquals(1, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));

                // - filter by name - person name as part of group
                members = siteService.listMembers(testGroupMembership, "UserTwo*", null, 0, true);
                assertNotNull(members);
                assertEquals(1, members.size());
                assertTrue(members.containsKey(USER_TWO));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(USER_TWO));

                // - filter by name - person name without group expansion
                // (won't match as the group name doesn't contain the user's name)
                members = siteService.listMembers(testGroupMembership, "UserTwo*", null, 0, false);
                assertNotNull(members);
                assertEquals(0, members.size());

                // - filter by name - group name
                members = siteService.listMembers(testGroupMembership, GROUP_TWO, null, 0, false);
                assertNotNull(members);
                assertEquals(1, members.size());
                assertTrue(members.containsKey(groupTwo));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(groupTwo));

                // - filter by name - group display name
                members = siteService.listMembers(testGroupMembership, GROUP_TWO_DISPLAY, null, 0, false);
                assertNotNull(members);
                assertEquals(1, members.size());
                assertTrue(members.containsKey(groupTwo));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(groupTwo));

                // - filter by name - group name with expansion
                // (won't match anyone as the group name won't hit people too)
                members = siteService.listMembers(testGroupMembership, GROUP_TWO, null, 0, true);
                assertNotNull(members);
                assertEquals(0, members.size());

                /**
                 * Add a group member (USER_THREE) as an explicit member
                 */
                siteService.setMembership(testGroupMembership, USER_THREE, SiteModel.SITE_COLLABORATOR);
                // - check the explicit members list
                members = siteService.listMembers(testGroupMembership, null, null, 0);
                assertNotNull(members);
                assertEquals(3, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
                assertTrue(members.containsKey(USER_THREE));
                assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_THREE));
                assertTrue(members.containsKey(groupTwo));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(groupTwo));
                // - check the expanded members list
                members = siteService.listMembers(testGroupMembership, null, null, 0, true);
                assertNotNull(members);
                assertEquals(3, members.size());
                assertTrue(members.containsKey(USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
                assertTrue(members.containsKey(USER_TWO));
                assertEquals(SiteModel.SITE_CONSUMER, members.get(USER_TWO));
                assertTrue(members.containsKey(USER_THREE));
                assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_THREE));

                // - check is member
                assertTrue(siteService.isMember(testGroupMembership, USER_ONE));
                assertTrue(siteService.isMember(testGroupMembership, USER_TWO));
                assertTrue(siteService.isMember(testGroupMembership, USER_THREE));
                assertTrue(!siteService.isMember(testGroupMembership, USER_FOUR));

                // - is the group a member?
                assertTrue(siteService.isMember(testGroupMembership, groupTwo));
                // - check get role directly
                assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(testGroupMembership, USER_ONE));
                assertEquals(SiteModel.SITE_CONSUMER, siteService.getMembersRole(testGroupMembership, USER_TWO));
                assertEquals(SiteModel.SITE_COLLABORATOR, siteService.getMembersRole(testGroupMembership, USER_THREE));
                assertEquals(SiteModel.SITE_CONSUMER, siteService.getMembersRole(testGroupMembership, groupTwo));

                // Check permissions of added group

                // Update the permissions of the group
                siteService.setMembership(testGroupMembership, USER_THREE, SiteModel.SITE_CONTRIBUTOR);

                /**
                 * Add other group (GROUP_3) with higher (MANAGER) role
                 *
                 * - is group in list? - is new user a member? - does redefined user have highest role? USER_TWO should be Manager from group 3 having higher priority than group 2 USER_THREE should still be Contributor from explicit membership. USER_FOUR should be Manager - from group 4 sub-group
                 */
                siteService.setMembership(testGroupMembership, groupThree, SiteModel.SITE_MANAGER);

                assertTrue(siteService.isMember(testGroupMembership, USER_ONE));
                assertTrue(siteService.isMember(testGroupMembership, USER_TWO));
                assertTrue(siteService.isMember(testGroupMembership, USER_THREE));
                assertTrue(siteService.isMember(testGroupMembership, USER_FOUR));

                assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(testGroupMembership, USER_ONE));
                assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(testGroupMembership, USER_TWO));
                assertEquals(SiteModel.SITE_CONTRIBUTOR, siteService.getMembersRole(testGroupMembership, USER_THREE));
                assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(testGroupMembership, groupThree));

                // From sub group four
                assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(testGroupMembership, USER_FOUR));

                // Set a membership with an illegal role. See ALF-619.
                // I'm checking that the exception type thrown is what it should be.
                boolean failed = false;
                try
                {
                    siteService.setMembership(testGroupMembership, groupThree, "rubbish");
                }
                catch (UnknownAuthorityException expected)
                {
                    failed = true;
                }

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.deleteSite(testGroupMembership);

                if (!failed)
                {
                    fail("Expected exception not thrown.");
                }

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    /**
     * 
     * See https://issues.alfresco.com/jira/browse/MNT-2229
     */
    @Test
    public void testUserRoleInGroups()
    {
        String sitName = "testMembership2" + UUID.randomUUID();
        // Create a site as user one
        this.siteService.createSite(TEST_SITE_PRESET, sitName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);

        /**
         * Add a group (GROUP_ONE) with role COLLABORATOR
         */
        this.siteService.setMembership(sitName, this.groupOne, SiteModel.SITE_COLLABORATOR);

        /**
         * Add a group (GROUP_TWO) with role CONSUMER
         */
        this.siteService.setMembership(sitName, this.groupTwo, SiteModel.SITE_CONSUMER);

        List<SiteMemberInfo> roles = this.siteService.listMembersInfo(sitName, USER_TWO, null, 0, true);

        assertEquals(roles.get(0).getMemberRole(), SiteModel.SITE_COLLABORATOR);
    }

    /**
     * Tests the visibility of a site
     * 
     * See https://issues.alfresco.com/jira/browse/JAWS-291
     */
    @Test
    public void testSiteVisibility()
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                // Create a public site
                SiteInfo siteInfo = createTestSiteWithContent("testSiteVisibilityPublicSite", "testComp", SiteVisibility.PUBLIC);
                // - is the value on the site nodeRef correct?
                assertEquals(SiteVisibility.PUBLIC.toString(), nodeService.getProperty(siteInfo.getNodeRef(), SiteModel.PROP_SITE_VISIBILITY));
                // - is the site info correct?
                checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testSiteVisibilityPublicSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
                siteInfo = siteService.getSite("testSiteVisibilityPublicSite");
                checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testSiteVisibilityPublicSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
                // - are the permissions correct for non-members?
                testVisibilityPermissions("Testing visibility of public site", USER_TWO, siteInfo, true, true);

                // Create a moderated site
                siteInfo = createTestSiteWithContent("testSiteVisibilityModeratedSite", "testComp", SiteVisibility.MODERATED);
                // - is the value on the site nodeRef correct?
                assertEquals(SiteVisibility.MODERATED.toString(), nodeService.getProperty(siteInfo.getNodeRef(), SiteModel.PROP_SITE_VISIBILITY));
                // - is the site info correct?
                checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testSiteVisibilityModeratedSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);
                siteInfo = siteService.getSite("testSiteVisibilityModeratedSite");
                checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testSiteVisibilityModeratedSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);
                // - are the permissions correct for non-members?
                testVisibilityPermissions("Testing visibility of moderated site", USER_TWO, siteInfo, true, false);

                // Create a private site
                siteInfo = createTestSiteWithContent("testSiteVisibilityPrivateSite", "testComp", SiteVisibility.PRIVATE);
                // - is the value on the site nodeRef correct?
                assertEquals(SiteVisibility.PRIVATE.toString(), nodeService.getProperty(siteInfo.getNodeRef(), SiteModel.PROP_SITE_VISIBILITY));
                // - is the site info correct?
                checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testSiteVisibilityPrivateSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
                siteInfo = siteService.getSite("testSiteVisibilityPrivateSite");
                checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testSiteVisibilityPrivateSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
                // - are the permissions correct for non-members?
                testVisibilityPermissions("Testing visibility of private site", USER_TWO, siteInfo, false, false);

                SiteInfo changeSite = createTestSiteWithContent("testSiteVisibilityChangeSite", "testComp", SiteVisibility.PUBLIC);
                // Switch from public -> moderated
                changeSite.setVisibility(SiteVisibility.MODERATED);
                siteService.updateSite(changeSite);
                // - check the updated sites visibility
                siteInfo = siteService.getSite("testSiteVisibilityChangeSite");
                checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testSiteVisibilityChangeSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);
                testVisibilityPermissions("Testing visibility of moderated site", USER_TWO, siteInfo, true, false);

                // Switch from moderated -> private
                changeSite.setVisibility(SiteVisibility.PRIVATE);
                siteService.updateSite(changeSite);
                // - check the updated sites visibility
                siteInfo = siteService.getSite("testSiteVisibilityChangeSite");
                checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testSiteVisibilityChangeSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);
                testVisibilityPermissions("Testing visibility of moderated site", USER_TWO, siteInfo, false, false);

                // Switch from private -> public
                changeSite.setVisibility(SiteVisibility.PUBLIC);
                siteService.updateSite(changeSite);
                // - check the updated sites visibility
                siteInfo = siteService.getSite("testSiteVisibilityChangeSite");
                checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testSiteVisibilityChangeSite", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
                testVisibilityPermissions("Testing visibility of moderated site", USER_TWO, siteInfo, true, true);

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.deleteSite("testSiteVisibilityPublicSite");
                siteService.deleteSite("testSiteVisibilityModeratedSite");
                siteService.deleteSite("testSiteVisibilityPrivateSite");
                siteService.deleteSite("testSiteVisibilityChangeSite");

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    /**
     * Gets the authorities and their allowed permissions for a site root
     */
    private Map<String, Set<String>> getAllowedPermissionsMap(SiteInfo site)
    {
        NodeRef nodeRef = site.getNodeRef();
        return getAllowedPermissionsMap(nodeRef);
    }

    /**
     * Gets the authorities and their allowed permissions for a node
     */
    private Map<String, Set<String>> getAllowedPermissionsMap(NodeRef nodeRef)
    {
        Map<String, Set<String>> perms = new HashMap<String, Set<String>>();
        for (AccessPermission ap : permissionService.getAllSetPermissions(nodeRef))
        {
            if (ap.getAccessStatus() == AccessStatus.ALLOWED)
            {
                Set<String> permsValue = perms.get(ap.getAuthority());
                if (permsValue == null)
                {
                    permsValue = new HashSet<String>();
                }
                permsValue.add(ap.getPermission());
                perms.put(ap.getAuthority(), permsValue);
            }
        }
        return perms;
    }

    /**
     * ALF-10343 - When the default public group for sites isn't EVERYBODY, check that creating and altering sites results in the correct permissions
     */
    @Test
    public void testNonDefaultPublicGroupPermissions() throws Exception
    {
        // Sanity check the current permissions
        assertEquals(PermissionService.ALL_AUTHORITIES, sysAdminParams.getSitePublicGroup());

        // Change the public site group
        SysAdminParamsImpl sp = new SysAdminParamsImpl();
        sp.setSitePublicGroup(groupFour);
        siteServiceImpl.setSysAdminParams(sp);

        // Create sites of the three types
        SiteInfo s1 = this.siteService.createSite(TEST_SITE_PRESET, "SiteTest_priv", "priv", "priv", SiteVisibility.PRIVATE);
        SiteInfo s2 = this.siteService.createSite(TEST_SITE_PRESET, "SiteTest_mod", "mod", "mod", SiteVisibility.MODERATED);
        SiteInfo s3 = this.siteService.createSite(TEST_SITE_PRESET, "SiteTest_pub", "pub", "pub", SiteVisibility.PUBLIC);

        // Check the permissions on them
        // Everyone has read permissions only, not Consumer
        assertTrue(getAllowedPermissionsMap(s1).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));
        assertTrue(getAllowedPermissionsMap(s2).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));
        assertTrue(getAllowedPermissionsMap(s3).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));

        // On the public + moderated sites, the special group will be a Consumer
        assertEquals(null, getAllowedPermissionsMap(s1).get(groupFour));
        assertTrue(getAllowedPermissionsMap(s2).get(groupFour).contains(SiteModel.SITE_CONSUMER));
        assertTrue(getAllowedPermissionsMap(s3).get(groupFour).contains(SiteModel.SITE_CONSUMER));

        // Our current user will be Manager
        assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(s1.getShortName(), USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(s2.getShortName(), USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(s3.getShortName(), USER_ONE));

        // Swap the visibilites around, private+moderated -> public, public -> private
        s1.setVisibility(SiteVisibility.PUBLIC);
        s2.setVisibility(SiteVisibility.PUBLIC);
        s3.setVisibility(SiteVisibility.PRIVATE);
        siteService.updateSite(s1);
        siteService.updateSite(s2);
        siteService.updateSite(s3);

        // Check the permissions now

        // Everyone still has read permissions everywhere, but nothing more
        assertTrue(getAllowedPermissionsMap(s1).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));
        assertTrue(getAllowedPermissionsMap(s2).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));
        assertTrue(getAllowedPermissionsMap(s3).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));

        // The site public group has consumer permissions on mod+public
        assertTrue(getAllowedPermissionsMap(s1).get(groupFour).contains(SiteModel.SITE_CONSUMER));
        assertTrue(getAllowedPermissionsMap(s2).get(groupFour).contains(SiteModel.SITE_CONSUMER));
        assertEquals(null, getAllowedPermissionsMap(s3).get(groupFour));

        // Our user is still the manager
        assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(s1.getShortName(), USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(s2.getShortName(), USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(s3.getShortName(), USER_ONE));

        // Swap them back again
        s1.setVisibility(SiteVisibility.PRIVATE);
        s2.setVisibility(SiteVisibility.MODERATED);
        s3.setVisibility(SiteVisibility.PUBLIC);
        siteService.updateSite(s1);
        siteService.updateSite(s2);
        siteService.updateSite(s3);

        // Check the permissions have restored

        // Everyone only has read permissions
        assertTrue(getAllowedPermissionsMap(s1).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));
        assertTrue(getAllowedPermissionsMap(s2).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));
        assertTrue(getAllowedPermissionsMap(s3).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));

        // The site public group has consumer permissions on mod+public
        assertEquals(null, getAllowedPermissionsMap(s1).get(groupFour));
        assertTrue(getAllowedPermissionsMap(s2).get(groupFour).contains(SiteModel.SITE_CONSUMER));
        assertTrue(getAllowedPermissionsMap(s3).get(groupFour).contains(SiteModel.SITE_CONSUMER));

        // Our user is still the manager
        assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(s1.getShortName(), USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(s2.getShortName(), USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, siteService.getMembersRole(s3.getShortName(), USER_ONE));
    }

    private SiteInfo createTestSiteWithContent(String siteShortName, String componentId, SiteVisibility visibility)
    {
        return this.createTestSiteWithContent(siteShortName, componentId, visibility, "");
    }

    /**
     * Creates a site with a simple content tree within it. The content looks like
     * 
     * <pre>
     * [site] {siteShortName}
     *    |
     *    --- [siteContainer] {componentId}
     *          |
     *          --- [cm:content] fileFolderPrefix + "file.txt"
     *          |
     *          |-- [folder] fileFolderPrefix + "folder"
     *                  |
     *                  |-- [cm:content] fileFolderPrefix + "fileInFolder.txt"
     *                  |
     *                  |-- [folder] fileFolderPrefix + "subfolder"
     *                         |
     *                         |-- [cm:content] fileFolderPrefix + "fileInSubfolder.txt"
     * </pre>
     * 
     * @param siteShortName
     *            short name for the site
     * @param componentId
     *            the component id for the container
     * @param visibility
     *            visibility for the site.
     * @param fileFolderPrefix
     *            a prefix String to put on all folders/files created.
     */
    private SiteInfo createTestSiteWithContent(String siteShortName, String componentId, SiteVisibility visibility, String fileFolderPrefix)
    {
        // Create a public site
        SiteInfo siteInfo = this.siteService.createSite(TEST_SITE_PRESET,
                siteShortName,
                TEST_TITLE,
                TEST_DESCRIPTION,
                visibility);

        NodeRef siteContainer = this.siteService.createContainer(siteShortName, componentId, ContentModel.TYPE_FOLDER, null);
        FileInfo fileInfo = this.fileFolderService.create(siteContainer, fileFolderPrefix + "file.txt", ContentModel.TYPE_CONTENT);
        ContentWriter writer = this.fileFolderService.getWriter(fileInfo.getNodeRef());
        writer.putContent("Just some old content that doesn't mean anything");

        FileInfo folder1Info = this.fileFolderService.create(siteContainer, fileFolderPrefix + "folder", ContentModel.TYPE_FOLDER);

        FileInfo fileInfo2 = this.fileFolderService.create(folder1Info.getNodeRef(), fileFolderPrefix + "fileInFolder.txt", ContentModel.TYPE_CONTENT);
        ContentWriter writer2 = this.fileFolderService.getWriter(fileInfo2.getNodeRef());
        writer2.putContent("Just some old content that doesn't mean anything");

        FileInfo folder2Info = this.fileFolderService.create(folder1Info.getNodeRef(), fileFolderPrefix + "subfolder", ContentModel.TYPE_FOLDER);

        FileInfo fileInfo3 = this.fileFolderService.create(folder2Info.getNodeRef(), fileFolderPrefix + "fileInSubfolder.txt", ContentModel.TYPE_CONTENT);
        ContentWriter writer3 = this.fileFolderService.getWriter(fileInfo3.getNodeRef());
        writer3.putContent("Just some old content that doesn't mean anything");

        return siteInfo;
    }

    private void testVisibilityPermissions(String message, String userName, SiteInfo siteInfo, boolean listSite, boolean readSite)
    {
        String holdUser = this.authenticationComponent.getCurrentUserName();
        this.authenticationComponent.setCurrentUser(userName);
        try
        {
            // Can the site be seen in the list sites by the user?
            List<SiteInfo> sites = this.siteService.listSites(null, null);
            boolean siteInList = sites.contains(siteInfo);
            if (listSite == true && siteInList == false)
            {
                fail(message + ":  The site '" + siteInfo.getShortName() + "' was expected in the list of sites for user '" + userName + "'");
            }
            else if (listSite == false && siteInList == true)
            {
                fail(message + ":  The site '" + siteInfo.getShortName() + "' was NOT expected in the list of sites for user '" + userName + "'");
            }

            if (siteInList == true)
            {
                try
                {
                    // Can site content be read by the user?
                    NodeRef folder = this.siteService.getContainer(siteInfo.getShortName(), "testComp");
                    @SuppressWarnings("unused")
                    List<FileInfo> files = this.fileFolderService.listFiles(folder);
                    if (readSite == false)
                    {
                        fail(message + ":  Content of the site '" + siteInfo.getShortName() + "' was NOT expected to be read by user '" + userName + "'");
                    }
                }
                catch (Exception exception)
                {
                    if (readSite == true)
                    {
                        fail(message + ":  Content of the site '" + siteInfo.getShortName() + "' was expected to be read by user '" + userName + "'");
                    }
                }
            }
        }
        finally
        {
            this.authenticationComponent.setCurrentUser(holdUser);
        }
    }

    /**
     * Create a site with a USER manager. Add Group manager membership.
     * 
     * Lower User membership - should be O.K. because of Group Membership Lower Group membership - should be prevented (last manager)
     * 
     * Reset User membership to Manager
     * 
     * Lower Group membership - should be O.K. because of User Membership Lower User membership - should be prevented (last manager)
     * 
     */
    @Test
    public void testALFCOM_3109()
    {
        // USER_ONE - SiteManager
        // GROUP_TWO - Manager

        String siteName = "testALFCOM_3019";

        // Create a site as user one
        this.siteService.createSite(TEST_SITE_PRESET, siteName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);

        Map<String, String> members = this.siteService.listMembers(siteName, null, null, 0);
        String managerName = members.keySet().iterator().next();

        /**
         * Add a group (GROUP_TWO) with role Manager
         */
        this.siteService.setMembership(siteName, this.groupTwo, SiteModel.SITE_MANAGER);

        // Should be allowed
        this.siteService.setMembership(siteName, managerName, SiteModel.SITE_CONTRIBUTOR);

        /**
         * Should not be allowed to delete last group
         */
        try
        {
            this.siteService.setMembership(siteName, this.groupTwo, SiteModel.SITE_CONTRIBUTOR);
            fail();
        }
        catch (Exception e)
        {
            // Should go here
        }

        this.siteService.setMembership(siteName, managerName, SiteModel.SITE_MANAGER);

        this.siteService.setMembership(siteName, this.groupTwo, SiteModel.SITE_CONTRIBUTOR);

        /**
         * Should not be allowed to delete last user
         */
        try
        {
            this.siteService.setMembership(siteName, managerName, SiteModel.SITE_CONTRIBUTOR);
            fail();
        }
        catch (Exception e)
        {
            // Should go here
        }
    }

    /**
     * Create a site with a USER manager. Add Group manager membership.
     * 
     * Remove User membership - should be O.K. because of Group Membership Remove Group membership - should be prevented (last manager)
     * 
     * Add User membership to Manager
     * 
     * Remove Group membership - should be O.K. because of User Membership Remove User membership - should be prevented (last manager)
     * 
     */
    @Test
    public void testALFCOM_3111()
    {
        // USER_ONE - SiteManager
        // GROUP_TWO - Manager

        String siteName = "testALFCOM_3019";

        // Create a site as user one
        this.siteService.createSite(TEST_SITE_PRESET, siteName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);

        Map<String, String> members = this.siteService.listMembers(siteName, null, null, 0);
        String managerName = members.keySet().iterator().next();

        /**
         * Add a group (GROUP_TWO) with role Manager
         */
        this.siteService.setMembership(siteName, this.groupTwo, SiteModel.SITE_MANAGER);

        // Should be allowed
        this.siteService.removeMembership(siteName, managerName);

        /**
         * Should not be allowed to delete last group
         */
        try
        {
            this.siteService.removeMembership(siteName, this.groupTwo);
            fail();
        }
        catch (Exception e)
        {
            // Should go here
        }

        this.siteService.setMembership(siteName, managerName, SiteModel.SITE_MANAGER);

        this.siteService.removeMembership(siteName, this.groupTwo);

        /**
         * Should not be allowed to delete last user
         */
        try
        {
            this.siteService.removeMembership(siteName, managerName);
            fail();
        }
        catch (Exception e)
        {
            // Should go here
        }
    }

    /**
     * Create a private site.
     *
     * Attempt to access a private site by someone that is not a consumer of that site.
     * 
     */
    @Test
    public void testETHREEOH_1268()
    {
        RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable
            {
                // USER_ONE - SiteManager
                // GROUP_TWO - Manager

                String siteName = "testALFCOM_XXXX" + UUID.randomUUID();

                // Create a site as user one
                siteService.createSite(TEST_SITE_PRESET, siteName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);

                SiteInfo si = siteService.getSite(siteName);

                assertNotNull("site info is null", si);

                authenticationComponent.setCurrentUser(USER_TWO);

                si = siteService.getSite(siteName);

                assertNull("site info is not null", si);

                authenticationComponent.setSystemUserAsCurrentUser();
                siteService.deleteSite(siteName);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(work);
    }

    /**
     * ALF-3200 You shouldn't be able to rename a Site using the normal node service type operations, because the relationship between a site and its authorities is based on a pattern that uses the site name. However, you are free to change a site's display name.
     */
    @Test
    public void testALF_3200() throws Exception
    {
        // Create the site
        String siteName = "testALF_3200";
        SiteInfo siteInfo = this.siteService.createSite(
                TEST_SITE_PRESET, siteName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);

        // Grab the details
        NodeRef siteNodeRef = siteInfo.getNodeRef();

        // Try to rename it
        try
        {
            fileFolderService.rename(siteNodeRef, "RenamedName");
            fail("Shouldn't be able to rename a site but did");
        }
        catch (SiteServiceException e)
        {
            // expected
        }

        // Now just try to change the display name (title) via the node service
        assertEquals(TEST_TITLE, nodeService.getProperty(siteNodeRef, ContentModel.PROP_TITLE));

        String newName = "ChangedTitleName";
        String newName2 = "Changed2Title2Name";
        nodeService.setProperty(siteNodeRef, ContentModel.PROP_TITLE, newName);
        assertEquals(newName, nodeService.getProperty(siteNodeRef, ContentModel.PROP_TITLE));

        // And also via the site info
        siteInfo = this.siteService.getSite(siteNodeRef);
        assertEquals(newName, siteInfo.getTitle());
        siteInfo.setTitle(newName2);
        siteService.updateSite(siteInfo);

        assertEquals(newName2, siteInfo.getTitle());
        assertEquals(newName2, nodeService.getProperty(siteNodeRef, ContentModel.PROP_TITLE));
    }

    @Test
    public void testALF_5556() throws Exception
    {
        String siteName = "testALF_5556";
        SiteInfo siteInfo = this.siteService.createSite(
                TEST_SITE_PRESET, siteName, TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.MODERATED);

        // create a container for the site
        NodeRef container = this.siteService.createContainer(siteInfo.getShortName(), "folder.component", null, null);

        // Try to rename the container
        try
        {
            fileFolderService.rename(container, "RenamedContainer");
            fail("Shouldn't be able to rename a container but was able to");
        }
        catch (SiteServiceException e)
        {
            // expected
        }
    }

    @Test
    public void testALF8036_PermissionsAfterCopyingFolderBetweenSites() throws Exception
    {
        alf8036Impl(true);
    }

    private void alf8036Impl(boolean copyNotMove)
    {
        // Create two test sites
        SiteInfo fromSite = this.createTestSiteWithContent("fromSite", "doclib", SiteVisibility.PUBLIC, "FROM");
        SiteInfo toSite = this.createTestSiteWithContent("toSite", "doclib", SiteVisibility.PUBLIC, "TO");

        // Find the folder to be copied/moved.
        NodeRef fromDoclibContainer = nodeService.getChildByName(fromSite.getNodeRef(), ContentModel.ASSOC_CONTAINS, "doclib");
        assertNotNull(fromDoclibContainer);
        NodeRef fromFolder = nodeService.getChildByName(fromDoclibContainer, ContentModel.ASSOC_CONTAINS, "FROMfolder");
        assertNotNull(fromFolder);
        NodeRef fromSubFolder = nodeService.getChildByName(fromFolder, ContentModel.ASSOC_CONTAINS, "FROMsubfolder");
        assertNotNull(fromSubFolder);

        // The bug is only observed if we set some specific permissions on the folder.
        // We'll demote contributors to consumer-level permissions.
        permissionService.setPermission(fromFolder, siteServiceImpl.getSiteRoleGroup(fromSite.getShortName(), SiteModel.SITE_CONTRIBUTOR, true), SiteModel.SITE_CONSUMER, false);

        // And we'll change permissions on a subfolder too
        permissionService.setPermission(fromSubFolder, siteServiceImpl.getSiteRoleGroup(fromSite.getShortName(), SiteModel.SITE_COLLABORATOR, true), SiteModel.SITE_CONSUMER, false);

        // Find the folder to copy/move it to.
        NodeRef toDoclibContainer = nodeService.getChildByName(toSite.getNodeRef(), ContentModel.ASSOC_CONTAINS, "doclib");
        assertNotNull(toDoclibContainer);
        NodeRef toFolder = nodeService.getChildByName(toDoclibContainer, ContentModel.ASSOC_CONTAINS, "TOfolder");
        assertNotNull(toFolder);

        // Copy/move it
        NodeRef relocatedNode;
        if (copyNotMove)
        {
            relocatedNode = copyService.copy(fromFolder, toFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, true);
        }
        else
        {
            relocatedNode = nodeService.moveNode(fromFolder, toDoclibContainer, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS).getChildRef();
        }
        siteService.cleanSitePermissions(relocatedNode, null);

        // Ensure the permissions on the copied/moved node are those of the target site and not those of the source site.
        Map<String, String> expectedPermissions = new HashMap<String, String>();
        expectedPermissions.put(siteService.getSiteRoleGroup(toSite.getShortName(), SiteModel.SITE_MANAGER), SiteModel.SITE_MANAGER);
        expectedPermissions.put(siteService.getSiteRoleGroup(toSite.getShortName(), SiteModel.SITE_COLLABORATOR), SiteModel.SITE_COLLABORATOR);
        expectedPermissions.put(siteService.getSiteRoleGroup(toSite.getShortName(), SiteModel.SITE_CONTRIBUTOR), SiteModel.SITE_CONTRIBUTOR);
        expectedPermissions.put(siteService.getSiteRoleGroup(toSite.getShortName(), SiteModel.SITE_CONSUMER), SiteModel.SITE_CONSUMER);

        validatePermissionsOnRelocatedNode(fromSite, toSite, relocatedNode, expectedPermissions);

        // Get the subfolder and check its permissions too.
        NodeRef copyOfSubFolder = nodeService.getChildByName(relocatedNode, ContentModel.ASSOC_CONTAINS, "FROMsubfolder");
        assertNotNull(copyOfSubFolder);
        validatePermissionsOnRelocatedNode(fromSite, toSite, copyOfSubFolder, expectedPermissions);
    }

    /**
     * ALF-1017 - Non sites in the Sites Space container shouldn't break the listing methods
     */
    @Test
    public void testALF_1017_nonSitesInSitesSpace() throws Exception
    {
        // Initially listing is fine
        List<SiteInfo> sites = this.siteService.listSites(null, null);
        assertNotNull("sites list was null.", sites);
        final int preexistingSitesCount = sites.size();

        // Create some sites
        SiteInfo site1 = this.siteService.createSite(TEST_SITE_PRESET, "mySiteOne", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        SiteInfo site2 = this.siteService.createSite(TEST_SITE_PRESET, "mySiteTwo", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PRIVATE);

        // Listing is still ok
        sites = this.siteService.listSites(null, null);
        assertNotNull("sites list was null.", sites);
        assertEquals(preexistingSitesCount + 2, sites.size());

        // Now add a random folder, and a random document to the sites root
        final NodeRef sitesSpace = this.nodeService.getPrimaryParent(site1.getNodeRef()).getParentRef();
        final NodeRef folder = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>() {
            @Override
            public NodeRef doWork() throws Exception
            {
                return nodeService.createNode(
                        sitesSpace, ContentModel.ASSOC_CONTAINS,
                        QName.createQName("Folder"), ContentModel.TYPE_FOLDER).getChildRef();
            }
        });
        final NodeRef document = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>() {
            @Override
            public NodeRef doWork() throws Exception
            {
                return nodeService.createNode(
                        sitesSpace, ContentModel.ASSOC_CONTAINS,
                        QName.createQName("Document"), ContentModel.TYPE_CONTENT).getChildRef();
            }
        });

        // Listing should still be fine, and count won't have increased
        sites = this.siteService.listSites(null, null);
        assertNotNull("sites list was null.", sites);
        assertEquals(preexistingSitesCount + 2, sites.size());

        // Delete one site, listing still ok
        this.siteService.deleteSite(site2.getShortName());
        sites = this.siteService.listSites(null, null);
        assertNotNull("sites list was null.", sites);
        assertEquals(preexistingSitesCount + 1, sites.size());

        // Tidy up the random nodes, listing still fine
        this.nodeService.deleteNode(folder);
        this.nodeService.deleteNode(document);

        sites = this.siteService.listSites(null, null);
        assertNotNull("sites list was null.", sites);
        assertEquals(preexistingSitesCount + 1, sites.size());
    }

    private SiteInfo createSite(String siteShortName, String componentId, SiteVisibility visibility)
    {
        // Create a public site
        SiteInfo siteInfo = this.siteService.createSite(TEST_SITE_PRESET,
                siteShortName,
                TEST_TITLE,
                TEST_DESCRIPTION,
                visibility);
        this.siteService.createContainer(siteShortName, componentId, ContentModel.TYPE_FOLDER, null);
        return siteInfo;
    }

    @Test
    public void testRenameSite()
    {
        // test that changing the name of a site generates an appropriate exception

        try
        {
            String siteName = GUID.generate();

            SiteInfo siteInfo = createSite(siteName, "doclib", SiteVisibility.PUBLIC);
            NodeRef childRef = siteInfo.getNodeRef();

            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_NAME, siteName + "Renamed");

            nodeService.addProperties(childRef, props);

            fail("Should have caught rename");
        }
        catch (SiteServiceException e)
        {
            assertTrue(e.getMessage().contains("can not be renamed"));
        }
    }

    private void validatePermissionsOnRelocatedNode(SiteInfo fromSite,
            SiteInfo toSite, NodeRef relocatedNode, Map<String, String> expectedPermissions)
    {
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(relocatedNode);

        // None of the 'from' site permissions should be there.
        for (String sitePermission : SiteModel.STANDARD_PERMISSIONS)
        {
            String siteRoleGroup = siteServiceImpl.getSiteRoleGroup(fromSite.getShortName(), sitePermission, true);
            AccessPermission ap = getPermission(permissions, siteRoleGroup);
            assertNull("Permission " + siteRoleGroup + " was unexpectedly present", ap);
        }

        // All of the 'to' site permissions should be there.
        for (String authority : expectedPermissions.keySet())
        {
            AccessPermission ap = getPermission(permissions, authority);
            assertNotNull("Permission " + authority + " missing", ap);

            assertEquals(authority, ap.getAuthority());
            assertEquals("Wrong permission for " + authority, expectedPermissions.get(authority), ap.getPermission());
            assertTrue(ap.isInherited());
        }
    }

    private AccessPermission getPermission(Set<AccessPermission> permissions, String expectedAuthority)
    {
        AccessPermission result = null;
        for (AccessPermission ap : permissions)
        {
            if (expectedAuthority.equals(ap.getAuthority()))
            {
                result = ap;
            }
        }
        return result;
    }

    @Test
    public void testPermissionsAfterMovingFolderBetweenSites() throws Exception
    {
        alf8036Impl(false);
    }

    // == Test the JavaScript API ==

    @Test
    public void testJSAPI() throws Exception
    {
        // Create a site with a custom property
        SiteInfo siteInfo = this.siteService.createSite(TEST_SITE_PRESET, "mySiteWithCustomProperty", TEST_TITLE, TEST_DESCRIPTION, SiteVisibility.PUBLIC);
        NodeRef siteNodeRef = siteInfo.getNodeRef();
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(QName.createQName(SiteModel.SITE_CUSTOM_PROPERTY_URL, "additionalInformation"), "information");
        this.nodeService.addAspect(siteNodeRef, QName.createQName(SiteModel.SITE_MODEL_URL, "customSiteProperties"), properties);

        // Create a model to pass to the unit test scripts
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("customSiteName", "mySiteWithCustomProperty");
        model.put("preexistingSiteCount", siteService.listSites(null, null).size());

        // Execute the unit test script
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/site/script/test_siteService.js");
        this.scriptService.executeScript(location, model);
    }

    @Test
    public void testListMembersInfo()
    {
        String siteShortName = "testMemberInfo";

        // Create a site as user one
        this.siteService.createSite(TEST_SITE_PRESET, siteShortName, TEST_TITLE, TEST_DESCRIPTION,
                SiteVisibility.PRIVATE);

        // Get the members of the site and check that user one is a manager
        List<SiteMemberInfo> members = this.siteService.listMembersInfo(siteShortName, null, null, 0, false);
        assertNotNull(members);
        assertEquals(1, members.size());
        SiteMemberInfo user = members.get(0);
        assertNotNull(user);
        assertTrue(user.getMemberName().equals(USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, user.getMemberRole());
        assertFalse("USER_ONE is NOT member of any group", user.isMemberOfGroup());

        // GROUP_TWO - USER_TWO, USER_THREE
        this.siteService.setMembership(siteShortName, this.groupTwo, SiteModel.SITE_COLLABORATOR);
        this.siteService.setMembership(siteShortName, USER_FOUR, SiteModel.SITE_CONSUMER);

        // Get the members of the site in expanded list
        members = this.siteService.listMembersInfo(siteShortName, null, null, 0, true);
        assertNotNull(members);
        assertEquals(4, members.size());
        // Get USER_TWO who is a member of group two
        user = lookupMemberInfoByUserName(members, USER_TWO);
        assertNotNull(user);
        assertEquals(SiteModel.SITE_COLLABORATOR, user.getMemberRole());
        assertTrue("USER_TWO is member of group two", user.isMemberOfGroup());
        // Get USER_THREE who is a member of group two
        user = lookupMemberInfoByUserName(members, USER_THREE);
        assertNotNull(user);
        assertEquals(SiteModel.SITE_COLLABORATOR, user.getMemberRole());
        assertTrue("USER_THREE is member of group two", user.isMemberOfGroup());
        // Get USER_FOUR
        user = lookupMemberInfoByUserName(members, USER_FOUR);
        assertNotNull(user);
        assertEquals(SiteModel.SITE_CONSUMER, user.getMemberRole());
        assertFalse("USER_FOUR is NOT member of any group", user.isMemberOfGroup());
    }

    private SiteMemberInfo lookupMemberInfoByUserName(List<SiteMemberInfo> members, String name)
    {
        for (SiteMemberInfo info : members)
        {
            if (name.equals(info.getMemberName()))
            {
                return info;
            }
        }
        return null;
    }

    /**
     * From CLOUD-957, insure that GROUP_EVERYONE does not have read access to private sites' containers.
     */
    @Test
    public void testPrivateSite() throws Exception
    {
        String siteName = GUID.generate();

        SiteInfo siteInfo = createSite(siteName, "doclib", SiteVisibility.PRIVATE);

        NodeRef container = this.siteService.getContainer(siteInfo.getShortName(), "doclib");

        assertNull("GROUP_EVERYONE shouldn't have any permissions on a private site's containers", getAllowedPermissionsMap(container).get(PermissionService.ALL_AUTHORITIES));

    }

    /**
     * From CLOUD-957, insure that GROUP_EVERYONE does not have read access to moderated sites' containers.
     */
    @Test
    public void testModeratedSite() throws Exception
    {
        String siteName = GUID.generate();

        SiteInfo siteInfo = createSite(siteName, "doclib", SiteVisibility.MODERATED);

        NodeRef container = this.siteService.getContainer(siteInfo.getShortName(), "doclib");

        assertNull("GROUP_EVERYONE shouldn't have any permissions on a moderated site's containers", getAllowedPermissionsMap(container).get(PermissionService.ALL_AUTHORITIES));

    }

    /**
     * From MNT-14452, insure that GROUP_EVERYONE have read access to public sites' containers.
     */
    @Test
    public void testChangeSiteVisibility()
    {
        String siteName = GUID.generate();

        // Check Private->public
        SiteInfo siteInfo = createSite(siteName, "doclib", SiteVisibility.PRIVATE);

        NodeRef container = this.siteService.getContainer(siteInfo.getShortName(), "doclib");

        siteInfo.setVisibility(SiteVisibility.PUBLIC);
        siteService.updateSite(siteInfo);

        assertTrue(getAllowedPermissionsMap(container).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));

        // Check public->moderated
        siteInfo.setVisibility(SiteVisibility.MODERATED);
        siteService.updateSite(siteInfo);

        assertNull("GROUP_EVERYONE shouldn't have any permissions on a moderated site's containers", getAllowedPermissionsMap(container).get(PermissionService.ALL_AUTHORITIES));

        // Check moderated->public
        siteInfo.setVisibility(SiteVisibility.PUBLIC);
        siteService.updateSite(siteInfo);

        assertTrue(getAllowedPermissionsMap(container).get(PermissionService.ALL_AUTHORITIES).contains("ReadPermissions"));

        // Check public->private
        siteInfo.setVisibility(SiteVisibility.PRIVATE);
        siteService.updateSite(siteInfo);

        assertNull("GROUP_EVERYONE shouldn't have any permissions on a moderated site's containers", getAllowedPermissionsMap(container).get(PermissionService.ALL_AUTHORITIES));

    }

    @Test
    public void testDeleteSiteAsAdministrator() throws Exception
    {
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        String shortName = GUID.generate();
        createSite(shortName, "doclib", SiteVisibility.MODERATED);
        // Do tests as site admin
        this.authenticationComponent.setCurrentUser(USER_ONE);

        try
        {
            // try to delete the site permission denied
            siteService.deleteSite(shortName);

            fail("We should not reach this point. the user that tries to run this code, add the file, is not yet a member of the site");
        }
        catch (AccessDeniedException e)
        {
            // Expected
        }

        this.authenticationComponent.setCurrentUser(USER_SITE_ADMIN);

        siteService.deleteSite(shortName);
    }
}
