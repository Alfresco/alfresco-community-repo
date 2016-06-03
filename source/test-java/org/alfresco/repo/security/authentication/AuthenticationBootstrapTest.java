package org.alfresco.repo.security.authentication;

import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;

import junit.framework.TestCase;

/**
 * Checks that no residual authentications are left over after bootstrap.  It is important that
 * this test run on its own and not part of a suite.
 * 
 * @author Derek Hulley
 * @since 3.0.1
 */
@Category(OwnJVMTestsCategory.class)
public class AuthenticationBootstrapTest extends TestCase
{
    /**
     * Creates the application context in the context of the test (not statically) and checks
     * that no residual authentication is left hanging around.
     */
    public void testBootstrap()
    {
        // Start the context
        ApplicationContextHelper.getApplicationContext();
        
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        assertNull(
                "Found user '" + user + "' still authenticated after bootstrap.\n" +
                "Use AuthenticationUtil.runAs or AuthenticationUtil.pushAuthentication " +
                "and AuthenticationUtil.popAuthentication to keep the thread clean of unwanted authentication tokens.",
                user);
    }
}
