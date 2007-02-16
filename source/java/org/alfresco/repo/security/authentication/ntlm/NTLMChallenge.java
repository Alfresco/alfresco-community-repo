/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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

import org.alfresco.filesys.util.HexDump;

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
