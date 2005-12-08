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
package org.alfresco.repo.model.filefolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.SearchLanguageConversion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the file/folder-specific service.
 * 
 * @author Derek Hulley
 */
public class FileFolderServiceImpl implements FileFolderService
{
    /** Shallow search for all files */
    private static final String XPATH_QUERY_SHALLOW_FILES =
        "./*" +
        "[(subtypeOf('" + ContentModel.TYPE_CONTENT + "'))]";
    
    /** Shallow search for all folder */
    private static final String XPATH_QUERY_SHALLOW_FOLDERS =
        "./*" +
        "[not (subtypeOf('" + ContentModel.TYPE_SYSTEM_FOLDER + "'))" +
        " and (subtypeOf('" + ContentModel.TYPE_FOLDER + "'))]";
    
    /** Shallow search for all files and folders */
    private static final String XPATH_QUERY_SHALLOW_ALL =
        "./*" +
        "[like(@cm:name, $cm:name, false)" +
        " and not (subtypeOf('" + ContentModel.TYPE_SYSTEM_FOLDER + "'))" +
        " and (subtypeOf('" + ContentModel.TYPE_FOLDER + "') or subtypeOf('" + ContentModel.TYPE_CONTENT + "'))]";
    
    /** Deep search for files and folders with a name pattern */
    private static final String XPATH_QUERY_DEEP_ALL =
        ".//*" +
        "[like(@cm:name, $cm:name, false)" +
        " and not (subtypeOf('" + ContentModel.TYPE_SYSTEM_FOLDER + "'))" +
        " and (subtypeOf('" + ContentModel.TYPE_FOLDER + "') or subtypeOf('" + ContentModel.TYPE_CONTENT + "'))]";
    
    /** empty parameters */
    private static final QueryParameterDefinition[] PARAMS_EMPTY = new QueryParameterDefinition[0];
    private static final QueryParameterDefinition[] PARAMS_ANY_NAME = new QueryParameterDefinition[1];
    
    private static Log logger = LogFactory.getLog(FileFolderServiceImpl.class);

    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private CopyService copyService;
    private SearchService searchService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    
    /**
     * Default constructor
     */
    public FileFolderServiceImpl()
    {
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setCopyService(CopyService copyService)
    {
        this.copyService = copyService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void init()
    {
        PARAMS_ANY_NAME[0] = new QueryParameterDefImpl(
                ContentModel.PROP_NAME,
                dictionaryService.getDataType(DataTypeDefinition.TEXT),
                true,
                "%");
    }

    /**
     * Helper method to convert node reference instances to file info
     * 
     * @param nodeRefs the node references
     * @return Return a list of file info
     * @throws InvalidTypeException if the node is not a valid type
     */
    private List<FileInfo> toFileInfo(List<NodeRef> nodeRefs) throws InvalidTypeException
    {
        List<FileInfo> results = new ArrayList<FileInfo>(nodeRefs.size());
        for (NodeRef nodeRef : nodeRefs)
        {
            FileInfo fileInfo = toFileInfo(nodeRef);
            results.add(fileInfo);
        }
        return results;
    }
    
    /**
     * Helper method to convert a node reference instance to a file info
     */
    private FileInfo toFileInfo(NodeRef nodeRef) throws InvalidTypeException
    {
        // get the file attributes
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        // is it a folder
        QName typeQName = nodeService.getType(nodeRef);
        boolean isFolder = isFolder(typeQName);
        
        // construct the file info and add to the results
        FileInfo fileInfo = new FileInfoImpl(nodeRef, isFolder, properties);
        // done
        return fileInfo;
    }

    /**
     * Ensure that a file or folder with the given name does not already exist
     * 
     * @throws FileExistsException if a same-named file or folder already exists
     */
    private void checkExists(NodeRef parentFolderRef, String name)
            throws FileExistsException
    {
        // check for existing file or folder
        List<FileInfo> existingFileInfos = this.search(parentFolderRef, name, true, true, false);
        if (existingFileInfos.size() > 0)
        {
            throw new FileExistsException(existingFileInfos.get(0));
        }
    }

    /**
     * Exception when the type is not a valid File or Folder type
     * 
     * @see ContentModel#TYPE_CONTENT
     * @see ContentModel#TYPE_FOLDER
     * 
     * @author Derek Hulley
     */
    private static class InvalidTypeException extends RuntimeException
    {
        private static final long serialVersionUID = -310101369475434280L;
        
        public InvalidTypeException(String msg)
        {
            super(msg);
        }
    }
    
    /**
     * Checks the type for whether it is a file or folder.  All invalid types
     * lead to runtime exceptions.
     * 
     * @param typeQName the type to check
     * @return Returns true if the type is a valid folder type, false if it is a file.
     * @throws AlfrescoRuntimeException if the type is not handled by this service
     */
    private boolean isFolder(QName typeQName) throws InvalidTypeException
    {
        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_FOLDER))
        {
            if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_SYSTEM_FOLDER))
            {
                throw new InvalidTypeException("This service should ignore type " + ContentModel.TYPE_SYSTEM_FOLDER);
            }
            return true;
        }
        else if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            // it is a regular file
            return false;
        }
        else
        {
            // unhandled type
            throw new InvalidTypeException("Type is not handled by this service: " + typeQName);
        }
    }

    /**
     * TODO: Use Lucene search to get file attributes without having to visit the node service
     */
    public List<FileInfo> list(NodeRef contextNodeRef)
    {
        // execute the query
        List<NodeRef> nodeRefs = searchService.selectNodes(
                contextNodeRef,
                XPATH_QUERY_SHALLOW_ALL,
                PARAMS_ANY_NAME,
                namespaceService,
                false);
        // convert the noderefs
        List<FileInfo> results = toFileInfo(nodeRefs);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Shallow search for files and folders: \n" +
                    "   context: " + contextNodeRef + "\n" +
                    "   results: " + results);
        }
        return results;
    }

    /**
     * TODO: Use Lucene search to get file attributes without having to visit the node service
     */
    public List<FileInfo> listFiles(NodeRef contextNodeRef)
    {
        // execute the query
        List<NodeRef> nodeRefs = searchService.selectNodes(
                contextNodeRef,
                XPATH_QUERY_SHALLOW_FILES,
                PARAMS_EMPTY,
                namespaceService,
                false);
        // convert the noderefs
        List<FileInfo> results = toFileInfo(nodeRefs);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Shallow search for files: \n" +
                    "   context: " + contextNodeRef + "\n" +
                    "   results: " + results);
        }
        return results;
    }

    /**
     * TODO: Use Lucene search to get file attributes without having to visit the node service
     */
    public List<FileInfo> listFolders(NodeRef contextNodeRef)
    {
        // execute the query
        List<NodeRef> nodeRefs = searchService.selectNodes(
                contextNodeRef,
                XPATH_QUERY_SHALLOW_FOLDERS,
                PARAMS_EMPTY,
                namespaceService,
                false);
        // convert the noderefs
        List<FileInfo> results = toFileInfo(nodeRefs);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Shallow search for folders: \n" +
                    "   context: " + contextNodeRef + "\n" +
                    "   results: " + results);
        }
        return results;
    }

    /**
     * @see #search(NodeRef, String, boolean, boolean, boolean)
     */
    public List<FileInfo> search(NodeRef contextNodeRef, String namePattern, boolean includeSubFolders)
    {
        return search(contextNodeRef, namePattern, true, true, includeSubFolders);
    }

    /**
     * Full search with all options
     */
    public List<FileInfo> search(
            NodeRef contextNodeRef,
            String namePattern,
            boolean fileSearch,
            boolean folderSearch,
            boolean includeSubFolders)
    {
        // shortcut if the search is requesting nothing
        if (!fileSearch && !folderSearch)
        {
            return Collections.emptyList();
        }
        
        // if the name pattern is null, then we use the ANY pattern
        QueryParameterDefinition[] params = null;
        if (namePattern != null)
        {
            // the interface specifies the Lucene syntax, so perform a conversion
            namePattern = SearchLanguageConversion.convert(
                    SearchLanguageConversion.DEF_LUCENE,
                    SearchLanguageConversion.DEF_XPATH_LIKE,
                    namePattern);
            
            params = new QueryParameterDefinition[1];
            params[0] = new QueryParameterDefImpl(
                    ContentModel.PROP_NAME,
                    dictionaryService.getDataType(DataTypeDefinition.TEXT),
                    true,
                    namePattern);
        }
        else
        {
            params = PARAMS_ANY_NAME;
        }
        // determine the correct query to use
        String query = null;
        if (includeSubFolders)
        {
            query = XPATH_QUERY_DEEP_ALL;
        }
        else
        {
            query = XPATH_QUERY_SHALLOW_ALL;
        }
        // execute the query
        List<NodeRef> nodeRefs = searchService.selectNodes(
                contextNodeRef,
                query,
                params,
                namespaceService,
                false);
        List<FileInfo> results = toFileInfo(nodeRefs);
        // eliminate unwanted files/folders
        Iterator<FileInfo> iterator = results.iterator(); 
        while (iterator.hasNext())
        {
            FileInfo file = iterator.next();
            if (file.isFolder() && !folderSearch)
            {
                iterator.remove();
            }
            else if (!file.isFolder() && !fileSearch)
            {
                iterator.remove();
            }
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deep search: \n" +
                    "   context: " + contextNodeRef + "\n" +
                    "   pattern: " + namePattern + "\n" +
                    "   files: " + fileSearch + "\n" +
                    "   folders: " + folderSearch + "\n" +
                    "   deep: " + includeSubFolders + "\n" +
                    "   results: " + results);
        }
        return results;
    }

    /**
     * @see #move(NodeRef, NodeRef, String)
     */
    public FileInfo rename(NodeRef sourceNodeRef, String newName) throws FileExistsException, FileNotFoundException
    {
        return move(sourceNodeRef, null, newName);
    }

    /**
     * @see #moveOrCopy(NodeRef, NodeRef, String, boolean)
     */
    public FileInfo move(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName) throws FileExistsException, FileNotFoundException
    {
        return moveOrCopy(sourceNodeRef, targetParentRef, newName, true);
    }
    
    /**
     * @see #moveOrCopy(NodeRef, NodeRef, String, boolean)
     */
    public FileInfo copy(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName) throws FileExistsException, FileNotFoundException
    {
        return moveOrCopy(sourceNodeRef, targetParentRef, newName, false);
    }

    /**
     * Implements both move and copy behaviour
     * 
     * @param move true to move, otherwise false to copy
     */
    private FileInfo moveOrCopy(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName, boolean move) throws FileExistsException, FileNotFoundException
    {
        // get file/folder in its current state
        FileInfo beforeFileInfo = toFileInfo(sourceNodeRef);
        // check the name - null means keep the existing name
        if (newName == null)
        {
            newName = beforeFileInfo.getName();
        }
        
        // we need the current association type
        ChildAssociationRef assocRef = nodeService.getPrimaryParent(sourceNodeRef);
        if (targetParentRef == null)
        {
            targetParentRef = assocRef.getParentRef();
        }
        
        // there is nothing to do if both the name and parent folder haven't changed
        if (targetParentRef.equals(assocRef.getParentRef()) && newName.equals(beforeFileInfo.getName()))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Doing nothing - neither filename or parent has not changed: \n" +
                        "   parent: " + targetParentRef + "\n" +
                        "   before: " + beforeFileInfo + "\n" +
                        "   new name: " + newName);
            }
            return beforeFileInfo;
        }
        
        // check for existing file or folder
        checkExists(targetParentRef, newName);
        
        QName qname = QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName(newName));
        
        // move or copy
        NodeRef targetNodeRef = null;
        if (move)
        {
            // move the node so that the association moves as well
            ChildAssociationRef newAssocRef = nodeService.moveNode(
                    sourceNodeRef,
                    targetParentRef,
                    assocRef.getTypeQName(),
                    qname);
            targetNodeRef = newAssocRef.getChildRef();
        }
        else
        {
            // copy the node
            targetNodeRef = copyService.copy(
                    sourceNodeRef,
                    targetParentRef,
                    assocRef.getTypeQName(),
                    qname,
                    true);
        }
        // changed the name property
        nodeService.setProperty(targetNodeRef, ContentModel.PROP_NAME, newName);
        
        // get the details after the operation
        FileInfo afterFileInfo = toFileInfo(targetNodeRef);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("" + (move ? "Moved" : "Copied") + " node: \n" +
                    "   parent: " + targetParentRef + "\n" +
                    "   before: " + beforeFileInfo + "\n" +
                    "   after: " + afterFileInfo);
        }
        return afterFileInfo;
    }
    
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName) throws FileExistsException
    {
        // file or folder
        boolean isFolder = false;
        try
        {
            isFolder = isFolder(typeQName);
        }
        catch (InvalidTypeException e)
        {
            throw new AlfrescoRuntimeException("The type is not supported by this service: " + typeQName);
        }
        
        // check for existing file or folder
        checkExists(parentNodeRef, name);
        
        // set up initial properties
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(11);
        properties.put(ContentModel.PROP_NAME, (Serializable) name);
        if (!isFolder)
        {
            // guess a mimetype based on the filename
            String mimetype = mimetypeService.guessMimetype(name);
            ContentData contentData = new ContentData(null, mimetype, 0L, "UTF-8");
            properties.put(ContentModel.PROP_CONTENT, contentData);
        }
        
        // create the node
        QName qname = QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName(name));
        ChildAssociationRef assocRef = nodeService.createNode(
                parentNodeRef,
                ContentModel.ASSOC_CONTAINS,
                qname,
                typeQName,
                properties);
        NodeRef nodeRef = assocRef.getChildRef();
        FileInfo fileInfo = toFileInfo(nodeRef);
        // done
        if (logger.isDebugEnabled())
        {
            FileInfo parentFileInfo = toFileInfo(parentNodeRef);
            logger.debug("Created: \n" +
                    "   parent: " + parentFileInfo + "\n" +
                    "   created: " + fileInfo);
        }
        return fileInfo;
    }
    
    public void delete(NodeRef nodeRef)
    {
        nodeService.deleteNode(nodeRef);
    }

    public FileInfo makeFolders(NodeRef parentNodeRef, List<String> pathElements, QName folderTypeQName)
    {
        if (pathElements.size() == 0)
        {
            throw new IllegalArgumentException("Path element list is empty");
        }
        
        // make sure that the folder is correct
        boolean isFolder = isFolder(folderTypeQName);
        if (!isFolder)
        {
            throw new IllegalArgumentException("Type is invalid to make folders with: " + folderTypeQName);
        }
        
        NodeRef currentParentRef = parentNodeRef;
        // just loop and create if necessary
        FileInfo lastFileInfo = null;
        for (String pathElement : pathElements)
        {
            try
            {
                // not present - make it
                FileInfo createdFileInfo = create(currentParentRef, pathElement, folderTypeQName);
                currentParentRef = createdFileInfo.getNodeRef();
                lastFileInfo = createdFileInfo;
            }
            catch (FileExistsException e)
            {
                // it exists - just get it
                List<FileInfo> fileInfos = search(currentParentRef, pathElement, false, true, false);
                if (fileInfos.size() == 0)
                {
                    // ? It must have been removed
                    throw new AlfrescoRuntimeException("Path element has just been removed: " + pathElement);
                }
                currentParentRef = fileInfos.get(0).getNodeRef();
                lastFileInfo = fileInfos.get(0);
            }
        }
        // done
        return lastFileInfo;
    }

    public List<FileInfo> getNamePath(NodeRef rootNodeRef, NodeRef nodeRef) throws FileNotFoundException
    {
        // check the root
        if (rootNodeRef == null)
        {
            rootNodeRef = nodeService.getRootNode(nodeRef.getStoreRef());
        }
        try
        {
            List<FileInfo> results = new ArrayList<FileInfo>(10);
            // get the primary path
            Path path = nodeService.getPath(nodeRef);
            // iterate and turn the results into file info objects
            boolean foundRoot = false;
            for (Path.Element element : path)
            {
                // ignore everything down to the root
                Path.ChildAssocElement assocElement = (Path.ChildAssocElement) element;
                NodeRef childNodeRef = assocElement.getRef().getChildRef();
                if (childNodeRef.equals(rootNodeRef))
                {
                    // just found the root - but we don't put in an entry for it
                    foundRoot = true;
                    continue;
                }
                else if (!foundRoot)
                {
                    // keep looking for the root
                    continue;
                }
                // we found the root and expect to be building the path up
                FileInfo pathInfo = toFileInfo(childNodeRef);
                results.add(pathInfo);
            }
            // check that we found the root
            if (!foundRoot || results.size() == 0)
            {
                throw new FileNotFoundException(nodeRef);
            }
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Built name path for node: \n" +
                        "   root: " + rootNodeRef + "\n" +
                        "   node: " + nodeRef + "\n" +
                        "   path: " + results);
            }
            return results;
        }
        catch (InvalidNodeRefException e)
        {
            throw new FileNotFoundException(nodeRef);
        }
    }

    public FileInfo resolveNamePath(NodeRef rootNodeRef, List<String> pathElements) throws FileNotFoundException
    {
        if (pathElements.size() == 0)
        {
            throw new IllegalArgumentException("Path elements list is empty");
        }
        // walk the folder tree first
        NodeRef parentNodeRef = rootNodeRef;
        StringBuilder currentPath = new StringBuilder(pathElements.size() * 20);
        int folderCount = pathElements.size() - 1;
        for (int i = 0; i < folderCount; i++)
        {
            String pathElement = pathElements.get(i);
            FileInfo pathElementInfo = getPathElementInfo(currentPath, rootNodeRef, parentNodeRef, pathElement, true);
            parentNodeRef = pathElementInfo.getNodeRef();
        }
        // we have resolved the folder path - resolve the last component
        String pathElement = pathElements.get(pathElements.size() - 1);
        FileInfo result = getPathElementInfo(currentPath, rootNodeRef, parentNodeRef, pathElement, false);
        // found it
        if (logger.isDebugEnabled())
        {
            logger.debug("Resoved path element: \n" +
                    "   root: " + rootNodeRef + "\n" +
                    "   path: " + currentPath + "\n" +
                    "   node: " + result);
        }
        return result;
    }
    
    /**
     * Helper method to dig down a level for a node based on name
     */
    private FileInfo getPathElementInfo(
            StringBuilder currentPath,
            NodeRef rootNodeRef,
            NodeRef parentNodeRef,
            String pathElement,
            boolean folderOnly) throws FileNotFoundException
    {
        currentPath.append("/").append(pathElement);
        
        boolean includeFiles = (folderOnly ? false : true);
        List<FileInfo> pathElementInfos = search(parentNodeRef, pathElement, includeFiles, true, false);
        // check
        if (pathElementInfos.size() == 0)
        {
            StringBuilder sb = new StringBuilder(128);
            sb.append(folderOnly ? "Folder" : "File or folder").append(" not found: \n")
              .append("   root: ").append(rootNodeRef).append("\n")
              .append("   path: ").append(currentPath);
            throw new FileNotFoundException(sb.toString());
        }
        else if (pathElementInfos.size() > 1)
        {
            // we have detected a duplicate name - warn, but allow
            StringBuilder sb = new StringBuilder(128);
            sb.append("Duplicate file or folder found: \n")
              .append("   root: ").append(rootNodeRef).append("\n")
              .append("   path: ").append(currentPath);
            logger.warn(sb);
        }
        FileInfo pathElementInfo = pathElementInfos.get(0);
        return pathElementInfo;
    }

    public FileInfo getFileInfo(NodeRef nodeRef)
    {
        try
        {
            return toFileInfo(nodeRef);
        }
        catch (InvalidTypeException e)
        {
            return null;
        }
    }

    public ContentReader getReader(NodeRef nodeRef)
    {
        FileInfo fileInfo = toFileInfo(nodeRef);
        if (fileInfo.isFolder())
        {
            throw new InvalidTypeException("Unable to get a content reader for a folder: " + fileInfo);
        }
        return contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
    }

    public ContentWriter getWriter(NodeRef nodeRef)
    {
        FileInfo fileInfo = toFileInfo(nodeRef);
        if (fileInfo.isFolder())
        {
            throw new InvalidTypeException("Unable to get a content writer for a folder: " + fileInfo);
        }
        return contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
    }
}
