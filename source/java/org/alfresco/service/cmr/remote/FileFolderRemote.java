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
package org.alfresco.service.cmr.remote;

import java.util.List;

import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Remote transport interface for the <code>FileFolderService</code>.  This includes the
 * authentication tickets and abstracts the stream transport as well.
 * <p/>
 * <b>NOTE:</b> This is not a production API and will most definitely be changed or removed.
 * 
 * @see org.alfresco.service.cmr.model.FileFolderService
 * 
 * @author Derek Hulley
 * @since 2.2.
 */
public interface FileFolderRemote
{
    /** The service name <b>org.alfresco.FileFolderRemote</b> */
    public static final String SERVICE_NAME = "org.alfresco.FileFolderRemote";
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#list(NodeRef)
     */
    public List<FileInfo> list(String ticket, NodeRef contextNodeRef);
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#listFiles(NodeRef)
     */
    public List<FileInfo> listFiles(String ticket, NodeRef folderNodeRef);
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#listFolders(NodeRef)
     */
    public List<FileInfo> listFolders(String ticket, NodeRef contextNodeRef);

    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#searchSimple(NodeRef, String)
     */
    public NodeRef searchSimple(String ticket, NodeRef contextNodeRef, String name);

    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#search(NodeRef, String, boolean)
     */
    public List<FileInfo> search(
            String ticket,
            NodeRef contextNodeRef,
            String namePattern,
            boolean includeSubFolders);
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#search(NodeRef, String, boolean, boolean, boolean)
     */
    public List<FileInfo> search(
            String ticket,
            NodeRef contextNodeRef,
            String namePattern,
            boolean fileSearch,
            boolean folderSearch,
            boolean includeSubFolders);
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#rename(NodeRef, String)
     */
    public FileInfo rename(String ticket, NodeRef fileFolderRef, String newName) throws FileExistsException, FileNotFoundException;
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#move(NodeRef, NodeRef, String)
     */
    public FileInfo move(String ticket, NodeRef sourceNodeRef, NodeRef targetParentRef, String newName)
            throws FileExistsException, FileNotFoundException;

    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#copy(NodeRef, NodeRef, String)
     */
    public FileInfo copy(String ticket, NodeRef sourceNodeRef, NodeRef targetParentRef, String newName)
            throws FileExistsException, FileNotFoundException;

    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#create(NodeRef, String, QName)
     */
    public FileInfo create(String ticket, NodeRef parentNodeRef, String name, QName typeQName) throws FileExistsException;
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#delete(NodeRef)
     */
    public void delete(String ticket, NodeRef nodeRef);
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#makeFolders(NodeRef, List, QName)
     */
    public FileInfo makeFolders(String ticket, NodeRef parentNodeRef, List<String> pathElements, QName folderTypeQName);
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#getNamePath(NodeRef, NodeRef)
     */
    public List<FileInfo> getNamePath(String ticket, NodeRef rootNodeRef, NodeRef nodeRef) throws FileNotFoundException;
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#resolveNamePath(NodeRef, List)
     */
    public FileInfo resolveNamePath(String ticket, NodeRef rootNodeRef, List<String> pathElements) throws FileNotFoundException;
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#getFileInfo(NodeRef)
     */
    public FileInfo getFileInfo(String ticket, NodeRef nodeRef);
    
    /**
     * TODO: Refactor!!!
     * The dirtiest of hacks.  When time permits, the APIs and implementations will be properly refactored.
     * For now, this remains adequate for small files.
     */
    public ContentData putContent(String ticket, NodeRef nodeRef, byte[] bytes, String filename);
    
    /**
     * TODO: Refactor!!!
     */
    public byte[] getContent(String ticket, NodeRef nodeRef);
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#getReader(NodeRef)
     */
    public ContentReader getReader(String ticket, NodeRef nodeRef);
    
    /**
     * @param token     the authentication ticket
     * 
     * @see FileFolderService#getWriter(NodeRef)
     */
    public ContentWriter getWriter(String ticket, NodeRef nodeRef);
}
