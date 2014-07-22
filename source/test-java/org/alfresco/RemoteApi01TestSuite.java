/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All Remote API project test classes and test suites as a sequence of RemoteApi&lt;NN>TestSuite
 * classes. The original order is the same as run by ant to avoid any data issues.
 * The new test suite boundaries exist to allow tests to have different suite setups.
 * It is better to have &lt;NN> startups than one for each test. 
 */
public class RemoteApi01TestSuite extends TestSuite
{
    /**
     * Creates the test suite
     *
     * @return  the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        tests1(suite);

        return suite;
    }
    
    static void tests1(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.management.subsystems.test.SubsystemsTest.class);
        suite.addTestSuite(org.alfresco.repo.remoteticket.RemoteAlfrescoTicketServiceTest.class);
    }
    
    static void tests2(TestSuite suite) // 
    {
        suite.addTest(org.alfresco.repo.web.scripts.WebScriptTestSuite.suite());
    }
    
    static void tests3(TestSuite suite) // 
    {
        suite.addTestSuite(org.alfresco.repo.webdav.GetMethodRegressionTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.MoveMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.UnlockMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.LockMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.WebDAVHelperIntegrationTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.WebDAVMethodTest.class));
    }
    
    static void tests4(TestSuite suite) // 
    {
        // TestNodeComments.testNodeComments() fails 50% of the time we with previous tests
        // TestCMIS.testCMIS()                 fails 30% of the time with previous tests
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.ApiTest.class));
    }
    
    static void tests5(TestSuite suite) // 
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestCMISAPI.class)); // Fails with previous or following tests
    }
    
    static void tests6(TestSuite suite) // 
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.workflow.api.tests.DeploymentWorkflowApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.workflow.api.tests.ProcessDefinitionWorkflowApiTest.class));
    }
    
    static void tests7(TestSuite suite) // 
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.workflow.api.tests.ProcessWorkflowApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.workflow.api.tests.TaskWorkflowApiTest.class));
    }
}