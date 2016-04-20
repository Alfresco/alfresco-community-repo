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

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.impl.PermissionsDaoComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ArgumentHelper;
import org.alfresco.util.Pair;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Test class for {@link FixedAclUpdater}
 * 
 * @author Andreea Dragoi
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
    private NodeRef folderNodeRef;
    private PermissionsDaoComponent permissionsDaoComponent;
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
        nodeDAO = (NodeDAO) ctx.getBean("nodeDAO");

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        NodeRef home = repository.getCompanyHome();
        // create a folder hierarchy for which will change permission inheritance
        int[] filesPerLevel = { 5, 5, 10 };
        RetryingTransactionCallback<NodeRef> cb = createFolderHierchyCallback(home, fileFolderService, "ROOT", filesPerLevel);
        folderNodeRef = txnHelper.doInTransaction(cb);

        // change setFixedAclMaxTransactionTime to lower value so setInheritParentPermissions on created folder hierarchy require async call
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
            txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    Set<QName> aspect = new HashSet<>();
                    aspect.add(ContentModel.ASPECT_TEMPORARY);
                    nodeDAO.addNodeAspects(nodeDAO.getNodePair(folderNodeRef).getFirst(), aspect);
                    fileFolderService.delete(folderNodeRef);
                    return null;
                }
            });
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
        final Set<QName> aspects = new HashSet<>(1);
        aspects.add(ContentModel.ASPECT_PENDING_FIX_ACL);
        GetNodesCountWithAspectCallback callback = new GetNodesCountWithAspectCallback();
        nodeDAO.getNodesWithAspects(aspects, 1L, null, callback);
        return callback.getNodesNumber();
    }

    @Test
    public void testMNT15368()
    {
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // call setInheritParentPermissions on a node that will required async
                AlfrescoTransactionSupport.bindResource(FixedAclUpdater.FIXED_ACL_ASYNC_CALL_KEY, true);
                permissionsDaoComponent.setInheritParentPermissions(folderNodeRef, false);

                Boolean asyncCallRequired = (Boolean) AlfrescoTransactionSupport.getResource(FixedAclUpdater.FIXED_ACL_ASYNC_REQUIRED_KEY);
                if (asyncCallRequired != null && asyncCallRequired)
                {
                    // check if there are nodes with ASPECT_PENDING_FIX_ACL
                    assertTrue(" No nodes with pending aspect", getNodesCountWithPendingFixedAclAspect() > 0);
                    AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter()
                    {
                        @Override
                        public void afterCommit()
                        {
                            // start fixedAclUpdater
                            Thread t = new Thread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    fixedAclUpdater.execute();
                                }
                            });
                            t.start();
                            try
                            {
                                // wait to finish work
                                t.join();
                            }
                            catch (InterruptedException e)
                            {
                            }
                        }
                    });
                }
                return null;
            }
        });
        // check if nodes with ASPECT_PENDING_FIX_ACL are processed
        assertTrue("Not all nodes were processed", getNodesCountWithPendingFixedAclAspect() == 0);

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
     * Creates a level in folder/file hierarchy. Intermediate levels will
     * contain folders and last ones files
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

    /**
     * Create a folder hierarchy and start FixedAclUpdater. See {@link #getUsage()} for usage parameters 
     */
    public static void main(String... args)
    {
        ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();
        try
        {
            run(ctx, args);
        }
        catch (Exception e)
        {
            System.out.println("Failed to run FixedAclUpdaterTest  test");
            e.printStackTrace();
        }
        finally
        {
            ctx.close();
        }
    }

    public static void run(final ApplicationContext ctx, String... args) throws InterruptedException
    {
        ArgumentHelper argHelper = new ArgumentHelper(getUsage(), args);
        int threadCount = argHelper.getIntegerValue("threads", true, 1, 100);
        String levels[] = argHelper.getStringValue("filesPerLevel", true, true).split(",");
        int fixedAclMaxTransactionTime = argHelper.getIntegerValue("fixedAclMaxTransactionTime", true, 1, 10000);
        final int[] filesPerLevel = new int[levels.length];
        for (int i = 0; i < levels.length; i++)
        {
            filesPerLevel[i] = Integer.parseInt(levels[i]);
        }

        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        final RetryingTransactionHelper txnHelper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();
        final FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        Repository repository = (Repository) ctx.getBean("repositoryHelper");
        final FixedAclUpdater fixedAclUpdater = (FixedAclUpdater) ctx.getBean("fixedAclUpdater");
        final PermissionsDaoComponent permissionsDaoComponent = (PermissionsDaoComponent) ctx.getBean("admPermissionsDaoComponent");

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        NodeRef home = repository.getCompanyHome();
        final NodeRef root = createFile(fileFolderService, home, "ROOT", ContentModel.TYPE_FOLDER);

        // create a folder hierarchy for which will change permission inheritance
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            final int index = i;
            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    AuthenticationUtil.runAs(new RunAsWork<Void>()
                    {
                        @Override
                        public Void doWork() throws Exception
                        {
                            
                            RetryingTransactionCallback<NodeRef> cb = createFolderHierchyCallback(root, fileFolderService, "FOLDER" + index, filesPerLevel);
                            txnHelper.doInTransaction(cb);
                            return null;
                        }
                    }, AuthenticationUtil.getSystemUserName());
                }
            });
            t.start();
            threads[i] = t;
        }
        for (int i = 0; i < threads.length; i++)
        {
            threads[i].join();
        }

        setFixedAclMaxTransactionTime(permissionsDaoComponent, home, fixedAclMaxTransactionTime);

        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // call setInheritParentPermissions on a node that will required async
                AlfrescoTransactionSupport.bindResource(FixedAclUpdater.FIXED_ACL_ASYNC_CALL_KEY, true);
                final long startTime = System.currentTimeMillis();
                permissionsDaoComponent.setInheritParentPermissions(root, false);

                Boolean asyncCallRequired = (Boolean) AlfrescoTransactionSupport.getResource(FixedAclUpdater.FIXED_ACL_ASYNC_REQUIRED_KEY);
                if (asyncCallRequired != null && asyncCallRequired)
                {
                    // check if there are nodes with ASPECT_PENDING_FIX_ACL
                    AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter()
                    {
                        @Override
                        public void afterCommit()
                        {
                            long userEndTime = System.currentTimeMillis();
                            // start fixedAclUpdater
                            Thread t = new Thread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    fixedAclUpdater.execute();
                                }
                            });
                            t.start();
                            try
                            {
                                // wait to finish work
                                t.join();
                                System.out.println("Backend time " + (System.currentTimeMillis() - startTime));
                                System.out.println("User time " + (userEndTime - startTime));
                            }
                            catch (InterruptedException e)
                            {
                            }
                        }
                    });
                }
                return null;
            }
        }, false, true);
    }
    
    private static String getUsage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("FixedAclUpdaterTest usage: ").append("\n");
        sb.append("   FixedAclUpdaterTest --threads=<threadcount> --fixedAclMaxTransactionTime=<maxtime> --filesPerLevel=<levelfiles>").append("\n");
        sb.append("      maxtime: max transaction time for fixed acl ").append("\n");
        sb.append("      threadcount: number of threads to create the folder hierarchy ").append("\n");
        sb.append("      levelfiles: number of folders/files per level separated by comma").append("\n");
        return sb.toString();
    }
}
