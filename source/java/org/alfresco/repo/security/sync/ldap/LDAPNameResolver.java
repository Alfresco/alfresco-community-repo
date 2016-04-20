package org.alfresco.repo.security.sync.ldap;

import org.alfresco.repo.security.authentication.AuthenticationDiagnostic;
import org.alfresco.repo.security.authentication.AuthenticationException;

/**
 * An interface for objects capable of resolving user IDs to full LDAP Distinguished Names (DNs).
 * 
 * @author dward
 */
public interface LDAPNameResolver
{
    
    /**
     * Resolves a user ID to a distinguished name.
     * 
     * @param userId
     *            the user id
     * @return the DN
     * @throws AuthenticationException
     *             if the user ID cannot be resolved
     */
    public String resolveDistinguishedName(String userId, AuthenticationDiagnostic diagnostic) throws AuthenticationException;
}
