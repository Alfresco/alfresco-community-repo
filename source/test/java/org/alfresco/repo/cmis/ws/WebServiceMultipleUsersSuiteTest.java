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
import static org.alfresco.repo.cmis.ws.CmisServiceTestHelper.*;

public class WebServiceMultipleUsersSuiteTest extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        // Discovery Service tests
        // suite.addTest(new DMDiscoveryServiceTest("testQuery", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMDiscoveryServiceTest("testQuery", USERNAME_USER1, PASSWORD_USER1));

        // MultiFiling Service tests
        // suite.addTest(new DMMultiFilingServiceTest("testAddObjectToFolder", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMMultiFilingServiceTest("testAddObjectToFolder", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMMultiFilingServiceTest("testRemoveObjectFromFolder", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMMultiFilingServiceTest("testRemoveObjectFromFolder", USERNAME_USER1, PASSWORD_USER1));

        // Navigation Service tests

        // suite.addTest(new DMNavigationServiceTest("testGetChildren", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMNavigationServiceTest("testGetChildren", USERNAME_USER1, PASSWORD_USER1));

        // suite.addTest(new DMNavigationServiceTest("testGetCheckedoutDocs", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMNavigationServiceTest("testGetCheckedoutDocs", USERNAME_USER1, PASSWORD_USER1));

        // suite.addTest(new DMNavigationServiceTest("testGetDescendants", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMNavigationServiceTest("testGetDescendants", USERNAME_USER1, PASSWORD_USER1));

        // suite.addTest(new DMNavigationServiceTest("testGetFolderParent", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMNavigationServiceTest("testGetFolderParent", USERNAME_USER1, PASSWORD_USER1));

        // suite.addTest(new DMNavigationServiceTest("testGetObjectParents", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMNavigationServiceTest("testGetObjectParents", USERNAME_USER1, PASSWORD_USER1));

        // Object Service tests
        // suite.addTest(new DMObjectServiceTest("testCreateDocument", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testCreateDocument", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testCreateFolder", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testCreateFolder", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testGetDocumentProperties", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testGetDocumentProperties", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testGetContentStream", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testGetContentStream", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testCreatePolicy", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testCreatePolicy", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testCreateRelationship", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testCreateRelationship", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testDeleteContentStream", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testDeleteContentStream", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testDeleteObject", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testDeleteObject", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testDeleteTree", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testDeleteTree", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testGetAllowableActions", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testGetAllowableActions", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testMoveObject", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testMoveObject", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testSetContentStream", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testSetContentStream", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMObjectServiceTest("testUpdateProperties", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMObjectServiceTest("testUpdateProperties", USERNAME_USER1, PASSWORD_USER1));

        // MultiThreadsServiceTest
        // suite.addTest(new MultiThreadsServiceTest("testUpdateDocumentMultiThreaded", USERNAME_USER1, PASSWORD_USER1));
        suite.addTest(new MultiThreadsServiceTest("testSetTextContentStreamMultiThreaded", USERNAME_USER1, PASSWORD_USER1));

        // //Policy Service tests
        // suite.addTest(new DMPolicyServiceTest("testApplyPolicy", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMPolicyServiceTest("testApplyPolicy", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMPolicyServiceTest("testGetAppliedPolicies", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMPolicyServiceTest("testGetAppliedPolicies", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMPolicyServiceTest("testRemovePolicy", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMPolicyServiceTest("testRemovePolicy", USERNAME_USER1, PASSWORD_USER1));

        // Repository Service tests
        // suite.addTest(new DMRepositoryServiceTest("testGetRepositories", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMRepositoryServiceTest("testGetRepositories", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMRepositoryServiceTest("testGetRepositoryInfo", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMRepositoryServiceTest("testGetRepositoryInfo", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMRepositoryServiceTest("testGetTypes", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMRepositoryServiceTest("testGetTypes", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMRepositoryServiceTest("testGetTypeDefinition", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMRepositoryServiceTest("testGetTypeDefinition", USERNAME_USER1, PASSWORD_USER1));

        // Versioning Service tests
        // suite.addTest(new DMVersioningServiceTest("testCheckOutCheckIn", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMVersioningServiceTest("testCheckOutCheckIn", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMVersioningServiceTest("testCheckOutCancelCheckOut", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMVersioningServiceTest("testCheckOutCancelCheckOut", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMVersioningServiceTest("testGetPropertiesOfLatestVersion", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMVersioningServiceTest("testGetPropertiesOfLatestVersion", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMVersioningServiceTest("testGetAllVersions", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMVersioningServiceTest("testGetAllVersions", USERNAME_USER1, PASSWORD_USER1));
        // suite.addTest(new DMVersioningServiceTest("testDeleteAllVersions", USERNAME_ADMIN, PASSWORD_ADMIN));
        suite.addTest(new DMVersioningServiceTest("testDeleteAllVersions", USERNAME_USER1, PASSWORD_USER1));
        return suite;

    }
}
