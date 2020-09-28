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
package org.alfresco.encryption;

import java.security.Key;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Provides system-wide secret keys for symmetric database encryption from a key store
 * in the filesystem. Just wraps a key store.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class KeystoreKeyProvider extends AbstractKeyProvider
{
    private static final Log logger = LogFactory.getLog(KeystoreKeyProvider.class);

    private AlfrescoKeyStore keyStore;
    private boolean useBackupKeys = false;

    /**
     * Constructs the provider with required defaults
     */
    public KeystoreKeyProvider()
    {
    }

    public KeystoreKeyProvider(KeyStoreParameters keyStoreParameters, KeyResourceLoader keyResourceLoader)
    {
        this();
        this.keyStore = new AlfrescoKeyStoreImpl(keyStoreParameters, keyResourceLoader);
        init();
    }
    
    public void setUseBackupKeys(boolean useBackupKeys)
    {
        this.useBackupKeys = useBackupKeys;
    }

    /**
     * 
     * @param keyStore
     */
    public KeystoreKeyProvider(AlfrescoKeyStore keyStore)
    {
        this();
        this.keyStore = keyStore;
        init();
    }
    
    public void setKeyStore(AlfrescoKeyStore keyStore)
    {
        this.keyStore = keyStore;
    }

    public void init()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Key getKey(String keyAlias)
    {
        if(useBackupKeys)
        {
            return keyStore.getBackupKey(keyAlias);            
        }
        else
        {
            return keyStore.getKey(keyAlias);
        }
    }
}
