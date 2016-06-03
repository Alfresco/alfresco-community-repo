package org.alfresco.repo.workflow;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;


/**
 * Contract for managing Workflow Packages.  A package is a container
 * of Content that's routed through a Workflow.
 * 
 * @author davidc
 */
public interface WorkflowPackageComponent
{

    /**
     * Create a Workflow Package (a container of content to route through the Workflow).
     * 
     * If an existing container is supplied, it's supplemented with the workflow package aspect.
     * 
     * @param  container  (optional) a pre-created container (e.g. folder, versioned folder or layered folder)
     * @return  the workflow package
     */
    public NodeRef createPackage(NodeRef container);
    
    /**
     * Deletes a Workflow Package
     * 
     * The workflow package aspect is removed, and if the container was previously created by the workflow
     * service (i.e. not provided from elsewhere), it will be deleted.
     * 
     * @param container NodeRef
     */
    public void deletePackage(NodeRef container);
    
    // TODO: Further support for finding packages via meta-data of WorkflowPackage aspect
    
    /**
     * Gets the Workflows that act upon the specified Repository content.
     *  
     * @param packageItem  the repository content item to get workflows for
     * @return  list of workflows which act upon the specified content
     */
    public List<String> getWorkflowIdsForContent(NodeRef packageItem);

    /**
     * Initialises the workflow package node on the {@link WorkflowInstance},
     * adding the appropriate aspect and setting the appropriate properties to
     * mark it as a package for the given {@link WorkflowInstance}.
     * 
     * @param instance
     *            the workflow instance to which the package belongs.
     * 
     * @return <code>true</code> if the package node was modified.
     */
    public boolean setWorkflowForPackage(WorkflowInstance instance);

}
