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

import org.alfresco.encryption.KeyStoreParameters;

/**
 * 
 * @since 4.0
 *
 */
public class SSLEncryptionParameters
{
    private KeyStoreParameters keyStoreParameters;
    private KeyStoreParameters trustStoreParameters;

    /**
     *  Default constructor (for use by Spring)
     */
    public SSLEncryptionParameters()
    {
        super();
    }

    public SSLEncryptionParameters(KeyStoreParameters keyStoreParameters, KeyStoreParameters trustStoreParameters)
    {
        super();
        this.keyStoreParameters = keyStoreParameters;
        this.trustStoreParameters = trustStoreParameters;
    }

    public KeyStoreParameters getKeyStoreParameters()
    {
        return keyStoreParameters;
    }
    
    public KeyStoreParameters getTrustStoreParameters()
    {
        return trustStoreParameters;
    }

    public void setKeyStoreParameters(KeyStoreParameters keyStoreParameters)
    {
        this.keyStoreParameters = keyStoreParameters;
    }

    public void setTrustStoreParameters(KeyStoreParameters trustStoreParameters)
    {
        this.trustStoreParameters = trustStoreParameters;
    }
}
