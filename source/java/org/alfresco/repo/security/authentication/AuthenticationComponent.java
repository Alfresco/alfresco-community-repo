package org.alfresco.repo.security.authentication;

import java.util.Set;

import net.sf.acegisecurity.Authentication;

public interface AuthenticationComponent extends AuthenticationContext
{
    public enum UserNameValidationMode
    {
        NONE, CHECK, CHECK_AND_FIX;
    }
    
    /**
     * Authenticate
     * 
     * @throws AuthenticationException
     */     
    public void authenticate(String userName, char[] password) throws AuthenticationException;
    
    /**
     * Explicitly set the current user to be authenticated.
     */
    
    public Authentication setCurrentUser(String userName);
    
    /**
     * Explicitly set the current user to be authenticated.
     * Specify if the userName is to be checked and fixed
     */
    
    public Authentication setCurrentUser(String userName, UserNameValidationMode validationMode);
    
    
    /**
     * Set the guest user as the current user.
     */
    public Authentication setGuestUserAsCurrentUser();
    
    
    /**
     * True if Guest user authentication is allowed, false otherwise
     */
    public boolean guestUserAuthenticationAllowed();
    
    /**
     * Gets a set of user names who for this particular authentication system should be considered administrators by
     * default. If the security framework is case sensitive these values should be case sensitive user names. If the
     * security framework is not case sensitive these values should be the lower-case user names.
     * 
     * @return a set of user names
     */
    public Set<String> getDefaultAdministratorUserNames();
    
    /**
     * Gets a set of user names who for this particular authentication system should be considered guests by
     * default. If the security framework is case sensitive these values should be case sensitive user names. If the
     * security framework is not case sensitive these values should be the lower-case user names.
     * 
     * @return a set of user names
     */
    public Set<String> getDefaultGuestUserNames();
}
