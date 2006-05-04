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
import org.springframework.context.ApplicationContext;

public class LDAPInitialDirContextFactoryImpl implements LDAPInitialDirContextFactory
{
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

        if (credentials == null)
        {
            throw new AuthenticationException("No credentials provided.");
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

}
