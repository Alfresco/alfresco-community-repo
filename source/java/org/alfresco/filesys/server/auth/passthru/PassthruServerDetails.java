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
