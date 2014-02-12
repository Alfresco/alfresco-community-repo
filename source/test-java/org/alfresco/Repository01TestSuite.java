/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.JUnit4TestAdapter;

/**
 * All Repository project test classes and test suites as a sequence of Repository&lt;NN>TestSuite
 * classes. The original order is the same as run by ant to avoid any data issues.
 * The new test suite boundaries exist to allow tests to have different suite setups.
 * It is better to have &lt;NN> startups than one for each test.
 */
public class Repository01TestSuite extends TestSuite
{
    /**
     * Creates the test suite
     *
     * @return  the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        tests1(suite);

        return suite;
    }

    static void tests1(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.RepositoryStartStopTest.class);
    }
    
    static void tests2(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.cmis.acl.CMISAccessControlServiceTest.class);
        suite.addTestSuite(org.alfresco.cmis.dictionary.CMISDictionaryTest.class);
        suite.addTestSuite(org.alfresco.cmis.mapping.CMISPropertyServiceTest.class);
        suite.addTestSuite(org.alfresco.cmis.renditions.CMISRenditionServiceTest.class);
        suite.addTestSuite(org.alfresco.cmis.search.QueryTest.class);
    }
    
    static void tests3(TestSuite suite) // tests="76" time="82.566"
    {
        suite.addTestSuite(org.alfresco.email.server.EmailServiceImplTest.class);
        suite.addTestSuite(org.alfresco.filesys.FTPServerTest.class);
        suite.addTestSuite(org.alfresco.filesys.repo.CifsIntegrationTest.class);
        suite.addTestSuite(org.alfresco.filesys.repo.ContentDiskDriverTest.class);
    }
    
    static void tests4(TestSuite suite) // tests="2" time="92.624"
    {
        suite.addTestSuite(org.alfresco.filesys.repo.LockKeeperImplTest.class);
    }
    
    static void tests5(TestSuite suite) // tests="19" time="12.852"
    {
        suite.addTestSuite(org.alfresco.jcr.importer.ImportTest.class);
        suite.addTestSuite(org.alfresco.jcr.item.Alf1791Test.class);
        suite.addTestSuite(org.alfresco.jcr.item.ItemTest.class);
        suite.addTestSuite(org.alfresco.jcr.query.QueryManagerImplTest.class);
        suite.addTestSuite(org.alfresco.jcr.repository.RepositoryImplTest.class);
        suite.addTestSuite(org.alfresco.jcr.session.SessionImplTest.class);
    }
    
    static void tests6(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.opencmis.CMISTest.class));
    }
    
    static void tests7(TestSuite suite) // tests="3" time="7.644"
    {
        suite.addTestSuite(org.alfresco.opencmis.OpenCmisLocalTest.class); // fails if with OpenCmisQueryTest
    }
    
    static void tests8(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.opencmis.search.OpenCmisQueryTest.class);
    }
    
    static void tests9(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.action.ActionTestSuite.class));
    }
    
    static void tests10(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.activities.ActivityServiceImplTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.activities.feed.FeedNotifierTest.class));
        suite.addTestSuite(org.alfresco.repo.admin.RepoAdminServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.admin.patch.PatchTest.class);
        suite.addTestSuite(org.alfresco.repo.admin.registry.RegistryServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.attributes.AttributeServiceTest.class);
    }
    
    static void tests11(TestSuite suite)
    {
        suite.addTest(org.alfresco.repo.audit.AuditTestSuite.suite());
    }
    
    static void tests12(TestSuite suite)
    {
        suite.addTest(org.alfresco.repo.avm.AVMTestSuite.suite());
    }
    
    static void tests13(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.blog.BlogServiceImplTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.bulkimport.impl.BulkImportTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.bulkimport.impl.StripingFilesystemTrackerTest.class));
    }

    static void tests14(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.cache.CacheTest.class); // errors if joined with previous tests
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.calendar.CalendarServiceImplTest.class)); // Can only be run once as "CalendarTestNewTestSite is already in use"
        suite.addTestSuite(org.alfresco.repo.coci.CheckOutCheckInServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.configuration.ConfigurableServiceImplTest.class);
    }
    
    static void tests15(TestSuite suite)
    {
        suite.addTest(org.alfresco.repo.content.ContentFullContextTestSuite.suite());
    }
    
    static void tests16(TestSuite suite)
    {
        suite.addTest(org.alfresco.repo.content.ContentMinimalContextTestSuite.suite());
    }
    
    static void tests17(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.content.caching.CachingContentStoreTestSuite.class));
    }
    
    static void tests18(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.copy.CopyServiceImplTest.class);
    }
    
    static void tests19(TestSuite suite) // fails if not on own - tests="3" time="5.6"
    {
        suite.addTestSuite(org.alfresco.repo.deploy.ASRDeploymentTest.class);
    }
    
    static void tests20(TestSuite suite) // fails if not on own - tests="9" time="170.012"
    {
        suite.addTestSuite(org.alfresco.repo.deploy.DeploymentServiceImplFSTest.class);
    }
    
    static void tests21(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.descriptor.DescriptorServiceTest.class);
        suite.addTestSuite(org.alfresco.repo.dictionary.DictionaryModelTypeTest.class);
        suite.addTestSuite(org.alfresco.repo.dictionary.DictionaryRepositoryBootstrapTest.class);
        suite.addTestSuite(org.alfresco.repo.dictionary.types.period.PeriodTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.discussion.DiscussionServiceImplTest.class));
    }
    
    static void tests22(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.domain.DomainTestSuite.class));
    }
    
    static void tests23(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.download.DownloadServiceIntegrationTest.class));
        suite.addTestSuite(org.alfresco.repo.exporter.ExporterComponentTest.class);
        suite.addTestSuite(org.alfresco.repo.exporter.RepositoryExporterComponentTest.class);
    }
    
    static void tests24(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.forms.FormServiceImplTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.forms.processor.action.ActionFormProcessorTest.class));
    }
    
    static void tests27(TestSuite suite) // 
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.forum.CommentsTest.class));
    }
    
    static void tests28(TestSuite suite) // tests="53" time="178.25"
    {
        suite.addTestSuite(org.alfresco.repo.i18n.MessageServiceImplTest.class); //  fails if with previous tests
        suite.addTestSuite(org.alfresco.repo.imap.ImapMessageTest.class);
        suite.addTestSuite(org.alfresco.repo.imap.ImapServiceImplCacheTest.class);
        suite.addTestSuite(org.alfresco.repo.imap.ImapServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.importer.FileImporterTest.class);
        suite.addTestSuite(org.alfresco.repo.importer.ImporterComponentTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.invitation.InvitationCleanupTest.class));
    }
    
    static void tests29(TestSuite suite) // tests="12" time="93.965"
    {
        suite.addTestSuite(org.alfresco.repo.jscript.PeopleTest.class); // fails if with previous tests
        suite.addTestSuite(org.alfresco.repo.jscript.RhinoScriptTest.class);
        suite.addTestSuite(org.alfresco.repo.jscript.ScriptBehaviourTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.jscript.ScriptNodeTest.class));
    }
    
    static void tests30(TestSuite suite) // tests="70" time="62.041" failures="1"
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.links.LinksServiceImplTest.class)); // fails if run more than once // fails if with previous tests
        suite.addTestSuite(org.alfresco.repo.lock.JobLockServiceTest.class);
        suite.addTestSuite(org.alfresco.repo.lock.LockBehaviourImplTest.class);
        suite.addTestSuite(org.alfresco.repo.lock.LockServiceImplTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.lock.mem.LockStoreImplTxTest.class)); // failed on bamboo
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.lock.mem.LockableAspectInterceptorTest.class));
        suite.addTestSuite(org.alfresco.repo.management.JmxDumpUtilTest.class);
    }
    
    static void tests31(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.model.ModelTestSuite.class));
    }
    
    static void tests32(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.module.ComponentsTest.class);
        suite.addTestSuite(org.alfresco.repo.module.ModuleComponentHelperTest.class);
        suite.addTestSuite(org.alfresco.repo.node.ConcurrentNodeServiceSearchTest.class);
        suite.addTestSuite(org.alfresco.repo.node.ConcurrentNodeServiceTest.class); // was null
        suite.addTestSuite(org.alfresco.repo.node.FullNodeServiceTest.class);
        suite.addTestSuite(org.alfresco.repo.node.NodeRefPropertyMethodInterceptorTest.class);
    }
    
    static void tests33(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.node.NodeServiceTest.class)); // moved to next test
    }
    
    static void tests34(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.node.PerformanceNodeServiceTest.class);
        suite.addTestSuite(org.alfresco.repo.node.archive.ArchiveAndRestoreTest.class);
    }
    
    static void tests35(TestSuite suite) // tests="86" time="553.462"
    {
        suite.addTestSuite(org.alfresco.repo.node.archive.LargeArchiveAndRestoreTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.node.cleanup.TransactionCleanupTest.class));
        suite.addTestSuite(org.alfresco.repo.node.db.DbNodeServiceImplTest.class);
    }
    
    static void tests36(TestSuite suite) // Fails with previous tests
    {
        suite.addTestSuite(org.alfresco.repo.node.getchildren.GetChildrenCannedQueryTest.class);
        suite.addTestSuite(org.alfresco.repo.node.index.AVMRemoteSnapshotTrackerTest.class);
        suite.addTestSuite(org.alfresco.repo.node.index.FullIndexRecoveryComponentTest.class);
        suite.addTestSuite(org.alfresco.repo.node.index.IndexTransactionTrackerTest.class);
    }
    
    static void tests37(TestSuite suite) // Hangs with the previous 4 tests
    {
        suite.addTestSuite(org.alfresco.repo.node.index.MissingContentReindexComponentTest.class);
    }
    
    static void tests38(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.node.integrity.IncompleteNodeTaggerTest.class);
        suite.addTestSuite(org.alfresco.repo.node.integrity.IntegrityTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.oauth1.OAuth1CredentialsStoreServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.oauth2.OAuth2CredentialsStoreServiceTest.class));
        suite.addTestSuite(org.alfresco.repo.policy.PolicyComponentTransactionTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.preference.PreferenceServiceImplTest.class));
    }
    
    static void tests39(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.publishing.WebPublishingTestSuite.class));
    }
    
    static void tests40(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.quickshare.QuickShareServiceIntegrationTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.rating.RatingServiceIntegrationTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.remotecredentials.RemoteCredentialsServicesTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.rendition.MultiUserRenditionTest.class));
        suite.addTestSuite(org.alfresco.repo.rendition.RenditionServiceIntegrationTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.rendition.RenditionServicePermissionsTest.class));
    }
    
    static void tests41(TestSuite suite) // tests="6" time="120.585"
    {
        suite.addTestSuite(org.alfresco.repo.rendition.StandardRenditionLocationResolverTest.class);
        suite.addTestSuite(org.alfresco.repo.rendition.executer.HTMLRenderingEngineTest.class);
    }
    
    static void tests42(TestSuite suite) // tests="27" time="134.585"
    {
        suite.addTestSuite(org.alfresco.repo.rendition.executer.XSLTFunctionsTest.class); // fails if with previous tests
        suite.addTestSuite(org.alfresco.repo.rendition.executer.XSLTRenderingEngineTest.class);
        suite.addTestSuite(org.alfresco.repo.replication.ReplicationServiceIntegrationTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.rule.MiscellaneousRulesTest.class));
    }
    
    static void tests43(TestSuite suite) // tests="53" time="113.999"
    {
        suite.addTestSuite(org.alfresco.repo.rule.RuleLinkTest.class); // fails if with previous tests
        suite.addTestSuite(org.alfresco.repo.rule.RuleServiceCoverageTest.class);
        suite.addTestSuite(org.alfresco.repo.rule.RuleServiceImplTest.class);
    }
    
    static void tests44(TestSuite suite) // tests="2" time="104.636" errors="1"
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.rule.RuleServiceIntegrationTest.class)); // fails locally even on its own
    }
    
    static void tests45(TestSuite suite) // tests="13" time="97.186"
    {
        suite.addTestSuite(org.alfresco.repo.rule.RuleTypeImplTest.class);
        suite.addTestSuite(org.alfresco.repo.rule.ruletrigger.RuleTriggerTest.class);
    }
    
    static void tests46(TestSuite suite)
    {
        suite.addTest(org.alfresco.repo.search.SearchTestSuite.suite());
    }
    
    static void tests47(TestSuite suite)
    {
        suite.addTest(org.alfresco.repo.security.SecurityTestSuite.suite());
    }
    
    static void tests48(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.security.sync.ChainingUserRegistrySynchronizerTest.class); // failed on agent - split from following tests?
    }
    
    static void tests49(TestSuite suite) // Not sure this break is needed
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.site.SiteServiceImplMoreTest.class));
        suite.addTestSuite(org.alfresco.repo.site.SiteServiceImplTest.class);
    }
    
    static void tests50(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.solr.SOLRTrackingComponentTest.class);
    }
    
    static void tests51(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.subscriptions.SubscriptionServiceActivitiesTest.class));
    }
    
    static void tests52(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.subscriptions.SubscriptionServiceImplTest.class);
    }
    
    static void tests53(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.tagging.TaggingServiceImplTest.class);
    }
    
    static void tests54(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.template.AVMTemplateNodeTest.class);
    }
    
    static void tests55(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.template.TemplateServiceImplTest.class);
    }
    
    static void tests56(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.tenant.MultiTDemoTest.class);
    }
    
    static void tests57(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.tenant.MultiTNodeServiceInterceptorTest.class);
        suite.addTestSuite(org.alfresco.repo.template.XSLTProcessorTest.class); // Moved, was before MultiTDemoTest
        suite.addTestSuite(org.alfresco.repo.thumbnail.ThumbnailServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.thumbnail.conditions.NodeEligibleForRethumbnailingEvaluatorTest.class);
        suite.addTestSuite(org.alfresco.repo.transaction.AlfrescoTransactionSupportTest.class);
        suite.addTestSuite(org.alfresco.repo.transaction.RetryingTransactionHelperTest.class);
        suite.addTestSuite(org.alfresco.repo.transaction.TransactionAwareSingletonTest.class);
        suite.addTestSuite(org.alfresco.repo.transaction.TransactionServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.NodeCrawlerTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.RepoTransferReceiverImplTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.TransferServiceCallbackTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.TransferServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.TransferServiceToBeRefactoredTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.TransferVersionCheckerImplTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.manifest.ManifestIntegrationTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.script.ScriptTransferServiceTest.class);
    }
    
    static void tests58(TestSuite suite)
    {
        suite.addTest(org.alfresco.repo.usage.UsageTestSuite.suite());
    }
    
    static void tests59(TestSuite suite)
    {
        suite.addTest(org.alfresco.repo.version.VersionTestSuite.suite());
    }
    
    static void tests60(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.wiki.WikiServiceImplTest.class));
    }
    
    static void tests61(TestSuite suite)
    {
        suite.addTest(org.alfresco.repo.workflow.WorkflowTestSuite.suite());
    }
        
    static void tests63(TestSuite suite) // tests="187" time="364.334"
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.DbToXMLTest.class));
       suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.ExportDbTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.SchemaReferenceFileTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.test.junitrules.AlfrescoPersonTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.test.junitrules.ApplicationContextInitTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.test.junitrules.TemporaryNodesTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.test.junitrules.TemporarySitesTest.class));
    }
}
