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
