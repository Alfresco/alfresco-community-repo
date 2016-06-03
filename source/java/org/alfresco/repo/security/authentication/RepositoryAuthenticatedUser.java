package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.providers.dao.User;

import java.io.Serializable;
import java.util.List;

/**
 * A user authenticated by the Alfresco repository using RepositoryAuthenticationDao
 * @author Gethin James
 */
public class RepositoryAuthenticatedUser extends User
{
    private List<String> hashIndicator;
    private Serializable salt;

    public Serializable getSalt()
    {
        return salt;
    }

    public List<String> getHashIndicator()
    {
        return hashIndicator;

    }

    public RepositoryAuthenticatedUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, GrantedAuthority[] authorities, List<String> hashIndicator, Serializable salt) throws IllegalArgumentException
    {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.hashIndicator = hashIndicator;
        this.salt = salt;
    }

}
