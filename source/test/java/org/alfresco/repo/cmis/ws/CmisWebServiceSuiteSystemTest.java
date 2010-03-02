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
package org.alfresco.repo.cmis.ws;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Cmis Web service system tests suite
 * 
 * @author Alexander Tsvetkov
 */
public class CmisWebServiceSuiteSystemTest extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(DMDiscoveryServiceTest.class);
        suite.addTestSuite(DMMultiFilingServiceTest.class);
        suite.addTestSuite(DMNavigationServiceTest.class);
        suite.addTestSuite(DMObjectServiceTest.class);
        suite.addTestSuite(DMPolicyServiceTest.class);
        suite.addTestSuite(DMRelationshipServiceTest.class);
        suite.addTestSuite(DMRepositoryServiceTest.class);
        suite.addTestSuite(DMVersioningServiceTest.class);
        suite.addTestSuite(DMAclServiceTest.class);
        suite.addTestSuite(MultiThreadsServiceTest.class);

        return suite;
    }
}
