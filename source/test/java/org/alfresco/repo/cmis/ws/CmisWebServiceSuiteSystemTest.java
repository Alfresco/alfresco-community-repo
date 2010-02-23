/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
