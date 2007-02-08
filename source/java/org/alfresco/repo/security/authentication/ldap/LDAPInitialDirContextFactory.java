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
package org.alfresco.repo.security.authentication.ldap;

import java.util.Map;

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
     * Use the environment properties and connect to the LDAP server.
     * Used to obtain read only access to the LDAP server.
     * 
     * @return
     * @throws AuthenticationException
     */
    public InitialDirContext getDefaultIntialDirContext() throws AuthenticationException;
    
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
