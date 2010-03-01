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

package org.alfresco.repo.workflow;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Alfresco implementation of Workflow Package where the package is stored
 * within the Alfresco Repository.
 * 
 * @author davidc
 */
public class WorkflowPackageImpl implements WorkflowPackageComponent
{
    private final static String PACKAGE_FOLDER = "packages";

    // service dependencies
    private ImporterBootstrap bootstrap;
    private SearchService searchService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private PermissionService permissionService;
    private NodeRef systemWorkflowContainer = null;
    private TenantService tenantService;

    /**
     * @param bootstrap the importer bootstrap for the store to place workflow
     *            items into
     */
    public void setImporterBootstrap(ImporterBootstrap bootstrap)
    {
        this.bootstrap = bootstrap;
    }

    /**
     * @param searchService search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param namespaceService namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param tenantService tenant service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.workflow.WorkflowPackageComponent#createPackage(org
     * .alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef createPackage(NodeRef container)
    {
        // create a container, if one is not specified
        boolean isSystemPackage = false;
        if (container == null)
        {
            container = makePackageContainer();
            isSystemPackage = true;
        }

        // attach workflow package
        if (nodeService.hasAspect(container, WorkflowModel.ASPECT_WORKFLOW_PACKAGE)) { throw new WorkflowException(
                    "Container '" + container + "' is already a workflow package."); }
        nodeService.addAspect(container, WorkflowModel.ASPECT_WORKFLOW_PACKAGE, null);
        nodeService.setProperty(container, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, isSystemPackage);

        // return container
        return container;
    }

    private NodeRef makePackageContainer()
    {
        NodeRef packages = findOrCreatePackagesFolder();
        String packageId = "pkg_" + GUID.generate();
        QName packageName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, packageId);
        ChildAssociationRef packageAssoc = nodeService.createNode(packages, ContentModel.ASSOC_CONTAINS, packageName,
                    WorkflowModel.TYPE_PACKAGE);
        NodeRef packageContainer = packageAssoc.getChildRef();
        // TODO: For now, grant full access to everyone
        permissionService.setPermission(packageContainer, PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true);
        return packageContainer;
    }

    /**
     * Finds the system folder in which all packages are stored. If this folder
     * has not been created yet then this method creates a new packages folder.
     * 
     * @return The system folder containing all workflow packages.
     */
    private NodeRef findOrCreatePackagesFolder()
    {
        // create simple folder in workflow system folder
        NodeRef system = getSystemWorkflowContainer();

        // TODO: Consider structuring this folder, if number of children becomes
        // an issue
        List<NodeRef> packageFolders = searchService.selectNodes(system, "./" + NamespaceService.CONTENT_MODEL_PREFIX
                    + ":" + PACKAGE_FOLDER, null, namespaceService, false);
        if (packageFolders.size() > 0)
        {
            return packageFolders.get(0); // Return folder if exists.
        }
        else
        // Create new package folder
        {
            QName packageFolderName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, PACKAGE_FOLDER);
            ChildAssociationRef packageFolderAssoc = nodeService.createNode(system, ContentModel.ASSOC_CHILDREN,
                        packageFolderName, ContentModel.TYPE_SYSTEM_FOLDER);
            return packageFolderAssoc.getChildRef();
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.workflow.WorkflowPackageComponent#deletePackage(org
     * .alfresco.service.cmr.repository.NodeRef)
     */
    public void deletePackage(NodeRef container)
    {
        if (container != null && nodeService.exists(container)
                    && nodeService.hasAspect(container, WorkflowModel.ASPECT_WORKFLOW_PACKAGE))
        {
            Boolean isSystemPackage = (Boolean) nodeService
                        .getProperty(container, WorkflowModel.PROP_IS_SYSTEM_PACKAGE);
            if (isSystemPackage != null && isSystemPackage.booleanValue())
            {
                nodeService.deleteNode(container);
            }
            else
            {
                nodeService.removeAspect(container, WorkflowModel.ASPECT_WORKFLOW_PACKAGE);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.workflow.WorkflowPackageComponent#getWorkflowIdsForContent
     * (org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public List<String> getWorkflowIdsForContent(NodeRef packageItem)
    {
        ParameterCheck.mandatory("packageItem", packageItem);
        List<String> workflowIds = new ArrayList<String>();
        if (nodeService.exists(packageItem))
        {
            List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(packageItem);
            for (ChildAssociationRef parentAssoc : parentAssocs)
            {
                NodeRef parentRef = parentAssoc.getParentRef();
                if (nodeService.hasAspect(parentRef, WorkflowModel.ASPECT_WORKFLOW_PACKAGE)
                            && !nodeService.hasAspect(parentRef, ContentModel.ASPECT_ARCHIVED))
                {
                    String workflowInstance = (String) nodeService.getProperty(parentRef,
                                WorkflowModel.PROP_WORKFLOW_INSTANCE_ID);
                    if (workflowInstance != null && workflowInstance.length() > 0)
                    {
                        workflowIds.add(workflowInstance);
                    }
                }
            }
        }
        return workflowIds;
    }

    /**
     * Gets the system workflow container for storing workflow related items
     * 
     * @return the system workflow container
     */
    private NodeRef getSystemWorkflowContainer()
    {
        if (tenantService.isEnabled())
        {
            NodeRef systemContainer = findSystemContainer();
            NodeRef tenantSystemWorkflowContainer = findSystemWorkflowContainer(systemContainer);
            if (tenantSystemWorkflowContainer == null) { throw new WorkflowException(
                        "Unable to find system workflow folder - does not exist."); }

            return tenantSystemWorkflowContainer;
        }
        else
        {
            if (systemWorkflowContainer == null)
            {
                NodeRef systemContainer = findSystemContainer();
                systemWorkflowContainer = findSystemWorkflowContainer(systemContainer);
                if (systemWorkflowContainer == null) { throw new WorkflowException(
                            "Unable to find system workflow folder - does not exist."); }
            }
            return systemWorkflowContainer;
        }

    }

    /**
     * Finds the system workflow container
     * 
     * @param systemContainer the system container
     * @return the system workflow container
     */
    private NodeRef findSystemWorkflowContainer(NodeRef systemContainer)
    {
        String path = bootstrap.getConfiguration().getProperty("system.workflow_container.childname");
        if (path == null) { throw new WorkflowException(
                    "Unable to locate workflow system container - path not specified"); }
        List<NodeRef> nodeRefs = searchService.selectNodes(systemContainer, path, null, namespaceService, false);

        if (tenantService.isEnabled())
        {
            NodeRef tenantSystemWorkflowContainer = null;
            if (nodeRefs != null && nodeRefs.size() > 0)
            {
                tenantSystemWorkflowContainer = nodeRefs.get(0);
            }
            return tenantSystemWorkflowContainer;
        }
        else
        {
            if (nodeRefs != null && nodeRefs.size() > 0)
            {
                systemWorkflowContainer = nodeRefs.get(0);
            }
            return systemWorkflowContainer;
        }
    }

    /**
     * Finds the system container
     * 
     * @return the system container
     */
    private NodeRef findSystemContainer()
    {
        String path = bootstrap.getConfiguration().getProperty("system.system_container.childname");
        if (path == null) { throw new WorkflowException("Unable to locate system container - path not specified"); }
        NodeRef root = nodeService.getRootNode(bootstrap.getStoreRef());
        List<NodeRef> nodeRefs = searchService.selectNodes(root, path, null, namespaceService, false);
        if (nodeRefs == null || nodeRefs.size() == 0) { throw new WorkflowException(
                    "Unable to locate system container - path not found"); }
        return nodeRefs.get(0);
    }

    /**
     * Creates the System Workflow Container
     * 
     * @return the system workflow container
     */
    public NodeRef createSystemWorkflowContainer()
    {
        NodeRef systemContainer = findSystemContainer();
        NodeRef systemWorkflowContainer = findSystemWorkflowContainer(systemContainer);
        if (systemWorkflowContainer == null)
        {
            String name = bootstrap.getConfiguration().getProperty("system.workflow_container.childname");
            QName qname = QName.createQName(name, namespaceService);
            ChildAssociationRef childRef = nodeService.createNode(systemContainer, ContentModel.ASSOC_CHILDREN, qname,
                        ContentModel.TYPE_CONTAINER);
            systemWorkflowContainer = childRef.getChildRef();
        }
        return systemWorkflowContainer;
    }

}
