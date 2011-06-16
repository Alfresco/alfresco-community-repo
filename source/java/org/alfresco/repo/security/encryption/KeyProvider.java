package org.alfresco.repo.security.encryption;

import java.security.Key;

/**
 * A key provider returns the secret keys for different use cases.
 * 
 * @since 4.0
 */
public interface KeyProvider
{
    /**
     * Enumeration of key aliases supported internally by Alfresco
     * 
     * @author derekh
     * @since 4.0
     */
    public static enum AlfrescoKeyAlias
    {
        METADATA,
        SOLR
    }
    
    /**
     * Get an encryption key if available.
     * 
     * @param keyAlias          the key alias
     * @return                  the encryption key or <tt>null</tt> if there is no associated key
     */
    public Key getKey(String keyAlias);
    
    /**
     * Get an encryption key if available, using a convenience constant.
     * 
     * @param keyAlias          the key alias
     * @return                  the encryption key or <tt>null</tt> if there is no associated key
     */
    public Key getKey(AlfrescoKeyAlias keyAlias);
}
