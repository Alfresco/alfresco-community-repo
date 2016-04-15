
package org.alfresco.filesys.auth.ftp;

import org.alfresco.jlan.ftp.FTPSrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;

/**
 * Test FTP Authenticator class. Will be created as several beans in test auth context.
 * Will authenticate the user as the flag {@link TestFtpAuthenticator#authenticateAs} is set, default is true.
 *
 * @author alex.mukha
 * @since 4.2.1
 */
public class TestFtpAuthenticator extends FTPAuthenticatorBase
{
    public boolean authenticateAs = true;

    public void setAuthenticateAs (boolean authenticateAs)
    {
        this.authenticateAs = authenticateAs;
    }

    @Override
    public boolean authenticateUser (ClientInfo info, FTPSrvSession sess)
    {
        return authenticateAs;
    }
}
