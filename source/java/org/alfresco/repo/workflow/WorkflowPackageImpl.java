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
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;


/**
 * Alfresco implementation of Workflow Package where the package is stored
 * within the Alfresco Repository.
 * 
 * @author davidc
 */
public class WorkflowPackageImpl implements WorkflowPackageComponent
{
    private final static String PACKAGE_FOLDER = "Workflow Packages";

    // service dependencies
    private ImporterBootstrap bootstrap;
    private SearchService searchService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private FileFolderService fileFolderService;
    private NodeRef systemWorkflowContainer = null;

    
    /**
     * @param bootstrap  the importer bootstrap for the store to place workflow items into
     */
    public void setImporterBootstrap(ImporterBootstrap bootstrap)
    {
        this.bootstrap = bootstrap;
    }

    /**
     * @param fileFolderService  file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
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
        if (container == null)
        {
            // create simple folder in workflow system folder
            NodeRef system = getSystemWorkflowContainer();
            
            // TODO: Consider structuring this folder, if number of children becomes an issue
            List<String> folders = new ArrayList<String>();
            folders.add(PACKAGE_FOLDER);
            folders.add(GUID.generate());
            FileInfo containerFolder = fileFolderService.makeFolders(system, folders, ContentModel.TYPE_FOLDER);
            container = containerFolder.getNodeRef();
        }
        
        // attach workflow package
        if (nodeService.hasAspect(container, WorkflowModel.ASPECT_WORKFLOW_PACKAGE))
        {
            throw new WorkflowException("Container '" + container + "' is already a workflow package.");
        }
        nodeService.addAspect(container, WorkflowModel.ASPECT_WORKFLOW_PACKAGE, null);
        
        // return container
        return container;
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
            ChildAssociationRef childRef = nodeService.createNode(systemContainer, ContentModel.ASSOC_CHILDREN, qname, ContentModel.TYPE_FOLDER);
            systemWorkflowContainer = childRef.getChildRef();
        }
        return systemWorkflowContainer;
    }
    
}
