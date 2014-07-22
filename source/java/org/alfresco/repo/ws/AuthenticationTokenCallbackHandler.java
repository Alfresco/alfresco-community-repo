/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.ws;

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
