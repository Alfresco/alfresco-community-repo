package org.alfresco.repo.security.authentication;

/**
 * Implementations of this interface generate a password
 * 
 * @author glen johnson at Alfresco dot com
 */
public interface PasswordGenerator
{
    /**
     * Returns a generated password
     * 
     * @return the generated password
     */
    public String generatePassword();
}
