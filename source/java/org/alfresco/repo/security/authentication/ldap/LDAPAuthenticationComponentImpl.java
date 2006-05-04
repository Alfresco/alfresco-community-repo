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
package org.alfresco.repo.security.authentication.ldap;

import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;

/**
 * Currently expects the cn name of the user which is in a fixed location.
 * 
 * @author Andy Hind
 */
public class LDAPAuthenticationComponentImpl extends AbstractAuthenticationComponent
{
    
    private String userNameFormat;
    
    private LDAPInitialDirContextFactory ldapInitialContextFactory;
    
    public LDAPAuthenticationComponentImpl()
    {
        super();
    }

    
    public void setLDAPInitialDirContextFactory(LDAPInitialDirContextFactory ldapInitialDirContextFactory)
    {
        this.ldapInitialContextFactory = ldapInitialDirContextFactory;
    }
    
    
    public void setUserNameFormat(String userNameFormat)
    {
        this.userNameFormat = userNameFormat;
    }
    
    /**
     * Implement the authentication method
     */
    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        InitialDirContext ctx = null;
        try
        {
            ctx = ldapInitialContextFactory.getInitialDirContext(String.format(userNameFormat, new Object[]{userName}), new String(password));
            
            // Authentication has been successful.
            // Set the current user, they are now authenticated.
            setCurrentUser(userName);         
            
        }
        finally
        {
            if(ctx != null)
            {
                try
                {
                    ctx.close();
                }
                catch (NamingException e)
                {
                    clearCurrentSecurityContext();
                    throw new AuthenticationException("Failed to close connection", e);
                }
            }
        }
    }


    @Override
    protected boolean implementationAllowsGuestLogin()
    {
        InitialDirContext ctx = null;
        try
        {
            ctx = ldapInitialContextFactory.getDefaultIntialDirContext();
            return true;       
            
        }
        catch(Exception e)
        {
            return false;
        }
        finally
        {
            if(ctx != null)
            {
                try
                {
                    ctx.close();
                }
                catch (NamingException e)
                {
                    throw new AuthenticationException("Failed to close connection", e);
                }
            }
        }
    }
    
    
}
