package org.alfresco;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All Repository project UNIT test classes should be added to this test suite.
 */
public class AllUnitTestsSuite extends TestSuite
{
    /**
     * Creates the test suite
     * 
     * @return the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        unitTests(suite);
        return suite;
    }

    static void unitTests(TestSuite suite)
    {
        suite.addTestSuite(org.alfresco.cmis.PropertyFilterTest.class);
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
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.lock.LockUtilsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.lock.mem.LockStoreImplTest.class));
        suite.addTestSuite(org.alfresco.repo.module.ModuleDetailsImplTest.class);
        suite.addTestSuite(org.alfresco.repo.module.tool.ModuleManagementToolTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.module.tool.WarHelperImplTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.nodelocator.NodeLocatorServiceImplTest.class));
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
        suite.addTest(new JUnit4TestAdapter(org.alfresco.service.cmr.repository.TemporalSourceOptionsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.service.cmr.repository.TransformationOptionLimitsTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.service.cmr.repository.TransformationOptionPairTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.tools.RenameUserTest.class));
        suite.addTestSuite(org.alfresco.util.DynamicallySizedThreadPoolExecutorTest.class);
        suite.addTestSuite(org.alfresco.util.FileNameValidatorTest.class);
        suite.addTestSuite(org.alfresco.util.JSONtoFmModelTest.class);
        suite.addTestSuite(org.alfresco.util.ModelUtilTest.class);
        suite.addTest(new JUnit4TestAdapter(org.alfresco.util.PropertyMapTest.class));
        suite.addTestSuite(org.alfresco.util.ValueProtectingMapTest.class);
        suite.addTestSuite(org.alfresco.util.json.ExceptionJsonSerializerTest.class);
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
    }
}
