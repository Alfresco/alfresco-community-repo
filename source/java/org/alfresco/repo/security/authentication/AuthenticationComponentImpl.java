/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.AuthenticationManager;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;

public class AuthenticationComponentImpl extends AbstractAuthenticationComponent
{
    private MutableAuthenticationDao authenticationDao;

    AuthenticationManager authenticationManager;

    public AuthenticationComponentImpl()
    {
        super();
    }

    /**
     * IOC
     * 
     * @param authenticationManager
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager)
    {
        this.authenticationManager = authenticationManager;
    }

    /**
     * IOC
     * 
     * @param authenticationDao
     */
    public void setAuthenticationDao(MutableAuthenticationDao authenticationDao)
    {
        this.authenticationDao = authenticationDao;
    }
    
    /**
     * Authenticate
     */
    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        try
        {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userName,
                    new String(password));
            authenticationManager.authenticate(authentication);
            setCurrentUser(userName);

        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            throw new AuthenticationException(ae.getMessage(), ae);
        }
    }

 
    /**
     * We actually have an acegi object so override the default method.  
     */
    protected UserDetails getUserDetails(String userName)
    {
        return (UserDetails) authenticationDao.loadUserByUsername(userName);
    }

    
    /**
     * Get the password hash from the DAO
     */
    public String getMD4HashedPassword(String userName)
    {
        return authenticationDao.getMD4HashedPassword(userName);
    }

    
    /**
     * This implementation supported MD4 password hashes. 
     */
    public NTLMMode getNTLMMode()
    {
        return NTLMMode.MD4_PROVIDER;
    }

    @Override
    protected boolean implementationAllowsGuestLogin()
    {
        return true;
    }
   
    
}
