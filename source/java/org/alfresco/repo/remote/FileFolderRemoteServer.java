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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.remote.FileFolderRemote;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * Server side implementation of the <code>FileFolderService</code> transport
 * layer.  This is the class that gets exported remotely as it contains the
 * explicit ticket arguments.
 * 
 * @author Derek Hulley
 * @since 2.2
 */
public class FileFolderRemoteServer implements FileFolderRemote
{
    private RetryingTransactionHelper retryingTransactionHelper;
    private AuthenticationService authenticationService;
    private FileFolderService fileFolderService;
    private MimetypeService mimetypeService;

    /**
     * @param transactionService            provides transactional support and retrying
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
    }

    /**
     * @param authenticationService         the service that will validate the tickets
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * @param filefolderService             the service that will do the work
     */
    public void setFileFolderService(FileFolderService filefolderService)
    {
        this.fileFolderService = filefolderService;
    }

    /**
     * @param mimetypeService               used to determine the character encoding
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * {@inheritDoc}
     */
    public List<FileInfo> list(String ticket, final NodeRef contextNodeRef)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<List<FileInfo>> callback = new RetryingTransactionCallback<List<FileInfo>>()
            {
                public List<FileInfo> execute() throws Throwable
                {
                    return fileFolderService.list(contextNodeRef);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, true, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> listFiles(String ticket, final NodeRef folderNodeRef)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<List<FileInfo>> callback = new RetryingTransactionCallback<List<FileInfo>>()
            {
                public List<FileInfo> execute() throws Throwable
                {
                    return fileFolderService.listFiles(folderNodeRef);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, true, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> listFolders(String ticket, final NodeRef contextNodeRef)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<List<FileInfo>> callback = new RetryingTransactionCallback<List<FileInfo>>()
            {
                public List<FileInfo> execute() throws Throwable
                {
                    return fileFolderService.listFolders(contextNodeRef);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, true, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }

    /**
     * {@inheritDoc}
     */
    public NodeRef searchSimple(String ticket, final NodeRef contextNodeRef, final String name)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    return fileFolderService.searchSimple(contextNodeRef, name);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, true, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
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
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<List<FileInfo>> callback = new RetryingTransactionCallback<List<FileInfo>>()
            {
                public List<FileInfo> execute() throws Throwable
                {
                    return fileFolderService.search(contextNodeRef, namePattern, includeSubFolders);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, true, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
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
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<List<FileInfo>> callback = new RetryingTransactionCallback<List<FileInfo>>()
            {
                public List<FileInfo> execute() throws Throwable
                {
                    return fileFolderService.search(contextNodeRef, namePattern, fileSearch, folderSearch, includeSubFolders);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, true, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo rename(String ticket, final NodeRef fileFolderRef, final String newName) throws FileExistsException, FileNotFoundException
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<FileInfo> callback = new RetryingTransactionCallback<FileInfo>()
            {
                public FileInfo execute() throws Throwable
                {
                    return fileFolderService.rename(fileFolderRef, newName);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo move(String ticket, final NodeRef sourceNodeRef, final NodeRef targetParentRef, final String newName)
            throws FileExistsException, FileNotFoundException
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<FileInfo> callback = new RetryingTransactionCallback<FileInfo>()
            {
                public FileInfo execute() throws Throwable
                {
                    return fileFolderService.move(sourceNodeRef, targetParentRef, newName);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }

    /**
     * {@inheritDoc}
     */
    public FileInfo copy(String ticket, final NodeRef sourceNodeRef, final NodeRef targetParentRef, final String newName)
            throws FileExistsException, FileNotFoundException
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<FileInfo> callback = new RetryingTransactionCallback<FileInfo>()
            {
                public FileInfo execute() throws Throwable
                {
                    return fileFolderService.copy(sourceNodeRef, targetParentRef, newName);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }

    /**
     * {@inheritDoc}
     */
    public FileInfo create(String ticket, final NodeRef parentNodeRef, final String name, final QName typeQName) throws FileExistsException
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<FileInfo> callback = new RetryingTransactionCallback<FileInfo>()
            {
                public FileInfo execute() throws Throwable
                {
                    return fileFolderService.create(parentNodeRef, name, typeQName);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete(String ticket, final NodeRef nodeRef)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    fileFolderService.delete(nodeRef);
                    return null;
                }
            };
            retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo makeFolders(String ticket, final NodeRef parentNodeRef, final List<String> pathElements, final QName folderTypeQName)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<FileInfo> callback = new RetryingTransactionCallback<FileInfo>()
            {
                public FileInfo execute() throws Throwable
                {
                    return fileFolderService.makeFolders(parentNodeRef, pathElements, folderTypeQName);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> getNamePath(String ticket, final NodeRef rootNodeRef, final NodeRef nodeRef) throws FileNotFoundException
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<List<FileInfo>> callback = new RetryingTransactionCallback<List<FileInfo>>()
            {
                public List<FileInfo> execute() throws Throwable
                {
                    return fileFolderService.getNamePath(rootNodeRef, nodeRef);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, true, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo resolveNamePath(String ticket, final NodeRef rootNodeRef, final List<String> pathElements) throws FileNotFoundException
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<FileInfo> callback = new RetryingTransactionCallback<FileInfo>()
            {
                public FileInfo execute() throws Throwable
                {
                    return fileFolderService.resolveNamePath(rootNodeRef, pathElements);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, true, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo getFileInfo(String ticket, final NodeRef nodeRef)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<FileInfo> callback = new RetryingTransactionCallback<FileInfo>()
            {
                public FileInfo execute() throws Throwable
                {
                    return fileFolderService.getFileInfo(nodeRef);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, true, true);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public ContentData putContent(
            String ticket,
            final NodeRef nodeRef,
            final byte[] bytes,
            final String filename)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<ContentData> callback = new RetryingTransactionCallback<ContentData>()
            {
                public ContentData execute() throws Throwable
                {
                    // Guess the mimetype
                    String mimetype = mimetypeService.guessMimetype(filename);
                    
                    // Get a writer
                    ContentWriter writer = fileFolderService.getWriter(nodeRef);
                    // Make a stream
                    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                    // Guess the encoding
                    ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
                    Charset charset = charsetFinder.getCharset(is, mimetype);
                    // Set metadata
                    writer.setEncoding(charset.name());
                    writer.setMimetype(mimetype);
                    
                    // Write the stream
                    writer.putContent(is);
                    
                    // Done
                    return writer.getContentData();
                }
            };
            ContentData contentData = retryingTransactionHelper.doInTransaction(callback, false, true);
            // Done
            return contentData;
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public byte[] getContent(String ticket, final NodeRef nodeRef)
    {
        Authentication authentication = AuthenticationUtil.getCurrentAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<byte[]> callback = new RetryingTransactionCallback<byte[]>()
            {
                public byte[] execute() throws Throwable
                {
                    // Get a reader
                    ContentReader reader = fileFolderService.getReader(nodeRef);
                    if (!reader.exists())
                    {
                        return null;
                    }
                    
                    // Extract the content
                    ByteArrayOutputStream bos = new ByteArrayOutputStream((int)reader.getSize());
                    reader.getContent(bos);
                    
                    // Done
                    return bos.toByteArray();
                }
            };
            byte[] bytes = retryingTransactionHelper.doInTransaction(callback, true, true);
            // Done
            return bytes;
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
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
