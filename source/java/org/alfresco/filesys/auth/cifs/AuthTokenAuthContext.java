package org.alfresco.filesys.auth.cifs;

import org.alfresco.jlan.server.auth.ChallengeAuthContext;
import org.alfresco.repo.security.authentication.ntlm.NTLMPassthruToken;

/**
 * Authenitcation Token Authentication Context Class
 * 
 * @author gkspencer
 */
public class AuthTokenAuthContext extends ChallengeAuthContext {

    // Passthru authentication token
    
    private NTLMPassthruToken m_token;
    
    /**
     * Class constructor
     * 
     * @param token NTLMPassthruToken
     */
    public AuthTokenAuthContext( NTLMPassthruToken token)
    {
        m_token = token;
    }
    
    /**
     * Return the passthru authentication token
     * 
     * @return NTLMPassthruToken
     */
    public final NTLMPassthruToken getToken()
    {
        return m_token;
    }
    
    /**
     * Get the challenge
     * 
     * return byte[]
     */
    public byte[] getChallenge() {
        return m_token.getChallenge().getBytes();
    }
}
