package org.alfresco.repo.security.authentication.ntlm;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.NTLMMode;

/**
 * An specialized {@link AuthenticationComponent} that is capable of handling NTLM authentication directly, either by
 * 'passing through' to a domain server or by validating an MD4 hashed password. Unlike other authentication methods,
 * these operations cannot be chained and must be handled by a specific authentication component.
 * 
 * @author dward
 */
public interface NLTMAuthenticator extends AuthenticationComponent
{
    /**
     * Authenticate using a token.
     * 
     * @param token
     *            Authentication
     * @return Authentication
     * @throws AuthenticationException
     *             the authentication exception
     */
    public Authentication authenticate(Authentication token) throws AuthenticationException;

    /**
     * Get the enum that describes NTLM integration.
     * 
     * @return the NTLM mode
     */
    public NTLMMode getNTLMMode();

    /**
     * Get the MD4 password hash, as required by NTLM based authentication methods.
     * 
     * @param userName
     *            the user name
     * @return the m d4 hashed password
     */
    public String getMD4HashedPassword(String userName);
}
