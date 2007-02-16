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
