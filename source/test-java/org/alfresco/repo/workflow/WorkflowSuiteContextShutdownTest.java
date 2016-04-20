
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