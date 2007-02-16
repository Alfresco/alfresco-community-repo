/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.cmr.model;

import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Provides methods specific to manipulating {@link org.alfresco.model.ContentModel#TYPE_CONTENT files}
 * and {@link org.alfresco.model.ContentModel#TYPE_FOLDER folders}.
 * 
 * @see org.alfresco.model.ContentModel
 * 
 * @author Derek Hulley
 */
@PublicService
public interface FileFolderService
{
    /**
     * Lists immediate child files and folders of the given context node
     * 
     * @param contextNodeRef the node to start searching in
     * @return Returns a list of matching files and folders
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contextNodeRef"})
    public List<FileInfo> list(NodeRef contextNodeRef);
    
    /**
     * Lists all immediate child files of the given context node
     * 
     * @param folderNodeRef the folder to start searching in
     * @return Returns a list of matching files
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"folderNodeRef"})
    public List<FileInfo> listFiles(NodeRef folderNodeRef);
    
    /**
     * Lists all immediate child folders of the given context node
     * 
     * @param contextNodeRef the node to start searching in
     * @return Returns a list of matching folders
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contextNodeRef"})
    public List<FileInfo> listFolders(NodeRef contextNodeRef);

    /**
     * Get a simple list of nodes that have the given name within the parent node
     * 
     * @param contextNodeRef the parent node
     * @param name the name of the node to search for
     * @return Returns the node that has the given name - or null if not found
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contextNodeRef", "name"})
    public NodeRef searchSimple(NodeRef contextNodeRef, String name);

    /**
     * Searches for all files and folders with the matching name pattern,
     * using wildcard characters <b>*</b> and <b>?</b>.
     * 
     * @param contextNodeRef the context of the search.  This node will never be returned
     *      as part of the search results.
     * @param namePattern the name of the file or folder to search for, or a
     *      {@link org.alfresco.util.SearchLanguageConversion#DEF_LUCENE wildcard} pattern
     *      to search for.
     * @param includeSubFolders true to search the entire hierarchy below the search context
     * @return Returns a list of file or folder matches
     *
     * @see #search(NodeRef, String, boolean, boolean, boolean)
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contextNodeRef", "namePattern", "includeSubFolders"})
    public List<FileInfo> search(
            NodeRef contextNodeRef,
            String namePattern,
            boolean includeSubFolders);
    
    /**
     * Perform a search against the name of the files or folders within a hierarchy.
     * Wildcard characters are <b>*</b> and <b>?</b>.
     * 
     * @param contextNodeRef the context of the search.  This node will never be returned
     *      as part of the search results.
     * @param namePattern the name of the file or folder to search for, or a
     *      {@link org.alfresco.util.SearchLanguageConversion#DEF_LUCENE wildcard} pattern
     *      to search for.
     * @param fileSearch true if file types are to be included in the search results
     * @param folderSearch true if folder types are to be included in the search results
     * @param includeSubFolders true to search the entire hierarchy below the search context
     * @return Returns a list of file or folder matches
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"contextNodeRef", "namePattern", "fileSearch", "folderSearch", "includeSubFolders"})
    public List<FileInfo> search(
            NodeRef contextNodeRef,
            String namePattern,
            boolean fileSearch,
            boolean folderSearch,
            boolean includeSubFolders);
    
    /**
     * Rename a file or folder in its current location
     * 
     * @param fileFolderRef the file or folder to rename
     * @param newName the new name
     * @return Return the new file info
     * @throws FileExistsException if a file or folder with the new name already exists
     * @throws FileNotFoundException the file or folder reference doesn't exist
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"fileFolderRef", "newName"})
    public FileInfo rename(NodeRef fileFolderRef, String newName) throws FileExistsException, FileNotFoundException;
    
    /**
     * Move a file or folder to a new name and/or location.
     * <p>
     * If both the parent folder and name remain the same, then nothing is done.
     * 
     * @param sourceNodeRef the file or folder to move
     * @param targetParentRef the new parent node to move the node to - null means rename in situ
     * @param newName the name to change the file or folder to - null to keep the existing name
     * @return Returns the new file info
     * @throws FileExistsException
     * @throws FileNotFoundException
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"sourceNodeRef", "targetParentRef", "newName"})
    public FileInfo move(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName)
            throws FileExistsException, FileNotFoundException;

    /**
     * Copy a source file or folder.  The source can be optionally renamed and optionally
     * moved into another folder.
     * <p>
     * If both the parent folder and name remain the same, then nothing is done.
     * 
     * @param sourceNodeRef the file or folder to copy
     * @param targetParentRef the new parent node to copy the node to - null means rename in situ
     * @param newName the new name, or null to keep the existing name.
     * @return Return the new file info
     * @throws FileExistsException
     * @throws FileNotFoundException
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"sourceNodeRef", "targetParentRef", "newName"})
    public FileInfo copy(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName)
            throws FileExistsException, FileNotFoundException;

    /**
     * Create a file or folder; or any valid node of type derived from file or folder
     * 
     * @param parentNodeRef the parent node.  The parent must be a valid
     *      {@link org.alfresco.model.ContentModel#TYPE_CONTAINER container}.
     * @param name the name of the node
     * @param typeQName the type to create
     * @return Returns the new node's file information
     * @throws FileExistsException
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"parentNodeRef", "name", "typeQName"})
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName) throws FileExistsException;
    
    /**
     * Delete a file or folder
     * 
     * @param nodeRef the node to delete
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public void delete(NodeRef nodeRef);
    
    /**
     * Checks for the presence of, and creates as necessary, the folder structure in the provided path.
     * <p>
     * An empty path list is not allowed as it would be impossible to necessarily return file info
     * for the parent node - it might not be a folder node.
     * 
     * @param parentNodeRef the node under which the path will be created
     * @param pathElements the folder name path to create - may not be empty
     * @param folderTypeQName the types of nodes to create.  This must be a valid subtype of
     *      {@link org.alfresco.model.ContentModel#TYPE_FOLDER they folder type}.
     * @return Returns the info of the last folder in the path.
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"parentNodeRef", "pathElements", "folderTypeQName"})
    public FileInfo makeFolders(NodeRef parentNodeRef, List<String> pathElements, QName folderTypeQName);
    
    /**
     * Get the file or folder names from the root down to and including the node provided.
     * <ul>
     *   <li>The root node can be of any type and is not included in the path list.</li>
     *   <li>Only the primary path is considered.  If the target node is not a descendant of the
     *       root along purely primary associations, then an exception is generated.</li>
     *   <li>If an invalid type is encountered along the path, then an exception is generated.</li>
     * </ul>
     * 
     * @param rootNodeRef the start of the returned path, or null if the <b>store</b> root
     *      node must be assumed.
     * @param nodeRef a reference to the file or folder
     * @return Returns a list of file/folder infos from the root (excluded) down to and
     *      including the destination file or folder
     * @throws FileNotFoundException if the node could not be found
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"rootNodeRef", "nodeRef"})
    public List<FileInfo> getNamePath(NodeRef rootNodeRef, NodeRef nodeRef) throws FileNotFoundException;
    
    /**
     * Resolve a file or folder name path from a given root node down to the final node.
     * 
     * @param rootNodeRef the start of the path given, i.e. the '/' in '/A/B/C' for example
     * @param pathElements a list of names in the path
     * @return Returns the info of the file or folder
     * @throws FileNotFoundException if no file or folder exists along the path
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"rootNodeRef", "pathElements"})
    public FileInfo resolveNamePath(NodeRef rootNodeRef, List<String> pathElements) throws FileNotFoundException;
    
    /**
     * Get the file info (name, folder, etc) for the given node
     * 
     * @param nodeRef the node to get info for
     * @return Returns the file info or null if the node does not represent a file or folder
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public FileInfo getFileInfo(NodeRef nodeRef);
    
    /**
     * Get the reader to the file represented by the node according to the File/Folder model.
     * (This is not the same as the method on the ContentService)
     * 
     * @param nodeRef the content node
     * @return Returns a handle to the content associated with the node
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public ContentReader getReader(NodeRef nodeRef);
    
    /**
     * Get the writer to the file represented by the node according to the File/Folder model.
     * (This is not the same as the method on the ContentService)
     * 
     * @param nodeRef the content node
     * @return Returns a handle to the content associated with the node
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public ContentWriter getWriter(NodeRef nodeRef);
}
