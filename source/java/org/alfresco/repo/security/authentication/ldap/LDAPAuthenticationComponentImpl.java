/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.authentication.ldap;

import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationDiagnostic;
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
        // Distinguished name of user.
        String userDN;
        
        AuthenticationDiagnostic diagnostic = new AuthenticationDiagnostic();
        
        if(userNameFormat == null)
        {
            // If we aren't using a fixed name format, do a search to resolve the user DN
            userDN = ldapNameResolver.resolveDistinguishedName(userName, diagnostic);
            
            Object[] params = {userName, userDN};
            diagnostic.addStep(AuthenticationDiagnostic.STEP_KEY_LDAP_LOOKEDUP_USER, true, params);
        }
        else
        // Otherwise, use the format, but disallow leading or trailing whitespace in the user ID as this can result in
        // ghost users (MNT-2597)
        {
            if (!userName.equals(userName.trim()))
            {
                throw new AuthenticationException("Invalid user ID with leading or trailing whitespace");
            }
            // we are using a fixed name format, 
            userDN = String.format(
                    userNameFormat, new Object[]
                    {
                        escapeUserName(userName, escapeCommasInBind)
                    });
            Object[] params = {userName, userDN, userNameFormat};
            diagnostic.addStep(AuthenticationDiagnostic.STEP_KEY_LDAP_FORMAT_USER, true, params);
        }

        InitialDirContext ctx = null;
        try
        {
            ctx = ldapInitialContextFactory.getInitialDirContext(userDN, new String(password), diagnostic);

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
    
    private String id = "default";
    
    /**
     * Set the unique name of this ldap authentication component e.g. "managed,ldap1"
     * 
     * @param id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Get the unique name of this ldap authentication component e.g. "managed,ldap1";
     * @return the unique name of this ldap authentication component
     */
    String getId()
    {
        return id;
    }
}
