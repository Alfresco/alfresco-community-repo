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
package org.alfresco.repo.workflow;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.ParameterCheck;


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
    private NodeRef systemWorkflowContainer = null;

    
    /**
     * @param bootstrap  the importer bootstrap for the store to place workflow items into
     */
    public void setImporterBootstrap(ImporterBootstrap bootstrap)
    {
        this.bootstrap = bootstrap;
    }

    /**
     * @param searchService  search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param nodeService  node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowPackageComponent#createPackage(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef createPackage(NodeRef container)
    {
        // create a container, if one is not specified
        boolean isSystemPackage = false;
        if (container == null)
        {
            // create simple folder in workflow system folder
            NodeRef system = getSystemWorkflowContainer();
            
            // TODO: Consider structuring this folder, if number of children becomes an issue
            NodeRef packages = null;
            List<NodeRef> results = searchService.selectNodes(system, "./" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + PACKAGE_FOLDER, null, namespaceService, false);
            if (results.size() > 0)
            {
                packages = results.get(0);
            }
            else
            {
                QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, PACKAGE_FOLDER);
                ChildAssociationRef childRef = nodeService.createNode(system, ContentModel.ASSOC_CHILDREN, qname, ContentModel.TYPE_SYSTEM_FOLDER);
                packages = childRef.getChildRef();
            }
            
            String containerName = "pkg_" + GUID.generate();
            QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, containerName);
            ChildAssociationRef childRef = nodeService.createNode(packages, ContentModel.ASSOC_CONTAINS, qname, ContentModel.TYPE_SYSTEM_FOLDER);
            container = childRef.getChildRef();
            isSystemPackage = true;
        }
        
        // attach workflow package
        if (nodeService.hasAspect(container, WorkflowModel.ASPECT_WORKFLOW_PACKAGE))
        {
            throw new WorkflowException("Container '" + container + "' is already a workflow package.");
        }
        nodeService.addAspect(container, WorkflowModel.ASPECT_WORKFLOW_PACKAGE, null);
        nodeService.setProperty(container, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, isSystemPackage);
        
        // return container
        return container;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowPackageComponent#deletePackage(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void deletePackage(NodeRef container)
    {
        if (container != null && nodeService.exists(container) && nodeService.hasAspect(container, WorkflowModel.ASPECT_WORKFLOW_PACKAGE))
        {
            Boolean isSystemPackage = (Boolean)nodeService.getProperty(container, WorkflowModel.PROP_IS_SYSTEM_PACKAGE);
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
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowPackageComponent#getWorkflowIdsForContent(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public List<String> getWorkflowIdsForContent(NodeRef packageItem)
    {
        ParameterCheck.mandatory("packageItem", packageItem);
        List<String> workflowIds = new ArrayList<String>();
        if (nodeService.exists(packageItem))
        {
            List<ChildAssociationRef> packageItemParents = nodeService.getParentAssocs(packageItem);
            for (ChildAssociationRef packageItemParent : packageItemParents)
            {
                NodeRef parentRef = packageItemParent.getParentRef();
                if (nodeService.hasAspect(parentRef, WorkflowModel.ASPECT_WORKFLOW_PACKAGE))
                {
                    String workflowInstance = (String)nodeService.getProperty(parentRef, WorkflowModel.PROP_WORKFLOW_INSTANCE_ID);
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
     * @return  the system workflow container
     */
    private NodeRef getSystemWorkflowContainer()
    {
        if (systemWorkflowContainer == null)
        {
            NodeRef systemContainer = findSystemContainer();
            systemWorkflowContainer = findSystemWorkflowContainer(systemContainer);
            if (systemWorkflowContainer == null)
            {
                throw new WorkflowException("Unable to find system workflow folder - does not exist.");
            }
        }
        return systemWorkflowContainer;
    }
    
    /**
     * Finds the system workflow container
     *  
     * @param systemContainer  the system container
     * @return  the system workflow container
     */
    private NodeRef findSystemWorkflowContainer(NodeRef systemContainer)
    {
        String path = bootstrap.getConfiguration().getProperty("system.workflow_container.childname");
        if (path == null)
        {
            throw new WorkflowException("Unable to locate workflow system container - path not specified");
        }
        List<NodeRef> nodeRefs = searchService.selectNodes(systemContainer, path, null, namespaceService, false);
        if (nodeRefs != null && nodeRefs.size() > 0)
        {
            systemWorkflowContainer = nodeRefs.get(0);
        }
        return systemWorkflowContainer;
    }

    /**
     * Finds the system container
     * 
     * @return  the system container
     */
    private NodeRef findSystemContainer()
    {
        String path = bootstrap.getConfiguration().getProperty("system.system_container.childname");
        if (path == null)
        {
            throw new WorkflowException("Unable to locate system container - path not specified");
        }
        NodeRef root = nodeService.getRootNode(bootstrap.getStoreRef());
        List<NodeRef> nodeRefs = searchService.selectNodes(root, path, null, namespaceService, false);
        if (nodeRefs == null || nodeRefs.size() == 0)
        {
            throw new WorkflowException("Unable to locate system container - path not found");
        }
        return nodeRefs.get(0);
    }

    /**
     * Creates the System Workflow Container
     * 
     * @return  the system workflow container
     */
    public NodeRef createSystemWorkflowContainer()
    {
        NodeRef systemContainer = findSystemContainer();
        NodeRef systemWorkflowContainer = findSystemWorkflowContainer(systemContainer);
        if (systemWorkflowContainer == null)
        {
            String name = bootstrap.getConfiguration().getProperty("system.workflow_container.childname");
            QName qname = QName.createQName(name, namespaceService);
            ChildAssociationRef childRef = nodeService.createNode(systemContainer, ContentModel.ASSOC_CHILDREN, qname, ContentModel.TYPE_CONTAINER);
            systemWorkflowContainer = childRef.getChildRef();
        }
        return systemWorkflowContainer;
    }

}
