/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import org.alfresco.repo.web.scripts.TestWebScriptRepoServer;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

public class AllRemoteApiTestsCatalogue
{
    // [classpath*:/publicapi/lucene/, classpath:alfresco/application-context.xml,
    // classpath:alfresco/web-scripts-application-context-test.xml,
    // classpath:alfresco/web-scripts-application-context.xml, rest-api-test-context.xml, testcmis-model-context.xml]
    static void applicationContext_01_part1(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestEnterpriseAtomPubTCK.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestPublicApiAtomPub10TCK.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestPublicApiAtomPub11TCK.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestPublicApiBrowser11TCK.class));
    }

    // [classpath*:/publicapi/lucene/, classpath:alfresco/application-context.xml,
    // classpath:alfresco/web-scripts-application-context-test.xml,
    // classpath:alfresco/web-scripts-application-context.xml, rest-api-test-context.xml, testcmis-model-context.xml]
    static void applicationContext_01_part2(TestSuite suite)
    {
        // this need to be run first
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestCMIS.class));
        
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestCustomModelExport.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.DeletedNodesTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.search.BasicSearchApiIntegrationTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.ActivitiesPostingTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.AuthenticationsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.DiscoveryApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.GroupsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.ModulePackagesApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.NodeApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.NodeAssociationsApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.NodeVersionsApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.QueriesNodesApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.QueriesPeopleApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.QueriesSitesApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestActivities.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestDownloads.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestFavouriteSites.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestFavourites.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestNetworks.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestNodeComments.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestNodeRatings.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestPersonSites.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestPublicApi128.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestPublicApiCaching.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestUserPreferences.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.WherePredicateApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestRemovePermissions.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.workflow.api.tests.DeploymentWorkflowApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.workflow.api.tests.ProcessDefinitionWorkflowApiTest.class));
    }

    // [classpath*:/publicapi/lucene/, classpath:alfresco/application-context.xml,
    // classpath:alfresco/web-scripts-application-context-test.xml,
    // classpath:alfresco/web-scripts-application-context.xml, rest-api-test-context.xml, testcmis-model-context.xml]
    static void applicationContext_01_part3(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.workflow.api.tests.ProcessWorkflowApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.workflow.api.tests.TaskWorkflowApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestCustomConstraint.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestCustomModel.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestCustomProperty.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestCustomTypeAspect.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestSiteContainers.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestSiteMembers.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestSiteMembershipRequests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestSites.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestTags.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.SharedLinkApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.RenditionsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.TestPeople.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/web-scripts-application-context-test.xml,
    // classpath:alfresco/web-scripts-application-context.xml]
    static void applicationContext_02_part1(TestSuite suite)
    {
        // Ensure that a suitable context is available
        TestWebScriptRepoServer.getTestServer();

        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.quickshare.QuickShareRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.admin.AdminWebScriptTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.audit.AuditWebScriptTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.blogs.BlogServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.dictionary.DictionaryRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.discussion.DiscussionRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.activities.feed.control.FeedControlTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.forms.FormRestApiGet_Test.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.forms.FormRestApiJsonPost_Test.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.groups.GroupsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.invitation.InvitationWebScriptTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.invite.InviteServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.LoginTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.search.PersonSearchTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.person.PersonServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.preference.PreferenceServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.rating.RatingRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.replication.ReplicationRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.RepositoryContainerTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.rule.RuleServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.action.RunningActionRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.site.SiteServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.tagging.TaggingServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.thumbnail.ThumbnailServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.transfer.TransferWebScriptTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.workflow.ActivitiWorkflowRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.publishing.PublishingRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.solr.SOLRWebScriptTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.subscriptions.SubscriptionServiceRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.facet.FacetRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.comment.CommentsApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.content.ContentGetTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.XssVulnerabilityTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.links.LinksRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.model.filefolder.RemoteFileFolderLoaderTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.ReadOnlyTransactionInGetRestApiTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.custommodel.CustomModelImportTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.site.SurfConfigTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.node.NodeWebScripTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/web-scripts-application-context-test.xml,
    // classpath:alfresco/web-scripts-application-context.xml]
    static void applicationContext_02_part2(TestSuite suite)
    {
        // this uses the same context set as applicationContext_02
        // this does not want to run at the beginning or the end of the applicationContext_02
        // these tests run very fast once the context is up
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.remoteticket.RemoteAlfrescoTicketServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.servlet.RemoteAuthenticatorFactoryTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/web-scripts-application-context.xml,
    // classpath:alfresco/web-scripts-application-context-test.xml,
    // classpath:alfresco/declarative-spreadsheet-webscript-application-context.xml]
    static void applicationContext_03(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.DeclarativeSpreadsheetWebScriptTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/public-rest-context.xml,
    // classpath:alfresco/web-scripts-application-context.xml]
    static void applicationContext_04(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.test.workflow.api.impl.ProcessesImplTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/web-scripts-application-context.xml,
    // classpath:alfresco/remote-api-context.xml]
    static void applicationContext_05(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.DeleteMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.LockMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.MoveMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.UnlockMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.WebDAVMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.PutMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.WebDAVonContentUpdateTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:subsystem-test-context.xml]
    static void applicationContext_06(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.management.subsystems.test.SubsystemsTest.class));
    }

    // [classpath:alfresco/application-context.xml]
    static void applicationContext_07(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.GetMethodRegressionTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.WebDAVHelperIntegrationTest.class));
    }

    // [classpath:test-rest-context.xml]
    static void testRestContext(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.core.ExceptionResolverTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.core.ExecutionTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.core.ResourceLocatorTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.core.SerializeTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.metadata.WriterTests.class));
    }

    // very fast - no context tests - true jUnit tests
    static void unitTestsNoContext(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilderTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.scripts.solr.StatsGetTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.util.PagingCursorTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.web.util.paging.PagingTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.GetMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.LockInfoImplTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.RenameShuffleDetectionTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.WebDAVHelperTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.webdav.WebDAVLockServiceImplTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.search.ResultMapperTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.search.SearchApiWebscriptTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.search.SearchMapperTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.search.SearchQuerySerializerTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.search.StoreMapperTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.api.tests.ModulePackageTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.core.InspectorTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.core.JsonJacksonTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.core.ParamsExtractorTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.core.ResourceWebScriptHelperTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.core.WhereTests.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tests.core.WithResponseTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.rest.framework.tools.RecognizedParamsExtractorTest.class));
    }
}
