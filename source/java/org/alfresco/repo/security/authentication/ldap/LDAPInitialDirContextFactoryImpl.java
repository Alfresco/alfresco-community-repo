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
package org.alfresco.repo.security.authentication.ldap;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

public class LDAPInitialDirContextFactoryImpl implements LDAPInitialDirContextFactory, InitializingBean
{
    private static final Log logger = LogFactory.getLog(LDAPInitialDirContextFactoryImpl.class);

    private Map<String, String> initialDirContextEnvironment = Collections.<String, String> emptyMap();

    static
    {
        System.setProperty("javax.security.auth.useSubjectCredentialsOnly", "false");
    }

    public LDAPInitialDirContextFactoryImpl()
    {
        super();
    }

    public void setInitialDirContextEnvironment(Map<String, String> initialDirContextEnvironment)

    {
        this.initialDirContextEnvironment = initialDirContextEnvironment;
    }

    public Map<String, String> getInitialDirContextEnvironment()
    {
        return initialDirContextEnvironment;
    }

    public InitialDirContext getDefaultIntialDirContext() throws AuthenticationException
    {
        Hashtable<String, String> env = new Hashtable<String, String>(initialDirContextEnvironment.size());
        env.putAll(initialDirContextEnvironment);
        env.put("javax.security.auth.useSubjectCredsOnly", "false");
        return buildInitialDirContext(env);
    }

    private InitialDirContext buildInitialDirContext(Hashtable<String, String> env) throws AuthenticationException
    {
        try
        {
            return new InitialDirContext(env);
        }
        catch (javax.naming.AuthenticationException ax)
        {
            throw new AuthenticationException("LDAP authentication failed.", ax);
        }
        catch (NamingException nx)
        {
            throw new AuthenticationException("Unable to connect to LDAP Server; check LDAP configuration", nx);
        }
    }

    public InitialDirContext getInitialDirContext(String principal, String credentials) throws AuthenticationException
    {
        if (principal == null)
        {
            throw new AuthenticationException("Null user name provided.");
        }
        
        if (principal.length() == 0)
        {
            throw new AuthenticationException("Empty user name provided.");
        }

        if (credentials == null)
        {
            throw new AuthenticationException("No credentials provided.");
        }
        
        if (credentials.length() == 0)
        {
            throw new AuthenticationException("Empty credentials provided.");
        }
        
        Hashtable<String, String> env = new Hashtable<String, String>(initialDirContextEnvironment.size());
        env.putAll(initialDirContextEnvironment);
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, credentials);

        return buildInitialDirContext(env);
    }

    public static void main(String[] args)
    {
        // ....build a pyramid selling scheme .....

        // A group has three user members and 2 group members .... and off we go ....
        // We make the people and groups to represent this and stick them into LDAP ...used to populate a test data base for user and groups

        int userMembers = Integer.parseInt(args[3]);

        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
        LDAPInitialDirContextFactory factory = (LDAPInitialDirContextFactory) applicationContext
                .getBean("ldapInitialDirContextFactory");

        InitialDirContext ctx = null;
        try
        {
            ctx = factory.getInitialDirContext("cn=" + args[0] + "," + args[2], args[1]);

            /* Values we'll use in creating the entry */
            Attribute objClasses = new BasicAttribute("objectclass");
            objClasses.add("top");
            objClasses.add("person");
            objClasses.add("organizationalPerson");
            objClasses.add("inetOrgPerson");

            for (int i = 0; i < userMembers; i++)
            {

                Attribute cn = new BasicAttribute("cn", "User" + i + " TestUser");
                Attribute sn = new BasicAttribute("sn", "TestUser");
                Attribute givenNames = new BasicAttribute("givenName", "User" + i);
                Attribute telephoneNumber = new BasicAttribute("telephoneNumber", "123");
                Attribute uid = new BasicAttribute("uid", "User" + i);
                Attribute mail = new BasicAttribute("mail", "woof@woof");
                Attribute o = new BasicAttribute("o", "Alfresco");
                Attribute userPassword = new BasicAttribute("userPassword", "bobbins");
                /* Specify the DN we're adding */
                String dn = "cn=User" + i + " TestUser," + args[2];

                Attributes orig = new BasicAttributes();
                orig.put(objClasses);
                orig.put(cn);
                orig.put(sn);
                orig.put(givenNames);
                orig.put(telephoneNumber);
                orig.put(uid);
                orig.put(mail);
                orig.put(o);
                orig.put(userPassword);

                try
                {
                    ctx.destroySubcontext(dn);
                }
                catch (NamingException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                ctx.createSubcontext(dn, orig);
            }

        }
        catch (NamingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

                    e.printStackTrace();
                }
            }
        }

    }

    public void afterPropertiesSet() throws Exception
    {
        // Check Anonymous bind
        
        Hashtable<String, String> env = new Hashtable<String, String>(initialDirContextEnvironment.size());
        env.putAll(initialDirContextEnvironment);
        env.remove(Context.SECURITY_PRINCIPAL);
        env.remove(Context.SECURITY_CREDENTIALS);
        try
        {
            new InitialDirContext(env);

            logger.warn("LDAP server supports anonymous bind " + env.get(Context.PROVIDER_URL));
        }
        catch (javax.naming.AuthenticationException ax)
        {

        }
        catch (NamingException nx)
        {
            throw new AuthenticationException("Unable to connect to LDAP Server; check LDAP configuration", nx);
        }
        
        // Simple DN and password
        
        env = new Hashtable<String, String>(initialDirContextEnvironment.size());
        env.putAll(initialDirContextEnvironment);
        env.put(Context.SECURITY_PRINCIPAL, "daftAsABrush");
        env.put(Context.SECURITY_CREDENTIALS, "daftAsABrush");
        try
        {

            new InitialDirContext(env);

            throw new AuthenticationException(
                    "The ldap server at "
                            + env.get(Context.PROVIDER_URL)
                            + " falls back to use anonymous bind if invalid security credentials are presented. This is not supported.");
        }
        catch (javax.naming.AuthenticationException ax)
        {
            logger.info("LDAP server does not fall back to anonymous bind for a string uid and password at " + env.get(Context.PROVIDER_URL));
        }
        catch (NamingException nx)
        {
            logger.info("LDAP server does not support simple string user ids and invalid credentials at "+ env.get(Context.PROVIDER_URL));
        }
        
        // DN and password
        
        env = new Hashtable<String, String>(initialDirContextEnvironment.size());
        env.putAll(initialDirContextEnvironment);
        env.put(Context.SECURITY_PRINCIPAL, "cn=daftAsABrush,dc=woof");
        env.put(Context.SECURITY_CREDENTIALS, "daftAsABrush");
        try
        {

            new InitialDirContext(env);

            throw new AuthenticationException(
                    "The ldap server at "
                            + env.get(Context.PROVIDER_URL)
                            + " falls back to use anonymous bind if invalid security credentials are presented. This is not supported.");
        }
        catch (javax.naming.AuthenticationException ax)
        {
            logger.info("LDAP server does not fall back to anonymous bind for a simple dn and password at " + env.get(Context.PROVIDER_URL));
        }
        catch (NamingException nx)
        {
            logger.info("LDAP server does not support simple DN and invalid password at "+ env.get(Context.PROVIDER_URL));
        }

        // Check more if we have a real principal we expect to work
        
        env = new Hashtable<String, String>(initialDirContextEnvironment.size());
        env.putAll(initialDirContextEnvironment);
        if(env.get(Context.SECURITY_PRINCIPAL) != null)
        {
            // Correct principal invalid password
            
            env = new Hashtable<String, String>(initialDirContextEnvironment.size());
            env.putAll(initialDirContextEnvironment);
            env.put(Context.SECURITY_CREDENTIALS, "sdasdasdasdasd123123123");
            try
            {

                new InitialDirContext(env);

                throw new AuthenticationException(
                        "The ldap server at "
                                + env.get(Context.PROVIDER_URL)
                                + " falls back to use anonymous bind for a known principal if  invalid security credentials are presented. This is not supported.");
            }
            catch (javax.naming.AuthenticationException ax)
            {
                logger.info("LDAP server does not fall back to anonymous bind for known principal and invalid credentials at " + env.get(Context.PROVIDER_URL));
            }
            catch (NamingException nx)
            {
                // already donw
            }
        }
    }
}
