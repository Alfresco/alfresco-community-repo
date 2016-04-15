package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.AuthenticationException;
import net.sf.acegisecurity.providers.AuthenticationProvider;

public class AuthenticatedAuthenticationPassthroughProvider implements AuthenticationProvider
{

    public AuthenticatedAuthenticationPassthroughProvider()
    {
        super();
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        if (!supports(authentication.getClass())) {
            return null;
        }
        if(authentication.isAuthenticated())
        {
            return authentication;
        }
        else
        {
            return null;
        }
    }

    public boolean supports(Class authentication)
    {
        return (Authentication.class.isAssignableFrom(authentication));
    }

}
