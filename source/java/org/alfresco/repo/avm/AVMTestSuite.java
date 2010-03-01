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
package org.alfresco.repo.avm;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.avm.locking.AVMLockingServiceTest;

/**
 * AVM test suite
 */
public class AVMTestSuite extends TestSuite
{
    /**
     * Creates the test suite
     *
     * @return  the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(AVMNodeConverterTest.class);
        suite.addTestSuite(AVMExpiredContentTest.class);
        suite.addTestSuite(AVMDeploymentAttemptCleanerTest.class);
        
        suite.addTestSuite(AVMServiceTestBase.class);
        suite.addTestSuite(AVMServiceTest.class);
        suite.addTestSuite(AVMServiceLocalTest.class);
        suite.addTestSuite(AVMLockingServiceTest.class);
        suite.addTestSuite(AVMServicePermissionsTest.class);
        suite.addTestSuite(AVMServiceIndexTest.class);
        
        suite.addTestSuite(AVMServicePerfTest.class);
        
        suite.addTestSuite(AVMCrawlTestP.class);
        suite.addTestSuite(AVMScaleTestP.class);
        suite.addTestSuite(AVMStressTestP.class);
        
        suite.addTestSuite(PurgeTestP.class);
        suite.addTestSuite(SimultaneousLoadTest.class);
        suite.addTestSuite(AVMDiffPerformanceTest.class);
        suite.addTestSuite(AVMChildNamePatternMatchPerformanceTest.class);
        
        return suite;
    }
}
