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
 * Repository project database tests.
 */
@RunWith(Categories.class)
@Categories.IncludeCategory(DBTests.class)
@Categories.ExcludeCategory(NonBuildTests.class)
@Suite.SuiteClasses({
	// From AllUnitTestsSuite
    org.alfresco.repo.dictionary.RepoDictionaryDAOTest.class,

	// From AppContext03TestSuite
    org.alfresco.repo.lock.JobLockServiceTest.class,
    org.alfresco.repo.node.db.DbNodeServiceImplPropagationTest.class,

	// From AppContext04TestSuite
    org.alfresco.util.schemacomp.DbToXMLTest.class,
    org.alfresco.util.schemacomp.ExportDbTest.class,
    org.alfresco.util.schemacomp.SchemaReferenceFileTest.class,
    org.alfresco.repo.node.getchildren.GetChildrenCannedQueryTest.class,

	// From AppContext05TestSuite
    org.alfresco.repo.domain.node.NodeDAOTest.class,
    org.alfresco.repo.security.permissions.impl.AclDaoComponentTest.class,
    org.alfresco.repo.domain.contentdata.ContentDataDAOTest.class,
    org.alfresco.repo.domain.encoding.EncodingDAOTest.class,
    org.alfresco.repo.domain.locks.LockDAOTest.class,
    org.alfresco.repo.domain.mimetype.MimetypeDAOTest.class,
    org.alfresco.repo.domain.locale.LocaleDAOTest.class,
    org.alfresco.repo.domain.qname.QNameDAOTest.class,
    org.alfresco.repo.domain.propval.PropertyValueDAOTest.class,
    org.alfresco.repo.domain.patch.AppliedPatchDAOTest.class,
    org.alfresco.repo.domain.permissions.AclCrudDAOTest.class,
    org.alfresco.repo.domain.usage.UsageDAOTest.class,
    org.alfresco.repo.domain.solr.SOLRDAOTest.class,
    org.alfresco.repo.domain.tenant.TenantAdminDAOTest.class,

    // REPO-1012 : run AuditDAOTest and PropertyValueCleanupTest near the end
    // because their failure can cause other tests to fail on MS SQL
    // AuditDAOTest fails if it runs after CannedQueryDAOTest.
    // CannedQueryDAOTest will fail on MS SQL if either AuditDAOTest or PropertyValueCleanupTest fail
    org.alfresco.repo.domain.propval.PropertyValueCleanupTest.class,
    org.alfresco.repo.domain.audit.AuditDAOTest.class,

	// From MiscContextTestSuite
    org.alfresco.repo.domain.query.CannedQueryDAOTest.class,

    // REPO-2963 : Tests causing a cascade of failures in AllDBTestsTestSuite on PostgreSQL/MySQL	
    // Moved at the bottom of the suite because DbNodeServiceImplTest.testNodeCleanupRegistry() takes a long time on a clean DB.
    org.alfresco.repo.node.db.DbNodeServiceImplTest.class,

    org.alfresco.repo.node.cleanup.TransactionCleanupTest.class,
    org.alfresco.repo.security.person.GetPeopleCannedQueryTest.class
})
public class AllDBTestsTestSuite
{
}
