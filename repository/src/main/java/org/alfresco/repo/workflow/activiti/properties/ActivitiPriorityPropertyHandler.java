/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.workflow.activiti.properties;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.task.Task;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiTaskPropertyHandler;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiPriorityPropertyHandler extends ActivitiTaskPropertyHandler
{
    /**
     * {@inheritDoc}
     */

    @Override
    protected Object handleTaskProperty(Task task, TypeDefinition type, QName key, Serializable value)
    {
        int priority;
        // ACE-3121: Workflow Admin Console: Cannot change priority for activiti
        // It could be a String that converts to an int, like when coming from WorkflowInterpreter.java
        if (value instanceof String)
        {
            try
            {
                priority = Integer.parseInt((String) value);
            }
            catch (NumberFormatException e)
            {
                throw getInvalidPropertyValueException(key, value);
            }
        }
        else
        {
            checkType(key, value, Integer.class);
            priority = (Integer) value;
        }

        // Priority value validation not performed to allow for future model changes
        task.setPriority(priority);

        return DO_NOT_ADD;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected Object handleDelegateTaskProperty(DelegateTask task, TypeDefinition type, QName key, Serializable value)
    {
        checkType(key, value, Integer.class);
        task.setPriority((Integer) value);
        return DO_NOT_ADD;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected QName getKey()
    {
        return WorkflowModel.PROP_PRIORITY;
    }
}
