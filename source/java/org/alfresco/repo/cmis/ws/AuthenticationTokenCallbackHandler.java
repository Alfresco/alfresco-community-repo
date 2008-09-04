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

import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.ws.security.WSPasswordCallback;

/**
 * @author Michael Shavnev
 */
public class AuthenticationTokenCallbackHandler implements CallbackHandler
{

    private AuthenticationService authenticationService;

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        WSPasswordCallback wssPasswordCallback = (WSPasswordCallback) callbacks[0];
        String userName = wssPasswordCallback.getIdentifer();
        String password = getPassword(userName);

        // Check the UsernameToken element.
        // Depending on the password type contained in the element the processing differs.
        if (wssPasswordCallback.getUsage() == WSPasswordCallback.USERNAME_TOKEN)
        {
            // If the password type is password digest provide stored password perform
            // hash algorithm and compare the result with the transmitted password
            wssPasswordCallback.setPassword(password);
        }
        else
        {
            // If the password is of type password text or any other yet unknown password type
            // the delegate the password validation to the callback class.
            if (!password.equals(wssPasswordCallback.getPassword()))
            {
                throw new SecurityException("Incorrect password");
            }
        }
    }

    private String getPassword(String userName)
    {
        // TODO Auto-generated method stub
        return userName;
    }

}
