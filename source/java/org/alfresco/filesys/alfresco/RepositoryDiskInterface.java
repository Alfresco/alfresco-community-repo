/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.filesys.alfresco;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.alfresco.filesys.repo.OpenFileMode;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extra methods for DiskInterface, primarily implemented to support CIFS shuffles.
 */
public interface RepositoryDiskInterface 
{
    /**
     * Copy the content from one node to another.
     * 
     * @param rootNode NodeRef
     * @param fromPath - the source node
     * @param toPath - the target node
     * @throws FileNotFoundException 
     */
    public void copyContent(NodeRef rootNode, String fromPath, String toPath) throws FileNotFoundException;

    
    /**
     * CreateFile.
     * 
     * @param rootNode NodeRef
     * @param Path - path
     * @param allocationSize size to allocate for new file
     * @param isHidden boolean
     * @throws FileNotFoundException 
     */
    public NetworkFile createFile(NodeRef rootNode, String Path, long allocationSize, boolean isHidden) throws IOException;

    /**
     * RestoreFile.
     * 
     * Either restores the file or creates a new one.
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param rootNode NodeRef
     * @param path - path
     * @param allocationSize size to allocate for new file
     * @param originalNodeRef NodeRef
     * @throws FileNotFoundException 
     */
    public NetworkFile restoreFile(SrvSession sess,
            TreeConnection tree, 
            NodeRef rootNode, 
            String path,
            long allocationSize, 
            NodeRef originalNodeRef) throws IOException;


    /**
     * 
     * @param session // temp until refactor
     * @param tree // temp until refactor
     * @param rootNode NodeRef
     * @param path String
     * @param mode OpenFileMode
     * @param truncate boolean
     * @return NetworkFile
     */
    public NetworkFile openFile(SrvSession session, TreeConnection tree, NodeRef rootNode, String path, OpenFileMode mode, boolean truncate) throws IOException;

    /**
     * CloseFile.
     * 
     * @param tree TreeConnection
     * @param rootNode NodeRef
     * @param Path - path
     * @param file - file
     * @throws FileNotFoundException
     * @return node ref of deleted file or null if no file deleted
     */
    public NodeRef closeFile(TreeConnection tree, NodeRef rootNode, String Path, NetworkFile file) throws IOException;
    
    
    /**
     * Delete file
     * @param session SrvSession
     * @param tree TreeConnection
     * @param rootNode NodeRef
     * @param path String
     * @return NodeRef of file deleted or null if no file deleted
     * @throws IOException
     */
    public NodeRef deleteFile2(final SrvSession session, final TreeConnection tree, NodeRef rootNode, String path) throws IOException;
    
    /**
     * 
     * @param session SrvSession
     * @param tree TreeConnection
     * @param file NetworkFile
     */
    public void reduceQuota(SrvSession session, TreeConnection tree, NetworkFile file);
    
    /**
     * 
     * @param rootNode NodeRef
     * @param path String
     */
    public void deleteEmptyFile(NodeRef rootNode, String path);
    
    /**
     * Rename the specified file.
     *
     * @param rootNode            root node
     * @param oldName     java.lang.String
     * @param newName     java.lang.String
     * @param soft boolean
     * @param moveAsSystem        move as system
     * @exception java.io.IOException The exception description.
     */
    public void renameFile(NodeRef rootNode, String oldName, String newName, boolean soft, boolean moveAsSystem)
      throws java.io.IOException;


}
