package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.providers.encoding.BaseDigestPasswordEncoder;


/**
 * <p>
 * NoOp implementation of PasswordEncoder.
 * </p>
 * The No Op Password Encoder produces a blank hash.  And will not match any value of hash.  
 * Used to replace an obsolete encoder like the MD4.
 * </p>
 */
public class NoOpPasswordEncoderImpl extends BaseDigestPasswordEncoder implements MD4PasswordEncoder
{
    
    public NoOpPasswordEncoderImpl()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    // ~ Methods
    // ================================================================

    public boolean isPasswordValid(String encPass, String rawPass, Object salt)
    {
       return false;
    }

    public String encodePassword(String rawPass, Object salt)
    {
        return "";
    }

    public byte[] decodeHash(String encodedHash)
    {
        return new byte[0];
    }

}
