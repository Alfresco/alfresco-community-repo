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
    org.alfresco.repo.domain.node.NodeDAOTest.class,
    org.alfresco.repo.security.authentication.AuthenticationBootstrapTest.class,
    org.alfresco.repo.security.authority.AuthorityServiceTest.class,
    org.alfresco.repo.security.authority.DuplicateAuthorityTest.class,
    org.alfresco.repo.security.authority.ExtendedPermissionServiceTest.class,
    org.alfresco.repo.security.permissions.dynamic.LockOwnerDynamicAuthorityTest.class,
    org.alfresco.repo.security.permissions.impl.AclDaoComponentTest.class,
    org.alfresco.repo.security.permissions.impl.PermissionServiceTest.class,
    org.alfresco.repo.security.permissions.impl.acegi.ACLEntryAfterInvocationTest.class,
    org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterTest.class,
    org.alfresco.repo.security.permissions.impl.model.PermissionModelTest.class,
    org.alfresco.repo.security.person.PersonTest.class,
    org.alfresco.repo.ownable.impl.OwnableServiceTest.class,
    org.alfresco.repo.security.permissions.impl.ReadPermissionTest.class,
    org.alfresco.repo.security.authentication.UpgradePasswordHashTest.class,
    org.alfresco.repo.security.authority.AuthorityBridgeTableAsynchronouslyRefreshedCacheTest.class,
    org.alfresco.repo.security.person.HomeFolderProviderSynchronizerTest.class,
    org.alfresco.repo.domain.permissions.FixedAclUpdaterTest.class,
    org.alfresco.repo.security.authentication.external.DefaultRemoteUserMapperTest.class,
    org.alfresco.repo.security.authentication.identityservice.IdentityServiceRemoteUserMapperTest.class,
    org.alfresco.repo.security.authentication.subsystems.SubsystemChainingFtpAuthenticatorTest.class,
    org.alfresco.repo.security.authentication.external.LocalAuthenticationServiceTest.class,
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
    // AuditDAOTest fails if it runs after CannedQueryDAOTest so this order is a compromise
    // CannedQueryDAOTest will fail on MS SQL if either AuditDAOTest or PropertyValueCleanupTest fail
    org.alfresco.repo.domain.propval.PropertyValueCleanupTest.class,
    org.alfresco.repo.domain.audit.AuditDAOTest.class,
    org.alfresco.repo.model.ModelTestSuite.class,
    org.alfresco.repo.tenant.MultiTNodeServiceInterceptorTest.class,
    org.alfresco.repo.transfer.RepoTransferReceiverImplTest.class,
})
public class AppContext05TestSuite
{
}
