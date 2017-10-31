/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
    org.alfresco.rest.workflow.api.tests.ProcessWorkflowApiTest.class,
    org.alfresco.rest.workflow.api.tests.TaskWorkflowApiTest.class,
    org.alfresco.rest.api.tests.TestCustomConstraint.class,
    org.alfresco.rest.api.tests.TestCustomModel.class,
    org.alfresco.rest.api.tests.TestCustomProperty.class,
    org.alfresco.rest.api.tests.TestCustomTypeAspect.class,
    org.alfresco.rest.api.tests.TestSiteContainers.class,
    org.alfresco.rest.api.tests.TestSiteMembers.class,
    org.alfresco.rest.api.tests.TestSiteMembershipRequests.class,
    org.alfresco.rest.api.tests.TestSites.class,
    org.alfresco.rest.api.tests.TestTags.class,
    org.alfresco.rest.api.tests.SharedLinkApiTest.class,
    org.alfresco.rest.api.tests.RenditionsTest.class,
    org.alfresco.rest.api.tests.TestPeople.class,
})
public class AppContext03TestSuite
{
}
