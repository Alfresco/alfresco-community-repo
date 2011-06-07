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
package org.alfresco.repo.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
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
     * @deprecated The mimetype service is no longer needed.
     */
    @Deprecated
    public void setMimetypeService(MimetypeService mimetypeService)
    {
    }

    /**
     * {@inheritDoc}
     */
    public List<FileInfo> list(String ticket, final NodeRef contextNodeRef)
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> listFiles(String ticket, final NodeRef folderNodeRef)
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> listFolders(String ticket, final NodeRef contextNodeRef)
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * {@inheritDoc}
     */
    public NodeRef searchSimple(String ticket, final NodeRef contextNodeRef, final String name)
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
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
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
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
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo rename(String ticket, final NodeRef fileFolderRef, final String newName) throws FileExistsException, FileNotFoundException
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo move(String ticket, final NodeRef sourceNodeRef, final NodeRef targetParentRef, final String newName)
            throws FileExistsException, FileNotFoundException
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * {@inheritDoc}
     */
    public FileInfo copy(String ticket, final NodeRef sourceNodeRef, final NodeRef targetParentRef, final String newName)
            throws FileExistsException, FileNotFoundException
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * {@inheritDoc}
     */
    public FileInfo create(String ticket, final NodeRef parentNodeRef, final String name, final QName typeQName) throws FileExistsException
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo[] create(String ticket, final NodeRef[] parentNodeRefs, final String[] names, final QName[] typesQName) throws FileExistsException
    {
        AuthenticationUtil.pushAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<FileInfo[]> callback = new RetryingTransactionCallback<FileInfo[]>()
            {
                public FileInfo[] execute() throws Throwable
                {
                    FileInfo[] result = new FileInfo[parentNodeRefs.length];
                    for (int i = 0; i< result.length; i++)
                    {
                        result[i] = fileFolderService.create(parentNodeRefs[i], names[i], typesQName[i]); 
                    }
                    return result;
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void delete(String ticket, final NodeRef nodeRef)
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete(String ticket, final NodeRef[] nodeRefs)
    {
        AuthenticationUtil.pushAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    for (NodeRef nodeRef : nodeRefs)
                        fileFolderService.delete(nodeRef);
                    return null;
                }
            };
            retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * {@inheritDoc}
     */
    public FileInfo makeFolders(String ticket, final NodeRef parentNodeRef, final List<String> pathElements, final QName folderTypeQName)
    {
        AuthenticationUtil.pushAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<FileInfo> callback = new RetryingTransactionCallback<FileInfo>()
            {
                public FileInfo execute() throws Throwable
                {
                    return FileFolderServiceImpl.makeFolders(fileFolderService, parentNodeRef, pathElements, folderTypeQName);
                }
            };
            return retryingTransactionHelper.doInTransaction(callback, false, true);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List<FileInfo> getNamePath(String ticket, final NodeRef rootNodeRef, final NodeRef nodeRef) throws FileNotFoundException
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo resolveNamePath(String ticket, final NodeRef rootNodeRef, final List<String> pathElements) throws FileNotFoundException
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FileInfo getFileInfo(String ticket, final NodeRef nodeRef)
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
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
        AuthenticationUtil.pushAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<ContentData> callback = new RetryingTransactionCallback<ContentData>()
            {
                public ContentData execute() throws Throwable
                {
                    // Get a writer
                    ContentWriter writer = fileFolderService.getWriter(nodeRef);
                    
                    // We need the mimetype and encoding finding for us
                    writer.guessEncoding();
                    writer.guessMimetype(filename);
                    
                    // Make a stream
                    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                    
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
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public ContentData[] putContent(
            String ticket,
            final NodeRef[] nodeRefs,
            final byte[][] bytes,
            final String[] filenames)
    {
        AuthenticationUtil.pushAuthentication();
        try
        {
            authenticationService.validate(ticket);
            // Make the call
            RetryingTransactionCallback<ContentData[]> callback = new RetryingTransactionCallback<ContentData[]>()
            {
                public ContentData[] execute() throws Throwable
                {
                    // Guess the mimetype
                    ContentData[] results = new ContentData[filenames.length];

                    for (int i = 0; i < filenames.length; i++)
                    {
                        // Get a writer
                        ContentWriter writer = fileFolderService.getWriter(nodeRefs[i]);
                        
                        // We need the mimetype and encoding finding for us
                        writer.guessEncoding();
                        writer.guessMimetype(filenames[i]);
                        
                        // Make a stream
                        ByteArrayInputStream is = new ByteArrayInputStream(bytes[i]);

                        // Write the stream
                        writer.putContent(is);
                        results[i] = writer.getContentData();
                    }
                    // Done
                    return results;
                }
            };
            ContentData[] contentData = retryingTransactionHelper.doInTransaction(callback, false, true);
            // Done
            return contentData;
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }


    /**
     * {@inheritDoc}
     */
    public byte[] getContent(String ticket, final NodeRef nodeRef)
    {
        AuthenticationUtil.pushAuthentication();
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
            AuthenticationUtil.popAuthentication();
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
