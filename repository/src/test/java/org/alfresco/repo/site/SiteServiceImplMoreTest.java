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

package org.alfresco.repo.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;
import org.junit.rules.TestName;
import org.springframework.extensions.webscripts.GUID;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.repo.node.archive.RestoreNodeReport.RestoreStatus;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.alfresco.util.test.junitrules.TemporarySites.TestSiteAndMemberInfo;
import org.alfresco.util.test.junitrules.TemporarySitesTest;

/**
 * This class contains some tests for the {@link SiteServiceImpl} - in addition to those already included in {@link SiteServiceImplTest}. This uses JUnit 4 annotations and JUnit Rules.
 * 
 * TODO Refactor the two classes together into one common approach.
 * 
 * @author Neil Mc Erlean
 * @since 4.0.3
 */
@Category(OwnJVMTestsCategory.class)
public class SiteServiceImplMoreTest
{
    protected static Log log = LogFactory.getLog(SiteServiceImplMoreTest.class);

    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = ApplicationContextInit.createStandardContextWithOverrides("classpath:sites/test-"
            + TemporarySitesTest.class.getSimpleName() + "-context.xml");

    // A rule to manage test nodes reused across all the test methods
    public static TemporarySites STATIC_TEST_SITES = new TemporarySites(APP_CONTEXT_INIT);

    // Tie them together in a static Rule Chain
    @ClassRule
    public static RuleChain staticRuleChain = RuleChain.outerRule(APP_CONTEXT_INIT)
            .around(STATIC_TEST_SITES);

    public RunAsFullyAuthenticatedRule runAllTestsAsAdmin = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getAdminUserName());
    public TemporarySites perMethodTestSites = new TemporarySites(APP_CONTEXT_INIT);
    public AlfrescoPerson testUser = new AlfrescoPerson(APP_CONTEXT_INIT);

    // Need to ensure the rules are in the correct order so that we're authenticated as admin before trying to create the person.
    @Rule
    public RuleChain ruleChain = RuleChain.outerRule(runAllTestsAsAdmin)
            .around(perMethodTestSites)
            .around(testUser);

    @Rule
    public TestName testName = new TestName();

    // Various services
    private static AuthorityService AUTHORITY_SERVICE;
    private static NamespaceService NAMESPACE_SERVICE;
    private static NodeService NODE_SERVICE;
    private static NodeArchiveService NODE_ARCHIVE_SERVICE;
    private static SiteService SITE_SERVICE;
    private static CheckOutCheckInService COCI_SERVICE;
    private static RetryingTransactionHelper TRANSACTION_HELPER;
    private static PermissionService PERMISSION_SERVICE;
    private static MutableAuthenticationService AUTHENTICATION_SERVICE;
    private static PersonService PERSON_SERVICE;
    private static FileFolderService FILE_FOLDER_SERVICE;
    private static AuthenticationComponent AUTHENTICATION_COMPONENT;
    private static LockService LOCK_SERVICE;

    private static String TEST_SITE_NAME, TEST_SUB_SITE_NAME;
    private static TestSiteAndMemberInfo TEST_SITE_WITH_MEMBERS;

    @BeforeClass
    public static void initStaticData() throws Exception
    {
        AUTHORITY_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("AuthorityService", AuthorityService.class);
        NAMESPACE_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("namespaceService", NamespaceService.class);
        NODE_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("NodeService", NodeService.class);
        NODE_ARCHIVE_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("nodeArchiveService", NodeArchiveService.class);
        SITE_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("siteService", SiteService.class);
        COCI_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("checkOutCheckInService", CheckOutCheckInService.class);
        TRANSACTION_HELPER = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        PERMISSION_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("permissionServiceImpl", PermissionService.class);
        AUTHENTICATION_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("authenticationService", MutableAuthenticationService.class);
        PERSON_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("PersonService", PersonService.class);
        FILE_FOLDER_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("FileFolderService", FileFolderService.class);
        AUTHENTICATION_COMPONENT = APP_CONTEXT_INIT.getApplicationContext().getBean("authenticationComponent", AuthenticationComponent.class);
        LOCK_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("lockService", LockService.class);

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
    @Test
    public void listSitesIncludingSubTypesOfSite() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
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
    @Test
    public void anySiteManagerShouldBeAbleToDeleteASite() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // We already have a site with 4 users created by the TestSiteAndMemberInfo rule above.
                // We'll use the testUser from the rule above - oh how I wish JUnit supported @Test-level rules, so I didn't have to create that user for all tests.
                SITE_SERVICE.setMembership(TEST_SITE_WITH_MEMBERS.siteInfo.getShortName(), testUser.getUsername(), SiteModel.SITE_MANAGER);

                // Now switch to run as that user and try to delete the site.
                AuthenticationUtil.runAs(new RunAsWork<Void>() {
                    @Override
                    public Void doWork() throws Exception
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
     * This test ensures that site deleted before MNT-10109 fix (i.e. deleted with their associated authorities) could be restored with site-groups recreation
     * 
     * @throws Exception
     */
    @Test
    public void deleteSiteDeleteAuthoritiesAndRestoreEnsuringSiteGroupsWasRecreated() throws Exception
    {
        final String siteShortName = "testsite-" + System.currentTimeMillis();
        final SiteServiceImpl siteServiceImpl = (SiteServiceImpl) SITE_SERVICE;
        log.debug("Creating test site called: " + siteShortName);

        // Create site
        final TestSiteAndMemberInfo testSiteAndMemberInfo = perMethodTestSites.createTestSiteWithUserPerRole(siteShortName, "sitePreset", SiteVisibility.PUBLIC, AuthenticationUtil.getAdminUserName());

        // Delete permissions and site
        final Map<String, String> membersBefore = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Map<String, String>>() {
            public Map<String, String> execute() throws Throwable
            {
                NodeRef siteNodeRef = testSiteAndMemberInfo.siteInfo.getNodeRef();

                Map<String, String> membersBefore = SITE_SERVICE.listMembers(siteShortName, null, null, 0, true);
                log.debug(membersBefore.size() + " members...");
                for (Map.Entry<String, String> entry : membersBefore.entrySet())
                {
                    log.debug(entry);
                }

                Map<String, Set<String>> groupsMemberships = new HashMap<String, Set<String>>();

                log.debug("About to delete site-related groups.");
                // delete authorities
                Set<String> permissions = PERMISSION_SERVICE.getSettablePermissions(SiteModel.TYPE_SITE);
                for (String permission : permissions)
                {
                    String prefixSiteRoleGroup = siteServiceImpl.getSiteRoleGroup(siteShortName, permission, true);

                    Set<String> groupUsers = AUTHORITY_SERVICE.getContainedAuthorities(null, prefixSiteRoleGroup, true);
                    groupsMemberships.put(prefixSiteRoleGroup, groupUsers);

                    AUTHORITY_SERVICE.deleteAuthority(prefixSiteRoleGroup);
                }
                // emulate onDelete site behavior before MNT-10109 fix
                NODE_SERVICE.setProperty(siteNodeRef, QName.createQName(null, "memberships"), (Serializable) groupsMemberships);
                log.debug("Site-related groups deleted.");

                log.debug("About to delete site.");
                SITE_SERVICE.deleteSite(siteShortName);
                log.debug("Site deleted.");

                return membersBefore;
            }
        });

        // restore the site
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                assertThatArchivedNodeExists(testSiteAndMemberInfo.siteInfo.getNodeRef(), "Site node not found in archive.");

                // ensure there are no authorities
                Set<String> permissions = PERMISSION_SERVICE.getSettablePermissions(SiteModel.TYPE_SITE);
                for (String permission : permissions)
                {
                    String permissionGroupShortName = siteServiceImpl.getSiteRoleGroup(siteShortName, permission, false);
                    String authorityName = AUTHORITY_SERVICE.getName(AuthorityType.GROUP, permissionGroupShortName);

                    assertTrue("Authotiry should not exist : " + authorityName, !AUTHORITY_SERVICE.authorityExists(authorityName));
                }

                log.debug("About to restore site node from archive");

                final NodeRef archivedSiteNode = NODE_ARCHIVE_SERVICE.getArchivedNode(testSiteAndMemberInfo.siteInfo.getNodeRef());
                RestoreNodeReport report = NODE_ARCHIVE_SERVICE.restoreArchivedNode(archivedSiteNode);
                // ...which should work
                assertEquals("Failed to restore site from archive", RestoreStatus.SUCCESS, report.getStatus());

                log.debug("Successfully restored site from arhive.");

                return null;
            }
        });

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Map<String, String>>() {
            public Map<String, String> execute() throws Throwable
            {
                // The site itself should have been restored, of course...
                assertTrue("The site noderef was not restored as expected", NODE_SERVICE.exists(testSiteAndMemberInfo.siteInfo.getNodeRef()));

                Map<String, String> members = SITE_SERVICE.listMembers(siteShortName, null, null, 0, true);
                assertEquals("Not all member have been restored", membersBefore.size(), members.size());
                log.debug(members.size() + " members...");
                for (Map.Entry<String, String> entry : SITE_SERVICE.listMembers(siteShortName, null, null, 0, true).entrySet())
                {
                    log.debug(entry);
                }

                // Group authority nodes should be restored or recreated
                for (String role : SITE_SERVICE.getSiteRoles())
                {
                    final String siteGroup = SITE_SERVICE.getSiteRoleGroup(siteShortName, role);
                    assertTrue("Site group for role " + role + " did not exist after site restoration",
                            AUTHORITY_SERVICE.authorityExists(siteGroup));
                }

                Set<String> currentManagers = AUTHORITY_SERVICE.getContainedAuthorities(AuthorityType.USER, siteServiceImpl.getSiteRoleGroup(siteShortName, SiteModel.SITE_MANAGER, true), false);
                // ensure that there is at least one site manager
                log.debug("Current Managers " + currentManagers);
                assertTrue("There should be at least one site manager", !currentManagers.isEmpty());

                return null;
            }
        });

        // remove site completely
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                log.debug("About to delete site completely.");
                SITE_SERVICE.deleteSite(siteShortName);
                log.debug("About to purge site from trashcan.");

                // get archive node reference
                String storePath = "archive://SpacesStore";
                StoreRef storeRef = new StoreRef(storePath);
                NodeRef archivedNodeRef = new NodeRef(storeRef, testSiteAndMemberInfo.siteInfo.getNodeRef().getId());
                NODE_ARCHIVE_SERVICE.purgeArchivedNode(archivedNodeRef);

                return null;
            }
        });
    }

    private void createUser(String userName, String nameSuffix)
    {
        if (AUTHENTICATION_SERVICE.authenticationExists(userName))
        {
            return;
        }
        AUTHENTICATION_SERVICE.createAuthentication(userName, "PWD".toCharArray());

        PropertyMap ppOne = new PropertyMap(4);
        ppOne.put(ContentModel.PROP_USERNAME, userName);
        ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName" + nameSuffix);
        ppOne.put(ContentModel.PROP_LASTNAME, "lastName" + nameSuffix);
        ppOne.put(ContentModel.PROP_EMAIL, "email" + nameSuffix + "@email.com");
        ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

        PERSON_SERVICE.createPerson(ppOne);
    }

    /**
     * Added as part of MNT-14671 : Site with document locked for Edit Offline cannot be deleted. This test checks that the owner of a site can delete the site even if there are locked files, that belong to the owner or to other members of that site;
     * 
     * This test also checks that after restore the locks are restored correctly for all the locked files;
     * 
     * MNT-15855: Checks the case when there is a working copy (simulate lock for offline edit)
     * 
     * @throws Exception
     */
    @Test
    public void deleteSiteDeleteAuthoritiesAndRestoreEnsuringLocksAreRestored() throws Exception
    {
        final String userOwner = "UserOwner";
        final String userCollaborator = "UserColaborator";
        final String userPrefix = "dart";// delete and restore test

        // create the users
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                createUser(userOwner, userPrefix);
                createUser(userCollaborator, userPrefix);
                return null;
            }
        });

        final String siteShortName = "testsite-" + System.currentTimeMillis();
        log.debug("Creating test site called: " + siteShortName);

        // Create site
        final TestSiteAndMemberInfo testSiteAndMemberInfo = perMethodTestSites.createTestSiteWithUserPerRole(
                siteShortName,
                "sitePreset",
                SiteVisibility.PUBLIC,
                userOwner);

        // create some documents into the test site as the owner of the site
        AUTHENTICATION_COMPONENT.setCurrentUser(userOwner);

        /* [site] {siteShortName} | --- [siteContainer] {componentId} | --- [cm:content] fileFolderPrefix + "file.txt" | |-- [folder] fileFolderPrefix + "folder" | |-- [cm:content] fileFolderPrefix + "fileInFolder.txt" | |-- [folder] fileFolderPrefix + "subfolder" | |-- [cm:content] fileFolderPrefix + "fileInSubfolder.txt" --- [cm:content] fileFolderPrefix + "fileEditOfline.txt" */
        String fileFolderPrefix = "TESTLOCK_";
        String componentId = "doclib";

        NodeRef siteContainer = SITE_SERVICE.createContainer(siteShortName, componentId, ContentModel.TYPE_FOLDER, null);
        final FileInfo fileInfo = FILE_FOLDER_SERVICE.create(
                siteContainer,
                fileFolderPrefix + "file.txt",
                ContentModel.TYPE_CONTENT);
        ContentWriter writer = FILE_FOLDER_SERVICE.getWriter(fileInfo.getNodeRef());
        writer.putContent("Just some old content that doesn't mean anything");

        FileInfo folder1Info = FILE_FOLDER_SERVICE.create(
                siteContainer,
                fileFolderPrefix + "folder",
                ContentModel.TYPE_FOLDER);

        FileInfo fileInfo2 = FILE_FOLDER_SERVICE.create(
                folder1Info.getNodeRef(),
                fileFolderPrefix + "fileInFolder.txt",
                ContentModel.TYPE_CONTENT);
        ContentWriter writer2 = FILE_FOLDER_SERVICE.getWriter(fileInfo2.getNodeRef());
        writer2.putContent("Just some old content that doesn't mean anything");

        FileInfo folder2Info = FILE_FOLDER_SERVICE.create(
                folder1Info.getNodeRef(),
                fileFolderPrefix + "subfolder",
                ContentModel.TYPE_FOLDER);

        FileInfo fileInfo3 = FILE_FOLDER_SERVICE.create(
                folder2Info.getNodeRef(),
                fileFolderPrefix + "fileInSubfolder.txt",
                ContentModel.TYPE_CONTENT);
        ContentWriter writer3 = FILE_FOLDER_SERVICE.getWriter(fileInfo3.getNodeRef());
        writer3.putContent("Just some old content that doesn't mean anything");

        // Make sure there are no locks on the fileInfo yet
        assertEquals(LockStatus.NO_LOCK, LOCK_SERVICE.getLockStatus(fileInfo.getNodeRef()));

        // Lock a file as userOwner
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                LOCK_SERVICE.lock(fileInfo.getNodeRef(), LockType.WRITE_LOCK);
                return null;
            }
        });

        // Make sure we have a lock now
        assertEquals(LockStatus.LOCK_OWNER, LOCK_SERVICE.getLockStatus(fileInfo.getNodeRef()));

        checkThatNonMembersCanNotCreateFiles(userCollaborator, fileFolderPrefix, folder2Info);

        // Make sure we are running as userOwner
        AUTHENTICATION_COMPONENT.setCurrentUser(userOwner);

        // Make userCollaborator a member of the site
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Object>() {
            public Object execute() throws Throwable
            {
                SITE_SERVICE.setMembership(siteShortName, userCollaborator, SiteModel.SITE_COLLABORATOR);
                return null;
            }
        });

        // Now, as userCollaborator create a file and lock it
        AUTHENTICATION_COMPONENT.setCurrentUser(userCollaborator);

        final FileInfo fileInfoForCollaboratorUser = FILE_FOLDER_SERVICE.create(
                folder2Info.getNodeRef(),
                fileFolderPrefix + "userCollaborator.txt",
                ContentModel.TYPE_CONTENT);
        ContentWriter writer4 = FILE_FOLDER_SERVICE.getWriter(fileInfoForCollaboratorUser.getNodeRef());
        writer4.putContent("Just some old content that doesn't mean anything");

        // Check that the node is not currently locked
        assertEquals(LockStatus.NO_LOCK, LOCK_SERVICE.getLockStatus(fileInfoForCollaboratorUser.getNodeRef()));

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // lock the file as userCollaborator
                LOCK_SERVICE.lock(fileInfoForCollaboratorUser.getNodeRef(), LockType.WRITE_LOCK);
                return null;
            }
        });

        // Test valid lock
        assertEquals(LockStatus.LOCK_OWNER, LOCK_SERVICE.getLockStatus(fileInfoForCollaboratorUser.getNodeRef()));

        // Create a file to test Edit offline
        final FileInfo fileEditOffline = FILE_FOLDER_SERVICE.create(
                siteContainer,
                fileFolderPrefix + "fileEditOfline.txt",
                ContentModel.TYPE_CONTENT);
        ContentWriter writerEO = FILE_FOLDER_SERVICE.getWriter(fileEditOffline.getNodeRef());
        writerEO.putContent("Just some old content that doesn't mean anything");

        // Make sure there are no locks on the fileEditOffline yet
        assertEquals(LockStatus.NO_LOCK, LOCK_SERVICE.getLockStatus(fileEditOffline.getNodeRef()));

        // Check out the document - simulate Edit offline
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                NodeRef workingCopy = COCI_SERVICE.checkout(fileEditOffline.getNodeRef());
                assertNotNull(workingCopy);
                return null;
            }
        });

        // Make sure we have a lock now on fileEditOffline
        assertEquals(LockStatus.LOCK_OWNER, LOCK_SERVICE.getLockStatus(fileEditOffline.getNodeRef()));

        // Switch back to userOwner so we can call delete the site
        AUTHENTICATION_COMPONENT.setCurrentUser(userOwner);

        // Delete the site
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                log.debug("About to delete site.");
                AUTHENTICATION_COMPONENT.getCurrentUserName();
                SITE_SERVICE.deleteSite(siteShortName);
                log.debug("Site deleted.");

                return null;
            }
        });

        // Restore the site
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                assertThatArchivedNodeExists(testSiteAndMemberInfo.siteInfo.getNodeRef(), "Site node not found in archive.");

                log.debug("About to restore site node from archive");

                final NodeRef archivedSiteNode = NODE_ARCHIVE_SERVICE.getArchivedNode(testSiteAndMemberInfo.siteInfo.getNodeRef());
                RestoreNodeReport report = NODE_ARCHIVE_SERVICE.restoreArchivedNode(archivedSiteNode);
                // ...which should work
                assertEquals("Failed to restore site from archive", RestoreStatus.SUCCESS, report.getStatus());

                log.debug("Successfully restored site from arhive.");

                return null;
            }
        });

        // Check that the files have been restored and all the locks are present
        AUTHENTICATION_COMPONENT.setCurrentUser(userCollaborator);
        assertEquals(LockStatus.LOCK_OWNER, LOCK_SERVICE.getLockStatus(fileInfoForCollaboratorUser.getNodeRef()));
        // Check that the file for edit offline has been restored and has the expected lock owner
        assertEquals(LockStatus.LOCK_OWNER, LOCK_SERVICE.getLockStatus(fileEditOffline.getNodeRef()));

        AUTHENTICATION_COMPONENT.setCurrentUser(userOwner);
        assertEquals(LockStatus.LOCK_OWNER, LOCK_SERVICE.getLockStatus(fileInfo.getNodeRef()));
        // Check that the file for edit offline has been restored and is locked
        assertEquals(LockStatus.LOCKED, LOCK_SERVICE.getLockStatus(fileEditOffline.getNodeRef()));

        // Remove site completely
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // if we apply the sys:temporary aspect to the site, the NodeService will not
                // archive it i.e. this bit of clean up will be a bit faster.
                NODE_SERVICE.addAspect(testSiteAndMemberInfo.siteInfo.getNodeRef(), ContentModel.ASPECT_TEMPORARY, null);

                log.debug("About to delete site completely.");
                SITE_SERVICE.deleteSite(siteShortName);
                log.debug("About to purge site from trashcan.");

                // get archive node reference
                String storePath = "archive://SpacesStore";
                StoreRef storeRef = new StoreRef(storePath);
                NodeRef archivedNodeRef = new NodeRef(storeRef, testSiteAndMemberInfo.siteInfo.getNodeRef().getId());
                NODE_ARCHIVE_SERVICE.purgeArchivedNode(archivedNodeRef);

                return null;
            }
        });
    }

    private void checkThatNonMembersCanNotCreateFiles(final String userCollaborator, String fileFolderPrefix, FileInfo folder2Info)
    {
        // now, as another user, that is not yet a member, try to create a file in this site
        // this use case is not really relevant to test method, but it is a good test for permissions
        AUTHENTICATION_COMPONENT.setCurrentUser(userCollaborator);
        try
        {
            FileInfo fileInfoForTestNewFileAsAnotherUser = FILE_FOLDER_SERVICE.create(
                    folder2Info.getNodeRef(),
                    fileFolderPrefix + "user2.txt",
                    ContentModel.TYPE_CONTENT);
            ContentWriter writer3_user2 = FILE_FOLDER_SERVICE.getWriter(fileInfoForTestNewFileAsAnotherUser.getNodeRef());
            writer3_user2.putContent("Just some old content that doesn't mean anything");

            fail("We should not reach this point. the user that tries to run this code, add the file, is not yet a member of the site");
        }
        catch (AccessDeniedException e)
        {
            // Expected
        }
    }

    /**
     * This test ensures that when sites are deleted (moved to the trashcan) and then restored, that the 4 role-based groups are restored correctly and that any users who were members of those groups are made members once more.
     * 
     * @see SiteServiceImpl#deleteSite(String)
     * @see SiteServiceImpl#onRestoreNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Test
    public void deleteSiteAndRestoreEnsuringSiteGroupsAreRecovered() throws Exception
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
        final TestData testData = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<TestData>() {
            public TestData execute() throws Throwable
            {
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_NAME, "testcontent");
                properties.put(ContentModel.PROP_DESCRIPTION, "content - test doc for test");
                ChildAssociationRef testDoc = NODE_SERVICE.createNode(testSiteAndMemberInfo.doclib, ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testcontent"),
                        ContentModel.TYPE_CONTENT, properties);
                NodeRef testDocNodeRef = testDoc.getChildRef();

                // change all groups to have the permissions from a contributor
                PERMISSION_SERVICE.deletePermissions(testDocNodeRef);
                PERMISSION_SERVICE.setInheritParentPermissions(testDocNodeRef, false);
                assertTrue("Permissions should be cleared", PERMISSION_SERVICE.getAllSetPermissions(testDocNodeRef).isEmpty());

                Set<String> permissions = PERMISSION_SERVICE.getSettablePermissions(SiteModel.TYPE_SITE);
                for (String permission : permissions)
                {
                    String siteRoleGroup = SITE_SERVICE.getSiteRoleGroup(siteShortName, permission);
                    PERMISSION_SERVICE.setPermission(testDocNodeRef, siteRoleGroup, "SiteContributor", true);
                }

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

                return new TestData(userNameToRoleMap, testDocNodeRef);
            }
        });

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
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

        final Map<String, String> associatedGroups = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Map<String, String>>() {
            public Map<String, String> execute() throws Throwable
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
                for (Map.Entry<String, String> entry : SITE_SERVICE.listMembers(siteShortName, null, null, 0, true).entrySet())
                {
                    log.debug(entry);
                }

                // And finally, the original members of the site should have been given the same membership that they had before.
                for (Map.Entry<String, String> entry : testData.userNameToRoleMap.entrySet())
                {
                    assertEquals("Unexpected role for site user: " + entry.getKey(),
                            entry.getValue(),
                            SITE_SERVICE.getMembersRole(siteShortName, entry.getKey()));
                }

                // When the site is restored custom permissions on contents should be also restored
                Set<AccessPermission> accessPermissions = PERMISSION_SERVICE.getAllSetPermissions(testData.testDocNodeRef);
                Map<String, String> associatedGroups = new HashMap<String, String>();
                for (AccessPermission access : accessPermissions)
                {
                    associatedGroups.put(access.getAuthority(), access.getPermission());
                }
                Set<String> permissions = PERMISSION_SERVICE.getSettablePermissions(SiteModel.TYPE_SITE);
                for (String permission : permissions)
                {
                    String siteRoleGroup = SITE_SERVICE.getSiteRoleGroup(siteShortName, permission);
                    assertTrue("all groups should have the permissions from a contributor on test content", "SiteContributor".equals(associatedGroups.get(siteRoleGroup)));
                }

                return associatedGroups;
            }
        });

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                log.debug("About to delete site completely.");
                SITE_SERVICE.deleteSite(siteShortName);
                for (String authority : associatedGroups.keySet())
                {
                    assertTrue("Associated groups should remain after site delete", AUTHORITY_SERVICE.authorityExists(authority));
                }

                return null;
            }
        });

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                log.debug("About to purge site from trashcan.");

                // get archive node reference
                String storePath = "archive://SpacesStore";
                StoreRef storeRef = new StoreRef(storePath);
                NodeRef archivedNodeRef = new NodeRef(storeRef, testSiteAndMemberInfo.siteInfo.getNodeRef().getId());
                NODE_ARCHIVE_SERVICE.purgeArchivedNode(archivedNodeRef);
                for (String authority : associatedGroups.keySet())
                {
                    assertTrue("Associated groups should be deleted on site purge", !AUTHORITY_SERVICE.authorityExists(authority));
                }

                return null;
            }
        });

    }

    @Test
    public void testSiteMembersPaged()
    {
        // Choose a site name that will link back to this test case...
        final String siteShortName = testName.getMethodName();
        log.debug("Creating test site called: " + siteShortName);

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // ...and create the site
                final TestSiteAndMemberInfo testSiteAndMemberInfo = perMethodTestSites.createTestSiteWithUserPerRole(siteShortName, "sitePreset", SiteVisibility.PUBLIC, AuthenticationUtil.getAdminUserName());

                List<Pair<SiteService.SortFields, Boolean>> sortProps = new ArrayList<Pair<SiteService.SortFields, Boolean>>(1);
                sortProps.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.FirstName, true));
                PagingResults<SiteMembership> pagedMembers = SITE_SERVICE.listMembersPaged(siteShortName, true, sortProps, new PagingRequest(100));
                assertNotNull(pagedMembers);
                assertNotNull(pagedMembers.getPage());
                assertNotNull(pagedMembers.getQueryExecutionId());
                assertFalse(pagedMembers.hasMoreItems());
                log.debug("About to delete site completely.");
                SITE_SERVICE.deleteSite(siteShortName);
                return null;
            }
        });
    }

    @Test
    public void testSiteGroupsPaged()
    {
        // Choose a site name that will link back to this test case...
        final String siteShortName = testName.getMethodName();
        log.debug("Creating test site called: " + siteShortName);

        TRANSACTION_HELPER.doInTransaction(() -> {
            perMethodTestSites.createTestSiteWithGroups(siteShortName, "sitePreset", SiteVisibility.PUBLIC, AuthenticationUtil.getAdminUserName(), 10);
            List<Pair<SiteService.SortFields, Boolean>> sortProps = new ArrayList<Pair<SiteService.SortFields, Boolean>>(1);
            sortProps.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.DisplayName, true));
            PagingResults<SiteGroupMembership> pagedMembers = SITE_SERVICE.listGroupMembersPaged(siteShortName, sortProps, new PagingRequest(5));
            assertNotNull(pagedMembers);
            assertNotNull(pagedMembers.getQueryExecutionId());
            assertTrue(pagedMembers.hasMoreItems());
            assertEquals(pagedMembers.getPage().size(), 5);
            log.debug("About to delete site completely.");
            SITE_SERVICE.deleteSite(siteShortName);
            return null;
        });
    }

    @Test
    public void testSiteMembersPagedV2()
    {
        // Choose a site name that will link back to this test case...
        final String siteShortName = testName.getMethodName();
        log.debug("Creating test site called: " + siteShortName);

        TRANSACTION_HELPER.doInTransaction(() -> {
            perMethodTestSites.createTestSiteWithGroups(siteShortName, "sitePreset", SiteVisibility.PUBLIC, AuthenticationUtil.getAdminUserName(), 10);
            List<Pair<SiteService.SortFields, Boolean>> sortProps = new ArrayList<Pair<SiteService.SortFields, Boolean>>(1);
            sortProps.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.FirstName, true));
            PagingResults<SiteMembership> pagedMembers = SITE_SERVICE.listMembersPaged(siteShortName, true, sortProps, new PagingRequest(25));
            assertNotNull(pagedMembers);
            assertNotNull(pagedMembers.getQueryExecutionId());
            assertFalse(pagedMembers.hasMoreItems());
            assertEquals(pagedMembers.getPage().size(), 11);

            List<SiteMembership> users = pagedMembers.getPage().stream().filter((member) -> !member.isMemberOfGroup()).collect(Collectors.toList());
            List<SiteMembership> groupsUsers = pagedMembers.getPage().stream().filter(SiteMembership::isMemberOfGroup).collect(Collectors.toList());
            assertEquals(users.size(), 1);
            assertEquals(groupsUsers.size(), 10);

            pagedMembers = SITE_SERVICE.listMembersPaged(siteShortName, false, sortProps, new PagingRequest(100));
            assertNotNull(pagedMembers);
            assertNotNull(pagedMembers.getQueryExecutionId());
            assertFalse(pagedMembers.hasMoreItems());
            assertEquals(pagedMembers.getPage().size(), 1);

            users = pagedMembers.getPage().stream().filter((member) -> !member.isMemberOfGroup()).collect(Collectors.toList());
            groupsUsers = pagedMembers.getPage().stream().filter(SiteMembership::isMemberOfGroup).collect(Collectors.toList());
            assertEquals(users.size(), 1);
            assertEquals(groupsUsers.size(), 0);

            log.debug("About to delete site completely.");
            SITE_SERVICE.deleteSite(siteShortName);
            return null;
        });
    }

    @Test
    public void testTokenizer()
    {
        String[] res = SiteServiceImpl.tokenizeFilterLowercase("Fred");
        assertNotNull(res);
        assertEquals(1, res.length);
        assertEquals("fred", res[0]);

        res = SiteServiceImpl.tokenizeFilterLowercase("king kong lives");
        assertNotNull(res);
        assertEquals(3, res.length);
        assertEquals("king", res[0]);
        assertEquals("kong", res[1]);
        assertEquals("lives", res[2]);

        res = SiteServiceImpl.tokenizeFilterLowercase("KING Kong livES");
        assertNotNull(res);
        assertEquals(3, res.length);
        assertEquals("king", res[0]);
        assertEquals("kong", res[1]);
        assertEquals("lives", res[2]);

        res = SiteServiceImpl.tokenizeFilterLowercase(null);
        assertNotNull(res);
        assertEquals(0, res.length);
    }

    /**
     * MNT-16043: Site Owner and Site Manager can delete working copy, Site Collaborator cannot
     *
     * @throws Exception
     */
    @Test
    public void testSiteRolesPermissionsToDeleteWorkingCopy() throws Exception
    {
        final String userSiteOwner = "UserSiteOwner";
        final String userSiteManager = "UserSiteManager";
        final String userSiteCollaborator = "UserSiteCollaborator";
        final String userPrefix = "delete-working-copy-file";

        // create the users
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                createUser(userSiteOwner, userPrefix);
                createUser(userSiteManager, userPrefix);
                createUser(userSiteCollaborator, userPrefix);
                return null;
            }
        });
        final String siteShortName = userPrefix + "Site" + System.currentTimeMillis();
        final String dummyContent = "Just some old content that doesn't mean anything";

        // Create site
        final TestSiteAndMemberInfo testSiteAndMemberInfo = perMethodTestSites.createTestSiteWithUserPerRole(siteShortName, "sitePreset",
                SiteVisibility.PUBLIC, userSiteOwner);

        // create 1 file into the site as the owner of the site
        AUTHENTICATION_COMPONENT.setCurrentUser(userSiteOwner);

        NodeRef siteContainer = SITE_SERVICE.createContainer(siteShortName, "doclib", ContentModel.TYPE_FOLDER, null);
        final FileInfo fileInfo1 = FILE_FOLDER_SERVICE.create(siteContainer, "fileInfo1.txt", ContentModel.TYPE_CONTENT);
        ContentWriter writer1 = FILE_FOLDER_SERVICE.getWriter(fileInfo1.getNodeRef());
        writer1.putContent(dummyContent);

        final FileInfo fileInfo2 = FILE_FOLDER_SERVICE.create(siteContainer, "fileInfo2.txt", ContentModel.TYPE_CONTENT);
        ContentWriter writer2 = FILE_FOLDER_SERVICE.getWriter(fileInfo2.getNodeRef());
        writer2.putContent(dummyContent);

        final FileInfo fileInfo3 = FILE_FOLDER_SERVICE.create(siteContainer, "fileInfo3.txt", ContentModel.TYPE_CONTENT);
        ContentWriter writer3 = FILE_FOLDER_SERVICE.getWriter(fileInfo2.getNodeRef());
        writer3.putContent(dummyContent);

        // Site COLLABORATOR - cannot delete working copy or original file
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // checkout file again as userOwner
                AUTHENTICATION_COMPONENT.setCurrentUser(userSiteOwner);
                NodeRef workingCopy = COCI_SERVICE.checkout(fileInfo3.getNodeRef());
                assertNotNull(workingCopy);

                // make userSiteCollaborator a member of the site
                SITE_SERVICE.setMembership(siteShortName, userSiteCollaborator, SiteModel.SITE_COLLABORATOR);

                // make sure we are running as userSiteCollaborator
                AUTHENTICATION_COMPONENT.setCurrentUser(userSiteCollaborator);

                // try to delete working copy file
                try
                {
                    NODE_SERVICE.deleteNode(workingCopy);
                    fail("You do not have the appropriate permissions to perform this operation");
                }
                catch (AccessDeniedException ex)
                {
                    // do nothing - is expected
                }

                // try to delete original checked-out
                try
                {
                    NODE_SERVICE.deleteNode(fileInfo3.getNodeRef());
                    fail("You do not have the appropriate permissions to perform this operation");
                }
                catch (AccessDeniedException ex)
                {
                    // do nothing - is expected
                }

                return null;
            }
        });

        // Site Owner - can delete working copy (or original)
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // checkout a file as userOwner
                AUTHENTICATION_COMPONENT.setCurrentUser(userSiteOwner);
                NodeRef workingCopy = COCI_SERVICE.checkout(fileInfo2.getNodeRef());
                assertNotNull(workingCopy);

                NODE_SERVICE.deleteNode(workingCopy);

                // checkout file again as userOwner
                workingCopy = COCI_SERVICE.checkout(fileInfo2.getNodeRef());
                assertNotNull(workingCopy);

                NODE_SERVICE.deleteNode(fileInfo2.getNodeRef());

                return null;
            }
        });

        // Site Manager - can delete working copy (or original)
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // checkout a file as userOwner
                AUTHENTICATION_COMPONENT.setCurrentUser(userSiteOwner);
                NodeRef workingCopy = COCI_SERVICE.checkout(fileInfo1.getNodeRef());
                assertNotNull(workingCopy);

                // make userSiteManager a member of the site
                SITE_SERVICE.setMembership(siteShortName, userSiteManager, SiteModel.SITE_MANAGER);

                // make sure we are running as userSiteManager
                AUTHENTICATION_COMPONENT.setCurrentUser(userSiteManager);

                NODE_SERVICE.deleteNode(workingCopy);

                // checkout file again as userOwner
                AUTHENTICATION_COMPONENT.setCurrentUser(userSiteOwner);
                workingCopy = COCI_SERVICE.checkout(fileInfo1.getNodeRef());
                assertNotNull(workingCopy);

                // make sure we are running as userSiteManager
                AUTHENTICATION_COMPONENT.setCurrentUser(userSiteManager);

                NODE_SERVICE.deleteNode(fileInfo1.getNodeRef());

                return null;
            }
        });

        AUTHENTICATION_COMPONENT.setCurrentUser(userSiteOwner);

        // Delete the site
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                AUTHENTICATION_COMPONENT.getCurrentUserName();
                SITE_SERVICE.deleteSite(siteShortName);

                return null;
            }
        });
    }

    /**
     * REPO-1688: Restore Site should disable lockable behaviour (to be symmetric with delete/purge). See also MNT-17093 for background on this.
     *
     * @throws Exception
     */
    @Test
    public void deleteSiteRestoreSiteWithLocks() throws Exception
    {
        final String userSiteOwner = "UserSiteOwner";
        final String userSiteManager = "UserSiteManager";
        final String userSiteCollaborator = "UserSiteCollaborator";
        final String userPrefix = "restore-with-lock";

        // create the users
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                createUser(userSiteOwner, userPrefix);
                createUser(userSiteManager, userPrefix);
                createUser(userSiteCollaborator, userPrefix);
                return null;
            }
        });

        final String siteShortName = "testsite-" + System.currentTimeMillis();
        final SiteServiceImpl siteServiceImpl = (SiteServiceImpl) SITE_SERVICE;
        log.debug("Creating test site called: " + siteShortName);

        // Create site
        final TestSiteAndMemberInfo testSiteAndMemberInfo = perMethodTestSites.createTestSiteWithUserPerRole(siteShortName, "sitePreset", SiteVisibility.PUBLIC, userSiteOwner);

        // Add Site COLLABORATOR - create file
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // make userSiteCollaborator a member of the site
                SITE_SERVICE.setMembership(siteShortName, userSiteCollaborator, SiteModel.SITE_COLLABORATOR);

                return null;
            }
        });

        // create document as userSiteCollaborator
        AUTHENTICATION_COMPONENT.setCurrentUser(userSiteCollaborator);

        String fileFolderPrefix = "TESTLOCK_";
        String componentId = "doclib";

        NodeRef siteContainer = SITE_SERVICE.createContainer(siteShortName, componentId, ContentModel.TYPE_FOLDER, null);
        final FileInfo fileInfo = FILE_FOLDER_SERVICE.create(
                siteContainer,
                fileFolderPrefix + "file.txt",
                ContentModel.TYPE_CONTENT);
        ContentWriter writer = FILE_FOLDER_SERVICE.getWriter(fileInfo.getNodeRef());
        writer.putContent("Just some old content that doesn't mean anything");

        // Make sure there are no locks on the fileInfo yet
        assertEquals(LockStatus.NO_LOCK, LOCK_SERVICE.getLockStatus(fileInfo.getNodeRef()));

        // Lock a file as userSiteCollaborator
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                LOCK_SERVICE.lock(fileInfo.getNodeRef(), LockType.READ_ONLY_LOCK);
                return null;
            }
        });

        // Make sure we have a lock now
        assertEquals(LockStatus.LOCK_OWNER, LOCK_SERVICE.getLockStatus(fileInfo.getNodeRef()));

        AUTHENTICATION_COMPONENT.setCurrentUser(userSiteOwner);

        // Delete site
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Map<String, String>>() {
            public Map<String, String> execute() throws Throwable
            {
                log.debug("About to delete site.");
                SITE_SERVICE.deleteSite(siteShortName);
                log.debug("Site deleted.");

                return null;
            }
        });

        // Add custom behaviour that triggers onCreateNode to cause update ... and hence triggers REPO-1688

        PolicyComponent policyComponent = (PolicyComponent) APP_CONTEXT_INIT.getApplicationContext().getBean("policyComponent");
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onCreateNodeSetTitle"));

        // restore the site
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                assertThatArchivedNodeExists(testSiteAndMemberInfo.siteInfo.getNodeRef(), "Site node not found in archive.");

                log.debug("About to restore site node from archive");

                final NodeRef archivedSiteNode = NODE_ARCHIVE_SERVICE.getArchivedNode(testSiteAndMemberInfo.siteInfo.getNodeRef());
                RestoreNodeReport report = NODE_ARCHIVE_SERVICE.restoreArchivedNode(archivedSiteNode);
                // ...which should work
                assertEquals("Failed to restore site from archive", RestoreStatus.SUCCESS, report.getStatus());

                log.debug("Successfully restored site from arhive.");

                return null;
            }
        });

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Map<String, String>>() {
            public Map<String, String> execute() throws Throwable
            {
                // The site itself should have been restored, of course...
                assertTrue("The site noderef was not restored as expected", NODE_SERVICE.exists(testSiteAndMemberInfo.siteInfo.getNodeRef()));

                return null;
            }
        });

        // remove site completely
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                log.debug("About to delete site completely.");
                SITE_SERVICE.deleteSite(siteShortName);
                log.debug("About to purge site from trashcan.");

                // get archive node reference
                String storePath = "archive://SpacesStore";
                StoreRef storeRef = new StoreRef(storePath);
                NodeRef archivedNodeRef = new NodeRef(storeRef, testSiteAndMemberInfo.siteInfo.getNodeRef().getId());
                NODE_ARCHIVE_SERVICE.purgeArchivedNode(archivedNodeRef);

                return null;
            }
        });
    }

    public void onCreateNodeSetTitle(ChildAssociationRef childAssocRef)
    {
        NodeRef newRef = childAssocRef.getChildRef();
        NODE_SERVICE.setProperty(newRef, ContentModel.PROP_TITLE, "Testing REPO-1688");
    }

    private void assertThatArchivedNodeExists(NodeRef originalNodeRef, String failureMsg)
    {
        final NodeRef archivedNodeRef = NODE_ARCHIVE_SERVICE.getArchivedNode(originalNodeRef);
        assertTrue(failureMsg, NODE_SERVICE.exists(archivedNodeRef));
    }

    public static class TestData
    {
        public final Map<String, String> userNameToRoleMap;
        public final NodeRef testDocNodeRef;

        public TestData(Map<String, String> userNameToRoleMap, NodeRef testDocNodeRef)
        {
            this.userNameToRoleMap = userNameToRoleMap;
            this.testDocNodeRef = testDocNodeRef;
        }
    }

    /**
     * REPO-2811 / ALF-21924: "Internal Server Error" when listSites is invoked for users belonging to a deleted Site.
     *
     * @throws Exception
     */
    @Test
    public void testListMembershipOnSitesDifferentCase() throws Exception
    {
        final String userSiteOwner = "UserSiteOwner";
        final String userSiteCollaborator = "UserSiteCollaborator";
        final String userPrefix = "test";

        // Create the users
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                createUser(userSiteOwner, userPrefix);
                createUser(userSiteCollaborator, userPrefix);
                return null;
            }
        });

        // Create sites to add user to
        final String id = Long.toString(System.currentTimeMillis());
        final String siteShortName = "testsite-" + id;
        final String secondSiteShortName = "testsite2-" + System.currentTimeMillis();

        log.debug("Creating test sites called: " + siteShortName + " " + secondSiteShortName);

        perMethodTestSites.createSite("sitePreset", siteShortName, null, null, SiteVisibility.PUBLIC, userSiteOwner);
        perMethodTestSites.createSite("sitePreset", secondSiteShortName, null, null, SiteVisibility.PUBLIC, userSiteOwner);

        // Add Site COLLABORATOR
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                // make userSiteCollaborator a member of both sites
                SITE_SERVICE.setMembership(siteShortName, userSiteCollaborator, SiteModel.SITE_COLLABORATOR);
                SITE_SERVICE.setMembership(secondSiteShortName, userSiteCollaborator, SiteModel.SITE_COLLABORATOR);

                return null;
            }
        });

        // Delete first site
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Map<String, String>>() {
            public Map<String, String> execute() throws Throwable
            {
                log.debug("About to delete site.");
                SITE_SERVICE.deleteSite(siteShortName);
                log.debug("Site deleted.");

                return null;
            }
        });

        /* Create site with different case than the deleted one. This is possible since the site id validation is case insensitive only for existing sites while sites in trashcan are validated using authorities which are case sensitive */
        final String siteShortName1 = "TesTsite-" + id;
        log.debug("Creating test site called: " + siteShortName1);

        perMethodTestSites.createSite("sitePreset", siteShortName1, null, null, SiteVisibility.PUBLIC, userSiteOwner);

        // Check that user membership can be retrieved and user is not a member of existing site
        List<SiteMembership> members = SITE_SERVICE.listSiteMemberships(userSiteCollaborator, 0);
        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals(userSiteCollaborator, members.get(0).getPersonId());
        assertEquals(SiteModel.SITE_COLLABORATOR, members.get(0).getRole());
        assertEquals(secondSiteShortName, members.get(0).getSiteInfo().getShortName());

        members = SITE_SERVICE.listSiteMemberships(userSiteCollaborator, 50);
        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals(userSiteCollaborator, members.get(0).getPersonId());
        assertEquals(SiteModel.SITE_COLLABORATOR, members.get(0).getRole());
        assertEquals(secondSiteShortName, members.get(0).getSiteInfo().getShortName());
    }
}
