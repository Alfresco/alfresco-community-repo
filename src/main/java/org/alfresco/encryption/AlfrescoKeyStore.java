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
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

/**
 * Manages a Java Keystore for Alfresco, including caching keys where appropriate.
 * 
 * @since 4.0
 *
 */
public interface AlfrescoKeyStore
{
    public static final String KEY_KEYSTORE_PASSWORD = "keystore.password";

    /**
     * The name of the keystore.
     * 
     * @return the name of the keystore.
     */
    public String getName();
    
    /**
     * Backup the keystore to the backup location. Write the keys to the backup keystore.
     */
    public void backup();

    /**
     * The key store parameters.
     *
     * @return KeyStoreParameters
     */
    public KeyStoreParameters getKeyStoreParameters();

    /**
     * The backup key store parameters.
     * 
     * @return      * @return

     */
    public KeyStoreParameters getBackupKeyStoreParameters();

    /**
     * Does the underlying key store exist?
     * 
     * @return true if it exists, false otherwise
     */
    public boolean exists();

    /**
     * Return the key with the given key alias.
     * 
     * @param keyAlias String
     * @return Key
     */
    public Key getKey(String keyAlias);

    /**
     * Return the timestamp (in ms) of when the key was last loaded from the keystore on disk.
     * 
     * @param keyAlias String
     * @return long
     */
    public long getKeyTimestamp(String keyAlias);
    
    /**
     * Return the backup key with the given key alias.
     * 
     * @param keyAlias String
     * @return Key
     */
    public Key getBackupKey(String keyAlias);
    
    /**
     * Return all key aliases in the key store.
     * 
     * @return Set<String>
     */
    public Set<String> getKeyAliases();
    
    /**
     * Create an array of key managers from keys in the key store.
     * 
     * @return KeyManager[]
     */
    public KeyManager[] createKeyManagers();
    
    /**
     * Create an array of trust managers from certificates in the key store.
     * 
     * @return TrustManager[]
     */
    public TrustManager[] createTrustManagers();
    
    /**
     * Create the key store if it doesn't exist.
     * A key for each key alias will be written to the keystore on disk, either from the cached keys or, if not present, a key will be generated.
     */
    public void create();
    
    /**
     * Reload the keys from the key store.
     */
    public void reload() throws InvalidKeystoreException, MissingKeyException;
    
    /**
     * Check that the keys in the key store are valid i.e. that they match those registered.
     */
    public void validateKeys() throws InvalidKeystoreException, MissingKeyException;
    
}
