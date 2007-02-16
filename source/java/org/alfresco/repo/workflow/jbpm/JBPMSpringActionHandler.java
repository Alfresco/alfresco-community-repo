/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;


/**
 * Abstract base implementation of a Jbpm Action Hander with access to
 * Alfresco Spring beans.
 * 
 * @author davidc
 */
public abstract class JBPMSpringActionHandler implements ActionHandler
{

    /**
     * Construct
     */
    protected JBPMSpringActionHandler()
    {
        // The following implementation is derived from Spring Modules v0.4
        BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
        BeanFactoryReference factory = factoryLocator.useBeanFactory(null);
        initialiseHandler(factory.getFactory());
    }
    
    /**
     * Initialise Action Handler
     * 
     * @param factory  Spring bean factory for accessing Alfresco beans
     */
    protected abstract void initialiseHandler(BeanFactory factory);

    
    /**
     * Gets the workflow instance id of the currently executing workflow
     * 
     * @param context  jBPM execution context
     * @return  workflow instance id
     */
    protected String getWorkflowInstanceId(ExecutionContext context)
    {
        String id = new Long(context.getProcessInstance().getId()).toString();
        return BPMEngineRegistry.createGlobalId("jbpm", id);
    }
    
}
