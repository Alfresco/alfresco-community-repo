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


/**
 * Workflow Path Data Object
 * 
 * Represents a path within an "in-flight" workflow instance.
 * 
 * Simple workflows consists of a single "root" path.  Multiple paths occur when a workflow
 * instance branches, therefore more than one concurrent path is taken.
 * 
 * @author davidc
 */
public class WorkflowPath
{
    /** Unique id of Workflow Path */
    public String id;
    
    /** Workflow Instance this path is part of */
    public WorkflowInstance instance;
    
    /** The Workflow Node the path is at */
    public WorkflowNode node;
    
    /** Is the path still active? */
    public boolean active;

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "WorkflowPath[id=" + id + ",instance=" + instance.toString() + ",active=" + active + ",node=" + node.toString()+ "]";
    }
}
