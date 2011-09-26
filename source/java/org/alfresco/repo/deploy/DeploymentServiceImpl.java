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

package org.alfresco.repo.deploy;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.alfresco.deployment.DeploymentReceiverService;
import org.alfresco.deployment.DeploymentReceiverTransport;
import org.alfresco.deployment.DeploymentToken;
import org.alfresco.deployment.DeploymentTransportOutputFilter;
import org.alfresco.deployment.FileDescriptor;
import org.alfresco.deployment.FileType;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.action.ActionServiceRemote;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMNodeService;
import org.alfresco.repo.avm.util.SimplePath;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.remote.AVMRemoteImpl;
import org.alfresco.repo.remote.AVMSyncServiceRemote;
import org.alfresco.repo.remote.ClientTicketHolder;
import org.alfresco.repo.remote.ClientTicketHolderThread;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionServiceTransport;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.AVMWrongTypeException;
import org.alfresco.service.cmr.avm.deploy.DeploymentCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.cmr.remote.AVMRemoteTransport;
import org.alfresco.service.cmr.remote.AVMSyncServiceTransport;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * Implementation of DeploymentService.
 * @author britt
 */
public class DeploymentServiceImpl implements DeploymentService
{
    private static Log fgLogger = LogFactory.getLog(DeploymentServiceImpl.class);

    private NodeService nodeService;
    private NamespacePrefixResolver namespacePrefixResolver;
    private SearchService searchService;

    /**
     * The local AVMService Instance.
     */
    private AVMService fAVMService;
    
    private AVMNodeService fAVMNodeService;

    /**
     * The local Transaction Service Instance
     */
    TransactionService trxService;
    
    /**
     * The jobLockService
     */
    private JobLockService jobLockService;
    
    /**
     * The Ticket holder.
     */
    private ClientTicketHolder fTicketHolder;
    
    /**
     * number of concurrent sending threads
     */
    private int numberOfSendingThreads = 4;
    
    /**
     * Hold the deployment lock for 3600 seconds  (1 hour)
     * <p>
     * This is how long we will wait for a business process to complete.
     * And needs to be fairly long to allow transmission of of big files 
     * over high latency networks.
     */
	private long targetLockTimeToLive = 3600000;
	
	/**
	 * Refresh the lock every minute or so
	 * <p>
	 * This is how long we keep the lock for before nudging it.  So if 
	 * this node in the cluster is shut down during deployment then 
	 * another node can take over.
	 */
	private long targetLockRefreshTime = 10000;
	
	/**
	 * Retry for target lock every 1 second
	 */
	private long targetLockRetryWait = 1000;
	
	/**
	 * Retry 10000 times before giving up, basically we 
	 * never want to give up.
	 */
	private int targetLockRetryCount = 10001;
	
	/**
	 * The size of the output buffers
	 */
	private int OUTPUT_BUFFER_SIZE = 20000;
	
	private int outputBufferSize = OUTPUT_BUFFER_SIZE;
    
    public void init()
    {
    	  PropertyCheck.mandatory(this, "jobLockService", jobLockService);
    	  PropertyCheck.mandatory(this, "transactionService", trxService);
    	  PropertyCheck.mandatory(this, "avmService", fAVMService);
    	  PropertyCheck.mandatory(this, "avmNodeService", fAVMNodeService);
    }

    /**
     * Default constructor.
     */
    public DeploymentServiceImpl()
    {
        fTicketHolder = new ClientTicketHolderThread();
    }

    /**
     * Setter.
     * @param service The instance to set.
     */
    public void setAvmService(AVMService service)
    {
        fAVMService = service;
    }

    /**
     * Setter.
     * @param trxService The instance to set.
     */
    public void setTransactionService(TransactionService trxService)
    {
        this.trxService = trxService;
    }
    
    /**
     * Setter.
     * @param nodeService The instance to set.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Setter.
     * @param namespacePrefixResolver The instance to set.
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    /**
     * Setter.
     * @param searchService The instance to set.
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /* 
     * Deploy differences to an ASR 
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.deploy.DeploymentService#deployDifference(int, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, boolean, boolean)
     */
    public void deployDifference(int version, 
    		String srcPath, 
    		String hostName,
            int port, 
            String userName, 
            String password,
            String dstPath, 
            final NameMatcher matcher, 
            boolean createDst,
            final boolean dontDelete, 
            final boolean dontDo,
            final List<DeploymentCallback> callbacks)
    {
        final String storeName = srcPath.substring(0, srcPath.indexOf(":"));

        /**
         * Lock the cluster for the remote target
         */
        String lockStr = hostName + "." + "asr." + storeName;
        QName lockQName = QName.createQName("{http://www.alfresco.org/deploymentService/1.0}" + lockStr);

        Lock lock = new Lock(lockQName);
        lock.makeLock();
        try
        {
            /**
             * Got the lock - now do a deployment
             */
            if (fgLogger.isDebugEnabled())
            {
                fgLogger.debug("Deploying to Remote Alfresco at " + hostName);
            }


            try
            {
                RetryingTransactionHelper trn = trxService.getRetryingTransactionHelper();

                fgLogger.debug("Connecting to remote AVM at " + hostName + ":" +port);
                final AVMRemote remote = getRemote(hostName, port, userName, password);
                if (version < 0)
                {
                    /**
                     * If version is -1, Create a local snapshot to deploy
                     */
                    fgLogger.debug("creating snapshot of local version");


                    RetryingTransactionCallback<Integer> localSnapshot = new RetryingTransactionCallback<Integer>()
                    {
                        public  Integer execute() throws Throwable
                        {
                            int newVersion = fAVMService.createSnapshot(storeName, null, null).get(storeName);
                            return new Integer(newVersion);
                        }
                    };
                    version = trn.doInTransaction(localSnapshot, false, true).intValue();  
                    fgLogger.debug("snapshot local created " + storeName + ", " + version);
                }

                {
                    DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.START,
                            new Pair<Integer, String>(version, srcPath),
                            dstPath);
                    processEvent(event, callbacks);
                }

                /*
                 * Create a snapshot on the destination server.
                 */
                boolean createdRoot = false;
                String [] storePath = dstPath.split(":");
                int snapshot = -1;

                // Get the root of the deployment on the destination server.
                AVMNodeDescriptor dstRoot = remote.lookup(-1, dstPath);

                if (!dontDo)
                {
                    // Get the root of the deployment on the destination server.

                    if (dstRoot == null)
                    {
                        if (createDst)
                        {
                            fgLogger.debug("Create destination parent folder:" +  dstPath);
                            createDestination(remote, dstPath);
                            dstRoot = remote.lookup(-1, dstPath);
                            createdRoot = true;
                        }
                        else
                        {
                            throw new AVMNotFoundException("Node Not Found: " + dstRoot);
                        }
                    }
                    fgLogger.debug("create snapshot on remote");
                    snapshot = remote.createSnapshot(storePath[0], "PreDeploy", "Pre Deployment Snapshot").get(storePath[0]);
                    fgLogger.debug("snapshot created on remote");
                }

                final int srcVersion = version;
                final String srcFinalPath = srcPath;
                RetryingTransactionCallback<AVMNodeDescriptor> readRoot = new RetryingTransactionCallback<AVMNodeDescriptor>()
                {
                    public  AVMNodeDescriptor execute() throws Throwable
                    {
                        return fAVMService.lookup(srcVersion, srcFinalPath);
                    }
                };

                final AVMNodeDescriptor srcRoot = trn.doInTransaction(readRoot, true, true);  

                // Get the root of the deployment from this server.
                // AVMNodeDescriptor srcRoot = fAVMService.lookup(version, srcPath);

                if (srcRoot == null)
                {
                    throw new AVMNotFoundException("Directory Not Found: " + srcPath);
                }
                if (!srcRoot.isDirectory())
                {
                    throw new AVMWrongTypeException("Not a directory: " + srcPath);
                }

                /**
                 * The destination directory exists - check is actually a directory
                 */
                if (!dstRoot.isDirectory())
                {
                    throw new AVMWrongTypeException("Not a Directory: " + dstPath);
                }

                try
                {
                    /**
                     * Recursivly copy
                     */
                    fgLogger.debug("both src and dest exist, recursivly deploy");
                    final AVMNodeDescriptor dstParentNode = dstRoot;
                    RetryingTransactionCallback<Integer> copyContentsRecursivly = new RetryingTransactionCallback<Integer>()
                    {
                        public  Integer execute() throws Throwable
                        {
                            deployDirectoryPush(srcVersion, srcRoot, dstParentNode, remote, matcher, dontDelete, dontDo, callbacks);
                            return new Integer(0);
                        }
                    };

                    trn.setMaxRetries(1);
                    trn.doInTransaction(copyContentsRecursivly, false, true);

                    fgLogger.debug("finished copying, snapshot remote");
                    remote.createSnapshot(storePath[0], "Deployment", "Post Deployment Snapshot.");

                    DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.END,
                            new Pair<Integer, String>(version, srcPath),
                            dstPath);
                    processEvent(event, callbacks);
                    return;
                }
                catch (AVMException e)
                {
                    fgLogger.debug("error during remote copy and snapshot");
                    try
                    {
                        if (snapshot != -1)
                        {
                            fgLogger.debug("Attempting to roll back ");
                            AVMSyncService syncService = getSyncService(hostName, port);
                            List<AVMDifference> diffs = syncService.compare(snapshot, dstPath, -1, dstPath, null);
                            syncService.update(diffs, null, false, false, true, true, "Aborted Deployment", "Aborted Deployment");
                        }
                    }
                    catch (Exception ee)
                    {
                        throw new AVMException("Failed to rollback to version " + snapshot + " on " + hostName, ee);
                    }
                    throw new AVMException("Deployment to " + hostName + " failed.", e);
                }
            }
            catch (Exception e)
            {
                DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.FAILED,
                        new Pair<Integer, String>(version, srcPath),
                        dstPath, e.getMessage());
                processEvent(event, callbacks);

                throw new AVMException("Deployment to " + hostName + " failed." + e.toString(), e);
            }
            finally
            {
                fgLogger.debug("ASR Finally block, Releasing ASR deployment ticket");
                fTicketHolder.setTicket(null);
            }
        }
        finally
        {
            fgLogger.debug("about to release lock");
            lock.releaseLock();
        }

    }    

    /**
     * Deploy all the children of corresponding directories. (ASR version)
     * @param src The source directory.
     * @param dst The destination directory.
     * @param remote The AVMRemote instance.
     * @param dontDelete Flag for not deleting.
     * @param dontDo Flag for dry run.
     */
    private void deployDirectoryPush(int version,
                                     AVMNodeDescriptor src, 
                                     AVMNodeDescriptor dst,
                                     AVMRemote remote,
                                     NameMatcher matcher,
                                     boolean dontDelete, boolean dontDo,
                                     List<DeploymentCallback> callbacks)
    {
        if (src.getGuid().equals(dst.getGuid()))
        {
            return;
        }
        if (!dontDo && !dontDelete)
        {
            copyMetadata(version, src, dst, remote);
        }
        // Get the listing for the source.
        SortedMap<String, AVMNodeDescriptor> srcList = fAVMService.getDirectoryListing(src);
        // Get the listing for the destination.
        SortedMap<String, AVMNodeDescriptor> dstList = remote.getDirectoryListing(dst);
        
        // Strip out stale nodes.
        for (Map.Entry<String, AVMNodeDescriptor> entry : srcList.entrySet())
        {
            String name = entry.getKey();
            AVMNodeDescriptor srcNode = entry.getValue();
            
            if (isStale(srcNode))
            {
                if (fgLogger.isDebugEnabled())
                {
                    fgLogger.debug("Stale child found: " + srcNode);
                }
                srcList.remove(name);
            }
        }
        
        for (Map.Entry<String, AVMNodeDescriptor> entry : srcList.entrySet())
        {
            String name = entry.getKey();
            AVMNodeDescriptor srcNode = entry.getValue();
            AVMNodeDescriptor dstNode = dstList.get(name);
            if (!excluded(matcher, srcNode.getPath(), dstNode != null ? dstNode.getPath() : null))
            {
                if(isStale(srcNode))
                {
                    fgLogger.debug("stale file not added" + srcNode);
                    continue;
                }
                
                deploySinglePush(version, srcNode, dst, dstNode, remote, matcher, dontDelete, dontDo, callbacks);
            }
        }
        // Delete nodes that are missing in the source.
        if (dontDelete)
        {
            return;
        }
        for (String name : dstList.keySet())
        {
            if (!srcList.containsKey(name))
            {
                Pair<Integer, String> source =
                    new Pair<Integer, String>(version, AVMNodeConverter.ExtendAVMPath(src.getPath(), name));
                String destination = AVMNodeConverter.ExtendAVMPath(dst.getPath(), name);
                if (!excluded(matcher, null, destination))
                {
                    DeploymentEvent event =
                        new DeploymentEvent(DeploymentEvent.Type.DELETED,
                                            source,
                                            destination);
                    processEvent(event, callbacks);
                    if (dontDo)
                    {
                        continue;
                    }
                    remote.removeNode(dst.getPath(), name);
                }
            }
        }
    }

    /**
     * Push out a single node.
     * @param src The source node.
     * @param dstParent The destination parent.
     * @param dst The destination node. May be null.
     * @param remote The AVMRemote instance.
     * @param dontDelete Flag for whether deletions should happen.
     * @param dontDo Dry run flag.
     */
    private void deploySinglePush(int version,
                                  AVMNodeDescriptor src, AVMNodeDescriptor dstParent,
                                  AVMNodeDescriptor dst, AVMRemote remote,
                                  NameMatcher matcher,
                                  boolean dontDelete, boolean dontDo,
                                  List<DeploymentCallback> callbacks)
    {
        // Destination does not exist.
        if (dst == null)
        {
            if (src.isDirectory())
            {
                // Recursively copy a source directory.
                Pair<Integer, String> source =
                    new Pair<Integer, String>(version, src.getPath());
                String destination = AVMNodeConverter.ExtendAVMPath(dstParent.getPath(), src.getName());
                DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.CREATED,
                                                      source,
                                                      destination);
                processEvent(event, callbacks);
                if (dontDo)
                {
                    return;
                }
                copyDirectory(version, src, dstParent, remote, matcher, callbacks);
                return;
            }
            
            // here when src is a file
            Pair<Integer, String> source =
                new Pair<Integer, String>(version, src.getPath());
            String destination = AVMNodeConverter.ExtendAVMPath(dstParent.getPath(), src.getName());
            DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.CREATED,
                                                        source,
                                                        destination);
            processEvent(event, callbacks);
            if (dontDo)
            {
                return;
            }
            // Copy a source file.
            OutputStream out = remote.createFile(dstParent.getPath(), src.getName());
            try
            {
                InputStream in = fAVMService.getFileInputStream(src);
                copyStream(in, out);
            }
            finally
            {
                if(out != null)
                {
                    // whatever happens close stream
                    try
                    {
                        out.close();
                    }
                    catch (IOException e)
                    {
                        throw new AVMException("I/O Exception", e);
                    }
                }
            }

            copyMetadata(version, src, remote.lookup(-1, dstParent.getPath() + '/' + src.getName()), remote);
            return;
        }
        
        // Destination exists and is a directory.
        if (src.isDirectory())
        {
            // If the destination is also a directory, recursively deploy.
            if (dst.isDirectory())
            {
                deployDirectoryPush(version, src, dst, remote, matcher, dontDelete, dontDo, callbacks);
                return;
            }
            Pair<Integer, String> source =
                new Pair<Integer, String>(version, src.getPath());
            String destination = dst.getPath();
            DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.CREATED,
                                                        source, destination);
            processEvent(event, callbacks);
            
            if (dontDo)
            {
                return;
            }
            // MER WHY IS THIS HERE ?
            fgLogger.debug("Remove and recopy node :" + dstParent.getPath() + '/' + src.getName());
            remote.removeNode(dstParent.getPath(), src.getName());
            copyDirectory(version, src, dstParent, remote, matcher, callbacks);
            return;
        }
        // Source exists and is a file.
        if (dst.isFile())
        {
            // Destination is also a file. Overwrite if the GUIDS are different.
            if (src.getGuid().equals(dst.getGuid()))
            {
                return;
            }
            Pair<Integer, String> source =
                new Pair<Integer, String>(version, src.getPath());
            String destination = dst.getPath();
            DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.UPDATED,
                                                        source,
                                                        destination);
            processEvent(event, callbacks);
            if (dontDo)
            {
                return;
            }

            OutputStream out = remote.getFileOutputStream(dst.getPath());
            try
            {
                InputStream in = fAVMService.getFileInputStream(src);
                copyStream(in, out);
            }
            finally
            {
                if(out != null)
                {
                    // whatever happens close stream
                    try
                    {
                        out.close();
                    }
                    catch (IOException e)
                    {
                        throw new AVMException("I/O Exception", e);
                    }
                }
            }

            copyMetadata(version, src, dst, remote);
            return;
        }
        Pair<Integer, String> source =
            new Pair<Integer, String>(version, src.getPath());
        String destination = AVMNodeConverter.ExtendAVMPath(dstParent.getPath(), src.getName());
        DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.UPDATED,
                                                    source,
                                                    destination);
        processEvent(event, callbacks);
        if (dontDo)
        {
            return;
        }
        // Destination is a directory and the source is a file.
        // Delete the destination directory and copy the file over.
        remote.removeNode(dstParent.getPath(), dst.getName());

        OutputStream out = remote.createFile(dstParent.getPath(), src.getName());
        try 
        {
            InputStream in = fAVMService.getFileInputStream(src);
            copyStream(in, out);
        }
        finally
        {
            if(out != null)
            {
                // whatever happens close stream
                try
                {
                    out.close();
                }
                catch (IOException e)
                {
                    throw new AVMException("I/O Exception", e);
                }
            }
        }
        copyMetadata(version, src, remote.lookup(-1, dstParent.getPath() + '/' + dst.getName()), remote);
    }

    /**
     * Recursively copy a directory.
     * @param src
     * @param parent
     * @param remote
     */
    private void copyDirectory(int version, AVMNodeDescriptor src, AVMNodeDescriptor parent,
                               AVMRemote remote, NameMatcher matcher, List<DeploymentCallback>callbacks)
    {
        // Create the destination directory.
        remote.createDirectory(parent.getPath(), src.getName());
        AVMNodeDescriptor newParent = remote.lookup(-1, parent.getPath() + '/' + src.getName());
        copyMetadata(version, src, newParent, remote);
        SortedMap<String, AVMNodeDescriptor> list =
            fAVMService.getDirectoryListing(src);
        // For each child in the source directory.
        for (AVMNodeDescriptor child : list.values())
        {
            if (!excluded(matcher, child.getPath(), null))
            {
                /**
                 * Temporary work around for staleness.
                 */
                if (isStale(child))
                {
                    if (fgLogger.isDebugEnabled())
                    {
                        fgLogger.debug("Stale child found: " + child);
                    }
                    continue;
                }
                
                // If it's a file, copy it over and move on.
                if (child.isFile())
                {   
                    DeploymentEvent event =
                        new DeploymentEvent(DeploymentEvent.Type.CREATED,
                                            new Pair<Integer, String>(version,  src.getPath() + '/' + child.getName()),
                                            newParent.getPath() + '/' + child.getName());
                    processEvent(event, callbacks);
                    
                    OutputStream out = remote.createFile(newParent.getPath(), child.getName());
                    try
                    {
                        InputStream in = fAVMService.getFileInputStream(child);
                        copyStream(in, out);
                    }
                    finally
                    {
                        if(out != null)
                        {
                            // whatever happens close stream
                            try
                            {
                                out.close();
                            }
                            catch (IOException e)
                            {
                                throw new AVMException("I/O Exception", e);
                            }
                        }
                    }
                    copyMetadata(version, child, remote.lookup(-1, newParent.getPath() + '/' + child.getName()), remote);
                }
                else
                {
                	// is a directory
                    DeploymentEvent event =
                        new DeploymentEvent(DeploymentEvent.Type.CREATED,
                                            new Pair<Integer, String>(version,  src.getPath() + '/' + child.getName() ),
                                            newParent.getPath() + '/' + child.getName());
                    processEvent(event, callbacks);
                    // Otherwise copy the child directory recursively.
                    copyDirectory(version, child, newParent, remote, matcher, callbacks);
                }
            }
        }
    }

    /**
     * Utility for copying from one stream to another.
     * 
     * in is closed.
     * 
     * out is not closed.
     * 
     * @param in The input stream.
     * @param out The output stream.
     */
    private void copyStream(InputStream in, OutputStream out)
    {
        byte[] buff = new byte[8192];
        int read = 0;
        try
        {
            while ((read = in.read(buff)) != -1)
            {
                out.write(buff, 0, read);
            }
            in.close();
            //out.flush();
            //out.close();
        }
        catch (IOException e)
        {
            throw new AVMException("I/O Exception", e);
        }
    }

    private void copyMetadata(int version, AVMNodeDescriptor src, AVMNodeDescriptor dst, AVMRemote remote)
    {
        Map<QName, PropertyValue> props = fAVMService.getNodeProperties(version, src.getPath());
        remote.setNodeProperties(dst.getPath(), props);
        Set<QName> aspects = fAVMService.getAspects(version, src.getPath());
        for (QName aspect : aspects)
        {
            if (remote.hasAspect(-1, dst.getPath(), aspect))
            {
                continue;
            }
            remote.addAspect(dst.getPath(), aspect);
        }
        remote.setGuid(dst.getPath(), src.getGuid());
        if (src.isFile())
        {
            ContentData contData = fAVMService.getContentDataForRead(version, src.getPath());
            remote.setEncoding(dst.getPath(), contData.getEncoding());
            remote.setMimeType(dst.getPath(), contData.getMimetype());
        }
    }

    /**
     * Utility to get an AVMRemote from a remote Alfresco Server.
     * @param hostName
     * @param port
     * @param userName
     * @param password
     * @return
     */
    private AVMRemote getRemote(String hostName, int port, String userName, String password)
    {
        try
        {
            RmiProxyFactoryBean authFactory = new RmiProxyFactoryBean();
            authFactory.setRefreshStubOnConnectFailure(true);
            authFactory.setServiceInterface(AuthenticationService.class);
            authFactory.setServiceUrl("rmi://" + hostName + ":" + port + "/authentication");
            authFactory.afterPropertiesSet();
            AuthenticationService authService = (AuthenticationService)authFactory.getObject();
            authService.authenticate(userName, password.toCharArray());
            String ticket = authService.getCurrentTicket();
            fTicketHolder.setTicket(ticket);
            RmiProxyFactoryBean remoteFactory = new RmiProxyFactoryBean();
            remoteFactory.setRefreshStubOnConnectFailure(true);
            remoteFactory.setServiceInterface(AVMRemoteTransport.class);
            remoteFactory.setServiceUrl("rmi://" + hostName + ":" + port + "/avm");
            remoteFactory.afterPropertiesSet();
            AVMRemoteTransport transport = (AVMRemoteTransport)remoteFactory.getObject();
            AVMRemoteImpl remote = new AVMRemoteImpl();
            remote.setAvmRemoteTransport(transport);
            remote.setClientTicketHolder(fTicketHolder);
            return remote;
        }
        catch (Exception e)
        {
            throw new AVMException("Could not Initialize Remote Connection to " + hostName, e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.deploy.DeploymentService#getRemoteActionService(java.lang.String, int, java.lang.String, java.lang.String)
     */
    public ActionService getRemoteActionService(String hostName, int port, String userName, String password)
    {
        try
        {
            RmiProxyFactoryBean authFactory = new RmiProxyFactoryBean();
            authFactory.setRefreshStubOnConnectFailure(true);
            authFactory.setServiceInterface(AuthenticationService.class);
            authFactory.setServiceUrl("rmi://" + hostName + ":" + port + "/authentication");
            authFactory.afterPropertiesSet();
            AuthenticationService authService = (AuthenticationService)authFactory.getObject();
            authService.authenticate(userName, password.toCharArray());
            String ticket = authService.getCurrentTicket();
            fTicketHolder.setTicket(ticket);
            RmiProxyFactoryBean remoteFactory = new RmiProxyFactoryBean();
            remoteFactory.setRefreshStubOnConnectFailure(true);
            remoteFactory.setServiceInterface(ActionServiceTransport.class);
            remoteFactory.setServiceUrl("rmi://" + hostName + ":" + port + "/action");
            remoteFactory.afterPropertiesSet();
            ActionServiceTransport transport = (ActionServiceTransport)remoteFactory.getObject();
            ActionServiceRemote remote = new ActionServiceRemote();
            remote.setActionServiceTransport(transport);
            remote.setClientTicketHolder(fTicketHolder);
            return remote;
        }
        catch (Exception e)
        {
            throw new AVMException("Could not Initialize Remote Connection to " + hostName, e);
        }
    }

    /**
     * Utility method to get the payload transformers for a named transport
     * 
     * The transport adapters are sprung into the deploymentReceiverTransportAdapters property
     * 
     * @return the transformers
     */
    private List<DeploymentTransportOutputFilter> getTransformers(String transportName)
    {    	
        
        DeploymentReceiverTransportAdapter adapter = deploymentReceiverTransportAdapters.get(transportName);
        	
        if(adapter == null) {
        		// Adapter does not exist
        		fgLogger.error("Deployment Receiver Transport adapter does not exist for transport. Name: " + transportName);
        		throw new AVMException("Deployment Receiver Transport adapter does not exist for transport. Name: " + transportName);
        }

        List<DeploymentTransportOutputFilter> transformers = adapter.getTransformers();
        return transformers;
    }

    
    
    /**
     * Utility method to get a connection to a remote file system receiver (FSR)
     * 
     * The transport adapters are sprung into the deploymentReceiverTransportAdapters property
     * @param transportName the name of the adapter for the transport 
     * @param hostName the hostname or IP address to connect to
     * @param port the port number
     * @param version the version of the website to deploy
     * @param srcPath the path of the website
     * 
     * @return an implementation of the service
     */
    private DeploymentReceiverService getDeploymentReceiverService(String transportName, String hostName, int port, int version, String srcPath)
    {    	
 
       DeploymentReceiverTransportAdapter adapter = deploymentReceiverTransportAdapters.get(transportName);
        	
        if(adapter == null) {
        	// Adapter does not exist
        	fgLogger.error("Deployment Receiver Transport adapter does not exist for transport. Name: " + transportName);
        		throw new AVMException("Deployment Receiver Transport adapter does not exist for transport. Name: " + transportName);
        }
        try
        {
        	DeploymentReceiverTransport transport = adapter.getTransport(hostName, port, version, srcPath);
        		
        	// Now decorate the transport with the service client
            DeploymentReceiverServiceClient service = new DeploymentReceiverServiceClient();
            service.setDeploymentReceiverTransport(transport);
            return service;
        }
        catch (Exception e)
        {
            throw new AVMException("Could not connect to remote deployment receiver, transportName:" + transportName + ", hostName:" + hostName + ", port: " + port, e);
        }
    }

    /**
     * Utility to get the sync service for rolling back after a failed deployment.
     * @param hostName The target machine.
     * @param port The port.
     * @return An AVMSyncService instance.
     */
    private AVMSyncService getSyncService(String hostName, int port)
    {
        try
        {
            RmiProxyFactoryBean syncFactory = new RmiProxyFactoryBean();
            syncFactory.setRefreshStubOnConnectFailure(true);
            syncFactory.setServiceInterface(AVMSyncServiceTransport.class);
            syncFactory.setServiceUrl("rmi://" + hostName + ":" + port + "/avmsync");
            syncFactory.afterPropertiesSet();
            AVMSyncServiceTransport syncServiceTransport = (AVMSyncServiceTransport)syncFactory.getObject();
            AVMSyncServiceRemote remote = new AVMSyncServiceRemote();
            remote.setAvmSyncServiceTransport(syncServiceTransport);
            remote.setClientTicketHolder(fTicketHolder);
            return remote;
        }
        catch (Exception e)
        {
            throw new AVMException("Could not roll back failed deployment to " + hostName, e);
        }
    }

    /**
     * Helper function to create a non existent destination.
     * @param remote The AVMRemote instance.
     * @param dstPath The destination path to create.
     */
    private void createDestination(AVMRemote remote, String dstPath)
    {
        String[] storePath = dstPath.split(":");
        String storeName = storePath[0];
        String path = storePath[1];
        AVMStoreDescriptor storeDesc = remote.getStore(storeName);
        if (storeDesc == null)
        {
            remote.createStore(storeName);
        }
        SimplePath simpPath = new SimplePath(path);
        if (simpPath.size() == 0)
        {
            return;
        }
        String prevPath = storeName + ":/";
        for (int i = 0; i < simpPath.size(); i++)
        {
            String currPath = AVMNodeConverter.ExtendAVMPath(prevPath, simpPath.get(i));
            AVMNodeDescriptor desc = remote.lookup(-1, currPath);
            if (desc == null)
            {
                remote.createDirectory(prevPath, simpPath.get(i));
            }
            prevPath = currPath;
        }
    }
    
  	private  Set<String>getAspects(AVMService avmService, AVMNodeDescriptor src)
  	{
    	Set<QName>aspects = avmService.getAspects(src);
  		Set<String>stringAspects = new HashSet<String>();
  		for (QName aspect : aspects)
  		{
  			stringAspects.add(aspect.toString());
  		}
  		return stringAspects;
  	}
	
  	private Map<String, Serializable> getProperties(AVMNodeDescriptor src, int version)
  	{
  		/**
  		 * Get the AVM properties - which do not have any of the "syntetic" Node Service Values.
  		 */
  		Map<QName, PropertyValue> properties = fAVMService.getNodeProperties(src);
  		NodeRef nodeRef = AVMNodeConverter.ToNodeRef(version, src.getPath());
  		
  		/**
  		 * Get the properties in Node Service format
  		 */
  		Map<QName, Serializable> nodeProps = fAVMNodeService.getProperties(nodeRef);
  	  		
  		Map<String, Serializable> retVal = new HashMap<String, Serializable>();
  	  	for(QName key : properties.keySet())
  	  	{
  	  			Serializable value = nodeProps.get(key);
  	  			retVal.put(key.toString(), value);
  	  	}
  	  	return retVal;
  	}

    /**
     * Deploy differences to a File System Receiver, FSR
     * 
     *  @param version snapshot version to deploy.  If 0 then a new snapshot is created.
     *  @param srcPath 
     *	@param adapterName
     *	@param hostName
     *  @param port
     *  @param userName 
     *  @param password
     *  @param target 
     *  @param matcher
     *  @param createDst 	Not implemented
     *  @param dontDelete   Not implemented
     *  @param dontDo		Not implemented
     *  @param callbacks	Event callbacks when a deployment Starts, Ends, Adds, Deletes etc.
     *
     *  @throws AVMException
     *  
     * @see org.alfresco.service.cmr.avm.deploy.DeploymentService#deployDifferenceFS(int, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, boolean, boolean)
     */
	public void deployDifferenceFS(int version, 
			final String srcPath,
			String adapterName, 
			String hostName, 
			int port, 
			String userName, 
			String password, 
			String target,
			final NameMatcher matcher, 
			boolean createDst, 
			boolean dontDelete,
			boolean dontDo, 
			List<DeploymentCallback> callbacks) 
    {
		
	    fgLogger.debug("deployDifferenceFS start");
    	/**
    	 * Lock cluster for the remote target
    	 */
		String lockStr = "deploy." + hostName + "." + port + "." + target;
		QName lockQName = QName.createQName("{http://www.alfresco.org/deploymentService/1.0}" + lockStr);
		final Lock lock = new Lock(lockQName);
		lock.makeLock();
		try
		{
		    /**
	         * Cluster Lock held here
	         */
		     if (fgLogger.isDebugEnabled())
		        {
		            Object[] objs = {version, srcPath, adapterName, hostName, port, target};
		            MessageFormat f = new MessageFormat("Deployment Lock Held: version {0}, srcPath {1}, adapterName {2}, hostName {3}, port {4}, target {5}");
		            fgLogger.debug(f.format(objs));
		        }
		        
		        DeploymentReceiverService service = null;
		        List<DeploymentTransportOutputFilter>transformers = null;
		        String ticket = null;
		        
		        String currentEffectiveUser = AuthenticationUtil.getRunAsUser();

		        try
		        {       
		            // Kick off the event queue that will process deployment call-backs 
		            final LinkedBlockingQueue<DeploymentEvent> eventQueue = new LinkedBlockingQueue<DeploymentEvent>();
		            EventQueueWorker eventQueueWorker = new EventQueueWorker(currentEffectiveUser, eventQueue, callbacks);
		            eventQueueWorker.setName(eventQueueWorker.getClass().getName());
		            eventQueueWorker.setPriority(Thread.currentThread().getPriority());
		            eventQueueWorker.start();
		                
		            try 
		            {
		                final String storeName = srcPath.substring(0, srcPath.indexOf(':'));
		                try {           
		                
		                    if (version < 0)
		                    {
		                        RetryingTransactionHelper trn = trxService.getRetryingTransactionHelper();
		                        
		                        RetryingTransactionCallback<Integer> localSnapshot = new RetryingTransactionCallback<Integer>()
		                        {
		                            public  Integer execute() throws Throwable
		                            {
		                                int newVersion = fAVMService.createSnapshot(storeName, null, null).get(storeName);
		                                return new Integer(newVersion);
		                            }
		                        };
		                        version = trn.doInTransaction(localSnapshot, false, true).intValue();  
		                        fgLogger.debug("snapshot local created " + storeName + ", " + version);
		                    }

		                    transformers = getTransformers(adapterName);
		                    service = getDeploymentReceiverService(adapterName, hostName, port, version, srcPath);
		                } 
		                catch (Exception e)
		                {
		                    // unable to get service
		                    eventQueue.add(new DeploymentEvent(DeploymentEvent.Type.FAILED,
		                         new Pair<Integer, String>(version, srcPath),
		                         target, e.getMessage()));
		                    throw e;
		                }
		                
		                eventQueue.add(new DeploymentEvent(DeploymentEvent.Type.START,
		                     new Pair<Integer, String>(version, srcPath),
		                     target));
		            
		                // Go parallel to reduce the problems of high network latency           

		                final LinkedBlockingQueue<DeploymentWork> sendQueue = new LinkedBlockingQueue<DeploymentWork>();
		                final List<Exception> errors = Collections.synchronizedList(new ArrayList<Exception>());

		                SendQueueWorker[] workers = new SendQueueWorker[numberOfSendingThreads];
		                for(int i = 0; i < numberOfSendingThreads; i++)
		                {
		                    workers[i] = new SendQueueWorker(currentEffectiveUser, service, fAVMService, trxService, errors, eventQueue, sendQueue, transformers);
		                    workers[i].setName(workers[i].getClass().getName());
		                    workers[i].setPriority(Thread.currentThread().getPriority());
		                }
		                
		                for(SendQueueWorker sender : workers) 
		                {
		                    sender.start();
		                }
		               
		                try 
		                {   
		                    fgLogger.debug("calling begin");
		                    DeploymentToken token = service.begin(target, storeName, version, userName, password.toCharArray());
		                    ticket = token.getTicket();
		                    
		                    lock.checkLock();
		                    
		                    // run this in its own txn
		                    final DeploymentReceiverService fservice = service; 
		                    final String fTicket = ticket;
		                    final int fVersion = version;
		                    RetryingTransactionCallback<Integer> pushFSR = new RetryingTransactionCallback<Integer>()
		                    {
		                        public Integer execute() throws Throwable
		                        {
		                            deployDirectoryPushFSR(fservice, fTicket, fVersion, srcPath, "/", matcher, eventQueue, sendQueue, errors, lock);
		                            return 0;
		                        }
		                    };
		                    
		                    RetryingTransactionHelper trn = trxService.getRetryingTransactionHelper();
		                    trn.doInTransaction(pushFSR, false, true); 
		    
		                }
		                catch (Exception e)
		                {
		                    errors.add(e);
		                }
                		catch (Throwable t)
                		{
                    		errors.add(new AVMException("Unexpected Throwable", t));
                		}
		                finally
		                {
		                    // clean up senders thread pool
		                    fgLogger.debug("closing deployment workers");
		                    for(SendQueueWorker sender : workers) 
		                    {
		                        sender.stopMeWhenIdle();
		                    }
		                    for(SendQueueWorker sender : workers) 
		                    {
		                        sender.join();
		                    }
		                    fgLogger.debug("deployment workers closed");
		                
		                    if (errors.size() <= 0 && ticket != null)
		                    {
		                        try 
		                        {
		                            fgLogger.debug("no errors - prepare and commit");
		                            lock.checkLock();
		                            
		                            service.prepare(ticket);
		                            lock.checkLock();
		                            
		                            service.commit(ticket);
		                            // no point checking the lock here - we have committed.
		                        } 
		                        catch (Exception e)
		                        {
		                            errors.add(e);
		                        }
		                    }
		                
		                    if(errors.size() > 0)
		                    {
		                        fgLogger.debug("errors on deployment workers");
		                        Exception firstError = errors.get(0);
		                
		                        eventQueue.add(new DeploymentEvent(DeploymentEvent.Type.FAILED,
		                        new Pair<Integer, String>(version, srcPath),
		                        target, firstError.getMessage()));

		                        if (ticket != null)
		                        {
		                            try 
		                            {
		                                service.abort(ticket);
		                            } 
		                            catch (Exception ae)
		                            {
		                                // nothing we can do here
		                                fgLogger.error("Unable to abort deployment.  Error in exception handler", ae);
		                            }
		                        }                   
		                        // yes there were errors, throw the first exception that was saved
		                        MessageFormat f = new MessageFormat("Error during deployment srcPath: {0}, version:{1}, adapterName:{2}, hostName:{3}, port:{4}, error:{5}");
		                        Object[] objs = { srcPath, version, adapterName, hostName, port, firstError };
		                                
		                        throw new AVMException(f.format(objs), firstError);
		                    }
		                } // end of finally block
		                
		                // Success if we get here
		                eventQueue.add(new DeploymentEvent(DeploymentEvent.Type.END,
		                                        new Pair<Integer, String>(version, srcPath),
		                                        target));
		                
		                fgLogger.debug("deployment completed successfully");
		            }
		            finally 
		            {
		                // Now stutdown the event queue
		                fgLogger.debug("closing event queue");
		                eventQueueWorker.stopMeWhenIdle();
		                eventQueueWorker.join();
		                fgLogger.debug("event queue closed");
		            }
		        }
		        catch (Exception e)
		        {
		            // yes there were errors
		            MessageFormat f = new MessageFormat("Deployment exception, unable to deploy : srcPath:{0}, target:{1}, version:{2}, adapterName:{3}, hostName:{4}, port:{5}, error:{6}");
		            Object[] objs = { srcPath, target, version, adapterName, hostName, port, e };       
		            throw new AVMException(f.format(objs), e);
		        }   
		}
		finally
		{
		    fgLogger.debug("At end of method - about to release lock");
		    lock.releaseLock();
		}
    } // End of deploy difference FS	
	
	
	private class ComparatorFileDescriptorCaseSensitive  implements Comparator<FileDescriptor> 
	{
		public int compare(FileDescriptor o1, FileDescriptor o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	}

	private class ComparatorAVMNodeDescriptorCaseSensitive implements Comparator<AVMNodeDescriptor> 
	{
		public int compare(AVMNodeDescriptor o1, AVMNodeDescriptor o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	}

	private ComparatorFileDescriptorCaseSensitive FILE_DESCRIPTOR_CASE_SENSITIVE  = new ComparatorFileDescriptorCaseSensitive();
	private ComparatorAVMNodeDescriptorCaseSensitive AVM_DESCRIPTOR_CASE_SENSITIVE = new ComparatorAVMNodeDescriptorCaseSensitive();
	
    /**
     * deployDirectoryPush (FSR only)
     * 
     * Compares the source and destination listings and updates report with update events required to make 
     * dest similar to src. 
     * 
     * @param service
     * @param ticket
     * @param report 
     * @param callbacks
     * @param version
     * @param srcPath
     * @param dstPath
     * @param matcher
     */
    private void deployDirectoryPushFSR(DeploymentReceiverService service, 
    		String ticket,
            int version,
            String srcPath, 
            String dstPath, 
            NameMatcher matcher,
    		BlockingQueue<DeploymentEvent> eventQueue,
    		BlockingQueue<DeploymentWork> sendQueue,
    		List<Exception> errors,
    		Lock lock)
    {
        Map<String, AVMNodeDescriptor> rawSrcListing = fAVMService.getDirectoryListing(version, srcPath);
        List<FileDescriptor> rawDstListing = service.getListing(ticket, dstPath);
       
        // Need to change from case insensitive order to case sensitive order
        TreeSet<FileDescriptor> dstListing = new TreeSet<FileDescriptor>(FILE_DESCRIPTOR_CASE_SENSITIVE);
        dstListing.addAll(rawDstListing);
        
        TreeSet<AVMNodeDescriptor> srcListing = new TreeSet<AVMNodeDescriptor>(AVM_DESCRIPTOR_CASE_SENSITIVE);
        srcListing.addAll(rawSrcListing.values());
        
        Iterator<FileDescriptor> dstIter = dstListing.iterator();
        Iterator<AVMNodeDescriptor> srcIter = srcListing.iterator();
        
        lock.checkLock();
	    
        // Here with two sorted directory listings
        AVMNodeDescriptor src = null;
        FileDescriptor dst = null;
        
        // Step through both directory listings
        while ((srcIter.hasNext() || dstIter.hasNext() || src != null || dst != null) && errors.size() <= 0)
        {
            if (src == null)
            {
                if (srcIter.hasNext())
                {
                    src = srcIter.next();
                    
                    /** 
                     * Temporary check for stale assets 
                     * 
                     * Correct fix would be to remove stale files from the snapshot.
                     * Code becomes obsolete once stale files are not part of the snapshot.
                     */
                    if (isStale(src))
                    {
                        if (fgLogger.isDebugEnabled())
                        {
                            fgLogger.debug("Stale child found: " + src);
                        }
                        src = null;
                        continue;
                    }    
                }
            }
            if (dst == null)
            {
                if (dstIter.hasNext())
                {
                    dst = dstIter.next();
                }
            }
            if (fgLogger.isDebugEnabled())
            {
                fgLogger.debug("comparing src:" + src + " dst:"+ dst);
            }
            
            lock.checkLock();
            
            // This means no entry on src so delete what is on dst.
            if (src == null)
            {
                String newDstPath = extendPath(dstPath, dst.getName());
                if (!excluded(matcher, null, newDstPath))
                {                	
                    sendQueue.add(new DeploymentWork(new DeploymentEvent(DeploymentEvent.Type.DELETED,
                            new Pair<Integer, String>(version, extendPath(srcPath, dst.getName())), 
                            newDstPath), ticket));
                }
                dst = null;
                continue;
            }
            // Nothing on the destination so copy over.
            if (dst == null)
            {
                if (!excluded(matcher, src.getPath(), null))
                {
                    createOnFSR(service, ticket, version, src, dstPath, matcher, sendQueue);
                }
                src = null;
                continue;
            }
            
            // Here with src and dst containing something
            int diff = src.getName().compareTo(dst.getName());
            if (diff < 0)
            {
            	// src is less than dst - must be new content in src
                if (!excluded(matcher, src.getPath(), null))
                {
                    createOnFSR(service, ticket, version, src, dstPath, matcher, sendQueue);
                }
                src = null;
                continue;
            }
            if (diff == 0)
            {
            	/**
            	 *  src and dst have same file name and GUID - nothing to do
            	 */
                if (src.getGuid().equals(dst.getGUID()))
                {                    
                    src = null;
                    dst = null;
                    continue;
                }
                
                /**
                 * src and dst are different and src is a file
                 */
                if (src.isFile())
                {
                	// this is an update to a file
                    String extendedPath = extendPath(dstPath, dst.getName());
                    if (!excluded(matcher, src.getPath(), extendedPath))
                    {
                    	// Work in progress
                    	sendQueue.add(new DeploymentWork(
                    			new DeploymentEvent(DeploymentEvent.Type.UPDATED,
                                new Pair<Integer, String>(version, src.getPath()),                              
                                extendedPath), ticket, src, version));
                    	}
                    src = null;
                    dst = null;
                    continue;
                }
                
                /**
                 * src and dst are different and src is a directory
                 */
                if (dst.getType() == FileType.DIR)
                {
                    String extendedPath = extendPath(dstPath, dst.getName());
                    
                    Set<String>stringAspects = getAspects(fAVMService, src);       	        	
                	Map<String, Serializable> stringProperties = getProperties(src, version);
                	
                	/**
                	 * Update the directory before any children
                	 */
                    service.updateDirectory(ticket, extendedPath, src.getGuid(), stringAspects, stringProperties);
                    
                    if (!excluded(matcher, src.getPath(), extendedPath))
                    {
                        deployDirectoryPushFSR(service, ticket, version, src.getPath(), extendedPath, matcher, eventQueue, sendQueue, errors, lock);
                    }
                   
                    src = null;
                    dst = null;
                    continue;
                }
                if (!excluded(matcher, src.getPath(), null))
                {
                    createOnFSR(service, ticket, version, src, dstPath, matcher, sendQueue);
                }
                src = null;
                dst = null;
                continue;
            }
            
            /**
             * diff > 0
             * Destination is missing in source, delete it.
             */ 
            String newDstPath = extendPath(dstPath, dst.getName());

            sendQueue.add(new DeploymentWork(new DeploymentEvent(DeploymentEvent.Type.DELETED,
                    new Pair<Integer, String>(version, extendPath(srcPath, dst.getName())), 
                    newDstPath), ticket));
            
            //

            dst = null;
        }
    }

    /**
     * Copy a file or directory to an empty destination on an FSR
     * @param service
     * @param ticket
     * @param report
     * @param callback
     * @param version
     * @param src
     * @param parentPath
     */
    private void createOnFSR(DeploymentReceiverService service, 
    		String ticket,
            int version, 
            AVMNodeDescriptor src, 
            String parentPath, 
            NameMatcher matcher,
    		BlockingQueue<DeploymentWork> sendQueue)
    {
        String dstPath = extendPath(parentPath, src.getName());
        
        // Need to queue the request to copy file or dir to remote.
    	sendQueue.add(new DeploymentWork(
    			new DeploymentEvent(DeploymentEvent.Type.CREATED,
                new Pair<Integer, String>(version, src.getPath()),                              
                dstPath), ticket, src, version));
    	
        if (src.isFile())
        {
            return;
        }
        
        // here if src is a directory.  
        
        // Need to create directories in controlling thread since it needs to be created  
        // BEFORE any children are written
    	Set<String>stringAspects = getAspects(fAVMService, src);       	        	
    	Map<String, Serializable> stringProperties = getProperties(src, version);
    	
    	service.createDirectory(ticket, dstPath, src.getGuid(), stringAspects, stringProperties);

        // now copy the children over
        Map<String, AVMNodeDescriptor> listing = fAVMService.getDirectoryListing(src);
        for (AVMNodeDescriptor child : listing.values())
        {
            if (!excluded(matcher, child.getPath(), null))
            {
                if (isStale(child))
                {
                    if (fgLogger.isDebugEnabled())
                    {
                        fgLogger.debug("Stale child found: " + child);
                    }
                    continue;
                }
                createOnFSR(service, ticket, version, child, dstPath, matcher, sendQueue);
            }
        }
    }
    
    private void processEvent(DeploymentEvent event,  List<DeploymentCallback> callbacks)
    {
        if (fgLogger.isDebugEnabled())
        {
            fgLogger.debug(event);
        }
        if (callbacks != null)
        {
            for (DeploymentCallback callback : callbacks)
            {
                callback.eventOccurred(event);
            }
        }
    }

    /**
     * Extend a path.
     * @param path
     * @param name
     * @return
     */
    private String extendPath(String path, String name)
    {
        if (path.endsWith("/"))
        {
            return path + name;
        }
        return path + '/' + name;
    }

    /**
     * Returns true if either srcPath or dstPath are matched by matcher.
     * @param matcher
     * @param srcPath
     * @param dstPath
     * @return
     */
    private boolean excluded(NameMatcher matcher, String srcPath, String dstPath)
    {
        return matcher != null && ((srcPath != null && matcher.matches(srcPath)) || (dstPath != null && matcher.matches(dstPath)));
    }    

    private Map<String, DeploymentReceiverTransportAdapter> deploymentReceiverTransportAdapters;
    /**
     * The deployment transport adapters provide the factories used to connect to a remote file system receiver.
     */
    public void setDeploymentReceiverTransportAdapters(Map<String, DeploymentReceiverTransportAdapter> adapters) {
    	this.deploymentReceiverTransportAdapters=adapters;
    }
    
    public Map<String, DeploymentReceiverTransportAdapter> getDeploymentReceiverTransportAdapters() {
    	return this.deploymentReceiverTransportAdapters;
    }

	public Set<String> getAdapterNames() 
	{
		if(deploymentReceiverTransportAdapters != null) {
			return(deploymentReceiverTransportAdapters.keySet());
		}	
		else 
		{
			Set<String> ret = new HashSet<String>(1);
			ret.add("default");
			return ret;
		}
	}
	
    public List<NodeRef> findLiveDeploymentServers(NodeRef webProjectRef)
    {
        return findDeploymentServers(webProjectRef, true, false);
    }

    public List<NodeRef> findTestDeploymentServers(NodeRef webProjectRef, boolean availableOnly)
    {
        return findDeploymentServers(webProjectRef, false, availableOnly);
    }

    private List<NodeRef> findDeploymentServers(NodeRef webProjectRef, boolean live, boolean availableOnly)
    {

        Path projectPath = nodeService.getPath(webProjectRef);
        String stringPath = projectPath.toPrefixString(namespacePrefixResolver);
        String serverType;

        if (live)
        {
            serverType = WCMAppModel.CONSTRAINT_LIVESERVER;
        }
        else
        {
            serverType = WCMAppModel.CONSTRAINT_TESTSERVER;
        }


        StringBuilder query = new StringBuilder("PATH:\"");

        query.append(stringPath);
        query.append("/*\" ");
        query.append(" AND @");
        query.append(NamespaceService.WCMAPP_MODEL_PREFIX);
        query.append("\\:");
        query.append(WCMAppModel.PROP_DEPLOYSERVERTYPE.getLocalName());
        query.append(":\"");
        query.append(serverType);
        query.append("\"");

        // if required filter the test servers
        if (live == false && availableOnly)
        {
            query.append(" AND ISNULL:\"");
            query.append(WCMAppModel.PROP_DEPLOYSERVERALLOCATEDTO.toString());
            query.append("\"");
        }

        if (fgLogger.isDebugEnabled())
            fgLogger.debug("Finding deployment servers using query: " + query.toString());

        // execute the query
        ResultSet results = null;
        List<NodeRef> servers = new ArrayList<NodeRef>();
        try
        {
            results = searchService.query(webProjectRef.getStoreRef(),
                    SearchService.LANGUAGE_LUCENE, query.toString());

            if (fgLogger.isDebugEnabled())
                fgLogger.debug("Found " + results.length() + " deployment servers");

            for (NodeRef server : results.getNodeRefs())
            {
                servers.add(server);
            }
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }

        return servers;
    }

	public void setNumberOfSendingThreads(int numberOfSendingThreads) {
		this.numberOfSendingThreads = numberOfSendingThreads;
	}

	public int getNumberOfSendingThreads() {
		return numberOfSendingThreads;
	}

	public void setJobLockService(JobLockService jobLockService) {
		this.jobLockService = jobLockService;
	}

	public JobLockService getJobLockService() {
		return jobLockService;
	}

	public void setTargetLockTimeToLive(long targetLockTimeToLive) {
		this.targetLockTimeToLive = targetLockTimeToLive;
	}

	public long getTargetLockTimeToLive() {
		return targetLockTimeToLive;
	}

	public void setTargetLockRetryWait(long targetLockRetryWait) {
		this.targetLockRetryWait = targetLockRetryWait;
	}

	public long getTargetLockRetryWait() {
		return targetLockRetryWait;
	}

	public void setTargetLockRetryCount(int targetLockRetryCount) {
		this.targetLockRetryCount = targetLockRetryCount;
	}

	public int getTargetLockRetryCount() {
		return targetLockRetryCount;
	}
	
	public void setAvmNodeService(AVMNodeService fAVMNodeService) {
		this.fAVMNodeService = fAVMNodeService;
	}

	public AVMNodeService getAvmNodeService() {
		return fAVMNodeService;
	}

	public void setOutputBufferSize(int outputBufferSize) {
		this.outputBufferSize = outputBufferSize;
	}

	public int getOutputBufferSize() {
		return outputBufferSize;
	}

	/**
	 * This thread processes the event queue to do the callbacks
	 * @author mrogers
	 *
	 */
	private class EventQueueWorker extends Thread
	{
		private BlockingQueue<DeploymentEvent> eventQueue;
		private List<DeploymentCallback> callbacks;
		private String userName;
		
		private boolean stopMe = false;
		
		EventQueueWorker(String userName, BlockingQueue<DeploymentEvent> eventQueue, List<DeploymentCallback> callbacks)
		{
			this.eventQueue = eventQueue;
			this.callbacks = callbacks;
			this.userName = userName;
		}
		
		public void run()
		{
		    AuthenticationUtil.setFullyAuthenticatedUser(userName);
		    
			while (true)
			{
				DeploymentEvent event = null;
				try {
					event = eventQueue.poll(3, TimeUnit.SECONDS);
				} catch (InterruptedException e1) {
					fgLogger.debug("Interrupted ", e1);
				}
		
				if(event == null) 
				{
					if(stopMe) 
					{
						fgLogger.debug("Event Queue Closing Normally");
						break;
					}
					continue;
				}
				
				if (fgLogger.isDebugEnabled())
		        {
		            fgLogger.debug(event);
		        }
		        if (callbacks != null)
		        {
		            for (DeploymentCallback callback : callbacks)
		            {
		                callback.eventOccurred(event);
		            }
		        }
			}
		}
		
		public void stopMeWhenIdle() 
		{
			stopMe = true;
		}
		
		
		
	}
	
	/**
	 * Inner Class to Decorate the jobLockService to 
	 * add control over the refreshLock behaviour.
	 * 
	 * Deployment service calls (On deployment main thread)
	 * makeLock and releaseLock around the deployment.
	 * periodically calls checkLock as it does its work.
	 * checkLock can throw an exception if the business process has timed out.
	 * 
	 * isActive and lockReleased called by Job Lock Thread
	 */
	private class Lock implements JobLockRefreshCallback
	{	
	    /**
	     * The name of the lock - unique for each target
	     */
		QName lockQName;
		
		/**
		 * The unique token for this lock instance.
		 */
		String lockToken;
		
		/**
		 * Is the lock active ?
		 */
		boolean active = false;
		
		/**
		 * When did we last check whether the lock is active
		 */
		Date lastActive = new Date();
		
		public Lock(QName lockQName)
		{
			this.lockQName = lockQName;
		}
		
		/**
		 * Make the lock - called on main deployment thread
		 * 
		 * @throws LockAquisitionException
		 */
		public void makeLock()
		{
		    if(fgLogger.isDebugEnabled())
		    {
		        fgLogger.debug("target lock refresh time :" + getTargetLockRefreshTime() + "targetLockRetryWait:" + targetLockRetryWait + "targetLockRetryCount:" + targetLockRetryCount);
		    }
		    lockToken = jobLockService.getLock(lockQName, targetLockRefreshTime, targetLockRetryWait, targetLockRetryCount);
		
		    synchronized(this)
		    {
		        active = true;
		    }
			if (fgLogger.isDebugEnabled())
            {
	            fgLogger.debug("lock taken:" + lockQName);
            }
			
			// We may have taken so long to begin that we have already timed out !
			checkLock();
			
			fgLogger.debug("register lock callback, target lock refresh time :" + getTargetLockRefreshTime());
			jobLockService.refreshLock(lockToken, lockQName, getTargetLockRefreshTime(), this);
			fgLogger.debug("callback registered");
		}
		
		/**
		 * Refresh the lock - called as the business process progresses.
		 * 
		 * Called on main deployment thread.
		 * @throws AVMException (Lock timeout)
		 */
		public void checkLock()
		{
		    // Do I need to sync this?
		    
		    if(active)
		    {
		        Date now = new Date();
		        
		        if(now.getTime() > lastActive.getTime() + targetLockTimeToLive)
		        {
		            // lock time to live has expired.
	                MessageFormat f = new MessageFormat("Deployment Lock timeout, lock time to live exceeded, timeout:{0}mS time since last activity:{1}mS");
	                Object[] objs = {new Long(targetLockTimeToLive), new Long(now.getTime() - lastActive.getTime()) };
	                throw new AVMException(f.format(objs));
		        }
		        
		        // Update lastActive to 1S boundary
		        if(now.getTime() > lastActive.getTime() + 1000)
		        {
		            lastActive = new Date();
		            fgLogger.debug("lastActive:" + lastActive);
		        }
		    }
		    else
		    {
		        // lock not active.   Has been switched off by Job Lock Service.
                MessageFormat f = new MessageFormat("Lock timeout, lock not active");
                Object[] objs = { };
                throw new AVMException(f.format(objs));
		    }
		}
		
		/**
		 * Release the lock
		 * 
		 * Called on main deployment thread
		 */
		public void releaseLock()
		{
		    if(fgLogger.isDebugEnabled())
		    {
		        fgLogger.debug("deployment service about to releaseLock : " + lockQName);
		    }
		    if(active)
		    {
		        jobLockService.releaseLock(lockToken, lockQName);
		    }
		    fgLogger.debug("setting active = false" + lockQName);
	        
		    // may need to sync this
		    synchronized(this)
		    {
		        active = false;
		    }
		}

		/**
		 * Job Lock Callback
		 * 
		 * Callback from the job lock service.   Is the deployment active?
		 */
        @Override
        public boolean isActive()
        {
            Date now = new Date();
            
            synchronized(this)
            {
                if(now.getTime() > lastActive.getTime() + targetLockTimeToLive)
                {
                    active = false;
                }
            
                // may need to sync active flag
                if(fgLogger.isDebugEnabled())
                {
                    fgLogger.debug("deployment service callback active: " + active);
                }
            
                return active;
            }
        }

        /**
         * Job Lock Callback.
         */
        @Override
        public void lockReleased()
        {
            fgLogger.debug("deployment service: lock released callback");
            synchronized(this)
            {
                active = false;
            }
        }

	}
	

	
	/**
	 * This thread processes the send queue
	 * @author mrogers
	 *
	 */
	private class SendQueueWorker extends Thread
	{
		private BlockingQueue<DeploymentEvent> eventQueue;
		private BlockingQueue<DeploymentWork> sendQueue;
		private DeploymentReceiverService service;
		private String userName;
		private AVMService avmService;
		private TransactionService trxService;
		List<Exception> errors;
		List<DeploymentTransportOutputFilter> transformers;
		
		private boolean stopMe = false;
		
		SendQueueWorker(String userName,
				DeploymentReceiverService service,
				AVMService avmService,
				TransactionService trxService,
				List<Exception> errors,
				BlockingQueue<DeploymentEvent> eventQueue, 
				BlockingQueue<DeploymentWork> sendQueue,
				List<DeploymentTransportOutputFilter> transformers
				)
		{
			this.eventQueue = eventQueue;
			this.sendQueue = sendQueue;
			this.service = service;
			this.avmService = avmService;
			this.trxService = trxService;
			this.errors = errors;
			this.transformers = transformers;
			this.userName = userName;
		}
		
		public void run()
		{
		    AuthenticationUtil.setFullyAuthenticatedUser(userName);
            
			while (errors.size() <= 0)
			{
				DeploymentWork work = null;
				try {
					work = sendQueue.poll(3, TimeUnit.SECONDS);
				} catch (InterruptedException e1) {
					fgLogger.debug("Interrupted ", e1);
					continue;
				}
								
				if(work == null) 
				{
					if(stopMe) 
					{	
						fgLogger.debug("Send Queue Worker Closing Normally");
						eventQueue = null;
						sendQueue = null;
						service = null;
						errors = null;
						break;
					}
				}
				
				if(work != null)
				{
					DeploymentEvent event = work.getEvent();
					String ticket = work.getTicket();
					try 
					{
						if(event.getType().equals(DeploymentEvent.Type.DELETED))
						{
							service.delete(ticket, event.getDestination());
						} 
						else if (event.getType().equals(DeploymentEvent.Type.CREATED))
						{
							AVMNodeDescriptor src = work.getSrc();
							if(src.isFile())
							{
								copyFileToFSR(src, true, work.getVersion(), event.getDestination(), ticket);
							}
							else
							{
								// Do nothing. mkdir done on main thread. 
								//makeDirectoryOnFSR(src, event.getDestination(), ticket);
							}
						}
						else if (event.getType().equals(DeploymentEvent.Type.UPDATED))
						{
							copyFileToFSR(work.getSrc(), false, work.getVersion(), event.getDestination(), ticket);
						}
						// success, now put the event onto the event queue
						eventQueue.add(event);
					}
					catch (Exception e)
					{
						errors.add(e);
					}
				}
			}
			fgLogger.debug("Send Queue Worker finished");
		}
		
		public void stopMeWhenIdle() 
		{
			stopMe = true;
		}
		
		
	   /**
	     * Create or update a single file on a remote FSR. 
	     * @param ticket
	     * @param src which file to copy
	     * @param dstPath where to copy the file
	     */
	    private void copyFileToFSR(
	            final AVMNodeDescriptor src, 
	            final boolean create,
	            final int version,
	            final String dstPath,
	            final String ticket)
	    {
	        try
	        {
	            // Perform copy within 'read only' transaction
	            RetryingTransactionHelper trx = trxService.getRetryingTransactionHelper();
	            trx.setMaxRetries(1);
	            trx.doInTransaction(new RetryingTransactionCallback<Boolean>()
                {
                    public Boolean execute() throws Exception
                    {
                    	ContentData data = avmService.getContentDataForRead(src);
         	        	InputStream in = avmService.getFileInputStream(src);
        	        	String encoding = data.getEncoding();
        	        	String mimeType = data.getMimetype();
  
        	        	Set<String>stringAspects = getAspects(avmService, src);       	        	
        	        	Map<String, Serializable> stringProperties = getProperties(src, version);
        	        	OutputStream out = service.send(ticket, create, dstPath, src.getGuid(), encoding, mimeType, stringAspects, stringProperties);
      
                        try
                        {
                        	// Buffer the output, we don't want to send lots of small packets
                        	out = new BufferedOutputStream(out, outputBufferSize);
        	        
                        	// Call content transformers here to transform from local to network format
                        	if(transformers != null && transformers.size() > 0) {
                        		// yes we have pay-load transformers
                        		for(DeploymentTransportOutputFilter transformer : transformers) 
                        		{
                        			out = transformer.addFilter(out, src.getPath(), encoding, mimeType);
                        		}
                        	}
        	        		        
                        	copyStream(in, out);
                        }
                        finally
                        {
                        	// whatever happens close the output stream
                        	if(out != null)
                        	{
                        		out.close();
                        		out = null;
                        	}
                        }
                        return true;
                    }
                }, true);
	        }
	        catch (Exception e)
	        {
	            fgLogger.debug("Failed to copy dstPath:" + dstPath , e);
	            
	            // throw first exception - this is the root of the problem.
	            throw new AVMException("Failed to copy filename:" + dstPath, e);
	        }
	    }
	}
    
    private boolean isStale(AVMNodeDescriptor avmRef)
    {
        // note: currently specific to WCM use-cases, eg. ETHREEOH-2758
        if ((avmRef.isLayeredDirectory() && avmRef.isPrimary()) || avmRef.isLayeredFile())
        {
            AVMNodeDescriptor srcNode = avmRef;
            
            while ((srcNode.isLayeredDirectory() && srcNode.isPrimary()) || srcNode.isLayeredFile())
            {
                AVMNodeDescriptor targetNode = fAVMService.lookup(srcNode.getIndirectionVersion(), srcNode.getIndirection());
                if (targetNode == null)
                {
                    if (srcNode.isLayeredFile() ||
                        (srcNode.isLayeredDirectory() &&
                         (! srcNode.getOpacity()) &&
                         fAVMService.getDirectoryListingDirect(srcNode, false).isEmpty()))
                    {
                        // The target node is missing
                        return true;
                    }
                    else
                    {
                        // unbacked layered dir - however opaque or not directly empty
                        return false;
                    }
                }
                srcNode = targetNode;
            }
        }
        return false;
    }

    
    public void setTargetLockRefreshTime(long targetLockRefreshTime)
    {
        this.targetLockRefreshTime = targetLockRefreshTime;
    }

    /**
     * How long to keep a lock before refreshing it?
     * <p>
     * Short time-out, typically a minute.
     * @return the time in mS for how long to keep the lock.
     */
    public long getTargetLockRefreshTime()
    {
        return targetLockRefreshTime;
    }
}
