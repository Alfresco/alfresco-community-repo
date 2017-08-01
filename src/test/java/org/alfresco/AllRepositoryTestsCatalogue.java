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

import org.alfresco.repo.action.ActionConditionDefinitionImplTest;
import org.alfresco.repo.action.ActionConditionImplTest;
import org.alfresco.repo.action.ActionDefinitionImplTest;
import org.alfresco.repo.action.ActionImplTest;
import org.alfresco.repo.action.ActionServiceImpl2Test;
import org.alfresco.repo.action.ActionServiceImplTest;
import org.alfresco.repo.action.ActionTrackingServiceImplTest;
import org.alfresco.repo.action.CompositeActionConditionImplTest;
import org.alfresco.repo.action.CompositeActionImplTest;
import org.alfresco.repo.action.ParameterDefinitionImplTest;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluatorTest;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluatorTest;
import org.alfresco.repo.action.evaluator.HasAspectEvaluatorTest;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluatorTest;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuterTest;
import org.alfresco.repo.action.executer.ContentMetadataEmbedderTest;
import org.alfresco.repo.action.executer.ContentMetadataExtracterTagMappingTest;
import org.alfresco.repo.action.executer.ContentMetadataExtracterTest;
import org.alfresco.repo.action.executer.ImporterActionExecuterTest;
import org.alfresco.repo.action.executer.MailActionExecuterTest;
import org.alfresco.repo.action.executer.RemoveFeaturesActionExecuterTest;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuterTest;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuterTest;
import org.alfresco.repo.audit.AuditBootstrapTest;
import org.alfresco.repo.audit.AuditComponentTest;
import org.alfresco.repo.audit.AuditMethodInterceptorTest;
import org.alfresco.repo.audit.AuditableAnnotationTest;
import org.alfresco.repo.audit.AuditableAspectTest;
import org.alfresco.repo.audit.PropertyAuditFilterTest;
import org.alfresco.repo.audit.UserAuditFilterTest;
import org.alfresco.repo.audit.access.AccessAuditorTest;
import org.alfresco.repo.content.ContentDataTest;
import org.alfresco.repo.content.GuessMimetypeTest;
import org.alfresco.repo.content.RoutingContentServiceTest;
import org.alfresco.repo.content.RoutingContentStoreTest;
import org.alfresco.repo.content.caching.CachingContentStoreTest;
import org.alfresco.repo.content.caching.ContentCacheImplTest;
import org.alfresco.repo.content.caching.FullTest;
import org.alfresco.repo.content.caching.cleanup.CachedContentCleanupJobTest;
import org.alfresco.repo.content.caching.quota.StandardQuotaStrategyMockTest;
import org.alfresco.repo.content.caching.quota.StandardQuotaStrategyTest;
import org.alfresco.repo.content.caching.quota.UnlimitedQuotaStrategyTest;
import org.alfresco.repo.content.caching.test.ConcurrentCachingStoreTest;
import org.alfresco.repo.content.caching.test.SlowContentStoreTest;
import org.alfresco.repo.content.cleanup.ContentStoreCleanerTest;
import org.alfresco.repo.content.filestore.FileContentStoreTest;
import org.alfresco.repo.content.filestore.NoRandomAccessFileContentStoreTest;
import org.alfresco.repo.content.filestore.ReadOnlyFileContentStoreTest;
import org.alfresco.repo.content.filestore.SpoofedTextContentReaderTest;
import org.alfresco.repo.content.metadata.MetadataExtracterLimitsTest;
import org.alfresco.repo.content.transform.TransformerConfigTestSuite;
import org.alfresco.repo.domain.audit.AuditDAOTest;
import org.alfresco.repo.domain.contentdata.ContentDataDAOTest;
import org.alfresco.repo.domain.encoding.EncodingDAOTest;
import org.alfresco.repo.domain.locale.LocaleDAOTest;
import org.alfresco.repo.domain.locks.LockDAOTest;
import org.alfresco.repo.domain.mimetype.MimetypeDAOTest;
import org.alfresco.repo.domain.node.NodeDAOTest;
import org.alfresco.repo.domain.patch.AppliedPatchDAOTest;
import org.alfresco.repo.domain.permissions.AclCrudDAOTest;
import org.alfresco.repo.domain.permissions.FixedAclUpdaterTest;
import org.alfresco.repo.domain.propval.PropertyTypeConverterTest;
import org.alfresco.repo.domain.propval.PropertyValueCleanupTest;
import org.alfresco.repo.domain.propval.PropertyValueDAOTest;
import org.alfresco.repo.domain.qname.QNameDAOTest;
import org.alfresco.repo.domain.query.CannedQueryDAOTest;
import org.alfresco.repo.domain.solr.SOLRDAOTest;
import org.alfresco.repo.domain.tenant.TenantAdminDAOTest;
import org.alfresco.repo.domain.usage.UsageDAOTest;
import org.alfresco.repo.ownable.impl.OwnableServiceTest;
import org.alfresco.repo.publishing.ChannelServiceImplIntegratedTest;
import org.alfresco.repo.publishing.ChannelServiceImplTest;
import org.alfresco.repo.publishing.PublishingEventHelperTest;
import org.alfresco.repo.publishing.PublishingIntegratedTest;
import org.alfresco.repo.publishing.PublishingPackageSerializerTest;
import org.alfresco.repo.publishing.PublishingQueueImplTest;
import org.alfresco.repo.publishing.PublishingRootObjectTest;
import org.alfresco.repo.search.DocumentNavigatorTest;
import org.alfresco.repo.search.MLAnaysisModeExpansionTest;
import org.alfresco.repo.search.QueryRegisterComponentTest;
import org.alfresco.repo.search.SearchServiceTest;
import org.alfresco.repo.search.SearcherComponentTest;
import org.alfresco.repo.search.impl.lucene.ADMLuceneCategoryTest;
import org.alfresco.repo.search.impl.lucene.ADMLuceneTest;
import org.alfresco.repo.search.impl.lucene.ALF947Test;
import org.alfresco.repo.search.impl.lucene.LuceneIndexBackupComponentTest;
import org.alfresco.repo.search.impl.lucene.MultiReaderTest;
import org.alfresco.repo.search.impl.lucene.index.IndexInfoTest;
import org.alfresco.repo.search.impl.parsers.CMISTest;
import org.alfresco.repo.search.impl.parsers.CMIS_FTSTest;
import org.alfresco.repo.search.impl.parsers.FTSTest;
import org.alfresco.repo.security.authentication.AlfrescoSSLSocketFactoryTest;
import org.alfresco.repo.security.authentication.AuthenticationBootstrapTest;
import org.alfresco.repo.security.authentication.AuthenticationTest;
import org.alfresco.repo.security.authentication.AuthorizationTest;
import org.alfresco.repo.security.authentication.ChainingAuthenticationServiceTest;
import org.alfresco.repo.security.authentication.NameBasedUserNameGeneratorTest;
import org.alfresco.repo.security.authentication.ResetPasswordServiceImplTest;
import org.alfresco.repo.security.authentication.UpgradePasswordHashTest;
import org.alfresco.repo.security.authentication.external.DefaultRemoteUserMapperTest;
import org.alfresco.repo.security.authentication.external.LocalAuthenticationServiceTest;
import org.alfresco.repo.security.authentication.subsystems.SubsystemChainingFtpAuthenticatorTest;
import org.alfresco.repo.security.authority.AuthorityBridgeTableAsynchronouslyRefreshedCacheTest;
import org.alfresco.repo.security.authority.AuthorityServiceTest;
import org.alfresco.repo.security.authority.DuplicateAuthorityTest;
import org.alfresco.repo.security.authority.ExtendedPermissionServiceTest;
import org.alfresco.repo.security.permissions.dynamic.LockOwnerDynamicAuthorityTest;
import org.alfresco.repo.security.permissions.impl.AclDaoComponentTest;
import org.alfresco.repo.security.permissions.impl.PermissionServiceTest;
import org.alfresco.repo.security.permissions.impl.ReadPermissionTest;
import org.alfresco.repo.security.permissions.impl.acegi.ACLEntryAfterInvocationTest;
import org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterTest;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSetTest;
import org.alfresco.repo.security.permissions.impl.model.PermissionModelTest;
import org.alfresco.repo.security.person.HomeFolderProviderSynchronizerTest;
import org.alfresco.repo.security.person.PersonTest;
import org.alfresco.repo.version.ContentServiceImplTest;
import org.alfresco.repo.version.NodeServiceImplTest;
import org.alfresco.repo.version.VersionServiceImplTest;
import org.alfresco.repo.version.common.VersionHistoryImplTest;
import org.alfresco.repo.version.common.VersionImplTest;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicyTest;
import org.alfresco.repo.workflow.StartWorkflowActionExecuterTest;
import org.alfresco.repo.workflow.WorkflowSuiteContextShutdownTest;
import org.alfresco.repo.workflow.activiti.ActivitiMultitenantWorkflowTest;
import org.alfresco.repo.workflow.activiti.ActivitiSpringTransactionTest;
import org.alfresco.repo.workflow.activiti.ActivitiTimerExecutionTest;
import org.alfresco.repo.workflow.activiti.ActivitiWorkflowServiceIntegrationTest;
import org.alfresco.repo.workflow.activiti.WorklfowObjectFactoryTest;
import org.alfresco.service.cmr.repository.TemporalSourceOptionsTest;
import org.alfresco.service.cmr.repository.TransformationOptionLimitsTest;
import org.alfresco.service.cmr.repository.TransformationOptionPairTest;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.NumericEncodingTest;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

public class AllRepositoryTestsCatalogue
{
    // no context - true JUNIT tests
    static void unitTestsNoContext(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.encryption.EncryptorTest.class);
        suite.addTestSuite(org.alfresco.encryption.KeyStoreKeyProviderTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.filesys.config.ServerConfigurationBeanTest.class));
        suite.addTestSuite(org.alfresco.filesys.repo.CIFSContentComparatorTest.class);
        suite.addTestSuite(org.alfresco.filesys.repo.rules.ShuffleTest.class);
        suite.addTestSuite(org.alfresco.repo.admin.Log4JHierarchyInitTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.attributes.PropTablesCleanupJobTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.cache.DefaultCacheFactoryTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.cache.DefaultSimpleCacheTest.class));
        suite.addTestSuite(org.alfresco.repo.cache.lookup.EntityLookupCacheTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.calendar.CalendarHelpersTest.class));
        suite.addTestSuite(org.alfresco.repo.dictionary.RepoDictionaryDAOTest.class);
        suite.addTestSuite(org.alfresco.repo.forms.processor.node.FieldProcessorTest.class);
        suite.addTestSuite(org.alfresco.repo.forms.processor.workflow.TaskFormProcessorTest.class);
        suite.addTestSuite(org.alfresco.repo.forms.processor.workflow.WorkflowFormProcessorTest.class);
        suite.addTestSuite(org.alfresco.repo.invitation.site.InviteSenderTest.class);
        suite.addTestSuite(org.alfresco.repo.invitation.site.InviteModeratedSenderTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.lock.LockUtilsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.lock.mem.LockStoreImplTest.class));
        suite.addTestSuite(org.alfresco.repo.module.ModuleDetailsImplTest.class);
        suite.addTestSuite(org.alfresco.repo.module.ModuleVersionNumberTest.class);
        suite.addTestSuite(org.alfresco.repo.module.tool.ModuleManagementToolTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.module.tool.WarHelperImplTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.module.tool.ModuleServiceImplTest.class));
        suite.addTestSuite(org.alfresco.repo.policy.MTPolicyComponentTest.class);
        suite.addTestSuite(org.alfresco.repo.policy.PolicyComponentTest.class);
        suite.addTestSuite(org.alfresco.repo.rendition.RenditionNodeManagerTest.class);
        suite.addTestSuite(org.alfresco.repo.rendition.RenditionServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.replication.ReplicationServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.service.StoreRedirectorProxyFactoryTest.class);
        suite.addTestSuite(org.alfresco.repo.site.RoleComparatorImplTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.thumbnail.ThumbnailServiceImplParameterTest.class));
        suite.addTestSuite(org.alfresco.repo.transfer.ContentChunkerImplTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.HttpClientTransmitterImplTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.manifest.TransferManifestTest.class);
        suite.addTestSuite(org.alfresco.repo.urlshortening.BitlyUrlShortenerTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.service.cmr.calendar.CalendarRecurrenceHelperTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.service.cmr.calendar.CalendarTimezoneHelperTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.tools.RenameUserTest.class));
        suite.addTestSuite(org.alfresco.util.FileNameValidatorTest.class);
        suite.addTestSuite(org.alfresco.util.HttpClientHelperTest.class);
        suite.addTestSuite(org.alfresco.util.JSONtoFmModelTest.class);
        suite.addTestSuite(org.alfresco.util.ModelUtilTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.PropertyMapTest.class));
        suite.addTestSuite(org.alfresco.util.ValueProtectingMapTest.class);
        suite.addTestSuite(org.alfresco.util.json.ExceptionJsonSerializerTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.collections.CollectionUtilsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.DbObjectXMLTransformerTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.DbPropertyTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.DefaultComparisonUtilsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.DifferenceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.MultiFileDumperTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.RedundantDbObjectTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.SchemaComparatorTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.SchemaToXMLTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.ValidatingVisitorTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.ValidationResultTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.XMLToSchemaTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.model.ColumnTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.model.ForeignKeyTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.model.IndexTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.model.PrimaryKeyTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.model.SchemaTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.model.SequenceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.model.TableTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.validator.IndexColumnsValidatorTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.validator.NameValidatorTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.validator.SchemaVersionValidatorTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.validator.TypeNameOnlyValidatorTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.test.junitrules.TemporaryMockOverrideTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.solr.SolrQueryHTTPClientTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.solr.SolrStatsResultTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.solr.facet.SolrFacetComparatorTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.solr.facet.FacetQNameUtilsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.BeanExtenderUnitTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.solr.SpellCheckDecisionManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.solr.SolrStoreMappingWrapperTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.security.authentication.CompositePasswordEncoderTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.security.authentication.PasswordHashingTest.class));
        suite.addTest(org.alfresco.traitextender.TraitExtenderUnitTestSuite.suite());
        suite.addTest(org.alfresco.repo.virtual.VirtualizationUnitTestSuite.suite());
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.security.authentication.AuthenticationServiceImplTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.EmailHelperTest.class));
        suite.addTest(new JUnit4TestAdapter(ParameterDefinitionImplTest.class));
        suite.addTest(new JUnit4TestAdapter(ActionDefinitionImplTest.class));
        suite.addTest(new JUnit4TestAdapter(ActionConditionDefinitionImplTest.class));
        suite.addTest(new JUnit4TestAdapter(ActionImplTest.class));
        suite.addTest(new JUnit4TestAdapter(ActionConditionImplTest.class));
        suite.addTest(new JUnit4TestAdapter(CompositeActionImplTest.class));
        suite.addTest(new JUnit4TestAdapter(CompositeActionConditionImplTest.class));

        suite.addTestSuite(AuditableAnnotationTest.class);
        suite.addTest(new JUnit4TestAdapter(PropertyAuditFilterTest.class));

        suite.addTest(new JUnit4TestAdapter(SpoofedTextContentReaderTest.class));
        suite.addTestSuite(ContentDataTest.class);

        suite.addTest(new JUnit4TestAdapter(TransformationOptionLimitsTest.class));
        suite.addTest(new JUnit4TestAdapter(TransformationOptionPairTest.class));
        // Transformer Config
        suite.addTest(new JUnit4TestAdapter(TransformerConfigTestSuite.class));
        // Source options
        suite.addTest(new JUnit4TestAdapter(TemporalSourceOptionsTest.class));
        suite.addTest(new JUnit4TestAdapter(MetadataExtracterLimitsTest.class));

        suite.addTest(new JUnit4TestAdapter(StandardQuotaStrategyMockTest.class));
        suite.addTest(new JUnit4TestAdapter(UnlimitedQuotaStrategyTest.class));
        suite.addTest(new JUnit4TestAdapter(CachingContentStoreTest.class));
        suite.addTest(new JUnit4TestAdapter(ContentCacheImplTest.class));

        suite.addTest(new JUnit4TestAdapter(PropertyTypeConverterTest.class));

        suite.addTestSuite(MLAnaysisModeExpansionTest.class);
        suite.addTestSuite(DocumentNavigatorTest.class);
        suite.addTestSuite(MultiReaderTest.class);
        suite.addTestSuite(IndexInfoTest.class);

        // TODO  update reference in the context file map report
        suite.addTestSuite(NumericEncodingTest.class);
        // TODO  update reference in the context file map report
        suite.addTestSuite(CMIS_FTSTest.class);
        // TODO  update reference in the context file map report
        suite.addTestSuite(org.alfresco.repo.search.impl.parsers.CMISTest.class);
        // TODO  update reference in the context file map report
        suite.addTestSuite(FTSTest.class);

        suite.addTest(new JUnit4TestAdapter(AlfrescoSSLSocketFactoryTest.class));
        suite.addTestSuite(AuthorizationTest.class); 
        suite.addTestSuite(FilteringResultSetTest.class);
        suite.addTestSuite(ChainingAuthenticationServiceTest.class); 
        suite.addTestSuite(NameBasedUserNameGeneratorTest.class);

        suite.addTestSuite(VersionImplTest.class);
        suite.addTestSuite(VersionHistoryImplTest.class); 
        suite.addTestSuite(SerialVersionLabelPolicyTest.class); 

        suite.addTestSuite(WorklfowObjectFactoryTest.class); 
        suite.addTestSuite(WorkflowSuiteContextShutdownTest.class); 

        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.lucene.analysis.PathTokenFilterTest.class));
    }

    /**
     * 
     * various tests that do use context files but do not fit into other main context files patterns
     */
    static void miscContext_999(TestSuite suite)
    {
        // context files: [classpath:alfresco/node-locator-context.xml, classpath:test-nodeLocatorServiceImpl-context.xml]
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.nodelocator.NodeLocatorServiceImplTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/minimal-context.xml]
    static void applicationContext_minimalContext_01(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.RepositoryStartStopTest.class);
    }

    // [classpath:alfresco/application-context.xml] - part 1
    static void applicationContext_01(TestSuite suite)
    {
        // was 09 (split)
        suite.addTestSuite(IsSubTypeEvaluatorTest.class);
        suite.addTestSuite(ComparePropertyValueEvaluatorTest.class);
        suite.addTestSuite(CompareMimeTypeEvaluatorTest.class);
        suite.addTestSuite(HasAspectEvaluatorTest.class);
        suite.addTestSuite(SetPropertyValueActionExecuterTest.class);
        suite.addTestSuite(AddFeaturesActionExecuterTest.class);
        suite.addTestSuite(ContentMetadataExtracterTest.class);
        suite.addTestSuite(ContentMetadataExtracterTagMappingTest.class);
        suite.addTestSuite(ContentMetadataEmbedderTest.class);
        suite.addTestSuite(SpecialiseTypeActionExecuterTest.class);
        suite.addTestSuite(RemoveFeaturesActionExecuterTest.class);
        suite.addTestSuite(ActionTrackingServiceImplTest.class); // intermittent - pending ALF-9773 & ALF-9774
        
        
        suite.addTestSuite(org.alfresco.email.server.EmailServiceImplTest.class);
        suite.addTestSuite(org.alfresco.email.server.EmailServerTest.class);
        //suite.addTestSuite(org.alfresco.filesys.FTPServerTest.class);
        suite.addTestSuite(org.alfresco.filesys.repo.CifsIntegrationTest.class);
        suite.addTestSuite(org.alfresco.filesys.repo.ContentDiskDriverTest.class);

        // was 04
        suite.addTestSuite(org.alfresco.filesys.repo.LockKeeperImplTest.class);

        // was 08
        suite.addTestSuite(org.alfresco.opencmis.search.OpenCmisQueryTest.class);
        
        // was 10
        suite.addTestSuite(org.alfresco.repo.activities.ActivityServiceImplTest.class);
        //suite.addTestSuite(org.alfresco.repo.admin.patch.PatchTest.class); //moved in the next part
        suite.addTestSuite(org.alfresco.repo.admin.registry.RegistryServiceImplTest.class);
    }

    // [classpath:alfresco/application-context.xml] - part 2
    static void applicationContext_02(TestSuite suite)
    {

        suite.addTestSuite(org.alfresco.repo.attributes.AttributeServiceTest.class); // there is a test that runs for 40s and one
                                                                                     // 184s

        // was 11 (part)
        suite.addTestSuite(AuditableAspectTest.class);
        suite.addTestSuite(AuditBootstrapTest.class);
        suite.addTestSuite(AuditComponentTest.class);
        suite.addTestSuite(UserAuditFilterTest.class);
        suite.addTestSuite(AuditMethodInterceptorTest.class);
        suite.addTest(new JUnit4TestAdapter(AccessAuditorTest.class));

        // was 10
        // the following test will lock up the DB if run after part 1 - in the same suite
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.activities.feed.FeedNotifierTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.activities.feed.FeedNotifierJobTest.class));
        suite.addTestSuite(org.alfresco.repo.admin.RepoAdminServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.admin.patch.PatchTest.class);

        // was 13
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.bulkimport.impl.StripingFilesystemTrackerTest.class));

        // was 14
        suite.addTestSuite(org.alfresco.repo.coci.CheckOutCheckInServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.configuration.ConfigurableServiceImplTest.class);

        // was 15 (part)
        suite.addTestSuite(GuessMimetypeTest.class);
        // suite.addTestSuite(CharsetFinderTest.class);
        suite.addTest(new JUnit4TestAdapter(FileContentStoreTest.class));
        suite.addTest(new JUnit4TestAdapter(NoRandomAccessFileContentStoreTest.class));
        suite.addTest(new JUnit4TestAdapter(ReadOnlyFileContentStoreTest.class));
        // suite.addTestSuite(MimetypeMapTest.class);
        suite.addTest(new JUnit4TestAdapter(RoutingContentStoreTest.class));

        try
        {
            @SuppressWarnings("rawtypes")
            Class clazz = Class.forName("org.alfresco.repo.content.routing.StoreSelectorAspectContentStoreTest");
            suite.addTestSuite(clazz);
        }
        catch (Throwable ignore)
        {
            // Ignore
        }
    }

    // [classpath:alfresco/application-context.xml] - part 3
    static void applicationContext_03(TestSuite suite)
    {
        // was 18
        suite.addTestSuite(org.alfresco.repo.copy.CopyServiceImplTest.class);

        // was 21 (part)
        suite.addTestSuite(org.alfresco.repo.descriptor.DescriptorServiceTest.class);
        suite.addTestSuite(org.alfresco.repo.dictionary.DictionaryModelTypeTest.class);
        suite.addTestSuite(org.alfresco.repo.dictionary.DictionaryRepositoryBootstrapTest.class);

        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.dictionary.ModelValidatorTest.class));
        suite.addTestSuite(org.alfresco.repo.dictionary.types.period.PeriodTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.discussion.DiscussionServiceImplTest.class));

        // was 22
        suite.addTest(new JUnit4TestAdapter(NodeDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(ContentDataDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(EncodingDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(LockDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(MimetypeDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(LocaleDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(QNameDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(PropertyValueDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(AppliedPatchDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(AclCrudDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(UsageDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(SOLRDAOTest.class));
        suite.addTest(new JUnit4TestAdapter(TenantAdminDAOTest.class));
        // REOPO-1012 : run AuditDAOTest and PropertyValueCleanupTest near the end
        // because their failure can cause other tests to fail on MS SQL
        // AuditDAOTest fails if it runs after CannedQueryDAOTest so this order is a compromise
        // CannedQueryDAOTest will fail on MS SQL if either AuditDAOTest or PropertyValueCleanupTest fail
        suite.addTest(new JUnit4TestAdapter(PropertyValueCleanupTest.class));
        suite.addTest(new JUnit4TestAdapter(AuditDAOTest.class));

        // was 23 (part)
        suite.addTestSuite(org.alfresco.repo.exporter.ExporterComponentTest.class);
        suite.addTestSuite(org.alfresco.repo.exporter.RepositoryExporterComponentTest.class);

        // was 24 (part)
        suite.addTestSuite(org.alfresco.repo.forms.FormServiceImplTest.class);

        // was 28 (part)
        suite.addTestSuite(org.alfresco.repo.i18n.MessageServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.imap.ImapMessageTest.class);
        suite.addTestSuite(org.alfresco.repo.imap.ImapServiceImplCacheTest.class);
        suite.addTestSuite(org.alfresco.repo.imap.ImapServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.importer.FileImporterTest.class);
        suite.addTestSuite(org.alfresco.repo.importer.ImporterComponentTest.class);

        // was 29 (part)
        suite.addTestSuite(org.alfresco.repo.jscript.PeopleTest.class);
        suite.addTestSuite(org.alfresco.repo.jscript.RhinoScriptTest.class);
        suite.addTestSuite(org.alfresco.repo.jscript.ScriptBehaviourTest.class);

        // was 30
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.links.LinksServiceImplTest.class)); // fails if run more than once
        suite.addTestSuite(org.alfresco.repo.lock.JobLockServiceTest.class);
        suite.addTestSuite(org.alfresco.repo.lock.LockBehaviourImplTest.class);
        suite.addTestSuite(org.alfresco.repo.lock.LockServiceImplTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.lock.mem.LockStoreImplTxTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.lock.mem.LockableAspectInterceptorTest.class));
        suite.addTestSuite(org.alfresco.repo.management.JmxDumpUtilTest.class);

        // was 31
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.model.ModelTestSuite.class));
    }

    // [classpath:alfresco/application-context.xml] - part 4
    static void applicationContext_04(TestSuite suite)
    {
        // was 32 (part)
        suite.addTestSuite(org.alfresco.repo.module.ModuleComponentHelperTest.class);
        suite.addTestSuite(org.alfresco.repo.node.ConcurrentNodeServiceSearchTest.class);
        suite.addTestSuite(org.alfresco.repo.node.ConcurrentNodeServiceTest.class);
        suite.addTestSuite(org.alfresco.repo.node.FullNodeServiceTest.class);
        suite.addTestSuite(org.alfresco.repo.node.NodeRefPropertyMethodInterceptorTest.class);

        // was 34
        suite.addTestSuite(org.alfresco.repo.node.PerformanceNodeServiceTest.class);
        suite.addTestSuite(org.alfresco.repo.node.archive.ArchiveAndRestoreTest.class);

        // was 35
        suite.addTestSuite(org.alfresco.repo.node.archive.LargeArchiveAndRestoreTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.node.cleanup.TransactionCleanupTest.class));
        suite.addTestSuite(org.alfresco.repo.node.db.DbNodeServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.node.db.DbNodeServiceImplPropagationTest.class);

        // was 36
        suite.addTestSuite(org.alfresco.repo.node.getchildren.GetChildrenCannedQueryTest.class);
        suite.addTestSuite(org.alfresco.repo.node.index.FullIndexRecoveryComponentTest.class);
        suite.addTestSuite(org.alfresco.repo.node.index.IndexTransactionTrackerTest.class);

        // was 37
        suite.addTestSuite(org.alfresco.repo.node.index.MissingContentReindexComponentTest.class);

        // was 38 (part)
        suite.addTestSuite(org.alfresco.repo.node.integrity.IncompleteNodeTaggerTest.class);
        suite.addTestSuite(org.alfresco.repo.node.integrity.IntegrityTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.oauth1.OAuth1CredentialsStoreServiceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.oauth2.OAuth2CredentialsStoreServiceTest.class));
        suite.addTestSuite(org.alfresco.repo.policy.PolicyComponentTransactionTest.class);

        // was 39 (part)
        suite.addTest(new JUnit4TestAdapter(ChannelServiceImplIntegratedTest.class));
        suite.addTest(new JUnit4TestAdapter(PublishingRootObjectTest.class));
        // suite.addTest(new JUnit4TestAdapter(EnvironmentImplTest.class));
        suite.addTest(new JUnit4TestAdapter(PublishingQueueImplTest.class));
        suite.addTest(new JUnit4TestAdapter(PublishingPackageSerializerTest.class));
        // suite.addTest(new JUnit4TestAdapter(PublishEventActionTest.class));
        suite.addTest(new JUnit4TestAdapter(PublishingIntegratedTest.class));

    }

    // [classpath:alfresco/application-context.xml] - part 5
    static void applicationContext_05(TestSuite suite)
    {
        // was 40 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.client.config.ClientAppConfigTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.rendition.MultiUserRenditionTest.class));
        suite.addTestSuite(org.alfresco.repo.rendition.RenditionServiceIntegrationTest.class);

        // was 41
        suite.addTestSuite(org.alfresco.repo.rendition.StandardRenditionLocationResolverTest.class);
        suite.addTestSuite(org.alfresco.repo.rendition.executer.HTMLRenderingEngineTest.class);

        // was 42 (part)
        suite.addTestSuite(org.alfresco.repo.rendition.executer.XSLTFunctionsTest.class); // fails if with previous tests
        suite.addTestSuite(org.alfresco.repo.rendition.executer.XSLTRenderingEngineTest.class);
        suite.addTestSuite(org.alfresco.repo.replication.ReplicationServiceIntegrationTest.class);

        // was 43
        suite.addTestSuite(org.alfresco.repo.rule.RuleLinkTest.class); // fails if with previous tests
        suite.addTestSuite(org.alfresco.repo.rule.RuleServiceCoverageTest.class);
        suite.addTestSuite(org.alfresco.repo.rule.RuleServiceImplTest.class);

        // was 45
        suite.addTestSuite(org.alfresco.repo.rule.RuleTypeImplTest.class);
        suite.addTestSuite(org.alfresco.repo.rule.ruletrigger.RuleTriggerTest.class);

        // was 46 (part)
        suite.addTestSuite(QueryRegisterComponentTest.class);
        suite.addTestSuite(SearcherComponentTest.class);
        suite.addTestSuite(SearchServiceTest.class);
        suite.addTestSuite(DocumentNavigatorTest.class);
        suite.addTestSuite(ADMLuceneCategoryTest.class);
        suite.addTestSuite(ADMLuceneTest.class);
        suite.addTestSuite(ALF947Test.class);
        suite.addTestSuite(LuceneIndexBackupComponentTest.class);
        suite.addTestSuite(CMIS_FTSTest.class);

        // was 47 (part)
        suite.addTestSuite(AuthenticationBootstrapTest.class);
        suite.addTestSuite(AuthorityServiceTest.class);
        suite.addTestSuite(DuplicateAuthorityTest.class);
        suite.addTestSuite(ExtendedPermissionServiceTest.class);
        suite.addTestSuite(LockOwnerDynamicAuthorityTest.class);
        suite.addTestSuite(AclDaoComponentTest.class);
        suite.addTestSuite(PermissionServiceTest.class);
        suite.addTestSuite(ACLEntryAfterInvocationTest.class);
        suite.addTestSuite(ACLEntryVoterTest.class);
        suite.addTestSuite(PermissionModelTest.class);
        suite.addTestSuite(PersonTest.class);
        suite.addTestSuite(OwnableServiceTest.class);
        suite.addTestSuite(ReadPermissionTest.class);
        suite.addTestSuite(AuthorizationTest.class);
        suite.addTestSuite(UpgradePasswordHashTest.class);
        suite.addTestSuite(AuthorityBridgeTableAsynchronouslyRefreshedCacheTest.class);
        suite.addTest(new JUnit4TestAdapter(HomeFolderProviderSynchronizerTest.class));
        suite.addTestSuite(FixedAclUpdaterTest.class);
        suite.addTestSuite(DefaultRemoteUserMapperTest.class);
        suite.addTestSuite(SubsystemChainingFtpAuthenticatorTest.class);
        suite.addTest(new JUnit4TestAdapter(LocalAuthenticationServiceTest.class));

        // was 50
        suite.addTestSuite(org.alfresco.repo.solr.SOLRTrackingComponentTest.class);

        // was 53
        suite.addTestSuite(org.alfresco.repo.tagging.TaggingServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.tagging.UpdateTagScopesActionExecuterTest.class);

        // was 55
        suite.addTestSuite(org.alfresco.repo.template.TemplateServiceImplTest.class);

        // was 56 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.tenant.MultiTServiceImplTest.class));

        // was 57 (part)
        suite.addTestSuite(org.alfresco.repo.tenant.MultiTNodeServiceInterceptorTest.class);
        suite.addTestSuite(org.alfresco.repo.template.XSLTProcessorTest.class);
        suite.addTestSuite(org.alfresco.repo.thumbnail.conditions.NodeEligibleForRethumbnailingEvaluatorTest.class);
        suite.addTestSuite(org.alfresco.repo.transaction.AlfrescoTransactionSupportTest.class);
        suite.addTestSuite(org.alfresco.repo.transaction.RetryingTransactionHelperTest.class);
        suite.addTestSuite(org.alfresco.repo.transaction.TransactionAwareSingletonTest.class);
        suite.addTestSuite(org.alfresco.repo.transaction.TransactionServiceImplTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.transaction.ConnectionPoolOverloadTest.class));
        suite.addTestSuite(org.alfresco.repo.transfer.NodeCrawlerTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.RepoTransferReceiverImplTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.TransferServiceCallbackTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.TransferServiceImplTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.TransferServiceToBeRefactoredTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.TransferVersionCheckerImplTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.manifest.ManifestIntegrationTest.class);
        suite.addTestSuite(org.alfresco.repo.transfer.script.ScriptTransferServiceTest.class);
        suite.addTestSuite(org.alfresco.repo.action.scheduled.CronScheduledQueryBasedTemplateActionDefinitionTest.class);
    }

    // [classpath:alfresco/application-context.xml] - part 6
    static void applicationContext_06(TestSuite suite)
    {
        // was 58
        suite.addTest(org.alfresco.repo.usage.UsageTestSuite.suite());

        // was 59 (part)
        suite.addTestSuite(VersionServiceImplTest.class);
        suite.addTestSuite(NodeServiceImplTest.class);
        suite.addTestSuite(ContentServiceImplTest.class);

        // was 61 (part)
        suite.addTestSuite(StartWorkflowActionExecuterTest.class);
        suite.addTestSuite(ActivitiWorkflowServiceIntegrationTest.class);
        suite.addTestSuite(ActivitiSpringTransactionTest.class);
        suite.addTestSuite(ActivitiTimerExecutionTest.class);

        // was 63 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.DbToXMLTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.ExportDbTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.schemacomp.SchemaReferenceFileTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.CronTriggerBeanSystemTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.filesys.auth.cifs.CifsAuthenticatorPassthruTest.class));

        // was 65
        suite.addTestSuite(org.alfresco.repo.content.transform.DifferrentMimeTypeTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.attributes.PropTablesCleanupJobIntegrationTest.class));
        suite.addTestSuite(org.alfresco.repo.tagging.UpdateTagScopesActionExecuterTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.service.ServiceRegistryTest.class));

        // was 66 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.solr.facet.SolrFacetConfigTest.class));

        // was 67
        suite.addTestSuite(org.alfresco.repo.doclink.DocumentLinkServiceImplTest.class);

        // was 71
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.invitation.ActivitiInvitationServiceImplTests.class));

        // was 68
        // This test opens, closes and again opens the alfresco application context.
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.dictionary.CustomModelRepoRestartTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:test-cmisinteger_modell-context.xml]
    static void applicationContext_cmisIntegerModel1Context_06(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.opencmis.CMISTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:opencmis/opencmistest-context.xml]
    static void applicationContext_openCmisContext_07(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.opencmis.OpenCmisLocalTest.class);
    }

    // [no_app_context] AND [no_app_context][use_mocks]
    static void noContext_09(TestSuite suite)
    {
        suite.addTestSuite(ParameterDefinitionImplTest.class);
        suite.addTestSuite(ActionDefinitionImplTest.class);
        suite.addTestSuite(ActionConditionDefinitionImplTest.class);
        suite.addTestSuite(ActionImplTest.class);
        suite.addTestSuite(ActionConditionImplTest.class);
        suite.addTestSuite(CompositeActionImplTest.class);
        suite.addTestSuite(CompositeActionConditionImplTest.class);

        // was 11
        suite.addTestSuite(AuditableAnnotationTest.class);
        suite.addTest(new JUnit4TestAdapter(PropertyAuditFilterTest.class));

        // was 15 (part)
        suite.addTest(new JUnit4TestAdapter(SpoofedTextContentReaderTest.class));
        suite.addTestSuite(ContentDataTest.class);

        // was 16 (part)
        suite.addTest(new JUnit4TestAdapter(TransformerConfigTestSuite.class));
        suite.addTest(new JUnit4TestAdapter(MetadataExtracterLimitsTest.class));

        // was 17 (part)
        suite.addTest(new JUnit4TestAdapter(UnlimitedQuotaStrategyTest.class));
        suite.addTest(new JUnit4TestAdapter(StandardQuotaStrategyMockTest.class));
        suite.addTest(new JUnit4TestAdapter(ConcurrentCachingStoreTest.class));
        suite.addTest(new JUnit4TestAdapter(SlowContentStoreTest.class));

        // was 22 (part)
        suite.addTest(new JUnit4TestAdapter(PropertyTypeConverterTest.class));

        // was 46 (part)
        suite.addTestSuite(MLAnaysisModeExpansionTest.class);
        suite.addTestSuite(MultiReaderTest.class);
        suite.addTestSuite(IndexInfoTest.class);

        // was 47 (part)
        suite.addTestSuite(AuthenticationTest.class);
        suite.addTestSuite(ChainingAuthenticationServiceTest.class);
        suite.addTestSuite(NameBasedUserNameGeneratorTest.class);
        suite.addTestSuite(FilteringResultSetTest.class);
        suite.addTest(new JUnit4TestAdapter(AlfrescoSSLSocketFactoryTest.class));

        // was 59 (part)
        suite.addTestSuite(VersionImplTest.class);
        suite.addTestSuite(VersionHistoryImplTest.class);
        suite.addTestSuite(SerialVersionLabelPolicyTest.class);

        // was 61 (part)
        suite.addTestSuite(WorklfowObjectFactoryTest.class);

        // was 63 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.lucene.analysis.PathTokenFilterTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:org/alfresco/repo/action/test-action-services-context.xml]
    static void applicationContext_testServiceActionServiceContext_10(TestSuite suite)
    {
        // was 09 (split)
        suite.addTestSuite(ActionServiceImplTest.class);
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/test/global-integration-test-context.xml]
    static void applicationContext_globalIntegrationTestContext_11(TestSuite suite)
    {
        // was 09 (part)
        suite.addTest(new JUnit4TestAdapter(MailActionExecuterTest.class));
        suite.addTest(new JUnit4TestAdapter(ActionServiceImpl2Test.class));
        suite.addTest(new JUnit4TestAdapter(ImporterActionExecuterTest.class));

        // was 21 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.dictionary.CustomModelServiceImplTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.dictionary.ValueDataTypeValidatorImplTest.class));

        // was 22 (part)
        suite.addTest(new JUnit4TestAdapter(CannedQueryDAOTest.class));

        // was 23 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.download.DownloadServiceIntegrationTest.class));

        // was 27
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.forum.CommentsTest.class));

        // was 28 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.invitation.InvitationCleanupTest.class));

        // was 29 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.jscript.ScriptNodeTest.class));

        // was 38 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.preference.PreferenceServiceImplTest.class));

        // was 40 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.quickshare.QuickShareServiceIntegrationTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.remotecredentials.RemoteCredentialsServicesTest.class));

        // was 42 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.rule.MiscellaneousRulesTest.class));

        // was 44
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.rule.RuleServiceIntegrationTest.class)); // fails locally even on
                                                                                                       // its own

        // was 47 (part)
        suite.addTest(new JUnit4TestAdapter(ResetPasswordServiceImplTest.class));

        // was 51
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.subscriptions.SubscriptionServiceActivitiesTest.class));

        // was 63 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.test.junitrules.AlfrescoPersonTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.test.junitrules.TemporaryNodesTest.class));

        // was 66 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.solr.facet.SolrFacetQueriesDisplayHandlersTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.search.impl.solr.facet.SolrFacetServiceImplTest.class));
    }


    // [classpath:alfresco/application-context.xml, classpath:cache-test/cache-test-context.xml]
    static void applicationContext_cacheTestContext_14(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.cache.CacheTest.class);
    }



    // [classpath:alfresco/minimal-context.xml]
    static void minimalContext_16(TestSuite suite)
    {
        // was 16 (part)
        suite.addTest(org.alfresco.repo.content.ContentMinimalContextTestSuite.suite());

        suite.addTest(new JUnit4TestAdapter(CachingContentStoreTest.class));
        suite.addTest(new JUnit4TestAdapter(ContentCacheImplTest.class));

        // was 52 (part)
        suite.addTestSuite(org.alfresco.repo.content.metadata.MappingMetadataExtracterTest.class);
    }


    // [classpath:cachingstore/test-context.xml]
    static void cachingstoreTestContext_18(TestSuite suite)
    {
        // was 17 (part)
        suite.addTest(new JUnit4TestAdapter(FullTest.class));
    }

    // [classpath:cachingstore/test-cleaner-context.xml]
    static void cachingstoreTestCleanerContext_21(TestSuite suite)
    {
        // was 17 (part)
        suite.addTest(new JUnit4TestAdapter(CachedContentCleanupJobTest.class));
    }

    // [classpath:cachingstore/test-std-quota-context.xml]
    static void cachingstoreTestStdQuotaContext_22(TestSuite suite)
    {
        // was 17 (part)
        suite.addTest(new JUnit4TestAdapter(StandardQuotaStrategyTest.class));
    }

    // [classpath:cachingstore/test-slow-context.xml]
    static void cachingstoreTestSlowContext_23(TestSuite suite)
    {
        // was 17 (part)
        suite.addTest(new JUnit4TestAdapter(CachingContentStoreTest.class));
        suite.addTest(new JUnit4TestAdapter(ContentCacheImplTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:org/alfresco/repo/forms/MNT-7383-context.xml]
    static void applicationContext_mnt7383Context_24(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.forms.processor.action.ActionFormProcessorTest.class));
    }



    // [classpath:org/alfresco/repo/jscript/test-context.xml]
    static void jscriptTestContext_29(TestSuite suite)
    {
        // was 29 (part)
        suite.addTestSuite(org.alfresco.repo.jscript.ScriptBehaviourTest.class);
    }



    // [module/module-component-test-beans.xml]
    static void moduleComponentTestBeansContext_32(TestSuite suite)
    {
        // was 32 (part)
        suite.addTestSuite(org.alfresco.repo.module.ComponentsTest.class);
    }

    // [classpath*:alfresco/ibatis/ibatis-test-context.xml, classpath:alfresco/application-context.xml,
    // classpath:alfresco/test/global-integration-test-context.xml]
    static void ibatisTestContext_applicationContext_globalIntegrationTestContext_33(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.node.NodeServiceTest.class));
    }



    // TODO can we remove this? Was it EOLed?
    // [classpath:test/alfresco/test-web-publishing-context.xml]
    static void testWebPublishingContext_39(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(ChannelServiceImplTest.class));
        suite.addTest(new JUnit4TestAdapter(PublishingEventHelperTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/test/global-integration-test-context.xml,
    // classpath:ratings/test-RatingServiceIntegrationTest-context.xml]
    static void applicationContext_globalIntegrationTestContext_testRatingServiceIntegrationTestContext_40(TestSuite suite)
    {
        // was 40 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.rating.RatingServiceIntegrationTest.class));
    }

    // [classpath:/test/alfresco/test-renditions-context.xml, classpath:alfresco/application-context.xml,
    // classpath:alfresco/test/global-integration-test-context.xml]
    static void testRenditionsContext_applicationContext_globalIntegrationTestContext_41(TestSuite suite)
    {
        // was 40 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.rendition.RenditionServicePermissionsTest.class));
    }


    // [to_investigate][alfresco-data-model-6.16-tests.jar]
    static void dataModel6_16TestsContext_46(TestSuite suite)
    {
        // was 46 (part)
        suite.addTestSuite(NumericEncodingTest.class);
        suite.addTestSuite(CMISTest.class);
        suite.addTestSuite(FTSTest.class);
    }


    // [classpath:alfresco/application-context.xml, classpath:sync-test-context.xml]
    static void applicationContext_syncTestContext_48(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.security.sync.ChainingUserRegistrySynchronizerTest.class);
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/test/global-integration-test-context.xml,
    // classpath:sites/test-TemporarySitesTest-context.xml]
    static void applicationContext_globalIntegrationTestContext_testTemporarySitesTestContext_49(TestSuite suite)
    {
        // was 49 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.site.SiteServiceImplMoreTest.class));

        // was 63 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.test.junitrules.TemporarySitesTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:org/alfresco/repo/site/site-custom-context.xml]
    static void applicationContext_siteCustomContext_50(TestSuite suite)
    {
        // was 49 (part)
        suite.addTestSuite(org.alfresco.repo.site.SiteServiceImplTest.class);
    }


    // TODO can we remove this? Was it EOLed?
    // [classpath:alfresco/application-context.xml, classpath:test/alfresco/test-subscriptions-context.xml]
    static void applicationContext_testSubscriptionsContext_52(TestSuite suite)
    {
        // was 52 (part)
        suite.addTestSuite(org.alfresco.repo.subscriptions.SubscriptionServiceImplTest.class);
    }



    // [classpath:alfresco/application-context.xml, classpath:tenant/mt-*context.xml]
    static void applicationContext_mtAllContext_56(TestSuite suite)
    {
        // was 56 (part)
        suite.addTestSuite(org.alfresco.repo.tenant.MultiTDemoTest.class);

        // was 61 (part)
        suite.addTestSuite(ActivitiMultitenantWorkflowTest.class);
    }

    // [classpath:alfresco/application-context.xml, classpath:org/alfresco/repo/thumbnail/test-thumbnail-context.xml]
    static void applicationContext_testThumnailContext_57(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.repo.thumbnail.ThumbnailServiceImplTest.class);
    }

    // TODO investigate
    // [to_investigate][one_test][close_context]
    static void oneTestCloseContext_61(TestSuite suite)
    {
        // was 61 (part)
        // This test will force the application context properly, which avoids
        // periodic wierd build failures
        ApplicationContextHelper.getApplicationContext();
        suite.addTestSuite(WorkflowSuiteContextShutdownTest.class);
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/filesys/auth/cifs/test-kerberos-context.xml]
    static void applicationContext_testKerberosCntext_63(TestSuite suite)
    {
        // was 63 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.filesys.auth.cifs.CifsAuthenticatorKerberosTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:scriptexec/script-exec-test.xml]
    static void applicationContext_scriptExecTestContext_65(TestSuite suite)
    {
        // was 63
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.domain.schema.script.ScriptExecutorImplIntegrationTest.class));
        suite.addTest(
                new JUnit4TestAdapter(org.alfresco.repo.domain.schema.script.ScriptBundleExecutorImplIntegrationTest.class));
    }

    // [alfresco/scheduler-core-context.xml, org/alfresco/util/test-scheduled-jobs-context.xml]
    static void schedulerCoreContext_testScheduledJobsContext_66(TestSuite suite)
    {
        // was 63 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.CronTriggerBeanTest.class));
    }

    // [classpath:alfresco/application-context.xml, classpath:alfresco/test/global-integration-test-context.xml,
    // classpath:org/alfresco/util/test/junitrules/dummy1-context.xml,
    // classpath:org/alfresco/util/test/junitrules/dummy2-context.xml]
    static void applicationContext_globalIntegrationTestContext_dummy1Context_dummy2Context_67(TestSuite suite)
    {
        // was 63 (part)
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.test.junitrules.ApplicationContextInitTest.class));
    }


    // [classpath:alfresco/application-context.xml,
    // classpath:org/alfresco/repo/policy/annotation/test-qname-type-editor-context.xml]
    static void applicationContext_testQnameTypeEditorContext_69(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.policy.annotation.QNameTypeEditorTest.class));
    }

    // [classpath:**/virtualization-test-context.xml, classpath:alfresco/application-context.xml]
    static void virtualizationTestContext_applicationContext_70(TestSuite suite)
    {
        suite.addTest(org.alfresco.repo.virtual.VirtualizationIntegrationTestSuite.suite());
    }

}
