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

import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
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
    private String folderPath;
    
    public RepositoryFolderConfigBean()
    {
        folderPath = "";
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Folder Path: ").append(super.getStoreRef()).append(super.getRootPath()).append("/").append(folderPath);
        return sb.toString();
    }

    /**
     * 
     * @return          Returns the string representation of the folder path
     */
    public String getFolderPath()
    {
        return folderPath;
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
            throw new IllegalArgumentException("Invalid folder name path for property 'folderPath': " + folderPath);
        }
        StringTokenizer tokenizer = new StringTokenizer(folderPath, "/");
        StringBuilder pathBuff = new StringBuilder(folderPath.length());
        while (tokenizer.hasMoreTokens())
        {
            String folderName = tokenizer.nextToken();
            if (folderName.length() == 0)
            {
                throw new IllegalArgumentException("Invalid folder name path for property 'folderPath': " + folderPath);
            }
            pathBuff.append(folderName);
            if (tokenizer.hasMoreTokens())
            {
                pathBuff.append('/');
            }
        }
        this.folderPath = pathBuff.toString();
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
        if (folderPath.length() == 0)
        {
            return pathStartNodeRef;
        }
        else
        {
            List<NodeRef> nodeRefs = searchService.selectNodes(pathStartNodeRef, folderPath, null, namespaceService, true);
            if (nodeRefs.size() == 0)
            {
                throw new AlfrescoRuntimeException("Folder not found: " + this);
            }
            else
            {
                NodeRef nodeRef = nodeRefs.get(0);
                FileInfo folderInfo = fileFolderService.getFileInfo(nodeRef);
                if (!folderInfo.isFolder())
                {
                    throw new AlfrescoRuntimeException("Not a folder: " + this);
                }
                return nodeRef;
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
        if (folderPath.length() == 0)
        {
            return pathStartNodeRef;
        }
        else
        {
            StringTokenizer folders = new StringTokenizer(folderPath, "/");
            NodeRef nodeRef = pathStartNodeRef;
            while (folders.hasMoreTokens())
            {
                QName folderQName = QName.createQName(folders.nextToken(), namespaceService);
                List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef, RegexQNamePattern.MATCH_ALL, folderQName); 
                if (children.isEmpty())
                {
                    nodeRef = fileFolderService.create(nodeRef, folderQName.getLocalName(), ContentModel.TYPE_FOLDER, folderQName).getNodeRef();
                }
                else
                {
                    nodeRef = children.get(0).getChildRef();
                }
            }
            return nodeRef;
        }
        // Done
    }
}
