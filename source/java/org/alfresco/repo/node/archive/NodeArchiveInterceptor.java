/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.node.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.StoreArchiveMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.VmShutdownListener;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An interceptor to handle handle the deletion of nodes.  This allows
 * deletion and archival process to be pushed into the background.
 *
 * @since 2.1
 * @author Derek Hulley
 */
public class NodeArchiveInterceptor extends TransactionListenerAdapter implements MethodInterceptor
{
    private static VmShutdownListener shutdownListener = new VmShutdownListener("NodeArchiveInterceptor");
    
    private static final Set<String> INBOUND_FIRST_ARG = new HashSet<String>(17);
    private static final Set<String> INBOUND_SECOND_ARG = new HashSet<String>(17);
    static
    {
        // First arguments
        INBOUND_FIRST_ARG.add("getNodeStatus");
        INBOUND_FIRST_ARG.add("createNode");
        INBOUND_FIRST_ARG.add("moveNode");
        INBOUND_FIRST_ARG.add("getType");
        INBOUND_FIRST_ARG.add("setType");
        INBOUND_FIRST_ARG.add("addAspect");
        INBOUND_FIRST_ARG.add("removeAspect");
        INBOUND_FIRST_ARG.add("hasAspect");
        INBOUND_FIRST_ARG.add("getAspects");
        INBOUND_FIRST_ARG.add("addChild");
        INBOUND_FIRST_ARG.add("removeChild");
        INBOUND_FIRST_ARG.add("getProperties");
        INBOUND_FIRST_ARG.add("getProperty");
        INBOUND_FIRST_ARG.add("setProperties");
        INBOUND_FIRST_ARG.add("setProperty");
        INBOUND_FIRST_ARG.add("removeProperty");
        INBOUND_FIRST_ARG.add("getParentAssocs");
        INBOUND_FIRST_ARG.add("getChildAssocs");
        INBOUND_FIRST_ARG.add("getChildByName");
        INBOUND_FIRST_ARG.add("getPrimaryParent");
        INBOUND_FIRST_ARG.add("createAssociation");
        INBOUND_FIRST_ARG.add("removeAssociation");
        INBOUND_FIRST_ARG.add("getTargetAssocs");
        INBOUND_FIRST_ARG.add("getSourceAssocs");
        INBOUND_FIRST_ARG.add("getPath");
        INBOUND_FIRST_ARG.add("getPaths");
        INBOUND_FIRST_ARG.add("restoreNode");
        // Second arguments
        INBOUND_SECOND_ARG.add("moveNode");
        INBOUND_SECOND_ARG.add("addChild");
        INBOUND_SECOND_ARG.add("removeChild");
        INBOUND_SECOND_ARG.add("createAssociation");
        INBOUND_SECOND_ARG.add("removeAssociation");
        INBOUND_SECOND_ARG.add("restoreNode");
    }

    /** A key for storing in-transaction values */
    private static final String KEY_DELETE_WORKERS = "NodeArchiveInterceptor.DeleteNodeWorkers";
    
    private static Log logger = LogFactory.getLog(NodeArchiveInterceptor.class);
    private static boolean isDebugEnabled = logger.isDebugEnabled();
    
    /**
     * An archival strategy to follow.
     * @since 2.1
     * @author Derek Hulley
     */
    public static enum ArchiveMode
    {
        /**
         * Node archival will be done immediately within the current transaction.
         */
        EAGER,
        /**
         * Node archival, where archival is going to occur, will be pushed onto a background
         * process.
         */
        LAZY
    }
    
    /** Used to ensure that the interceptor isn't in a configuration endless loop */
    private ThreadLocal<Boolean> deleting = new ThreadLocal<Boolean>();
    
    /** Used for running background deletes */
    private TransactionService transactionService;
    /** Direct access to the NodeService */
    private NodeService nodeService;
    /** Used to access property definitions */
    private DictionaryService dictionaryService;
    /** A map of stores to send archived nodes to */
    private StoreArchiveMap storeArchiveMap;
    /** Helper to perform background deletes */
    private ThreadPoolExecutor threadPoolExecutor;
    /** The archival timing */
    private ArchiveMode archiveMode;

    /**
     * Default constructor
     */
    public NodeArchiveInterceptor()
    {
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);               // Just to be explicit
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();                // Just to be explicit
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param nodeService       the NodeService that doesn't include this interceptor
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setStoreArchiveMap(StoreArchiveMap storeArchiveMap)
    {
        this.storeArchiveMap = storeArchiveMap;
    }
    
    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor)
    {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void setArchiveMode(ArchiveMode archiveMode)
    {
        this.archiveMode = archiveMode;
    }

    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        Object ret = null;
        
        String methodName = invocation.getMethod().getName();
        Object[] args = invocation.getArguments();

        if (methodName.equals("deleteNode"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            // Handle the deletion
            boolean deleted = handleDeleteNode(nodeRef);
            ret = Boolean.valueOf(deleted);
        }
//        else if (methodName.equals("exists"))
//        {
//            if (args[0] instanceof NodeRef)
//            {
//                NodeRef nodeRef = (NodeRef) args[0];
//                if (nodeService.exists(nodeRef))
//                {
//                    if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_DELETED_NODE))
//                    {
//                        // It really exists, but shouldn't be visible
//                        ret = Boolean.FALSE;
//                    }
//                    else
//                    {
//                        ret = Boolean.TRUE;
//                    }
//                }
//                else
//                {
//                    ret = Boolean.FALSE;
//                }
//            }
//            else
//            {
//                ret = invocation.proceed();
//            }
//        }
        else
        {
            // All other methods will be checked for 'real' deletion.  We post-process
            // the successful methods as required so that we don't unnecessarily check
            // for missing nodes when it would be picked up anyway.
            ret = invocation.proceed();
            
        }
//        // Check first argument
//        if (INBOUND_FIRST_ARG.contains(methodName))
//        {
//            checkNodeForDeleteMarker((NodeRef)args[0]);
//        }
//        // Check seconds argument
//        if (INBOUND_SECOND_ARG.contains(methodName))
//        {
//            checkNodeForDeleteMarker((NodeRef)args[1]);
//        }
        
        // done
        return ret;
    }
    
//    /**
//     * Check if the node should be treated as invalid due to a deletion
//     * 
//     * @param nodeRef           the node to check
//     * @throws InvalidNodeRefException
//     *                          if the node has the <b>sys:deleted</b> aspect
//     */
//    private void checkNodeForDeleteMarker(NodeRef nodeRef)
//    {
//        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_DELETED_NODE))
//        {
//            throw new InvalidNodeRefException("Node has been deleted: " + nodeRef, nodeRef);
//        }
//    }
//    
//    /**
//     * 
//     * @param nodeRef           the node to check
//     * @return                  Returns <tt>true</tt> if the node has the <b>sys:deleted</b> aspect
//     */
//    private boolean isDeleted(NodeRef nodeRef)
//    {
//        return nodeService.hasAspect(nodeRef, ContentModel.ASPECT_DELETED_NODE);
//    }
//    
    /**
     * Determines whether the node can be archived.
     * 
     * @param nodeRef       the node to check
     * @return              Returns <tt>true</tt> if the node can be archived
     */
    private boolean isArchivable(NodeRef nodeRef)
    {
        // Temporary nodes can't be archived
        boolean isTemporary = nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY);
        if (isTemporary)
        {
            return false;
        }
        // Check that the store has an associated archive store
        StoreRef storeRef = nodeRef.getStoreRef();
        if (!storeArchiveMap.getArchiveMap().containsKey(storeRef))
        {
            // There is no mapping for the store
            return false;
        }
        // Check the type
        QName nodeTypeQName = nodeService.getType(nodeRef);
        TypeDefinition typeDef = dictionaryService.getType(nodeTypeQName);
        if (typeDef == null || !typeDef.isArchive())
        {
            // It is not an archivable type
            return false;
        }
        // Otherwise it can be archived
        return true;
    }
    
    /**
     * Performs a real delete, whilst ensuring that the interceptor doesn't get into an
     * infinite loop in the case of a configuration error.
     * 
     * @param nodeRef       the node to delete
     */
    private boolean deleteNodeDirectly(NodeRef nodeRef)
    {
        // Catch the infinite loop
        if (deleting.get() == Boolean.TRUE)     // Handles null and TRUE
        {
            throw new AlfrescoRuntimeException(
                    "The NodeArchiveInterceptor must be given a " +
                    "NodeService that is not similarly intercepted.");
        }
        try
        {
            deleting.set(Boolean.TRUE);
            // It can really be deleted
            return nodeService.deleteNode(nodeRef);
        }
        finally
        {
            deleting.set(Boolean.FALSE);
        }
    }
    
    /**
     * Get the worker runnables that need to be executed after the current transaction has committed.
     * 
     * @return          Returns a list of delete node workers
     */
    private List<BackgroundDeleteRunner> getDeleteWorkers()
    {
        @SuppressWarnings("unchecked")
        List<BackgroundDeleteRunner> deleteRunners =
            (List<BackgroundDeleteRunner>) AlfrescoTransactionSupport.getResource(KEY_DELETE_WORKERS);
        if (deleteRunners == null)
        {
            // It is not bound, yet
            deleteRunners = new ArrayList<BackgroundDeleteRunner>(20);
            AlfrescoTransactionSupport.bindResource(KEY_DELETE_WORKERS, deleteRunners);
        }
        return deleteRunners;
    }
    
    private boolean handleDeleteNode(NodeRef nodeRef) throws Throwable
    {
        boolean deleteDirect = false;
        // If the node is not archivable, then we delete it inline
        boolean isArchivable = isArchivable(nodeRef);
        if (!isArchivable)
        {
            deleteDirect = true;
            if (isDebugEnabled)
            {
                logger.debug("\n" +
                        "Deleted node directly as it is not archivable: \n" +
                        "   Node: " + nodeRef + "\n" +
                        "   Type: " + nodeService.getType(nodeRef));
            }
        }
        // Check the archive mode
        if (archiveMode == ArchiveMode.EAGER)
        {
            deleteDirect = true;
            if (isDebugEnabled)
            {
                logger.debug("\n" +
                        "Deleted node directly due to archive mode: \n" +
                        "   Node: " + nodeRef + "\n" +
                        "   Mode: " + archiveMode);
            }
        }
        // When must we the nodes?
        if (deleteDirect)
        {
            return deleteNodeDirectly(nodeRef);
        }
        else
        {
            try
            {
                if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_DELETED_NODE))
                {
                    // We need to keep the node's original name for later use
                    Serializable name = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                    String currentUser = AuthenticationUtil.getCurrentUserName();
                    // Add the sys:deletedNode aspect
                    PropertyMap properties = new PropertyMap();
                    properties.put(ContentModel.PROP_DELETED_NODE_ORIGINAL_NAME, name);
                    properties.put(ContentModel.PROP_DELETED_NODE_USER, currentUser);
                    nodeService.addAspect(nodeRef, ContentModel.ASPECT_DELETED_NODE, properties);
                    // Now rename the node to a random name
                    String guid = GUID.generate();
                    nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, guid);
                }
                // Store it for later deletion
                BackgroundDeleteRunner backgroundDeleteRunner = new BackgroundDeleteRunner(nodeRef);
                getDeleteWorkers().add(backgroundDeleteRunner);
                
                // Register this instance as a listener on the transaction
                AlfrescoTransactionSupport.bindListener(this);
            }
            catch(Throwable e)
            {
                e.printStackTrace();
                throw e;
            }
            
            if (isDebugEnabled)
            {
                logger.debug("\n" +
                        "Queued node deletion for post-transaction processing: \n" +
                        "   Node: " + nodeRef);
            }
            
            return false;
        }
    }
    
    /**
     * Checks if there are any nodes that were earmarked for deletion.  These are then
     * pushed onto an execution queue to be handled in the background.  What we are sure
     * of is that any nodes that were created have been committed by the transaction
     * that has just ended.
     */
    public void afterCommit()
    {
        // Get the list of nodes
        List<BackgroundDeleteRunner> deleteWorkers = getDeleteWorkers();
        for (BackgroundDeleteRunner deleteWorker : deleteWorkers)
        {
            // Push the node onto the execution queue
            threadPoolExecutor.submit(deleteWorker);
        }
    }

    /**
     * A worker class that is able to delete a node, on behalf of a particular user, as a background
     * task.
     * 
     * @since 2.1
     * @author Derek Hulley
     */
    private class BackgroundDeleteRunner implements Runnable
    {
        private NodeRef nodeRef;
        
        /**
         * @param nodeRef           the node to delete
         */
        public BackgroundDeleteRunner(NodeRef nodeRef)
        {
            this.nodeRef = nodeRef;
        }
        public void run()
        {
            // Transaction wrapper
            RetryingTransactionCallback<Object> deleteTxnCallback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    // Determine if the execution should proceed
                    RunAsWork<Boolean> getContinueAuthCallback = new RunAsWork<Boolean>()
                    {
                        public Boolean doWork() throws Exception
                        {
                            if (nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef, ContentModel.ASPECT_DELETED_NODE))
                            {
                                return Boolean.TRUE;
                            }
                            else
                            {
                                return Boolean.FALSE;
                            }
                        }
                    };
                    Boolean mustContinue = AuthenticationUtil.runAs(getContinueAuthCallback, AuthenticationUtil.SYSTEM_USER_NAME);
                    if (mustContinue == Boolean.FALSE)
                    {
                        if (isDebugEnabled)
                        {
                            logger.debug("\n" +
                                    "Queued deletion stopped.  The node is no longer marked for deletion or no longer exists. \n" +
                                    "   Node:    " + nodeRef);
                        }
                        return null;
                    }
                    // Get the user that initiated the delete
                    RunAsWork<String> getUserAuthCallback = new RunAsWork<String>()
                    {
                        public String doWork() throws Exception
                        {
                            return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_DELETED_NODE_USER);
                        }
                    };
                    String runAs = AuthenticationUtil.runAs(getUserAuthCallback, AuthenticationUtil.SYSTEM_USER_NAME);
                    // Authentication wrapper
                    RunAsWork<Object> deleteAuthCallback = new RunAsWork<Object>()
                    {
                        /**
                         * Recursive method that removes the aspect from all children of the given node
                         */
                        private void removeAspectFromHierarchy(NodeRef nodeRef)
                        {
                            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_DELETED_NODE))
                            {
                                // Restore the original name
                                Serializable originalName = nodeService.getProperty(nodeRef, ContentModel.PROP_DELETED_NODE_ORIGINAL_NAME);
                                nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, originalName);
                                // Remove the aspect to stake our claim
                                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_DELETED_NODE);
                            }
                            // Make sure that nothing in the hierarchy has the aspect, either
                            List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(nodeRef);
                            for (ChildAssociationRef assocRef : childAssocRefs)
                            {
                                // Ignore non-primary assocs
                                if (!assocRef.isPrimary())
                                {
                                    continue;
                                }
                                removeAspectFromHierarchy(assocRef.getChildRef());
                            }
                        }
                        public Object doWork() throws Exception
                        {
                            deleteNodeDirectly(nodeRef);
                            // If the node went into an archive, then follow it and remove the sys:deleted aspect
                            // and revert the cm:name property
                            NodeRef archivedRootNodeRef = nodeService.getStoreArchiveNode(nodeRef.getStoreRef());
                            if (archivedRootNodeRef != null)
                            {
                                StoreRef archiveStoreRef = archivedRootNodeRef.getStoreRef();
                                NodeRef archivedNodeRef = new NodeRef(archiveStoreRef, nodeRef.getId());
                                if (nodeService.exists(archivedNodeRef))
                                {
                                    removeAspectFromHierarchy(archivedNodeRef);
                                }
                            }
                            // Success
                            if (isDebugEnabled)
                            {
                                logger.debug("\n" +
                                        "Successfully deleted node.\n" +
                                        "   Node:    " + nodeRef);
                            }
                            // Done
                            return null;
                        }
                    };
                    return AuthenticationUtil.runAs(deleteAuthCallback, runAs);
                }
            };
            try
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(deleteTxnCallback);
                // Done
            }
            catch (Throwable e)
            {
                // We can ignore all errors if the VM is shutting down
                if (NodeArchiveInterceptor.shutdownListener.isVmShuttingDown())
                {
                    return;
                }
                
                // It failed, so just ensure that the sys:deleted aspect has been removed
                RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        RunAsWork<Object> authCallback = new RunAsWork<Object>()
                        {
                            public Object doWork() throws Exception
                            {
                                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_DELETED_NODE);
                                return null;
                            }
                        };
                        return AuthenticationUtil.runAs(authCallback, AuthenticationUtil.SYSTEM_USER_NAME);
                    }
                };
                try
                {
                    transactionService.getRetryingTransactionHelper().doInTransaction(callback);
                }
                catch (Throwable ee)
                {
                    // This is bad, but the original exception is the one that really needs to get out.
                    // We dump this error.
                    logger.info("\n" +
                            "Failed to remove sys:deletedNode aspect from node: \n" +
                            "   Node:        " + nodeRef + "\n" +
                            "   After Error: " + e.getMessage(),
                            e);
                }
                // Rethrow the original error
                throw new AlfrescoRuntimeException("\n" +
                        "Failed to delete node: \n" +
                        "   Node: " + nodeRef,
                        e);
            }
        }
    }
}
