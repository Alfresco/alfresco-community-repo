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
