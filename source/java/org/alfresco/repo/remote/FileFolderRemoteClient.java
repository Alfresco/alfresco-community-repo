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
package org.alfresco.repo.remote;

import java.util.List;

import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.remote.FileFolderRemote;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * Client side implementation of the remotable FileFolder interface.
 * 
 * @author Derek Hulley
 * @since 2.2
 */
public class FileFolderRemoteClient implements FileFolderRemote
{
    private String rmiUrl;
    private FileFolderRemote remotePeer;

    public FileFolderRemoteClient(String rmiUrl)
    {
        // Ensure the RMI URL is consistent
        if (!rmiUrl.endsWith("/"))
        {
            rmiUrl += "/";
        }

        this.rmiUrl = rmiUrl;
        // Connect
        connect();
    }
    
    private void connect()
    {
        // Get the FileFolderServiceTransport
        RmiProxyFactoryBean fileFolderFactory = new RmiProxyFactoryBean();
        fileFolderFactory.setRefreshStubOnConnectFailure(true);
        fileFolderFactory.setServiceInterface(FileFolderRemote.class);
        fileFolderFactory.setServiceUrl(rmiUrl + FileFolderRemote.SERVICE_NAME);
        fileFolderFactory.afterPropertiesSet();
        FileFolderRemote fileFolderRemote = (FileFolderRemote) fileFolderFactory.getObject();
        this.remotePeer = fileFolderRemote;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> list(String ticket, final NodeRef contextNodeRef)
    {
        return remotePeer.list(ticket, contextNodeRef);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> listFiles(String ticket, final NodeRef folderNodeRef)
    {
        return remotePeer.listFiles(ticket, folderNodeRef);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> listFolders(String ticket, final NodeRef contextNodeRef)
    {
        return remotePeer.listFolders(ticket, contextNodeRef);
    }

    /**
     * {@inheritDoc}
     */
    public NodeRef searchSimple(String ticket, final NodeRef contextNodeRef, final String name)
    {
        return remotePeer.searchSimple(ticket, contextNodeRef, name);
    }

    /**
     * {@inheritDoc}
     */
    public List<FileInfo> search(
            String ticket,
            final NodeRef contextNodeRef,
            final String namePattern,
            final boolean includeSubFolders)
    {
        return remotePeer.search(ticket, contextNodeRef, namePattern, includeSubFolders);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> search(
            String ticket,
            final NodeRef contextNodeRef,
            final String namePattern,
            final boolean fileSearch,
            final boolean folderSearch,
            final boolean includeSubFolders)
    {
        return remotePeer.search(ticket, contextNodeRef, namePattern, fileSearch, folderSearch, includeSubFolders);
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo rename(String ticket, final NodeRef fileFolderRef, final String newName) throws FileExistsException, FileNotFoundException
    {
        return remotePeer.rename(ticket, fileFolderRef, newName);
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo move(String ticket, final NodeRef sourceNodeRef, final NodeRef targetParentRef, final String newName)
            throws FileExistsException, FileNotFoundException
    {
        return remotePeer.move(ticket, sourceNodeRef, targetParentRef, newName);
    }

    /**
     * {@inheritDoc}
     */
    public FileInfo copy(String ticket, final NodeRef sourceNodeRef, final NodeRef targetParentRef, final String newName)
            throws FileExistsException, FileNotFoundException
    {
        return remotePeer.copy(ticket, sourceNodeRef, targetParentRef, newName);
    }

    /**
     * {@inheritDoc}
     */
    public FileInfo create(String ticket, final NodeRef parentNodeRef, final String name, final QName typeQName) throws FileExistsException
    {
        return remotePeer.create(ticket, parentNodeRef, name, typeQName);
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete(String ticket, final NodeRef nodeRef)
    {
        remotePeer.delete(ticket, nodeRef);
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo makeFolders(String ticket, final NodeRef parentNodeRef, final List<String> pathElements, final QName folderTypeQName)
    {
        return remotePeer.makeFolders(ticket, parentNodeRef, pathElements, folderTypeQName);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> getNamePath(String ticket, final NodeRef rootNodeRef, final NodeRef nodeRef) throws FileNotFoundException
    {
        return remotePeer.getNamePath(ticket, rootNodeRef, nodeRef);
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo resolveNamePath(String ticket, final NodeRef rootNodeRef, final List<String> pathElements) throws FileNotFoundException
    {
        return remotePeer.resolveNamePath(ticket, rootNodeRef, pathElements);
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo getFileInfo(String ticket, final NodeRef nodeRef)
    {
        return remotePeer.getFileInfo(ticket, nodeRef);
    }
    
    /**
     * {@inheritDoc}
     */
    public ContentData putContent(String ticket, NodeRef nodeRef, byte[] bytes, String filename)
    {
        return remotePeer.putContent(ticket, nodeRef, bytes, filename);
    }
    
    /**
     * {@inheritDoc}
     */
    public byte[] getContent(String ticket, NodeRef nodeRef)
    {
        return remotePeer.getContent(ticket, nodeRef);
    }
    
    /**
     * {@inheritDoc}
     */
    public ContentReader getReader(String ticket, NodeRef nodeRef)
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    public ContentWriter getWriter(String ticket, NodeRef nodeRef)
    {
        throw new UnsupportedOperationException();
    }
}
