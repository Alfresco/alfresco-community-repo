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
