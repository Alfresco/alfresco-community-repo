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
package org.alfresco.repo.workflow.jbpm;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.TaskInstanceFactory;
import org.jbpm.taskmgmt.exe.TaskInstance;


/**
 * jBPM factory for creating Alfresco derived Task Instances
 * 
 * @author davidc
 */
public class WorkflowTaskInstanceFactory implements TaskInstanceFactory
{
    private static final long serialVersionUID = -8097108150047415711L;


    /* (non-Javadoc)
     * @see org.jbpm.taskmgmt.TaskInstanceFactory#createTaskInstance(org.jbpm.graph.exe.ExecutionContext)
     */
    public TaskInstance createTaskInstance(ExecutionContext executionContext)
    {
        return new WorkflowTaskInstance();
    }
}
