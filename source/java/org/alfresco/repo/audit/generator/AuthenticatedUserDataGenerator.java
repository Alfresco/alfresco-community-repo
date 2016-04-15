package org.alfresco.repo.audit.generator;

import java.io.Serializable;

import org.alfresco.repo.security.authentication.AuthenticationUtil;

/**
 * Gives back the currently-authenticated user.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuthenticatedUserDataGenerator extends AbstractDataGenerator
{
    /**
     * @return              Returns the currently-authenticated user
     */
    public Serializable getData() throws Throwable
    {
        return AuthenticationUtil.getFullyAuthenticatedUser();
    }
}
