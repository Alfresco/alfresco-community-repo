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

package org.alfresco.repo.deploy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.alfresco.deployment.DeploymentReceiverService;
import org.alfresco.deployment.DeploymentReceiverTransport;
import org.alfresco.deployment.FileDescriptor;
import org.alfresco.deployment.FileType;
import org.alfresco.deployment.impl.client.DeploymentReceiverServiceClient;
import org.alfresco.repo.action.ActionServiceRemote;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.util.SimplePath;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.remote.AVMRemoteImpl;
import org.alfresco.repo.remote.ClientTicketHolder;
import org.alfresco.repo.remote.ClientTicketHolderThread;
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
import org.alfresco.service.cmr.avm.deploy.DeploymentReport;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.cmr.remote.AVMRemoteTransport;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.NameMatcher;
import org.alfresco.util.Pair;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * Implementation of DeploymentService.
 * @author britt
 */
public class DeploymentServiceImpl implements DeploymentService
{
    /**
     * The local AVMService Instance.
     */
    private AVMService fAVMService;
    
    /**
     * The Ticket holder.
     */
    private ClientTicketHolder fTicketHolder;

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
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.deploy.DeploymentService#deployDifference(int, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, boolean, boolean)
     */
    public DeploymentReport deployDifference(int version, String srcPath, String hostName, int port, String userName, String password, String dstPath, NameMatcher matcher, boolean createDst, boolean dontDelete, boolean dontDo, DeploymentCallback callback)
    {
        try
        {
            DeploymentReport report = new DeploymentReport();
            AVMRemote remote = getRemote(hostName, port, userName, password);
            if (callback != null)
            {
                DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.START, 
                                                            new Pair<Integer, String>(version, srcPath),
                                                            dstPath);
                callback.eventOccurred(event);
            }
            if (version < 0)
            {
                String storeName = srcPath.substring(0, srcPath.indexOf(":"));
                version = fAVMService.createSnapshot(storeName, null, null).get(storeName);
            }
            // Get the root of the deployment from this server.
            AVMNodeDescriptor srcRoot = fAVMService.lookup(version, srcPath);
            if (srcRoot == null)
            {
                throw new AVMNotFoundException("Directory Not Found: " + srcPath);
            }
            if (!srcRoot.isDirectory())
            {
                throw new AVMWrongTypeException("Not a directory: " + srcPath);
            }
            // Create a snapshot on the destination store.
            String [] storePath = dstPath.split(":");
            int snapshot = -1;
            AVMNodeDescriptor dstParent = null;
            if (!dontDo)
            {
                String[] parentBase = AVMNodeConverter.SplitBase(dstPath);
                dstParent = remote.lookup(-1, parentBase[0]);
                if (dstParent == null)
                {
                    if (createDst)
                    {
                        createDestination(remote, parentBase[0]);
                        dstParent = remote.lookup(-1, parentBase[0]);
                    }
                    else
                    {
                        throw new AVMNotFoundException("Node Not Found: " + parentBase[0]);
                    }
                }
                snapshot = remote.createSnapshot(storePath[0], "PreDeploy", "Pre Deployment Snapshot").get(storePath[0]);   
            }
            // Get the root of the deployment on the destination server.
            AVMNodeDescriptor dstRoot = remote.lookup(-1, dstPath);
            if (dstRoot == null)
            {
                // If it doesn't exist, do a copyDirectory to create it.
                DeploymentEvent event = 
                    new DeploymentEvent(DeploymentEvent.Type.COPIED,
                                        new Pair<Integer, String>(version, srcPath),
                                        dstPath);
                report.add(event);
                if (callback != null)
                {
                    callback.eventOccurred(event);
                }
                if (dontDo)
                {
                    return report;
                }
                copyDirectory(version, srcRoot, dstParent, remote, matcher);
                remote.createSnapshot(storePath[0], "Deployment", "Post Deployment Snapshot.");
                if (callback != null)
                {
                    event = new DeploymentEvent(DeploymentEvent.Type.END, 
                                                new Pair<Integer, String>(version, srcPath),
                                                dstPath);
                    callback.eventOccurred(event);
                }
                return report;
            }
            if (!dstRoot.isDirectory())
            {
                throw new AVMWrongTypeException("Not a Directory: " + dstPath);
            } 
            // The corresponding directory exists so recursively deploy.
            try
            {
                deployDirectoryPush(version, srcRoot, dstRoot, remote, matcher, dontDelete, dontDo, report, callback);
                remote.createSnapshot(storePath[0], "Deployment", "Post Deployment Snapshot.");
                if (callback != null)
                {
                    DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.END, 
                                                                new Pair<Integer, String>(version, srcPath),
                                                                dstPath);
                    callback.eventOccurred(event);
                }
                return report;
            }
            catch (AVMException e)
            {
                try
                {
                    if (snapshot != -1)
                    {
                        AVMSyncService syncService = getSyncService(hostName, port);
                        List<AVMDifference> diffs = syncService.compare(snapshot, dstPath, -1, dstPath, null);
                        syncService.update(diffs, null, false, false, true, true, "Aborted Deployment", "Aborted Deployment");
                    }
                }
                catch (Exception ee)
                {
                    throw new AVMException("Failed to rollback to version " + snapshot + " on " + hostName, ee);
                }
                throw new AVMException("Deployment to " + hostName + "failed.", e);
            }
        }
        finally
        {
            fTicketHolder.setTicket(null);
        }        
    }
    
    /**
     * Deploy all the children of corresponding directories.
     * @param src The source directory.
     * @param dst The destination directory.
     * @param remote The AVMRemote instance.
     * @param dontDelete Flag for not deleting.
     * @param dontDo Flag for dry run.
     */
    private void deployDirectoryPush(int version, 
                                     AVMNodeDescriptor src, AVMNodeDescriptor dst,
                                     AVMRemote remote, 
                                     NameMatcher matcher,
                                     boolean dontDelete, boolean dontDo,
                                     DeploymentReport report,
                                     DeploymentCallback callback)
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
        for (Map.Entry<String, AVMNodeDescriptor> entry : srcList.entrySet())
        {
            String name = entry.getKey();
            AVMNodeDescriptor srcNode = entry.getValue();
            AVMNodeDescriptor dstNode = dstList.get(name);
            if (!excluded(matcher, srcNode.getPath(), dstNode != null ? dstNode.getPath() : null))
            {
                deploySinglePush(version, srcNode, dst, dstNode, remote, matcher, dontDelete, dontDo, report, callback);
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
                    report.add(event);
                    if (callback != null)
                    {
                        callback.eventOccurred(event);
                    }
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
                                  DeploymentReport report,
                                  DeploymentCallback callback)
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
                DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.COPIED,
                                                      source,
                                                      destination);
                report.add(event);
                if (callback != null)
                {
                    callback.eventOccurred(event);
                }
                if (dontDo)
                {
                    return;
                }
                copyDirectory(version, src, dstParent, remote, matcher);
                return;
            }
            Pair<Integer, String> source = 
                new Pair<Integer, String>(version, src.getPath());
            String destination = AVMNodeConverter.ExtendAVMPath(dstParent.getPath(), src.getName());
            DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.COPIED,
                                                        source,
                                                        destination);
            report.add(event);
            if (callback != null)
            {
                callback.eventOccurred(event);
            }
            if (dontDo)
            {
                return;
            }
            // Copy a source file.
            OutputStream out = remote.createFile(dstParent.getPath(), src.getName());
            InputStream in = fAVMService.getFileInputStream(src);
            copyStream(in, out);
            copyMetadata(version, src, remote.lookup(-1, dstParent.getPath() + '/' + src.getName()), remote);
            return;
        }
        // Destination exists.
        if (src.isDirectory())
        {
            // If the destination is also a directory, recursively deploy.
            if (dst.isDirectory())
            {
                deployDirectoryPush(version, src, dst, remote, matcher, dontDelete, dontDo, report, callback);
                return;
            }
            Pair<Integer, String> source = 
                new Pair<Integer, String>(version, src.getPath());
            String destination = dst.getPath();
            DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.COPIED,
                                                        source, destination);
            report.add(event);
            if (callback != null)
            {
                callback.eventOccurred(event);
            }
            if (dontDo)
            {
                return;
            }
            remote.removeNode(dstParent.getPath(), src.getName());
            copyDirectory(version, src, dstParent, remote, matcher);
            return;
        }
        // Source is a file.
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
            report.add(event);
            if (callback != null)
            {
                callback.eventOccurred(event);
            }
            if (dontDo)
            {
                return;
            }
            InputStream in = fAVMService.getFileInputStream(src);
            OutputStream out = remote.getFileOutputStream(dst.getPath());
            copyStream(in, out);
            copyMetadata(version, src, dst, remote);
            return;
        }
        Pair<Integer, String> source =
            new Pair<Integer, String>(version, src.getPath());
        String destination = AVMNodeConverter.ExtendAVMPath(dstParent.getPath(), src.getName());
        DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.UPDATED,
                                                    source,
                                                    destination);
        report.add(event);
        if (callback != null)
        {
            callback.eventOccurred(event);
        }
        if (dontDo)
        {
            return;
        }
        // Destination is a directory and the source is a file.
        // Delete the destination directory and copy the file over.
        remote.removeNode(dstParent.getPath(), dst.getName());
        InputStream in = fAVMService.getFileInputStream(src);
        OutputStream out = remote.createFile(dstParent.getPath(), src.getName());
        copyStream(in, out);
        copyMetadata(version, src, remote.lookup(-1, dstParent.getPath() + '/' + dst.getName()), remote);
    }
    
    /**
     * Recursively copy a directory.
     * @param src
     * @param parent
     * @param remote
     */
    private void copyDirectory(int version, AVMNodeDescriptor src, AVMNodeDescriptor parent,
                               AVMRemote remote, NameMatcher matcher)
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
                // If it's a file, copy it over and move on.
                if (child.isFile())
                {
                    InputStream in = fAVMService.getFileInputStream(child);
                    OutputStream out = remote.createFile(newParent.getPath(), child.getName());
                    copyStream(in, out);
                    copyMetadata(version, child, remote.lookup(-1, newParent.getPath() + '/' + child.getName()), remote);
                    continue;
                }
                // Otherwise copy the child directory recursively.
                copyDirectory(version, child, newParent, remote, matcher);
            }
        }
    }
    
    /**
     * Utility for copying from one stream to another.
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
            out.close();
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

    private DeploymentReceiverService getReceiver(String hostName, int port)
    {
        try
        {
            RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
            factory.setRefreshStubOnConnectFailure(true);
            factory.setServiceInterface(DeploymentReceiverTransport.class);
            factory.setServiceUrl("rmi://" + hostName + ":" + port + "/deployment");
            factory.afterPropertiesSet();
            DeploymentReceiverTransport transport = (DeploymentReceiverTransport)factory.getObject();
            DeploymentReceiverServiceClient service = new DeploymentReceiverServiceClient();
            service.setDeploymentReceiverTransport(transport);
            return service;
        }
        catch (Exception e)
        {
            throw new AVMException("Could not connect to " + hostName + " at " + port, e);
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
            syncFactory.setServiceInterface(AVMSyncService.class);
            syncFactory.setServiceUrl("rmi://" + hostName + ":" + port + "/avmsync");
            syncFactory.afterPropertiesSet();
            AVMSyncService syncService = (AVMSyncService)syncFactory.getObject();
            return syncService;
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
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.deploy.DeploymentService#deployDifferenceFS(int, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, boolean, boolean)
     */
    public DeploymentReport deployDifferenceFS(int version, String srcPath, String hostName, int port, String userName, String password, String target, NameMatcher matcher, boolean createDst, boolean dontDelete, boolean dontDo, DeploymentCallback callback)
    {
        DeploymentReport report = new DeploymentReport();
        DeploymentReceiverService service = getReceiver(hostName, port);
        DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.START,
                                                    new Pair<Integer, String>(version, srcPath),
                                                    target);
        if (callback != null)
        {
            callback.eventOccurred(event);
        }
        report.add(event);
        String storeName = srcPath.substring(0, srcPath.indexOf(':'));
        System.out.println(storeName);
        if (version < 0)
        {
            version = fAVMService.createSnapshot(storeName, null, null).get(storeName);
        }
        String ticket = service.begin(target, userName, password);
        deployDirectoryPush(service, ticket, report, callback, version, srcPath, "/", matcher);
        service.commit(ticket);
        event = new DeploymentEvent(DeploymentEvent.Type.END,
                                    new Pair<Integer, String>(version, srcPath),
                                    target);
        if (callback != null)
        {
            callback.eventOccurred(event);
        }
        report.add(event);
        return report;
    }
    
    private void deployDirectoryPush(DeploymentReceiverService service, String ticket,
                                     DeploymentReport report, DeploymentCallback callback,
                                     int version,
                                     String srcPath, String dstPath, NameMatcher matcher)
    {
        Map<String, AVMNodeDescriptor> srcListing = fAVMService.getDirectoryListing(version, srcPath);
        List<FileDescriptor> dstListing = service.getListing(ticket, dstPath);
        Iterator<AVMNodeDescriptor> srcIter = srcListing.values().iterator();
        Iterator<FileDescriptor> dstIter = dstListing.iterator();
        AVMNodeDescriptor src = null;
        FileDescriptor dst = null;
        while (srcIter.hasNext() || dstIter.hasNext())
        {
            if (src == null)
            {
                if (srcIter.hasNext())
                {
                    src = srcIter.next();
                }
            }
            if (dst == null)
            {
                if (dstIter.hasNext())
                {
                    dst = dstIter.next();
                }
            }
            // This means no entry on src so delete.
            if (src == null)
            {
                String newDstPath = extendPath(dstPath, dst.getName());
                if (!excluded(matcher, null, newDstPath))
                {
                    service.delete(ticket, newDstPath);
                    DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.DELETED, 
                                                                new Pair<Integer, String>(version, extendPath(srcPath, dst.getName())),
                                                                newDstPath);
                    if (callback != null)
                    {
                        callback.eventOccurred(event);
                    }
                    report.add(event);
                }
                dst = null;
                continue;
            }
            // Nothing on the destination so copy over.
            if (dst == null)
            {
                if (!excluded(matcher, src.getPath(), null))
                {
                    copy(service, ticket, report, callback, version, src, dstPath, matcher);
                }
                src = null;
                continue;
            }
            int diff = src.getName().compareTo(dst.getName());
            if (diff < 0)
            {
                if (!excluded(matcher, src.getPath(), null))
                {
                    copy(service, ticket, report, callback, version, src, dstPath, matcher);
                }
                src = null;
                continue;
            }
            if (diff == 0)
            {
                if (src.getGuid().equals(dst.getGUID()))
                {
                    src = null;
                    dst = null;
                    continue;
                }
                if (src.isFile())
                {
                    String extendedPath = extendPath(dstPath, dst.getName());
                    if (!excluded(matcher, src.getPath(), extendedPath))
                    {
                        copyFile(service, ticket, report, callback, version, src,
                                 extendedPath);
                    }
                    src = null;
                    dst = null;
                    continue;
                }
                // Source is a directory.
                if (dst.getType() == FileType.DIR)
                {
                    if (!dstPath.equals("/"))
                    {
                        service.setGuid(ticket, dstPath, src.getGuid());
                    }
                    String extendedPath = extendPath(dstPath, dst.getName());
                    if (!excluded(matcher, src.getPath(), extendedPath))
                    {
                        deployDirectoryPush(service, ticket, report, callback, version, src.getPath(), extendPath(dstPath, dst.getName()), matcher);
                    }
                    src = null;
                    dst = null;
                    continue;
                }
                if (!excluded(matcher, src.getPath(), null))
                {
                    copy(service, ticket, report, callback, version, src, dstPath, matcher);
                }
                src = null;
                dst = null;
                continue;
            }
            // diff > 0
            // Destination is missing in source, delete it.
            String newDstPath = extendPath(dstPath, dst.getName());
            service.delete(ticket, newDstPath);
            DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.DELETED,
                                                        new Pair<Integer, String>(version, extendPath(srcPath, dst.getName())),
                                                        newDstPath);
            if (callback != null) 
            {
                callback.eventOccurred(event);
            }
            report.add(event);
            dst = null;
        }
    }
    
    /**
     * Copy or overwrite a single file.
     * @param service
     * @param ticket
     * @param report
     * @param callback
     * @param version
     * @param src
     * @param dstPath
     */
    private void copyFile(DeploymentReceiverService service, String ticket, 
                          DeploymentReport report, DeploymentCallback callback, int version,
                          AVMNodeDescriptor src, String dstPath)
    {
        InputStream in = fAVMService.getFileInputStream(src);
        OutputStream out = service.send(ticket, dstPath, src.getGuid());
        try
        {
            copyStream(in, out);
            service.finishSend(ticket, out);
            DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.COPIED,
                                                        new Pair<Integer, String>(version, src.getPath()),
                                                        dstPath);
            if (callback != null)
            {
                callback.eventOccurred(event);
            }
            report.add(event);
        }
        catch (Exception e)
        {
            service.abort(ticket);
            throw new AVMException("Failed to copy " + src + ". Deployment aborted.", e);
        }
    }
    
    /**
     * Copy a file or directory to an empty destination.
     * @param service
     * @param ticket
     * @param report
     * @param callback
     * @param version
     * @param src
     * @param parentPath
     */
    private void copy(DeploymentReceiverService service, String ticket,
                      DeploymentReport report, DeploymentCallback callback, 
                      int version, AVMNodeDescriptor src, String parentPath, NameMatcher matcher)
    {
        String dstPath = extendPath(parentPath, src.getName());
        if (src.isFile())
        {
            copyFile(service, ticket, report, callback, version, src, dstPath);
            return;
        }
        // src is a directory.
        service.mkdir(ticket, dstPath, src.getGuid());
        DeploymentEvent event = new DeploymentEvent(DeploymentEvent.Type.COPIED,
                                                    new Pair<Integer, String>(version, src.getPath()),
                                                    dstPath);
        if (callback != null)
        {
            callback.eventOccurred(event);
        }
        report.add(event);
        Map<String, AVMNodeDescriptor> listing = fAVMService.getDirectoryListing(src);
        for (AVMNodeDescriptor child : listing.values())
        {
            if (!excluded(matcher, child.getPath(), null))
            {
                copy(service, ticket, report, callback, version, child, dstPath, matcher);
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
}
