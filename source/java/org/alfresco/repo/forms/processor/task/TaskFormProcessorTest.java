/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.forms.processor.task;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.HashMap;

import junit.framework.TestCase;

import org.alfresco.repo.forms.Item;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.NamespaceServiceMemoryImpl;
import org.alfresco.service.namespace.QName;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Nick Smith
 */
public class TaskFormProcessorTest extends TestCase
{
    /**
     * 
     */
    private static final String TASK_ID = "Real Id";

    private WorkflowService workflowService;

    TaskFormProcessor processor;

    private WorkflowTask task;

    public void testGetTypedItem() throws Exception
    {
        try
        {
            processor.getTypedItem(null);
            fail("Should have thrown an Exception here!");
        }
        catch (IllegalArgumentException e)
        {
            // Do nothing!
        }
        try
        {
            processor.getTypedItem(new Item("task", "bad id"));
            fail("Should have thrown an Exception here!");
        }
        catch (WorkflowException e)
        {
            // Do nothing!
        }

        Item item = new Item("task", TASK_ID);
        WorkflowTask task = processor.getTypedItem(item);
        assertNotNull(task);
        assertEquals(TASK_ID, task.id);
    }

    // public void testGenerateSimple()
    // {
    // Item item = new Item("task", TASK_ID);
    // Form form = new Form(item);
    // List<String> fields = Arrays.asList("description");
    // processor.internalGenerate(task, fields, null, form, null);
    // }

    // public void testPersist() throws Exception
    // {
    // FormData data = new FormData();
    // processor.internalPersist(task, data);
    // }

    /*
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        task = makeTask();
        workflowService = makeWorkflowService();
        DictionaryService dictionaryService = makeDictionaryService();
        NamespaceService namespaceService = makeNamespaceService();
        processor = new TaskFormProcessor(workflowService, namespaceService, dictionaryService);
    }

    /**
     * 
     */
    private WorkflowTask makeTask()
    {
        WorkflowTask task = new WorkflowTask();
        task.id = TASK_ID;
        task.description = "Description";
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        QName descName = WorkflowModel.PROP_DESCRIPTION;
        properties.put(descName, "Description");
        return task;

    }

    private NamespaceService makeNamespaceService()
    {
        return new NamespaceServiceMemoryImpl();
    }

    private DictionaryService makeDictionaryService()
    {
        return mock(DictionaryService.class);
    }

    private WorkflowService makeWorkflowService()
    {
        WorkflowService service = mock(WorkflowService.class);
        when(service.getTaskById(anyString())).thenAnswer(new Answer<WorkflowTask>()
        {

            public WorkflowTask answer(InvocationOnMock invocation) throws Throwable
            {
                String id = (String) invocation.getArguments()[0];
                if (TASK_ID.equals(id))
                    return task;
                else
                    throw new WorkflowException("Task Id not found!");
            }
        });
        return service;
    }
}
