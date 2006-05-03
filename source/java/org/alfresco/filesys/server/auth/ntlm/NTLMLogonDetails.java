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
package org.alfresco.filesys.server.auth.ntlm;

import java.util.Date;

import net.sf.acegisecurity.Authentication;

/**
 * NTLM Logon Details Class
 * 
 * <p>Contains the details from the NTLM authentication session that was used to authenticate a user.
 * 
 * @author GKSpencer
 */
public class NTLMLogonDetails
{
    // User name, workstation name and domain
    
    private String m_user;
    private String m_workstation;
    private String m_domain;
    
    // Authentication server name/address
    
    private String m_authSrvAddr;
    
    // Date/time the user was authenticated
    
    private long m_authTime;
    
    // User logged on via guest access
    
    private boolean m_guestAccess;
    
    // Cached type 2 NTLM message and copy of the NTLM hash used to successfully logon
    
    private Type2NTLMMessage m_type2Msg;
    private byte[] m_ntlmHash;
    
    // Authentication token, used for passthru mode
    
    private Authentication m_authToken;
    
    /**
     * Default constructor
     */
    public NTLMLogonDetails()
    {
    }
    
    /**
     * Class constructor
     *
     * @param user String
     * @param wks String
     * @param domain String
     * @param guest boolean
     * @param authSrv String
     */
    public NTLMLogonDetails(String user, String wks, String domain, boolean guest, String authSrv)
    {
        setDetails(user, wks, domain, guest, authSrv);
    }
    
    /**
     * Return the user name
     * 
     * @return String
     */
    public final String getUserName()
    {
        return m_user;
    }
    
    /**
     * Return the workstation name
     * 
     * @return String
     */
    public final String getWorkstation()
    {
        return m_workstation;
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
     * Return the authentication server name/address
     * 
     * @return String
     */
    public final String getAuthenticationServer()
    {
        return m_authSrvAddr;
    }

    /**
     * Return the date/time the user was authenticated
     * 
     * @return long
     */
    public final long authenticatedAt()
    {
        return m_authTime;
    }
    
    /**
     * Determine if the type 2 NTLM message has been cached
     * 
     * @return boolean
     */
    public final boolean hasType2Message()
    {
        return m_type2Msg != null ? true : false;
    }
    
    /**
     * Return the cached type 2 NTLM message
     * 
     * @return Type2NTLMMessage
     */
    public final Type2NTLMMessage getType2Message()
    {
        return m_type2Msg;
    }
    
    /**
     * Determine if there is a cached NTLM hashed password
     * 
     * @return boolean
     */
    public final boolean hasNTLMHashedPassword()
    {
        return m_ntlmHash != null ? true : false;
    }
    
    /**
     * Return the cached NTLM hashed password
     * 
     * @return byte[]
     */
    public final byte[] getNTLMHashedPassword()
    {
        return m_ntlmHash;
    }
    
    /**
     * Return the challenge key from the type2 message
     * 
     * @return byte[]
     */
    public final byte[] getChallengeKey()
    {
        if ( m_type2Msg != null)
            return m_type2Msg.getChallenge();
        return null;
    }
    
    /**
     * Determine if the passthru authentication token is valid
     * 
     * @return boolean
     */
    public final boolean hasAuthenticationToken()
    {
        return m_authToken != null ? true : false;
    }
    
    /**
     * Return the authentication token
     * 
     * @return Authentication
     */
    public final Authentication getAuthenticationToken()
    {
        return m_authToken;
    }
    
    /**
     * Set the authentication date/time
     * 
     * @param authTime long
     */
    public final void setAuthenticatedAt(long authTime)
    {
        m_authTime = authTime;
    }

    /**
     * Set the client details
     * 
     * @param user String
     * @param wks String
     * @param domain String
     * @param guest boolean
     * @param authSrv String
     */
    public final void setDetails(String user, String wks, String domain, boolean guest, String authSrv)
    {
        m_user = user;
        m_workstation = wks;
        m_domain = domain;

        m_authSrvAddr = authSrv;
        
        m_guestAccess = guest;
        
        m_authTime = System.currentTimeMillis();
    }
    
    /**
     * Set the type 2 NTLM message
     * 
     * @param type2 Type2NTLMMessage
     */
    public final void setType2Message(Type2NTLMMessage type2)
    {
        m_type2Msg = type2;
    }
    
    /**
     * Set the cached NTLM hashed password
     * 
     * @param ntlmHash byte[]
     */
    public final void setNTLMHashedPassword(byte[] ntlmHash)
    {
        m_ntlmHash = ntlmHash;
    }
    
    /**
     * Set the passthru authentication token
     * 
     * @param token Authentication
     */
    public final void setAuthenticationToken(Authentication token)
    {
        m_authToken = token;
    }
    
    /**
     * Return the NTLM logon details as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[");
        str.append(getUserName());
        str.append(",Wks:");
        str.append(getWorkstation());
        str.append(",Dom:");
        str.append(getDomain());
        str.append(",AuthSrv:");
        str.append(getAuthenticationServer());
        str.append(",");
        str.append(new Date(authenticatedAt()));
        
        if ( hasAuthenticationToken())
        {
            str.append(",token=");
            str.append(getAuthenticationToken());
        }
        
        str.append("]");
        
        return str.toString();
    }
}
