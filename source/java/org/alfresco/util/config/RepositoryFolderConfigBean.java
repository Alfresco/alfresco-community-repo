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
package org.alfresco.util.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.PropertyCheck;

/**
 * Composite property bean to identify a folder in the repository.  This uses the
 * {@link RepositoryPathConfigBean#getPath() path} to identify a root and then a
 * {@link #getFolderNames() folder-name path} to identify a folder.
 * 
 * @author Derek Hulley
 * @since 3.2 
 */
public class RepositoryFolderConfigBean extends RepositoryPathConfigBean
{
    private List<String> folderPath;
    
    public RepositoryFolderConfigBean()
    {
        folderPath = Collections.emptyList();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Folder Path: ").append(super.getStoreRef()).append(super.getRootPath());
        for (String folder : folderPath)
        {   
            sb.append("/").append(folder);
        }
        return sb.toString();
    }

    /**
     * Get the folder name path
     */
    public List<String> getFolderNames()
    {
        return folderPath;
    }
    
    /**
     * 
     * @return          Returns the string representation of the folder path
     */
    public String getFolderPath()
    {
        StringBuilder sb = new StringBuilder(56);
        for (String pathElement : folderPath)
        {
            sb.append("/").append(pathElement);
        }
        return sb.toString();
    }

    /**
     * Set the folder name path <b>relative to the {@link RepositoryPathConfigBean#getPath() path}</b>.
     * 
     * @param folderPath            a folder-name path using the '<b>/</b>' path separator e.g. '<b>IMAP HOME/MAIL-1</b>'
     */
    public void setFolderPath(String folderPath)
    {
        if (!PropertyCheck.isValidPropertyString(folderPath))
        {
            folderPath = "";
        }
        this.folderPath = new ArrayList<String>(5);
        StringTokenizer tokenizer = new StringTokenizer(folderPath, "/");
        while (tokenizer.hasMoreTokens())
        {
            String folderName = tokenizer.nextToken();
            if (folderName.length() == 0)
            {
                throw new IllegalArgumentException("Invalid folder name path for property 'folderPath': " + folderPath);
            }
            this.folderPath.add(folderName);
        }
    }
    
    /**
     * Helper method to find the folder path referenced by this bean.
     * The {@link #getPath() path} to the start of the {@link #getFolderNames() folder path}
     * must exist.
     * <p>
     * Authentication and transactions are the client's responsibility.
     * 
     * @return                      Returns an existing folder reference or null
     */
    public NodeRef getFolderPath(
            NamespaceService namespaceService,
            NodeService nodeService,
            SearchService searchService,
            FileFolderService fileFolderService)
    {
        NodeRef pathStartNodeRef = super.resolveNodePath(namespaceService, nodeService, searchService);
        if (pathStartNodeRef == null)
        {
            throw new AlfrescoRuntimeException(
                    "Folder path resolution requires an existing base path. \n" +
                    "   Base path: " + getRootPath());
        }
        // Just choose the root path if the folder path is empty
        if (folderPath.size() == 0)
        {
            return pathStartNodeRef;
        }
        else
        {
            try
            {
                FileInfo folderInfo = fileFolderService.resolveNamePath(pathStartNodeRef, folderPath);
                if (!folderInfo.isFolder())
                {
                    throw new AlfrescoRuntimeException("Not a folder: " + this);
                }
                return folderInfo.getNodeRef();
            }
            catch (FileNotFoundException e)
            {
                throw new AlfrescoRuntimeException("Folder not found: " + this);
            }
        }
        // Done
    }
    
    /**
     * Helper method to find or create the folder path referenced by this bean.
     * The {@link #getPath() path} to the start of the {@link #getFolderNames() folder path}
     * must exist.  The folder path will be created, if required.
     * <p>
     * Authentication and transactions are the client's responsibility.
     * 
     * @return                      Returns an existing or new folder reference
     */
    public NodeRef getOrCreateFolderPath(
            NamespaceService namespaceService,
            NodeService nodeService,
            SearchService searchService,
            FileFolderService fileFolderService)
    {
        NodeRef pathStartNodeRef = super.resolveNodePath(namespaceService, nodeService, searchService);
        if (pathStartNodeRef == null)
        {
            throw new AlfrescoRuntimeException(
                    "Folder path resolution requires an existing base path. \n" +
                    "   Base path: " + getRootPath());
        }
        // Just choose the root path if the folder path is empty
        if (folderPath.size() == 0)
        {
            return pathStartNodeRef;
        }
        else
        {
            FileInfo folderInfo = FileFolderServiceImpl.makeFolders(
                    fileFolderService,
                    pathStartNodeRef,
                    folderPath,
                    ContentModel.TYPE_FOLDER);
            return folderInfo.getNodeRef();
        }
        // Done
    }
}
