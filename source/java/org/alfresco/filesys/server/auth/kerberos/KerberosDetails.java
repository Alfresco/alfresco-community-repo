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
package org.alfresco.filesys.server.auth.kerberos;

import org.ietf.jgss.GSSName;

/**
 * Kerberos Details Class
 * 
 * <p>Holds the Kerberos response token and session details about the user.
 * 
 * @author gkspencer
 */
public class KerberosDetails
{
    // Source and target details
    
    private String m_krbSource;
    private String m_krbTarget;
    
    // Kerberos response token
    
    private byte[] m_krbResponse;
    
    /**
     * Class constructor
     * 
     * @param source GSSName
     * @param target GSSName
     * @param response byte[]
     */
    public KerberosDetails(GSSName source, GSSName target, byte[] response)
    {
        m_krbSource = source.toString();
        m_krbTarget = target.toString();
        
        m_krbResponse = response;
    }
    
    /**
     * Return the context initiator for the Kerberos authentication
     * 
     * @return String
     */
    public final String getSourceName()
    {
        return m_krbSource;
    }
    
    /**
     * Return the context acceptor for the Kerberos authentication
     * 
     * @return String
     */
    public final String getTargetName()
    {
        return m_krbTarget;
    }
    
    /**
     * Return the Kerberos response token
     * 
     * @return byte[]
     */
    public final byte[] getResponseToken()
    {
        return m_krbResponse;
    }
    
    /**
     * Parse the source name to return the user name part only
     * 
     * @return String
     */
    public final String getUserName()
    {
        String userName = m_krbSource;
        
        if ( m_krbSource != null)
        {
            int pos = m_krbSource.indexOf( '@');
            if ( pos != -1)
            {
                userName = m_krbSource.substring(0, pos);
            }
        }
        
        return userName;
    }
    
    /**
     * Return the response token length
     * 
     * @return int
     */
    public final int getResponseLength()
    {
        return m_krbResponse != null ? m_krbResponse.length : 0; 
    }
    
    /**
     * Return the Kerberos authentication details as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[Source=");
        str.append(getSourceName());
        str.append(",Target=");
        str.append(getTargetName());
        str.append(":Response=");
        str.append(getResponseLength());
        str.append(" bytes]");
        
        return str.toString();
    }
}
