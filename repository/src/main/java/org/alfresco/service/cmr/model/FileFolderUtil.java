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
package org.alfresco.service.cmr.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Higher level utility methods to be used in conjunction with the FileFolderService.
 * 
 * @author mrogers
 */
public class FileFolderUtil
{
    /**
     * Checks for the presence of, and creates as necessary, the folder structure in the provided path.
     * <p>
     * An empty path list is not allowed as it would be impossible to necessarily return file info for the parent node - it might not be a folder node.
     * 
     * @param parentNodeRef
     *            the node under which the path will be created
     * @param pathElements
     *            the folder name path to create - may not be empty
     * @param folderTypeQName
     *            the types of nodes to create. This must be a valid subtype of {@link org.alfresco.model.ContentModel#TYPE_FOLDER they folder type}.
     * @return Returns the info of the last folder in the path.
     */
    public static FileInfo makeFolders(FileFolderService service,
            NodeRef parentNodeRef, List<String> pathElements,
            QName folderTypeQName)
    {
        return makeFolders(service, parentNodeRef, pathElements, folderTypeQName, null, null);
    }

    /**
     * Same as above, with option to disable parent behaviour(s) when creating sub-folder
     * 
     * @param service
     *            FileFolderService
     * @param parentNodeRef
     *            NodeRef
     * @param folderTypeQName
     *            QName
     * @param behaviourFilter
     *            BehaviourFilter
     * @return FileInfo
     */
    public static FileInfo makeFolders(FileFolderService service, NodeRef parentNodeRef, List<String> pathElements,
            QName folderTypeQName, BehaviourFilter behaviourFilter, Set<QName> parentBehavioursToDisable)
    {
        validate(pathElements, service, folderTypeQName);

        List<PathElementDetails> list = new ArrayList<>(pathElements.size());
        for (String pathElement : pathElements)
        {
            list.add(new PathElementDetails(pathElement, null));
        }

        FileInfo fileInfo = makeFolders(service, null, parentNodeRef, list, folderTypeQName, behaviourFilter, parentBehavioursToDisable);

        // Should we check the type?
        return fileInfo;
    }

    /**
     * Checks for the presence of, and creates as necessary, the folder structure in the provided paths with the following options:
     * <ul>
     * <li>Option to disable parent behaviour(s) when creating sub-folder.</li>
     * <li>Each folder has the option to have its own set of aspects</li>
     * </ul>
     * 
     * @param service
     *            the FileFolderService object
     * @param nodeService
     *            the NodeService object
     * @param parentNodeRef
     *            the node under which the path will be created
     * @param pathElementDetails
     *            the list of folder hierarchy where each folder can have its own set of aspects - may not be empty
     * @param folderTypeQName
     *            the types of nodes to create. This must be a valid subtype of {@link org.alfresco.model.ContentModel#TYPE_FOLDER they folder type}
     * @param behaviourFilter
     *            the BehaviourFilter object
     * @param parentBehavioursToDisable
     *            the set of behaviours that must be disabled
     * @return Returns the {@code FileInfo} of the last folder in the path.
     */
    public static FileInfo makeFolders(FileFolderService service, NodeService nodeService, NodeRef parentNodeRef,
            List<PathElementDetails> pathElementDetails, QName folderTypeQName, BehaviourFilter behaviourFilter,
            Set<QName> parentBehavioursToDisable)
    {
        return makeFolders(service, nodeService, parentNodeRef, pathElementDetails, folderTypeQName, behaviourFilter, parentBehavioursToDisable, null);
    }

    /**
     * Checks for the presence of, and creates as necessary, the folder structure in the provided paths with the following options:
     * <ul>
     * <li>Option to disable parent behaviour(s) when creating sub-folder.</li>
     * <li>Each folder has the option to have its own set of aspects</li>
     * </ul>
     *
     * @param service
     *            the FileFolderService object
     * @param nodeService
     *            the NodeService object
     * @param parentNodeRef
     *            the node under which the path will be created
     * @param pathElementDetails
     *            the list of folder hierarchy where each folder can have its own set of aspects - may not be empty
     * @param folderTypeQName
     *            the types of nodes to create. This must be a valid subtype of {@link org.alfresco.model.ContentModel#TYPE_FOLDER they folder type}
     * @param behaviourFilter
     *            the BehaviourFilter object
     * @param parentBehavioursToDisable
     *            the set of behaviours that must be disabled
     * @param allFoldersRefsInThePath
     *            (Optional) if an instance of a Set is provided, then it'd be populated with nodeRefs of all the folders that have been specified in the path elements details.({@code pathElementDetails}).
     * @return Returns the {@code FileInfo} of the last folder in the path.
     */
    public static FileInfo makeFolders(FileFolderService service, NodeService nodeService, NodeRef parentNodeRef,
            List<PathElementDetails> pathElementDetails, QName folderTypeQName, BehaviourFilter behaviourFilter,
            Set<QName> parentBehavioursToDisable, Set<NodeRef> allFoldersRefsInThePath)
    {
        validate(pathElementDetails, service, folderTypeQName);

        NodeRef currentParentRef = parentNodeRef;
        // just loop and create if necessary
        for (PathElementDetails pathElement : pathElementDetails)
        {
            // does it exist?
            // Navigation should not check permissions
            NodeRef nodeRef = AuthenticationUtil.runAs(
                    new SearchAsSystem(service, currentParentRef, pathElement.getFolderName()),
                    AuthenticationUtil.getSystemUserName());

            if (nodeRef == null)
            {
                if ((behaviourFilter != null) && (parentBehavioursToDisable != null))
                {
                    for (QName parentBehaviourToDisable : parentBehavioursToDisable)
                    {
                        behaviourFilter.disableBehaviour(currentParentRef, parentBehaviourToDisable);
                    }
                }

                try
                {
                    // not present - make it
                    // If this uses the public service it will check create
                    // permissions
                    FileInfo createdFileInfo = service.create(currentParentRef, pathElement.getFolderName(), folderTypeQName);
                    currentParentRef = createdFileInfo.getNodeRef();

                    Map<QName, Map<QName, Serializable>> requireddAspects = pathElement.getAspects();
                    if (requireddAspects.size() > 0 && nodeService != null)
                    {
                        for (QName aspect : requireddAspects.keySet())
                        {
                            nodeService.addAspect(currentParentRef, aspect, requireddAspects.get(aspect));
                        }
                    }
                }
                finally
                {
                    if ((behaviourFilter != null) && (parentBehavioursToDisable != null))
                    {
                        for (QName parentBehaviourToDisable : parentBehavioursToDisable)
                        {
                            behaviourFilter.enableBehaviour(currentParentRef, parentBehaviourToDisable);
                        }
                    }
                }
            }
            else
            {
                // it exists
                currentParentRef = nodeRef;
            }
            if (allFoldersRefsInThePath != null)
            {
                allFoldersRefsInThePath.add(currentParentRef);
            }
        }
        // done
        // Used to call toFileInfo((currentParentRef, true);
        // If this uses the public service this will check the final read access
        FileInfo fileInfo = service.getFileInfo(currentParentRef);

        // Should we check the type?
        return fileInfo;
    }

    private static <T> void validate(List<T> pathElements, FileFolderService service, QName folderTypeQName)
    {
        if (pathElements.size() == 0)
        {
            throw new IllegalArgumentException("Path element list is empty");
        }

        // make sure that the folder is correct
        boolean isFolder = service.getType(folderTypeQName) == FileFolderServiceType.FOLDER;
        if (!isFolder)
        {
            throw new IllegalArgumentException("Type is invalid to make folders with: " + folderTypeQName);
        }
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

    /**
     * A simple POJO to hold information about the folder which will be created.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static class PathElementDetails
    {
        private final String folderName;
        private final Map<QName, Map<QName, Serializable>> aspects;

        public PathElementDetails(String folderName, Map<QName, Map<QName, Serializable>> aspects)
        {
            this.folderName = folderName;
            if (aspects == null)
            {
                this.aspects = Collections.emptyMap();
            }
            else
            {
                this.aspects = Collections.unmodifiableMap(aspects);
            }
        }

        /**
         * @return the folderName
         */
        public String getFolderName()
        {
            return this.folderName;
        }

        /**
         * @return the aspects
         */
        public Map<QName, Map<QName, Serializable>> getAspects()
        {
            return this.aspects;
        }
    }
}
