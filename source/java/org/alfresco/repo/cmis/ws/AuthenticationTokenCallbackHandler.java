/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;

/**
 * @author Dmitry Velichkevich
 */
public class AuthenticationTokenCallbackHandler implements CallbackHandler
{
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        WSPasswordCallback wssPasswordCallback = (WSPasswordCallback) callbacks[0];

        if ((WSPasswordCallback.USERNAME_TOKEN_UNKNOWN != wssPasswordCallback.getUsage()) && (WSPasswordCallback.USERNAME_TOKEN != wssPasswordCallback.getUsage()))
        {
            throw new SecurityException("Only 'UsernameToken' usage is supported.");
        }

        if (!WSConstants.PASSWORD_TEXT.equals(wssPasswordCallback.getPasswordType()))
        {
            throw new SecurityException("Password type '" + wssPasswordCallback.getPasswordType() + "' unsupported. Only '" + WSConstants.PW_TEXT + "' is supported.");
        }
    }
}
