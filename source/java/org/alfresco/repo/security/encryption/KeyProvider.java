package org.alfresco.repo.security.encryption;

import java.security.Key;

/**
 * A key provider returns the secret key used to encrypt text and mltext properties in the
 * database.
 *
 */
public interface KeyProvider
{
    public Key getKey();
}
