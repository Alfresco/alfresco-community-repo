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

package org.alfresco.repo.workflow;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.alfresco.repo.workflow.jbpm.WorkflowTaskInstance;
import org.alfresco.util.ApplicationContextHelper;

public class WorkflowSuiteContextShutdownTest extends TestCase 
{
    public void testDummy() { /*Do Nothing */ }

    @Override
    protected void tearDown() throws Exception 
    {
        System.err.println("Workflow test suite has completed, shutting down the ApplicationContext...");
        closeContext();

        System.err.println("Workflow test suite shutdown has finished");
    }

    public static void closeContext() throws NoSuchFieldException, IllegalAccessException, InterruptedException
    {
        ApplicationContextHelper.closeApplicationContext();
      
        // Null out the static Workflow engine field
        Field engineField = WorkflowTaskInstance.class.getDeclaredField("jbpmEngine");
        engineField.setAccessible(true);
        engineField.set(null, null);

        Thread.yield();
        Thread.sleep(25);
        Thread.yield();
    }
    
}