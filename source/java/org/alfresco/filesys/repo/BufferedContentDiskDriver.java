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
package org.alfresco.filesys.repo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import org.alfresco.filesys.config.ServerConfigurationBean;
import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
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
import org.alfresco.jlan.server.locking.OpLockInterface;
import org.alfresco.jlan.server.locking.OpLockManager;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.util.DataBuffer;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigElement;

/**
 * Alfresco Content Disk Driver Cache
 * <p>
 * Decorates ContentDiskDriver with a performance cache of some frequently used 
 * results.   In particular for getFileInformation and fileExists
 */
public class BufferedContentDiskDriver implements ExtendedDiskInterface, 
    DiskInterface, DiskSizeInterface, IOCtlInterface, OpLockInterface, NodeServicePolicies.OnDeleteNodePolicy,
    NodeServicePolicies.OnMoveNodePolicy 
{
    // Logging
    private static final Log logger = LogFactory.getLog(BufferedContentDiskDriver.class);
    
    private ExtendedDiskInterface diskInterface;
    
    private DiskSizeInterface diskSizeInterface;
    
    private IOCtlInterface ioctlInterface;
    
    private OpLockInterface opLockInterface;
    
    private PolicyComponent policyComponent;
        
    public void init()
    {
        PropertyCheck.mandatory(this, "diskInterface", diskInterface);
        PropertyCheck.mandatory(this, "diskSizeInterface", diskSizeInterface);
        PropertyCheck.mandatory(this, "ioctltInterface", ioctlInterface);
        PropertyCheck.mandatory(this, "fileInfoCache", fileInfoCache);
        PropertyCheck.mandatory(this, "opLockInterface", getOpLockInterface());
        PropertyCheck.mandatory(this, "policyComponent", getPolicyComponent());
        
        getPolicyComponent().bindClassBehaviour( NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                this, new JavaBehaviour(this, "onDeleteNode"));   
        getPolicyComponent().bindClassBehaviour( NodeServicePolicies.OnMoveNodePolicy.QNAME,
                this, new JavaBehaviour(this, "onMoveNode"));
    }
    

    
    /**
     * FileInfo Cache for path to FileInfo
     */    
    private SimpleCache<Serializable, FileInfo> fileInfoCache;
    
    /**
     * Set the cache that maintains node ID-NodeRef cross referencing data
     * 
     * @param cache                 the cache
     */
    public void setFileInfoCache(SimpleCache<Serializable, FileInfo> cache)
    {
        this.fileInfoCache = cache;
    }
    
    private class FileInfoKey implements Serializable
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        String path;
        String user;
        int hashCode;
        
        public FileInfoKey(String user, String path)
        {
            this.path = path;
            this.user = user;
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
              
           FileInfoKey o = (FileInfoKey)other;
           
           return path.equals(o.path) && user.equals(o.user);
        }     
          
        @Override
        public int hashCode()
        {
            if(hashCode == 0)
            {
                hashCode = (user+path).hashCode();
            }
            return hashCode;
        }
    }
    
    @Override
    public FileInfo getFileInformation(SrvSession sess, TreeConnection tree,
            String path) throws IOException
    {
               
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
       
        if(logger.isDebugEnabled())
        {
            logger.debug("getFileInformation userName:" + userName + ", path:" + path);
        }
        
        if(path == null)
        {
            throw new IllegalArgumentException("Path is null");
        }
        
        FileInfoKey key = new FileInfoKey(userName, path);
        
        FileInfo fromCache = fileInfoCache.get(key);
        
        if(fromCache != null)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("returning FileInfo from cache");
            }
            return fromCache;
        }
        
        FileInfo info = diskInterface.getFileInformation(sess, tree, path);
        
        if(info != null)
        {
            fileInfoCache.put(key, info);
        }
        
        /*
         * Dual Key the cache so it can be looked up by NodeRef or Path
         */
        if(info instanceof ContentFileInfo)
        {
            ContentFileInfo cinfo = (ContentFileInfo)info;
            fileInfoCache.put(cinfo.getNodeRef(), info);
            
        }
        
        return info;
    }
    
    @Override
    public int fileExists(SrvSession sess, TreeConnection tree, String path)
    {
       
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        
        if(logger.isDebugEnabled())
        {
            logger.debug("fileExists userName:" + userName + ", path:" + path);
        }
        
        FileInfoKey key = new FileInfoKey(userName, path);
        
        FileInfo fromCache = fileInfoCache.get(key);
        
        if(fromCache != null)
        {
            if(logger.isDebugEnabled())
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
                FileInfo lookup = getFileInformation(sess, tree, path);
                
                if(logger.isDebugEnabled())
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
        
        // Not in cache - use the repo directly
        //return diskInterface.fileExists(sess, tree, path);
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
    public void registerContext(DeviceContext ctx, ServerConfigurationBean scb)
            throws DeviceContextException
    {
        diskInterface.registerContext(ctx, scb);        
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
        if(fileInfoCache.contains(oldChildAssocRef.getChildRef()))
        {
            logger.debug("cached node moved - clear the cache");
            fileInfoCache.clear();
        }
    }

    @Override
    public void onDeleteNode(ChildAssociationRef oldChildAssocRef, boolean isArchived)
    {
        if(fileInfoCache.contains(oldChildAssocRef.getChildRef()))
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
}
  