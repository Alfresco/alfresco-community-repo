/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 * All Repository project UNIT test classes (no application context) should be added to this test suite.
 * Tests marked as DBTests are automatically excluded and are run as part of {@link AllDBTestsTestSuite}.
 */
@RunWith(Categories.class)
@Categories.ExcludeCategory({DBTests.class, NonBuildTests.class})
@Suite.SuiteClasses({
    org.alfresco.repo.site.SiteMembershipTest.class,
    org.alfresco.encryption.EncryptorTest.class,
    org.alfresco.encryption.KeyStoreKeyProviderTest.class,
    org.alfresco.filesys.config.ServerConfigurationBeanTest.class,
    org.alfresco.filesys.repo.rules.ShuffleTest.class,
    org.alfresco.opencmis.AlfrescoCmisExceptionInterceptorTest.class,
    org.alfresco.repo.admin.Log4JHierarchyInitTest.class,
    org.alfresco.repo.attributes.PropTablesCleanupJobTest.class,
    org.alfresco.repo.cache.AbstractCacheFactoryTest.class,
    org.alfresco.repo.cache.DefaultCacheFactoryTest.class,
    org.alfresco.repo.cache.DefaultSimpleCacheTest.class,
    org.alfresco.repo.cache.InMemoryCacheStatisticsTest.class,
    org.alfresco.repo.cache.TransactionStatsTest.class,
    org.alfresco.repo.cache.lookup.EntityLookupCacheTest.class,
    org.alfresco.repo.calendar.CalendarHelpersTest.class,
    org.alfresco.repo.copy.CopyServiceImplUnitTest.class,
    org.alfresco.repo.dictionary.RepoDictionaryDAOTest.class,
    org.alfresco.repo.forms.processor.node.FieldProcessorTest.class,
    org.alfresco.repo.forms.processor.workflow.TaskFormProcessorTest.class,
    org.alfresco.repo.forms.processor.workflow.WorkflowFormProcessorTest.class,
    org.alfresco.repo.invitation.site.InviteSenderTest.class,
    org.alfresco.repo.invitation.site.InviteModeratedSenderTest.class,
    org.alfresco.repo.jscript.ScriptSearchTest.class,
    org.alfresco.repo.lock.LockUtilsTest.class,
    org.alfresco.repo.lock.mem.LockStoreImplTest.class,
    org.alfresco.repo.management.subsystems.CryptodocSwitchableApplicationContextFactoryTest.class,
    org.alfresco.repo.module.ModuleDetailsImplTest.class,
    org.alfresco.repo.module.ModuleVersionNumberTest.class,
    org.alfresco.repo.node.integrity.IntegrityEventTest.class,
    org.alfresco.repo.policy.MTPolicyComponentTest.class,
    org.alfresco.repo.policy.PolicyComponentTest.class,
    org.alfresco.repo.rendition.RenditionNodeManagerTest.class,
    org.alfresco.repo.rendition.RenditionServiceImplTest.class,
    org.alfresco.repo.replication.ReplicationServiceImplTest.class,
    org.alfresco.repo.service.StoreRedirectorProxyFactoryTest.class,
    org.alfresco.repo.site.RoleComparatorImplTest.class,
    org.alfresco.repo.tenant.MultiTAdminServiceImplTest.class,
    org.alfresco.repo.thumbnail.ThumbnailServiceImplParameterTest.class,
    org.alfresco.repo.transfer.ContentChunkerImplTest.class,
    org.alfresco.repo.transfer.HttpClientTransmitterImplTest.class,
    org.alfresco.repo.transfer.manifest.TransferManifestTest.class,
    org.alfresco.repo.transfer.TransferVersionCheckerImplTest.class,
    org.alfresco.repo.urlshortening.BitlyUrlShortenerTest.class,
    org.alfresco.service.cmr.calendar.CalendarRecurrenceHelperTest.class,
    org.alfresco.service.cmr.calendar.CalendarTimezoneHelperTest.class,
    org.alfresco.tools.RenameUserTest.class,
    org.alfresco.util.VersionNumberTest.class,
    org.alfresco.util.FileNameValidatorTest.class,
    org.alfresco.util.HttpClientHelperTest.class,
    org.alfresco.util.JSONtoFmModelTest.class,
    org.alfresco.util.ModelUtilTest.class,
    org.alfresco.util.PropertyMapTest.class,
    org.alfresco.util.ValueProtectingMapTest.class,
    org.alfresco.util.json.ExceptionJsonSerializerTest.class,
    org.alfresco.util.collections.CollectionUtilsTest.class,
    org.alfresco.util.schemacomp.DbObjectXMLTransformerTest.class,
    org.alfresco.util.schemacomp.DbPropertyTest.class,
    org.alfresco.util.schemacomp.DefaultComparisonUtilsTest.class,
    org.alfresco.util.schemacomp.DifferenceTest.class,
    org.alfresco.util.schemacomp.MultiFileDumperTest.class,
    org.alfresco.util.schemacomp.RedundantDbObjectTest.class,
    org.alfresco.util.schemacomp.SchemaComparatorTest.class,
    org.alfresco.util.schemacomp.SchemaToXMLTest.class,
    org.alfresco.util.schemacomp.ValidatingVisitorTest.class,
    org.alfresco.util.schemacomp.ValidationResultTest.class,
    org.alfresco.util.schemacomp.XMLToSchemaTest.class,
    org.alfresco.util.schemacomp.model.ColumnTest.class,
    org.alfresco.util.schemacomp.model.ForeignKeyTest.class,
    org.alfresco.util.schemacomp.model.IndexTest.class,
    org.alfresco.util.schemacomp.model.PrimaryKeyTest.class,
    org.alfresco.util.schemacomp.model.SchemaTest.class,
    org.alfresco.util.schemacomp.model.SequenceTest.class,
    org.alfresco.util.schemacomp.model.TableTest.class,
    org.alfresco.util.schemacomp.validator.IndexColumnsValidatorTest.class,
    org.alfresco.util.schemacomp.validator.NameValidatorTest.class,
    org.alfresco.util.schemacomp.validator.SchemaVersionValidatorTest.class,
    org.alfresco.util.schemacomp.validator.TypeNameOnlyValidatorTest.class,
    org.alfresco.util.test.OmittedTestClassFinderUnitTest.class,
    org.alfresco.util.test.junitrules.RetryAtMostRuleTest.class,
    org.alfresco.util.test.junitrules.TemporaryMockOverrideTest.class,
    org.alfresco.repo.search.impl.solr.AbstractSolrQueryHTTPClientTest.class,
    org.alfresco.repo.search.impl.solr.SpellCheckDecisionManagerTest.class,
    org.alfresco.repo.search.impl.solr.SolrStoreMappingWrapperTest.class,
    org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryEngineTest.class,
    org.alfresco.repo.search.impl.querymodel.impl.db.NodePermissionAssessorLimitsTest.class,
    org.alfresco.repo.search.impl.querymodel.impl.db.NodePermissionAssessorPermissionsTest.class,
    org.alfresco.repo.search.impl.solr.DbOrIndexSwitchingQueryLanguageTest.class,
    org.alfresco.repo.search.impl.solr.SolrQueryHTTPClientTest.class,
    org.alfresco.repo.search.impl.solr.SolrSQLHttpClientTest.class,
    org.alfresco.repo.search.impl.solr.SolrStatsResultTest.class,
    org.alfresco.repo.search.impl.solr.SolrSQLJSONResultMetadataSetTest.class,
    org.alfresco.repo.search.impl.solr.facet.SolrFacetComparatorTest.class,
    org.alfresco.repo.search.impl.solr.facet.FacetQNameUtilsTest.class,
    org.alfresco.util.BeanExtenderUnitTest.class,
    org.alfresco.repo.solr.SOLRTrackingComponentUnitTest.class,
    org.alfresco.repo.security.authentication.CompositePasswordEncoderTest.class,
    org.alfresco.repo.security.authentication.PasswordHashingTest.class,
    org.alfresco.repo.security.authority.script.ScriptAuthorityService_RegExTest.class,
    org.alfresco.repo.security.permissions.PermissionCheckCollectionTest.class,
    org.alfresco.repo.security.sync.LDAPUserRegistryTest.class,
    org.alfresco.traitextender.TraitExtenderIntegrationTest.class,
    org.alfresco.traitextender.AJExtensionsCompileTest.class,

    org.alfresco.repo.virtual.page.PageCollatorTest.class,
    org.alfresco.repo.virtual.ref.GetChildByIdMethodTest.class,
    org.alfresco.repo.virtual.ref.GetParentReferenceMethodTest.class,
    org.alfresco.repo.virtual.ref.NewVirtualReferenceMethodTest.class,
    org.alfresco.repo.virtual.ref.PlainReferenceParserTest.class,
    org.alfresco.repo.virtual.ref.PlainStringifierTest.class,
    org.alfresco.repo.virtual.ref.ProtocolTest.class,
    org.alfresco.repo.virtual.ref.ReferenceTest.class,
    org.alfresco.repo.virtual.ref.ResourceParameterTest.class,
    org.alfresco.repo.virtual.ref.StringParameterTest.class,
    org.alfresco.repo.virtual.ref.VirtualProtocolTest.class,
    org.alfresco.repo.virtual.store.ReferenceComparatorTest.class,

    org.alfresco.repo.virtual.ref.ZeroReferenceParserTest.class,
    org.alfresco.repo.virtual.ref.ZeroStringifierTest.class,

    org.alfresco.repo.virtual.ref.HashStringifierTest.class,
    org.alfresco.repo.virtual.ref.NodeRefRadixHasherTest.class,
    org.alfresco.repo.virtual.ref.NumericPathHasherTest.class,
    org.alfresco.repo.virtual.ref.StoredPathHasherTest.class,

    org.alfresco.repo.virtual.template.VirtualQueryImplTest.class,
    org.alfresco.repo.virtual.store.TypeVirtualizationMethodUnitTest.class,

    org.alfresco.repo.security.authentication.AuthenticationServiceImplTest.class,
    org.alfresco.util.EmailHelperTest.class,
    org.alfresco.repo.action.ParameterDefinitionImplTest.class,
    org.alfresco.repo.action.ActionDefinitionImplTest.class,
    org.alfresco.repo.action.ActionConditionDefinitionImplTest.class,
    org.alfresco.repo.action.ActionImplTest.class,
    org.alfresco.repo.action.ActionConditionImplTest.class,
    org.alfresco.repo.action.CompositeActionImplTest.class,
    org.alfresco.repo.action.CompositeActionConditionImplTest.class,
    org.alfresco.repo.action.executer.TransformActionExecuterTest.class,
    org.alfresco.repo.audit.AuditableAnnotationTest.class,
    org.alfresco.repo.audit.PropertyAuditFilterTest.class,
    org.alfresco.repo.audit.access.NodeChangeTest.class,
    org.alfresco.repo.content.ContentServiceImplUnitTest.class,
    org.alfresco.repo.content.directurl.SystemWideDirectUrlConfigUnitTest.class,
    org.alfresco.repo.content.directurl.ContentStoreDirectUrlConfigUnitTest.class,
    org.alfresco.repo.content.LimitedStreamCopierTest.class,
    org.alfresco.repo.content.filestore.FileIOTest.class,
    org.alfresco.repo.content.filestore.SpoofedTextContentReaderTest.class,
    org.alfresco.repo.content.ContentDataTest.class,
    org.alfresco.repo.content.replication.AggregatingContentStoreUnitTest.class,
    org.alfresco.service.cmr.repository.TransformationOptionLimitsTest.class,
    org.alfresco.service.cmr.repository.TransformationOptionPairTest.class,
    org.alfresco.repo.content.transform.TransformerConfigTestSuite.class,
    org.alfresco.repo.content.transform.TransformerDebugTest.class,
    org.alfresco.service.cmr.repository.TemporalSourceOptionsTest.class,
    org.alfresco.repo.content.metadata.MetadataExtracterLimitsTest.class,
    org.alfresco.repo.content.caching.quota.StandardQuotaStrategyMockTest.class,
    org.alfresco.repo.content.caching.quota.UnlimitedQuotaStrategyTest.class,
    org.alfresco.repo.content.caching.CachingContentStoreTest.class,
    org.alfresco.repo.content.caching.ContentCacheImplTest.class,
    org.alfresco.repo.domain.permissions.FixedAclUpdaterUnitTest.class,
    org.alfresco.repo.domain.propval.PropertyTypeConverterTest.class,
    org.alfresco.repo.domain.schema.script.ScriptBundleExecutorImplTest.class,
    org.alfresco.repo.search.MLAnaysisModeExpansionTest.class,
    org.alfresco.repo.search.DocumentNavigatorTest.class,
    org.alfresco.util.NumericEncodingTest.class,
    org.alfresco.repo.search.impl.parsers.CMIS_FTSTest.class,
    org.alfresco.repo.search.impl.parsers.CMISTest.class,
    org.alfresco.repo.search.impl.parsers.FTSTest.class,
    org.alfresco.repo.security.authentication.AlfrescoSSLSocketFactoryTest.class,
    org.alfresco.repo.security.authentication.AuthorizationTest.class,
    org.alfresco.repo.security.permissions.PermissionCheckedCollectionTest.class,
    org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSetTest.class,
    org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterUtilsTest.class,
    org.alfresco.repo.security.authentication.ChainingAuthenticationServiceTest.class,
    org.alfresco.repo.security.authentication.NameBasedUserNameGeneratorTest.class,
    org.alfresco.repo.version.common.VersionImplTest.class,
    org.alfresco.repo.version.common.VersionHistoryImplTest.class,
    org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicyTest.class,
    org.alfresco.repo.workflow.activiti.WorklfowObjectFactoryTest.class,
    org.alfresco.repo.workflow.activiti.properties.ActivitiPriorityPropertyHandlerTest.class,
    org.alfresco.repo.workflow.WorkflowSuiteContextShutdownTest.class,
    org.alfresco.repo.search.LuceneUtilsTest.class,

    org.alfresco.heartbeat.HBDataCollectorServiceImplTest.class,
    org.alfresco.heartbeat.jobs.LockingJobTest.class,
    org.alfresco.heartbeat.jobs.QuartzJobSchedulerTest.class,
    org.alfresco.heartbeat.AuthoritiesDataCollectorTest.class,
    org.alfresco.heartbeat.ConfigurationDataCollectorTest.class,
    org.alfresco.heartbeat.InfoDataCollectorTest.class,
    org.alfresco.heartbeat.ModelUsageDataCollectorTest.class,
    org.alfresco.heartbeat.SessionsUsageDataCollectorTest.class,
    org.alfresco.heartbeat.SystemUsageDataCollectorTest.class,

    org.alfresco.util.BeanExtenderUnitTest.class,
    org.alfresco.util.bean.HierarchicalBeanLoaderTest.class,
    org.alfresco.util.resource.HierarchicalResourceLoaderTest.class,
    org.alfresco.repo.events.ClientUtilTest.class,
    org.alfresco.repo.rendition2.RenditionService2Test.class,
    org.alfresco.repo.rendition2.TransformationOptionsConverterTest.class,

    org.alfresco.repo.event2.RepoEvent2UnitSuite.class,

    org.alfresco.util.schemacomp.SchemaDifferenceHelperUnitTest.class
})
public class AllUnitTestsSuite
{
}
