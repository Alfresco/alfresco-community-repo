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

    // very fast - no context tests - true jUnit tests
    org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilderTest.class,
    org.alfresco.repo.web.scripts.solr.StatsGetTest.class,
    org.alfresco.repo.web.scripts.solr.SOLRSerializerTest.class,
    org.alfresco.repo.web.scripts.solr.SOLRAuthenticationFilterTest.class,
    org.alfresco.repo.web.util.PagingCursorTest.class,
    org.alfresco.repo.web.util.paging.PagingTest.class,
    org.alfresco.repo.webdav.GetMethodTest.class,
    org.alfresco.repo.webdav.LockInfoImplTest.class,
    org.alfresco.repo.webdav.RenameShuffleDetectionTest.class,
    org.alfresco.repo.webdav.WebDAVHelperTest.class,
    org.alfresco.repo.webdav.WebDAVLockServiceImplTest.class,
    org.alfresco.rest.api.impl.ContentStorageInformationImplTest.class,
    org.alfresco.rest.api.nodes.NodeStorageInfoRelationTest.class,
    org.alfresco.rest.api.search.ResultMapperTests.class,
    org.alfresco.rest.api.search.SearchApiWebscriptTests.class,
    org.alfresco.rest.api.search.SearchMapperTests.class,
    org.alfresco.rest.api.search.SearchQuerySerializerTests.class,
    org.alfresco.rest.api.search.StoreMapperTests.class,
    org.alfresco.rest.api.tests.ModulePackageTest.class,
    org.alfresco.rest.framework.tests.core.InspectorTests.class,
    org.alfresco.rest.framework.tests.core.JsonJacksonTests.class,
    org.alfresco.rest.framework.tests.core.ParamsExtractorTests.class,
    org.alfresco.rest.framework.tests.core.WhereTests.class,
    org.alfresco.rest.framework.tests.core.WithResponseTest.class,
    org.alfresco.rest.framework.tools.RecognizedParamsExtractorTest.class,
        // add applicationContext_02_part2 as it is compatible with the rest of the tests in this test suite
        // and because it balances the load of the build jobs
    
    // [classpath:alfresco/application-context.xml, classpath:alfresco/web-scripts-application-context-test.xml,
    // classpath:alfresco/web-scripts-application-context.xml]
    // this uses the same context set as applicationContext_02
    // this does not want to run at the beginning or the end of the applicationContext_02
    // these tests run very fast once the context is up
    org.alfresco.repo.remoteticket.RemoteAlfrescoTicketServiceTest.class,
    org.alfresco.repo.web.scripts.servlet.RemoteAuthenticatorFactoryTest.class,
    org.alfresco.repo.web.scripts.servlet.RemoteAuthenticatorFactoryAdminConsoleAccessTest.class,

    // [classpath:alfresco/application-context.xml]
    org.alfresco.repo.webdav.GetMethodRegressionTest.class,
    org.alfresco.repo.webdav.WebDAVHelperIntegrationTest.class,
    org.alfresco.repo.web.scripts.person.UserCSVUploadTest.class,

    // [classpath:alfresco/application-context.xml, classpath:alfresco/web-scripts-application-context.xml,
    // classpath:alfresco/web-scripts-application-context-test.xml,
    // classpath:alfresco/declarative-spreadsheet-webscript-application-context.xml]
    org.alfresco.repo.web.scripts.DeclarativeSpreadsheetWebScriptTest.class,

    // [classpath:alfresco/application-context.xml, classpath:alfresco/public-rest-context.xml,
    // classpath:alfresco/web-scripts-application-context.xml]
    org.alfresco.rest.test.workflow.api.impl.ProcessesImplTest.class,

    // [classpath:alfresco/application-context.xml, classpath:alfresco/web-scripts-application-context.xml,
    // classpath:alfresco/remote-api-context.xml]
    org.alfresco.repo.webdav.DeleteMethodTest.class,
    org.alfresco.repo.webdav.LockMethodTest.class,
    org.alfresco.repo.webdav.MoveMethodTest.class,
    org.alfresco.repo.webdav.UnlockMethodTest.class,
    org.alfresco.repo.webdav.WebDAVMethodTest.class,
    org.alfresco.repo.webdav.PutMethodTest.class,
    org.alfresco.repo.webdav.WebDAVonContentUpdateTest.class,

    // [classpath:test-rest-context.xml]
    org.alfresco.rest.framework.tests.core.ExceptionResolverTests.class,
    org.alfresco.rest.framework.tests.core.ExecutionTests.class,
    org.alfresco.rest.framework.tests.core.ResourceLocatorTests.class,
    org.alfresco.rest.framework.tests.core.SerializeTests.class,
    org.alfresco.rest.framework.tests.metadata.WriterTests.class,
})
public class AppContextExtraTestSuite
{
}
