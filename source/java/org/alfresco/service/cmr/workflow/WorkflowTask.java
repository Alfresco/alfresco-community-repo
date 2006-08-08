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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.namespace.QName;


/**
 * Workflow Task Data Object
 * 
 * Represents a human-oriented task within an "in-fligth" workflow instance
 * 
 * @author davidc
 */
public class WorkflowTask
{
    /** Unique id of Task */
    public String id;
    
    /** Name of Task */
    public String name;
    
    /** Task State */
    public WorkflowTaskState state;
    
    /** Workflow path this Task is associated with */
    public WorkflowPath path;
    
    /** Task Definition */
    public WorkflowTaskDefinition definition;
    
    /** Task Properties as described by Task Definition */
    public Map<QName, Serializable> properties;
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String propCount = (properties == null) ? "null" : "" + properties.size();
        return "WorkflowTask[id=" + id + ",name=" + name + ",state=" + state + ",props=" + propCount + ",def=" + definition + ",path=" + path.toString() + "]";
    }

}
