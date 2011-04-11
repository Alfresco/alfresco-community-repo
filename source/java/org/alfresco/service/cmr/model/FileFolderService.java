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
package org.alfresco.service.cmr.model;

import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Provides methods specific to manipulating {@link org.alfresco.model.ContentModel#TYPE_CONTENT files}
 * and {@link org.alfresco.model.ContentModel#TYPE_FOLDER folders}.    
 * 
 * So this interface provides a simple way of accessing simple trees of files and folders in Alfresco.
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
    @Auditable(parameters = {"contextNodeRef"})
    public List<FileInfo> list(NodeRef contextNodeRef);
    
    /**
     * Lists all immediate child files of the given context node
     * 
     * @param folderNodeRef the folder to start searching in
     * @return Returns a list of matching files
     */
    @Auditable(parameters = {"folderNodeRef"})
    public List<FileInfo> listFiles(NodeRef folderNodeRef);
    
    /**
     * Lists all immediate child folders of the given context node
     * 
     * @param contextNodeRef the node to start searching in
     * @return Returns a list of matching folders
     */
    @Auditable(parameters = {"contextNodeRef"})
    public List<FileInfo> listFolders(NodeRef contextNodeRef);
       
    /**
     * Lists all folders below the given context node, both immediate and lower levels
     * 
     * The filter parameter allows subfolders to be excluded from the search. 
     * 
     * @param contextNodeRef the node to start searching in
     * @param filter - may be null in which case all sub-folders will be searched
     */
    @Auditable(parameters = {"contextNodeRef"})
    public List<FileInfo> listDeepFolders(NodeRef contextNodeRef, SubFolderFilter filter);
    
    /**
     * Uses the <b>cm:name</b> of the given node and attempts to find a sibling node
     * with a more specific localized name.  The node passed in must represent the base
     * of the possible translations i.e. the base name for the resource names will be
     * calculated using the filename without extension.  The locale used will come from
     * {@link I18NUtil#getLocale() the thread's default locale}.
     * 
     * @param nodeRef           the node that acts as the baseline for the search
     * @return                  Returns a sibling node or the original node
     */
    @Auditable(parameters = ("nodeRef"))
    public NodeRef getLocalizedSibling(NodeRef nodeRef);
    
    /**
     * Get a node ref of the node that has the name within the parent node
     * 
     * @param contextNodeRef the parent node
     * @param name the name of the node to search for
     * @return Returns the node that has the given name - or null if not found
     */
    @Auditable(parameters = {"contextNodeRef", "name"})
    public NodeRef searchSimple(NodeRef contextNodeRef, String name);
    
    /**
     * Searches for all files and folders with the matching name pattern,
     * using wildcard characters <b>*</b> and <b>?</b>.
     * 
     * Warning: Please avoid using this method with any "namePattern" other than "*".  
     * Although it works, its performance is poor, which is why this method is deprecated.
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
     * @deprecated for shallow search use list, listFolders, listFiles, searchSimple.
     * For deep listing use listDeepFolders. 
     * Avoid calling this method with any name pattern except for "*".    
     */
    @Auditable(parameters = {"contextNodeRef", "namePattern", "includeSubFolders"})
    public List<FileInfo> search(
            NodeRef contextNodeRef,
            String namePattern,
            boolean includeSubFolders);
    
    /**
     * Perform a search against the name of the files or folders within a hierarchy.
     * Wildcard characters are <b>*</b> and <b>?</b>. 
     * 
     * Warning: Please avoid using this method with any "namePattern" other than "*".  
     * Although it works, its performance is poor which is why this method is deprecated.
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
     * @deprecated for shallow search use list, listFolders, listFiles, searchSimple.
     * For deep listing use listDeepFolders.    
     * Avoid calling this method with any name pattern except for "*".  
     */
    @Auditable(parameters = {"contextNodeRef", "namePattern", "fileSearch", "folderSearch", "includeSubFolders"})
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
    @Auditable(parameters = {"fileFolderRef", "newName"})
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
    @Auditable(parameters = {"sourceNodeRef", "targetParentRef", "newName"})
    public FileInfo move(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName)
            throws FileExistsException, FileNotFoundException;
    
    /**
     * Move a file or folder to a new name and/or location.
     * <p>
     * If both the parent folder and name remain the same, then nothing is done.
     * <p/>
     * It is possible to specify <i>which</i> is the parent node when moving nodes; nodes
     * can reside in multiple locations.
     * 
     * @param sourceNodeRef the file or folder to move
     * @param sourceParentRef the source parent of node - <tt>null</tt> means move from primary parent
     * @param targetParentRef the new parent node to move the node to - null means rename in situ
     * @param newName the name to change the file or folder to - null to keep the existing name
     * @return Returns the new file info
     * @throws FileExistsException
     * @throws FileNotFoundException
     */
    @Auditable(parameters = { "sourceNodeRef", "sourceParentRef", "targetParentRef", "newName" })
    public FileInfo moveFrom(NodeRef sourceNodeRef, NodeRef sourceParentRef, NodeRef targetParentRef, String newName) throws FileExistsException, FileNotFoundException;

    /**
     * @deprecated  From 3.4.2, use {@link #moveFrom(NodeRef, NodeRef, NodeRef, String)} or
     *              {@link #move(NodeRef, NodeRef, String)}.  See
     *              <a href="https://issues.alfresco.com/jira/browse/ALF-7692">ALF-7692</a>
     */
    @Auditable(parameters = { "sourceNodeRef", "sourceParentRef", "targetParentRef", "newName" })
    public FileInfo move(NodeRef sourceNodeRef, NodeRef sourceParentRef, NodeRef targetParentRef, String newName) throws FileExistsException, FileNotFoundException;

    /**
     * Copy a source file or folder. The source can be optionally renamed and optionally moved into another folder.
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
    @Auditable(parameters = {"sourceNodeRef", "targetParentRef", "newName"})
    public FileInfo copy(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName)
            throws FileExistsException, FileNotFoundException;

    /**
     * Create a file or folder; or any valid node of type derived from file or folder.
     * <p>
     * The association QName for the patch defaults to <b>cm:filename</b> i.e. the
     * <b>Content Model</b> namespace with the filename as the local name.
     * 
     * @param parentNodeRef the parent node.  The parent must be a valid
     *      {@link org.alfresco.model.ContentModel#TYPE_FOLDER folder}.
     * @param name the name of the node
     * @param typeQName the type to create
     * @return Returns the new node's file information
     * @throws FileExistsException
     * 
     * @see {@link #create(NodeRef, String, QName, QName)}
     */
    @Auditable(parameters = {"parentNodeRef", "name", "typeQName"})
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName) throws FileExistsException;
    
    /**
     * Create a file or folder; or any valid node of type derived from file or folder
     * 
     * @param parentNodeRef the parent node.  The parent must be a valid
     *      {@link org.alfresco.model.ContentModel#TYPE_FOLDER folder}.
     * @param name the name of the node
     * @param typeQName the type to create
     * @param assocQName the association QName to set for the path (may be <tt>null</tt>).
     * @return Returns the new node's file information
     * @throws FileExistsException
     */
    @Auditable(parameters = {"parentNodeRef", "name", "typeQName"})
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName, QName assocQName) throws FileExistsException;
    
    /**
     * Delete a file or folder
     * 
     * @param nodeRef the node to delete
     */
    @Auditable(parameters = {"nodeRef"})
    public void delete(NodeRef nodeRef);
      
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
    @Auditable(parameters = {"rootNodeRef", "nodeRef"})
    public List<FileInfo> getNamePath(NodeRef rootNodeRef, NodeRef nodeRef) throws FileNotFoundException;
    
    /**
     * Resolve a file or folder name path from a given root node down to the final node.
     * 
     * @param rootNodeRef the start point node - a cm:folder type or subtype, e.g. the Company Home's nodeRef
     * @param pathElements a list of names in the path. Do not include the referenced rootNodeRef's path element.
     * @return Returns the info of the file or folder
     * @throws FileNotFoundException if no file or folder exists along the path
     */
    @Auditable(parameters = {"rootNodeRef", "pathElements"})
    public FileInfo resolveNamePath(NodeRef rootNodeRef, List<String> pathElements) throws FileNotFoundException;
    
    /**
     * Get the file info (name, folder, etc) for the given node
     * 
     * @param nodeRef the node to get info for
     * @return Returns the file info or null if the node does not represent a file or folder
     */
    @Auditable(parameters = {"nodeRef"})
    public FileInfo getFileInfo(NodeRef nodeRef);
    
    /**
     * Get the reader to the file represented by the node according to the File/Folder model.
     * (This is not the same as the method on the ContentService)
     * 
     * @param nodeRef the content node
     * @return Returns a handle to the content associated with the node
     */
    @Auditable(parameters = {"nodeRef"})
    public ContentReader getReader(NodeRef nodeRef);
    
    /**
     * Get the writer to the file represented by the node according to the File/Folder model.
     * (This is not the same as the method on the ContentService)
     * 
     * @param nodeRef the content node
     * @return Returns a handle to the content associated with the node
     */
    @Auditable(parameters = {"nodeRef"})
    public ContentWriter getWriter(NodeRef nodeRef);
    
    
    /**
     * Check the validity of a node reference
     * 
     * @return          returns <tt>true</tt> if the NodeRef is valid
     */
    @Auditable(parameters = {"nodeRef"})
    public boolean exists(NodeRef nodeRef);
    
    /**
     * Checks the type for whether it is a recognised file or folder type or is invalid for the FileFolderService.
     * 
     * @param typeQName the type to check
     * @return - the type
     */
    @Auditable(parameters = {"typeQName"})
    public FileFolderServiceType getType(QName typeQName);
}
