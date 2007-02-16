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

import java.security.PrivilegedAction;

import org.alfresco.filesys.server.auth.spnego.OID;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;

/**
 * Session Setup Privileged Action Class
 *
 * <p>Handle the processing of a received SPNEGO packet in the context of the CIFS server.
 * 
 * @author gkspencer
 */
public class SessionSetupPrivilegedAction implements PrivilegedAction
{
    //  Received security blob details
    
    private byte[] m_secBlob;
    private int m_secOffset;
    private int m_secLen;

    // CIFS server account name
    
    private String m_accountName;
    
    /**
     * Class constructor
     * 
     * @param accountName String
     * @param secBlob byte[]
     */
    public SessionSetupPrivilegedAction ( String accountName, byte[] secBlob)
    {
        m_accountName = accountName;
        
        m_secBlob   = secBlob;
        m_secOffset = 0;
        m_secLen    = secBlob.length;
    }
    
    /**
     * Class constructor
     * 
     * @param accountName String
     * @param secBlob byte[]
     * @param secOffset int
     * @param secLen int
     */
    public SessionSetupPrivilegedAction ( String accountName, byte[] secBlob, int secOffset, int secLen)
    {
        m_accountName = accountName;
        
        m_secBlob   = secBlob;
        m_secOffset = secOffset;
        m_secLen    = secLen;
    }
    
    /**
     * Run the privileged action
     */
    public Object run()
    {
        KerberosDetails krbDetails = null;
        
        try
        {
            GSSManager gssManager = GSSManager.getInstance();
            GSSName serverGSSName = gssManager.createName(m_accountName, GSSName.NT_USER_NAME);
            GSSCredential serverGSSCreds = gssManager.createCredential( serverGSSName, GSSCredential.INDEFINITE_LIFETIME,
                    OID.KERBEROS5, GSSCredential.ACCEPT_ONLY);
            
            GSSContext serverGSSContext = gssManager.createContext( serverGSSCreds);
            
            // Accept the incoming security blob and generate the response blob
            
            byte[] respBlob = serverGSSContext.acceptSecContext( m_secBlob, m_secOffset, m_secLen);

            // Create the Kerberos response details

            krbDetails = new KerberosDetails( serverGSSContext.getSrcName(), serverGSSContext.getTargName(), respBlob);
        }
        catch (GSSException ex)
        {
            System.out.println("GSSException: " + ex.getMajorString());
            System.out.println("  " + ex.getMessage());
        }
        
        // Return the Kerberos response
        
        return krbDetails;
    }
}
