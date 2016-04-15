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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.activiti.engine.task.Task;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.workflow.WorkflowPropertyHandler;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActivitiPriorityPropertyHandlerTest
{
    private static ActivitiPriorityPropertyHandler handler;
    private @Mock
    Task task;
    private TypeDefinition type = null;
    private QName key = null;

    @BeforeClass
    public static void setUp()
    {
        handler = new ActivitiPriorityPropertyHandler();
        MessageService messageService = mock(MessageService.class);
        handler.setMessageService(messageService);
    }

    @Test
    public void handleTaskPropertySetsValidIntPriority()
    {
        for (int priority = 1; priority <= 3; priority++)
        {
            Object result = handler.handleTaskProperty(task, type, key, priority);
            assertEquals(WorkflowPropertyHandler.DO_NOT_ADD, result);
            verify(task).setPriority(priority);
        }
    }

    @Test
    public void handleTaskPropertySetsValidStringPriority()
    {
        for (int priority = 1; priority <= 3; priority++)
        {
            Object result = handler.handleTaskProperty(task, type, key, "" + priority);
            assertEquals(WorkflowPropertyHandler.DO_NOT_ADD, result);
            verify(task).setPriority(priority);
        }
    }

    @Test(expected = org.alfresco.service.cmr.workflow.WorkflowException.class)
    public void handleTaskPropertyDoesNotSetInvalidStringPriority()
    {
        String priority = "Not an integer";
        handler.handleTaskProperty(task, type, key, priority);
        fail("The method should throw an exception and not reach here.");
    }

    @Test(expected = org.alfresco.service.cmr.workflow.WorkflowException.class)
    public void handleTaskPropertyDoesNotSetInvalidClassPriority()
    {
        Long priority = 2l;
        handler.handleTaskProperty(task, type, key, priority);
        fail("The method should throw an exception and not reach here.");
    }
}
