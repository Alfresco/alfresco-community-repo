/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;


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
    
}
