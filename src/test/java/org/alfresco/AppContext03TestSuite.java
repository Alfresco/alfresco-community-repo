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

    // needs a clean DB to run
    org.alfresco.repo.calendar.CalendarServiceImplTest.class,

    org.alfresco.RepositoryStartupTest.class,
    org.alfresco.repo.content.cleanup.ContentStoreCleanerTest.class,
    org.alfresco.repo.content.RoutingContentServiceTest.class,
    org.alfresco.repo.exporter.ExporterComponentTest.class,

    // the following two tests fail on windows
    org.alfresco.repo.rendition.MultiUserRenditionTest.class,
    org.alfresco.repo.rendition.RenditionServiceIntegrationTest.class,

    org.alfresco.repo.lock.LockBehaviourImplTest.class,
    org.alfresco.repo.node.archive.LargeArchiveAndRestoreTest.class,
    org.alfresco.repo.copy.CopyServiceImplTest.class,
    org.alfresco.repo.descriptor.DescriptorServiceTest.class,
    org.alfresco.repo.dictionary.DictionaryModelTypeTest.class,
    org.alfresco.repo.dictionary.DictionaryRepositoryBootstrapTest.class,
    org.alfresco.repo.dictionary.ModelValidatorTest.class,
    org.alfresco.repo.dictionary.types.period.PeriodTest.class,
    org.alfresco.repo.exporter.RepositoryExporterComponentTest.class,
    org.alfresco.repo.i18n.MessageServiceImplTest.class,
    org.alfresco.repo.importer.FileImporterTest.class,
    org.alfresco.repo.importer.ImporterComponentTest.class,
    org.alfresco.repo.jscript.PeopleTest.class,
    org.alfresco.repo.jscript.RhinoScriptTest.class,

    // needs a clean DB to run
    org.alfresco.repo.links.LinksServiceImplTest.class,
    org.alfresco.repo.lock.JobLockServiceTest.class,
    org.alfresco.repo.lock.LockServiceImplTest.class,
    org.alfresco.repo.lock.mem.LockStoreImplTxTest.class,
    org.alfresco.repo.lock.mem.LockableAspectInterceptorTest.class,
    org.alfresco.repo.management.JmxDumpUtilTest.class,
    org.alfresco.repo.node.ConcurrentNodeServiceSearchTest.class,
    org.alfresco.repo.node.ConcurrentNodeServiceTest.class,
    org.alfresco.repo.node.FullNodeServiceTest.class,
    org.alfresco.repo.node.NodeRefPropertyMethodInterceptorTest.class,
    org.alfresco.repo.node.PerformanceNodeServiceTest.class,
    org.alfresco.repo.node.archive.ArchiveAndRestoreTest.class,
    org.alfresco.repo.node.db.DbNodeServiceImplTest.class,
    org.alfresco.repo.node.cleanup.TransactionCleanupTest.class,
    org.alfresco.repo.node.db.DbNodeServiceImplPropagationTest.class,
})
public class AppContext03TestSuite
{
}
