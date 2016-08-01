/*
 * #%L
 * Alfresco Repository
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
        suite.addTestSuite(org.alfresco.util.FileNameValidatorTest.class);
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
    }
}
