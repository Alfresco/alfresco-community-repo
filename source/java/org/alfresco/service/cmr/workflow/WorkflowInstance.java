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
package org.alfresco.service.cmr.workflow;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Workflow Instance Data Object
 * 
 * Represents an "in-flight" workflow.
 * 
 * @author davidc
 */
public class WorkflowInstance
{
    /** Workflow Instance unique id */
    public String id;

    /** Workflow Instance description */
    public String description;

    /** Is this Workflow instance still "in-flight" or has it completed? */
    public boolean active;

    /** Initiator (cm:person) - null if System initiated */
    public NodeRef initiator;
    
    /** Workflow Start Date */
    public Date startDate;
    
    /** Workflow End Date */
    public Date endDate;

    /** Workflow Package */
    public NodeRef workflowPackage;
    
    /** Workflow Context */
    public NodeRef context;
    
    /** Workflow Definition */
    public WorkflowDefinition definition;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "WorkflowInstance[id=" + id + ",active=" + active + ",def=" + definition.toString() + "]";
    }
}
