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

import org.alfresco.repo.web.scripts.TestWebScriptRepoServer;
import org.alfresco.util.testing.category.DBTests;
import org.alfresco.util.testing.category.NonBuildTests;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Categories.ExcludeCategory({DBTests.class, NonBuildTests.class})
@Suite.SuiteClasses({
    // [classpath:alfresco/application-context.xml, classpath:alfresco/web-scripts-application-context-test.xml,
    // classpath:alfresco/web-scripts-application-context.xml]
    org.alfresco.repo.web.scripts.quickshare.QuickShareRestApiTest.class,
    org.alfresco.repo.web.scripts.admin.AdminWebScriptTest.class,
    org.alfresco.repo.web.scripts.audit.AuditWebScriptTest.class,
    org.alfresco.repo.web.scripts.blogs.BlogServiceTest.class,
    org.alfresco.repo.web.scripts.dictionary.DictionaryRestApiTest.class,
    org.alfresco.repo.web.scripts.discussion.DiscussionRestApiTest.class,
    org.alfresco.repo.web.scripts.activities.feed.control.FeedControlTest.class,
    org.alfresco.repo.web.scripts.forms.FormRestApiGet_Test.class,
    org.alfresco.repo.web.scripts.forms.FormRestApiJsonPost_Test.class,
    org.alfresco.repo.web.scripts.groups.GroupsTest.class,
    org.alfresco.repo.web.scripts.invitation.InvitationWebScriptTest.class,
    org.alfresco.repo.web.scripts.invite.InviteServiceTest.class,
    org.alfresco.repo.web.scripts.LoginTest.class,
    org.alfresco.repo.web.scripts.search.PersonSearchTest.class,
    org.alfresco.repo.web.scripts.person.PersonServiceTest.class,
    org.alfresco.repo.web.scripts.preference.PreferenceServiceTest.class,
    org.alfresco.repo.web.scripts.rating.RatingRestApiTest.class,
    org.alfresco.repo.web.scripts.replication.ReplicationRestApiTest.class,
    org.alfresco.repo.web.scripts.RepositoryContainerTest.class,
    org.alfresco.repo.web.scripts.rule.RuleServiceTest.class,
    org.alfresco.repo.web.scripts.action.RunningActionRestApiTest.class,
    org.alfresco.repo.web.scripts.site.SiteServiceTest.class,
    org.alfresco.repo.web.scripts.tagging.TaggingServiceTest.class,
    org.alfresco.repo.web.scripts.thumbnail.ThumbnailServiceTest.class,
    org.alfresco.repo.web.scripts.transfer.TransferWebScriptTest.class,
    org.alfresco.repo.web.scripts.workflow.ActivitiWorkflowRestApiTest.class,
    org.alfresco.repo.web.scripts.solr.SOLRWebScriptTest.class,
    org.alfresco.repo.web.scripts.subscriptions.SubscriptionServiceRestApiTest.class,
    org.alfresco.repo.web.scripts.facet.FacetRestApiTest.class,
    org.alfresco.repo.web.scripts.comment.CommentsApiTest.class,
    org.alfresco.repo.web.scripts.content.ContentGetTest.class,
    org.alfresco.repo.web.scripts.XssVulnerabilityTest.class,
    org.alfresco.repo.web.scripts.links.LinksRestApiTest.class,
    org.alfresco.repo.model.filefolder.RemoteFileFolderLoaderTest.class,
    org.alfresco.repo.web.scripts.ReadOnlyTransactionInGetRestApiTest.class,
    org.alfresco.repo.web.scripts.custommodel.CustomModelImportTest.class,
    org.alfresco.repo.web.scripts.site.SurfConfigTest.class,
    org.alfresco.repo.web.scripts.node.NodeWebScripTest.class,
    org.alfresco.rest.api.impl.CommentsImplUnitTest.class,
    org.alfresco.rest.api.impl.DownloadsImplCheckArchiveStatusUnitTest.class,
    org.alfresco.rest.api.impl.RestApiDirectUrlConfigUnitTest.class
})
public class AppContext04TestSuite
{
    public AppContext04TestSuite()
    {
        // Ensure that a suitable context is available
        TestWebScriptRepoServer.getTestServer();
    }
}
