/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
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
