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
package org.alfresco.repo.web.scripts;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.model.filefolder.RemoteFileFolderLoaderTest;
import org.alfresco.repo.web.scripts.action.RunningActionRestApiTest;
import org.alfresco.repo.web.scripts.activities.feed.control.FeedControlTest;
import org.alfresco.repo.web.scripts.admin.AdminWebScriptTest;
import org.alfresco.repo.web.scripts.audit.AuditWebScriptTest;
import org.alfresco.repo.web.scripts.blogs.BlogServiceTest;
import org.alfresco.repo.web.scripts.comment.CommentsApiTest;
import org.alfresco.repo.web.scripts.custommodel.CustomModelImportTest;
import org.alfresco.repo.web.scripts.dictionary.DictionaryRestApiTest;
import org.alfresco.repo.web.scripts.discussion.DiscussionRestApiTest;
import org.alfresco.repo.web.scripts.facet.FacetRestApiTest;
import org.alfresco.repo.web.scripts.forms.FormRestApiGet_Test;
import org.alfresco.repo.web.scripts.forms.FormRestApiJsonPost_Test;
import org.alfresco.repo.web.scripts.groups.GroupsTest;
import org.alfresco.repo.web.scripts.invitation.InvitationWebScriptTest;
import org.alfresco.repo.web.scripts.invite.InviteServiceTest;
import org.alfresco.repo.web.scripts.links.LinksRestApiTest;
import org.alfresco.repo.web.scripts.node.NodeWebScripTest;
import org.alfresco.repo.web.scripts.person.PersonServiceTest;
import org.alfresco.repo.web.scripts.preference.PreferenceServiceTest;
import org.alfresco.repo.web.scripts.quickshare.QuickShareRestApiTest;
import org.alfresco.repo.web.scripts.rating.RatingRestApiTest;
import org.alfresco.repo.web.scripts.replication.ReplicationRestApiTest;
import org.alfresco.repo.web.scripts.rule.RuleServiceTest;
import org.alfresco.repo.web.scripts.search.PersonSearchTest;
import org.alfresco.repo.web.scripts.site.SiteServiceTest;
import org.alfresco.repo.web.scripts.site.SurfConfigTest;
import org.alfresco.repo.web.scripts.solr.SOLRWebScriptTest;
import org.alfresco.repo.web.scripts.subscriptions.SubscriptionServiceRestApiTest;
import org.alfresco.repo.web.scripts.tagging.TaggingServiceTest;
import org.alfresco.repo.web.scripts.thumbnail.ThumbnailServiceTest;
import org.alfresco.repo.web.scripts.transfer.TransferWebScriptTest;
import org.alfresco.repo.web.scripts.workflow.ActivitiWorkflowRestApiTest;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilderTest;
import org.alfresco.repo.web.scripts.content.ContentGetTest;

/**
 * Web Scripts test suite
 */
public class WebScriptTestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        
        // Ensure that a suitable context is available
        TestWebScriptRepoServer.getTestServer();
        
        // Add the tests
        suite.addTestSuite( QuickShareRestApiTest.class );
        suite.addTestSuite( AdminWebScriptTest.class );
        suite.addTestSuite( AuditWebScriptTest.class );
        suite.addTestSuite( BlogServiceTest.class );
        suite.addTestSuite( DictionaryRestApiTest.class );
        suite.addTestSuite( DiscussionRestApiTest.class );
        suite.addTestSuite( FeedControlTest.class );
        suite.addTestSuite( FormRestApiGet_Test.class );
        suite.addTestSuite( FormRestApiJsonPost_Test.class );
        suite.addTestSuite( GroupsTest.class );
        suite.addTestSuite( InvitationWebScriptTest.class );
        suite.addTestSuite( InviteServiceTest.class );
        suite.addTestSuite( LoginTest.class );
        suite.addTestSuite( PersonSearchTest.class );
        suite.addTestSuite( PersonServiceTest.class );
        suite.addTestSuite( PreferenceServiceTest.class );
        suite.addTestSuite( RatingRestApiTest.class );
        suite.addTestSuite( ReplicationRestApiTest.class );
        suite.addTestSuite( RepositoryContainerTest.class );
        suite.addTestSuite( RuleServiceTest.class );
        suite.addTestSuite( RunningActionRestApiTest.class );
        suite.addTestSuite( SiteServiceTest.class );
        suite.addTestSuite( TaggingServiceTest.class );
        suite.addTestSuite( ThumbnailServiceTest.class );
        suite.addTestSuite( TransferWebScriptTest.class );
        suite.addTestSuite( WorkflowModelBuilderTest.class );
        suite.addTestSuite( ActivitiWorkflowRestApiTest.class );
        suite.addTestSuite( SOLRWebScriptTest.class );
        suite.addTestSuite( SubscriptionServiceRestApiTest.class );
        suite.addTestSuite( FacetRestApiTest.class );
        suite.addTestSuite( CommentsApiTest.class );
        suite.addTestSuite( DeclarativeSpreadsheetWebScriptTest.class );
        suite.addTestSuite( ContentGetTest.class);
        suite.addTestSuite( XssVulnerabilityTest.class );
        suite.addTestSuite( LinksRestApiTest.class );
        suite.addTestSuite( RemoteFileFolderLoaderTest.class );
        suite.addTestSuite( ReadOnlyTransactionInGetRestApiTest.class );
        suite.addTestSuite( CustomModelImportTest.class );
        suite.addTestSuite( SurfConfigTest.class );
        suite.addTestSuite( NodeWebScripTest.class );
        // This uses a slightly different context
        // As such, we can't run it in the same suite as the others,
        //  due to finalisers closing caches when we're not looking
        //suite.addTestSuite( AssetTest.class );
        
        return suite;
    }
}
