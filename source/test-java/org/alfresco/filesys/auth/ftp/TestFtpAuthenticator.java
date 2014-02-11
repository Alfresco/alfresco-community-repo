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
