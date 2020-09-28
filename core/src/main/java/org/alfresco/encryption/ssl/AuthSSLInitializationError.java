/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.encryption.ssl;

/**
 * <p>
 * Signals fatal error in initialization of {@link AuthSSLProtocolSocketFactory}.
 * </p>
 * 
 * <p>
 * Adapted from code here: http://svn.apache.org/viewvc/httpcomponents/oac.hc3x/trunk/src/contrib/org/apache/commons/httpclient/contrib/ssl/AuthSSLX509TrustManager.java?revision=608014&view=co
 * </p>
 * 
 * @since 4.0
 */
public class AuthSSLInitializationError extends Error
{
    private static final long serialVersionUID = 8135341334029823112L;

    /**
     * Creates a new AuthSSLInitializationError.
     */
    public AuthSSLInitializationError()
    {
        super();
    }

    /**
     * Creates a new AuthSSLInitializationError with the specified message.
     *
     * @param message error message
     */
    public AuthSSLInitializationError(String message)
    {
        super(message);
    }
}
