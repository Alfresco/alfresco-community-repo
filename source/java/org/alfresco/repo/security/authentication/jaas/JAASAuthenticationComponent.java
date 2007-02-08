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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.security.authentication.jaas;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.LanguageCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;

/**
 * JAAS based authentication
 * 
 * The user name and password are picked up from login.
 * 
 * The other configurable parameters are:
 * realm - the authentication realm if required,
 * and the entry name to use from the login context.
 *
 * You will need to be familiar with the JAAS authentication process to set this up.
 * 
 * In summary you will need to configure java.security (in the lib/security directory of the jre you are using)
 * to find a jaas configuration.
 * 
 * This entry could be used if you want to put the login configuration in the same place (in the lib/security directory of the jre you are using)
 * 
 * <code>
 * login.config.url.1=file:${java.home}/lib/security/java.login.config
 * </code>
 * 
 * Example configuration entries for Kerberos would be:
 * 
 * <code>
 * Alfresco {
 *    com.sun.security.auth.module.Krb5LoginModule sufficient;
 * };
 *
 * com.sun.net.ssl.client {
 *    com.sun.security.auth.module.Krb5LoginModule sufficient;
 * };
 *
 * other {
 *    com.sun.security.auth.module.Krb5LoginModule sufficient;
 * };
 * </code>
 * 
 * This sets up authentication using Kerberos for Alfresco and some defaults that would use the same mechanism if sasl failed for example.
 * 
 * You could use kerberos and LDAP combined against an Active Directory server.
 * 
 * @author Andy Hind
 */
public class JAASAuthenticationComponent extends AbstractAuthenticationComponent
{

    /**
     * A key into the login config that defines the authentication mechamisms required.
     */
    private String jaasConfigEntryName = "Alfresco";
    
    /**
     * A default realm
     */
    private String realm = null;

    public JAASAuthenticationComponent()
    {
        super();
    }

    // Springification
    
    public void setJaasConfigEntryName(String jaasConfigEntryName)
    {
        this.jaasConfigEntryName = jaasConfigEntryName;
    }
    
   
    public void setRealm(String realm)
    {
        this.realm = realm;
    }

    /**
     * Jaas does not support guest login
     */
    @Override
    protected boolean implementationAllowsGuestLogin()
    {
        return false;
    }

    /**
     * Implement Authentication
     */
    public void authenticate(String userName, char[] password) throws AuthenticationException
    {

        LoginContext lc;
        try
        {
            lc = new LoginContext(jaasConfigEntryName, new SimpleCallback(userName, realm, password));
        }
        catch (LoginException e)
        {
            throw new AuthenticationException("Login Failed", e);
        }
        try
        {
            lc.login();
            // Login has gone through OK, set up the acegi context
            setCurrentUser(userName);
        }
        catch (LoginException e)
        {
            throw new AuthenticationException("Login Failed", e);
        }

    }

    /**
     * Simple call back class to support the common requirements.
     * 
     * @author Andy Hind
     */
    private static class SimpleCallback implements CallbackHandler
    {
        String userName;

        String realm;

        char[] password;

        SimpleCallback(String userName, String realm, char[] password)
        {
            this.userName = userName;
            this.realm = realm;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
        {
            for (int i = 0; i < callbacks.length; i++)
            {
                if (callbacks[i] instanceof AuthorizeCallback)
                {
                    AuthorizeCallback cb = (AuthorizeCallback) callbacks[i];
                    cb.setAuthorized(false);
                }
                else if (callbacks[i] instanceof LanguageCallback)
                {
                    LanguageCallback cb = (LanguageCallback) callbacks[i];
                    cb.setLocale(I18NUtil.getLocale());
                }
                else if (callbacks[i] instanceof NameCallback)
                {
                    NameCallback cb = (NameCallback) callbacks[i];
                    cb.setName(userName);
                }
                else if (callbacks[i] instanceof PasswordCallback)
                {
                    PasswordCallback cb = (PasswordCallback) callbacks[i];
                    cb.setPassword(password);
                }
                else if (callbacks[i] instanceof RealmCallback)
                {
                    RealmCallback cb = (RealmCallback) callbacks[i];
                    cb.setText(realm);
                }
                else
                {
                    throw new UnsupportedCallbackException(callbacks[i]);
                }
            }
        }
    }
}
