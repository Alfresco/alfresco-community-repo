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
    public void handleTaskPropertyDoesNotSetInvalidIntPriority0()
    {
        int priority = 0;
        handler.handleTaskProperty(task, type, key, priority);
        fail("The method should throw an exception and not reach here.");
    }

    @Test(expected = org.alfresco.service.cmr.workflow.WorkflowException.class)
    public void handleTaskPropertyDoesNotSetInvalidIntPriority4()
    {
        int priority = 4;
        handler.handleTaskProperty(task, type, key, priority);
        fail("The method should throw an exception and not reach here.");
    }

    @Test(expected = org.alfresco.service.cmr.workflow.WorkflowException.class)
    public void handleTaskPropertyDoesNotSetInvalidStringPriority0()
    {
        String priority = "0";
        handler.handleTaskProperty(task, type, key, priority);
        fail("The method should throw an exception and not reach here.");
    }

    @Test(expected = org.alfresco.service.cmr.workflow.WorkflowException.class)
    public void handleTaskPropertyDoesNotSetInvalidStringPriority4()
    {
        String priority = "4";
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
