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
package org.alfresco.repo.workflow.jbpm;

import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;


/**
 * Abstract base implementation of a Jbpm Assignment Handler with access to
 * Alfresco Spring beans.
 * 
 * @author davidc
 */
public abstract class JBPMSpringAssignmentHandler implements AssignmentHandler
{
    private static final long serialVersionUID = -2233750219905283562L;

    /**
     * Construct
     */
    protected JBPMSpringAssignmentHandler()
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

    
}
