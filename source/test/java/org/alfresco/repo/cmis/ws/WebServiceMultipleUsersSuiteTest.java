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
