package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.dao.DaoAuthenticationProvider;
import net.sf.acegisecurity.providers.dao.SaltSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A DaoAuthenticationProvider that makes use of a CompositePasswordEncoder to check the
 * password is correct.
 *
 * @author Gethin James
 */
public class RepositoryAuthenticationProvider extends DaoAuthenticationProvider
{
    private static Log logger = LogFactory.getLog(RepositoryAuthenticationProvider.class);
    CompositePasswordEncoder compositePasswordEncoder;

    public void setCompositePasswordEncoder(CompositePasswordEncoder compositePasswordEncoder)
    {
        this.compositePasswordEncoder = compositePasswordEncoder;
    }

    @Override
    protected boolean isPasswordCorrect(Authentication authentication, UserDetails user)
    {
        if (user instanceof RepositoryAuthenticatedUser)
        {
            RepositoryAuthenticatedUser repoUser = (RepositoryAuthenticatedUser) user;
            return compositePasswordEncoder.matchesPassword(authentication.getCredentials().toString(),user.getPassword(), repoUser.getSalt(), repoUser.getHashIndicator() );
        }

        logger.error("Password check error for "+user.getUsername()+" unknown user type: "+user.getClass().getName());
        return false;
    }
}
