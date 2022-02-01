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
package org.alfresco.repo.security;

import org.alfresco.repo.domain.permissions.FixedAclUpdaterTest;
import org.alfresco.repo.ownable.impl.OwnableServiceTest;
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
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceAuthenticationComponentTest;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceRemoteUserMapperTest;
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

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andy Hind
 *
 */
public class SecurityTestSuite extends TestSuite
{

    /**
     * Creates the test suite
     * 
     * @return  the test suite
     */
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(AuthenticationBootstrapTest.class);
        suite.addTestSuite(AuthenticationTest.class);
        suite.addTestSuite(ChainingAuthenticationServiceTest.class);
        suite.addTestSuite(NameBasedUserNameGeneratorTest.class);
        suite.addTestSuite(AuthorityServiceTest.class);
        suite.addTestSuite(DuplicateAuthorityTest.class);
        suite.addTestSuite(ExtendedPermissionServiceTest.class);
        suite.addTestSuite(LockOwnerDynamicAuthorityTest.class);
        suite.addTestSuite(AclDaoComponentTest.class);
        suite.addTestSuite(PermissionServiceTest.class);
        suite.addTestSuite(ACLEntryAfterInvocationTest.class);
        suite.addTestSuite(ACLEntryVoterTest.class);
        suite.addTestSuite(FilteringResultSetTest.class);
        suite.addTestSuite(PermissionModelTest.class);
        suite.addTestSuite(PersonTest.class);
        // Note  org.alfresco.repo.security.sync.ChainingUserRegistrySynchronizerTest has its own context and runs on its own 
        // suite.addTestSuite(ChainingUserRegistrySynchronizerTest.class);
        suite.addTestSuite(OwnableServiceTest.class);    
        suite.addTestSuite(ReadPermissionTest.class);    
        suite.addTestSuite(AuthorizationTest.class);
        suite.addTestSuite(UpgradePasswordHashTest.class);
        suite.addTestSuite(AuthorityBridgeTableAsynchronouslyRefreshedCacheTest.class);

        suite.addTest(new JUnit4TestAdapter(HomeFolderProviderSynchronizerTest.class));
        suite.addTest(new JUnit4TestAdapter(AlfrescoSSLSocketFactoryTest.class));
		suite.addTest(new JUnit4TestAdapter(FixedAclUpdaterTest.class));

		suite.addTestSuite(DefaultRemoteUserMapperTest.class);
		suite.addTestSuite(IdentityServiceRemoteUserMapperTest.class);
        suite.addTest(new JUnit4TestAdapter(IdentityServiceAuthenticationComponentTest.class));
		suite.addTestSuite(SubsystemChainingFtpAuthenticatorTest.class);
		suite.addTest(new JUnit4TestAdapter(LocalAuthenticationServiceTest.class));

        suite.addTest(new JUnit4TestAdapter(ResetPasswordServiceImplTest.class));
        return suite;
    }
}
