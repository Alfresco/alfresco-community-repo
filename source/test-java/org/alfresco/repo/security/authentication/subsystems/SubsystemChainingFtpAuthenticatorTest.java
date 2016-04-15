
package org.alfresco.repo.security.authentication.subsystems;

import org.alfresco.filesys.auth.ftp.FTPAuthenticatorBase;
import org.alfresco.jlan.ftp.FTPSrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseSpringTest;
import org.mockito.Mock;

import java.util.List;

/**
 * Test class with test spring contexts for auth subsystems.
 *
 * @author alex.mukha
 * @since 4.2.1
 */
public class SubsystemChainingFtpAuthenticatorTest extends BaseSpringTest
{
    private static String SOURCE_BEAN_NAME = "testFtpAuthenticator";

    private SubsystemChainingFtpAuthenticator chainingAuthenticator;

    private @Mock ClientInfo info;
    private @Mock FTPSrvSession session;

    @Override
    protected String[] getConfigLocations()
    {
        return new String[]
        {
            ApplicationContextHelper.CONFIG_LOCATIONS[0], "classpath:test-ftp-auth-context.xml"
        };
    }

    public void testNumberOfAuthenticatorsInChain()
    {
        // The contexts are configured for 3 subsystems:
        // 1) authType1 - ftp auth disabled, the user is always NOT authenticated as that bean is switched off
        // 2) authType2 - ftp auth enabled, the user is always NOT authenticated
        // 3) authType3 - ftp auth enabled, the user is always authenticated

        setContextForSubsystem("testFtpAuthentication");
        
        List<FTPAuthenticatorBase> authenticators = chainingAuthenticator.getUsableFtpAuthenticators();
        assertTrue("The context configuration was created for 3 test FTP authenticators with 1 disabled.", authenticators.size() == 2);
        
        // The contexts are configured for 2 subsystems:
        // 1) authType1 - ftp auth disabled, the user is always NOT authenticated as that bean is switched off
        // 2) authType2 - ftp auth disabled, the user is always NOT authenticated as that bean is switched off

        setContextForSubsystem("testFtpAuthenticationAllDisabled"); 
        
        authenticators = chainingAuthenticator.getUsableFtpAuthenticators();
        assertTrue("The context configuration was created for 2 test FTP authenticators - all disabled.", authenticators.isEmpty());
    }

    /**
     * As the context is configured to fail on first two subsystems, the third should be used
     */
    public void testAuthenticatorChain()
    {
        // The contexts are configured for 3 subsystems:
        // 1) authType1 - ftp auth disabled, the user is always NOT authenticated as that bean is switched off
        // 2) authType2 - ftp auth enabled, the user is always NOT authenticated
        // 3) authType3 - ftp auth enabled, the user is always authenticated
        setContextForSubsystem("testFtpAuthentication");

        // The last in the chain should work
        assertTrue("The user should be authenticated", chainingAuthenticator.authenticateUser(info, session));
        
        // The contexts are configured for 2 subsystems:
        // 1) authType1 - ftp auth enabled, the user is always NOT authenticated
        // 2) authType2 - ftp auth enabled, the user is always NOT authenticated

        setContextForSubsystem("testFtpAuthenticationAllFailing"); 
        
        // All of them should fail
        assertFalse("The user should be authenticated", chainingAuthenticator.authenticateUser(info, session));
    }
    
    @SuppressWarnings("deprecation")
    private void setContextForSubsystem(String beanName)
    {
        chainingAuthenticator = new SubsystemChainingFtpAuthenticator();
        ChildApplicationContextManager applicationContextManager = (ChildApplicationContextManager) getApplicationContext().getBean(beanName); 
        chainingAuthenticator.setApplicationContextManager(applicationContextManager);
        chainingAuthenticator.setSourceBeanName(SOURCE_BEAN_NAME);
    }
}
