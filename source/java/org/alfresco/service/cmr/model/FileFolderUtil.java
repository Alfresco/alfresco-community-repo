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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Higher level utility methods to be used in conjunction with the FileFolderService.
 * 
 * @author mrogers
 */
public class FileFolderUtil
{
        
    /**
     * Checks for the presence of, and creates as necessary, the folder
     * structure in the provided path.
     * <p>
     * An empty path list is not allowed as it would be impossible to
     * necessarily return file info for the parent node - it might not be a
     * folder node.
     * 
     * @param parentNodeRef
     *            the node under which the path will be created
     * @param pathElements
     *            the folder name path to create - may not be empty
     * @param folderTypeQName
     *            the types of nodes to create. This must be a valid subtype of
     *            {@link org.alfresco.model.ContentModel#TYPE_FOLDER they folder
     *            type}.
     * @return Returns the info of the last folder in the path.
     */
    public static FileInfo makeFolders(FileFolderService service,
            NodeRef parentNodeRef, List<String> pathElements,
            QName folderTypeQName)
    {
        if (pathElements.size() == 0)
        {
            throw new IllegalArgumentException("Path element list is empty");
        }

        // make sure that the folder is correct
        boolean isFolder = service.getType(folderTypeQName) == FileFolderServiceType.FOLDER;
        if (!isFolder)
        {
            throw new IllegalArgumentException(
                    "Type is invalid to make folders with: " + folderTypeQName);
        }

        NodeRef currentParentRef = parentNodeRef;
        // just loop and create if necessary
        for (String pathElement : pathElements)
        {
            // does it exist?
            // Navigation should not check permissions
            NodeRef nodeRef = AuthenticationUtil.runAs(new SearchAsSystem(
                    service, currentParentRef, pathElement), AuthenticationUtil
                    .getSystemUserName());

            if (nodeRef == null)
            {
                // not present - make it
                // If this uses the public service it will check create
                // permissions
                FileInfo createdFileInfo = service.create(currentParentRef,
                        pathElement, folderTypeQName);
                currentParentRef = createdFileInfo.getNodeRef();
            }
            else
            {
                // it exists
                currentParentRef = nodeRef;
            }
        }
        // done
        // Used to call toFileInfo((currentParentRef, true);
        // If this uses the public service this will check the final read access
        FileInfo fileInfo = service.getFileInfo(currentParentRef);

        // Should we check the type?
        return fileInfo;
    }

    private static class SearchAsSystem implements RunAsWork<NodeRef>
    {
        FileFolderService service;
        NodeRef node;
        String name;

        SearchAsSystem(FileFolderService service, NodeRef node, String name)
        {
            this.service = service;
            this.node = node;
            this.name = name;
        }

        public NodeRef doWork() throws Exception
        {
            return service.searchSimple(node, name);
        }

    }

}
