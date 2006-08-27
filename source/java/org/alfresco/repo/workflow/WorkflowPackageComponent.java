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
    
    // TODO: Support for finding packages via meta-data of WorkflowPackage aspect
    
}
