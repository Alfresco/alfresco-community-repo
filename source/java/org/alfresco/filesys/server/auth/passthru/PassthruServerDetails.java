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
package org.alfresco.filesys.server.auth.passthru;

import java.net.*;
import java.util.*;

/**
 * <p>Contains the details of a server used for passthru authentication, the current status of the server
 * and count of authentications done via this server.
 * 
 * @author GKSpencer
 */
public class PassthruServerDetails
{
    // Server details
    
    private String m_name;
    private String m_domain;
    private InetAddress m_address;
    
    // Server status
    
    private boolean m_online;
    
    // Authentication statistics
    
    private int m_authCount;
    private long m_lastAuthTime;
    
    /**
     * Class constructor
     * 
     * @param name String
     * @param domain String
     * @param addr InetAddress
     * @param online boolean
     */
    PassthruServerDetails(String name, String domain, InetAddress addr, boolean online)
    {
        m_name    = name;
        m_domain  = domain;
        m_address = addr;
        m_online  = online;
    }
    
    /**
     * Return the server name
     * 
     * @return String
     */
    public final String getName()
    {
        return m_name;
    }
    
    /**
     * Return the domain name
     * 
     * @return String
     */
    public final String getDomain()
    {
        return m_domain;
    }
    
    /**
     * Return the server address
     * 
     * @return InetAddress
     */
    public final InetAddress getAddress()
    {
        return m_address;
    }
    
    /**
     * Return the online status of the server
     * 
     * @return boolean
     */
    public final boolean isOnline()
    {
        return m_online;
    }
    
    /**
     * Return the authentication count for the server
     * 
     * @return int
     */
    public final int getAuthenticationCount()
    {
        return m_authCount;
    }
    
    /**
     * Return the date/time of the last authentication by this server
     * 
     * @return long
     */
    public final long getAuthenticationDateTime()
    {
        return m_lastAuthTime;
    }
    
    /**
     * Set the online status for the server
     * 
     * @param online boolean
     */
    public final void setOnline(boolean online)
    {
        m_online = online;
    }
    
    /**
     * Update the authentication count and date/time
     */
    public synchronized final void incrementAuthenticationCount()
    {
        m_authCount++;
        m_lastAuthTime = System.currentTimeMillis();
    }
    
    /**
     * Return the hash code for this object
     * 
     * @return int
     */
    public int hashCode()
    {
        return m_address.hashCode();
    }
    
    /**
     * Return the passthru server details as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[");
        if ( getDomain() != null)
        {
            str.append(getDomain());
            str.append("\\");
        }
        str.append(getName());
        
        str.append(":");
        str.append(getAddress().getHostAddress());
        
        str.append(isOnline() ? ":Online" : ":Offline");
        
        str.append(":");
        str.append(getAuthenticationCount());
        str.append(",");
        str.append(getAuthenticationDateTime() != 0L ? new Date(getAuthenticationDateTime()) : "0");
        str.append("]");
        
        return str.toString();
    }
}
