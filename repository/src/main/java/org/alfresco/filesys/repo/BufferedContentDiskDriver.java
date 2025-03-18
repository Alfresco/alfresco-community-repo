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
package org.alfresco.filesys.repo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigElement;

import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSizeInterface;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.IOControlNotImplementedException;
import org.alfresco.jlan.server.filesys.IOCtlInterface;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.SrvDiskInfo;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.FileStateCache;
import org.alfresco.jlan.server.locking.FileLockingInterface;
import org.alfresco.jlan.server.locking.LockManager;
import org.alfresco.jlan.server.locking.OpLockInterface;
import org.alfresco.jlan.server.locking.OpLockManager;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.util.DataBuffer;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.util.PropertyCheck;

/**
 * Alfresco Content Disk Driver Cache
 * <p>
 * Decorates ContentDiskDriver with a performance cache of some frequently used results. In particular for getFileInformation and fileExists
 */
/* MER - this class is also acting as a proxy to gather together the different interfaces and present them to JLAN. This was not the intention and is a short term hack. It should be possible to un-spring the buffering, however that's not possible at the moment. */
public class BufferedContentDiskDriver implements ExtendedDiskInterface,
        DiskInterface,
        DiskSizeInterface,
        IOCtlInterface,
        OpLockInterface,
        FileLockingInterface,
        NodeServicePolicies.OnDeleteNodePolicy,
        NodeServicePolicies.OnMoveNodePolicy
{
    // Logging
    private static final Log logger = LogFactory.getLog(BufferedContentDiskDriver.class);

    private ExtendedDiskInterface diskInterface;

    private DiskSizeInterface diskSizeInterface;

    private IOCtlInterface ioctlInterface;

    private OpLockInterface opLockInterface;

    private FileLockingInterface fileLockingInterface;

    private PolicyComponent policyComponent;

    public void init()
    {
        PropertyCheck.mandatory(this, "diskInterface", diskInterface);
        PropertyCheck.mandatory(this, "diskSizeInterface", diskSizeInterface);
        PropertyCheck.mandatory(this, "ioctltInterface", ioctlInterface);
        PropertyCheck.mandatory(this, "fileInfoCache", fileInfoCache);
        PropertyCheck.mandatory(this, "fileLockingInterface", getFileLockingInterface());
        PropertyCheck.mandatory(this, "opLockInterface", getOpLockInterface());
        PropertyCheck.mandatory(this, "fileLockingInterface", fileLockingInterface);
        PropertyCheck.mandatory(this, "policyComponent", getPolicyComponent());

        getPolicyComponent().bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                this, new JavaBehaviour(this, "onDeleteNode"));
        getPolicyComponent().bindClassBehaviour(NodeServicePolicies.OnMoveNodePolicy.QNAME,
                this, new JavaBehaviour(this, "onMoveNode"));
    }

    /**
     * FileInfo Cache for path to FileInfo
     */
    private SimpleCache<Serializable, FileInfo> fileInfoCache;

    /**
     * Set the cache that maintains node ID-NodeRef cross referencing data
     * 
     * @param cache
     *            the cache
     */
    public void setFileInfoCache(SimpleCache<Serializable, FileInfo> cache)
    {
        this.fileInfoCache = cache;
    }

    private static class FileInfoKey implements Serializable
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        String deviceName;
        String path;
        String user;
        int hashCode;

        public FileInfoKey(SrvSession sess, String path, TreeConnection tree)
        {
            this.path = path;
            this.user = sess.getUniqueId();
            this.deviceName = tree.getSharedDevice().getName();

            // if(deviceName == null)
            // {
            // throw new RuntimeException("device name is null");
            // }
            // if(path == null)
            // {
            // throw new RuntimeException("path is null");
            // }
            // if(user == null)
            // {
            // throw new RuntimeException("unique id is null");
            // }
        }

        @Override
        public boolean equals(Object other)
        {
            if (this == other)
            {
                return true;
            }
            if (other == null || !(other instanceof FileInfoKey))
            {
                return false;
            }

            FileInfoKey o = (FileInfoKey) other;

            return path.equals(o.path) && user.equals(o.user) && deviceName.equals(o.deviceName);
        }

        @Override
        public int hashCode()
        {
            if (hashCode == 0)
            {
                hashCode = (user + path + deviceName).hashCode();
            }
            return hashCode;
        }
    }

    private FileInfo getFileInformationInternal(SrvSession sess, TreeConnection tree,
            String path) throws IOException
    {

        // String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        SharedDevice device = tree.getSharedDevice();
        String deviceName = device.getName();

        if (logger.isDebugEnabled())
        {
            logger.debug("getFileInformation session:" + sess.getUniqueId() + ", deviceName:" + deviceName + ", path:" + path);
        }

        if (path == null)
        {
            throw new IllegalArgumentException("Path is null");
        }

        FileInfoKey key = new FileInfoKey(sess, path, tree);

        FileInfo fromCache = fileInfoCache.get(key);

        if (fromCache != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("returning FileInfo from cache");
            }
            return fromCache;
        }

        FileInfo info = diskInterface.getFileInformation(sess, tree, path);

        if (info != null)
        {
            /**
             * Don't cache directories since the modification date is important.
             */
            if (!info.isDirectory())
            {
                fileInfoCache.put(key, info);
            }
        }

        /* Dual Key the cache so it can be looked up by NodeRef or Path */
        if (info instanceof ContentFileInfo)
        {
            ContentFileInfo cinfo = (ContentFileInfo) info;
            fileInfoCache.put(cinfo.getNodeRef(), info);
        }

        return info;
    }

    @Override
    public FileInfo getFileInformation(SrvSession sess, TreeConnection tree,
            String path) throws IOException
    {
        ContentContext tctx = (ContentContext) tree.getContext();

        FileInfo info = getFileInformationInternal(sess, tree, path);

        /* Some information is not maintained by the repo and represents an in-progress update. For example as a file is being written the modification and access dates change. */
        if (tctx.hasStateCache())
        {
            FileStateCache cache = tctx.getStateCache();
            FileState fstate = cache.findFileState(path, false);
            if (fstate != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("state cache available - overwriting from state cache: isDirectory=" + info.isDirectory());
                }
                FileInfo finfo = new FileInfo();
                finfo.copyFrom(info);

                /**
                 * File state is probably stale for directories which is why we don't attempt to cache.
                 */
                if (!info.isDirectory())
                {
                    /* What about stale file state values here? */
                    if (fstate.hasFileSize())
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("replace file size " + info.getSize() + " with " + fstate.getFileSize());
                        }
                        finfo.setFileSize(fstate.getFileSize());
                    }
                    if (fstate.hasAccessDateTime())
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("replace access date " + new Date(finfo.getAccessDateTime()) + " with " + new Date(fstate.getAccessDateTime()));
                        }
                        finfo.setAccessDateTime(fstate.getAccessDateTime());
                    }
                    if (fstate.hasChangeDateTime())
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("replace change date " + new Date(finfo.getChangeDateTime()) + " with " + new Date(fstate.getChangeDateTime()));
                        }
                        finfo.setChangeDateTime(fstate.getChangeDateTime());
                    }
                    if (fstate.hasModifyDateTime())
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("replace modified date " + new Date(finfo.getModifyDateTime()) + " with " + new Date(fstate.getModifyDateTime()));
                        }
                        finfo.setModifyDateTime(fstate.getModifyDateTime());
                    }
                    if (fstate.hasAllocationSize())
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("replace allocation size" + finfo.getAllocationSize() + " with " + fstate.getAllocationSize());
                        }
                        finfo.setAllocationSize(fstate.getAllocationSize());
                    }
                }

                if (logger.isDebugEnabled())
                {
                    logger.debug("Return getFileInformation, path: " + path +
                            ", returning:" + finfo +
                            ", readOnly:" + finfo.isReadOnly() +
                            ", fileId:" + finfo.getFileId() +
                            ", fileSize:" + finfo.getSize() +
                            ", directoryId:" + finfo.getDirectoryId() +
                            ", createdDate: " + new Date(finfo.getCreationDateTime()) +
                            ", accessDate:" + new Date(finfo.getAccessDateTime()) +
                            ", modifiedDate:" + new Date(finfo.getModifyDateTime()) +
                            ", changeDate:" + new Date(finfo.getChangeDateTime()) +
                            ", fileAttributes: 0x" + Integer.toHexString(info.getFileAttributes()) +
                            ", mode: 0x" + Integer.toHexString(finfo.getMode()));
                }

                return finfo;
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("getFileInformation Return:" + path + " returning" + info);
        }

        return info;

    }

    @Override
    public int fileExists(SrvSession sess, TreeConnection tree, String path)
    {
        String deviceName = tree.getSharedDevice().getName();

        if (logger.isDebugEnabled())
        {
            logger.debug("fileExists session:" + sess.getUniqueId() + ", deviceName" + deviceName + ", path:" + path);
        }

        FileInfoKey key = new FileInfoKey(sess, path, tree);

        FileInfo fromCache = fileInfoCache.get(key);

        if (fromCache != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("fileExists found FileInfo in cache");
            }
            if (fromCache.isDirectory())
            {
                return FileStatus.DirectoryExists;
            }
            else
            {
                return FileStatus.FileExists;
            }
        }
        else
        {
            try
            {
                FileInfo lookup = getFileInformationInternal(sess, tree, path);

                if (logger.isDebugEnabled())
                {
                    logger.debug("fileExists obtained file information");
                }
                if (lookup.isDirectory())
                {
                    return FileStatus.DirectoryExists;
                }
                else
                {
                    return FileStatus.FileExists;
                }
            }
            catch (IOException ie)
            {
                return FileStatus.NotExist;
            }
        }
    }

    @Override
    public DeviceContext createContext(String shareName, ConfigElement args)
            throws DeviceContextException
    {
        return diskInterface.createContext(shareName, args);
    }

    @Override
    public void treeOpened(SrvSession sess, TreeConnection tree)
    {
        diskInterface.treeOpened(sess, tree);
    }

    @Override
    public void treeClosed(SrvSession sess, TreeConnection tree)
    {
        diskInterface.treeClosed(sess, tree);
    }

    @Override
    public DataBuffer processIOControl(SrvSession sess, TreeConnection tree,
            int ctrlCode, int fid, DataBuffer dataBuf, boolean isFSCtrl,
            int filter) throws IOControlNotImplementedException, SMBException
    {
        return ioctlInterface.processIOControl(sess, tree, ctrlCode, fid, dataBuf, isFSCtrl, filter);
    }

    @Override
    public void getDiskInformation(DiskDeviceContext ctx, SrvDiskInfo diskDev)
            throws IOException
    {
        diskSizeInterface.getDiskInformation(ctx, diskDev);
    }

    @Override
    public void closeFile(SrvSession sess, TreeConnection tree,
            NetworkFile param) throws IOException
    {
        diskInterface.closeFile(sess, tree, param);

        /**
         * If the fileInfo cache may have just had some content updated.
         */
        if (!param.isDirectory() && !param.isReadOnly())
        {
            fileInfoCache.clear();
        }
    }

    @Override
    public void createDirectory(SrvSession sess, TreeConnection tree,
            FileOpenParams params) throws IOException
    {
        diskInterface.createDirectory(sess, tree, params);
    }

    @Override
    public NetworkFile createFile(SrvSession sess, TreeConnection tree,
            FileOpenParams params) throws IOException
    {
        return diskInterface.createFile(sess, tree, params);
    }

    @Override
    public void deleteDirectory(SrvSession sess, TreeConnection tree, String dir)
            throws IOException
    {
        fileInfoCache.remove(dir);

        diskInterface.deleteDirectory(sess, tree, dir);
    }

    @Override
    public void deleteFile(SrvSession sess, TreeConnection tree, String name)
            throws IOException
    {
        fileInfoCache.remove(name);

        diskInterface.deleteFile(sess, tree, name);
    }

    @Override
    public void flushFile(SrvSession sess, TreeConnection tree, NetworkFile file)
            throws IOException
    {
        diskInterface.flushFile(sess, tree, file);
    }

    @Override
    public boolean isReadOnly(SrvSession sess, DeviceContext ctx)
            throws IOException
    {
        return diskInterface.isReadOnly(sess, ctx);
    }

    @Override
    public NetworkFile openFile(SrvSession sess, TreeConnection tree,
            FileOpenParams params) throws IOException
    {
        return diskInterface.openFile(sess, tree, params);
    }

    @Override
    public int readFile(SrvSession sess, TreeConnection tree, NetworkFile file,
            byte[] buf, int bufPos, int siz, long filePos) throws IOException
    {
        return diskInterface.readFile(sess, tree, file, buf, bufPos, siz, filePos);
    }

    @Override
    public void renameFile(SrvSession sess, TreeConnection tree,
            String oldName, String newName) throws IOException
    {
        diskInterface.renameFile(sess, tree, oldName, newName);
    }

    @Override
    public long seekFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, long pos, int typ) throws IOException
    {
        return diskInterface.seekFile(sess, tree, file, pos, typ);
    }

    @Override
    public void setFileInformation(SrvSession sess, TreeConnection tree,
            String name, FileInfo info) throws IOException
    {
        diskInterface.setFileInformation(sess, tree, name, info);
    }

    @Override
    public SearchContext startSearch(SrvSession sess, TreeConnection tree,
            String searchPath, int attrib) throws FileNotFoundException
    {
        return diskInterface.startSearch(sess, tree, searchPath, attrib);
    }

    @Override
    public void truncateFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, long siz) throws IOException
    {
        diskInterface.truncateFile(sess, tree, file, siz);
    }

    @Override
    public int writeFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, byte[] buf, int bufoff, int siz, long fileoff)
            throws IOException
    {
        return diskInterface.writeFile(sess, tree, file, buf, bufoff, siz, fileoff);
    }

    @Override
    public void registerContext(DeviceContext ctx)
            throws DeviceContextException
    {
        diskInterface.registerContext(ctx);
    }

    public void setDiskInterface(ExtendedDiskInterface diskInterface)
    {
        this.diskInterface = diskInterface;
    }

    public ExtendedDiskInterface getDiskInterface()
    {
        return diskInterface;
    }

    public void setDiskSizeInterface(DiskSizeInterface diskSizeInterface)
    {
        this.diskSizeInterface = diskSizeInterface;
    }

    public DiskSizeInterface getDiskSizeInterface()
    {
        return diskSizeInterface;
    }

    public void setIoctlInterface(IOCtlInterface iocltlInterface)
    {
        this.ioctlInterface = iocltlInterface;
    }

    public IOCtlInterface getIoctlInterface()
    {
        return ioctlInterface;
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef,
            ChildAssociationRef newChildAssocRef)
    {
        if (fileInfoCache.contains(oldChildAssocRef.getChildRef()))
        {
            logger.debug("cached node moved - clear the cache");
            fileInfoCache.clear();
        }
    }

    @Override
    public void onDeleteNode(ChildAssociationRef oldChildAssocRef, boolean isArchived)
    {
        if (fileInfoCache.contains(oldChildAssocRef.getChildRef()))
        {
            logger.debug("cached node deleted - clear the cache");
            fileInfoCache.clear();
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public PolicyComponent getPolicyComponent()
    {
        return policyComponent;
    }

    public void setOpLockInterface(OpLockInterface opLockInterface)
    {
        this.opLockInterface = opLockInterface;
    }

    public OpLockInterface getOpLockInterface()
    {
        return opLockInterface;
    }

    @Override
    public OpLockManager getOpLockManager(SrvSession sess, TreeConnection tree)
    {
        return opLockInterface.getOpLockManager(sess, tree);
    }

    @Override
    public boolean isOpLocksEnabled(SrvSession sess, TreeConnection tree)
    {
        return opLockInterface.isOpLocksEnabled(sess, tree);
    }

    @Override
    public LockManager getLockManager(SrvSession sess, TreeConnection tree)
    {
        return getFileLockingInterface().getLockManager(sess, tree);
    }

    public void setFileLockingInterface(FileLockingInterface fileLockingInterface)
    {
        this.fileLockingInterface = fileLockingInterface;
    }

    public FileLockingInterface getFileLockingInterface()
    {
        return fileLockingInterface;
    }
}
