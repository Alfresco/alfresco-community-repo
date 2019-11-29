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

import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.DBTests;
import org.alfresco.util.testing.category.NonBuildTests;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.context.ApplicationContext;

/**
 * Repository project tests using the various application contexts including the minimal context
 * alfresco/minimal-context.xml but not the main one alfresco/application-context.xml.
 * Tests marked as DBTests are automatically excluded and are run as part of {@link AllDBTestsTestSuite}.
 */
@RunWith(Categories.class)
@Categories.ExcludeCategory({DBTests.class, NonBuildTests.class})
@Suite.SuiteClasses({

    // ----------------------------------------------------------------------
    // Minimum context [classpath:alfresco/minimal-context.xml]
    // ----------------------------------------------------------------------

    // Limits
    org.alfresco.repo.content.transform.AbstractContentTransformerLimitsTest.class,

    // Transform tests
    org.alfresco.repo.content.transform.BinaryPassThroughContentTransformerTest.class,
    org.alfresco.repo.content.transform.ComplexContentTransformerTest.class,
    org.alfresco.repo.content.transform.ContentTransformerRegistryTest.class,
    org.alfresco.repo.content.transform.HtmlParserContentTransformerTest.class,
    org.alfresco.repo.content.transform.MailContentTransformerTest.class,
    org.alfresco.repo.content.transform.EMLTransformerTest.class,
    org.alfresco.repo.content.transform.MediaWikiContentTransformerTest.class,
    org.alfresco.repo.content.transform.OpenOfficeContentTransformerTest.class,
    // Requires a transformer to be installed in the system
    //org.alfresco.repo.content.transform.PdfBoxContentTransformerTest.class,
    org.alfresco.repo.content.transform.PoiContentTransformerTest.class,
    org.alfresco.repo.content.transform.PoiHssfContentTransformerTest.class,
    org.alfresco.repo.content.transform.PoiOOXMLContentTransformerTest.class,
    org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerTest.class,
    org.alfresco.repo.content.transform.StringExtractingContentTransformerTest.class,
    org.alfresco.repo.content.transform.TextMiningContentTransformerTest.class,
    org.alfresco.repo.content.transform.TextToPdfContentTransformerTest.class,
    org.alfresco.repo.content.transform.TikaAutoContentTransformerTest.class,
    org.alfresco.repo.content.transform.magick.ImageMagickContentTransformerTest.class,
    org.alfresco.repo.content.transform.AppleIWorksContentTransformerTest.class,
    org.alfresco.repo.content.transform.ArchiveContentTransformerTest.class,

    // Metadata tests
    org.alfresco.repo.content.metadata.DWGMetadataExtracterTest.class,
    org.alfresco.repo.content.metadata.HtmlMetadataExtracterTest.class,
    org.alfresco.repo.content.metadata.MailMetadataExtracterTest.class,
    org.alfresco.repo.content.metadata.MP3MetadataExtracterTest.class,
    org.alfresco.repo.content.metadata.OfficeMetadataExtracterTest.class,
    org.alfresco.repo.content.metadata.OpenDocumentMetadataExtracterTest.class,
    org.alfresco.repo.content.metadata.JodMetadataExtractorOOoTest.class,
    org.alfresco.repo.content.metadata.PdfBoxMetadataExtracterTest.class,
    org.alfresco.repo.content.metadata.ConcurrencyPdfBoxMetadataExtracterTest.class,
    org.alfresco.repo.content.metadata.PoiMetadataExtracterTest.class,
    org.alfresco.repo.content.metadata.RFC822MetadataExtracterTest.class,
    org.alfresco.repo.content.metadata.TikaAutoMetadataExtracterTest.class,

    org.alfresco.repo.content.metadata.MappingMetadataExtracterTest.class,

        // ----------------------------------------------------------------------
        // Transformer/Rendition contexts
        //
        // The following tests can be extracted in a separate test suite
        // if/when we decide to move the transformations in a separate component
        // ----------------------------------------------------------------------

        // [classpath:alfresco/application-context.xml, classpath:org/alfresco/repo/thumbnail/test-thumbnail-context.xml]
        // some tests fail locally - on windows
        org.alfresco.repo.thumbnail.ThumbnailServiceImplTest.class,

        // [classpath:/test/alfresco/test-renditions-context.xml, classpath:alfresco/application-context.xml,
        // classpath:alfresco/test/global-integration-test-context.xml]
        // this does NOT passes locally
        org.alfresco.repo.rendition.RenditionServicePermissionsTest.class,

    // ----------------------------------------------------------------------
    // Misc contexts
    // ----------------------------------------------------------------------

    // [classpath:alfresco/node-locator-context.xml, classpath:test-nodeLocatorServiceImpl-context.xml]
    org.alfresco.repo.nodelocator.NodeLocatorServiceImplTest.class,

    // [classpath*:alfresco/ibatis/ibatis-test-context.xml, classpath:alfresco/application-context.xml,
    // classpath:alfresco/test/global-integration-test-context.xml]
    org.alfresco.repo.domain.query.CannedQueryDAOTest.class,
    org.alfresco.repo.node.NodeServiceTest.class,

    // [classpath:alfresco/application-context.xml, classpath:alfresco/minimal-context.xml]
    org.alfresco.RepositoryStartStopTest.class,

    // [classpath:cachingstore/test-context.xml]
    org.alfresco.repo.content.caching.FullTest.class,

    // [classpath:cachingstore/test-cleaner-context.xml]
    org.alfresco.repo.content.caching.cleanup.CachedContentCleanupJobTest.class,

    // [classpath:cachingstore/test-std-quota-context.xml]
    org.alfresco.repo.content.caching.quota.StandardQuotaStrategyTest.class,

    // [classpath:cachingstore/test-slow-context.xml]
    org.alfresco.repo.content.caching.test.SlowContentStoreTest.class,
    org.alfresco.repo.content.caching.test.ConcurrentCachingStoreTest.class,

    // [classpath:org/alfresco/repo/jscript/test-context.xml]
    org.alfresco.repo.jscript.ScriptBehaviourTest.class,

    // [module/module-component-test-beans.xml]
    org.alfresco.repo.module.ComponentsTest.class,

    // [ibatis/hierarchy-test/hierarchy-test-context.xml]
    org.alfresco.ibatis.HierarchicalSqlSessionFactoryBeanTest.class
})
public class MiscContextTestSuite
{
    /**
     * Asks {@link ApplicationContextHelper} to give us a
     *  suitable, perhaps cached context for use in our tests
     */
    public static ApplicationContext getMinimalContext() {
        ApplicationContextHelper.setUseLazyLoading(false);
        ApplicationContextHelper.setNoAutoStart(true);
        return ApplicationContextHelper.getApplicationContext(
            new String[] { "classpath:alfresco/minimal-context.xml" }
        );
    }

    static
    {
        getMinimalContext();
    }
}
