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
import java.util.Date;

import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.filesys.alfresco.NetworkFileLegacyReferenceCount;
import org.alfresco.filesys.config.ServerConfigurationBean;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.FileAccessToken;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.FileStateCache;
import org.alfresco.jlan.server.filesys.cache.NetworkFileStateInterface;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.jlan.server.locking.FileLockingInterface;
import org.alfresco.jlan.server.locking.LockManager;
import org.alfresco.jlan.server.locking.OpLockInterface;
import org.alfresco.jlan.server.locking.OpLockManager;
import org.alfresco.jlan.smb.SharingMode;
import org.alfresco.model.ContentModel;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigElement;

/**
 * The Legacy file state driver is used to update JLAN's file state cache.
 * <p>
 * This class decorates an ExtendedDiskInterface with odds and ends to keep JLan happy.  
 * <p>
 * In particular this implementation cannot contain any code that requires access to the 
 * alfresco repository.
 * 
 */
public class LegacyFileStateDriver implements ExtendedDiskInterface
{
    private ExtendedDiskInterface diskInterface;
    
    private OpLockInterface opLockInterface;
    
    private FileLockingInterface fileLockingInterface; 
          
    public void init()
    {
        PropertyCheck.mandatory(this, "diskInterface", diskInterface);
        PropertyCheck.mandatory(this, "fileLockingInterface", fileLockingInterface);
        PropertyCheck.mandatory(this, "opLockInterface", getOpLockInterface());
    }
    
    private static final Log logger = LogFactory.getLog(LegacyFileStateDriver.class);

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
    public NetworkFile createFile(SrvSession sess, TreeConnection tree,
            FileOpenParams params) throws IOException
    {
        ContentContext tctx = (ContentContext) tree.getContext();
        
        FileStateCache cache = null;
        FileState fstate = null;
  
        FileAccessToken token = null;
        
        if(tctx.hasStateCache())
        {
            cache = tctx.getStateCache();
            fstate = tctx.getStateCache().findFileState( params.getPath(), true);
            token = cache.grantFileAccess(params, fstate, FileStatus.NotExist);
            if(logger.isDebugEnabled())
            {
                logger.debug("create file created lock token:" + token);
            }
        }
        
        try
        {
            NetworkFile newFile = diskInterface.createFile(sess, tree, params);
            
            int openCount = 1;
            
            if(newFile instanceof NetworkFileLegacyReferenceCount)
            {
                NetworkFileLegacyReferenceCount counter = (NetworkFileLegacyReferenceCount)newFile;
                openCount = counter.incrementLegacyOpenCount();
            }
            // This is the create so we store the first access token always
            newFile.setAccessToken(token);   
          
            if(tctx.hasStateCache())
            {
                fstate.setProcessId(params.getProcessId());
                fstate.setSharedAccess( params.getSharedAccess());
            
                // Indicate that the file is open
                fstate.setFileStatus(newFile.isDirectory()? FileStatus.DirectoryExists : FileStatus.FileExists);
         
                long allocationSize = params.getAllocationSize();
                if(allocationSize > 0)
                {
                    fstate.setAllocationSize(allocationSize);
                    fstate.setFileSize(allocationSize);
                }
                
                if (newFile instanceof NodeRefNetworkFile)
                {
                    NodeRefNetworkFile x = (NodeRefNetworkFile)newFile;
                    x.setFileState(fstate);
                }
                
                if (newFile instanceof TempNetworkFile)
                {
                    TempNetworkFile x = (TempNetworkFile)newFile;
                    x.setFileState(fstate);
                }   
            }
            
            if (newFile instanceof NodeRefNetworkFile)
            {
                NodeRefNetworkFile x = (NodeRefNetworkFile)newFile;
                x.setProcessId( params.getProcessId());
                x.setAccessToken(token);
            }
            
            if (newFile instanceof TempNetworkFile)
            {
                TempNetworkFile x = (TempNetworkFile)newFile;
                x.setAccessToken(token);
            }
            
            return newFile;
            
        }
        catch(IOException ie)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("create file exception caught", ie);   
            }    
            if(tctx.hasStateCache() && token != null)
            {
                if(cache != null && fstate != null && token != null)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("create file release lock token:" + token);
                    }
                    cache.releaseFileAccess(fstate, token);
                }
            }
            throw ie;
        }
        catch (RuntimeException re)
        {
        	// we could be out of memory or a NPE or some other unforseen situation.  JLAN will complain loudly ... as it should. 
            if(logger.isDebugEnabled())
            {
                logger.debug("create file exception caught", re);   
            }    
            if(tctx.hasStateCache() && token != null)
            {
                if(cache != null && fstate != null && token != null)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("create file release lock token:" + token);
                    }
                    cache.releaseFileAccess(fstate, token);
                }
            }
            throw re;
        }
    }

    @Override
    public NetworkFile openFile(SrvSession sess, TreeConnection tree,
            FileOpenParams params) throws IOException
    {
        ContentContext tctx = (ContentContext) tree.getContext();
        String path = params.getPath();
        
        boolean rollbackOpen = false;
        boolean rollbackToken = false;
        boolean rollbackCount = false;
        boolean rollbackSetToken = false;

        FileAccessToken token = null;
        
        FileStateCache cache = null;
        FileState fstate = null;
        NetworkFile openFile = null;

        if(tctx.hasStateCache())
        {
            cache = tctx.getStateCache();
            fstate = tctx.getStateCache().findFileState( params.getPath(), true);
            
            if(!params.isDirectory())
            {
                try
                {
                    token = cache.grantFileAccess(params, fstate, FileStatus.Unknown);
                }
                catch (IOException e)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("UNABLE to grant file access for path:" + path + ", params" + params, e);
                    }
                    throw e;
                }
                
                rollbackToken = true;
                if(logger.isDebugEnabled())
                {
                    logger.debug("open file created lock token:" + token + ", for path:" + path);
                }
            }
        }

        try
        {
            openFile = diskInterface.openFile(sess, tree, params);
            rollbackOpen = true;
             
            if(openFile instanceof NetworkFileLegacyReferenceCount)
            {
                NetworkFileLegacyReferenceCount counter = (NetworkFileLegacyReferenceCount)openFile;
                int legacyOpenCount = counter.incrementLegacyOpenCount();
                if(logger.isDebugEnabled())
                {
                    logger.debug("openFile: legacyOpenCount: " + legacyOpenCount);
                }
                
                rollbackCount = true;
            }
            else
            {
                logger.debug("openFile does not implement NetworkFileLegacyReferenceCount");
            }
            
            if( openFile.hasAccessToken())
            {
                // already has an access token, release the second token
                if(cache != null && fstate != null && token != null)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("already has access token, release lock token:" + token);
                    }
                    cache.releaseFileAccess(fstate, token);
                }
            }
            else
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("store access token on open network file object token:" + token);
                }
  
                // first access token
                openFile.setAccessToken(token);
                rollbackSetToken = true;
            }
            
            if(tctx.hasStateCache())
            {
                fstate = tctx.getStateCache().findFileState( path, true);
                fstate.setProcessId(params.getProcessId());
                fstate.setSharedAccess( params.getSharedAccess());
                
                // Access date time is read/write time not open time
                // fstate.updateAccessDateTime();
                
                fstate.setFileSize(openFile.getFileSize());
                fstate.updateChangeDateTime(openFile.getModifyDate());
                fstate.updateModifyDateTime(openFile.getModifyDate());
            }
            
            if (openFile instanceof ContentNetworkFile)
            {
                ContentNetworkFile x = (ContentNetworkFile)openFile;
                x.setProcessId( params.getProcessId());

                if(fstate != null)
                {
                    x.setFileState(fstate);
                    fstate.setFileStatus(FileStatus.FileExists);
                }
            } 
            else if (openFile instanceof TempNetworkFile)
            {
                TempNetworkFile x = (TempNetworkFile)openFile;
                if(fstate != null)
                {
                    x.setFileState(fstate);
                    fstate.setFileStatus(FileStatus.FileExists);
                }
            }
            else if (openFile instanceof AlfrescoFolder)
            {
                AlfrescoFolder x = (AlfrescoFolder)openFile;
                if(fstate != null)
                {
                    x.setFileState(fstate);
                    fstate.setFileStatus(FileStatus.DirectoryExists);
                }
            }
            else if (openFile instanceof NetworkFile)
            {
                NetworkFile x = (NetworkFile)openFile;
                if(fstate != null)
                {
                    // NetworkFile does not have setFileState
                    //x.setFileState(fstate);
                    fstate.setFileStatus(FileStatus.FileExists);
                }           
            }
            
            rollbackToken = false;
            rollbackCount = false;
            rollbackSetToken = false;
            rollbackOpen = false;
            
            if(logger.isDebugEnabled())
            {
                logger.debug("successfully opened file:" + openFile);
            }

            return openFile;
        }
        finally
        {
            if(rollbackToken)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("rollback token:" + token);
                }
                if(cache != null && fstate != null && token != null)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("open file release lock token:" + token);
                    }
                    cache.releaseFileAccess(fstate, token);
                }
            }
            if(rollbackCount)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("rollback legacy open count:" + token);
                }
                if(openFile instanceof NetworkFileLegacyReferenceCount)
                {
                    NetworkFileLegacyReferenceCount counter = (NetworkFileLegacyReferenceCount)openFile;
                    counter.decrementLagacyOpenCount();
                }
            }
            if(rollbackSetToken)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("rollback set access token:" + token);
                }
                openFile.setAccessToken(null);
            }
            if(rollbackOpen)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("rollback open:" + token);
                }
                diskInterface.closeFile(sess, tree, openFile);
            }
        }
    }
    
    @Override
    public void closeFile(SrvSession sess, TreeConnection tree,
            NetworkFile file) throws IOException
    {
        ContentContext tctx = (ContentContext) tree.getContext();
        FileStateCache cache = null;
        FileState fstate = null;
   
        if(logger.isDebugEnabled())
        {
            logger.debug("closeFile:" + file.getFullName() + ", accessToken:" + file.getAccessToken());
        }
        
        int legacyOpenCount = 0;
        
        if(file instanceof NetworkFileLegacyReferenceCount)
        {
            NetworkFileLegacyReferenceCount counter = (NetworkFileLegacyReferenceCount)file;
            legacyOpenCount = counter.decrementLagacyOpenCount();
            if(logger.isDebugEnabled())
            {
                logger.debug("closeFile: legacyOpenCount=" + legacyOpenCount);
            }
        }
        else
        {
            logger.debug("file to close does not implement NetworkFileLegacyReferenceCount");
        }

        try
        {            
            if ( file.hasOpLock()) 
            {
                if ( logger.isDebugEnabled())
                {
                   logger.debug("File Has OpLock - release oplock for closed file, file=" + file.getFullName());
                }
                // Release the oplock
                
                OpLockManager oplockMgr = opLockInterface.getOpLockManager(sess, tree);
                
                oplockMgr.releaseOpLock( file.getOpLock().getPath());

                //  DEBUG
                
                if ( logger.isDebugEnabled())
                {
                   logger.debug("Released oplock for closed file, file=" + file.getFullName());
                }
            }
            
            
            //  Release any locks on the file owned by this session
            
            if ( file.hasLocks()) 
            {
                if ( logger.isDebugEnabled())
                {
                   logger.debug("Release all locks, file=" + file.getFullName());
                }
              
                LockManager lockMgr = fileLockingInterface.getLockManager(sess, tree);
                
                if(lockMgr != null)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Releasing locks for closed file, file=" + file.getFullName() + ", locks=" + file.numberOfLocks());
                    }              
                    //  Release all locks on the file owned by this session
              
                    lockMgr.releaseLocksForFile(sess, tree, file);
                }
            }

            diskInterface.closeFile(sess, tree, file);
            
            logger.debug("file closed");
            
        } 
        finally
        {
            if(tctx.hasStateCache())
            {
                cache = tctx.getStateCache();
                fstate = cache.findFileState( file.getFullName(), true);
                
                if(legacyOpenCount == 0 || file.isForce())
                {
                    if(cache != null && fstate != null && file.getAccessToken() != null)
                    {
                        FileAccessToken token = file.getAccessToken();
                        if(logger.isDebugEnabled() && token != null)
                        {
                            logger.debug("close file, legacy count == 0 release access token:" + token);
                        }
                        cache.releaseFileAccess(fstate, token);
                        file.setAccessToken( null);
                    }
                }
                
                if(fstate.getOpenCount() == 0 )
                {
                    logger.debug("fstate OpenCount == 0, reset in-flight state");
                    fstate.setAllocationSize(-1);
                    fstate.setFileSize(-1);
                    fstate.updateChangeDateTime(0);
                    fstate.updateModifyDateTime(0);    
                }
            }
        }
    }

    @Override
    public void registerContext(DeviceContext ctx) throws DeviceContextException
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

    @Override
    public void createDirectory(SrvSession sess, TreeConnection tree,
            FileOpenParams params) throws IOException
    {
        diskInterface.createDirectory(sess, tree, params);
    }

    @Override
    public void deleteDirectory(SrvSession sess, TreeConnection tree, String dir)
            throws IOException
    {
        diskInterface.deleteDirectory(sess, tree, dir);
    }

    @Override
    public void deleteFile(SrvSession sess, TreeConnection tree, String name)
            throws IOException
    {
        ContentContext tctx = (ContentContext) tree.getContext();

        diskInterface.deleteFile(sess, tree, name);
        
        if(tctx.hasStateCache())
        {
            FileStateCache cache = tctx.getStateCache();
            FileState fstate = cache.findFileState( name, false);
            
            if(fstate != null)
            {
                fstate.setFileStatus(FileStatus.NotExist);
                fstate.setOpenCount(0);
            }
        }
    }

    @Override
    public int fileExists(SrvSession sess, TreeConnection tree, String name)
    {
        return diskInterface.fileExists(sess, tree, name);
    }

    @Override
    public void flushFile(SrvSession sess, TreeConnection tree, NetworkFile file)
            throws IOException
    {
        diskInterface.flushFile(sess, tree, file);        
    }

    @Override
    public FileInfo getFileInformation(SrvSession sess, TreeConnection tree,
            String name) throws IOException
    {
        return diskInterface.getFileInformation(sess, tree, name);
    }

    @Override
    public boolean isReadOnly(SrvSession sess, DeviceContext ctx)
            throws IOException
    {
        return diskInterface.isReadOnly(sess, ctx);
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
        ContentContext tctx = (ContentContext) tree.getContext();
        
        diskInterface.renameFile(sess, tree, oldName, newName);  
        
        if(tctx.hasStateCache())
        {
            FileStateCache cache = tctx.getStateCache();
            FileState fstate = cache.findFileState( oldName, false);
            
            if(fstate != null)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("rename file state from:" + oldName + ", to:" + newName);
                }
                cache.renameFileState(newName, fstate, fstate.isDirectory());               
            }
        }
        
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
        
       ContentContext tctx = (ContentContext) tree.getContext();
        
       if(tctx.hasStateCache())
       {
           FileStateCache cache = tctx.getStateCache();
           FileState fstate = cache.findFileState( name, true);
 
//           if ( info.hasSetFlag(FileInfo.SetCreationDate))
//           {
//               if ( logger.isDebugEnabled())
//               {
//                   logger.debug("Set creation date in file state cache" + name + ", " + info.getCreationDateTime());
//               }
//               Date createDate = new Date( info.getCreationDateTime());
//               fstate.u(createDate.getTime()); 
//           }
           if ( info.hasSetFlag(FileInfo.SetModifyDate)) 
           {   
               if ( logger.isDebugEnabled())
               {
                   logger.debug("Set modification date in file state cache" + name + ", " + info.getModifyDateTime());
               }
               Date modifyDate = new Date( info.getModifyDateTime());
               fstate.updateModifyDateTime(modifyDate.getTime()); 
           }
       }        
    }

    @Override
    public SearchContext startSearch(SrvSession sess, TreeConnection tree,
            String searchPath, int attrib) throws FileNotFoundException
    {
        InFlightCorrector t = new InFlightCorrectorImpl(tree);  
        
        SearchContext ctx = diskInterface.startSearch(sess, tree, searchPath, attrib);
        
        if(ctx instanceof InFlightCorrectable)
        {
            InFlightCorrectable thingable = (InFlightCorrectable)ctx;
            thingable.setInFlightCorrector(t);
        }
             
        return ctx;

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
    public DeviceContext createContext(String shareName, ConfigElement args)
            throws DeviceContextException
    {
        
        return diskInterface.createContext(shareName, args);
    }

     
    public void setFileLockingInterface(FileLockingInterface fileLockingInterface)
    {
        this.fileLockingInterface = fileLockingInterface;
    }

    public FileLockingInterface getFileLockingInterface()
    {
        return fileLockingInterface;
    }
    
    public void setOpLockInterface(OpLockInterface opLockInterface)
    {
        this.opLockInterface = opLockInterface;
    }

    public OpLockInterface getOpLockInterface()
    {
        return opLockInterface;
    }
}
  