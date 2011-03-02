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
     * @param container
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
