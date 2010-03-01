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

import org.alfresco.jlan.util.HexDump;

/**
 * Contains the NTLM challenge bytes.
 * 
 * @author GKSpencer
 */
public class NTLMChallenge
{
    // Challenge bytes
    
    private byte[] m_challenge;
    
    /**
     * Class constructor
     * 
     * @param chbyts byte[]
     */
    protected NTLMChallenge(byte[] chbyts)
    {
        m_challenge = chbyts;
    }
    
    /**
     * Return the challenge bytes
     * 
     * @return byte[]
     */
    public final byte[] getBytes()
    {
        return m_challenge;
    }
    
    /**
     * Check for object equality
     * 
     * @param obj Object
     * @return boolean
     */
    public boolean equals(Object obj)
    {
        if ( obj instanceof NTLMChallenge)
        {
            NTLMChallenge ntlmCh = (NTLMChallenge) obj;
            
            // Check if both challenges are null
            
            if ( getBytes() == null && ntlmCh.getBytes() == null)
                return true;
            
            // Check if both challenges are the same length
            
            if ( getBytes() != null && ntlmCh.getBytes() != null &&
                    getBytes().length == ntlmCh.getBytes().length)
            {
                // Check if challenages are the same value
                
                byte[] ntlmBytes = ntlmCh.getBytes();
                
                for ( int i = 0; i < m_challenge.length; i++)
                    if ( m_challenge[i] != ntlmBytes[i])
                        return false;
            }
            else
                return false;
        }
        
        // Not the same type
        
        return false;
    }
    
    /**
     * Return the challenge as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[");
        str.append(HexDump.hexString(getBytes(), " "));
        str.append("]");
        
        return str.toString();
    }
}
