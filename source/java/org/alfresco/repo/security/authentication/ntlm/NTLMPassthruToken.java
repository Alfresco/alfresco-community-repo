/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.authentication.ntlm;

import java.net.InetAddress;

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
     * Class constructor
     * 
     * @params domain String
     */
    public NTLMPassthruToken( String domain)
    {
        // We do not know the username yet, and will not know the password
        
        super("", "", domain, null);
    }
    
    /**
     * Class constructor
     * 
     * @param ipAddr InetAddress
     */
    public NTLMPassthruToken( InetAddress ipAddr)
    {
        super( ipAddr);
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
