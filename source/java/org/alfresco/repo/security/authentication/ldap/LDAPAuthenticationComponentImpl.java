/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authentication.ldap;

import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.sync.ldap.LDAPNameResolver;
import org.springframework.beans.factory.InitializingBean;

/**
 * Authenticates a user by LDAP. To convert the user name to an LDAP DN, it uses the fixed format in
 * <code>userNameFormat</code> if set, or calls the {@link LDAPNameResolver} otherwise.
 * 
 * @author Andy Hind
 */
public class LDAPAuthenticationComponentImpl extends AbstractAuthenticationComponent implements InitializingBean,
        ActivateableBean
{
    private boolean escapeCommasInBind = false;
    
    private boolean escapeCommasInUid = false;
    
    private boolean active = true;

    private String userNameFormat;
    
    private LDAPNameResolver ldapNameResolver;

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
        this.userNameFormat = userNameFormat == null || userNameFormat.length() == 0 ? null : userNameFormat;
    }
        
    public void setLdapNameResolver(LDAPNameResolver ldapNameResolver)
    {
        this.ldapNameResolver = ldapNameResolver;
    }

    public void setEscapeCommasInBind(boolean escapeCommasInBind)
    {
        this.escapeCommasInBind = escapeCommasInBind;
    }

    public void setEscapeCommasInUid(boolean escapeCommasInUid)
    {
        this.escapeCommasInUid = escapeCommasInUid;
    }
    
    public void setActive(boolean active)
    {
        this.active = active;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
     */
    public boolean isActive()
    {
        return this.active;
    }        

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        if (this.ldapNameResolver == null && this.userNameFormat == null)
        {
            throw new IllegalStateException("At least one of ldapNameResolver and userNameFormat must be set");
        }
    }

    /**
     * Implement the authentication method
     */
    protected void authenticateImpl(String userName, char[] password) throws AuthenticationException
    {
        // If we aren't using a fixed name format, do a search to resolve the user DN
        String userDN = userNameFormat == null ? ldapNameResolver.resolveDistinguishedName(userName) : String.format(
                userNameFormat, new Object[]
                {
                    escapeUserName(userName, escapeCommasInBind)
                });

        InitialDirContext ctx = null;
        try
        {
            ctx = ldapInitialContextFactory.getInitialDirContext(userDN, new String(password));

            // Authentication has been successful.
            // Set the current user, they are now authenticated.
            setCurrentUser(escapeUserName(userName, escapeCommasInUid));

        }
        finally
        {
            if (ctx != null)
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

    private static String escapeUserName(String userName, boolean escape)
    {
        if (escape)
        {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < userName.length(); i++)
            {
                char c = userName.charAt(i);
                if (c == ',')
                {
                    sb.append('\\');
                }
                sb.append(c);
            }
            return sb.toString();

        }
        else
        {
            return userName;
        }

    }

    @Override
    protected boolean implementationAllowsGuestLogin()
    {
        return true;
    }
}
