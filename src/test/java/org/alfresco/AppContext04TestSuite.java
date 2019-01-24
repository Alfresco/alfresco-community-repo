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

/**
 * Repository project tests using the main context alfresco/application-context.xml.
 * To balance test jobs tests using this context have been split into multiple test suites.
 * Tests marked as DBTests are automatically excluded and are run as part of {@link AllDBTestsTestSuite}.
 */
@RunWith(Categories.class)
@Categories.ExcludeCategory({DBTests.class, NonBuildTests.class})
@Suite.SuiteClasses({
    org.alfresco.repo.usage.RepoUsageComponentTest.class,
    org.alfresco.repo.usage.UserUsageTest.class,
    org.alfresco.repo.usage.UserUsageTrackingComponentTest.class,
    org.alfresco.repo.version.VersionServiceImplTest.class,
    org.alfresco.repo.version.NodeServiceImplTest.class,
    org.alfresco.repo.version.ContentServiceImplTest.class,
    org.alfresco.repo.workflow.StartWorkflowActionExecuterTest.class,
    org.alfresco.repo.workflow.activiti.ActivitiWorkflowServiceIntegrationTest.class,
    org.alfresco.repo.workflow.activiti.ActivitiSpringTransactionTest.class,
    org.alfresco.repo.workflow.activiti.ActivitiTimerExecutionTest.class,
    org.alfresco.repo.invitation.ActivitiInvitationServiceImplTests.class,
    org.alfresco.repo.search.impl.solr.facet.SolrFacetConfigTest.class,
    org.alfresco.repo.doclink.DocumentLinkServiceImplTest.class,

    // This test opens, closes and again opens the alfresco application context.
    org.alfresco.repo.dictionary.CustomModelRepoRestartTest.class,

    org.alfresco.repo.rendition.executer.HTMLRenderingEngineTest.class,
    org.alfresco.repo.rendition.executer.XSLTFunctionsTest.class,
    org.alfresco.repo.rendition.executer.XSLTRenderingEngineTest.class,
    org.alfresco.repo.replication.ReplicationServiceIntegrationTest.class,
    org.alfresco.repo.template.XSLTProcessorTest.class,
    org.alfresco.repo.search.QueryRegisterComponentTest.class,
    org.alfresco.repo.search.SearchServiceTest.class,
    org.alfresco.repo.tagging.UpdateTagScopesActionExecuterTest.class,
    org.alfresco.repo.thumbnail.conditions.NodeEligibleForRethumbnailingEvaluatorTest.class,
    org.alfresco.repo.transaction.ConnectionPoolOverloadTest.class,
    org.alfresco.repo.action.scheduled.CronScheduledQueryBasedTemplateActionDefinitionTest.class,
    org.alfresco.repo.node.integrity.IncompleteNodeTaggerTest.class,
    org.alfresco.repo.node.integrity.IntegrityTest.class,
    org.alfresco.repo.policy.PolicyComponentTransactionTest.class,
    org.alfresco.repo.forms.FormServiceImplTest.class,
    org.alfresco.repo.imap.ImapMessageTest.class,
    org.alfresco.repo.imap.ImapServiceImplCacheTest.class,
    org.alfresco.repo.imap.ImapServiceImplTest.class,
    org.alfresco.repo.bulkimport.impl.BulkImportTest.class,
    org.alfresco.repo.discussion.DiscussionServiceImplTest.class,
    org.alfresco.repo.transfer.NodeCrawlerTest.class,
    org.alfresco.repo.transfer.TransferServiceCallbackTest.class,
    org.alfresco.repo.transfer.TransferServiceImplTest.class,
    org.alfresco.repo.transfer.TransferServiceToBeRefactoredTest.class,
    org.alfresco.repo.transfer.manifest.ManifestIntegrationTest.class,
    org.alfresco.repo.transfer.script.ScriptTransferServiceTest.class,
    org.alfresco.util.schemacomp.DbToXMLTest.class,
    org.alfresco.util.schemacomp.ExportDbTest.class,
    org.alfresco.util.schemacomp.SchemaReferenceFileTest.class,
    org.alfresco.repo.module.ModuleComponentHelperTest.class,
    org.alfresco.repo.node.getchildren.GetChildrenCannedQueryTest.class,

    // the following test fails locally - on windows
    org.alfresco.repo.content.transform.DifferrentMimeTypeTest.class,

    org.alfresco.repo.attributes.PropTablesCleanupJobIntegrationTest.class,
    org.alfresco.service.ServiceRegistryTest.class,

    // does not want to work in the same test suite as org.alfresco.repo.rule.* tests
    org.alfresco.opencmis.search.OpenCmisQueryTest.class
    })
public class AppContext04TestSuite
{
}
