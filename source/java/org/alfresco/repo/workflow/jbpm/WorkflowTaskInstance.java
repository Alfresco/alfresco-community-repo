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

import org.alfresco.service.cmr.workflow.WorkflowException;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;


/**
 * Alfresco specific implementation of a jBPM task instance
 * 
 * @author davidc
 */
public class WorkflowTaskInstance extends TaskInstance
{
    private static final long serialVersionUID = 6824116036569411964L;

    /** Alfresco JBPM Engine */ 
    private static JBPMEngine jbpmEngine = null;

    /**
     * Gets the JBPM Engine instance
     * 
     * @return  JBPM Engine
     */
    private JBPMEngine getJBPMEngine()
    {
        if (jbpmEngine == null)
        {
            BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
            BeanFactoryReference factory = factoryLocator.useBeanFactory(null);
            jbpmEngine = (JBPMEngine)factory.getFactory().getBean("jbpm_engine");
            if (jbpmEngine == null)
            {
                throw new WorkflowException("Failed to retrieve JBPMEngine component");
            }
        }
        return jbpmEngine;
    }
    
    /**
     * Construct
     */
    public WorkflowTaskInstance()
    {
        super();
    }

    /**
     * Construct
     * 
     * @param taskName
     * @param actorId
     */
    public WorkflowTaskInstance(String taskName, String actorId)
    {
        super(taskName, actorId);
    }

    /**
     * Construct
     * 
     * @param taskName
     */
    public WorkflowTaskInstance(String taskName)
    {
        super(taskName);
    }

    @Override
    public void create(ExecutionContext executionContext)
    {
        super.create(executionContext);
        getJBPMEngine().setDefaultTaskProperties(this);
    }

    @Override
    public void end(Transition transition)
    {
        // NOTE: Set the outcome first, so it's available during the submission of
        //       task variables to the process context
        Transition outcome = (transition == null) ? token.getNode().getDefaultLeavingTransition() : transition; 
        if (outcome != null)
        {
            getJBPMEngine().setTaskOutcome(this, outcome);
        }
        super.end(transition);
    }

}
