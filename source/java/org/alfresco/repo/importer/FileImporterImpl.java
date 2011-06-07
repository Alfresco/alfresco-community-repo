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
package org.alfresco.repo.importer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple import of content into the repository
 * 
 * @author andyh
 */
public class FileImporterImpl implements FileImporter
{
    private static Log logger = LogFactory.getLog(FileImporterImpl.class);

    private AuthenticationService authenticationService;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private TransactionService transactionService;
    private boolean txnPerFile = false;

    public FileImporterImpl()
    {
        super();
    }

    public int loadFile(NodeRef container, File file, boolean recurse) throws FileImporterException
    {
        try
        {
            Counter counter = new Counter();
            create(counter, container, file, null, recurse, null);
            return counter.getCount();
        }
        catch (Throwable e)
        {
            throw new FileImporterException("Failed to load file: \n" +
                    "   container: " + container + "\n" +
                    "   file: " + file + "\n" +
                    "   recurse: " + recurse,
                    e);
        }
    }
    
    public int loadNamedFile(NodeRef container, File file, boolean recurse, String name) throws FileImporterException
    {
        try
        {
            Counter counter = new Counter();
            create(counter, container, file, null, recurse, name);
            return counter.getCount();
        }
        catch (Throwable e)
        {
            throw new FileImporterException("Failed to load file: \n" +
                    "   container: " + container + "\n" +
                    "   file: " + file + "\n" +
                    "   name: " + name + "\n" +
                    "   recurse: " + recurse,
                    e);
        }
    }

    public int loadFile(NodeRef container, File file, FileFilter filter, boolean recurse) throws FileImporterException
    {
        try
        {
            Counter counter = new Counter();
            create(counter, container, file, filter, recurse, null);
            return counter.getCount();
        }
        catch (Throwable e)
        {
            throw new FileImporterException("Failed to load file: \n" +
                    "   container: " + container + "\n" +
                    "   file: " + file + "\n" +
                    "   filter: " + filter + "\n" +
                    "   recurse: " + recurse,
                    e);
        }
    }

    public int loadFile(NodeRef container, File file) throws FileImporterException
    {
        try
        {
            Counter counter = new Counter();
            create(counter, container, file, null, false, null);
            return counter.getCount();
        }
        catch (Throwable e)
        {
            throw new FileImporterException("Failed to load file: \n" +
                    "   container: " + container + "\n" +
                    "   file: " + file,
                    e);
        }
    }
    
    /** Helper class for mutable int */
    private static class Counter
    {
        private int count = 0;
        public void increment()
        {
            count++;
        }
        public int getCount()
        {
            return count;
        }
    }

    private NodeRef create(
            Counter counter,
            final NodeRef container,
            final File file,
            FileFilter filter,
            boolean recurse,
            final String containerName) throws Throwable
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        if (containerName != null)
        {
            RetryingTransactionCallback<NodeRef> createDirectoryWork = new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Exception
                {
                    return createDirectory(container, containerName, containerName);
                }
            };
            NodeRef newContainer = txnHelper.doInTransaction(createDirectoryWork);
            return create(counter, newContainer, file, filter, recurse, null);
          
        }
        if (file.isDirectory())
        {
            counter.increment();
            RetryingTransactionCallback<NodeRef> createDirectoryWork = new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Exception
                {
                    return createDirectory(container, file);
                }
            };
            NodeRef directoryNodeRef = null;
            if (txnPerFile)
            {
                directoryNodeRef = txnHelper.doInTransaction(createDirectoryWork);
            }
            else
            {
                directoryNodeRef = createDirectoryWork.execute();
            }
            
            if (recurse)
            {
                File[] files = ((filter == null) ? file.listFiles() : file.listFiles(filter));
                for(int i = 0; i < files.length; i++)
                {
                    create(counter, directoryNodeRef, files[i], filter, recurse, null);
                }
            }
            
            return directoryNodeRef;
        }
        else
        {
            counter.increment();
            RetryingTransactionCallback<NodeRef> createFileWork = new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Exception
                {
                    return createFile(container, file);
                }
            };
            NodeRef fileNodeRef = null;
            if (txnPerFile)
            {
                fileNodeRef = txnHelper.doInTransaction(createFileWork);
            }
            else
            {
                fileNodeRef = createFileWork.execute();
            }
            return fileNodeRef;
        }
    }

    /**
     * Get the type of child association that should be created.
     * 
     * @param parentNodeRef the parent
     * @return Returns the appropriate child association type qualified name for the type of the
     *      parent.  Null will be returned if it can't be determined.
     */
    private QName getAssocTypeQName(NodeRef parentNodeRef)
    {
        // check the parent node's type to determine which association to use
        QName parentNodeTypeQName = nodeService.getType(parentNodeRef);
        QName assocTypeQName = null;
        if (dictionaryService.isSubClass(parentNodeTypeQName, ContentModel.TYPE_CONTAINER))
        {
            // it may be a root node or something similar
            assocTypeQName = ContentModel.ASSOC_CHILDREN;
        }
        else if (dictionaryService.isSubClass(parentNodeTypeQName, ContentModel.TYPE_FOLDER))
        {
            // more like a directory
            assocTypeQName = ContentModel.ASSOC_CONTAINS;
        }
        return assocTypeQName;
    }

    private NodeRef createFile(NodeRef parentNodeRef, File file)
    {
        // check the parent node's type to determine which association to use
        QName assocTypeQName = getAssocTypeQName(parentNodeRef);
        if (assocTypeQName == null)
        {
            throw new IllegalArgumentException(
                    "Unable to create file.  " +
                    "Parent type is inappropriate: " + nodeService.getType(parentNodeRef));
        }
        
        // Identify the type of the file
        FileContentReader reader = new FileContentReader(file);
        String mimetype = mimetypeService.guessMimetype(file.getName(), reader);

        // create properties for content type
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(3, 1.0f);
        contentProps.put(ContentModel.PROP_NAME, file.getName());
        contentProps.put(
                ContentModel.PROP_CONTENT,
                new ContentData(null, mimetype, 0L, "UTF-8"));
        String currentUser = authenticationService.getCurrentUserName();
        contentProps.put(ContentModel.PROP_CREATOR, currentUser == null ? "unknown" : currentUser);

        // create the node to represent the node
        String assocName = QName.createValidLocalName(file.getName());
        ChildAssociationRef assocRef = this.nodeService.createNode(
                parentNodeRef,
                assocTypeQName,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, assocName),
                ContentModel.TYPE_CONTENT, contentProps);

        NodeRef fileNodeRef = assocRef.getChildRef();
        

        if (logger.isDebugEnabled())
            logger.debug("Created file node for file: " + file.getName());

        // apply the titled aspect - title and description
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(5);
        titledProps.put(ContentModel.PROP_TITLE, file.getName());

        titledProps.put(ContentModel.PROP_DESCRIPTION, file.getPath());

        this.nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_TITLED, titledProps);

        if (logger.isDebugEnabled())
            logger.debug("Added titled aspect with properties: " + titledProps);

        // get a writer for the content and put the file
        ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
        try
        {
            writer.putContent(new BufferedInputStream(new FileInputStream(file)));
        }
        catch (ContentIOException e)
        {
            throw new FileImporterException("Failed to load content from "+file.getPath(), e);
        }
        catch (FileNotFoundException e)
        {
            throw new FileImporterException("Failed to load content (file not found) "+file.getPath(), e);
        }
        
        return fileNodeRef;
    }

    private NodeRef createDirectory(NodeRef parentNodeRef, File file)
    {
        return createDirectory(parentNodeRef, file.getName(), file.getPath());
        
    }
    
    private NodeRef createDirectory(NodeRef parentNodeRef, String name, String path)
    {
        // check the parent node's type to determine which association to use
        QName assocTypeQName = getAssocTypeQName(parentNodeRef);
        if (assocTypeQName == null)
        {
            throw new IllegalArgumentException(
                    "Unable to create directory.  " +
                    "Parent type is inappropriate: " + nodeService.getType(parentNodeRef));
        }
        
        String qname = QName.createValidLocalName(name);
        ChildAssociationRef assocRef = this.nodeService.createNode(
              parentNodeRef,
              assocTypeQName,
              QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname),
              ContentModel.TYPE_FOLDER);
        
        NodeRef nodeRef = assocRef.getChildRef();
        
        // set the name property on the node
        this.nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
        
        if (logger.isDebugEnabled())
           logger.debug("Created folder node with name: " + name);

        // apply the uifacets aspect - icon, title and description props
        Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, "space-icon-default");
        uiFacetsProps.put(ContentModel.PROP_TITLE, name);
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, path);
        this.nodeService.addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
        
        if (logger.isDebugEnabled())
           logger.debug("Added uifacets aspect with properties: " + uiFacetsProps);
        
        return nodeRef;
    }

    protected void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    protected void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    protected void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }
    
    protected void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param txnPerFile true to force each file or directory creation to be in its
     *      own file
     */
    public void setTxnPerFile(boolean txnPerFile)
    {
        this.txnPerFile = txnPerFile;
    }
}
