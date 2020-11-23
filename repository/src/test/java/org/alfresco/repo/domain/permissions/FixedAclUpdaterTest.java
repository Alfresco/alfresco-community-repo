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
package org.alfresco.repo.domain.permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.PermissionsDaoComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.ConcurrencyFailureException;

import junit.framework.TestCase;

/**
 * Test class for {@link FixedAclUpdater}
 * 
 * @author Andreea Dragoi
 * @author sglover
 * @since 4.2.7
 *
 */
public class FixedAclUpdaterTest extends TestCase
{
    private ApplicationContext ctx;
    private RetryingTransactionHelper txnHelper;
    private FileFolderService fileFolderService;
    private Repository repository;
    private FixedAclUpdater fixedAclUpdater;
    private PermissionsDaoComponent permissionsDaoComponent;
    private PermissionService permissionService;
    private NodeDAO nodeDAO;
    private NodeRef homeFolderNodeRef;
    private NodeArchiveService nodeArchiveService;
    private LockService lockService;
    private CheckOutCheckInService checkOutCheckInService;
    private ContentService contentService;
    private AuthorityService authorityService;
    private static final long MAX_TRANSACTION_TIME_DEFAULT = 50;
    private static final int[] filesPerLevel = { 5, 5, 20 };
    private long maxTransactionTime;
    private static HashMap<Integer, Class<?>> errors;

    @Override
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        txnHelper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();
        fileFolderService = serviceRegistry.getFileFolderService();
        repository = (Repository) ctx.getBean("repositoryHelper");
        fixedAclUpdater = (FixedAclUpdater) ctx.getBean("fixedAclUpdater");
        permissionsDaoComponent = (PermissionsDaoComponent) ctx.getBean("admPermissionsDaoComponent");
        permissionService = (PermissionService) ctx.getBean("permissionService");
        nodeDAO = (NodeDAO) ctx.getBean("nodeDAO");
        nodeArchiveService = (NodeArchiveService) ctx.getBean("nodeArchiveService");
        lockService = (LockService) ctx.getBean("lockService");
        checkOutCheckInService = (CheckOutCheckInService) ctx.getBean("checkOutCheckInService");
        contentService = (ContentService) ctx.getBean("contentService");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        homeFolderNodeRef = repository.getCompanyHome();
        maxTransactionTime = MAX_TRANSACTION_TIME_DEFAULT;
        setFixedAclMaxTransactionTime(permissionsDaoComponent, homeFolderNodeRef, maxTransactionTime);
        txnHelper.setMaxRetries(3);
    }

    @Override
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /*
     * Test setting permissions having the maxTransactionTime set to 24H, disabling the need for the job
     */
    @Test
    public void testSyncNoTimeOut()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testSyncNoTimeOutFolder");

        try
        {
            maxTransactionTime = 86400000;
            setFixedAclMaxTransactionTime(permissionsDaoComponent, homeFolderNodeRef, maxTransactionTime);
            setPermissionsOnTree(folderRef, false, false);
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * Test setting permissions explicitly as sync, but the operaration times out
     */
    @Test
    public void testSyncTimeOut()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testSyncTimeOutFolder");

        try
        {
            setPermissionsOnTree(folderRef, false, true);
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * Test setting permissions explicitly as async
     */
    @Test
    public void testAsync()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * MNT-21847 - Create a new content in folder that has the aspect applied
     */
    @Test
    public void testAsyncWithNodeCreation()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithNodeCreationFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            NodeRef folderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER);
            assertNotNull("No children folders were found with pendingFixACl aspect", folderWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                createFile(fileFolderService, folderWithPendingAcl, "NewFile", ContentModel.TYPE_CONTENT);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * MNT-22009 - Delete node that has the aspect applied before job runs
     */
    @Test
    public void testAsyncWithNodeDeletion()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithNodeDeletionFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            NodeRef folderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER);
            assertNotNull("No children folders were found with pendingFixACl aspect", folderWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                fileFolderService.delete(folderWithPendingAcl);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * MNT-22040 - Copy node that has the aspect applied before job runs
     */
    @Test
    public void testAsyncWithNodeCopy()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithNodeCopyOriginFolder");
        NodeRef targetRef = createFolderHierarchyInRoot("testAsyncWithNodeCopyTargetFolder");

        try
        {
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                // Set fixed permissions on target folder
                permissionService.setInheritParentPermissions(targetRef, false, false);
                permissionService.setPermission(targetRef, PermissionService.ADMINISTRATOR_AUTHORITY,
                        PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            setPermissionsOnTree(folderRef, true, true);

            NodeRef folderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER);
            assertNotNull("No children folders were found with pendingFixACl aspect", folderWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                // Copy folder with pending acl to target
                fileFolderService.copy(folderWithPendingAcl, targetRef, "CopyOfFolder");
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(targetRef);
            deleteNodes(folderRef);
        }
    }

    /*
     * Move node that has the aspect applied before job runs
     */
    @Test
    public void testAsyncWithNodeMove()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithNodeMoveOriginFolder");
        NodeRef targetRef = createFolderHierarchyInRoot("testAsyncWithNodeMoveTargetFolder");

        try
        {
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                // Set fixed permissions on target folder and move original folder with pending acl
                permissionService.setInheritParentPermissions(targetRef, false, false);
                permissionService.setPermission(targetRef, PermissionService.ADMINISTRATOR_AUTHORITY,
                        PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            setPermissionsOnTree(folderRef, true, true);

            NodeRef folderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER);
            assertNotNull("No children folders were found with pendingFixACl aspect", folderWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                // Move original folder with pending acl
                fileFolderService.move(folderWithPendingAcl, targetRef, "MovedFolder");
                return null;
            }, false, true);
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(targetRef);
            deleteNodes(folderRef);
        }
    }

    /*
     * Lock node that has the aspect applied before job runs
     */
    @Test
    public void testAsyncWithNodeLock()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithNodeLockFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_CONTENT);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                lockService.lock(nodeWithPendingAcl, LockType.READ_ONLY_LOCK);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * Checkout a node for editing that has the aspect applied before job runs
     */
    @Test
    public void testAsyncWithNodeCheckout()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithNodeCheckoutFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_CONTENT);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                NodeRef workingCopy = checkOutCheckInService.checkout(nodeWithPendingAcl);
                assertNotNull("Working copy is null", workingCopy);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * Update the permissions of a node that has the aspect applied (new permissions: fixed)
     */
    @Test
    public void testAsyncWithNodeUpdatePermissionsFixed()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithNodeUpdatePermissionsFixedFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_CONTENT);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(nodeWithPendingAcl, false, false);
                permissionService.setPermission(nodeWithPendingAcl, PermissionService.ADMINISTRATOR_AUTHORITY,
                        PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * Update the permissions of a node that has the aspect applied (new permissions: shared)
     */
    @Test
    public void testAsyncWithNodeUpdatePermissionsShared()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithNodeUpdatePermissionsSharedFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_CONTENT);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(nodeWithPendingAcl, true, false);
                permissionService.setPermission(nodeWithPendingAcl, PermissionService.ADMINISTRATOR_AUTHORITY,
                        PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * Update the permissions of the parent of a node that has the aspect applied (new permissions: fixed)
     */
    @Test
    public void testAsyncWithParentUpdatePermissionsFixed()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithParentUpdatePermissionsFixedFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_CONTENT);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                NodeRef parentRef = nodeDAO.getPrimaryParentAssoc(nodeDAO.getNodePair(nodeWithPendingAcl).getFirst()).getSecond()
                        .getParentRef();
                permissionService.setInheritParentPermissions(parentRef, false, false);
                permissionService.setPermission(parentRef, PermissionService.ADMINISTRATOR_AUTHORITY,
                        PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * Update the permissions of the parent of a node that has the aspect applied (new permissions: shared)
     */
    @Test
    public void testAsyncWithParentUpdatePermissionsShared()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithParentUpdatePermissionsSharedFolder");

        try
        {

            setPermissionsOnTree(folderRef, true, true);

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_CONTENT);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                NodeRef parentRef = nodeDAO.getPrimaryParentAssoc(nodeDAO.getNodePair(nodeWithPendingAcl).getFirst()).getSecond()
                        .getParentRef();
                permissionService.setInheritParentPermissions(parentRef, true, false);
                permissionService.setPermission(parentRef, PermissionService.ADMINISTRATOR_AUTHORITY,
                        PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * Update the content of a node that has the aspect applied before job runs
     */
    @Test
    public void testAsyncWithNodeContentUpdate()
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncWithNodeContentUpdateFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_CONTENT);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                ContentWriter contentWriter = contentService.getWriter(nodeWithPendingAcl, ContentModel.PROP_CONTENT, true);
                contentWriter.setEncoding("UTF-8");
                contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                contentWriter.putContent("Updated content for file");
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * Test setting permissions concurrently to actually cause the expected concurrency exception
     */
    @Test
    public void testAsyncConcurrentPermissionsUpdate() throws Throwable
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncConcurrentPermissionsUpdateFolder");
        List<FileInfo> subFolders = fileFolderService.listFolders(folderRef);
        String group_prefix = "TEST_";
        int concurrentUpdates = 5;

        try
        {
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                Set<String> zones = new HashSet<String>(2, 1.0f);
                zones.add(AuthorityService.ZONE_APP_DEFAULT);
                for (int i = 0; i < concurrentUpdates; i++)
                {
                    if (!authorityService.authorityExists("GROUP_" + group_prefix + i))
                    {
                        authorityService.createAuthority(AuthorityType.GROUP, group_prefix + i, group_prefix + i, zones);
                    }
                }
                return null;
            }, false, true);

            final Runnable[] runnables = new Runnable[concurrentUpdates];
            final List<Thread> threads = new ArrayList<Thread>();
            errors = new HashMap<Integer, Class<?>>();

            // First thread is setting permissions on top folder
            runnables[0] = createRunnableToSetPermissions(folderRef, group_prefix + 0, 0);
            Thread threadBase = new Thread(runnables[0]);
            threads.add(threadBase);
            threadBase.start();

            // All remaining threads will set permissions on sub-folders
            for (int i = 1; i < runnables.length; i++)
            {
                NodeRef nodeRef = subFolders.get(i - 1).getNodeRef();
                runnables[i] = createRunnableToSetPermissions(nodeRef, group_prefix + i, i);
                Thread thread = new Thread(runnables[i]);
                threads.add(thread);
                thread.start();
            }

            // Wait for the threads to finish
            for (Thread t : threads)
            {
                t.join();
            }

            // We expect at least one error to occur
            assertTrue("There were no concurrency errors", errors.entrySet().size() > 0);

            // There should only be ConcurrencyFailureException
            for (Map.Entry<Integer, Class<?>> error : errors.entrySet())
            {
                assertEquals("Unexpected error on Concurrent Update", ConcurrencyFailureException.class, error.getValue());
            }

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * Test setting permissions concurrently as the job runs at the same time to actually cause the expected concurrency
     * exception but the job should be able to recover
     */
    @Test
    public void testAsyncConcurrentUpdateAndJob() throws Throwable
    {
        NodeRef folderRef = createFolderHierarchyInRoot("testAsyncConcurrentUpdateAndJobFolder");
        List<FileInfo> subFolders = fileFolderService.listFolders(folderRef);
        String group_prefix = "TEST_";
        int concurrentUpdates = 5;

        try
        {
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                Set<String> zones = new HashSet<String>(2, 1.0f);
                zones.add(AuthorityService.ZONE_APP_DEFAULT);
                for (int i = 0; i < concurrentUpdates; i++)
                {
                    if (!authorityService.authorityExists("GROUP_" + group_prefix + i))
                    {
                        authorityService.createAuthority(AuthorityType.GROUP, group_prefix + i, group_prefix + i, zones);
                    }

                }
                return null;
            }, false, true);

            final Runnable[] runnables = new Runnable[concurrentUpdates];
            final List<Thread> threads = new ArrayList<Thread>();
            errors = new HashMap<Integer, Class<?>>();

            // Set permissions on top folder
            setPermissionsOnTree(folderRef, true, true);

            // First thread runs job to process setting permissions on top folder.
            runnables[0] = createRunnableToRunJob();
            Thread threadBase = new Thread(runnables[0]);
            threads.add(threadBase);
            threadBase.start();

            // Meanwhile other threads are updating permissions on subfolders as the job runs
            for (int i = 1; i < runnables.length; i++)
            {
                NodeRef nodeRef = subFolders.get(i - 1).getNodeRef();
                runnables[i] = createRunnableToSetPermissions(nodeRef, group_prefix + i, i);
                Thread thread = new Thread(runnables[i]);
                threads.add(thread);
                thread.start();
            }

            // Wait for the threads to finish
            for (Thread t : threads)
            {
                t.join();
            }

            // Verify that we only have errors of type ConcurrencyFailureException
            for (Map.Entry<Integer, Class<?>> error : errors.entrySet())
            {
                assertEquals("Unexpected error on Concurrent Update", ConcurrencyFailureException.class, error.getValue());
            }
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    private void setFixedPermissionsForTestGroup(NodeRef folderRef, String authName, int groupNumber)
    {
        txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
            try
            {
                Thread.sleep(500);
                permissionService.setInheritParentPermissions(folderRef, false, true);
                permissionService.setPermission(folderRef, authName, PermissionService.COORDINATOR, true);
            }
            catch (Exception e)
            {
                errors.put(groupNumber, e.getClass());
            }

            return null;
        }, false, true);
    }

    private Runnable createRunnableToSetPermissions(NodeRef folderRef, String authName, int thread) throws Throwable
    {
        return new Runnable()
        {
            @Override
            public synchronized void run()
            {
                setFixedPermissionsForTestGroup(folderRef, authName, thread);
            }
        };

    }

    private Runnable createRunnableToRunJob() throws Throwable
    {
        return new Runnable()
        {
            @Override
            public synchronized void run()
            {
                triggerFixedACLJob();
            }
        };

    }

    private static void setFixedAclMaxTransactionTime(PermissionsDaoComponent permissionsDaoComponent, NodeRef folderNodeRef,
            long fixedAclMaxTransactionTime)
    {
        if (permissionsDaoComponent instanceof ADMPermissionsDaoComponentImpl)
        {
            AccessControlListDAO acldao = ((ADMPermissionsDaoComponentImpl) permissionsDaoComponent).getACLDAO(folderNodeRef);
            if (acldao instanceof ADMAccessControlListDAO)
            {
                ADMAccessControlListDAO admAcLDao = (ADMAccessControlListDAO) acldao;
                admAcLDao.setFixedAclMaxTransactionTime(fixedAclMaxTransactionTime);
            }
        }
    }

    private NodeRef createFolderHierarchyInRoot(String folderName)
    {
        return txnHelper.doInTransaction((RetryingTransactionCallback<NodeRef>) () -> {
            NodeRef parent = createFile(fileFolderService, homeFolderNodeRef, folderName, ContentModel.TYPE_FOLDER);
            createFolderHierchy(fileFolderService, parent, 0, filesPerLevel);
            return parent;
        }, false, true);
    }

    private static NodeRef createFile(FileFolderService fileFolderService, NodeRef parent, String name, QName type)
    {
        return fileFolderService.create(parent, name + "_" + System.currentTimeMillis(), type).getNodeRef();
    }

    /**
     * Get number of nodes with ASPECT_PENDING_FIX_ACL
     */
    private int getNodesCountWithPendingFixedAclAspect()
    {
        return txnHelper.doInTransaction((RetryingTransactionCallback<Integer>) () -> {
            final Set<QName> aspects = new HashSet<>(1);
            aspects.add(ContentModel.ASPECT_PENDING_FIX_ACL);
            GetNodesCountWithAspectCallback callback = new GetNodesCountWithAspectCallback();
            nodeDAO.getNodesWithAspects(aspects, 1L, null, callback);
            return callback.getNodesNumber();
        }, true, true);
    }

    private void setPermissionsOnTree(NodeRef folderRef, boolean asyncCall, boolean shouldHaveNodesPending)
    {
        // kick it off by setting inherit parent permissions == false
        txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
            permissionService.setInheritParentPermissions(folderRef, false, asyncCall);
            return null;
        }, false, true);

        // Assert that there are nodes with aspect ASPECT_PENDING_FIX_ACL to be processed
        int pendingNodes = getNodesCountWithPendingFixedAclAspect();
        if (shouldHaveNodesPending)
        {
            assertTrue("There are no nodes to process", pendingNodes > 0);
        }
        else
        {
            assertEquals("There are nodes to process", pendingNodes, 0);
        }
    }

    private void triggerFixedACLJob()
    {
        // run the fixedAclUpdater until there is nothing more to fix (running the updater may create more to fix up) or
        // the count doesn't change, meaning we have a problem.
        txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
            int count = 0;
            int previousCount = 0;
            do
            {
                previousCount = count;
                count = fixedAclUpdater.execute();
            } while (count > 0 && previousCount != count);
            return null;
        }, false, true);
    }

    private NodeRef getFirstNodeWithAclPending(QName nodeType)
    {
        return txnHelper.doInTransaction((RetryingTransactionCallback<NodeRef>) () -> {
            final GetNodesWithAspectCallback getNodesCallback = new GetNodesWithAspectCallback();
            nodeDAO.getNodesWithAspects(Collections.singleton(ContentModel.ASPECT_PENDING_FIX_ACL), 0l, null, getNodesCallback);
            List<NodeRef> nodesWithAclPendingAspect = getNodesCallback.getNodes();
            for (int i = 0; i < nodesWithAclPendingAspect.size(); i++)
            {
                NodeRef nodeRef = nodesWithAclPendingAspect.get(i);
                if (nodeDAO.getNodeType(nodeDAO.getNodePair(nodeRef).getFirst()).equals(nodeType))
                {
                    return nodeRef;
                }
            }
            return null;
        }, false, true);

    }

    private void deleteNodes(NodeRef folder)
    {
        txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
            if (nodeDAO.exists(folder))
            {
                Set<QName> aspect = new HashSet<>();
                aspect.add(ContentModel.ASPECT_TEMPORARY);
                nodeDAO.addNodeAspects(nodeDAO.getNodePair(folder).getFirst(), aspect);
                fileFolderService.delete(folder);
            }
            return null;
        }, false, true);
    }

    private static class GetNodesCountWithAspectCallback implements NodeRefQueryCallback
    {
        int nodesNumber = 0;

        @Override
        public boolean handle(Pair<Long, NodeRef> nodePair)
        {
            nodesNumber++;
            return true;
        }

        public int getNodesNumber()
        {
            return nodesNumber;
        }
    }

    private static class GetNodesWithAspectCallback implements NodeRefQueryCallback
    {
        private List<NodeRef> nodes = new ArrayList<>();

        @Override
        public boolean handle(Pair<Long, NodeRef> nodePair)
        {
            nodes.add(nodePair.getSecond());
            return true;
        }

        public List<NodeRef> getNodes()
        {
            return nodes;
        }
    }

    /**
     * Creates a level in folder/file hierarchy. Intermediate levels will contain folders and last ones files
     * 
     * @param fileFolderService
     * @param parent
     *            - parent node of the of hierarchy level
     * @param level
     *            - zero based
     * @param filesPerLevel
     *            - array containing number of folders/files per level
     */
    private static void createFolderHierchy(FileFolderService fileFolderService, NodeRef parent, int level, int[] filesPerLevel)
    {
        int levels = filesPerLevel.length;
        // intermediate level
        if (level < levels - 1)
        {
            int numFiles = filesPerLevel[level];
            for (int i = 0; i < numFiles; i++)
            {
                NodeRef node = createFile(fileFolderService, parent, "LVL" + level + i, ContentModel.TYPE_FOLDER);
                createFolderHierchy(fileFolderService, node, level + 1, filesPerLevel);
            }
        }
        // last level
        else if (level == levels - 1)
        {
            int numFiles = filesPerLevel[level];
            for (int i = 0; i < numFiles; i++)
            {
                createFile(fileFolderService, parent, "File" + i, ContentModel.TYPE_CONTENT);
            }
        }
    }
}
