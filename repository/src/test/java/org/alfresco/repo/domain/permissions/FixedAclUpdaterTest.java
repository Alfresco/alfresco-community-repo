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

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.impl.PermissionsDaoComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ArgumentHelper;
import org.alfresco.util.Pair;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

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
    private NodeRef folderAsyncCallNodeRef;
    private NodeRef folderSyncCallNodeRef;
    private PermissionsDaoComponent permissionsDaoComponent;
    private PermissionService permissionService;
    private NodeDAO nodeDAO;

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

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        NodeRef home = repository.getCompanyHome();
        // create a folder hierarchy for which will change permission inheritance
        int[] filesPerLevel = { 5, 5, 10 };
        RetryingTransactionCallback<NodeRef> cb1 = createFolderHierchyCallback(home, fileFolderService, "rootFolderAsyncCall",
                filesPerLevel);
        folderAsyncCallNodeRef = txnHelper.doInTransaction(cb1);

        RetryingTransactionCallback<NodeRef> cb2 = createFolderHierchyCallback(home, fileFolderService, "rootFolderSyncCall",
                filesPerLevel);
        folderSyncCallNodeRef = txnHelper.doInTransaction(cb2);

        // change setFixedAclMaxTransactionTime to lower value so setInheritParentPermissions on created folder
        // hierarchy require async call
        setFixedAclMaxTransactionTime(permissionsDaoComponent, home, 50);

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

    private static RetryingTransactionCallback<NodeRef> createFolderHierchyCallback(final NodeRef root,
            final FileFolderService fileFolderService, final String rootName, final int[] filesPerLevel)
    {
        RetryingTransactionCallback<NodeRef> cb = new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                NodeRef parent = createFile(fileFolderService, root, rootName, ContentModel.TYPE_FOLDER);
                createFolderHierchy(fileFolderService, parent, 0, filesPerLevel);
                return parent;
            }
        };
        return cb;
    }

    @Override
    public void tearDown() throws Exception
    {
        // delete created folder hierarchy
        try
        {
            txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
                Set<QName> aspect = new HashSet<>();
                aspect.add(ContentModel.ASPECT_TEMPORARY);
                nodeDAO.addNodeAspects(nodeDAO.getNodePair(folderAsyncCallNodeRef).getFirst(), aspect);
                nodeDAO.addNodeAspects(nodeDAO.getNodePair(folderSyncCallNodeRef).getFirst(), aspect);
                fileFolderService.delete(folderAsyncCallNodeRef);
                fileFolderService.delete(folderSyncCallNodeRef);
                return null;
            }, false, true);
        }
        catch (Exception e)
        {
        }
        AuthenticationUtil.clearCurrentSecurityContext();
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

    @Test
    public void testSyncTimeOut()
    {
        testWork(folderSyncCallNodeRef, false);
    }

    @Test
    public void testAsync()
    {
        testWork(folderAsyncCallNodeRef, true);
    }

    private void testWork(NodeRef folderRef, boolean asyncCall)
    {
        // kick it off by setting inherit parent permissions == false
        txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
            permissionService.setInheritParentPermissions(folderRef, false, asyncCall);
            assertTrue("asyncCallRequired should be true", isFixedAclAsyncRequired());
            return null;
        }, false, true);

        // Assert that there are nodes with aspect ASPECT_PENDING_FIX_ACL to be processed
        txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
            assertTrue("There are no nodes to process", getNodesCountWithPendingFixedAclAspect() > 0);
            return null;
        }, false, true);

        // run the fixedAclUpdater until there is nothing more to fix (running the updater
        // may create more to fix up)
        txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
            int count = 0;
            do
            {
                count = fixedAclUpdater.execute();
            } while (count > 0);
            return null;
        }, false, true);

        // check if nodes with ASPECT_PENDING_FIX_ACL are processed
        txnHelper.doInTransaction((RetryingTransactionCallback<Void>) () -> {
            assertEquals("Not all nodes were processed", 0, getNodesCountWithPendingFixedAclAspect());
            return null;
        }, false, true);
    }

    private static boolean isFixedAclAsyncRequired()
    {
        if (AlfrescoTransactionSupport.getResource(FixedAclUpdater.FIXED_ACL_ASYNC_REQUIRED_KEY) == null)
        {
            return false;
        }
        return (Boolean) AlfrescoTransactionSupport.getResource(FixedAclUpdater.FIXED_ACL_ASYNC_REQUIRED_KEY);
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
