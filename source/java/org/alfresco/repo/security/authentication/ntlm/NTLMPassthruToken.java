/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.security.authentication.ntlm;

/**
 * <p>Used to provide passthru authentication to a remote Windows server using multiple stages that
 * allows authentication details to be passed between a client and the remote authenticating server without
 * the password being known by the authentication provider.
 * 
 * @author GKSpencer
 */
public class NTLMPassthruToken extends NTLMLocalToken
{
    private static final long serialVersionUID = -4635444888514735368L;

    // Challenge for this session
    
    private NTLMChallenge m_challenge;
    
    // User name, hashed password and algorithm type

    private String m_username;
    private byte[] m_hashedPassword;
    private int m_hashType;

    // Time that the authentication session will expire
    
    private long m_authExpiresAt;
    
    /**
     * Class constructor
     */
    public NTLMPassthruToken()
    {
        // We do not know the username yet, and will not know the password
        
        super("", "");
    }
    
    /**
     * Return the challenge
     * 
     * @return NTLMChallenge
     */
    public final NTLMChallenge getChallenge()
    {
        return m_challenge;
    }

    /**
     * Return the user account
     * 
     * @return Object
     */
    public final Object getPrincipal()
    {
        return m_username;
    }
    
    /**
     * Return the hashed password
     * 
     * @return byte[]
     */
    public final byte[] getHashedPassword()
    {
        return m_hashedPassword;
    }
    
    /**
     * Return the hashed password type
     * 
     * @return int
     */
    public final int getPasswordType()
    {
        return m_hashType;
    }

    /**
     * Return the authentication expiry time, this will be zero if the authentication session has not yet
     * been opened to the server
     * 
     * @return long
     */
    public final long getAuthenticationExpireTime()
    {
        return m_authExpiresAt;
    }
    
    /**
     * Set the hashed password and type
     * 
     * @param hashedPassword byte[]
     * @param hashType int
     */
    public final void setUserAndPassword(String username, byte[] hashedPassword, int hashType)
    {
        m_username       = username.toLowerCase();
        m_hashedPassword = hashedPassword;
        m_hashType       = hashType;
    }
    
    /**
     * Set the challenge for this token
     * 
     * @param challenge NTLMChallenge
     */
    protected final void setChallenge(NTLMChallenge challenge)
    {
        m_challenge = challenge;
    }
    
    /**
     * Set the authentication expire time, this indicates that an authentication session is associated with this
     * token and the session will be closed if the authentication is not completed by this time.
     * 
     * @param startTime long
     */
    protected final void setAuthenticationExpireTime(long expireTime)
    {
        m_authExpiresAt = expireTime;
    }
    
    /**
     * Check for object equality
     * 
     * @param obj Object
     * @return boolean
     */
    public boolean equals(Object obj)
    {
        // Only match on the same object
        
        return this == obj;
    }
}
