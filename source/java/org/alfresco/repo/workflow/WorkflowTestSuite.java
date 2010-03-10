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

import org.alfresco.repo.workflow.jbpm.JBPMDeleteProcessTest;
import org.alfresco.repo.workflow.jbpm.JBPMEngineTest;
import org.alfresco.repo.workflow.jbpm.JBPMEngineUnitTest;
import org.alfresco.repo.workflow.jbpm.JBPMSpringTest;
import org.alfresco.repo.workflow.jbpm.NodeListConverterTest;
import org.alfresco.repo.workflow.jbpm.ReviewAndApproveTest;
import org.alfresco.util.ApplicationContextHelper;

import junit.framework.Test;
import junit.framework.TestSuite;

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

        // Add the tests to be run
        suite.addTestSuite( StartWorkflowActionExecuterTest.class );
        suite.addTestSuite( WorkflowServiceImplTest.class );
        suite.addTestSuite( ReviewAndApproveTest.class );
        suite.addTestSuite( NodeListConverterTest.class );
        suite.addTestSuite( JBPMDeleteProcessTest.class );
        suite.addTestSuite( JBPMSpringTest.class );
        suite.addTestSuite( JBPMEngineTest.class );

        // This should go last, as its uses a different
        //  context to the other tests
        suite.addTestSuite( JBPMEngineUnitTest.class );
        
        return suite;
    }
}
