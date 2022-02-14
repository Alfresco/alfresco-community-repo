/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco;

import org.alfresco.util.testing.category.DBTests;
import org.alfresco.util.testing.category.NonBuildTests;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Categories.ExcludeCategory({DBTests.class, NonBuildTests.class})
@Suite.SuiteClasses({
    // [classpath*:/publicapi/lucene/, classpath:alfresco/application-context.xml,
    // classpath:alfresco/web-scripts-application-context-test.xml,
    // classpath:alfresco/web-scripts-application-context.xml, rest-api-test-context.xml, testcmis-model-context.xml]

    // this need to be run first
    org.alfresco.rest.api.tests.TestCMIS.class,

    org.alfresco.rest.api.tests.TestCustomModelExport.class,
    org.alfresco.rest.DeletedNodesTest.class,
    org.alfresco.rest.api.search.BasicSearchApiIntegrationTest.class,
    org.alfresco.rest.api.tests.ActivitiesPostingTest.class,
    org.alfresco.rest.api.tests.AuthenticationsTest.class,
    org.alfresco.rest.api.tests.DiscoveryApiTest.class,
    org.alfresco.rest.api.discovery.DiscoveryApiWebscriptUnitTest.class,
    org.alfresco.rest.api.tests.GroupsTest.class,
    org.alfresco.rest.api.tests.ModulePackagesApiTest.class,
    org.alfresco.rest.api.tests.NodeApiTest.class,
    org.alfresco.rest.api.tests.NodeAssociationsApiTest.class,
    org.alfresco.rest.api.tests.NodeVersionsApiTest.class,
    org.alfresco.rest.api.tests.NodeVersionRenditionsApiTest.class,
    org.alfresco.rest.api.tests.QueriesNodesApiTest.class,
    org.alfresco.rest.api.tests.QueriesPeopleApiTest.class,
    org.alfresco.rest.api.tests.QueriesSitesApiTest.class,
    org.alfresco.rest.api.tests.TestActivities.class,
    org.alfresco.rest.api.tests.TestDownloads.class,
    org.alfresco.rest.api.tests.TestFavouriteSites.class,
    org.alfresco.rest.api.tests.TestFavourites.class,
    org.alfresco.rest.api.tests.TestNetworks.class,
    org.alfresco.rest.api.tests.TestNodeComments.class,
    org.alfresco.rest.api.tests.TestNodeRatings.class,
    org.alfresco.rest.api.tests.TestPersonSites.class,
    org.alfresco.rest.api.tests.TestPublicApi128.class,
    org.alfresco.rest.api.tests.TestPublicApiCaching.class,
    org.alfresco.rest.api.tests.TestUserPreferences.class,
    org.alfresco.rest.api.tests.WherePredicateApiTest.class,
    org.alfresco.rest.api.tests.TestRemovePermissions.class, 
    org.alfresco.rest.api.tests.TempOutputStreamTest.class,
    org.alfresco.rest.api.tests.BufferedResponseTest.class,
    org.alfresco.rest.workflow.api.tests.DeploymentWorkflowApiTest.class,
    org.alfresco.rest.workflow.api.tests.ProcessDefinitionWorkflowApiTest.class,
})
public class AppContext02TestSuite
{
}
