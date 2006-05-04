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
