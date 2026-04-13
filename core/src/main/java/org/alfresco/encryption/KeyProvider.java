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

/**
 * A key provider returns the secret keys for different use cases.
 * 
 * @since 4.0
 */
public interface KeyProvider
{
    // TODO: Allow the aliases to be configured i.e. include an alias mapper
    /**
     * Constant representing the keystore alias for keys to encrypt/decrypt node metadata
     */
    public static final String ALIAS_METADATA = "metadata";

    /**
     * Constant representing the keystore alias for keys to encrypt/decrypt SOLR transfer data
     */
    public static final String ALIAS_SOLR = "solr";
    
    /**
     * Get an encryption key if available.
     * 
     * @param keyAlias          the key alias
     * @return                  the encryption key and a timestamp of when it was last changed
     */
    public Key getKey(String keyAlias);
}
