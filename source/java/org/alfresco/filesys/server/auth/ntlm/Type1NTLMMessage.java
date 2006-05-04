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
package org.alfresco.filesys.server.auth.ntlm;

/**
 * Type1 NTLM Message Class
 * 
 * @author GKSpencer
 */
public class Type1NTLMMessage extends NTLMMessage
{
    // Minimal type 1 message length
    
    public static final int MinimalMessageLength = 16;
    
    // Type 1 field offsets
    
    public static final int OffsetFlags                 = 12;
    public static final int OffsetData                  = 16;
    
    /**
     * Default constructor
     */
    public Type1NTLMMessage()
    {
        super();
    }
    
    /**
     * Class constructor
     * 
     * @param buf byte[]
     */
    public Type1NTLMMessage(byte[] buf)
    {
        super(buf, 0, buf.length);
    }
    
    /**
     * Class constructor
     * 
     * @param buf byte[]
     * @param offset int
     * @param len int
     */
    public Type1NTLMMessage(byte[] buf, int offset, int len)
    {
        super(buf, offset, len);
    }

    /**
     * Return the flags value
     * 
     * @return int
     */
    public int getFlags()
    {
        return getIntValue(OffsetFlags);
    }
    
    /**
     * Check if the domain security buffer is included
     * 
     * @return boolean
     */
    public final boolean hasDomain()
    {
        if ( getLength() == MinimalMessageLength || hasFlag(NTLM.FlagDomainSupplied) == false)
            return false;
        return true;
    }
    
    /**
     * Return the domain name
     * 
     * @return String
     */
    public final String getDomain()
    {
        if ( hasFlag(NTLM.FlagDomainSupplied) == false)
            return null;
        
        return getStringValue(OffsetData, false);
    }
    
    /**
     * Check if the workstation security buffer is included
     * 
     * @return boolean
     */
    public final boolean hasWorkstation()
    {
        if ( getLength() == MinimalMessageLength || hasFlag(NTLM.FlagWorkstationSupplied) == false)
            return false;
        return true;
    }
    
    /**
     * Return the workstation name
     * 
     * @return String
     */
    public final String getWorkstation()
    {
        if ( hasFlag(NTLM.FlagWorkstationSupplied) == false)
            return null;
        
        int bufPos = OffsetData;
        if ( hasFlag(NTLM.FlagDomainSupplied))
            bufPos += BufferHeaderLen;
            
        return getStringValue(bufPos, false);
    }
    
    /**
     * Build a type 1 message
     * 
     * @param flags int
     * @param domain String
     * @param workstation String
     */
    public final void buildType1(int flags, String domain, String workstation)
    {
        int bufPos = OffsetData;
        int strOff = OffsetData;
        
        if ( domain != null)
            strOff += BufferHeaderLen;
        if ( workstation != null)
            strOff += BufferHeaderLen;

        // Pack the domain name
        
        if ( domain != null)
        {
            strOff = setStringValue(bufPos, domain, strOff, false);
            flags |= NTLM.FlagDomainSupplied;
            bufPos += BufferHeaderLen;
        }

        // Pack the workstation name

        if ( workstation != null)
        {
            strOff = setStringValue(bufPos, workstation, strOff, false);
            flags |= NTLM.FlagWorkstationSupplied;
        }

        // Initialize the header/flags
        
        initializeHeader(NTLM.Type1, flags);
        
        // Set the message length
        
        setLength(strOff);
    }
    
    /**
     * Set the message flags
     * 
     * @param flags int
     */
    protected void setFlags(int flags)
    {
        setIntValue( OffsetFlags, flags);
    }
    
    /**
     * Return the type 1 message as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[Type1:0x");
        str.append(Integer.toHexString(getFlags()));
        str.append(",Domain:");
        if ( hasDomain())
            str.append(getDomain());
        else
            str.append("<NotSet>");
        str.append(",Wks:");
        
        if ( hasWorkstation())
            str.append(getWorkstation());
        else
            str.append("<NotSet>");
        str.append("]");
        
        return str.toString();
    }
}
