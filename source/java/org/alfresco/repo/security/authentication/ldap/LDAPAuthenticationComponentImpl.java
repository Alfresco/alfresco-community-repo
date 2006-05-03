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
