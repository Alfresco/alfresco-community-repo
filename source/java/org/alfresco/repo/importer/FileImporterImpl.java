/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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

import org.alfresco.model.ContentModel;
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

    public FileImporterImpl()
    {
        super();
    }

    public int loadFile(NodeRef container, File file, boolean recurse) throws FileImporterException
    {
        Counter counter = new Counter();
        create(counter, container, file, null, recurse, null);
        return counter.getCount();
    }
    
    public int loadNamedFile(NodeRef container, File file, boolean recurse, String name) throws FileImporterException
    {
        Counter counter = new Counter();
        create(counter, container, file, null, recurse, name);
        return counter.getCount();
    }

    public int loadFile(NodeRef container, File file, FileFilter filter, boolean recurse) throws FileImporterException
    {
        Counter counter = new Counter();
        create(counter, container, file, filter, recurse, null);
        return counter.getCount();
    }

    public int loadFile(NodeRef container, File file) throws FileImporterException
    {
        Counter counter = new Counter();
        create(counter, container, file, null, false, null);
        return counter.getCount();
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

    private NodeRef create(Counter counter, NodeRef container, File file, FileFilter filter, boolean recurse, String containerName)
    {
        if(containerName != null)
        {
            NodeRef newContainer = createDirectory(container, containerName, containerName);
            return create(counter, newContainer, file, filter, recurse, null);
          
        }
        if (file.isDirectory())
        {
            NodeRef directoryNodeRef = createDirectory(container, file);
            counter.increment();
            
            if(recurse)
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
            return createFile(container, file);
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

        // create properties for content type
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(3, 1.0f);
        contentProps.put(ContentModel.PROP_NAME, file.getName());
        contentProps.put(
                ContentModel.PROP_CONTENT,
                new ContentData(null, mimetypeService.guessMimetype(file.getName()), 0L, "UTF-8"));
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
        uiFacetsProps.put(ContentModel.PROP_ICON, "space-icon-default");
        uiFacetsProps.put(ContentModel.PROP_TITLE, name);
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, path);
        this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_UIFACETS, uiFacetsProps);
        
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
}
