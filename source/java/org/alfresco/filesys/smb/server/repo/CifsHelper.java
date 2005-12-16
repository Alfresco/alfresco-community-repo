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
package org.alfresco.filesys.smb.server.repo;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileExistsException;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
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
     * @param path
     * @return Returns the existing node reference
     * @throws FileNotFoundException
     */
    public FileInfo getFileInformation(NodeRef pathRootNodeRef, String path) throws FileNotFoundException
    {
        // get the node being referenced
        NodeRef nodeRef = getNodeRef(pathRootNodeRef, path);

        FileInfo fileInfo = getFileInformation(nodeRef);

        return fileInfo;
    }

    /**
     * Helper method to extract file info from a specific node.
     * <p>
     * This method goes direct to the repo for all information and no data is
     * cached here.
     * 
     * @param nodeRef the node that the path is relative to
     * @param path the path to get info for
     * @return Returns the file information pertinent to the node
     * @throws FileNotFoundException if the path refers to a non-existent file
     */
    public FileInfo getFileInformation(NodeRef nodeRef) throws FileNotFoundException
    {
        // get the file info
        org.alfresco.service.cmr.model.FileInfo fileFolderInfo = fileFolderService.getFileInfo(nodeRef);
        
        // retrieve required properties and create file info
        FileInfo fileInfo = new FileInfo();
        
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
            // get the file size
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
        }
        // name
        String name = fileFolderInfo.getName();
        if (name != null)
        {
            fileInfo.setFileName(name);
        }
        
        // read/write access
        if ( permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.DENIED)
            fileInfo.setFileAttributes(fileInfo.getFileAttributes() + FileAttribute.ReadOnly);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Fetched file info: \n" +
                    "   info: " + fileInfo);
        }
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
    public NodeRef createNode(NodeRef rootNodeRef, String path, boolean isFile) throws FileExistsException
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
            parentFolderNodeRef = fileFolderService.makeFolders(
                    rootNodeRef,
                    folderPathElements,
                    ContentModel.TYPE_FOLDER).getNodeRef();
        }
        // add the file or folder
        QName typeQName = isFile ? ContentModel.TYPE_CONTENT : ContentModel.TYPE_FOLDER;
        try
        {
            NodeRef nodeRef = fileFolderService.create(parentFolderNodeRef, name, typeQName).getNodeRef();
            
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Created node: \n" +
                        "   device root: " + rootNodeRef + "\n" +
                        "   path: " + path + "\n" +
                        "   is file: " + isFile + "\n" +
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
     * Performs an XPath query to get the first-level descendents matching the given path
     * 
     * @param pathRootNodeRef
     * @param pathElement
     * @return
     */
    private List<NodeRef> getDirectDescendents(NodeRef pathRootNodeRef, String pathElement)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Getting direct descendents: \n" +
                    "   Path Root: " + pathRootNodeRef + "\n" +
                    "   Path Element: " + pathElement);
        }
        
        // do the lookup
        List<org.alfresco.service.cmr.model.FileInfo> childInfos = fileFolderService.search(pathRootNodeRef, pathElement, false);
        // convert to noderefs
        List<NodeRef> results = new ArrayList<NodeRef>(childInfos.size());
        for (org.alfresco.service.cmr.model.FileInfo info : childInfos)
        {
            results.add(info.getNodeRef());
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
        
        // kick off the path walking
        addDescendents(pathRootNodeRefs, pathElements, results); 
        
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
            if (newContentData == null)
            {
                String mimetype = mimetypeService.guessMimetype(newName);
                newContentData = ContentData.setMimetype(null, mimetype);
            }
            
            nodeService.setProperty(tempNodeRef, ContentModel.PROP_CONTENT, newContentData);
            nodeService.setProperty(nodeToMoveRef, ContentModel.PROP_CONTENT, oldContentData);
        }
    }
    
    public void move(NodeRef nodeToMoveRef, NodeRef newParentNodeRef, String newName) throws FileExistsException
    {
        try
        {
            fileFolderService.move(nodeToMoveRef, newParentNodeRef, newName);
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
}
