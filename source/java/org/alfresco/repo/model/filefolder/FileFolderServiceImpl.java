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
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
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
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
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
    /** Shallow search for files and folders with a name pattern */
    private static final String XPATH_QUERY_SHALLOW_ALL =
        "./*" +
        "[like(@cm:name, $cm:name, false)" +
        " and not (subtypeOf('" + ContentModel.TYPE_SYSTEM_FOLDER + "'))" +
        " and (subtypeOf('" + ContentModel.TYPE_FOLDER + "') or subtypeOf('" + ContentModel.TYPE_CONTENT + "')" +
        " or subtypeOf('" + ContentModel.TYPE_LINK + "'))]";
    
    /** Shallow search for all files and folders */
    private static final String LUCENE_QUERY_SHALLOW_ALL =
        "+PARENT:\"${cm:parent}\"" +
        "-TYPE:\"" + ContentModel.TYPE_SYSTEM_FOLDER + "\" " +
        "+(" +
        "TYPE:\"" + ContentModel.TYPE_CONTENT + "\" " +
        "TYPE:\"" + ContentModel.TYPE_FOLDER + "\" " +
        "TYPE:\"" + ContentModel.TYPE_LINK + "\" " +
        ")";
    
    /** Shallow search for all files and folders */
    private static final String LUCENE_QUERY_SHALLOW_FOLDERS =
        "+PARENT:\"${cm:parent}\"" +
        "-TYPE:\"" + ContentModel.TYPE_SYSTEM_FOLDER + "\" " +
        "+TYPE:\"" + ContentModel.TYPE_FOLDER + "\" ";
    
    /** Shallow search for all files and folders */
    private static final String LUCENE_QUERY_SHALLOW_FILES =
        "+PARENT:\"${cm:parent}\"" +
        "-TYPE:\"" + ContentModel.TYPE_SYSTEM_FOLDER + "\" " +
        "+TYPE:\"" + ContentModel.TYPE_CONTENT + "\" ";
    
    /** Deep search for files and folders with a name pattern */
    private static final String XPATH_QUERY_DEEP_ALL =
        ".//*" +
        "[like(@cm:name, $cm:name, false)" +
        " and not (subtypeOf('" + ContentModel.TYPE_SYSTEM_FOLDER + "'))" +
        " and (subtypeOf('" + ContentModel.TYPE_FOLDER + "') or subtypeOf('" + ContentModel.TYPE_CONTENT + "')" +
        " or subtypeOf('" + ContentModel.TYPE_LINK + "'))]";
    
    /** empty parameters */
    private static final QueryParameterDefinition[] PARAMS_ANY_NAME = new QueryParameterDefinition[1];
    private static final QName PARAM_QNAME_PARENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "parent");
    
    private static Log logger = LogFactory.getLog(FileFolderServiceImpl.class);

    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private TenantService tenantService;
    private CopyService copyService;
    private SearchService searchService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    
    // TODO: Replace this with a more formal means of identifying "system" folders (i.e. aspect or UUID)
    private List systemPaths;
    private DataTypeDefinition dataTypeNodeRef;
    
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

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
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

    // TODO: Replace this with a more formal means of identifying "system" folders (i.e. aspect or UUID)
    public void setSystemPaths(List<String> systemPaths)
    {
        this.systemPaths = systemPaths;
    }
    
    
    public void init()
    {
        PARAMS_ANY_NAME[0] = new QueryParameterDefImpl(
                ContentModel.PROP_NAME,
                dictionaryService.getDataType(DataTypeDefinition.TEXT),
                true,
                "%");
        dataTypeNodeRef = dictionaryService.getDataType(DataTypeDefinition.NODE_REF);
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
            if (nodeService.exists(nodeRef))
            {
                FileInfo fileInfo = toFileInfo(nodeRef, true);
                results.add(fileInfo);
            }
        }
        return results;
    }
    
    /**
     * Helper method to convert a node reference instance to a file info
     */
    private FileInfo toFileInfo(NodeRef nodeRef, boolean addTranslations) throws InvalidTypeException
    {
        // Get the file attributes
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        // Is it a folder
        QName typeQName = nodeService.getType(nodeRef);
        boolean isFolder = isFolder(typeQName);
        
        // Construct the file info and add to the results
        FileInfo fileInfo = new FileInfoImpl(nodeRef, isFolder, properties);
        // Done
        return fileInfo;
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
        else if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) ||
        		dictionaryService.isSubClass(typeQName, ContentModel.TYPE_LINK))
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

    public List<FileInfo> list(NodeRef contextNodeRef)
    {
        // execute the query
        List<NodeRef> nodeRefs = luceneSearch(contextNodeRef, true, true);
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

    public List<FileInfo> listFiles(NodeRef contextNodeRef)
    {
        // execute the query
        List<NodeRef> nodeRefs = luceneSearch(contextNodeRef, false, true);
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

    public List<FileInfo> listFolders(NodeRef contextNodeRef)
    {
        // execute the query
        List<NodeRef> nodeRefs = luceneSearch(contextNodeRef, true, false);
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
    
    public NodeRef searchSimple(NodeRef contextNodeRef, String name)
    {
        NodeRef childNodeRef = nodeService.getChildByName(contextNodeRef, ContentModel.ASSOC_CONTAINS, name);
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Simple name search results: \n" +
                    "   parent: " + contextNodeRef + "\n" +
                    "   name: " + name + "\n" +
                    "   result: " + childNodeRef);
        }
        return childNodeRef;
    }

    /**
     * @see #search(NodeRef, String, boolean, boolean, boolean)
     */
    public List<FileInfo> search(NodeRef contextNodeRef, String namePattern, boolean includeSubFolders)
    {
        return search(contextNodeRef, namePattern, true, true, includeSubFolders);
    }

    private static final String LUCENE_MULTI_CHAR_WILDCARD = "*";
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
        // get the raw nodeRefs
        List<NodeRef> nodeRefs = searchInternal(contextNodeRef, namePattern, fileSearch, folderSearch, includeSubFolders);
        
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
     * Performs a full search, but doesn't translate the node references into
     * file info objects.  This allows {@link #checkExists(NodeRef, String)} to
     * bypass the retrieval of node properties.
     */
    private List<NodeRef> searchInternal(
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
        
        if (namePattern == null)
        {
            namePattern = LUCENE_MULTI_CHAR_WILDCARD;      // default to wildcard
        }
        // now check if we can use Lucene to handle this query
        boolean useLucene = false;
        boolean anyName = namePattern.equals(LUCENE_MULTI_CHAR_WILDCARD);
        if (!includeSubFolders && anyName)
        {
            // Lucene only handles any name or exact name
            useLucene = true;
        }
        
        List<NodeRef> nodeRefs = null;
        if (!useLucene)         // go with the XPath queries
        {
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
            nodeRefs = searchService.selectNodes(
                    contextNodeRef,
                    query,
                    params,
                    namespaceService,
                    false);
        }
        else            // go with Lucene queries
        {
            nodeRefs = luceneSearch(contextNodeRef, folderSearch, fileSearch);
        }
        // done
        return nodeRefs;
    }
    
    private List<NodeRef> luceneSearch(NodeRef contextNodeRef, boolean folders, boolean files)
    {
        contextNodeRef = tenantService.getName(contextNodeRef);

        SearchParameters params = new SearchParameters();
        params.setLanguage(SearchService.LANGUAGE_LUCENE);
        params.addStore(contextNodeRef.getStoreRef());
        // set the parent parameter
        QueryParameterDefinition parentParamDef = new QueryParameterDefImpl(
                PARAM_QNAME_PARENT,
                dataTypeNodeRef,
                true,
                contextNodeRef.toString());
        params.addQueryParameterDefinition(parentParamDef);
        if (folders && files)   // search for both files and folders
        {
            params.setQuery(LUCENE_QUERY_SHALLOW_ALL);
        }
        else if (folders)       // search for folders only
        {
            params.setQuery(LUCENE_QUERY_SHALLOW_FOLDERS);
        }
        else if (files)       // search for files only
        {
            params.setQuery(LUCENE_QUERY_SHALLOW_FILES);
        }
        else
        {
            throw new IllegalArgumentException("Must search for either files or folders or both");
        }
        ResultSet rs = searchService.query(params);
        int length = rs.length();
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(length);
        try
        {
            for (ResultSetRow row : rs)
            {
                nodeRefs.add(row.getNodeRef());
            }
        }
        finally
        {
            rs.close();
        }
        // done
        return nodeRefs;
    }
    
    /**
     * @see #move(NodeRef, NodeRef, String)
     */
    public FileInfo rename(NodeRef sourceNodeRef, String newName) throws FileExistsException, FileNotFoundException
    {
    	// NOTE:  
    	//
    	// This information is placed in the transaction to indicate that a rename has taken place.  This information is
    	// used by the rule trigger to ensure inbound rule is not triggered by a file rename
    	//
    	// See http://issues.alfresco.com/browse/AR-1544
    	AlfrescoTransactionSupport.bindResource(sourceNodeRef.toString()+"rename", sourceNodeRef);
    	
        return moveOrCopy(sourceNodeRef, null, newName, true);
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
        FileInfo beforeFileInfo = toFileInfo(sourceNodeRef, true);
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
        if (targetParentRef.equals(assocRef.getParentRef()))
        {
            if (newName.equals(beforeFileInfo.getName()))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Doing nothing - neither filename or parent has changed: \n" +
                            "   parent: " + targetParentRef + "\n" +
                            "   before: " + beforeFileInfo + "\n" +
                            "   new name: " + newName);
                }
                return beforeFileInfo;
            }
            else if (newName.equalsIgnoreCase(beforeFileInfo.getName()))
            {
            }
        }
        
        QName qname = QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName(newName));
        
        QName targetParentType = nodeService.getType(targetParentRef);
        
        // Fix AWC-1517
        QName assocTypeQname = null;
        if (dictionaryService.isSubClass(targetParentType, ContentModel.TYPE_FOLDER))
        {
        	assocTypeQname = ContentModel.ASSOC_CONTAINS; // cm:folder -> cm:contains
        }
        else if (dictionaryService.isSubClass(targetParentType, ContentModel.TYPE_CONTAINER))
        {
        	assocTypeQname = ContentModel.ASSOC_CHILDREN; // sys:container -> sys:children
        }
        else
        {
        	throw new InvalidTypeException("Unexpected type (" + targetParentType + ") for target parent: " + targetParentRef);
        }
               
        // move or copy
        NodeRef targetNodeRef = null;
        if (move)
        {
            // TODO: Replace this with a more formal means of identifying "system" folders (i.e. aspect or UUID)
            if (!isSystemPath(sourceNodeRef))
            {
                // move the node so that the association moves as well
                ChildAssociationRef newAssocRef = nodeService.moveNode(
                        sourceNodeRef,
                        targetParentRef,
                        assocTypeQname,
                        qname);
                targetNodeRef = newAssocRef.getChildRef();
            }
            else
            {
                // system path folders do not need to be moved
                targetNodeRef = sourceNodeRef;
            }
        }
        else
        {
            try
            {
                // copy the node
                targetNodeRef = copyService.copy(
                        sourceNodeRef,
                        targetParentRef,
                        assocTypeQname,
                        qname,
                        true);
            }
            catch (DuplicateChildNodeNameException e)
            {
                throw new FileExistsException(targetParentRef, newName);
            }
        }
       
        // Only update the name if it has changed
        String currentName = (String)nodeService.getProperty(targetNodeRef, ContentModel.PROP_NAME);
        if (currentName.equals(newName) == false)
        {
            try
            {
                // changed the name property
                nodeService.setProperty(targetNodeRef, ContentModel.PROP_NAME, newName);
                
                // May need to update the mimetype, to support apps using .tmp files when saving
                ContentData contentData = (ContentData)nodeService.getProperty(targetNodeRef, ContentModel.PROP_CONTENT);
                if (contentData != null)
                {
                    String targetMimetype = contentData.getMimetype();
                    String newMimetype = mimetypeService.guessMimetype(newName);
                    if (!targetMimetype.equalsIgnoreCase(newMimetype))
                	{
                        contentData = ContentData.setMimetype(contentData, newMimetype);
                        nodeService.setProperty(targetNodeRef, ContentModel.PROP_CONTENT, contentData);
                	}
                }
            }
            catch (DuplicateChildNodeNameException e)
            {
                throw new FileExistsException(targetParentRef, newName);
            }
        }
        
        // get the details after the operation
        FileInfo afterFileInfo = toFileInfo(targetNodeRef, true);
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
    
    /**
     * Determine if the specified node is a special "system" folder path based node
     * 
     * TODO: Replace this with a more formal means of identifying "system" folders (i.e. aspect or UUID)
     * 
     * @param nodeRef  node to check
     * @return  true => system folder path based node
     */
    private boolean isSystemPath(NodeRef nodeRef)
    {
        Path path = nodeService.getPath(nodeRef);
        String prefixedPath = path.toPrefixString(namespaceService);
        return systemPaths.contains(prefixedPath);
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
        ChildAssociationRef assocRef = null;
        try
        {
            assocRef = nodeService.createNode(
                    parentNodeRef,
                    ContentModel.ASSOC_CONTAINS,
                    qname,
                    typeQName,
                    properties);
        }
        catch (DuplicateChildNodeNameException e)
        {
            throw new FileExistsException(parentNodeRef, name);
        }
        
        NodeRef nodeRef = assocRef.getChildRef();
        FileInfo fileInfo = toFileInfo(nodeRef, true);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created: \n" +
                    "   parent: " + parentNodeRef + "\n" +
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
        for (String pathElement : pathElements)
        {
            // does it exist?
            NodeRef nodeRef = searchSimple(currentParentRef, pathElement);
            if (nodeRef == null)
            {
                // not present - make it
                FileInfo createdFileInfo = create(currentParentRef, pathElement, folderTypeQName);
                currentParentRef = createdFileInfo.getNodeRef();
            }
            else
            {
                // it exists
                currentParentRef = nodeRef;
            }
        }
        // done
        FileInfo fileInfo = toFileInfo(currentParentRef, true);
        return fileInfo;
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
                FileInfo pathInfo = toFileInfo(childNodeRef, true);
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
        StringBuilder currentPath = new StringBuilder(pathElements.size() << 4);
        int folderCount = pathElements.size() - 1;
        for (int i = 0; i < folderCount; i++)
        {
            String pathElement = pathElements.get(i);
            NodeRef folderNodeRef = searchSimple(parentNodeRef, pathElement);
            if (folderNodeRef == null)
            {
                StringBuilder sb = new StringBuilder(128);
                sb.append("Folder not found: " + currentPath);
                throw new FileNotFoundException(sb.toString());
            }
            parentNodeRef = folderNodeRef;
        }
        // we have resolved the folder path - resolve the last component
        String pathElement = pathElements.get(pathElements.size() - 1);
        NodeRef fileNodeRef = searchSimple(parentNodeRef, pathElement);
        if (fileNodeRef == null)
        {
            StringBuilder sb = new StringBuilder(128);
            sb.append("File not found: " + currentPath);
            throw new FileNotFoundException(sb.toString());
        }
        FileInfo result = getFileInfo(fileNodeRef);
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

    public FileInfo getFileInfo(NodeRef nodeRef)
    {
        try
        {
            return toFileInfo(nodeRef, true);
        }
        catch (InvalidTypeException e)
        {
            return null;
        }
    }

    public ContentReader getReader(NodeRef nodeRef)
    {
        FileInfo fileInfo = toFileInfo(nodeRef, false);
        if (fileInfo.isFolder())
        {
            throw new InvalidTypeException("Unable to get a content reader for a folder: " + fileInfo);
        }
        return contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
    }

    public ContentWriter getWriter(NodeRef nodeRef)
    {
        FileInfo fileInfo = toFileInfo(nodeRef, false);
        if (fileInfo.isFolder())
        {
            throw new InvalidTypeException("Unable to get a content writer for a folder: " + fileInfo);
        }
        return contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
    }
}
