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
import java.util.List;
import java.util.Set;

/**
 * Stores registered encryption keys.
 * 
 * @since 4.0
 *
 */
public interface EncryptionKeysRegistry
{
    public static enum KEY_STATUS
    {
        OK, CHANGED, MISSING;
    };

    /**
     * Is the key with alias 'keyAlias' registered?
     * @param keyAlias String
     * @return boolean
     */
    public boolean isKeyRegistered(String keyAlias);
    
    /**
     * Register the key.
     * 
     * @param keyAlias String
     * @param key Key
     */
    public void registerKey(String keyAlias, Key key);
    
    /**
     * Unregister the key.
     * 
     * @param keyAlias String
     */
    public void unregisterKey(String keyAlias);
    
    /**
     * Check the validity of the key against the registry.
     * 
     * @param keyAlias String
     * @param key Key
     * @return KEY_STATUS
     */
    public KEY_STATUS checkKey(String keyAlias, Key key);
    
    /**
     * Remove the set of keys from the registry.
     * 
     * @param keys Set<String>
     */
    public void removeRegisteredKeys(Set<String> keys);
    
    /**
     * Return those keys in the set that have been registered.
     *
     * @param keys Set<String>
     * @return List<String>
     */
    public List<String> getRegisteredKeys(Set<String> keys);
}
