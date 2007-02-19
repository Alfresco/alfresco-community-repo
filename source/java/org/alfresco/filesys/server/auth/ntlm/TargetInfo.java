/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.filesys.server.auth.ntlm;

/**
 * Target Information Class
 * 
 * <p>Contains the target information from an NTLM message.
 * 
 * @author GKSpencer
 */
public class TargetInfo
{
    // Target type and name
    
    private int m_type;
    private String m_name;
    
    /**
     * Class constructor
     * 
     * @param type int
     * @param name String
     */
    public TargetInfo(int type, String name)
    {
        m_type = type;
        m_name = name;
    }
    
    /**
     * Return the target type
     * 
     * @return int
     */
    public final int isType()
    {
        return m_type;
    }
    
    /**
     * Return the target name
     * 
     * @return String
     */
    public final String getName()
    {
        return m_name;
    }
    
    /**
     * Return the target information as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[");
        str.append(getTypeAsString(isType()));
        str.append(":");
        str.append(getName());
        str.append("]");
        
        return str.toString();
    }
    
    /**
     * Return the target type as a string
     * 
     * @param typ int
     * @return String
     */
    public final static String getTypeAsString(int typ)
    {
        String typStr = null;
        
        switch ( typ)
        {
        case NTLM.TargetServer:
            typStr = "Server";
            break;
        case NTLM.TargetDomain:
            typStr = "Domain";
            break;
        case NTLM.TargetFullDNS:
            typStr = "DNS";
            break;
        case NTLM.TargetDNSDomain:
            typStr = "DNS Domain";
            break;
        default:
            typStr = "Unknown 0x" + Integer.toHexString(typ);
            break;
        }
        
        return typStr;
    }
}
