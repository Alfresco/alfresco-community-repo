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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.filesys.repo;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileExistsException;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.util.WildCard;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.SearchLanguageConversion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class with supplying helper methods and potentially acting as a cache for
 * queries.
 *  
 * @author derekh
 */
public class CifsHelper
{
    // Logging
    private static Log logger = LogFactory.getLog(CifsHelper.class);
    
    // Services
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private MimetypeService mimetypeService;
    private PermissionService permissionService;
       
    private Set<QName> excludedTypes = new HashSet<QName>();
    
    private boolean isReadOnlyFlagOnFolders = false;
    
    /**
     * Class constructor
     */
    public CifsHelper()
    {
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Return the node service
     * 
     * @return NodeService
     */
    protected NodeService getNodeService() {
        return nodeService;
    }
    
    public void setExcludedTypes(List<String> excludedTypes)
    {
        for(String exType:excludedTypes)
        {
            this.excludedTypes.add(QName.createQName(exType));
        }
    }
   
    /**
     * Controls whether the read only flag is set on folders. This flag, when set, may cause problematic # behaviour in
     * Windows clients and doesn't necessarily mean a folder can't be written to. See ALF-6727. Should we ever set the
     * read only flag on folders?
     * 
     * @param setReadOnlyFlagOnFolders
     *            the setReadOnlyFlagOnFolders to set
     */
    public void setReadOnlyFlagOnFolders(boolean setReadOnlyFlagOnFolders)
    {
        this.isReadOnlyFlagOnFolders = setReadOnlyFlagOnFolders;
    }

    /**
     * @param serviceRegistry for repo connection
     * @param nodeRef
     * @return Returns true if the node is a subtype of {@link ContentModel#TYPE_FOLDER folder}
     * @throws AlfrescoRuntimeException if the type is neither related to a folder or content
     */
    public boolean isDirectory(NodeRef nodeRef)
    {
        QName nodeTypeQName = nodeService.getType(nodeRef);
        if (dictionaryService.isSubClass(nodeTypeQName, ContentModel.TYPE_FOLDER))
        {
            return true;
        }
        else if (dictionaryService.isSubClass(nodeTypeQName, ContentModel.TYPE_CONTENT))
        {
            return false;
        }
        else
        {
            // it is not a directory, but what is it?
            return false;   
        }
    }

    /**
     * Extract a single node's file info, where the node is reference by
     * a path relative to an ancestor node.
     * 
     * @param pathRootNodeRef
     * @param path the path
     * @return Returns the existing node reference
     * @throws FileNotFoundException
     */
    public ContentFileInfo getFileInformation(NodeRef pathRootNodeRef, String path, boolean readOnly, boolean lockedFilesAsOffline) throws FileNotFoundException
    {
        // get the node being referenced
        NodeRef nodeRef = getNodeRef(pathRootNodeRef, path);

        return getFileInformation(nodeRef, readOnly, lockedFilesAsOffline);
    }

    /**
     * Helper method to extract file info from a specific node.
     * <p>
     * This method goes direct to the repo for all information and no data is
     * cached here.
     * 
     * @param nodeRef the node
     * @param readOnly
     * @param lockedFilesAsOffline
     * 
     * @return Returns the file information pertinent to the node
     * @throws FileNotFoundException if the path refers to a non-existent file
     */
    public ContentFileInfo getFileInformation(NodeRef nodeRef, boolean readOnly, boolean lockedFilesAsOffline) throws FileNotFoundException
    {
        // get the file info
        org.alfresco.service.cmr.model.FileInfo fileFolderInfo = fileFolderService.getFileInfo(nodeRef);
        
        // retrieve required properties and create new JLAN file info
        ContentFileInfo fileInfo = new ContentFileInfo(nodeRef);
        
        // unset all attribute flags
        int fileAttributes = 0;
        fileInfo.setFileAttributes(fileAttributes);
        
        if (fileFolderInfo.isFolder())
        {
            // add directory attribute
            fileAttributes |= FileAttribute.Directory;
            fileInfo.setFileAttributes(fileAttributes);
        }
        else
        {
            Map<QName, Serializable> nodeProperties = fileFolderInfo.getProperties();
            
            // Get the file size from the content
            
            ContentData contentData = (ContentData) nodeProperties.get(ContentModel.PROP_CONTENT);
            long size = 0L;
            if (contentData != null)
            {
                size = contentData.getSize();
            }
            fileInfo.setSize(size);
            
            // Set the allocation size by rounding up the size to a 512 byte block boundary
            
            if ( size > 0)
                fileInfo.setAllocationSize((size + 512L) & 0xFFFFFFFFFFFFFE00L);
            
            // Check the lock status of the file
            
            String lockTypeStr = (String) nodeProperties.get(ContentModel.PROP_LOCK_TYPE);
                    
            if ( lockTypeStr != null )
            {
                // File is locked so mark it as read-only and offline

            	int attr = fileInfo.getFileAttributes();
            	
            	if (( attr & FileAttribute.ReadOnly) == 0)
            		attr += FileAttribute.ReadOnly;
            	
                if ( lockedFilesAsOffline)
                {
                	attr += FileAttribute.NTOffline;
                }
                
                fileInfo.setFileAttributes( attr);
            }
            
            // Check if it is a link node
            
            if ( fileFolderInfo.isLink())
            	fileInfo.setLinkNodeRef( fileFolderInfo.getLinkNodeRef());
        }
        
        // created
        Date createdDate = fileFolderInfo.getCreatedDate();
        if (createdDate != null)
        {
            long created = DefaultTypeConverter.INSTANCE.longValue(createdDate);
            fileInfo.setCreationDateTime(created);
        }
        // modified
        Date modifiedDate = fileFolderInfo.getModifiedDate();
        if (modifiedDate != null)
        {
            long modified = DefaultTypeConverter.INSTANCE.longValue(modifiedDate);
            fileInfo.setModifyDateTime(modified);
            fileInfo.setAccessDateTime(modified);
        }
        // name
        String name = fileFolderInfo.getName();
        if (name != null)
        {
            fileInfo.setFileName(name);
        }
        
        // Read/write access
        
        if (!fileFolderInfo.isFolder() || isReadOnlyFlagOnFolders)
        {
            boolean deniedPermission = permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.DENIED; 
            if (readOnly || deniedPermission)
            {
                int attr = fileInfo.getFileAttributes();
                if (( attr & FileAttribute.ReadOnly) == 0)
                {
                    attr += FileAttribute.ReadOnly;
                    fileInfo.setFileAttributes(attr);
                }
            }
        }

        // Set the normal file attribute if no other attributes are set
        
        if ( fileInfo.getFileAttributes() == 0)
            fileInfo.setFileAttributes(FileAttribute.NTNormal);
        
        // Debug
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Fetched file info: \n" +
                    "   info: " + fileInfo);
        }
        
        // Return the file information
        
        return fileInfo;
    }
    
    /**
     * Creates a file or directory using the given paths.
     * <p>
     * If the directory path doesn't exist, then all the parent directories will be created.
     * If the file path is <code>null</code>, then the file will not be created
     * 
     * @param rootNodeRef the root node of the path
     * @param path the path to a node
     * @param isFile true if the node to be created must be a file
     * @return Returns a newly created file or folder node
     * @throws FileExistsException if the file or folder already exists
     */
    public NodeRef createNode(NodeRef rootNodeRef, String path, QName typeQName) throws FileExistsException
    {
        // split the path up into its constituents
        StringTokenizer tokenizer = new StringTokenizer(path, FileName.DOS_SEPERATOR_STR, false);
        List<String> folderPathElements = new ArrayList<String>(10);
        String name = null;
        while (tokenizer.hasMoreTokens())
        {
            String pathElement = tokenizer.nextToken();
            
            if (!tokenizer.hasMoreTokens())
            {
                // the last token becomes the name
                name = pathElement;
            }
            else
            {
                // add the path element to the parent folder path
                folderPathElements.add(pathElement);
            }
        }
        // ensure that the folder path exists
        NodeRef parentFolderNodeRef = rootNodeRef;
        if (folderPathElements.size() > 0)
        {
            parentFolderNodeRef = FileFolderUtil.makeFolders(
                    fileFolderService,
                    rootNodeRef,
                    folderPathElements,
                    ContentModel.TYPE_FOLDER).getNodeRef();
        }
        try
        {
            NodeRef nodeRef = fileFolderService.create(parentFolderNodeRef, name, typeQName).getNodeRef();
            
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Created node: \n" +
                        "   device root: " + rootNodeRef + "\n" +
                        "   path: " + path + "\n" +
                        "   type: " + typeQName + "\n" +
                        "   new node: " + nodeRef);
            }
            return nodeRef;
        }
        catch (org.alfresco.service.cmr.model.FileExistsException e)
        {
            throw new FileExistsException(path);
        }
    }

    private void addDescendents(List<NodeRef> pathRootNodeRefs, Stack<String> pathElements, List<NodeRef> results)
    {
        if (pathElements.isEmpty())
        {
            // if this method is called with an empty path element stack, then the
            // current context nodes are the results to be added
            results.addAll(pathRootNodeRefs);
            return;
        }
        
        // take the first path element off the stack
        String pathElement = pathElements.pop();

        // iterate over each path root node
        for (NodeRef pathRootNodeRef : pathRootNodeRefs)
        {
            // deal with cyclic relationships by not traversing down any node already in the results
            if (results.contains(pathRootNodeRef))
            {
                continue;
            }
            // get direct descendents along the path
            List<NodeRef> directDescendents = getDirectDescendents(pathRootNodeRef, pathElement);
            // recurse onto the descendents
            addDescendents(directDescendents, pathElements, results);
        }
        
        // restore the path element stack
        pathElements.push(pathElement);
    }
    
    /**
     * Searches for the node or nodes that match the path element for the given parent node
     */
    private List<NodeRef> getDirectDescendents(NodeRef pathRootNodeRef, String pathElement)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Getting direct descendents: \n" +
                    "   Path Root: " + pathRootNodeRef + "\n" +
                    "   Path Element: " + pathElement);
        }
        List<NodeRef> results = null;
        // if this contains no wildcards, then we can fasttrack it
        if (!WildCard.containsWildcards(pathElement))
        {
            // a specific name is required
            NodeRef foundNodeRef = fileFolderService.searchSimple(pathRootNodeRef, pathElement);
            if (foundNodeRef == null)
            {
                results = Collections.emptyList();
            }
            else
            {
                results = Collections.singletonList(foundNodeRef);
            }
        }
        else
        {
            // escape for the Lucene syntax search
            String escapedPathElement = SearchLanguageConversion.convertCifsToLucene(pathElement);
            // do the lookup
            List<org.alfresco.service.cmr.model.FileInfo> childInfos = fileFolderService.search(
                    pathRootNodeRef,
                    escapedPathElement,
                    false);
            // convert to noderefs
            results = new ArrayList<NodeRef>(childInfos.size());
            for (org.alfresco.service.cmr.model.FileInfo info : childInfos)
            {
                results.add(info.getNodeRef());
            }
        }
        // done
        return results;
    }

    /**
     * Finds the nodes being reference by the given directory and file paths.
     * <p>
     * Examples of the path are:
     * <ul>
     *   <li>\New Folder\New Text Document.txt</li>
     *   <li>\New Folder\Sub Folder</li>
     * </ul>
     * 
     * @param searchRootNodeRef the node from which to start the path search
     * @param path the search path to either a folder or file
     * @return Returns references to all matching nodes
     */
    public List<NodeRef> getNodeRefs(NodeRef pathRootNodeRef, String path)
    {
        // tokenize the path and push into a stack in reverse order so that
        // the root directory gets popped first
        StringTokenizer tokenizer = new StringTokenizer(path, FileName.DOS_SEPERATOR_STR, false);
        String[] tokens = new String[tokenizer.countTokens()];
        int count = 0;
        while(tokenizer.hasMoreTokens())
        {
            tokens[count] = tokenizer.nextToken();
            count++;
        }
        Stack<String> pathElements = new Stack<String>();
        for (int i = tokens.length - 1; i >= 0; i--)
        {
            pathElements.push(tokens[i]);
        }
        
        // start with a single parent node
        List<NodeRef> pathRootNodeRefs = Collections.singletonList(pathRootNodeRef);
        
        // result storage
        List<NodeRef> results = new ArrayList<NodeRef>(5);
        List<NodeRef> rubeResults = new ArrayList<NodeRef>(5);
        
        // kick off the path walking
        addDescendents(pathRootNodeRefs, pathElements, rubeResults); 
        
        for (NodeRef nodeRef : rubeResults)
        {
            QName nodeType = nodeService.getType(nodeRef);
            if (!excludedTypes.contains(nodeType))
            {
                results.add(nodeRef);
            }
        }
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved node references for path: \n" +
                    "   path root: " + pathRootNodeRef + "\n" +
                    "   path: " + path + "\n" +
                    "   results: " + results);
        }
        return results;
    }
    
    /**
     * Attempts to fetch a specific single node at the given path.
     * <p>
     * The path may contain wild cards
     * <p>
     * 
     * @throws FileNotFoundException if the path can't be resolved to a node
     * 
     * @see #getNodeRefs(NodeRef, String)
     */
    public NodeRef getNodeRef(NodeRef pathRootNodeRef, String path) throws FileNotFoundException
    {
        // attempt to get the file/folder node using hierarchy walking
        List<NodeRef> nodeRefs = getNodeRefs(pathRootNodeRef, path);
        if (nodeRefs.size() == 0)
        {
            throw new FileNotFoundException(path);
        }
        else if (nodeRefs.size() > 1)
        {
            logger.warn("Multiple matching nodes: \n" +
                    "   search root: " + pathRootNodeRef + "\n" +
                    "   path: " + path);
        }
        // take the first one - not sure if it is possible for the path to refer to more than one
        NodeRef nodeRef = nodeRefs.get(0);
        // done
        return nodeRef;
    }

    /**
     * Relink the content data from a new node to an existing node to preserve the version history.
     * 
     * @param oldNodeRef NodeRef
     * @param newNodeRef NodeRef
     */
    public void relinkNode(NodeRef tempNodeRef, NodeRef nodeToMoveRef, NodeRef newParentNodeRef, String newName)
            throws FileNotFoundException, FileExistsException
    {
        // Get the properties for the old and new nodes
        org.alfresco.service.cmr.model.FileInfo tempFileInfo = fileFolderService.getFileInfo(tempNodeRef);
        org.alfresco.service.cmr.model.FileInfo fileToMoveInfo = fileFolderService.getFileInfo(nodeToMoveRef);
        
        // Save the current name of the old node
        String tempName = tempFileInfo.getName();

        try
        {
            // Rename operation will add or remove the sys:temporary aspect appropriately
            
            // rename temp file to the new name
            fileFolderService.rename(tempNodeRef, newName);
            
            // rename new file to old name
            fileFolderService.rename(nodeToMoveRef, tempName);
        }
        catch (org.alfresco.service.cmr.model.FileNotFoundException e)
        {
            throw new FileNotFoundException(e.getMessage());
        }
        catch (org.alfresco.service.cmr.model.FileExistsException e)
        {
            throw new FileExistsException(e.getMessage());
        }
        
        if (!tempFileInfo.isFolder() && !fileToMoveInfo.isFolder())
        {
            // swap the content between the two
            ContentData oldContentData = tempFileInfo.getContentData();
            if (oldContentData == null)
            {
                String mimetype = mimetypeService.guessMimetype(tempName);
                oldContentData = ContentData.setMimetype(null, mimetype);
            }
            
            ContentData newContentData = fileToMoveInfo.getContentData();

            // Reset the mime type
            // TODO Pass the content along when guessing the mime type, so we're more accurate
            String mimetype = mimetypeService.guessMimetype(newName);
            newContentData = ContentData.setMimetype(newContentData, mimetype);

            nodeService.setProperty(tempNodeRef, ContentModel.PROP_CONTENT, newContentData);
            nodeService.setProperty(nodeToMoveRef, ContentModel.PROP_CONTENT, oldContentData);
        }
    }
    
    /**
     * Move a node
     * 
     * @param nodeToMoveRef Node to be moved
     * @param newParentNodeRef New parent folder node
     * @param newName New name for the moved node
     * @throws FileExistsException
     */
    public void move(NodeRef nodeToMoveRef, NodeRef oldParent, NodeRef newParentNodeRef, String newName) throws FileExistsException
    {
        try
        {
            fileFolderService.moveFrom(nodeToMoveRef, oldParent, newParentNodeRef, newName);
        }
        catch (org.alfresco.service.cmr.model.FileExistsException e)
        {
            throw new FileExistsException(newName);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Move failed: \n" +
                    "   node to move: " + nodeToMoveRef + "\n" +
                    "   new parent: " + newParentNodeRef + "\n" +
                    "   new name: " + newName,
                    e);
        }
    }
    
    /**
     * Rename a node
     * 
     * @param nodeToRenameRef Node to be renamed
     * @param newName New name for the node
     * @throws FileExistsException
     */
    public void rename(NodeRef nodeToRenameRef, String newName) throws FileExistsException
    {
        try
        {
            fileFolderService.rename(nodeToRenameRef, newName);
        }
        catch (org.alfresco.service.cmr.model.FileExistsException e)
        {
            throw new FileExistsException(newName);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Rename failed: \n" +
                    "   node to rename: " + nodeToRenameRef + "\n" +
                    "   new name: " + newName,
                    e);
        }
    }
    
    /**
     * Return the file name for a node
     * 
     * @param node NodeRef
     * @return String
     * @throws FileNotFoundException
     */
    public String getFileName(NodeRef node)
    {
        String fname = null;
        
        try
        {
            fname = (String) nodeService.getProperty( node, ContentModel.PROP_NAME);
        }
        catch (InvalidNodeRefException ex)
        {
        }
        
        return fname;
    }
    
    /**
     * Check if the folder node is empty
     * 
     * @param folderNode NodeRef
     * @return boolean
     */
    public boolean isFolderEmpty( NodeRef folderNode) {

    	// Check if the node has any child files/folders
    	
    	List<FileInfo> files = fileFolderService.listFiles( folderNode);
    	if ( files == null || files.size() == 0)
    	{
    		return true;
    	}
    	return false;
    }
}
