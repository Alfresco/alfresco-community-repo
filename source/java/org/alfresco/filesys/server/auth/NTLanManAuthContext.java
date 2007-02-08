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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.server.auth;

import java.util.Random;

import org.alfresco.filesys.util.DataPacker;
import org.alfresco.filesys.util.HexDump;

/**
 * NTLM1/LanMan CIFS Authentication Context Class
 * 
 * <p>Holds the challenge sent to the client during the negotiate phase that is used to verify the hashed password
 * in the session setup phase.
 *  
 * @author gkspencer
 */
public class NTLanManAuthContext extends AuthContext
{
    // Random number generator used to generate challenge
    
    private static Random m_random = new Random(System.currentTimeMillis());
    
    // Challenge sent to client
    
    private byte[] m_challenge;
    
    /**
     * Class constructor
     */
    public NTLanManAuthContext()
    {
        // Generate a new challenge key, pack the key and return
        
        m_challenge = new byte[8];
        DataPacker.putIntelLong(m_random.nextLong(), m_challenge, 0);
    }
    
    /**
     * Class constructor
     * 
     * @param challenge byte[]
     */
    public NTLanManAuthContext( byte[] challenge)
    {
        m_challenge = challenge;
    }
    
    /**
     * Get the challenge
     * 
     * return byte[]
     */
    public final byte[] getChallenge()
    {
        return m_challenge;
    }
    
    /**
     * Return the CIFS authentication context as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[NTLM,Challenge=");
        str.append(HexDump.hexString(m_challenge));
        str.append("]");
        
        return str.toString();
    }
}
