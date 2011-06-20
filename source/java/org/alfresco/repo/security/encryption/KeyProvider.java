package org.alfresco.repo.security.encryption;

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
     * Constant representing the keystore alias for keys to encrypt/decrype node metadata
     */
    public static final String ALIAS_METADATA = "metadata";
    /**
     * Constant representing the keystore alias for keys to encrypt/decrype SOLR transfer data
     */
    public static final String ALIAS_SOLR = "solr";
    
    /**
     * Get an encryption key if available.
     * 
     * @param keyAlias          the key alias
     * @return                  the encryption key or <tt>null</tt> if there is no associated key
     */
    public Key getKey(String keyAlias);
}
