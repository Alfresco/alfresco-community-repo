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
package org.alfresco.repo.workflow;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.workflow.activiti.ActivitiMultitenantWorkflowTest;
import org.alfresco.repo.workflow.activiti.ActivitiSpringTransactionTest;
import org.alfresco.repo.workflow.activiti.ActivitiTimerExecutionTest;
import org.alfresco.repo.workflow.activiti.ActivitiWorkflowServiceIntegrationTest;
import org.alfresco.repo.workflow.activiti.WorklfowObjectFactoryTest;
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

        // General workflows tests
        suite.addTestSuite(WorklfowObjectFactoryTest.class);

        // Note the following workflow tests are not included in this sutie:
        // ActivitiTaskComponentTest
        // ActivitiWorkflowComponentTest
        // ActivitiWorkflowRestApiTest
        return suite;
    }
}
