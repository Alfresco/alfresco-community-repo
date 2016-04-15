package org.alfresco.repo.workflow;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.workflow.activiti.ActivitiMultitenantWorkflowTest;
import org.alfresco.repo.workflow.activiti.ActivitiSpringTransactionTest;
import org.alfresco.repo.workflow.activiti.ActivitiTimerExecutionTest;
import org.alfresco.repo.workflow.activiti.ActivitiWorkflowServiceIntegrationTest;
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

        suite.addTestSuite( StartWorkflowActionExecuterTest.class );

        // Add the Activiti tests to be run
        suite.addTestSuite( ActivitiWorkflowServiceIntegrationTest.class );
        suite.addTestSuite( ActivitiSpringTransactionTest.class );
        suite.addTestSuite( ActivitiTimerExecutionTest.class );

         // This test will force the application context properly, which avoids
        // periodic wierd build failures
        suite.addTestSuite( WorkflowSuiteContextShutdownTest.class );

        // These tests use a different Spring config.
        suite.addTestSuite( ActivitiMultitenantWorkflowTest.class );
                
        // Note the following workflow tests are not included in this sutie:
        // ActivitiTaskComponentTest
        // ActivitiWorkflowComponentTest
        // ActivitiWorkflowRestApiTest
        // JbpmWorkflowRestApiTest
        return suite;
    }
}
