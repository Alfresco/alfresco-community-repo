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
package org.alfresco.repo.workflow.jbpm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.QName;
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
        // Force assignment of task if transition is taken, but no owner has yet been assigned
        if (actorId == null)
        {
            actorId = AuthenticationUtil.getCurrentUserName();
        }
        
        // Set task properties on completion of task
        // NOTE: Set properties first, so they're available during the submission of
        //       task variables to the process context
        Map<QName, Serializable> taskProperties = new HashMap<QName, Serializable>();
        Transition outcome = (transition == null) ? token.getNode().getDefaultLeavingTransition() : transition; 
        if (outcome != null)
        {
            taskProperties.put(WorkflowModel.PROP_OUTCOME, outcome.getName());
        }
        taskProperties.put(WorkflowModel.PROP_STATUS, "Completed");
        getJBPMEngine().setTaskProperties(this, taskProperties);
        
        // perform transition
        super.end(transition);
        
        if (getTask().getStartState() != null)
        {
            // if ending a start task, push start task properties to process context, if not
            // already done
            getJBPMEngine().setDefaultWorkflowProperties(this);

            // set task description
            getJBPMEngine().setDefaultStartTaskDescription(this);
        }
    }

}
