package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.providers.encoding.PasswordEncoder;

public interface MD4PasswordEncoder extends PasswordEncoder
{
    /**
     * Get the MD4 byte array 
     * 
     * @param encodedHash String
     * @return byte[]
     */
    public byte[] decodeHash(String encodedHash);
}