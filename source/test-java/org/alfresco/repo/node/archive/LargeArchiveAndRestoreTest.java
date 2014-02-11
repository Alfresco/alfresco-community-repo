/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.node.archive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * Test large datasets' archival and restore
 * 
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
public class LargeArchiveAndRestoreTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private static Log logger = LogFactory.getLog(LargeArchiveAndRestoreTest.class);
    
    private NodeService nodeService;
    private NodeArchiveService nodeArchiveService;
    private FileFolderService fileFolderService;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    
    private NodeRef rootNodeRef;

    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        nodeArchiveService = (NodeArchiveService) ctx.getBean("nodeArchiveService");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        transactionService = serviceRegistry.getTransactionService();
        
        try
        {
            authenticationComponent.setSystemUserAsCurrentUser();
            
            // create a test store
            StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
            rootNodeRef = nodeService.getRootNode(storeRef);
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
    }
    
    @Override
    public void tearDown() throws Exception
    {
    }
    
    public void testSetUp() throws Exception
    {
    }

    private static final int NUM_FOLDERS = 5;
    private static final int MAX_DEPTH = 3;
    private static final int NUM_FILES = 10;
    private class CreateDataCallback implements RetryingTransactionCallback<NodeRef>
    {
        private NodeRef parentNodeRef;
        private List<String> rollbackMessages;
        
        public CreateDataCallback(NodeRef parentNodeRef)
        {
            this.parentNodeRef = parentNodeRef;
            this.rollbackMessages = new ArrayList<String>(20);
        }
        
        public NodeRef execute() throws Throwable
        {
            // Make the root of the tree
            String foldername = String.format("FOLDER-%04d-%04d", 0, 0);
            System.out.println("Creating folder " + foldername + " in parent " + parentNodeRef);
            PropertyMap props = new PropertyMap();
            props.put(ContentModel.PROP_NAME, "foldername");
            NodeRef folderNodeRef = nodeService.createNode(
                    parentNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.TYPE_FOLDER,
                    props).getChildRef();
            // Now add children
            makeFolders(folderNodeRef, 1);
            
            // Rollback if necessary
            if (rollbackMessages.size() > 0)
            {
                StringBuilder sb = new StringBuilder(1024);
                sb.append("Errors during create: \n");
                for (String msg : rollbackMessages)
                {
                    sb.append("   ").append(msg);
                }
                throw new RuntimeException(sb.toString());
            }
            
            return folderNodeRef;
        }
        
        private void makeFolders(NodeRef parentNodeRef, int depth)
        {
            if (depth == MAX_DEPTH)
            {
                // We're deep enough, so add the files
                for (int i = 0; i < NUM_FILES; i++)
                {
                    String filename = String.format("FILE-%04d-%04d", depth, i);
                    addFile(parentNodeRef, filename, depth);
                }
            }
            else
            {
                int nextDepth = depth + 1;
                // Not deep enough yet, so add directories
                for (int i = 0; i < NUM_FOLDERS; i++)
                {
                    // Create the directory
                    String foldername = String.format("FOLDER-%04d-%04d", depth, i);
                    NodeRef folderNodeRef = addFolder(parentNodeRef, foldername, depth);
                    // Recurse
                    makeFolders(folderNodeRef, nextDepth);
                }
            }
        }
        
        private NodeRef addFolder(NodeRef parentNodeRef, String foldername, int depth)
        {
            System.out.println(makeTabs(depth) + "Creating folder " + foldername + " in parent " + parentNodeRef);
            FileInfo info = fileFolderService.create(parentNodeRef, foldername, ContentModel.TYPE_FOLDER);
            String name = info.getName();
            if (!name.equals(foldername))
            {
                String msg = "A foldername '" + foldername + "' was not persisted: " + info;
                logger.error(msg);
                rollbackMessages.add(msg);
            }
            return info.getNodeRef();
        }
        
        private NodeRef addFile(NodeRef parentNodeRef, String filename, int depth)
        {
            System.out.println(makeTabs(depth) + "Creating file " + filename + " in parent " + parentNodeRef);
            FileInfo info = fileFolderService.create(parentNodeRef, filename, ContentModel.TYPE_CONTENT);
            String name = info.getName();
            if (!name.equals(filename))
            {
                String msg = "A filename '" + filename + "' was not persisted: " + info;
                logger.error(msg);
                rollbackMessages.add(msg);
            }
            return info.getNodeRef();
        }
        
        private String makeTabs(int count)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++)
            {
                sb.append("   ");
            }
            return sb.toString();
        }
    }
    
    public void testCreateAndRestore() throws Exception
    {
        RunAsWork<NodeRef> createHierarchyWork = new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                CreateDataCallback callback = new CreateDataCallback(rootNodeRef);
                return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
            }
        };
        // Create the hierarchy
        final NodeRef parentNodeRef = AuthenticationUtil.runAs(createHierarchyWork, AuthenticationUtil.SYSTEM_USER_NAME);
        // Delete it
        final RetryingTransactionCallback<NodeRef> deleteHierarchyCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Delete it
                fileFolderService.delete(parentNodeRef);
                // Now try to find the archived node
                NodeRef archivedParentNodeRef = new NodeRef("archive", parentNodeRef.getStoreRef().getIdentifier(), parentNodeRef.getId());
                // Check it
                if (!nodeService.exists(archivedParentNodeRef))
                {
                    throw new InvalidNodeRefException("Archived node not found after delete: " + archivedParentNodeRef, archivedParentNodeRef);
                }
                if (nodeService.exists(parentNodeRef))
                {
                    throw new InvalidNodeRefException("Original node was found after delete: " + parentNodeRef, parentNodeRef);
                }
                return archivedParentNodeRef;
            }
        };
        RunAsWork<NodeRef> deleteHierarchyWork = new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                return transactionService.getRetryingTransactionHelper().doInTransaction(deleteHierarchyCallback);
            }
        };
        final NodeRef archivedParentNodeRef = AuthenticationUtil.runAs(deleteHierarchyWork, AuthenticationUtil.SYSTEM_USER_NAME);
        // Restore it
        final RetryingTransactionCallback<NodeRef> restoreHierarchyCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Delete it
                List<RestoreNodeReport> report = nodeArchiveService.restoreArchivedNodes(Collections.singletonList(archivedParentNodeRef));
                // Dump the report
                System.out.println("Restore report: \n" + report);
                // Check it
                if (nodeService.exists(archivedParentNodeRef))
                {
                    throw new InvalidNodeRefException("Archived node was found after restore: " + archivedParentNodeRef, archivedParentNodeRef);
                }
                if (!nodeService.exists(parentNodeRef))
                {
                    throw new InvalidNodeRefException("Original node was not found after restore: " + parentNodeRef, parentNodeRef);
                }
                return parentNodeRef;
            }
        };
        RunAsWork<NodeRef> restoreHierarchyWork = new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                return transactionService.getRetryingTransactionHelper().doInTransaction(restoreHierarchyCallback);
            }
        };
        final NodeRef checkParentNodeRef = AuthenticationUtil.runAs(restoreHierarchyWork, AuthenticationUtil.SYSTEM_USER_NAME);
        assertEquals("Restored node reference doesn't match original", parentNodeRef, checkParentNodeRef);
        
        // Purge it
        final RetryingTransactionCallback<Object> purgeHierarchyCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Delete it
                nodeService.addAspect(parentNodeRef, ContentModel.ASPECT_TEMPORARY, null);
                fileFolderService.delete(parentNodeRef);
                // Now try to find the archived node
                NodeRef archivedParentNodeRef = new NodeRef("archive", parentNodeRef.getStoreRef().getIdentifier(), parentNodeRef.getId());
                // Check it
                if (nodeService.exists(archivedParentNodeRef))
                {
                    throw new InvalidNodeRefException("Node not purged by delete: " + archivedParentNodeRef, archivedParentNodeRef);
                }
                if (nodeService.exists(parentNodeRef))
                {
                    throw new InvalidNodeRefException("Original node was found after purge: " + parentNodeRef, parentNodeRef);
                }
                return null;
            }
        };
        RunAsWork<Object> purgeHierarchyWork = new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(purgeHierarchyCallback);
                return null;
            }
        };
        AuthenticationUtil.runAs(purgeHierarchyWork, AuthenticationUtil.SYSTEM_USER_NAME);
    }
}
