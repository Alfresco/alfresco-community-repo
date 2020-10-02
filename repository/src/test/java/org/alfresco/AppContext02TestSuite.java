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
    
    // there is a test that runs for 184s and another one that runs for 40s
    org.alfresco.repo.attributes.AttributeServiceTest.class,

    org.alfresco.repo.audit.AuditableAspectTest.class,
    org.alfresco.repo.audit.AuditBootstrapTest.class,
    org.alfresco.repo.audit.AuditComponentTest.class,
    org.alfresco.repo.audit.UserAuditFilterTest.class,
    org.alfresco.repo.audit.AuditMethodInterceptorTest.class,
    org.alfresco.repo.audit.access.AccessAuditorTest.class,

    // the following test will lock up the DB if run in the applicationContext_01 test suite
    org.alfresco.repo.activities.feed.FeedNotifierTest.class,

    org.alfresco.repo.activities.feed.FeedNotifierJobTest.class,
    org.alfresco.repo.admin.RepoAdminServiceImplTest.class,
    org.alfresco.repo.admin.patch.PatchTest.class,
    org.alfresco.repo.bulkimport.impl.StripingFilesystemTrackerTest.class,
    org.alfresco.repo.coci.CheckOutCheckInServiceImplTest.class,
    org.alfresco.repo.configuration.ConfigurableServiceImplTest.class,
    org.alfresco.repo.content.GuessMimetypeTest.class,
    org.alfresco.repo.content.filestore.FileContentStoreTest.class,
    org.alfresco.repo.content.filestore.NoRandomAccessFileContentStoreTest.class,
    org.alfresco.repo.content.filestore.ReadOnlyFileContentStoreTest.class,
    org.alfresco.repo.content.RoutingContentStoreTest.class,

    org.alfresco.encryption.EncryptionTests.class,
    org.alfresco.encryption.KeyStoreTests.class

    // TODO REPO-2791 org.alfresco.repo.content.routing.StoreSelectorAspectContentStoreTest.class,
})
public class AppContext02TestSuite
{
}
