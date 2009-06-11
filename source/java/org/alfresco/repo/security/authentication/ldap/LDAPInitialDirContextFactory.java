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

import java.util.Map;

import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.alfresco.repo.security.authentication.AuthenticationException;

/**
 * Interface that defines a factory for obtaining ldap directory contexts.
 * 
 * @author Andy Hind
 */
public interface LDAPInitialDirContextFactory
{
    /**
     * Set the LDAP environment Hashtable properties used ot initialise the LDAP connection.
     * 
     * @param environment
     */
    public void setInitialDirContextEnvironment(Map<String, String> environment);
    
    /**
     * Use the environment properties and connect to the LDAP server, optionally configuring RFC 2696 paged results.
     * Used to obtain read only access to the LDAP server.
     * 
     * @param pageSize
     *            if a positive value, indicates that a LDAP v3 RFC 2696 paged results control should be used. The
     *            results of a search operation should be returned by the LDAP server in batches of the specified size.
     * @return the default intial dir context
     * @throws AuthenticationException
     *             the authentication exception
     */
    public InitialDirContext getDefaultIntialDirContext(int pageSize) throws AuthenticationException;
    
    /**
     * Use the environment properties and connect to the LDAP server.
     * Used to obtain read only access to the LDAP server.
     * 
     * @return
     * @throws AuthenticationException
     */
    public InitialDirContext getDefaultIntialDirContext() throws AuthenticationException;
    
    /**
     * Determines whether there is another page to fetch from the last search to be run in this context. Also prepares
     * the request controls so that the appropriate cookie will be passed in the next search.
     * 
     * @param ctx
     *            the context
     * @param pageSize
     *            if a positive value, indicates that a LDAP v3 RFC 2696 paged results control should be used. The
     *            results of a search operation should be returned by the LDAP server in batches of the specified size.
     * @return true, if is ready for next page
     */
    public boolean hasNextPage(DirContext ctx, int pageSize);    

    /**
     * Augment the connection environment with the identity and credentials and bind to the ldap server.
     * Mainly used to validate a user's credentials during authentication. 
     * 
     * @param principal
     * @param credentials
     * @return
     * @throws AuthenticationException
     */
    public InitialDirContext getInitialDirContext(String principal, String credentials)  throws AuthenticationException;
}
