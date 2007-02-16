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
package org.alfresco.filesys.server.auth.spnego;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

/**
 * OID Class
 * 
 * <p>Contains Oids used by SPNEGO
 * 
 * @author gkspencer
 */
public class OID
{
    // IDs
    
    public static final String ID_SPNEGO       = "1.3.6.1.5.5.2";
    
    // Kerberos providers
    
    public static final String ID_KERBEROS5    = "1.2.840.113554.1.2.2";
    public static final String ID_MSKERBEROS5  = "1.2.840.48018.1.2.2";
    
    // Microsoft NTLM security support provider
    
    public static final String ID_NTLMSSP      = "1.3.6.1.4.1.311.2.2.10";
    
    // OIDs
    
    public static Oid SPNEGO; 

    public static Oid KERBEROS5;
    public static Oid MSKERBEROS5;
    
    public static Oid NTLMSSP;
    
    /**
     * Static initializer
     */
    
    static {

        // Create the OIDs
        
        try
        {
            SPNEGO = new Oid(ID_SPNEGO);
            
            KERBEROS5   = new Oid(ID_KERBEROS5);
            MSKERBEROS5 = new Oid( ID_MSKERBEROS5);
            
            NTLMSSP  = new Oid(ID_NTLMSSP);
        }
        catch ( GSSException ex)
        {
        }
    }
}
