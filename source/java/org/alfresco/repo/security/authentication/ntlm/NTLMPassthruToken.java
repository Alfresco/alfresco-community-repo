/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
