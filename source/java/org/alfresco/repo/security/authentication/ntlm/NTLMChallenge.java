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
