/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.security.authentication;

import org.alfresco.filesys.auth.ftp.FTPAuthenticatorBase;
import org.alfresco.jlan.ftp.FTPSrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;

import java.util.List;

/**
 * Base chaining FTP Authenticator class. Where appropriate, methods will 'chain' across multiple
 * {@link #FTPAuthenticatorBase} instances, as returned by {@link #getUsableFtpAuthenticators()}.
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
