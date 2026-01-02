/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.springframework.dao.ConcurrencyFailureException;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;

/**
 * Records management security service test.
 *
 * @author Roy Wetherall
 */
public class ExtendedSecurityServiceImplTest extends BaseRMTestCase
{
    private NodeRef record;
    private NodeRef recordToo;
    private NodeRef moveRecordCategory;
    private NodeRef moveRecordFolder;

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected void setupTestDataImpl()
    {
        super.setupTestDataImpl();

        record = utils.createRecord(rmFolder, "record.txt");
        recordToo = utils.createRecord(rmFolder, "recordToo.txt");

        moveRecordCategory = filePlanService.createRecordCategory(filePlan, "moveRecordCategory");
        moveRecordFolder = recordFolderService.createRecordFolder(moveRecordCategory, "moveRecordFolder");
    }

    private String createTestUser()
    {
        return doTestInTransaction(new Test<String>() {
            public String run()
            {
                String userName = GUID.generate();
                createPerson(userName);
                return userName;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    public void testExtendedSecurity()
    {
        final String monkey = createTestUser();
        final String elephant = createTestUser();
        final String snake = createTestUser();

        doTestInTransaction(new Test<Void>() {
            public Void run()
            {
                assertFalse(extendedSecurityService.hasExtendedSecurity(filePlan));
                assertFalse(extendedSecurityService.hasExtendedSecurity(rmContainer));
                assertFalse(extendedSecurityService.hasExtendedSecurity(rmFolder));
                assertFalse(extendedSecurityService.hasExtendedSecurity(record));

                assertTrue(extendedSecurityService.getReaders(record).isEmpty());
                assertTrue(extendedSecurityService.getWriters(record).isEmpty());

                Set<String> extendedReaders = new HashSet<>(2);
                extendedReaders.add(monkey);
                extendedReaders.add(elephant);

                extendedSecurityService.set(record, extendedReaders, null);
                checkExtendedReaders(record, extendedReaders);

                Set<String> extendedReadersToo = new HashSet<>(2);
                extendedReadersToo.add(monkey);
                extendedReadersToo.add(snake);

                extendedSecurityService.set(recordToo, extendedReadersToo, null);
                checkExtendedReaders(recordToo, extendedReadersToo);

                // test remove
                extendedSecurityService.remove(recordToo);

                assertFalse(extendedSecurityService.hasExtendedSecurity(recordToo));
                assertTrue(extendedSecurityService.getReaders(recordToo).isEmpty());
                assertTrue(extendedSecurityService.getWriters(recordToo).isEmpty());

                return null;
            }
        });
    }

    public void testMove()
    {
        final String monkey = createTestUser();
        final String elephant = createTestUser();

        doTestInTransaction(new Test<Void>() {
            Set<String> extendedReaders = new HashSet<>(2);

            public Void run() throws Exception
            {
                extendedReaders.add(monkey);
                extendedReaders.add(elephant);

                assertFalse(extendedSecurityService.hasExtendedSecurity(filePlan));
                assertFalse(extendedSecurityService.hasExtendedSecurity(rmContainer));
                assertFalse(extendedSecurityService.hasExtendedSecurity(rmFolder));
                assertFalse(extendedSecurityService.hasExtendedSecurity(record));
                assertFalse(extendedSecurityService.hasExtendedSecurity(moveRecordCategory));
                assertFalse(extendedSecurityService.hasExtendedSecurity(moveRecordFolder));

                assertTrue(extendedSecurityService.getReaders(record).isEmpty());

                extendedSecurityService.set(record, extendedReaders, null);

                checkExtendedReaders(record, extendedReaders);
                assertFalse(extendedSecurityService.hasExtendedSecurity(moveRecordCategory));
                assertFalse(extendedSecurityService.hasExtendedSecurity(moveRecordFolder));

                fileFolderService.move(record, moveRecordFolder, "movedRecord");

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                checkExtendedReaders(record, extendedReaders);
            }
        });
    }

    /**
     * Check extended readers helper method
     */
    private void checkExtendedReaders(NodeRef nodeRef, Set<String> testReaders)
    {
        assertTrue(extendedSecurityService.hasExtendedSecurity(nodeRef));

        Set<String> readers = extendedSecurityService.getReaders(nodeRef);
        assertNotNull(readers);
        assertEquals(testReaders, readers);
    }

    public void testDifferentUsersDifferentPermissions()
    {
        final String userNone = createTestUser();
        final String userRead = createTestUser();
        final String userWrite = createTestUser();
        final String siteShortName = GUID.generate();

        doTestInTransaction(new Test<Void>() {
            public Void run() throws Exception
            {
                siteService.createSite(null, siteShortName, "test", "test", SiteVisibility.PRIVATE);
                return null;
            }
        });

        final NodeRef documentLibrary = doTestInTransaction(new Test<NodeRef>() {
            public NodeRef run() throws Exception
            {
                siteService.setMembership(siteShortName, userRead, SiteModel.SITE_CONSUMER);
                siteService.setMembership(siteShortName, userWrite, SiteModel.SITE_COLLABORATOR);
                return siteService.createContainer(siteShortName, SiteService.DOCUMENT_LIBRARY, null, null);
            }
        });

        final NodeRef record = doTestInTransaction(new Test<NodeRef>() {
            public NodeRef run() throws Exception
            {
                NodeRef record = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_CONTENT)
                        .getNodeRef();
                recordService.createRecord(filePlan, record);
                return record;
            }
        });

        doTestInTransaction(new Test<Void>() {
            public Void run() throws Exception
            {
                AuthenticationUtil.runAs(new RunAsWork<Void>() {
                    public Void doWork() throws Exception
                    {
                        // check permissions
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record, READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record, FILING));
                        return null;
                    }
                }, userNone);

                AuthenticationUtil.runAs(new RunAsWork<Void>() {
                    public Void doWork() throws Exception
                    {
                        // check permissions
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record, READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record, FILING));
                        return null;
                    }
                }, userRead);

                AuthenticationUtil.runAs(new RunAsWork<Void>() {
                    public Void doWork() throws Exception
                    {
                        // check permissions
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record, READ_RECORDS));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record, FILING));
                        return null;
                    }
                }, userWrite);

                AuthenticationUtil.runAs(new RunAsWork<Void>() {
                    public Void doWork() throws Exception
                    {
                        // check permissions
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record, READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record, FILING));
                        return null;
                    }
                }, userNone);

                AuthenticationUtil.runAs(new RunAsWork<Void>() {
                    public Void doWork() throws Exception
                    {
                        // check permissions
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record, READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(record, FILING));
                        return null;
                    }
                }, userRead);

                AuthenticationUtil.runAs(new RunAsWork<Void>() {
                    public Void doWork() throws Exception
                    {
                        // check permissions
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record, READ_RECORDS));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(record, FILING));
                        return null;
                    }
                }, userWrite);

                return null;
            }
        });
    }

    public void testConcurrentSetWithRetry()
    {
        Set<String> extendedReaders = new HashSet<>(2);
        Set<String> extendedWriters = new HashSet<>(2);

        Set<NodeRef> documents = setupConcurrentTestCase(10, extendedReaders, extendedWriters);

        // For each record created previously, spawn a thread to set extended security so we cause concurrency
        // failure trying to create IPR groups with the same name
        fireParallelExecutionOfSetExtendedSecurity(documents, extendedReaders, extendedWriters, true);

        // Look for duplicated IPR groups and verify all documents have the same groups assigned
        verifyCreatedGroups(documents, false);

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    public void testConcurrentSetWithoutRetry()
    {
        Set<String> extendedReaders = new HashSet<>(2);
        Set<String> extendedWriters = new HashSet<>(2);

        Set<NodeRef> documents = setupConcurrentTestCase(10, extendedReaders, extendedWriters);

        // For each record created previously, spawn a thread to set extended security so we cause concurrency
        // failure trying to create IPR groups with the same name.
        // Since there is no retry, we expect to get a ConcurrencyFailureException
        Assert.assertThrows(ConcurrencyFailureException.class, () -> {
            fireParallelExecutionOfSetExtendedSecurity(documents, extendedReaders, extendedWriters, false);
        });

        // Look for duplicated IPR groups and verify all documents have the same groups assigned
        // Since there was a ConcurrencyFailureException some threads failed to set extended security so some
        // documents may not have IPR groups created.
        verifyCreatedGroups(documents, true);

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    private Set<NodeRef> setupConcurrentTestCase(int concurrentThreads, Set<String> extendedReaders, Set<String> extendedWriters)
    {
        final String usera = createTestUser();
        final String userb = createTestUser();
        final String owner = createTestUser();

        extendedReaders.add(usera);
        extendedReaders.add(userb);
        extendedWriters.add(usera);
        extendedWriters.add(userb);

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // Create a site
        NodeRef documentLib = createSite(new HashSet<>(), new HashSet<>());

        // Create records in the site document library
        return createRecords(concurrentThreads, documentLib, owner);
    }

    private NodeRef createSite(Set<String> readers, Set<String> writers)
    {
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {
            @Override
            public NodeRef execute() throws Throwable
            {
                final String siteShortName = GUID.generate();
                siteService.createSite(null, siteShortName, "test", "test", SiteVisibility.PRIVATE);
                readers.forEach(reader -> siteService.setMembership(siteShortName, reader, SiteModel.SITE_CONSUMER));
                writers.forEach(writer -> siteService.setMembership(siteShortName, writer, SiteModel.SITE_COLLABORATOR));
                return siteService.createContainer(siteShortName, SiteService.DOCUMENT_LIBRARY, null, null);
            }
        }, false, true);
    }

    private Set<NodeRef> createRecords(int numRecords, NodeRef parent, String owner)
    {
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Set<NodeRef>>() {
            @Override
            public Set<NodeRef> execute() throws Throwable
            {
                int createdRecords = 0;
                Set<NodeRef> documents = new HashSet<>();
                while (createdRecords < numRecords)
                {
                    final NodeRef doc = fileFolderService.create(parent, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
                    ownableService.setOwner(doc, owner);
                    recordService.createRecord(filePlan, doc, rmFolder, true);
                    recordService.file(doc);
                    recordService.complete(doc);
                    documents.add(doc);
                    createdRecords++;
                }
                return documents;
            }
        }, false, true);
    }

    private void setExtendedSecurity(NodeRef doc, Set<String> readers, Set<String> writers, boolean useRetry)
    {
        if (!useRetry)
        {
            setExtendedSecurity(doc, readers, writers);
            return;
        }

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                setExtendedSecurity(doc, readers, writers);
                return null;
            }
        }, false, true);
    }

    private void setExtendedSecurity(NodeRef doc, Set<String> readers, Set<String> writers)
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        extendedSecurityService.set(doc, readers, writers);
    }

    private void fireParallelExecutionOfSetExtendedSecurity(Set<NodeRef> documents, Set<String> extendedReaders, Set<String> extendedWriters, boolean useRetry)
    {
        CompletableFuture<?>[] futures = documents.stream()
                .map(doc -> CompletableFuture.runAsync(() -> setExtendedSecurity(doc, extendedReaders, extendedWriters, useRetry)))
                .toArray(CompletableFuture[]::new);

        try
        {
            CompletableFuture.allOf(futures).join();
        }
        catch (Exception e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof ConcurrencyFailureException)
            {
                throw (ConcurrencyFailureException) cause;
            }
            throw new RuntimeException("Error during parallel execution", e);
        }
    }

    private void verifyCreatedGroups(Set<NodeRef> documents, boolean onlyDuplicatesValidation)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                Set<String> expectedAuthorities = null;
                Set<Set<String>> errors = new HashSet<>();
                for (NodeRef doc : documents)
                {
                    Set<AccessPermission> permissions = permissionService.getAllSetPermissions(doc);
                    Set<String> authorities = getDocumentAuthorities(permissions);
                    Set<String> authoritiesById = getAuthorityIds(authorities);

                    verifyIPRGroups(authorities, onlyDuplicatesValidation);

                    if (onlyDuplicatesValidation)
                    {
                        // Some documents may not have IPR groups created if there was a ConcurrencyFailureException
                        continue;
                    }

                    // All documents should have the same exact set of groups assigned
                    if (expectedAuthorities == null)
                    {
                        expectedAuthorities = authoritiesById;
                    }

                    if (!expectedAuthorities.equals(authoritiesById))
                    {
                        errors.add(authoritiesById);
                    }
                }

                assertTrue("Unexpected authorities linked to document", errors.isEmpty());

                return null;
            }
        }, false, true);
    }

    private Set<String> getDocumentAuthorities(Set<AccessPermission> permissions)
    {
        Set<String> authorities = new HashSet<>();

        for (AccessPermission accessPermission : permissions)
        {
            String authority = accessPermission.getAuthority();
            String authName = authorityService.getName(AuthorityType.GROUP, authority);
            authorities.add(authName);

        }
        return authorities;
    }

    private Set<String> getAuthorityIds(Set<String> authorities)
    {
        Set<String> authorityIds = new HashSet<>();
        for (String authority : authorities)
        {
            String authId = authorityService.getAuthorityNodeRef(authority) != null
                    ? authorityService.getAuthorityNodeRef(authority).getId()
                    : null;
            authorityIds.add(authId);
        }
        return authorityIds;
    }

    private void verifyIPRGroups(Set<String> authorities, boolean onlyDuplicatesValidation)
    {
        boolean hasGroupIPR = false;

        for (String authorityName : authorities)
        {
            String shortName = authorityService.getShortName(authorityName);

            if (authorityName.startsWith("GROUP_IPR"))
            {
                hasGroupIPR = true;
                PagingResults<String> results = authorityService.getAuthorities(AuthorityType.GROUP, null, shortName, false,
                        false, new PagingRequest(0, 10));

                assertEquals("No duplicated IPR group expected", 1, results.getPage().size());
            }
        }

        if (!onlyDuplicatesValidation)
        {
            assertTrue("No IPR Groups created", hasGroupIPR);
        }
    }
}
