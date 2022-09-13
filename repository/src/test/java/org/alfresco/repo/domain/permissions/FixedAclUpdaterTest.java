/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.PermissionsDaoComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.test.junitrules.RetryAtMostRule;
import org.alfresco.util.test.junitrules.RetryAtMostRule.RetryAtMost;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Test class for {@link FixedAclUpdater}
 *
 * @author Andreea Dragoi
 * @author sglover
 * @since 4.2.7
 *
 */
public class FixedAclUpdaterTest
{
    private static final Logger LOG = LoggerFactory.getLogger(FixedAclUpdaterTest.class);

    private ApplicationContext ctx;
    private RetryingTransactionHelper txnHelper;
    private FileFolderService fileFolderService;
    private Repository repository;
    private FixedAclUpdater fixedAclUpdater;
    private PermissionsDaoComponent permissionsDaoComponent;
    private PermissionService permissionService;
    private NodeDAO nodeDAO;
    private NodeRef homeFolderNodeRef;
    private LockService lockService;
    private CheckOutCheckInService checkOutCheckInService;
    private ContentService contentService;
    private AuthorityService authorityService;
    private static final long MAX_TRANSACTION_TIME_DEFAULT = 10;
    private static final int[] filesPerLevelMoreFolders = { 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
    private static final int[] filesPerLevelMoreFiles = { 5, 100 };
    private long maxTransactionTime;
    private static HashMap<Integer, Class<?>> errors;
    private static String TEST_GROUP_NAME = "FixedACLUpdaterTest";
    private static String TEST_GROUP_NAME_FULL = PermissionService.GROUP_PREFIX + TEST_GROUP_NAME;
    private static String DEFAULT_PERMISSION = PermissionService.CONTRIBUTOR;

    @Rule
    public RetryAtMostRule retryAtMostRule = new RetryAtMostRule();

    @Before
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
        lockService = (LockService) ctx.getBean("lockService");
        checkOutCheckInService = (CheckOutCheckInService) ctx.getBean("checkOutCheckInService");
        contentService = (ContentService) ctx.getBean("contentService");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        homeFolderNodeRef = repository.getCompanyHome();
        maxTransactionTime = MAX_TRANSACTION_TIME_DEFAULT;
        setFixedAclMaxTransactionTime(permissionsDaoComponent, homeFolderNodeRef, maxTransactionTime);
    }

    @After
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
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testSyncNoTimeOutFolder");
        ACLComparator aclComparator = new ACLComparator(folderRef);

        try
        {
            maxTransactionTime = 86400000;
            setFixedAclMaxTransactionTime(permissionsDaoComponent, homeFolderNodeRef, maxTransactionTime);
            setPermissionsOnTree(folderRef, false, false);
            aclComparator.compareACLs();

            assertEquals("There are nodes pending", 0, getNodesCountWithPendingFixedAclAspect());
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
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testSyncTimeOutFolder");
        ACLComparator aclComparator = new ACLComparator(folderRef);

        try
        {
            setPermissionsOnTree(folderRef, false, true);

            // Get current ACLS on non pending nodes and validate
            aclComparator.updateCurrentACLs();
            assertTrue("Permissions not applied", aclComparator.parentHasOriginalPermission());

            // Validate values in pending ACL node
            NodeRef folderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER);
            ACLComparator aclComparatorForPending = new ACLComparator(folderWithPendingAcl);
            assertEquals("Pending inheritFrom value should be the parent ACL id", aclComparator.getParentAcl(),
                    aclComparatorForPending.getPendingInheritFromAcl());
            assertFalse("Permissions not expected to be applied on a pending node before job",
                    aclComparatorForPending.firstChildHasOriginalPermission());

            // Trigger job
            triggerFixedACLJob();

            // Verify if ACLs where applied correctly
            aclComparator.updateCurrentACLs();
            aclComparatorForPending.updateCurrentACLs();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertEquals("Processed Pending ACL children doesn't have correct ACL", aclComparator.getChildAcl(),
                    aclComparatorForPending.getChildAcl());
            assertTrue("Permissions not applied on pending nodes", aclComparatorForPending.firstChildHasOriginalPermission());
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
    @RetryAtMost(3)
    public void testAsync()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncFolder");
        ACLComparator aclComparator = new ACLComparator(folderRef);

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            // Get current ACLS on non pending nodes and validate
            aclComparator.updateCurrentACLs();
            assertTrue("Permissions not applied", aclComparator.parentHasOriginalPermission());

            // Validate values in pending ACL node
            NodeRef folderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER);
            assertNotNull("No children folders were found with pendingFixACl aspect", folderWithPendingAcl);
            ACLComparator aclComparatorForPending = new ACLComparator(folderWithPendingAcl);
            assertEquals("Pending inheritFrom value should be the parent ACL id", aclComparator.getParentAcl(),
                    aclComparatorForPending.getPendingInheritFromAcl());
            assertFalse("Permissions not expected to be applied on a pending node before job",
                    aclComparatorForPending.firstChildHasOriginalPermission());

            // Trigger job
            triggerFixedACLJob();

            // Verify if ACLs where applied correctly
            aclComparator.updateCurrentACLs();
            aclComparatorForPending.updateCurrentACLs();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertEquals("Processed Pending ACL children doesn't have correct ACL", aclComparator.getChildAcl(),
                    aclComparatorForPending.getChildAcl());
            assertTrue("Pending nodes doesn't have same permission as parent",
                    aclComparatorForPending.parentHasOriginalPermission());
            assertTrue("Children of Pending nodes doesn't have same permission as parent",
                    aclComparatorForPending.firstChildHasOriginalPermission());
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
    @RetryAtMost(3)
    public void testAsyncWithNodeCreation()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeCreationFolder");

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
    @RetryAtMost(3)
    public void testAsyncWithNodeDeletion()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeDeletionFolder");

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
     * Copy node with no timeout and no pending nodes
     */
    @Test
    public void testSyncCopyNoTimeOut() throws FileExistsException, FileNotFoundException
    {
        NodeRef originalRef = createFolderHierarchyInRootForFolderTests("originFolder");
        NodeRef targetRefBase = createFolderHierarchyInRootForFolderTests("targetFolder");

        // Get ACLS for later comparison
        ACLComparator aclComparatorOrigin = new ACLComparator(originalRef);

        try
        {
            maxTransactionTime = 86400000;
            setFixedAclMaxTransactionTime(permissionsDaoComponent, homeFolderNodeRef, maxTransactionTime);

            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRefBase, true, false);
                permissionService.setPermission(targetRefBase, TEST_GROUP_NAME_FULL, PermissionService.CONSUMER, true);
                return null;
            }, false, true);

            // Trigger the job so the target folder structure has a different base ACL
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());

            NodeRef targetRef = nodeDAO.getNodePair(getChild(nodeDAO.getNodePair(targetRefBase).getFirst())).getSecond();

            // Set Shared permissions on origin
            permissionService.setInheritParentPermissions(originalRef, true, false);
            permissionService.setPermission(originalRef, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
            aclComparatorOrigin.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);

            // Set Shared permissions on target and inherit permissions from parent
            permissionService.setInheritParentPermissions(targetRef, true, false);
            permissionService.setPermission(targetRef, TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION, true);

            // Copy the nodes
            NodeRef copiedNode = fileFolderService.copy(originalRef, targetRef, null).getNodeRef();
            ACLComparator aclComparatorCopied = new ACLComparator(copiedNode);
            aclComparatorOrigin.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);

            // Validate the results - Permissions should merge on copied node
            assertEquals("There are nodes pending", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Copied node did not inherit permissions from target",
                    aclComparatorCopied.hasPermission(TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION));
            assertTrue("Child of Copied node did not inherit permissions from target",
                    aclComparatorCopied.firstChildHasPermission(TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION));
            assertTrue("Copied node did not keep original permissions", aclComparatorCopied.parentHasOriginalPermission());
            assertTrue("Child of Copied node did not keep original permissions",
                    aclComparatorCopied.firstChildHasOriginalPermission());
        }
        finally
        {
            deleteNodes(originalRef);
            deleteNodes(targetRefBase);
        }
    }

    /*
     * MNT-22040 - Copy node that has the aspect applied before job runs
     */
    @Test
    @RetryAtMost(3)
    public void testAsyncWithNodeCopy()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeCopyOriginFolder");
        NodeRef targetRefBase = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeCopyTargetFolder");

        try
        {
            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRefBase, true, false);
                permissionService.setPermission(targetRefBase, TEST_GROUP_NAME_FULL, PermissionService.CONSUMER, true);
                return null;
            }, false, true);

            // Trigger the job so the target folder structure has a different base ACL
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());

            NodeRef targetRef = nodeDAO.getNodePair(getChild(nodeDAO.getNodePair(targetRefBase).getFirst())).getSecond();

            // Get ACLS for later comparison
            ACLComparator aclComparatorTarget = new ACLComparator(targetRef);

            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRef, false, false);
                permissionService.setPermission(targetRef, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                aclComparatorTarget.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);
                return null;
            }, false, true);

            assertTrue("Target Folder does not have correct permission", aclComparatorTarget.parentHasOriginalPermission());

            // Set permissions async on origin folder and inherit permissions from parent
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(folderRef, true, false);
                permissionService.setPermission(folderRef, TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION, true);
                return null;
            }, false, true);

            // Find a pending ACL folder to copy
            NodeRef folderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, folderRef);
            assertNotNull("No children folders were found with pendingFixACl aspect", folderWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                // copy folder with pending acl to target
                fileFolderService.copy(folderWithPendingAcl, targetRef, "copyOfFolder");
                return null;
            }, false, true);

            NodeRef copiedChild = fileFolderService.searchSimple(targetRef, "copyOfFolder");
            ACLComparator aclComparatorCopiedPendingChild = new ACLComparator(copiedChild);

            // Trigger job
            triggerFixedACLJob();

            // Validate results - ACL's aren't suppose to merge as the Copied folder doesn't have any ACL's directly
            // applied to them and should just inherit from whatever parent they have
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Pending Copied node did not inherit permissions from target",
                    aclComparatorCopiedPendingChild.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Child of Pending Copied node did not inherit permissions from target",
                    aclComparatorCopiedPendingChild.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertFalse("Pending Copied node kept original permissions",
                    aclComparatorCopiedPendingChild.parentHasOriginalPermission());
            assertFalse("Child of Pending Copied node kept original permissions",
                    aclComparatorCopiedPendingChild.firstChildHasOriginalPermission());
        }
        finally
        {
            deleteNodes(folderRef);
            deleteNodes(targetRefBase);
        }
    }

    /*
     * Copy node that has the aspect to another folder that also has the aspect applied before job runs
     */
    @Test
    @RetryAtMost(3)
    public void testAsyncWithNodeCopyToPendingFolder()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeCopyOriginFolder");
        NodeRef targetRefBase = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeCopyTargetFolder");

        try
        {
            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRefBase, true, false);
                permissionService.setPermission(targetRefBase, TEST_GROUP_NAME_FULL, PermissionService.CONSUMER, true);
                return null;
            }, false, true);

            // Trigger the job so the target folder structure has a different base ACL
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());

            NodeRef targetRef = nodeDAO.getNodePair(getChild(nodeDAO.getNodePair(targetRefBase).getFirst())).getSecond();

            // Get ACLS for later comparison
            ACLComparator aclComparatorTarget = new ACLComparator(targetRef);

            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRef, false, false);
                permissionService.setPermission(targetRef, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                aclComparatorTarget.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);
                return null;
            }, false, true);

            assertTrue("Target Folder does not have correct permission", aclComparatorTarget.parentHasOriginalPermission());

            // Get target Folder with a pending ACL to copy the pending folder to
            NodeRef targetFolderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, targetRef);
            assertNotNull("No children folders were found with pendingFixACl aspect", targetFolderWithPendingAcl);
            ACLComparator aclComparatorTargetPendingChild = new ACLComparator(targetFolderWithPendingAcl);
            aclComparatorTargetPendingChild.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);

            // Set permissions on origin folder and get a pending folder to copy
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(folderRef, true, false);
                permissionService.setPermission(folderRef, TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION, true);
                return null;
            }, false, true);

            // Find a pending ACL folder to copy
            NodeRef originFolderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, folderRef);
            assertNotNull("No children folders were found with pendingFixACl aspect", originFolderWithPendingAcl);

            // copy one pending folder into the other
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                // copy folder with pending acl to target
                fileFolderService.copy(originFolderWithPendingAcl, targetFolderWithPendingAcl, "copyOfFolder");
                return null;
            }, false, true);

            NodeRef copiedChild = fileFolderService.searchSimple(targetFolderWithPendingAcl, "copyOfFolder");
            ACLComparator aclComparatorCopiedPendingChild = new ACLComparator(copiedChild);

            // Trigger job
            triggerFixedACLJob();

            // Validate results - ACL's aren't suppose to merge as the Copied folder doesn't have any ACL's directly
            // applied to them and should just inherit from whatever parent they have
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Pending Copied node did not inherit permissions from target",
                    aclComparatorCopiedPendingChild.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Child of PendingCopied node did not inherit permissions from target",
                    aclComparatorCopiedPendingChild.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertFalse("Pending Copied kept original permissions",
                    aclComparatorCopiedPendingChild.parentHasOriginalPermission());
            assertFalse("Child of Pending Copied node kept original permissions",
                    aclComparatorCopiedPendingChild.firstChildHasOriginalPermission());
            assertTrue("Pending target node does not have parent's permissions",
                    aclComparatorTargetPendingChild.parentHasOriginalPermission());
            assertTrue("Child of Pending target node does not have parent's permissions",
                    aclComparatorTargetPendingChild.firstChildHasOriginalPermission());
        }
        finally
        {
            deleteNodes(folderRef);
            deleteNodes(targetRefBase);
        }
    }

    /*
     * Copy parent of node that has the aspect to a child folder of a folder that also has the aspect applied before job
     * runs
     */
    @Test
    @RetryAtMost(3)
    public void testAsyncWithNodeCopyParentToChildPendingFolder()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeCopyOriginFolder");
        NodeRef targetRefBase = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeCopyTargetFolder");

        try
        {
            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRefBase, true, false);
                permissionService.setPermission(targetRefBase, TEST_GROUP_NAME_FULL, PermissionService.CONSUMER, true);
                return null;
            }, false, true);

            // Trigger the job so the target folder structure has a different base ACL
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());

            NodeRef targetRef = nodeDAO.getNodePair(getChild(nodeDAO.getNodePair(targetRefBase).getFirst())).getSecond();

            // Get ACLS for later comparison
            ACLComparator aclComparatorTarget = new ACLComparator(targetRef);

            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRef, false, false);
                permissionService.setPermission(targetRef, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                aclComparatorTarget.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);
                return null;
            }, false, true);

            assertTrue("Target Folder does not have correct permission", aclComparatorTarget.parentHasOriginalPermission());

            // Get target Folder with a pending ACL to copy the pending folder to
            NodeRef targetFolderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, targetRef);
            assertNotNull("No children folders were found with pendingFixACl aspect", targetFolderWithPendingAcl);
            NodeRef targetFolderWithPendingAclChild = nodeDAO
                    .getNodePair(getChild(nodeDAO.getNodePair(targetFolderWithPendingAcl).getFirst())).getSecond();
            ACLComparator aclComparatorTargetPendingChild = new ACLComparator(targetFolderWithPendingAclChild);
            aclComparatorTargetPendingChild.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);

            // Set permissions on origin folder and get a pending folder to copy
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(folderRef, true, false);
                permissionService.setPermission(folderRef, TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION, true);
                return null;
            }, false, true);

            // Find a pending ACL folder and copy its parent
            NodeRef originFolderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, folderRef);
            assertNotNull("No children folders were found with pendingFixACl aspect", originFolderWithPendingAcl);
            NodeRef originFolderWithPendingAclParent = nodeDAO
                    .getPrimaryParentAssoc(nodeDAO.getNodePair(originFolderWithPendingAcl).getFirst()).getSecond().getParentRef();

            // copy one pending folder into the other
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                // copy folder with pending acl to target
                fileFolderService.copy(originFolderWithPendingAclParent, targetFolderWithPendingAcl, "copyOfFolder");
                return null;
            }, false, true);

            NodeRef copiedChild = fileFolderService.searchSimple(targetFolderWithPendingAcl, "copyOfFolder");
            ACLComparator aclComparatorCopiedPendingParent = new ACLComparator(copiedChild);

            // Trigger job
            triggerFixedACLJob();

            // Validate results - ACL's aren't suppose to merge as the Copied folder doesn't have any ACL's directly
            // applied to them and should just inherit from whatever parent they have
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Copied Parent node did not inherit permissions from target",
                    aclComparatorCopiedPendingParent.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Pending Node copied with parent node did not inherit permissions from target",
                    aclComparatorCopiedPendingParent.firstChildHasPermission(TEST_GROUP_NAME_FULL,
                            PermissionService.COORDINATOR));

            // Depending on when the time runs out of the transaction, the parent node we copied may be the node we
            // actually set permission on. In this case, if this parent node is copied, it is expected to keep the
            // permissions we set directly on it
            if (originFolderWithPendingAclParent.equals(folderRef))
            {
                assertTrue("Copied Parent (from original node where permissions were set) did not keep original permissions",
                        aclComparatorCopiedPendingParent.parentHasOriginalPermission());
                assertTrue("Pending Node copied with parent node did not keep original permissions",
                        aclComparatorCopiedPendingParent.firstChildHasOriginalPermission());
            }
            else
            {
                assertFalse("Copied Parent kept original permissions",
                        aclComparatorCopiedPendingParent.parentHasOriginalPermission());
                assertFalse("Pending Node copied with parent node kept original permissions",
                        aclComparatorCopiedPendingParent.firstChildHasOriginalPermission());
            }

            assertTrue("Pending target node does not have parent's permissions",
                    aclComparatorTargetPendingChild.parentHasOriginalPermission());
            assertTrue("Child of Pending target node does not have parent's permissions",
                    aclComparatorTargetPendingChild.firstChildHasOriginalPermission());
        }
        finally
        {
            deleteNodes(folderRef);
            deleteNodes(targetRefBase);
        }
    }

    /*
     * Move child of node that has the aspect to a child folder of a folder that also has the aspect applied before job
     * runs
     */
    @Test
    @RetryAtMost(3)
    public void testAsyncWithNodeMoveChildToChildPendingFolder()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeMoveChildToChildPendingFolderOrigin");
        NodeRef targetRefBase = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeMoveChildToChildPendingFolderTarget");

        try
        {
            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRefBase, true, false);
                permissionService.setPermission(targetRefBase, TEST_GROUP_NAME_FULL, PermissionService.CONSUMER, true);
                return null;
            }, false, true);

            // Trigger the job so the target folder structure has a different base ACL
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());

            NodeRef targetRef = nodeDAO.getNodePair(getChild(nodeDAO.getNodePair(targetRefBase).getFirst())).getSecond();

            // Set permissions on a child to get a new shared ACL with pending acl nodes
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRef, true, false);
                permissionService.setPermission(targetRef, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            // Get target Folder with a pending ACL
            NodeRef targetFolderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, targetRef);
            assertNotNull("No children folders were found with pendingFixACl aspect", targetFolderWithPendingAcl);
            NodeRef targetFolderWithPendingAclChild = nodeDAO
                    .getNodePair(getChild(nodeDAO.getNodePair(targetFolderWithPendingAcl).getFirst())).getSecond();

            // Get ACLS for later comparison
            ACLComparator aclComparatorTarget = new ACLComparator(targetFolderWithPendingAcl);
            aclComparatorTarget.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);

            // Set permissions on origin folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(folderRef, true, false);
                permissionService.setPermission(folderRef, TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION, true);
                return null;
            }, false, true);

            // Find a pending ACL folder
            NodeRef originFolderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, folderRef);
            assertNotNull("No children folders were found with pendingFixACl aspect", originFolderWithPendingAcl);
            NodeRef originFolderWithPendingAclChild = nodeDAO
                    .getNodePair(getChild(nodeDAO.getNodePair(originFolderWithPendingAcl).getFirst())).getSecond();

            // Get ACLS for later comparison
            ACLComparator aclComparatorMovedNode = new ACLComparator(originFolderWithPendingAclChild);
            aclComparatorMovedNode.setOriginalPermission(TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION);

            // Move one pending folder into the other
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                fileFolderService.move(originFolderWithPendingAclChild, targetFolderWithPendingAclChild, "movedFolder");
                return null;
            }, false, true);

            // Trigger job
            triggerFixedACLJob();

            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Moved node did not inherit permissions from target",
                    aclComparatorMovedNode.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Child of Pending Moved node did not inherit permissions from target",
                    aclComparatorMovedNode.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertFalse("Moved node kept original permissions", aclComparatorMovedNode.parentHasOriginalPermission());
            assertFalse("Child of Moved node kept original permissions",
                    aclComparatorMovedNode.firstChildHasOriginalPermission());
        }
        finally
        {
            deleteNodes(folderRef);
            deleteNodes(targetRefBase);
        }
    }

    /*
     * Create a conflicting ACL on a node and then try to run the job normally, without forcing the ACL to get the
     * expected error and then run it again with the forcedShareACL property as true so it can override the problematic
     * ACL
     */
    @Test
    @RetryAtMost(3)
    public void testAsyncWithErrorsForceSharedACL()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithErrorsForceSharedACL");

        try
        {
            // Set permissions on origin folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(folderRef, true, false);
                permissionService.setPermission(folderRef, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            // Find a pending ACL folder
            NodeRef originFolderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, folderRef);
            assertNotNull("No children folders were found with pendingFixACl aspect", originFolderWithPendingAcl);
            NodeRef originFolderWithPendingAclChild = nodeDAO
                    .getNodePair(getChild(nodeDAO.getNodePair(originFolderWithPendingAcl).getFirst())).getSecond();

            // Create a new ACL elsewhere and put the shared ACL (from a child) on the pending node child to simulate
            // conflict
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                NodeRef tempNode = createFile(fileFolderService, folderRef, "testAsyncWithErrorsForceSharedACLTemp",
                        ContentModel.TYPE_FOLDER);
                permissionService.setInheritParentPermissions(tempNode, false, false);
                permissionService.setPermission(tempNode, TEST_GROUP_NAME_FULL, PermissionService.CONSUMER, true);
                NodeRef tempNodeChild = createFile(fileFolderService, tempNode, "testAsyncWithErrorsForceSharedACLTempChild",
                        ContentModel.TYPE_FOLDER);
                setACL(permissionsDaoComponent, originFolderWithPendingAclChild,
                        nodeDAO.getNodeAclId(nodeDAO.getNodePair(tempNodeChild).getFirst()));
                return null;
            }, false, true);

            ACLComparator aclComparator = new ACLComparator(originFolderWithPendingAclChild);

            // Trigger job without forcing the shared ACL, only 1 error is expected
            triggerFixedACLJob(false);
            assertEquals("Unexpected number of errors", 1, getNodesCountWithPendingFixedAclAspect());

            // Trigger job forcing the shared ACL
            triggerFixedACLJob(true);

            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Child of node with conflict does not have correct permissions",
                    aclComparator.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Node with conflict does not have correct permissions",
                    aclComparator.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    /*
     * MNT-22040 - Move node that has the aspect applied before job runs
     */
    @Test
    @RetryAtMost(3)
    public void testAsyncWithNodeMove()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeMoveOriginFolder");
        NodeRef targetRefBase = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeMoveTargetFolder");

        try
        {
            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRefBase, true, false);
                permissionService.setPermission(targetRefBase, TEST_GROUP_NAME_FULL, PermissionService.CONSUMER, true);
                return null;
            }, false, true);

            // Trigger the job so the target folder structure has a different base ACL
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());

            NodeRef targetRef = nodeDAO.getNodePair(getChild(nodeDAO.getNodePair(targetRefBase).getFirst())).getSecond();

            // Get ACLS for later comparison
            ACLComparator aclComparatorTarget = new ACLComparator(targetRef);

            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRef, false, false);
                permissionService.setPermission(targetRef, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                aclComparatorTarget.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);
                return null;
            }, false, true);

            assertTrue("Target Folder does not have correct permission", aclComparatorTarget.parentHasOriginalPermission());

            // Set permissions async on origin folder and inherit permissions from parent
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(folderRef, true, false);
                permissionService.setPermission(folderRef, TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION, true);
                return null;
            }, false, true);

            // Find a pending ACL folder to move
            NodeRef folderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, folderRef);
            ACLComparator aclComparatorOriginPendingChild = new ACLComparator(folderWithPendingAcl);
            assertNotNull("No children folders were found with pendingFixACl aspect", folderWithPendingAcl);
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                // move folder with pending acl to target
                fileFolderService.move(folderWithPendingAcl, targetRef, "moveOfFolder");
                return null;
            }, false, true);

            // Trigger job
            triggerFixedACLJob();

            // Validate results - ACL's aren't suppose to merge as the Moved folder doesn't have any ACL's directly
            // applied to them and should just inherit from whatever parent they have
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Pending Moved node did not inherit permissions from target",
                    aclComparatorOriginPendingChild.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Child of Pending Moved node did not inherit permissions from target",
                    aclComparatorOriginPendingChild.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertFalse("Pending Moved node kept original permissions",
                    aclComparatorOriginPendingChild.parentHasOriginalPermission());
            assertFalse("Child of Pending Moved node kept original permissions",
                    aclComparatorOriginPendingChild.firstChildHasOriginalPermission());
        }
        finally
        {
            deleteNodes(folderRef);
            deleteNodes(targetRefBase);
        }
    }

    /*
     * Move node that has the aspect to another folder that also has the aspect applied before job runs
     */
    @Test
    @RetryAtMost(3)
    public void testAsyncWithNodeMoveToPendingFolder()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeMoveOriginFolder");
        NodeRef targetRefBase = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeMoveTargetFolder");

        try
        {

            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRefBase, true, false);
                permissionService.setPermission(targetRefBase, TEST_GROUP_NAME_FULL, PermissionService.CONSUMER, true);
                return null;
            }, false, true);

            // Trigger the job so the target folder structure has a different base ACL
            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());

            NodeRef targetRef = nodeDAO.getNodePair(getChild(nodeDAO.getNodePair(targetRefBase).getFirst())).getSecond();

            // Get ACLS for later comparison
            ACLComparator aclComparatorTarget = new ACLComparator(targetRef);

            // Set permissions on target folder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(targetRef, false, false);
                permissionService.setPermission(targetRef, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                aclComparatorTarget.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);
                return null;
            }, false, true);

            assertTrue("Target Folder does not have correct permission", aclComparatorTarget.parentHasOriginalPermission());

            // Get target Folder with a pending ACL to move the pending folder to
            NodeRef targetFolderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, targetRef);
            assertNotNull("No children folders were found with pendingFixACl aspect", targetFolderWithPendingAcl);
            ACLComparator aclComparatorTargetPendingChild = new ACLComparator(targetFolderWithPendingAcl);
            aclComparatorTargetPendingChild.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR);

            // Set permissions on origin folder and get a pending folder to move
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(folderRef, true, false);
                permissionService.setPermission(folderRef, TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION, true);
                return null;
            }, false, true);

            // Find a pending ACL folder to move
            NodeRef originFolderWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER, folderRef);
            assertNotNull("No children folders were found with pendingFixACl aspect", originFolderWithPendingAcl);
            ACLComparator aclComparatorOriginPendingChild = new ACLComparator(originFolderWithPendingAcl);

            // move one pending folder into the other
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                // move folder with pending acl to target
                fileFolderService.move(originFolderWithPendingAcl, targetFolderWithPendingAcl, "movedFolder");
                return null;
            }, false, true);

            // Trigger job
            triggerFixedACLJob();

            // Validate results - ACL's aren't suppose to merge as the Moved folder doesn't have any ACL's directly
            // applied to them and should just inherit from whatever parent they have
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Pending Moved node did not inherit permissions from target",
                    aclComparatorOriginPendingChild.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Child of PendingMoved node did not inherit permissions from target",
                    aclComparatorOriginPendingChild.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertFalse("Pending Moved kept original permissions", aclComparatorOriginPendingChild.parentHasOriginalPermission());
            assertFalse("Child of Pending Moved node kept original permissions",
                    aclComparatorOriginPendingChild.firstChildHasOriginalPermission());
            assertTrue("Pending target node does not have parent's permissions",
                    aclComparatorTargetPendingChild.parentHasOriginalPermission());
            assertTrue("Child of Pending target node does not have parent's permissions",
                    aclComparatorTargetPendingChild.firstChildHasOriginalPermission());
        }
        finally
        {
            deleteNodes(folderRef);
            deleteNodes(targetRefBase);
        }
    }

    /*
     * Lock node that has the aspect applied before job runs
     */
    @Test
    @RetryAtMost(3)
    public void testAsyncWithNodeLock()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFileTests("testAsyncWithNodeLockFolder");

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
    @RetryAtMost(3)
    public void testAsyncWithNodeCheckout()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFileTests("testAsyncWithNodeCheckoutFolder");

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
    @RetryAtMost(3)
    public void testAsyncWithNodeUpdatePermissionsFixed()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeUpdatePermissionsFixedFolder");
        ACLComparator aclComparatorTop = new ACLComparator(folderRef);

        try
        {
            setPermissionsOnTree(folderRef, true, true);
            aclComparatorTop.updateCurrentACLs();

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);
            ACLComparator aclComparator = new ACLComparator(nodeWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(nodeWithPendingAcl, false, false);
                permissionService.setPermission(nodeWithPendingAcl, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                return null;
            }, false, true);
            aclComparator.updateCurrentACLs();

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertFalse("Pending node is not expected to have old permission", aclComparator.parentHasOriginalPermission());
            assertFalse("Child of Pending node is not expected to have old permission",
                    aclComparator.firstChildHasOriginalPermission());
            assertTrue("Pending node is expected to have new permission",
                    aclComparator.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Child of Pending node is expected to have new permission",
                    aclComparator.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
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
    @RetryAtMost(3)
    public void testAsyncWithNodeUpdatePermissionsShared()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithNodeUpdatePermissionsSharedFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);
            ACLComparator aclComparator = new ACLComparator(nodeWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(nodeWithPendingAcl, true, false);
                permissionService.setPermission(nodeWithPendingAcl, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Pending node is expected to have old permission", aclComparator.parentHasOriginalPermission());
            assertTrue("Child of Pending node is expected to have old permission",
                    aclComparator.firstChildHasOriginalPermission());
            assertTrue("Pending node is expected to have new permission",
                    aclComparator.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Child of Pending node is expected to have new permission",
                    aclComparator.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
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
    @RetryAtMost(3)
    public void testAsyncWithParentUpdatePermissionsFixed()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithParentUpdatePermissionsFixedFolder");

        try
        {
            setPermissionsOnTree(folderRef, true, true);

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);
            ACLComparator aclComparator = new ACLComparator(nodeWithPendingAcl);

            NodeRef parentRef = nodeDAO.getPrimaryParentAssoc(nodeDAO.getNodePair(nodeWithPendingAcl).getFirst()).getSecond()
                    .getParentRef();

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(parentRef, false, false);
                permissionService.setPermission(parentRef, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertFalse("Pending node is not expected to have old permission", aclComparator.parentHasOriginalPermission());
            assertFalse("Child of Pending node is not expected to have old permission",
                    aclComparator.firstChildHasOriginalPermission());
            assertTrue("Pending node is expected to have new permission",
                    aclComparator.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Child of Pending node is expected to have new permission",
                    aclComparator.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
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
    @RetryAtMost(3)
    public void testAsyncWithParentUpdatePermissionsShared()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncWithParentUpdatePermissionsSharedFolder");

        try
        {

            setPermissionsOnTree(folderRef, true, true);

            NodeRef nodeWithPendingAcl = getFirstNodeWithAclPending(ContentModel.TYPE_FOLDER);
            assertNotNull("No children files were found with pendingFixACl aspect", nodeWithPendingAcl);
            ACLComparator aclComparator = new ACLComparator(nodeWithPendingAcl);

            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                NodeRef parentRef = nodeDAO.getPrimaryParentAssoc(nodeDAO.getNodePair(nodeWithPendingAcl).getFirst()).getSecond()
                        .getParentRef();
                permissionService.setInheritParentPermissions(parentRef, true, false);
                permissionService.setPermission(parentRef, TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR, true);
                return null;
            }, false, true);

            triggerFixedACLJob();
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Pending node is expected to have old permission", aclComparator.parentHasOriginalPermission());
            assertTrue("Child of Pending node is expected to have old permission",
                    aclComparator.firstChildHasOriginalPermission());
            assertTrue("Pending node is expected to have new permission",
                    aclComparator.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
            assertTrue("Child of Pending node is expected to have new permission",
                    aclComparator.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.COORDINATOR));
        }
        finally
        {
            deleteNodes(folderRef);
        }
    }

    @Test
    @RetryAtMost(3)
    public void testAsyncCascadeUpdatePermissions()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncCascadeUpdatePermissionsFolder");
        List<FileInfo> subFolders = fileFolderService.listFolders(folderRef);
        NodeRef subFolder1 = subFolders.get(0).getNodeRef();
        // Get ACLS for later comparison
        ACLComparator aclComparatorBase = new ACLComparator(folderRef);
        ACLComparator aclComparatorSubfolder1 = new ACLComparator(subFolder1);

        try
        {

            // Set permissions First Subfolder - should put ACL on subfolder and add aspect to child
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(subFolder1, false, true);
                permissionService.setPermission(subFolder1, TEST_GROUP_NAME_FULL, PermissionService.CONTRIBUTOR, true);
                aclComparatorSubfolder1.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.CONTRIBUTOR);
                return null;
            }, false, true);

            // Set permissions on base folder - should put ACL on base folder and add aspect to the previous subfolder
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                permissionService.setInheritParentPermissions(folderRef, false, true);
                permissionService.setPermission(folderRef, TEST_GROUP_NAME_FULL, PermissionService.CONSUMER, true);
                aclComparatorBase.setOriginalPermission(TEST_GROUP_NAME_FULL, PermissionService.CONSUMER);
                return null;
            }, false, true);

            assertTrue("There are no nodes to process", getNodesCountWithPendingFixedAclAspect() > 0);

            // Trigger job
            triggerFixedACLJob();

            // Validate results
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            assertTrue("Base Folder permissions are incorrect",
                    aclComparatorBase.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.CONSUMER));
            assertTrue("First Sub-Folder permissions are incorrect",
                    aclComparatorSubfolder1.hasPermission(TEST_GROUP_NAME_FULL, PermissionService.CONTRIBUTOR));
            assertTrue("Child of First Sub-Folder permissions are incorrect",
                    aclComparatorSubfolder1.firstChildHasPermission(TEST_GROUP_NAME_FULL, PermissionService.CONTRIBUTOR));
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
    @RetryAtMost(3)
    public void testAsyncWithNodeContentUpdate()
    {
        NodeRef folderRef = createFolderHierarchyInRootForFileTests("testAsyncWithNodeContentUpdateFolder");

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
    @RetryAtMost(3)
    public void testAsyncConcurrentPermissionsUpdate() throws Throwable
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncConcurrentPermissionsUpdateFolder");
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
                    if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + group_prefix + i))
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

            // There should only be ConcurrencyFailureException
            for (Map.Entry<Integer, Class<?>> error : errors.entrySet())
            {
                assertEquals("Unexpected error on Concurrent Update", ConcurrencyFailureException.class, error.getValue());
            }

            triggerFixedACLJob();
            // We expect at least one error to occur when threads were running
            assertTrue("There were no concurrency errors", errors.entrySet().size() > 0);

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
    @RetryAtMost(3)
    public void testAsyncConcurrentUpdateAndJob() throws Throwable
    {
        NodeRef folderRef = createFolderHierarchyInRootForFolderTests("testAsyncConcurrentUpdateAndJobFolder");
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
                    if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + group_prefix + i))
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

    private Long getChild(Long parentId)
    {
        List<FileInfo> children = fileFolderService.list(nodeDAO.getNodePair(parentId).getSecond());
        if (children.size() > 0)
        {
            NodeRef childRef = children.get(0).getNodeRef();
            return nodeDAO.getNodePair(childRef).getFirst();
        }

        return null;
    }

    private Long getAclOfFirstChild(Long nodeId)
    {
        Long firstChild = getChild(nodeId);
        if (firstChild != null)
        {
            return nodeDAO.getNodeAclId(firstChild);
        }
        return null;
    }

    private void setFixedPermissionsForTestGroup(NodeRef folderRef, String authName, int thread)
    {
        txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
            try
            {
                Thread.sleep(200);
                permissionService.setInheritParentPermissions(folderRef, false, true);
                permissionService.setPermission(folderRef, authName, PermissionService.COORDINATOR, true);
            }
            catch (Exception e)
            {
                errors.put(thread, e.getClass());
                throw e;
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

    private static void setACL(PermissionsDaoComponent permissionsDaoComponent, NodeRef nodeRef, long aclId)
    {
        if (permissionsDaoComponent instanceof ADMPermissionsDaoComponentImpl)
        {
            AccessControlListDAO acldao = ((ADMPermissionsDaoComponentImpl) permissionsDaoComponent).getACLDAO(nodeRef);
            if (acldao instanceof ADMAccessControlListDAO)
            {
                ADMAccessControlListDAO admAcLDao = (ADMAccessControlListDAO) acldao;
                admAcLDao.setAccessControlList(nodeRef, aclId);
            }
        }
    }

    private NodeRef createFolderHierarchyInRoot(String folderName, int[] filesPerLevel)
    {
        return txnHelper.doInTransaction((RetryingTransactionCallback<NodeRef>) () -> {
            NodeRef parent = createFile(fileFolderService, homeFolderNodeRef, folderName, ContentModel.TYPE_FOLDER);
            createFolderHierchy(fileFolderService, parent, 0, filesPerLevel);
            return parent;
        }, false, true);
    }

    private NodeRef createFolderHierarchyInRootForFolderTests(String folderName)
    {
        return createFolderHierarchyInRoot(folderName, filesPerLevelMoreFolders);
    }

    private NodeRef createFolderHierarchyInRootForFileTests(String folderName)
    {
        return createFolderHierarchyInRoot(folderName, filesPerLevelMoreFiles);
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
        txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {

            // create a group for tests
            Set<String> zones = new HashSet<String>(2, 1.0f);
            zones.add(AuthorityService.ZONE_APP_DEFAULT);
            if (!authorityService.authorityExists(TEST_GROUP_NAME_FULL))
            {
                authorityService.createAuthority(AuthorityType.GROUP, TEST_GROUP_NAME, TEST_GROUP_NAME, zones);
            }
            // kick it off by setting inherit parent permissions == false
            permissionService.setInheritParentPermissions(folderRef, false, asyncCall);
            permissionService.setPermission(folderRef, TEST_GROUP_NAME_FULL, DEFAULT_PERMISSION, true);
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
        triggerFixedACLJob(false);
    }

    private void triggerFixedACLJob(boolean forceSharedACL)
    {
        LOG.debug("Fixing ACL");
        final int rounds = 30;
        final int enoughZeros = 3;

        int numberOfConsecutiveZeros = 0;
        for (int round = 0; round < rounds; round++)
        {
            int count = txnHelper.doInTransaction(() -> {
                fixedAclUpdater.setForceSharedACL(forceSharedACL);
                return fixedAclUpdater.execute();
            }, false, true);
            numberOfConsecutiveZeros = count == 0 ? numberOfConsecutiveZeros + 1 : 0;
            if (numberOfConsecutiveZeros == enoughZeros)
            {
                LOG.info("ACL has been fixed in {} rounds", round);
                return;
            }
        }
        LOG.warn("Haven't fixed ACL in {} rounds.", rounds);
    }

    private NodeRef getFirstNodeWithAclPending(QName nodeType, NodeRef parentRef)
    {
        return txnHelper.doInTransaction((RetryingTransactionCallback<NodeRef>) () -> {
            final GetNodesWithAspectCallback getNodesCallback = new GetNodesWithAspectCallback();
            nodeDAO.getNodesWithAspects(Collections.singleton(ContentModel.ASPECT_PENDING_FIX_ACL), 0l, null, getNodesCallback);
            List<NodeRef> nodesWithAclPendingAspect = getNodesCallback.getNodes();
            for (int i = 0; i < nodesWithAclPendingAspect.size(); i++)
            {
                NodeRef nodeRef = nodesWithAclPendingAspect.get(i);
                boolean isDescendent = false;
                List<FileInfo> path = fileFolderService.getNamePath(homeFolderNodeRef, nodeRef);
                for (FileInfo element : path)
                {
                    if (element.getNodeRef().equals(parentRef))
                    {
                        isDescendent = true;
                    }
                }

                if (isDescendent && nodeDAO.getNodeType(nodeDAO.getNodePair(nodeRef).getFirst()).equals(nodeType))
                {
                    // If folder, the tests will need a child and a grandchild to verify permissions
                    if (nodeType.equals(ContentModel.TYPE_FOLDER) && !hasGrandChilden(nodeRef)) {
                        continue;
                    }
                    return nodeRef;
                }
            }
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
                    // If folder, the tests will need a child and a grandchild to verify permissions
                    if (nodeType.equals(ContentModel.TYPE_FOLDER) && !hasGrandChilden(nodeRef)) {
                        continue;
                    }
                    return nodeRef;
                }
            }
            return null;
        }, false, true);

    }

    private boolean hasGrandChilden(NodeRef nodeRef)
    {
        Long nodeId = nodeDAO.getNodePair(nodeRef).getFirst();
        Long childId = getChild(nodeId);
        Long grandChild = null;
        if (childId != null)
        {
            grandChild = getChild(childId);
        }
        return (grandChild != null);
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

    private class ACLComparator
    {
        Long parentId;
        Long oParentACL;
        Long oFirstChildACL;
        Long tParentACL;
        Long tFirstChildACL;
        String originalPermission = DEFAULT_PERMISSION;
        String originalAuthority = TEST_GROUP_NAME_FULL;
        Long pendingInheritFrom = 0L;

        public ACLComparator(NodeRef nodeRef)
        {
            Long parentId = nodeDAO.getNodePair(nodeRef).getFirst();
            this.parentId = parentId;
            this.oParentACL = nodeDAO.getNodeAclId(parentId);
            this.oFirstChildACL = getAclOfFirstChild(parentId);
            updateCurrentACLs();
        }

        public void updateCurrentACLs()
        {
            this.tParentACL = nodeDAO.getNodeAclId(parentId);
            this.tFirstChildACL = getAclOfFirstChild(parentId);

            if (nodeDAO.hasNodeAspect(parentId, ContentModel.ASPECT_PENDING_FIX_ACL))
            {
                this.pendingInheritFrom = (Long) nodeDAO.getNodeProperty(parentId, ContentModel.PROP_INHERIT_FROM_ACL);
            }
            else
            {
                this.pendingInheritFrom = 0L;
            }
        }

        public void compareACLs()
        {
            updateCurrentACLs();
            assertTrue("Permissions were not changed on top folder", !oParentACL.equals(tParentACL));
            assertTrue("Permissions were not changed on child", !oFirstChildACL.equals(tFirstChildACL));
        }

        public void setOriginalPermission(String originalAuthority, String originalPermission)
        {
            this.originalPermission = originalPermission;
            this.originalAuthority = originalAuthority;
        }

        public boolean hasPermission(String authority, String permission)
        {
            return hasPermission(parentId, authority, permission);
        }

        public boolean hasPermission(Long nodeId, String authority, String permission)
        {
            boolean hasExpectedPermission = false;
            Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeDAO.getNodePair(nodeId).getSecond());
            for (Iterator<AccessPermission> iterator = permissions.iterator(); iterator.hasNext();)
            {
                AccessPermission accessPermission = (AccessPermission) iterator.next();
                if (accessPermission.getPermission().equalsIgnoreCase(permission)
                        && accessPermission.getAuthority().equalsIgnoreCase(authority))
                {
                    hasExpectedPermission = true;
                    break;
                }
            }
            return hasExpectedPermission;
        }

        public boolean firstChildHasPermission(String authority, String permission)
        {
            return hasPermission(getChild(parentId), authority, permission);
        }

        public boolean firstChildHasOriginalPermission()
        {
            return hasPermission(getChild(parentId), originalAuthority, originalPermission);
        }

        public boolean parentHasOriginalPermission()
        {
            return hasPermission(originalAuthority, originalPermission);
        }

        private Long getParentAcl()
        {
            return tParentACL;
        }

        private Long getChildAcl()
        {
            return tFirstChildACL;
        }

        private Long getPendingInheritFromAcl()
        {
            return pendingInheritFrom;
        }
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
                NodeRef node = createFile(fileFolderService, parent, "-LVL" + level + i, ContentModel.TYPE_FOLDER);
                createFolderHierchy(fileFolderService, node, level + 1, filesPerLevel);
            }
        }
        // last level
        else if (level == levels - 1)
        {
            int numFiles = filesPerLevel[level];
            for (int i = 0; i < numFiles; i++)
            {
                createFile(fileFolderService, parent, "-File" + i, ContentModel.TYPE_CONTENT);
            }
        }
    }
}
