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
