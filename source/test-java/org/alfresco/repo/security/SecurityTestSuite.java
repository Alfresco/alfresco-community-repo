package org.alfresco.repo.security;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.domain.permissions.FixedAclUpdaterTest;
import org.alfresco.repo.ownable.impl.OwnableServiceTest;
import org.alfresco.repo.security.authentication.AlfrescoSSLSocketFactoryTest;
import org.alfresco.repo.security.authentication.AuthenticationBootstrapTest;
import org.alfresco.repo.security.authentication.AuthenticationTest;
import org.alfresco.repo.security.authentication.AuthorizationTest;
import org.alfresco.repo.security.authentication.ChainingAuthenticationServiceTest;
import org.alfresco.repo.security.authentication.NameBasedUserNameGeneratorTest;
import org.alfresco.repo.security.authentication.UpgradePasswordHashTest;
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
		suite.addTestSuite(FixedAclUpdaterTest.class);
		
        return suite;
    }
}
