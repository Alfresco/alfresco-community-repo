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
package org.alfresco.repo.security.authentication.ntlm;

import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.providers.*;

/**
 * <p>Used to provide authentication with a remote Windows server when the username and password are
 * provided locally.
 * 
 * @author GKSpencer
 */
public class NTLMLocalToken extends UsernamePasswordAuthenticationToken
{
    private static final long serialVersionUID = -7946514578455279387L;

    /**
     * Class constructor
     */
    protected NTLMLocalToken()
    {
        super(null, null);
    }
    
    /**
     * Class constructor
     * 
     * @param username String
     * @param plainPwd String
     */
    public NTLMLocalToken(String username, String plainPwd) {
        super(username.toLowerCase(), plainPwd);
    }
    
    /**
     * Check if the user logged on as a guest
     * 
     * @return boolean
     */
    public final boolean isGuestLogon()
    {
        return hasAuthority(NTLMAuthenticationProvider.NTLMAuthorityGuest);
    }

    /**
     * Check if the user is an administrator
     * 
     * @return boolean
     */
    public final boolean isAdministrator()
    {
        return hasAuthority(NTLMAuthenticationProvider.NTLMAuthorityAdministrator);
    }
    
    /**
     * Search for the specified authority
     * 
     * @param authority String
     * @return boolean
     */
    public final boolean hasAuthority(String authority)
    {
        boolean found = false;
        GrantedAuthority[] authorities = getAuthorities();
        
        if ( authorities != null && authorities.length > 0)
        {
            // Search for the specified authority
            
            int i = 0;
            
            while ( found == false && i < authorities.length)
            {
                if ( authorities[i++].getAuthority().equals(authority))
                    found = true;
            }
        }

        // Return the status
        
        return found;
    }
}
