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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.alfresco.repo.workflow.activiti.ActivitiSpringTransactionTest;
import org.alfresco.repo.workflow.activiti.ActivitiWorkflowServiceIntegrationTest;
import org.alfresco.repo.workflow.jbpm.AlfrescoJavaScriptIntegrationTest;
import org.alfresco.repo.workflow.jbpm.JBPMEngineTest;
import org.alfresco.repo.workflow.jbpm.JBPMSpringTest;
import org.alfresco.repo.workflow.jbpm.JbpmWorkflowServiceIntegrationTest;
import org.alfresco.repo.workflow.jbpm.ReviewAndApproveTest;
import org.alfresco.repo.workflow.jbpm.WorkflowTaskInstance;
import org.alfresco.util.ApplicationContextHelper;

/**
 * Workflow test suite
 */
public class WorkflowTestSuite extends TestSuite
{
    /**
     * Creates the test suite
     *
     * @return  the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        // Ensure that the default context is available
        ApplicationContextHelper.getApplicationContext();

        // Add the JBPM tests to be run
        suite.addTestSuite( StartWorkflowActionExecuterTest.class );
        suite.addTestSuite( JbpmWorkflowServiceIntegrationTest.class );
        suite.addTestSuite( ReviewAndApproveTest.class );
        suite.addTestSuite( JBPMSpringTest.class );
        suite.addTestSuite( JBPMEngineTest.class );
        suite.addTestSuite( AlfrescoJavaScriptIntegrationTest.class );
        // Add the Activiti tests to be run
        suite.addTestSuite( ActivitiWorkflowServiceIntegrationTest.class );
        suite.addTestSuite( ActivitiSpringTransactionTest.class );
        // TODO: ALF-9096
        // suite.addTestSuite( ActivitiTimerExecutionTest.class );

        // This test will force the application context properly, which avoids
        // periodic wierd build failures
        suite.addTestSuite( WorkflowSuiteContextShutdownTest.class );

        // Note the following workflow tests are not included in this sutie:
        // ActivitiTaskComponentTest
        // ActivitiWorkflowComponentTest
        // ActivitiWorkflowRestApiTest
        // JbpmWorkflowRestApiTest
        return suite;
    }
    
    public static class WorkflowSuiteContextShutdownTest extends TestCase {
       public void testDummy() { /*Do Nothing */ }

       @Override
    protected void tearDown() throws Exception {
          System.err.println("Workflow test suite has completed, shutting down the ApplicationContext...");
          ApplicationContextHelper.closeApplicationContext();
          
          // Null out the static Workflow engine field
          Field engineField = WorkflowTaskInstance.class.getDeclaredField("jbpmEngine");
          engineField.setAccessible(true);
          engineField.set(null, null);
          
          Thread.yield();
          Thread.sleep(25);
          Thread.yield();
          
          System.err.println("Workflow test suite shutdown has finished");
       }
    }
}
