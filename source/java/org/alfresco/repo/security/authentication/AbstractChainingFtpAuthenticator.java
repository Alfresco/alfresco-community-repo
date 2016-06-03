
package org.alfresco.repo.security.authentication;

import org.alfresco.filesys.auth.ftp.FTPAuthenticatorBase;
import org.alfresco.jlan.ftp.FTPSrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;

import java.util.List;

/**
 * Base chaining FTP Authenticator class. Where appropriate, methods will 'chain' across multiple
 * {@link FTPAuthenticatorBase} instances, as returned by {@link #getUsableFtpAuthenticators()}.
 *
 * @author alex.mukha
 * @since 4.2.1
 */
public abstract class AbstractChainingFtpAuthenticator extends FTPAuthenticatorBase
{
    @Override
    public boolean authenticateUser(ClientInfo info, FTPSrvSession sess)
    {
        for (FTPAuthenticatorBase authenticator : getUsableFtpAuthenticators())
        {
            if (authenticator.authenticateUser(info, sess))
                return true;
        }
        // authentication failed in all of the authenticators
        return false;
    }

    /**
     * Gets the FTP authenticators across which methods will chain.
     *
     * @return the usable FTP authenticators
     */
    protected abstract List<FTPAuthenticatorBase> getUsableFtpAuthenticators();
}
