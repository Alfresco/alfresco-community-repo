/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.io.IOException;

import org.alfresco.filesys.config.ServerConfigurationBean;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.smb.SharingMode;

/**
 * The Legacy file state driver is used to update JLAN's file state cache.
 * <p>
 * This class contains odds and ends to keep JLan happy.    In particular it
 * cannot contain any code that requires access to the alfresco repository.
 * 
 */
public class LegacyFileStateDriver implements ContentDiskCallback
{
          
    public void init()
    {
    }

    @Override
    public void getFileInformation(SrvSession sess, TreeConnection tree,
            String path, FileInfo info)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void fileExists(SrvSession sess, TreeConnection tree, String path,
            int fileExists)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void treeOpened(SrvSession sess, TreeConnection tree)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void treeClosed(SrvSession sess, TreeConnection tree)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void closeFile(SrvSession sess, TreeConnection tree,
            NetworkFile param)
    {
        ContentContext tctx = (ContentContext) tree.getContext();

        if(tctx.hasStateCache())
        {
            FileState fstate = tctx.getStateCache().findFileState( param.getFullName(), true);
            if ( fstate.decrementOpenCount() == 0)
            {
                fstate.setSharedAccess( SharingMode.READWRITE + SharingMode.DELETE);
            }
        }
        
    }

    @Override
    public void createDirectory(SrvSession sess, TreeConnection tree,
            FileOpenParams params)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createFile(SrvSession sess, TreeConnection tree,
            FileOpenParams params, NetworkFile newFile)
    {
        ContentContext tctx = (ContentContext) tree.getContext();
        
        // TODO temp code - not of interest to the repo
        if (newFile instanceof NodeRefNetworkFile)
        {
            NodeRefNetworkFile x = (NodeRefNetworkFile)newFile;
            x.setProcessId( params.getProcessId());
        }
        
        if(tctx.hasStateCache())
        {
            FileState fstate = tctx.getStateCache().findFileState( params.getPath(), true);
            fstate.incrementOpenCount();
            fstate.setProcessId(params.getProcessId());
            fstate.setSharedAccess( params.getSharedAccess());
            fstate.setProcessId( params.getProcessId());
        
            // Indicate that the file is open
            fstate.setFileStatus(FileStatus.FileExists);
            fstate.incrementOpenCount();
            //fstate.setFilesystemObject(result.getSecond());
            fstate.setAllocationSize( params.getAllocationSize());
        }        
    }

    @Override
    public void deleteDirectory(SrvSession sess, TreeConnection tree, String dir)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteFile(SrvSession sess, TreeConnection tree, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void flushFile(SrvSession sess, TreeConnection tree, NetworkFile file)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void isReadOnly(SrvSession sess, DeviceContext ctx,
            boolean isReadOnly)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void openFile(SrvSession sess, TreeConnection tree,
            FileOpenParams param, NetworkFile openFile)
    {
        ContentContext tctx = (ContentContext) tree.getContext();
        String path = param.getPath();
        
        // Stuff to keep JLAN working - of no interest to the repo.
        if (openFile instanceof ContentNetworkFile)
        {
            ContentNetworkFile x = (ContentNetworkFile)openFile;
            x.setProcessId( param.getProcessId());
            if(tctx.hasStateCache())
            {
                FileState fstate = tctx.getStateCache().findFileState( path, true);
                x.setFileState(fstate);
                fstate.incrementOpenCount();
                fstate.setProcessId(param.getProcessId());
            }
        }
        
        if (openFile instanceof TempNetworkFile)
        {
            TempNetworkFile x = (TempNetworkFile)openFile;
            //x.setProcessId( param.getProcessId());
            if(tctx.hasStateCache())
            {
                FileState fstate = tctx.getStateCache().findFileState( path, true);
                x.setFileState(fstate);
            }
        }
        
        if (openFile instanceof AlfrescoFolder)
        {
            AlfrescoFolder x = (AlfrescoFolder)openFile;
            //x.setProcessId( param.getProcessId());
            if(tctx.hasStateCache())
            {
                FileState fstate = tctx.getStateCache().findFileState( path, true);
                x.setFileState(fstate);
                fstate.setFileStatus(FileStatus.DirectoryExists);
            }
        }
    }

    @Override
    public void readFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, byte[] buf, int bufPos, int siz, long filePos,
            int readSize)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void renameFile(SrvSession sess, TreeConnection tree,
            String oldPath, String newPath)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void seekFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, long pos, int typ) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFileInformation(SrvSession sess, TreeConnection tree,
            String name, FileInfo info) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void startSearch(SrvSession sess, TreeConnection tree,
            String searchPath, int attrib, SearchContext context)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void truncateFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, long siz)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, byte[] buf, int bufoff, int siz, long fileoff,
            int writeSize)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerContext(DeviceContext ctx,
            ServerConfigurationBean serverConfig) throws DeviceContextException
    {
        // TODO Auto-generated method stub
        
    }
    
  

 
 
}
  